# MaaS 平台设计文档

> 面向内部团队的 AI 能力管理平台，涵盖模型接入、MCP 配置、Skill/Tool 注册及工作流编排。

## 1. 概述

### 1.1 目标

构建一个内部统一的 AI 管理平台（MaaS），解决团队在使用 AI 能力时面临的多供应商接入分散、API 密钥管理混乱、AI 能力复用困难等问题，提供模型网关、插件注册、工作流编排等一体化能力。

### 1.2 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java (Spring Boot) |
| 前端 | Vue.js |
| 数据库 | MySQL / PostgreSQL |

### 1.3 核心原则

- **OpenAI API 兼容** — Gateway 对外暴露兼容 OpenAI 的接口，现有工具和框架零改动接入
- **轻量可演进** — 优先单体部署，模块化内部架构，未来可拆分微服务
- **自服务优先** — 团队可通过 Portal 自助管理，减少后端依赖
- **最小权限** — Key 级权限隔离，每个应用/项目只拥有所需的最小模型访问权限

## 2. 整体架构

```
┌──────────────────────────────────────────────────────┐
│                  Vue Admin Portal                      │
│  Dashboard │ Keys │ Providers │ MCP │ Skills │ Workflows │ Scheduler │ Security│
└────────────────────────┬─────────────────────────────┘
                         │ HTTP/REST
┌────────────────────────▼─────────────────────────────┐
│              Spring Boot Backend                       │
│                                                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Gateway      │  │ Security     │  │ Provider     │  │
│  │ Router       │  │ Inspector    │  │ Manager      │  │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤  │
│  │ MCP Config   │  │ Skills/Tools │  │ Workflow     │  │
│  │ Manager      │  │ Registry     │  │ Engine       │  │
│  ├──────────────┤  ├──────────────┤  ├──────────────┤  │
│  │ Scheduled   │  │ Usage        │  │              │  │
│  │ Task Mgr     │  │ Monitor      │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Database (MySQL/PostgreSQL)          │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────┬─────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────┐
│              AI Provider Layer                         │
│  OpenAI │ Claude │ vLLM │ Ollama │ 自定义 ...         │
└──────────────────────────────────────────────────────┘
```

## 3. 模块设计

### 3.1 Provider Manager —— 提供商管理

统一管理所有 AI 提供商的注册和连接配置。

**支持的提供商类型：**

| 类型 | 说明 | 关键配置 |
|------|------|----------|
| `openai-compatible` | OpenAI 兼容 API | api-key, base-url |
| `anthropic` | Claude 原生协议 | api-key, api-version |
| `vllm` | 自托管 vLLM | endpoint, api-key(可选) |
| `ollama` | 本地 Ollama | endpoint |
| `custom` | 自定义协议 | 适配器插件 |

**能力：**
- 提供商注册（名称、类型、连接参数、网络区域标签）
- 模型列表自动同步（定期拉取 `/v1/models`）
- 健康检查与自动熔断（连续失败 N 次后摘除，恢复后重新加入）
- 启用/停用控制

### 3.2 Gateway Router —— 统一模型网关

所有 AI 调用的统一入口，兼容 OpenAI API 协议。

**对外接口：**

| 端点 | 说明 |
|------|------|
| `POST /v1/chat/completions` | 对话补全（SSE 流式支持） |
| `POST /v1/embeddings` | 向量嵌入 |
| `GET /v1/models` | 可用模型列表 |

**请求处理流程：**

```
请求 → API Key 鉴权 → Security Inspection → Rate Limiter → 路由选择 → Provider 调用 → Security Inspection → 响应
                                                                 ↓
                                                            用量记录 → DB
```

**安全检测拦截点：**

安全检测在请求链路中有两个插入点：
- **请求入站拦截**（Request Inspection）— AI 请求到达后、转发给提供商之前，检测 prompt 注入、敏感内容、PII 等
- **响应出站拦截**（Response Inspection）— AI 响应返回后、返回给调用方之前，检测有害内容、数据泄露等

检测到违规时，根据策略配置执行：`block`（拦截并返回错误）/ `flag`（放行但记录告警）/ `audit`（仅记录）。

**路由策略：**
- **手动路由**：请求 header `X-Maas-Provider` 指定目标提供商
- **自动路由**：根据请求 body 中的 `model` 字段自动映射到已注册的提供商
- **Fallback 链**：主提供商失败时按配置顺序降级（如 claude → openai → vllm）

**API Key 鉴权：**
- 请求通过 `Authorization: Bearer <maas-key>` 传递
- 鉴权同时验证：Key 是否有效、是否允许调用该 model、是否超额度

### 3.3 API Key Manager —— 密钥管理

分层密钥体系，细粒度权限控制。

**层级结构：**

```
Platform Root Key        — 平台管理员
  └── Team Key           — 团队管理员（可管理本团队的子 Key）
       └── Application Key — 接入应用的最终 Key（最小权限）
```

**每个 Key 可绑定的策略：**
- 允许调用的模型列表
- 每分钟/每天的速率上限
- 月度预算额度（USD）
- 生效时段
- 关联标签（用于成本分摊）

**安全能力：**
- 密钥自动轮换
- 密钥掩码展示（只显示末 8 位）
- 废弃密钥可追溯但不可用
- 调用来源 IP 白名单（可选）

### 3.4 MCP Config Manager —— MCP 配置管理

轻量级的 MCP Server 连接配置中心。

**数据结构：**

```
MCP Server 记录
├── id, name, description
├── transport: "stdio" | "sse"
├── command (stdio): npx/node/path + args
├── url (sse): 端点地址
├── env_vars: [{ key, value, is_secret }]  ← 环境变量（敏感值加密）
├── skills: [关联 Skill ID]
├── tools: [{ name, description, schema }] ← 自动注册的 Tool 清单
├── status: online / offline / error
└── version, last_heartbeat
```

**功能：**
- MCP Server 注册与配置管理
- 连接测试与健康探测
- 自动解析 MCP Server 暴露的 Tools 清单
- 按环境管理不同的配置集（dev/staging/prod）
- 导出为 MCP 客户端兼容格式

### 3.5 Skills & Tools Registry —— 插件注册中心

统一的 Skill/Tool 定义和版本管理，方便团队发现和复用 AI 能力。

**Skill 定义：**

```
{
  "name": "code-reviewer",
  "version": "1.2.0",
  "description": "代码审查助手",
  "provider": "claude",
  "model": "claude-sonnet-4-6",
  "system_prompt": "你是一个资深代码审查者...",
  "tools": ["search-codebase", "git-diff"],
  "mcp_servers": ["mcp-github"],
  "tags": ["dev", "review"],
  "status": "published"  // draft | published | deprecated | retired
}
```

**Tool 定义：**

```
{
  "name": "search-codebase",
  "version": "1.0.0",
  "source": "built-in" | "mcp" | "api",
  "schema": { /* JSON Schema 格式的参数定义 */ },
  "endpoint": { /* api 类型时：方法、URL、鉴权方式 */ },
  "mcp_server": { /* mcp 类型时：关联的 MCP Server */ }
}
```

**生命周期管理：**
- 状态流转：`draft → published → deprecated → retired`
- 版本号语义化（SemVer）
- 已发布的版本不可变，修改后升版本
- Tag 分类组织，支持模糊搜索

### 3.6 Workflow Engine —— 工作流管理

将 AI 调用、MCP Tool、Skill、代码逻辑编排成可执行的多步流程。

**工作流定义：**

```
{
  "name": "代码审查自动评论",
  "version": "1.0.0",
  "trigger": "manual" | "api" | "webhook" | "schedule",
  "variables": [
    { "name": "pr_url", "type": "string", "required": true }
  ],
  "steps": [
    {
      "id": "step-1",
      "type": "ai_call",
      "name": "AI 审查代码",
      "config": {
        "provider": "claude",
        "model": "claude-sonnet-4-6",
        "prompt": "请审查以下 PR 的代码变更：{{variables.pr_url}}"
      },
      "retry": { "max_attempts": 2, "backoff": "exponential" },
      "on_failure": "stop"
    },
    {
      "id": "step-2",
      "type": "tool",
      "name": "提交评论到 PR",
      "config": {
        "tool_id": "mcp-github-create-comment",
        "input": {
          "pr_url": "{{variables.pr_url}}",
          "comment": "{{steps.step-1.output}}"
        }
      },
      "on_failure": "skip"
    }
  ],
  "outputs": {
    "review_result": "{{steps.step-1.output}}"
  }
}
```

**支持的 Step 类型：**

| 类型 | 功能 | 适用场景 |
|------|------|----------|
| `ai_call` | 调用任意 AI 模型 | 核心 AI 推理 |
| `tool` | 执行已注册的 Tool | 对接外部系统 |
| `skill` | 执行已发布的 Skill | 复用预定义能力 |
| `condition` | 条件分支 | if/else 判断 |
| `loop` | 循环迭代 | 批量处理 |
| `code` | 执行脚本片段 | 数据转换（Python/JS） |
| `human_approval` | 人工审批节点 | 敏感操作确认 |
| `parallel` | 并行执行 | 多路同时处理 |

**运行时特性：**
- 可视化 DAG 状态跟踪
- 每步独立记录输入/输出/耗时
- 变量模板引擎（`{{...}}` 语法，上步输出映射到下一步输入）
- 失败重试、超时控制、熔断
- 版本化管理（发布后生成快照，不可变更）

**执行模型：**

```
触发执行
  │
  ▼
创建 Execution 记录
  │
  ▼
按 DAG 顺序执行 Steps ─── 每步写入 StepExecution 日志
  │                            │
  ▼                            ▼
完成 / 失败              Portal 实时查看进度
```

### 3.7 Usage Monitor —— 用量与监控

**数据采集：**

每次 AI 调用记录以下信息：
- 时间戳、API Key ID、应用标签
- 提供商、模型、请求内容摘要（可选）
- Token 消耗（prompt + completion）
- 响应延迟（总耗时、TTFT）
- 预估成本
- 状态（成功/失败/超限/超时）

**Portal 展示：**

| 视图 | 内容 |
|------|------|
| 全局仪表盘 | 今日/本周/本月调用量、Token、花费趋势 |
| 应用排行 | 按 API Key 维度的用量排行榜 |
| 模型排行 | 各模型调用量和成本分布 |
| 提供商健康 | 各提供商的成功率和平均延迟 |
| 预算告警 | Key 级月度预算阈值，超限自动通知 |

### 3.8 Scheduled Task Manager —— 调度任务管理

基于定时表达式的工作流调度引擎，负责在指定时间自动触发工作流执行。

**调度方式：**

| 方式 | 说明 | 配置示例 |
|------|------|----------|
| **Cron 表达式** | 按 cron 周期执行 | `0 9 * * 1-5`（每个工作日 9:00） |
| **固定间隔** | 每隔 N 分钟/小时执行 | `间隔 30 分钟` |
| **一次性调度** | 指定时间执行一次 | `2026-06-01 10:00:00` |
| **日历调度** | 按日历日期/排除节假日 | `每月 1 号，排除周末` |
| **Cron 驱动** | 监听外部系统事件触发 | 通过 Webhook 接收事件后触发 |

**调度任务定义：**

```
{
  "id": "sched-001",
  "name": "每日代码质量报告",
  "description": "每天早上 9 点生成代码质量报告并推送到飞书",
  "enabled": true,
  "schedule": {
    "type": "cron",           // cron | interval | once | calendar
    "expression": "0 9 * * 1-5",  // cron 表达式
    "timezone": "Asia/Shanghai",
    "start_at": "2026-05-10 00:00:00",
    "end_at": null,            // 可选：调度截止时间
    "max_executions": null     // 可选：最大执行次数
  },
  "target": {
    "type": "workflow",       // workflow | skill | ai_call
    "workflow_id": "wf-003",
    "workflow_version": "latest",  // latest | 具体版本号
    "input": {
      "project": "maas",
      "report_type": "daily"
    }
  },
  "notifications": [
    { "type": "on_failure", "channel": "feishu", "webhook_url": "..." },
    { "type": "on_success", "channel": "email", "to": ["team@example.com"] }
  ],
  "concurrency": {
    "max_active_runs": 1,     // 最大同时运行数，防止重叠
    "skip_if_running": true   // 若上次未完成则跳过本次
  }
}
```

**调度引擎架构：**

```
Spring TaskScheduler (本地)
  └── 轻量级调度，适合单节点
  └── 服务重启后调度丢失 → 启动时从 DB 恢复

可选：分布式调度（未来演进）
  └── Quartz / XXL-Job
  └── 支持集群部署、分片执行
```

**核心功能：**
- 调度任务 CRUD（启用/停用/立即触发一次）
- 执行历史查看（每次调度触发的执行记录）
- 防重叠策略（skip_if_running / queue）
- 失败自动重试（调度级重试，与工作流内部的重试独立）
- 通知与告警（执行成功/失败时通过 Webhook/邮件/即时通讯通知）
- 日历排除（节假日不执行）
- 调度统计（准时率、平均执行耗时、失败率）

**执行流程：**

```
Spring TaskScheduler
  │  到达触发时间
  ▼
检查 concurrency 策略 → 跳过 或 排队
  │
  ▼
创建 Workflow Execution（trigger = "scheduler"）
  │
  ▼
绑定 sched_task_execution 记录
  │
  ▼
异步执行工作流
  │
  ▼
完成 → 通知 on_success
失败 → 重试策略判断 → 最终失败 → 通知 on_failure
```

**数据库模型补充：**

- `scheduled_task` (id, name, description, schedule_config_json, target_config_json, notification_config_json, concurrency_config_json, enabled, status, created_by, created_at, updated_at)
- `sched_task_execution` (id, task_id, scheduled_time, started_at, finished_at, workflow_execution_id, status, error_message)

**与 Workflow Engine 的关系：**

Scheduled Task Manager 是 Workflow Engine 的上层调度层。Workflow Engine 定义"做什么"，Scheduled Task Manager 定义"什么时候做"。一个调度任务绑定一个工作流（或 Skill/ai_call），在其触发时自动发起一次执行。

### 3.9 Security Inspector —— 安全检测与拦截

AI 请求和响应的安全检测中间件，保护平台免受滥用和数据泄露。

**检测引擎架构：**

```
                         ┌─────────────────────┐
                         │   Security Rules     │
                         │  (配置中心管理)       │
                         └──────────┬──────────┘
                                    │ 加载
┌──────┐    ┌───────────────────────▼──────────────────────┐   ┌──────┐
│Client│───►│  Security Inspector (Plugin Chain)           │──►│Provider│
│      │    │  ┌──────────┐ ┌──────────┐ ┌──────────────┐ │   │      │
│      │    │  │ Prompt   │ │ PII /    │ │ Custom       │ │   │      │
│      │    │  │ Injection│ │ Secret   │ │ Keyword      │ │   │      │
│      │    │  │ Detector │ │ Detector │ │ Filter       │ │   │      │
│      │    │  └──────────┘ └──────────┘ └──────────────┘ │   │      │
│      │    │  ┌──────────┐ ┌──────────┐ ┌──────────────┐ │   │      │
│      │    │  │ Response │ │ Toxicity │ │ Data Leakage │ │   │      │
│      │    │  │ Scanner  │ │ Detector │ │ Detector     │ │   │      │
│      │    │  └──────────┘ └──────────┘ └──────────────┘ │   │      │
│      │    │  ┌────────────────────────────────────────┐ │   │      │
│      │    │  │  AI-Powered Guard (可选)               │ │   │      │
│      │    │  │  用 AI 检测 AI 输出安全              │ │   │      │
│      │    │  └────────────────────────────────────────┘ │   │      │
│      │    └──────────────────────┬──────────────────────┘   │      │
│      │                           │                          │      │
│      │                   ┌───────▼───────┐                  │      │
│      │                   │ Security Log  │                  │      │
│      │                   │ & Alert       │                  │      │
│      │                   └───────────────┘                  │      │
└──────┘                                                     └──────┘
```

**检测能力：**

| 检测器 | 检测维度 | 扫描方向 | 说明 |
|--------|----------|----------|------|
| **Prompt 注入检测** | 越狱提示词、角色注入、指令劫持 | 请求入站 | 基于规则 + ML 模型识别常见攻击模式 |
| **敏感内容过滤** | 关键词、正则表达式、自定义规则 | 请求 + 响应 | 支持黑名单/白名单，按模型/Key 差异化配置 |
| **PII 检测** | 身份证、手机号、邮箱、银行卡等 | 请求 + 响应 | 基于正则 + NLP 实体识别，支持脱敏替换 |
| **Secret 泄露检测** | API Key、Token、密码、AK/SK | 请求 + 响应 | 检测类 `sk-*`、`AKIA*` 等模式，防止密钥外泄 |
| **有害内容检测** | 色情、暴力、仇恨言论等 | 响应出站 | 分类模型对模型输出进行安全评分 |
| **数据泄露防护** | 代码库匹配、文档相似度 | 响应出站 | 检测模型输出是否包含受保护的源码或文档段落 |
| **AI Guard** | 综合安全评分 | 响应出站 | 可选 AI 模型二次审核（用另一个模型审查输出） |

**检测策略配置：**

每条策略定义：

```
{
  "id": "rule-001",
  "name": "阻止 Prompt 注入",
  "enabled": true,
  "scope": {
    "api_keys": ["all"],          // 应用于哪些 Key
    "models": ["all"],            // 应用于哪些模型
    "direction": "request"        // request | response | both
  },
  "detector": "prompt-injection",
  "severity": "high",             // low | medium | high | critical
  "action": "block",              // block | flag | audit
  "threshold": 0.85,              // 判定阈值 (0-1)
  "response_on_block": {
    "status": 403,
    "message": "请求因安全策略被拦截"
  }
}
```

**严重级别与响应动作：**

| 严重级别 | 典型场景 | 默认动作 |
|----------|----------|----------|
| `low` | 轻微匹配，可能是误报 | `audit`（仅记录） |
| `medium` | 可疑内容 | `flag`（放行但告警） |
| `high` | 确定的违规内容 | `block`（拦截） |
| `critical` | Secret 泄露、注入攻击 | `block` + 即时通知管理员 |

**安全事件与告警：**

- 所有检测结果写入 `security_event` 表，支持检索和导出
- 支持配置 Webhook 通知（飞书/钉钉/邮件）
- 支持聚合告警（同一来源频繁触发时合并通知）
- 安全仪表盘展示拦截趋势、Top 违规来源、常见违规类型

**安全仪表盘（Portal）：**

| 视图 | 内容 |
|------|------|
| 安全总览 | 今日拦截数、拦截率、高危事件数 |
| 拦截趋势 | 按时间维度的拦截量折线图 |
| Top 违规来源 | 按 API Key/应用统计的违规排行 |
| 策略命中分布 | 各检测器命中占比（注入/PII/Secret 等） |
| 事件详情 | 安全事件列表，支持查看原文和上下文 |

## 4. 数据库模型概要

**Provider 相关：**
- `provider` (id, name, type, config_json, status, health_status, created_at, updated_at)
- `provider_model` (id, provider_id, model_name, capabilities, status)

**Key 相关：**
- `api_key` (id, key_hash, name, key_type, policy_json, created_by, expires_at, status)
- `key_policy` (id, key_id, allowed_models, rate_limit, monthly_budget, ip_whitelist)

**MCP 相关：**
- `mcp_server` (id, name, transport, command, url, env_config_json, status, version)
- `mcp_tool` (id, server_id, name, description, schema_json)

**Skill/Tool 相关：**
- `skill` (id, name, version, description, config_json, status, tags)
- `tool_definition` (id, name, version, source, schema_json, config_json, status)

**Workflow 相关：**
- `workflow` (id, name, description, status)
- `workflow_version` (id, workflow_id, version, definition_json, status, created_at)
- `execution` (id, workflow_version_id, trigger, status, started_at, finished_at)
- `step_execution` (id, execution_id, step_id, type, input, output, status, started_at, finished_at, error_message)

**Usage 相关：**
- `usage_record` (id, api_key_id, provider_id, model, prompt_tokens, completion_tokens, latency_ms, cost, status, created_at)

**Scheduler 相关：**
- `scheduled_task` (id, name, description, schedule_config_json, target_config_json, notification_config_json, concurrency_config_json, enabled, status, created_by, created_at, updated_at)
- `sched_task_execution` (id, task_id, scheduled_time, started_at, finished_at, workflow_execution_id, status, error_message)

**Security 相关：**
- `security_rule` (id, name, detector_type, config_json, scope_json, severity, action, threshold, enabled, created_at, updated_at)
- `security_rule_set` (id, name, description, rules_json, assigned_to, status, created_at)
- `security_event` (id, rule_id, api_key_id, provider, model, direction, request_summary, matched_content_hmac, severity, action_taken, created_at)
- `security_alert` (id, rule_id, event_ids, aggregate_count, severity, status, notified_at, created_at)

## 5. 页面结构（Vue Router）

| 路径 | 页面 | 模块归属 |
|------|------|----------|
| `/` | 仪表盘（总览指标、趋势图） | Monitor |
| `/keys` | API Key 管理（列表/创建/编辑） | Key Manager |
| `/providers` | 提供商列表（配置/健康状态） | Provider Manager |
| `/models` | 全局模型视图 | Gateway |
| `/mcp` | MCP Server 管理 | MCP Manager |
| `/skills` | Skill 定义与版本管理 | Registry |
| `/tools` | Tool 注册中心 | Registry |
| `/workflows` | 工作流列表 | Workflow Engine |
| `/workflows/:id` | 工作流详情与执行历史 | Workflow Engine |
| `/workflows/:id/edit` | 工作流编辑器 | Workflow Engine |
| `/executions` | 执行记录列表 | Workflow Engine |
| `/executions/:id` | 执行详情与 DAG 状态 | Workflow Engine |
| `/logs` | 调用日志查询 | Monitor |
| `/scheduler` | 调度任务列表 | Scheduled Task |
| `/scheduler/:id` | 调度任务详情与执行历史 | Scheduled Task |
| `/scheduler/:id/edit` | 调度任务编辑 | Scheduled Task |
| `/security` | 安全总览仪表盘 | Security Inspector |
| `/security/rules` | 安全规则管理 | Security Inspector |
| `/security/events` | 安全事件列表与详情 | Security Inspector |
| `/settings` | 系统配置 | System |

## 6. 模块依赖关系

```
Provider Manager     ← 独立，无依赖
       │
       ▼
Gateway Router      ← 依赖 Provider（路由目标）
Security Inspector  ← 依赖 Gateway（嵌入请求链路）、API Key Manager（按 Key 应用策略）
API Key Manager     ← 独立，可单独使用
       │
       ▼
Usage Monitor       ← 依赖 Gateway（数据源）
       │
       ├──── MCP Config Manager  ← 独立，可选集成 Gateway（MCP Tool 可作为路由目标）
       │
       ├──── Skills/Tools Registry ← 依赖 Provider、MCP（Skill/Tool 引用底层能力）
       │
       └──── Workflow Engine     ← 依赖 Provider、MCP、Registry（编排所有底层能力）
       │
       └──── Scheduled Task Mgr ← 依赖 Workflow Engine（调度触发工作流执行）
```

## 7. 实施优先级

| 阶段 | 模块 | 价值 |
|------|------|------|
| **P0** | Provider Manager + Gateway Router + API Key Manager | 解决核心痛点：统一模型入口与密钥管理 |
| **P1** | Security Inspector（基础规则引擎 + Prompt 注入检测 + Secret 泄露检测）| 安全防护基线 |
| **P1** | Usage Monitor | 成本可见性 |
| **P1** | MCP Config Manager | 团队 MCP 资产统一管理 |
| **P1** | Skills/Tools Registry + Workflow Engine | 能力复用与流程编排 |
| **P2** | Security Inspector（AI Guard + 高级检测器）| AI 辅助安全审核 |
| **P2** | Scheduled Task Manager | 定时任务自动触发 |

---

*设计版本：v1.2*
*最后更新：2026-05-11*
