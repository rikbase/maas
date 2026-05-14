<template>
  <div class="layout">
    <aside :class="['sidebar', { 'sidebar--collapsed': collapsed }]">
      <div class="sidebar__header">
        <div class="sidebar__logo">
          <svg class="sidebar__logo-icon" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          <span v-show="!collapsed" class="sidebar__title">MaaS</span>
        </div>
        <button class="sidebar__collapse" @click="collapsed = !collapsed">
          <IconChevronLeft v-if="!collapsed" />
          <IconChevronRight v-else />
        </button>
      </div>

      <nav class="sidebar__nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'nav-item--active': isActive(item.path) }"
        >
          <component :is="item.icon" class="nav-item__icon" />
          <span v-show="!collapsed" class="nav-item__label">{{ $t(item.label) }}</span>
        </router-link>
      </nav>

      <div class="sidebar__footer">
        <div v-if="auth.user" class="sidebar__user">
          <div class="sidebar__avatar">{{ auth.user.displayName?.charAt(0) || auth.user.username?.charAt(0) || 'U' }}</div>
          <div v-show="!collapsed" class="sidebar__user-info">
            <span class="sidebar__user-name">{{ auth.user.displayName || auth.user.username }}</span>
            <span class="sidebar__user-role">{{ auth.user.role }}</span>
          </div>
        </div>
        <div class="sidebar__actions">
          <button class="sidebar__action-btn" :title="$t('nav.language')" @click="toggleLang">
            <IconLanguage />
            <span v-show="!collapsed">{{ $t('nav.language') }}</span>
          </button>
          <button class="sidebar__action-btn sidebar__action-btn--logout" :title="$t('auth.logout')" @click="handleLogout">
            <IconLogout />
            <span v-show="!collapsed">{{ $t('auth.logout') }}</span>
          </button>
        </div>
      </div>
    </aside>
    <main :class="['main', { 'main--expanded': collapsed }]">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { setLocale } from '../i18n'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import IconDashboard from './icons/IconDashboard.vue'
import IconProviders from './icons/IconProviders.vue'
import IconModels from './icons/IconModels.vue'
import IconKeys from './icons/IconKeys.vue'
import IconSecurity from './icons/IconSecurity.vue'
import IconMCP from './icons/IconMCP.vue'
import IconDify from './icons/IconDify.vue'
import IconSkills from './icons/IconSkills.vue'
import IconTools from './icons/IconTools.vue'
import IconWorkflows from './icons/IconWorkflows.vue'
import IconExecutions from './icons/IconExecutions.vue'
import IconUser from './icons/IconUser.vue'
import IconChevronLeft from './icons/IconChevronLeft.vue'
import IconChevronRight from './icons/IconChevronRight.vue'
import IconLanguage from './icons/IconLanguage.vue'
import IconLogout from './icons/IconLogout.vue'

const { locale } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const collapsed = ref(false)

const navItems = [
  { path: '/', icon: IconDashboard, label: 'nav.dashboard' },
  { path: '/providers', icon: IconProviders, label: 'nav.providers' },
  { path: '/models', icon: IconModels, label: 'nav.models' },
  { path: '/keys', icon: IconKeys, label: 'nav.apiKeys' },
  { path: '/security/rules', icon: IconSecurity, label: 'security.nav' },
  { path: '/users', icon: IconUser, label: 'user.title' },
  { path: '/mcp/servers', icon: IconMCP, label: 'nav.mcp' },
  { path: '/dify', icon: IconDify, label: 'nav.dify' },
  { path: '/skills', icon: IconSkills, label: 'nav.skills' },
  { path: '/tools', icon: IconTools, label: 'nav.tools' },
  { path: '/workflows', icon: IconWorkflows, label: 'nav.workflows' },
  { path: '/executions', icon: IconExecutions, label: 'nav.executions' },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function toggleLang() {
  const next = locale.value === 'en' ? 'zh' : 'en'
  setLocale(next)
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  background: var(--color-bg-page);
}

/* ── Sidebar ── */
.sidebar {
  width: var(--sidebar-width);
  background: var(--sidebar-bg);
  color: var(--sidebar-text);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  overflow: hidden;
  flex-shrink: 0;
  z-index: 100;
}
.sidebar--collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-4);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  min-height: 60px;
}
.sidebar__logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  overflow: hidden;
}
.sidebar__logo-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}
.sidebar__title {
  font-size: 1.286rem;
  font-weight: 800;
  color: #fff;
  white-space: nowrap;
}
.sidebar__collapse {
  background: rgba(255,255,255,0.06);
  border: none;
  color: var(--sidebar-text);
  cursor: pointer;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}
.sidebar__collapse:hover {
  background: rgba(255,255,255,0.12);
  color: #fff;
}

/* ── Navigation ── */
.sidebar__nav {
  flex: 1;
  padding: var(--space-3) var(--space-2);
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 9px var(--space-3);
  border-radius: var(--radius-md);
  color: var(--sidebar-text);
  text-decoration: none;
  font-size: 0.929rem;
  font-weight: 500;
  transition: all var(--transition-fast);
  position: relative;
  white-space: nowrap;
}
.nav-item:hover {
  background: var(--sidebar-hover-bg);
  color: var(--sidebar-text-active);
}
.nav-item--active {
  background: var(--sidebar-hover-bg);
  color: var(--sidebar-text-active);
}
.nav-item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--sidebar-active-indicator);
  border-radius: 0 3px 3px 0;
}
.nav-item__icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
}
.nav-item__label {
  overflow: hidden;
}

/* ── Footer ── */
.sidebar__footer {
  padding: var(--space-3) var(--space-2);
  border-top: 1px solid rgba(255,255,255,0.06);
}
.sidebar__user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-2);
}
.sidebar__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.857rem;
  font-weight: 700;
  flex-shrink: 0;
}
.sidebar__user-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar__user-name {
  font-size: 0.857rem;
  font-weight: 600;
  color: var(--sidebar-text-active);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sidebar__user-role {
  font-size: 0.714rem;
  color: var(--sidebar-text);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.sidebar__actions {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.sidebar__action-btn {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 8px var(--space-3);
  border-radius: var(--radius-md);
  border: none;
  background: transparent;
  color: var(--sidebar-text);
  font-size: 0.857rem;
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-fast);
  width: 100%;
}
.sidebar__action-btn:hover {
  background: var(--sidebar-hover-bg);
  color: var(--sidebar-text-active);
}
.sidebar__action-btn--logout:hover {
  color: var(--color-danger);
}

/* ── Main content ── */
.main {
  flex: 1;
  padding: var(--space-5) var(--space-6);
  overflow-y: auto;
  transition: margin-left var(--transition-normal);
  min-width: 0;
}
</style>
