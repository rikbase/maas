<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">MaaS Platform</div>
      <nav>
        <router-link to="/" class="nav-item">Dashboard</router-link>
        <router-link to="/providers" class="nav-item">Providers</router-link>
        <router-link to="/models" class="nav-item">Models</router-link>
        <router-link to="/keys" class="nav-item">API Keys</router-link>
      </nav>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout { display: flex; height: 100vh; }
.sidebar { width: 240px; background: #1a1a2e; color: white; padding: 20px; }
.logo { font-size: 18px; font-weight: bold; margin-bottom: 30px; }
.nav-item { display: block; padding: 10px 0; color: #ccc; text-decoration: none; }
.nav-item:hover { color: white; }
.nav-item.router-link-active { color: #4fc3f7; }
.main { flex: 1; padding: 24px; background: #f5f5f5; overflow-y: auto; }
</style>
