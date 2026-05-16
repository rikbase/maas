<template>
  <div>
    <BasePageHeader :title="$t('provider.title')" :description="$t('provider.subtitle')">
      <template #actions>
        <BaseButton variant="primary" size="sm" @click="$router.push('/providers/new')">
          {{ $t('provider.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="providers"
      :loading="loading"
      :empty-text="$t('provider.empty')"
      card
    >
      <template #cell-name="{ row }">
        <span class="cell-name">{{ row.name }}</span>
      </template>
      <template #cell-type="{ row }">
        {{ $t('provider.types.' + row.type) }}
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="statusBadgeVariant(row.status)">
          {{ $t('provider.statuses.' + row.status) }}
        </BaseBadge>
      </template>
      <template #cell-health="{ row }">
        <BaseBadge :variant="healthBadgeVariant(row.healthStatus)">
          {{ $t('provider.healthStatuses.' + (row.healthStatus || 'unknown')) }}
        </BaseBadge>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton variant="secondary" size="sm" @click="$router.push(`/providers/${row.id}/edit`)">
          {{ $t('provider.edit') }}
        </BaseButton>
        <BaseButton
          variant="secondary"
          size="sm"
          :loading="checkingIds.has(row.id)"
          :disabled="checkingIds.has(row.id)"
          @click="refreshHealth(row)"
        >
          {{ $t('provider.refreshHealth') }}
        </BaseButton>
        <BaseButton v-if="row.healthStatus === 'unhealthy'" variant="primary" size="sm" @click="markHealthy(row)">
          {{ $t('provider.markHealthy') }}
        </BaseButton>
        <BaseButton variant="danger" size="sm" @click="deleteProvider(row.id)">
          {{ $t('provider.delete') }}
        </BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../../api/providers'
import type { Provider } from '../../types'
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
const providers = ref<Provider[]>([])
const checkingIds = ref<Set<string>>(new Set())

const columns: TableColumn[] = [
  { key: 'name', label: t('provider.name') },
  { key: 'type', label: t('provider.type') },
  { key: 'status', label: t('provider.status') },
  { key: 'health', label: t('provider.health') },
  { key: 'actions', label: t('provider.actions') },
]

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
  if (!(await confirmDialog(t('provider.deleteConfirm')))) return
  try {
    await providerApi.delete(id)
    providers.value = providers.value.filter(p => p.id !== id)
    show(t('provider.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function markHealthy(p: Provider) {
  try {
    const res = await providerApi.setHealthStatus(p.id, 'healthy')
    const idx = providers.value.findIndex(x => x.id === p.id)
    if (idx >= 0) providers.value[idx] = { ...providers.value[idx], ...res.data }
    show(t('provider.healthMarkedHealthy'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function refreshHealth(p: Provider) {
  checkingIds.value = new Set([...checkingIds.value, p.id])
  try {
    const res = await providerApi.checkHealth(p.id)
    const idx = providers.value.findIndex(x => x.id === p.id)
    if (idx >= 0) providers.value[idx] = { ...providers.value[idx], ...res.data }
    show(t('provider.healthRefreshed'))
  } catch {
    show(t('common.error'), 'error')
  } finally {
    const next = new Set(checkingIds.value)
    next.delete(p.id)
    checkingIds.value = next
  }
}

function statusBadgeVariant(status: Provider['status']): 'success' | 'danger' | 'neutral' {
  switch (status) {
    case 'enabled': return 'success'
    case 'error': return 'danger'
    case 'disabled': default: return 'neutral'
  }
}

function healthBadgeVariant(healthStatus: string): 'success' | 'danger' | 'warning' | 'neutral' {
  switch (healthStatus) {
    case 'healthy': return 'success'
    case 'unhealthy': case 'error': return 'danger'
    case 'degraded': return 'warning'
    default: return 'neutral'
  }
}
</script>

<style scoped>
.cell-name {
  font-weight: 600;
}
</style>
