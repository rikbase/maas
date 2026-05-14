<template>
  <div>
    <BasePageHeader :title="$t('security.rules.title')">
      <template #actions>
        <BaseButton variant="primary" size="sm" @click="$router.push('/security/rules/new')">
          {{ $t('security.rules.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" size="lg" />
    <BaseEmpty
      v-else-if="rules.length === 0"
      :text="$t('security.rules.empty')"
      :action-text="$t('security.rules.emptyHint')"
    />
    <table v-else class="design-table">
      <thead>
        <tr>
          <th>{{ $t('security.rules.name') }}</th>
          <th>{{ $t('security.rules.type') }}</th>
          <th>{{ $t('security.rules.severity') }}</th>
          <th>{{ $t('security.rules.action') }}</th>
          <th>{{ $t('security.rules.status') }}</th>
          <th>{{ $t('security.rules.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in rules" :key="r.id">
          <td>{{ r.name }}</td>
          <td>{{ $t('security.rules.detectorTypes.' + r.detectorType) }}</td>
          <td>
            <BaseBadge :variant="severityVariant(r.severity)">
              {{ $t('security.rules.severities.' + r.severity) }}
            </BaseBadge>
          </td>
          <td>
            <BaseBadge :variant="actionVariant(r.action)">
              {{ $t('security.rules.actionsList.' + r.action) }}
            </BaseBadge>
          </td>
          <td>
            <BaseBadge :variant="r.enabled ? 'success' : 'neutral'">
              {{ $t('security.rules.statuses.' + (r.enabled ? 'active' : 'inactive')) }}
            </BaseBadge>
          </td>
          <td class="actions-cell">
            <BaseButton variant="ghost" size="sm" @click="$router.push(`/security/rules/${r.id}/edit`)">
              {{ $t('security.rules.edit') }}
            </BaseButton>
            <BaseButton variant="secondary" size="sm" @click="toggleRule(r)">
              {{ r.enabled ? $t('security.rules.disable') : $t('security.rules.enable') }}
            </BaseButton>
            <BaseButton variant="danger" size="sm" @click="deleteRule(r.id)">
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
import { ruleApi } from '../../api/security'
import type { SecurityRule } from '../../api/security'
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
const rules = ref<SecurityRule[]>([])

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

<style scoped>
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
