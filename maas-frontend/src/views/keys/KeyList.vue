<template>
  <div>
    <div class="header">
      <h1>{{ $t('key.title') }}</h1>
      <router-link v-if="auth.isAdmin" to="/keys/new" class="btn-primary">{{ $t('key.create') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('key.loading') }}</div>
    <div v-else-if="keys.length === 0" class="empty">
      <p>{{ $t('key.empty') }}</p>
      <p class="hint">{{ $t('key.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead><tr><th>{{ $t('key.name') }}</th><th>{{ $t('key.type') }}</th><th>{{ $t('key.keySuffix') }}</th><th>{{ $t('key.status') }}</th><th>{{ $t('key.expires') }}</th><th v-if="auth.isAdmin">{{ $t('key.actions') }}</th></tr></thead>
      <tbody>
        <tr v-for="k in keys" :key="k.id">
          <td>{{ k.name }}</td>
          <td>{{ $t('key.types.' + k.keyType) }}</td>
          <td><code>...{{ k.keyPrefix }}</code></td>
          <td><span :class="'badge ' + k.status">{{ $t('key.statuses.' + k.status) }}</span></td>
          <td>{{ k.expiresAt ? new Date(k.expiresAt).toLocaleDateString() : $t('key.never') }}</td>
          <td v-if="auth.isAdmin">
            <button v-if="k.status === 'active'" @click="revokeKey(k.id)" class="btn-sm btn-warning">{{ $t('key.revoke') }}</button>
            <button @click="deleteKey(k.id)" class="btn-sm btn-danger">{{ $t('key.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { keyApi } from '../../api/keys'
import type { ApiKey } from '../../types'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const keys = ref<ApiKey[]>([])

onMounted(async () => {
  try {
    const res = await keyApi.list()
    keys.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function revokeKey(id: string) {
  if (!confirm(t('key.revokeConfirm'))) return
  try {
    await keyApi.revoke(id)
    keys.value = keys.value.map(k => k.id === id ? { ...k, status: 'revoked' } : k)
    show(t('key.revoked'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteKey(id: string) {
  if (!confirm(t('key.deleteConfirm'))) return
  try {
    await keyApi.delete(id)
    keys.value = keys.value.filter(k => k.id !== id)
    show(t('key.deleted'))
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
code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-size: 12px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.active { background: #e8f5e9; color: #2e7d32; }
.badge.revoked { background: #ffebee; color: #c62828; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; font-size: 13px; }
.btn-warning { background: #fff3e0; color: #e65100; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
