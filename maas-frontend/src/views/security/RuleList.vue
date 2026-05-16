<template>
  <div>
    <BasePageHeader :title="$t('security.rules.title')">
      <template #actions>
        <BaseButton variant="primary" size="sm" @click="$router.push('/security/rules/new')">
          {{ $t('security.rules.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="rules"
      :loading="loading"
      :empty-text="$t('security.rules.empty')"
      card
    >
      <template #cell-name="{ row }">
        <strong>{{ row.name }}</strong>
      </template>
      <template #cell-type="{ row }">
        {{ $t('security.rules.detectorTypes.' + row.detectorType) }}
      </template>
      <template #cell-severity="{ row }">
        <BaseBadge :variant="severityVariant(row.severity)">
          {{ $t('security.rules.severities.' + row.severity) }}
        </BaseBadge>
      </template>
      <template #cell-action="{ row }">
        <BaseBadge :variant="actionVariant(row.action)">
          {{ $t('security.rules.actionsList.' + row.action) }}
        </BaseBadge>
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="row.enabled ? 'success' : 'neutral'">
          {{ $t('security.rules.statuses.' + (row.enabled ? 'active' : 'inactive')) }}
        </BaseBadge>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton variant="ghost" size="sm" @click="$router.push(`/security/rules/${row.id}/edit`)">
          {{ $t('security.rules.edit') }}
        </BaseButton>
        <BaseButton variant="secondary" size="sm" @click="toggleRule(row)">
          {{ row.enabled ? $t('security.rules.disable') : $t('security.rules.enable') }}
        </BaseButton>
        <BaseButton variant="danger" size="sm" @click="deleteRule(row.id)">
          {{ $t('provider.delete') }}
        </BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ruleApi } from '../../api/security'
import type { SecurityRule } from '../../api/security'
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
const rules = ref<SecurityRule[]>([])

const columns: TableColumn[] = [
  { key: 'name', label: t('security.rules.name') },
  { key: 'type', label: t('security.rules.type') },
  { key: 'severity', label: t('security.rules.severity') },
  { key: 'action', label: t('security.rules.action') },
  { key: 'status', label: t('security.rules.status') },
  { key: 'actions', label: t('security.rules.actions') },
]

onMounted(async () => {
  try {
    const res = await ruleApi.list()
    rules.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

function severityVariant(severity: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { low: 'info', medium: 'warning', high: 'danger', critical: 'danger' }
  return map[severity] || 'info'
}

function actionVariant(action: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { block: 'danger', flag: 'warning', audit: 'success' }
  return map[action] || 'neutral'
}

async function toggleRule(r: SecurityRule) {
  try {
    await ruleApi.toggleEnabled(r.id)
    r.enabled = !r.enabled
    show(r.enabled ? t('security.rules.enabled') : t('security.rules.disabled'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteRule(id: string) {
  if (!(await confirmDialog(t('security.rules.deleteConfirm')))) return
  try {
    await ruleApi.delete(id)
    rules.value = rules.value.filter(r => r.id !== id)
    show(t('security.rules.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>
