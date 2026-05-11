<template>
  <div>
    <div class="header">
      <h1>Providers</h1>
      <router-link to="/providers/new" class="btn-primary">+ Add Provider</router-link>
    </div>

    <table class="table">
      <thead><tr><th>Name</th><th>Type</th><th>Status</th><th>Health</th><th>Actions</th></tr></thead>
      <tbody>
        <tr v-for="p in providers" :key="p.id">
          <td>{{ p.name }}</td>
          <td>{{ p.type }}</td>
          <td><span :class="'badge ' + p.status">{{ p.status }}</span></td>
          <td><span :class="'badge ' + p.healthStatus">{{ p.healthStatus }}</span></td>
          <td>
            <router-link :to="`/providers/${p.id}/edit`" class="btn-sm">Edit</router-link>
            <button @click="deleteProvider(p.id)" class="btn-sm btn-danger">Delete</button>
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

const providers = ref<Provider[]>([])

onMounted(async () => {
  const res = await providerApi.list()
  providers.value = res.data
})

async function deleteProvider(id: string) {
  if (!confirm('Delete this provider?')) return
  await providerApi.delete(id)
  providers.value = providers.value.filter(p => p.id !== id)
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
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
