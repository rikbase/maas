<template>
  <div>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <template v-else-if="server">
      <div class="header">
        <h1>{{ server.name }}</h1>
        <div>
          <router-link v-if="auth.isAdmin" :to="`/mcp/servers/${server.id}/edit`" class="btn-primary">{{ $t('provider.edit') }}</router-link>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="label">{{ $t('mcp.servers.transport') }}</span>
          <span class="value"><span class="badge badge-transport">{{ server.transport }}</span></span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('mcp.servers.status') }}</span>
          <span class="value"><span :class="'badge ' + server.status">{{ $t('mcp.servers.statuses.' + server.status) }}</span></span>
        </div>
        <div class="info-item" v-if="server.command">
          <span class="label">{{ $t('mcp.servers.command') }}</span>
          <span class="value mono">{{ server.command }}</span>
        </div>
        <div class="info-item" v-if="server.url">
          <span class="label">{{ $t('mcp.servers.url') }}</span>
          <span class="value mono">{{ server.url }}</span>
        </div>
      </div>

      <!-- Tools -->
      <div class="section">
        <h2>{{ $t('mcp.servers.tools') }} ({{ tools.length }})</h2>
        <div v-if="tools.length === 0" class="empty">{{ $t('mcp.servers.noTools') }}</div>
        <table v-else class="table">
          <thead><tr>
            <th>{{ $t('mcp.servers.toolName') }}</th>
            <th>{{ $t('mcp.servers.description') }}</th>
            <th>{{ $t('mcp.servers.autoRegister') }}</th>
            <th v-if="auth.isAdmin"></th>
          </tr></thead>
          <tbody>
            <tr v-for="t in tools" :key="t.id">
              <td><strong>{{ t.name }}</strong></td>
              <td class="desc">{{ t.description || '-' }}</td>
              <td><span class="badge" :class="t.autoRegister ? 'enabled' : 'disabled'">{{ t.autoRegister ? 'Yes' : 'No' }}</span></td>
              <td v-if="auth.isAdmin">
                <button @click="deleteTool(t.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { mcpServerApi } from '../../api/mcp'
import type { McpServer, McpTool } from '../../api/mcp'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const server = ref<McpServer | null>(null)
const tools = ref<McpTool[]>([])

onMounted(async () => {
  try {
    const [sRes, tRes] = await Promise.all([
      mcpServerApi.get(route.params.id as string),
      mcpServerApi.listTools(route.params.id as string),
    ])
    server.value = sRes.data
    tools.value = tRes.data
  } catch {
    show(t('common.error'), 'error')
    router.push('/mcp/servers')
  } finally {
    loading.value = false
  }
})

async function deleteTool(toolId: string) {
  if (!confirm('Delete this tool?')) return
  try {
    await mcpServerApi.deleteTool(route.params.id as string, toolId)
    tools.value = tools.value.filter(t => t.id !== toolId)
    show('Tool deleted')
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 20px; color: #666; font-size: 13px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 24px; }
.info-item { background: white; padding: 12px 16px; border-radius: 8px; }
.label { display: block; font-size: 11px; color: #888; text-transform: uppercase; margin-bottom: 4px; }
.value { font-size: 14px; }
.mono { font-family: monospace; font-size: 13px; word-break: break-all; }
.section { margin-top: 16px; }
.section h2 { font-size: 16px; margin-bottom: 12px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.desc { color: #666; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.online { background: #e8f5e9; color: #2e7d32; }
.badge.offline { background: #eee; color: #666; }
.badge.error { background: #ffebee; color: #c62828; }
.badge-transport { background: #e8eaf6; color: #283593; }
.badge.enabled { background: #e8f5e9; color: #2e7d32; }
.badge.disabled { background: #eee; color: #666; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
