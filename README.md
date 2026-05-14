# MaaS — Model as a Service

AI 模型网关与管理平台。统一管理和编排多家 AI 提供商（OpenAI、Anthropic、vLLM、Ollama 等），提供 API 密钥管理、安全检测、工作流编排、MCP 服务器集成、用户管理和登录审计等功能。

## 架构

```
Vue 3 Admin Portal (Dashboard | Providers | Models | Keys | MCP | Skills | Tools | Workflows | Security | Dify | Users)
        │
    HTTP/REST (axios → localhost:8080/api)
        │
Spring Boot Backend (Modular Monolith)
  ┌──────────────┬──────────────┬──────────────┬──────────────┐
  │ Gateway      │ Provider     │ API Key      │ Security     │
  │ Router       │ Manager      │ Manager      │ Inspector    │
  ├──────────────┼──────────────┼──────────────┼──────────────┤
  │ MCP Config   │ Skills/Tools │ Workflow     │ Usage        │
  │ Manager      │ Registry     │ Engine       │ Monitor      │
  ├──────────────┼──────────────┼──────────────┼──────────────┤
  │ User & Auth  │ Login Log    │ Dify         │ Model        │
  │ Management   │ Audit        │ Integration  │ Management   │
  └──────────────┴──────────────┴──────────────┴──────────────┘
        │
    PostgreSQL (Flyway migrations V1-V10)
        │
    AI Providers (OpenAI, Anthropic, vLLM, Ollama, Custom)
```

## 功能模块

| 模块 | 说明 |
|------|------|
| **Provider Manager** | 管理 AI 提供商连接，自动健康检测（60s 周期），模型列表自动同步 |
| **Gateway Router** | OpenAI 兼容 API 网关 (`/v1/chat/completions`)，支持自动路由和手动路由 |
| **API Key Manager** | 层次化密钥体系（root→team→application），SHA-256 哈希存储，速率限制 |
| **Security Inspector** | 提示注入检测 + 密钥泄露检测，可配置规则引擎（拦截/标记/审计） |
| **MCP Config Manager** | MCP 服务器管理，支持 stdio 和 SSE 传输协议，自动发现工具 |
| **Skills & Tools Registry** | 技能和工具定义注册中心，带版本生命周期管理 |
| **Workflow Engine** | DAG 多步骤工作流引擎（AI 调用、工具、技能、条件、循环），模板变量插值 |
| **Usage Monitor** | Token 用量统计，成本追踪，趋势排行 |
| **User & Auth** | 用户名/密码登录，JWT 令牌，角色权限（admin/viewer），用户 CRUD 管理 |
| **Login Log** | 登录尝试记录（成功/失败），仪表盘统计和最近活动展示 |
| **Model Management** | 模型定义管理，自动同步提供商模型，启用/停用控制 |
| **Dify Integration** | Dify 连接管理，OAuth 2.0 授权码 SSO 免登录跳转 |

## 技术栈

| 层 | 技术 |
|------|---------|
| 后端 | Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA |
| 前端 | Vue 3 (Composition API), TypeScript, Vite 5, Pinia, Vue Router 4 |
| UI | 自研设计系统（CSS 变量 + 基础组件库），无外部 UI 框架依赖 |
| 数据库 | PostgreSQL 16, Flyway (数据库迁移) |
| 认证 | JWT (jjwt 0.12.5), BCrypt |
| 国际化 | vue-i18n (zh-CN / en)，侧栏一键切换 |
| 工作流 | @vue-flow/core (DAG 可视化编辑器) |

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 16+

### 1. 创建数据库

```sql
CREATE DATABASE maas;
CREATE USER maas WITH PASSWORD 'maas';
GRANT ALL PRIVILEGES ON DATABASE maas TO maas;
```

### 2. 启动后端

```bash
cd maas-backend
# 可选：设置加密密钥（默认仅用于开发）
export MAAS_ENCRYPTION_KEY=your-aes-256-key
export MAAS_JWT_SECRET=your-jwt-secret-at-least-256-bits

mvn spring-boot:run
```

启动后 Flyway 自动执行迁移，DataSeeder 创建默认账号：
- 管理员: `admin` / `admin123`
- 普通用户: `viewer` / `viewer123`

### 3. 启动前端

```bash
cd maas-frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，API 请求自动代理到 `http://localhost:8080`。

### 4. 访问

打开 `http://localhost:5173` 进入管理界面。

## 项目结构

```
maas-backend/
  src/main/java/com/maas/
    apikey/          # API 密钥管理
    common/          # 公共工具（ApiResponse, EncryptionUtil, GlobalExceptionHandler）
    dify/            # Dify 集成 + OAuth 2.0 SSO
    gateway/         # OpenAI 兼容 API 网关
    log/             # 登录日志记录和统计
    mcp/             # MCP 服务器管理
    monitor/         # 用量监控
    provider/        # AI 提供商管理（适配器模式：OpenAI、Anthropic、vLLM、Ollama）
    registry/        # 技能和工具注册中心
    security/        # 安全检测
    user/            # 用户认证和用户管理（admin/viewer 角色）
    workflow/        # 工作流引擎
  src/main/resources/
    db/migration/    # Flyway SQL 迁移（V1-V10）
    application.yml  # 配置文件

maas-frontend/
  src/
    api/             # API 客户端模块（按领域拆分）
    components/
      icons/         # 18 个内联 SVG 图标组件
      ui/            # 基础 UI 组件库（BaseButton, BaseCard, BaseModal 等）
      workflow/      # 工作流编辑器节点组件
    composables/     # Vue 组合式函数（useToast, useConfirm）
    locales/         # 翻译文件（en.ts, zh.ts）
    router/          # 路由配置（含 JWT 角色守卫）
    styles/          # 设计系统（variables.css, base.css, utilities.css）
    types/           # TypeScript 类型定义
    views/           # 页面组件（~25 个视图）
```

## API 概览

### 认证

- `POST /api/auth/login` — 登录，返回 JWT（记录登录日志）
- `GET /api/auth/me` — 获取当前用户信息

### 用户管理（需 admin 角色）

- `GET /api/users` — 用户列表
- `POST /api/users` — 创建用户
- `PUT /api/users/{id}` — 更新用户
- `DELETE /api/users/{id}` — 删除用户

### 提供商

- `GET /api/providers` — 列表
- `POST /api/providers` — 创建
- `GET /api/providers/{id}` — 详情
- `PUT /api/providers/{id}` — 更新
- `DELETE /api/providers/{id}` — 删除
- `POST /api/providers/{id}/test` — 测试连接
- `POST /api/providers/{id}/health` — 刷新健康状态

### 模型管理

- `GET /api/models` — 模型列表
- `POST /api/models` — 添加模型
- `PUT /api/models/{id}` — 更新模型
- `DELETE /api/models/{id}` — 删除模型

### API 密钥

- `GET /api/keys` — 密钥列表
- `POST /api/keys` — 创建密钥
- `POST /api/keys/{id}/revoke` — 撤销密钥
- `DELETE /api/keys/{id}` — 删除密钥

### 网关

- `POST /v1/chat/completions` — OpenAI 兼容的聊天补全
- `GET /v1/models` — 可用模型列表

### 安全

- `GET /api/security/rules` — 规则列表
- `POST /api/security/rules` — 创建规则
- `PUT /api/security/rules/{id}` — 更新规则
- `DELETE /api/security/rules/{id}` — 删除规则
- `GET /api/security/events` — 安全事件（分页）
- `GET /api/security/events/stats` — 安全事件统计

### 日志

- `GET /api/logs/login/recent` — 最近登录记录
- `GET /api/logs/login/stats` — 登录统计（今日成功/失败/24h 总数）

### Dify / OAuth 2.0

- `GET /api/dify` — Dify 连接列表
- `POST /api/dify` — 创建连接
- `PUT /api/dify/{id}` — 更新连接
- `DELETE /api/dify/{id}` — 删除连接
- `POST /api/dify/{id}/test` — 测试连接
- `GET /api/dify/{id}/sso` — 直接 SSO 跳转
- `POST /api/oauth2/authorize` — OAuth 授权（需 JWT）
- `GET /api/oauth2/callback` — OAuth 回调（SSO 跳转页）

### 工作流

- `GET /api/workflows` — 工作流列表
- `POST /api/workflows` — 创建工作流
- `GET /api/workflows/{id}` — 工作流详情
- `PUT /api/workflows/{id}` — 更新工作流
- `DELETE /api/workflows/{id}` — 删除工作流
- `POST /api/workflows/{id}/publish` — 发布工作流
- `GET /api/workflows/{id}/versions` — 版本历史
- `POST /api/workflows/{id}/execute` — 执行工作流
- `GET /api/workflows/{id}/executions` — 执行记录（分页）
- `GET /api/executions` — 全局执行记录
- `GET /api/executions/trends` — 执行趋势（7 天）

## 前端设计系统

前端基于 CSS 变量构建了完整的设计系统，无需外部 UI 框架。

### 设计令牌

```
--color-primary: #6366F1     (Indigo 主色)
--color-success: #10b981     (翠绿)
--color-danger: #ef4444      (红色)
--color-warning: #f59e0b     (琥珀)
--sidebar-bg: #0f172a        (深蓝黑侧栏)
```

### 基础组件库

```
BaseButton.vue      — 按钮（primary/danger/ghost variant）
BaseBadge.vue       — 徽章（success/danger/warning/info/neutral）
BaseCard.vue        — 卡片容器（header/body/footer slots）
BaseModal.vue       — 模态对话框（Promise-based API）
BasePageHeader.vue  — 页面标题 + actions 区域
BaseSpinner.vue     — CSS 加载动画
BaseEmpty.vue       — 空状态占位
BasePagination.vue  — 分页组件
BaseFormField.vue   — 表单字段包装
```

### 图标

18 个内联 SVG 图标组件，无外部图标库依赖。

## 配置说明

| 环境变量 | 默认值 | 说明 |
|-----------|---------|------|
| `MAAS_ENCRYPTION_KEY` | `change-me-in-production` | AES 加密密钥 |
| `MAAS_JWT_SECRET` | 内置 256-bit 密钥 | JWT 签名密钥 |

数据库连接配置在 `application.yml` 中修改。

## License

MIT
