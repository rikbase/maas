<template>
  <div>
    <div class="header">
      <h1>{{ $t('provider.title') }}</h1>
      <router-link to="/providers/new" class="btn-primary">{{ $t('provider.add') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('provider.loading') }}</div>
    <div v-else-if="providers.length === 0" class="empty">
      <p>{{ $t('provider.empty') }}</p>
      <p class="hint">{{ $t('provider.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead><tr><th>{{ $t('provider.name') }}</th><th>{{ $t('provider.type') }}</th><th>{{ $t('provider.status') }}</th><th>{{ $t('provider.health') }}</th><th>{{ $t('provider.actions') }}</th></tr></thead>
      <tbody>
        <tr v-for="p in providers" :key="p.id">
          <td>{{ p.name }}</td>
          <td>{{ $t('provider.types.' + p.type) }}</td>
          <td><span :class="'badge ' + p.status">{{ $t('provider.statuses.' + p.status) }}</span></td>
          <td><span :class="'badge ' + p.healthStatus">{{ $t('provider.healthStatuses.' + (p.healthStatus || 'unknown')) }}</span></td>
          <td>
            <router-link :to="`/providers/${p.id}/edit`" class="btn-sm">{{ $t('provider.edit') }}</router-link>
            <button @click="deleteProvider(p.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../../api/providers'
import type { Provider } from '../../types'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()

const loading = ref(true)
const providers = ref<Provider[]>([])

onMounted(async () => {
  try {
    const res = await providerApi.list()
    providers.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function deleteProvider(id: string) {
  if (!confirm(t('provider.deleteConfirm'))) return
  try {
    await providerApi.delete(id)
    providers.value = providers.value.filter(p => p.id !== id)
    show(t('provider.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.hint { font-size: 13px; color: #999; margin-top: 8px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.enabled, .badge.healthy { background: #e8f5e9; color: #2e7d32; }
.badge.disabled { background: #eee; color: #666; }
.badge.error, .badge.unhealthy { background: #ffebee; color: #c62828; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; text-decoration: none; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
