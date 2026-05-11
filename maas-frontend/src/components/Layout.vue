<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">MaaS Platform</div>
      <nav>
        <router-link to="/" class="nav-item">{{ $t('nav.dashboard') }}</router-link>
        <router-link to="/providers" class="nav-item">{{ $t('nav.providers') }}</router-link>
        <router-link to="/models" class="nav-item">{{ $t('nav.models') }}</router-link>
        <router-link to="/keys" class="nav-item">{{ $t('nav.apiKeys') }}</router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="lang-btn" @click="toggleLang">{{ $t('nav.language') }}</button>
      </div>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { setLocale } from '../i18n'
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

function toggleLang() {
  const next = locale.value === 'en' ? 'zh' : 'en'
  setLocale(next)
}
</script>

<style scoped>
.layout { display: flex; height: 100vh; }
.sidebar { width: 240px; background: #1a1a2e; color: white; padding: 20px; display: flex; flex-direction: column; }
.logo { font-size: 18px; font-weight: bold; margin-bottom: 30px; }
.nav-item { display: block; padding: 10px 0; color: #ccc; text-decoration: none; }
.nav-item:hover { color: white; }
.nav-item.router-link-active { color: #4fc3f7; }
.main { flex: 1; padding: 24px; background: #f5f5f5; overflow-y: auto; }
.sidebar-footer { margin-top: auto; padding-top: 20px; }
.lang-btn { width: 100%; padding: 8px; background: rgba(255,255,255,0.1); color: #ccc; border: 1px solid rgba(255,255,255,0.2); border-radius: 4px; cursor: pointer; font-size: 13px; }
.lang-btn:hover { background: rgba(255,255,255,0.2); color: white; }
</style>
