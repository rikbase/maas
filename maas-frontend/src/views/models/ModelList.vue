<template>
  <div>
    <h1>{{ $t('model.title') }}</h1>
    <p class="hint">{{ $t('model.hint') }}</p>

    <div v-if="loading" class="loading">{{ $t('model.loading') }}</div>
    <div v-else-if="models.length === 0" class="empty">
      <p>{{ $t('model.empty') }}</p>
      <p class="hint">{{ $t('model.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead><tr><th>{{ $t('model.modelName') }}</th><th>{{ $t('model.provider') }}</th><th>{{ $t('model.status') }}</th></tr></thead>
      <tbody>
        <tr v-for="m in models" :key="m.id">
          <td>{{ m.name }}</td>
          <td>{{ m.providerName }}</td>
          <td><span :class="'badge ' + m.status">{{ $t('model.statuses.' + m.status) }}</span></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../../api/providers'

const loading = ref(true)
const models = ref<Array<{ id: string; name: string; providerName: string; status: string }>>([])

onMounted(async () => {
  try {
    const res = await providerApi.list()
    models.value = res.data.flatMap(p => [{ id: p.id, name: p.name, providerName: p.name, status: 'active' }])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hint { color: #666; font-size: 14px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.active { background: #e8f5e9; color: #2e7d32; }
</style>
