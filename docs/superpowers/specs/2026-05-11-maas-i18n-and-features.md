# MaaS 前端 i18n 中英文切换与功能完善

## 目标

1. 所有页面支持中英文切换
2. 补充加载状态、空状态、操作反馈等交互体验
3. 修复 API Key 创建后显示完整密钥的问题

## 方案

### i18n 实现

- 使用 `vue-i18n`（Vue 3 官方国际化方案）
- 自动检测浏览器语言（`navigator.language`），匹配 `zh-CN` 或 `en`
- 用户切换后存入 `localStorage`，下次访问自动恢复
- 侧边栏底部放置语言切换按钮（中/EN 切换）

### 翻译组织

```
src/locales/
  en.ts      — 英文翻译（~80 条 key）
  zh.ts      — 中文翻译（~80 条 key）
```

按页面划分 key 命名空间：
- `nav.xxx` — 导航
- `dashboard.xxx` — 仪表盘
- `provider.xxx` — Provider 管理
- `key.xxx` — API Key 管理
- `model.xxx` — 模型列表
- `common.xxx` — 通用（保存、取消、删除等）
- `toast.xxx` — 操作反馈

### 功能完善

1. **Toast 通知系统** — 轻量级 composable `useToast()`，底部弹出的成功/错误提示
2. **加载状态** — 列表页添加 loading 标志，数据加载中显示"加载中..."
3. **空状态** — 列表为空时显示"暂无数据"提示
4. **API Key 创建** — 后端返回 `rawKey` 字段，前端展示完整密钥（仅创建时一次性展示）
5. **Model 列表** — 调用 ProviderModel 接口展示真实模型数据（暂用 Provider 列表模拟）

## 涉及文件

### 新增
- `src/locales/en.ts`
- `src/locales/zh.ts`
- `src/i18n/index.ts`
- `src/components/Toast.vue`
- `src/composables/useToast.ts`

### 修改
- `package.json` — 添加 vue-i18n 依赖
- `src/main.ts` — 注册 i18n 插件
- `src/types/index.ts` — ApiKey 添加 rawKey 字段
- `src/App.vue` — 添加 Toast 组件
- `src/components/Layout.vue` — 添加语言切换按钮
- `src/views/Dashboard.vue` — i18n + loading
- `src/views/providers/ProviderList.vue` — i18n + loading + empty + toast
- `src/views/providers/ProviderForm.vue` — i18n + toast
- `src/views/keys/KeyList.vue` — i18n + loading + empty + toast
- `src/views/keys/KeyForm.vue` — i18n + raw key 展示 + toast
- `src/views/models/ModelList.vue` — i18n + loading + empty
