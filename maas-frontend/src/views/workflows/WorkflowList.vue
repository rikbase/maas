<template>
  <div>
    <BasePageHeader :title="$t('workflow.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/workflows/new')">
          {{ $t('workflow.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" class="spinner" />

    <BaseEmpty v-else-if="workflows.length === 0" :text="$t('workflow.empty')" />

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>{{ $t('workflow.name') }}</th>
          <th>{{ $t('workflow.status') }}</th>
          <th>{{ $t('workflow.version') }}</th>
          <th>{{ $t('workflow.lastRun') }}</th>
          <th>{{ $t('workflow.updatedAt') }}</th>
          <th>{{ $t('workflow.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="w in workflows" :key="w.id">
          <td>
            <router-link :to="`/workflows/${w.id}`" class="data-table-link">{{ w.name }}</router-link>
            <span v-if="w.description" class="data-table-desc">&mdash; {{ w.description }}</span>
          </td>
          <td>
            <BaseBadge :variant="statusVariant(w.status)">{{ $t('workflow.statuses.' + w.status) }}</BaseBadge>
          </td>
          <td>{{ w.latestVersion ?? '-' }}</td>
          <td>
            <BaseBadge v-if="w.lastRunStatus" :variant="runStatusVariant(w.lastRunStatus)">
              {{ $t('workflow.runStatuses.' + w.lastRunStatus) }}
            </BaseBadge>
            <span v-else class="muted-text">-</span>
          </td>
          <td class="muted-text">{{ formatTime(w.updatedAt) }}</td>
          <td class="action-cell">
            <BaseButton size="sm" variant="ghost" @click="router.push(`/workflows/${w.id}/edit`)">{{ $t('workflow.edit') }}</BaseButton>
            <BaseButton v-if="w.status !== 'published'" size="sm" variant="ghost" @click="publishWorkflow(w.id)">{{ $t('workflow.publish') }}</BaseButton>
            <BaseButton v-if="w.status === 'published'" size="sm" variant="ghost" @click="executeWorkflow(w.id)">{{ $t('workflow.execute') }}</BaseButton>
            <BaseButton size="sm" variant="danger" @click="deleteWorkflow(w.id)">{{ $t('common.delete') }}</BaseButton>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { workflowApi } from '../../api/workflows'
import type { Workflow } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'

const { t } = useI18n()
const { show } = useToast()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const workflows = ref<Workflow[]>([])

function statusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = {
    draft: 'neutral',
    published: 'success',
    archived: 'warning',
  }
  return map[status] || 'neutral'
}

function runStatusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = {
    completed: 'success',
    failed: 'danger',
    running: 'info',
    cancelled: 'neutral',
    pending: 'warning',
  }
  return map[status] || 'neutral'
}

onMounted(async () => {
  try {
    const res = await workflowApi.list()
    workflows.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function publishWorkflow(id: string) {
  try {
    await workflowApi.publish(id)
    show(t('workflow.published'))
    const res = await workflowApi.list()
    workflows.value = res.data
  } catch {
    show(t('common.error'), 'error')
  }
}

async function executeWorkflow(id: string) {
  try {
    await workflowApi.execute(id)
    show(t('workflow.executed'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteWorkflow(id: string) {
  if (!(await confirmDialog(t('workflow.deleteConfirm')))) return
  try {
    await workflowApi.delete(id)
    workflows.value = workflows.value.filter(w => w.id !== id)
    show(t('workflow.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}

function formatTime(ts: string) {
  return new Date(ts).toLocaleString()
}
</script>

<style scoped>
.spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.data-table th {
  text-transform: uppercase;
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
  font-weight: 600;
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
}
.data-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  font-size: 0.875rem;
  color: var(--color-foreground);
}
.data-table tbody tr:hover {
  background: var(--color-bg-muted);
}
.data-table tbody tr:last-child td {
  border-bottom: none;
}
.data-table-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}
.data-table-link:hover {
  text-decoration: underline;
}
.data-table-desc {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
.muted-text {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
.action-cell {
  display: flex;
  gap: var(--space-1);
  flex-wrap: wrap;
}
</style>
