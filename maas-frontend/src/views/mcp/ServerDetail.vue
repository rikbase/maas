<template>
  <div>
    <BaseSpinner v-if="loading" size="lg" />
    <template v-else-if="server">
      <BasePageHeader :title="server.name">
        <template #actions>
          <BaseButton variant="primary" size="sm" @click="$router.push(`/mcp/servers/${server.id}/edit`)">
            {{ $t('provider.edit') }}
          </BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard>
        <template #header>{{ $t('mcp.servers.details') }}</template>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">{{ $t('mcp.servers.transport') }}</span>
            <span class="value"><BaseBadge variant="info">{{ server.transport }}</BaseBadge></span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('mcp.servers.status') }}</span>
            <span class="value">
              <BaseBadge :variant="statusVariant(server.status)">
                {{ $t('mcp.servers.statuses.' + server.status) }}
              </BaseBadge>
            </span>
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
      </BaseCard>

      <!-- Tools -->
      <BaseCard style="margin-top: var(--space-4);">
        <template #header>{{ $t('mcp.servers.tools') }} ({{ tools.length }})</template>
        <template v-if="tools.length === 0">
          <BaseEmpty :text="$t('mcp.servers.noTools')" />
        </template>
        <table v-else class="design-table">
          <thead>
            <tr>
              <th>{{ $t('mcp.servers.toolName') }}</th>
              <th>{{ $t('mcp.servers.description') }}</th>
              <th>{{ $t('mcp.servers.autoRegister') }}</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tools" :key="t.id">
              <td><strong>{{ t.name }}</strong></td>
              <td class="desc-cell">{{ t.description || '-' }}</td>
              <td>
                <BaseBadge :variant="t.autoRegister ? 'success' : 'neutral'">
                  {{ t.autoRegister ? 'Yes' : 'No' }}
                </BaseBadge>
              </td>
              <td>
                <BaseButton variant="danger" size="sm" @click="deleteTool(t.id)">
                  {{ $t('provider.delete') }}
                </BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
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
import { useConfirm } from '../../composables/useConfirm'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

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

function statusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { connected: 'success', online: 'success', error: 'danger', offline: 'neutral' }
  return map[status] || 'neutral'
}

async function deleteTool(toolId: string) {
  if (!(await confirmDialog('Delete this tool?'))) return
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
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.label {
  font-size: 0.786rem;
  color: var(--color-foreground-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}
.value {
  font-size: 0.929rem;
  color: var(--color-foreground);
}
.mono {
  font-family: var(--font-mono);
  font-size: 0.857rem;
  word-break: break-all;
}
.design-table {
  width: 100%;
  border-collapse: collapse;
}
.design-table th {
  font-size: 0.857rem;
  font-weight: 600;
  color: var(--color-foreground-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid var(--color-border);
  padding: 10px 12px;
  text-align: left;
}
.design-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
}
.design-table tr:hover td {
  background: var(--color-bg-muted);
}
.desc-cell {
  color: var(--color-foreground-secondary);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
