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
