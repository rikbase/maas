<template>
  <div>
    <BasePageHeader :title="$t('mcp.servers.title')">
      <template #actions>
        <BaseButton variant="primary" size="sm" @click="$router.push('/mcp/servers/new')">
          {{ $t('mcp.servers.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="servers"
      :loading="loading"
      :empty-text="$t('mcp.servers.empty')"
      :row-click="(s: any) => $router.push(`/mcp/servers/${s.id}`)"
      card
    >
      <template #cell-name="{ row }">
        <router-link :to="`/mcp/servers/${row.id}`" class="name-link" @click.stop>{{ row.name }}</router-link>
        <span v-if="row.description" class="desc">{{ row.description }}</span>
      </template>
      <template #cell-transport="{ row }">
        <BaseBadge variant="info">{{ row.transport }}</BaseBadge>
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="statusVariant(row.status)">
          {{ $t('mcp.servers.statuses.' + row.status) }}
        </BaseBadge>
      </template>
      <template #cell-toolCount="{ row }">
        {{ row.toolCount }}
      </template>
      <template #cell-actions="{ row }">
        <BaseButton variant="ghost" size="sm" @click.stop="$router.push(`/mcp/servers/${row.id}/edit`)">
          {{ $t('provider.edit') }}
        </BaseButton>
        <BaseButton variant="danger" size="sm" @click.stop="deleteServer(row.id)">
          {{ $t('provider.delete') }}
        </BaseButton>
      </template>
    </BaseTable>
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
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const servers = ref<McpServer[]>([])

const columns: TableColumn[] = [
  { key: 'name', label: t('mcp.servers.name') },
  { key: 'transport', label: t('mcp.servers.transport') },
  { key: 'status', label: t('mcp.servers.status') },
  { key: 'toolCount', label: t('mcp.servers.toolCount') },
  { key: 'actions', label: t('mcp.servers.actions') },
]

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
</style>
