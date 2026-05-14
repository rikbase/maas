import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/executions/:id',
      name: 'ExecutionDetail',
      component: () => import('../views/workflows/ExecutionDetail.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/',
      component: () => import('../components/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
        { path: 'providers', name: 'Providers', component: () => import('../views/providers/ProviderList.vue') },
        { path: 'providers/new', name: 'ProviderCreate', component: () => import('../views/providers/ProviderForm.vue'), meta: { requiresAdmin: true } },
        { path: 'providers/:id/edit', name: 'ProviderEdit', component: () => import('../views/providers/ProviderForm.vue'), meta: { requiresAdmin: true } },
        { path: 'keys', name: 'Keys', component: () => import('../views/keys/KeyList.vue') },
        { path: 'keys/new', name: 'KeyCreate', component: () => import('../views/keys/KeyForm.vue'), meta: { requiresAdmin: true } },
        { path: 'models', name: 'Models', component: () => import('../views/models/ModelList.vue') },
        {
          path: 'security/rules',
          name: 'SecurityRules',
          component: () => import('../views/security/RuleList.vue'),
        },
        {
          path: 'security/rules/new',
          name: 'SecurityRuleCreate',
          component: () => import('../views/security/RuleForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'security/rules/:id/edit',
          name: 'SecurityRuleEdit',
          component: () => import('../views/security/RuleForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'security/events',
          name: 'SecurityEvents',
          component: () => import('../views/security/EventList.vue'),
        },
        {
          path: 'mcp/servers',
          name: 'McpServers',
          component: () => import('../views/mcp/ServerList.vue'),
        },
        {
          path: 'mcp/servers/new',
          name: 'McpServerCreate',
          component: () => import('../views/mcp/ServerForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'mcp/servers/:id',
          name: 'McpServerDetail',
          component: () => import('../views/mcp/ServerDetail.vue'),
        },
        {
          path: 'mcp/servers/:id/edit',
          name: 'McpServerEdit',
          component: () => import('../views/mcp/ServerForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'skills',
          name: 'Skills',
          component: () => import('../views/registry/SkillList.vue'),
        },
        {
          path: 'skills/new',
          name: 'SkillCreate',
          component: () => import('../views/registry/SkillForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'skills/:id',
          name: 'SkillDetail',
          component: () => import('../views/registry/SkillDetail.vue'),
        },
        {
          path: 'skills/:id/edit',
          name: 'SkillEdit',
          component: () => import('../views/registry/SkillForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'tools',
          name: 'Tools',
          component: () => import('../views/registry/ToolList.vue'),
        },
        {
          path: 'tools/new',
          name: 'ToolCreate',
          component: () => import('../views/registry/ToolForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'tools/:id',
          name: 'ToolDetail',
          component: () => import('../views/registry/ToolDetail.vue'),
        },
        {
          path: 'tools/:id/edit',
          name: 'ToolEdit',
          component: () => import('../views/registry/ToolForm.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'workflows',
          name: 'Workflows',
          component: () => import('../views/workflows/WorkflowList.vue'),
        },
        {
          path: 'workflows/new',
          name: 'WorkflowCreate',
          component: () => import('../views/workflows/WorkflowEditor.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'workflows/:id',
          name: 'WorkflowDetail',
          component: () => import('../views/workflows/WorkflowDetail.vue'),
        },
        {
          path: 'workflows/:id/edit',
          name: 'WorkflowEdit',
          component: () => import('../views/workflows/WorkflowEditor.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'workflows/:id/executions',
          name: 'WorkflowExecutions',
          component: () => import('../views/workflows/ExecutionList.vue'),
        },
        { path: 'dify', name: 'Dify', component: () => import('../views/dify/DifyConfig.vue') },
        {
          path: 'executions',
          name: 'Executions',
          component: () => import('../views/workflows/ExecutionList.vue'),
        },
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('maas_token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'Login' }
  }
  if (to.name === 'Login' && token) {
    return { name: 'Dashboard' }
  }
})

export default router
