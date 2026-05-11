<template>
  <div>
    <h1>{{ $t('dashboard.title') }}</h1>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else class="cards">
      <div class="card"><h3>{{ $t('dashboard.providers') }}</h3><p>{{ stats.providers }} {{ $t('dashboard.active') }}</p></div>
      <div class="card"><h3>{{ $t('dashboard.apiKeys') }}</h3><p>{{ stats.keys }} {{ $t('dashboard.active') }}</p></div>
      <div class="card"><h3>{{ $t('dashboard.todayTokens') }}</h3><p>{{ stats.todayTokens }}</p></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../api/providers'
import { keyApi } from '../api/keys'
import { useToast } from '../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()

const loading = ref(true)
const stats = ref({ providers: 0, keys: 0, todayTokens: 0 })

onMounted(async () => {
  try {
    const [providersRes, keysRes] = await Promise.allSettled([
      providerApi.list(),
      keyApi.list()
    ])
    if (providersRes.status === 'fulfilled') stats.value.providers = providersRes.value.data.length
    if (keysRes.status === 'fulfilled') stats.value.keys = keysRes.value.data.length
  } catch (e) {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading { color: #666; padding: 20px; text-align: center; }
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 16px; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
</style>
