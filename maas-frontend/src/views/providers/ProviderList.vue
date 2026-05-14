<template>
  <div>
    <BasePageHeader :title="$t('provider.title')" :description="$t('provider.subtitle')">
      <template #actions>
        <BaseButton variant="primary" size="sm" @click="$router.push('/providers/new')">
          {{ $t('provider.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseCard noPadding>
      <BaseSpinner v-if="loading" size="lg" class="loading-spinner" />
      <BaseEmpty
        v-else-if="providers.length === 0"
        :text="$t('provider.empty')"
      />
      <table v-else class="provider-table">
        <thead>
          <tr>
            <th>{{ $t('provider.name') }}</th>
            <th>{{ $t('provider.type') }}</th>
            <th>{{ $t('provider.status') }}</th>
            <th>{{ $t('provider.health') }}</th>
            <th>{{ $t('provider.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in providers" :key="p.id">
            <td class="cell-name">{{ p.name }}</td>
            <td>{{ $t('provider.types.' + p.type) }}</td>
            <td>
              <BaseBadge :variant="statusBadgeVariant(p.status)">
                {{ $t('provider.statuses.' + p.status) }}
              </BaseBadge>
            </td>
            <td>
              <BaseBadge :variant="healthBadgeVariant(p.healthStatus)">
                {{ $t('provider.healthStatuses.' + (p.healthStatus || 'unknown')) }}
              </BaseBadge>
            </td>
            <td class="cell-actions">
              <BaseButton variant="secondary" size="sm" @click="$router.push(`/providers/${p.id}/edit`)">
                {{ $t('provider.edit') }}
              </BaseButton>
              <BaseButton variant="secondary" size="sm" :loading="checkingIds.has(p.id)" :disabled="checkingIds.has(p.id)" @click="refreshHealth(p)">
                {{ $t('provider.refreshHealth') }}
              </BaseButton>
              <BaseButton v-if="p.healthStatus === 'unhealthy'" variant="primary" size="sm" @click="markHealthy(p)">
                {{ $t('provider.markHealthy') }}
              </BaseButton>
              <BaseButton variant="danger" size="sm" @click="deleteProvider(p.id)">
                {{ $t('provider.delete') }}
              </BaseButton>
            </td>
          </tr>
        </tbody>
      </table>
    </BaseCard>
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
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const providers = ref<Provider[]>([])
const checkingIds = ref<Set<string>>(new Set())

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
    case 'enabled':
      return 'success'
    case 'error':
      return 'danger'
    case 'disabled':
    default:
      return 'neutral'
  }
}

function healthBadgeVariant(healthStatus: string): 'success' | 'danger' | 'warning' | 'neutral' {
  switch (healthStatus) {
    case 'healthy':
      return 'success'
    case 'unhealthy':
    case 'error':
      return 'danger'
    case 'degraded':
      return 'warning'
    default:
      return 'neutral'
  }
}
</script>

<style scoped>
.loading-spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-12);
}

.provider-table {
  width: 100%;
  border-collapse: collapse;
}

.provider-table th {
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
  border-bottom: 2px solid var(--color-border);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.provider-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
  font-size: 0.929rem;
  color: var(--color-foreground);
}

.provider-table tr:hover td {
  background: var(--color-bg-muted);
}

.provider-table tr:last-child td {
  border-bottom: none;
}

.cell-name {
  font-weight: 600;
}

.cell-actions {
  display: flex;
  gap: var(--space-2);
}
</style>
