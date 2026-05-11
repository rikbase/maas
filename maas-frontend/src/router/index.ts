import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../components/Layout.vue'),
      children: [
        { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
        { path: 'providers', name: 'Providers', component: () => import('../views/providers/ProviderList.vue') },
        { path: 'providers/new', name: 'ProviderCreate', component: () => import('../views/providers/ProviderForm.vue') },
        { path: 'providers/:id/edit', name: 'ProviderEdit', component: () => import('../views/providers/ProviderForm.vue') },
        { path: 'keys', name: 'Keys', component: () => import('../views/keys/KeyList.vue') },
        { path: 'keys/new', name: 'KeyCreate', component: () => import('../views/keys/KeyForm.vue') },
        { path: 'models', name: 'Models', component: () => import('../views/models/ModelList.vue') },
      ]
    }
  ]
})

export default router
