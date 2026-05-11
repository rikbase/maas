<template>
  <div>
    <h1>Dashboard</h1>
    <div class="cards">
      <div class="card"><h3>Providers</h3><p>{{ stats.providers }} active</p></div>
      <div class="card"><h3>API Keys</h3><p>{{ stats.keys }} active</p></div>
      <div class="card"><h3>Today's Tokens</h3><p>{{ stats.todayTokens }}</p></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../api/providers'
import { keyApi } from '../api/keys'

const stats = ref({ providers: 0, keys: 0, todayTokens: 0 })

onMounted(async () => {
  const [providersRes, keysRes] = await Promise.allSettled([
    providerApi.list(),
    keyApi.list()
  ])
  if (providersRes.status === 'fulfilled') stats.value.providers = providersRes.value.data.length
  if (keysRes.status === 'fulfilled') stats.value.keys = keysRes.value.data.length
})
</script>

<style scoped>
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 16px; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
</style>
