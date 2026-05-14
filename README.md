# MaaS — Model as a Service

AI 模型网关与管理平台。统一管理和编排多家 AI 提供商（OpenAI、Anthropic、vLLM、Ollama 等），提供 API 密钥管理、安全检测、工作流编排、MCP 服务器集成等功能。

## 架构

```
Vue 3 Admin Portal (Dashboard | Providers | Keys | MCP | Skills | Workflows | Security | Dify)
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
  └──────────────┴──────────────┴──────────────┴──────────────┘
        │
    PostgreSQL (17 tables, Flyway migrations)
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
| **Dify Integration** | Dify 连接管理，OAuth 2.0 授权码 SSO 免登录跳转 |
| **User Auth** | 用户名/密码登录，JWT 令牌，角色权限（admin/viewer） |

## 技术栈

| 层 | 技术 |
|------|---------|
| 后端 | Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA |
| 前端 | Vue 3 (Composition API), TypeScript, Vite 5, Pinia, Vue Router 4 |
| 数据库 | PostgreSQL 16, Flyway (数据库迁移) |
| 认证 | JWT (jjwt 0.12.5), BCrypt |
| 国际化 | vue-i18n (zh-CN / en) |
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

启动后 Flyway 自动执行迁移，DataSeeder 创建默认管理员账号：
- 用户名: `admin`
- 密码: `admin123`

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
    mcp/             # MCP 服务器管理
    monitor/         # 用量监控
    provider/        # AI 提供商管理
    registry/        # 技能和工具注册中心
    security/        # 安全检测
    user/            # 用户认证
    workflow/        # 工作流引擎
  src/main/resources/
    db/migration/    # Flyway SQL 迁移（V1-V9）
    application.yml  # 配置文件

maas-frontend/
  src/
    api/             # API 客户端模块
    components/      # 通用组件（Layout, Toast, workflow nodes）
    composables/     # Vue 组合式函数
    i18n/            # 国际化初始化
    locales/         # 翻译文件（en.ts, zh.ts）
    router/          # 路由配置（含权限守卫）
    stores/          # Pinia 状态管理
    types/           # TypeScript 类型定义
    views/           # 页面组件
```

## API 概览

### 认证

- `POST /api/auth/login` — 登录，返回 JWT

### 提供商

- `GET/POST /api/providers` — 列表/创建
- `GET/PUT/DELETE /api/providers/{id}` — 详情/更新/删除
- `POST /api/providers/{id}/test` — 测试连接

### API 密钥

- `GET/POST /api/keys` — 列表/创建
- `POST /api/keys/{id}/revoke` — 撤销

### 网关

- `POST /v1/chat/completions` — OpenAI 兼容的聊天补全
- `GET /v1/models` — 可用模型列表

### 安全

- `GET/POST /api/security/rules` — 规则列表/创建
- `GET /api/security/events` — 安全事件

### Dify / OAuth 2.0

- `GET/POST/PUT/DELETE /api/dify` — Dify 连接配置
- `POST /api/dify/{id}/test` — 测试连接
- `POST /api/dify/{id}/login` — 服务器端登录验证
- `POST /api/oauth2/authorize` — OAuth 授权（需 JWT）
- `GET /api/oauth2/callback` — OAuth 回调（SSO 跳转页）
- `GET /api/dify/{id}/sso` — 直接 SSO 跳转

### 工作流

- `GET/POST /api/workflows` — 工作流列表/创建
- `POST /api/workflows/{id}/execute` — 执行工作流
- `GET /api/executions` — 执行记录

## 配置说明

| 环境变量 | 默认值 | 说明 |
|-----------|---------|------|
| `MAAS_ENCRYPTION_KEY` | `change-me-in-production` | AES 加密密钥 |
| `MAAS_JWT_SECRET` | 内置 256-bit 密钥 | JWT 签名密钥 |

数据库连接配置在 `application.yml` 中修改。

## License

MIT
