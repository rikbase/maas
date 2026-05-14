<template>
  <div>
    <BasePageHeader :title="$t('mcp.servers.title')">
      <template #actions>
        <BaseButton variant="primary" size="sm" @click="$router.push('/mcp/servers/new')">
          {{ $t('mcp.servers.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" size="lg" />
    <BaseEmpty
      v-else-if="servers.length === 0"
      :text="$t('mcp.servers.empty')"
      :action-text="$t('mcp.servers.emptyHint')"
    />
    <table v-else class="design-table">
      <thead>
        <tr>
          <th>{{ $t('mcp.servers.name') }}</th>
          <th>{{ $t('mcp.servers.transport') }}</th>
          <th>{{ $t('mcp.servers.status') }}</th>
          <th>{{ $t('mcp.servers.toolCount') }}</th>
          <th>{{ $t('mcp.servers.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in servers" :key="s.id">
          <td>
            <router-link :to="`/mcp/servers/${s.id}`" class="name-link">{{ s.name }}</router-link>
            <span v-if="s.description" class="desc">{{ s.description }}</span>
          </td>
          <td><BaseBadge variant="info">{{ s.transport }}</BaseBadge></td>
          <td>
            <BaseBadge :variant="statusVariant(s.status)">
              {{ $t('mcp.servers.statuses.' + s.status) }}
            </BaseBadge>
          </td>
          <td>{{ s.toolCount }}</td>
          <td class="actions-cell">
            <BaseButton variant="ghost" size="sm" @click="$router.push(`/mcp/servers/${s.id}/edit`)">
              {{ $t('provider.edit') }}
            </BaseButton>
            <BaseButton variant="danger" size="sm" @click="deleteServer(s.id)">
              {{ $t('provider.delete') }}
            </BaseButton>
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
import { useConfirm } from '../../composables/useConfirm'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()

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

function statusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { connected: 'success', online: 'success', error: 'danger', offline: 'neutral' }
  return map[status] || 'neutral'
}

async function deleteServer(id: string) {
  if (!(await confirmDialog(t('mcp.servers.deleteConfirm')))) return
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
.name-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}
.name-link:hover {
  text-decoration: underline;
}
.desc {
  display: block;
  font-size: 0.786rem;
  color: var(--color-foreground-secondary);
  margin-top: 2px;
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
.actions-cell {
  display: flex;
  gap: 6px;
}
</style>
