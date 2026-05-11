<template>
  <div>
    <div class="header">
      <h1>API Keys</h1>
      <router-link to="/keys/new" class="btn-primary">+ Create Key</router-link>
    </div>

    <table class="table">
      <thead><tr><th>Name</th><th>Type</th><th>Key (suffix)</th><th>Status</th><th>Expires</th><th>Actions</th></tr></thead>
      <tbody>
        <tr v-for="k in keys" :key="k.id">
          <td>{{ k.name }}</td>
          <td>{{ k.keyType }}</td>
          <td><code>...{{ k.keyPrefix }}</code></td>
          <td><span :class="'badge ' + k.status">{{ k.status }}</span></td>
          <td>{{ k.expiresAt ? new Date(k.expiresAt).toLocaleDateString() : 'Never' }}</td>
          <td>
            <button v-if="k.status === 'active'" @click="revokeKey(k.id)" class="btn-sm btn-warning">Revoke</button>
            <button @click="deleteKey(k.id)" class="btn-sm btn-danger">Delete</button>
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

const keys = ref<ApiKey[]>([])

onMounted(async () => {
  const res = await keyApi.list()
  keys.value = res.data
})

async function revokeKey(id: string) {
  if (!confirm('Revoke this key? This cannot be undone.')) return
  await keyApi.revoke(id)
  keys.value = keys.value.map(k => k.id === id ? { ...k, status: 'revoked' } : k)
}

async function deleteKey(id: string) {
  if (!confirm('Delete this key?')) return
  await keyApi.delete(id)
  keys.value = keys.value.filter(k => k.id !== id)
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
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
