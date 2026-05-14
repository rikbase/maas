<template>
  <div>
    <div class="header">
      <h1>{{ $t('mcp.servers.title') }}</h1>
      <router-link v-if="auth.isAdmin" to="/mcp/servers/new" class="btn-primary">{{ $t('mcp.servers.add') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="servers.length === 0" class="empty">
      <p>{{ $t('mcp.servers.empty') }}</p>
      <p class="hint">{{ $t('mcp.servers.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead><tr>
        <th>{{ $t('mcp.servers.name') }}</th>
        <th>{{ $t('mcp.servers.transport') }}</th>
        <th>{{ $t('mcp.servers.status') }}</th>
        <th>{{ $t('mcp.servers.toolCount') }}</th>
        <th v-if="auth.isAdmin">{{ $t('mcp.servers.actions') }}</th>
      </tr></thead>
      <tbody>
        <tr v-for="s in servers" :key="s.id">
          <td>
            <router-link :to="`/mcp/servers/${s.id}`" class="link">{{ s.name }}</router-link>
            <span v-if="s.description" class="desc">{{ s.description }}</span>
          </td>
          <td><span class="badge badge-transport">{{ s.transport }}</span></td>
          <td><span :class="'badge ' + s.status">{{ $t('mcp.servers.statuses.' + s.status) }}</span></td>
          <td>{{ s.toolCount }}</td>
          <td v-if="auth.isAdmin">
            <router-link :to="`/mcp/servers/${s.id}/edit`" class="btn-sm">{{ $t('provider.edit') }}</router-link>
            <button @click="deleteServer(s.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { mcpServerApi } from '../../api/mcp'
import type { McpServer } from '../../api/mcp'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const servers = ref<McpServer[]>([])

onMounted(async () => {
  try {
    const res = await mcpServerApi.list()
    servers.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function deleteServer(id: string) {
  if (!confirm(t('mcp.servers.deleteConfirm'))) return
  try {
    await mcpServerApi.delete(id)
    servers.value = servers.value.filter(s => s.id !== id)
    show(t('mcp.servers.deleted'))
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
.link { color: #1976d2; text-decoration: none; font-weight: 500; }
.link:hover { text-decoration: underline; }
.desc { display: block; font-size: 12px; color: #999; margin-top: 2px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.online { background: #e8f5e9; color: #2e7d32; }
.badge.offline { background: #eee; color: #666; }
.badge.error { background: #ffebee; color: #c62828; }
.badge-transport { background: #e8eaf6; color: #283593; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; text-decoration: none; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
