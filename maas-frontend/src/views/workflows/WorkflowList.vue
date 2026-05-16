<template>
  <div>
    <BasePageHeader :title="$t('workflow.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/workflows/new')">
          {{ $t('workflow.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="workflows"
      :loading="loading"
      :empty-text="$t('workflow.empty')"
      card
      :row-click="(w: any) => router.push(`/workflows/${w.id}`)"
    >
      <template #cell-name="{ row }">
        <router-link :to="`/workflows/${row.id}`" class="workflow-link">{{ row.name }}</router-link>
        <span v-if="row.description" class="workflow-desc">&mdash; {{ row.description }}</span>
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="statusVariant(row.status)">{{ $t('workflow.statuses.' + row.status) }}</BaseBadge>
      </template>
      <template #cell-version="{ row }">
        {{ row.latestVersion ?? '-' }}
      </template>
      <template #cell-lastRun="{ row }">
        <BaseBadge v-if="row.lastRunStatus" :variant="runStatusVariant(row.lastRunStatus)">
          {{ $t('workflow.runStatuses.' + row.lastRunStatus) }}
        </BaseBadge>
        <span v-else class="muted-text">-</span>
      </template>
      <template #cell-updatedAt="{ row }">
        <span class="muted-text">{{ formatTime(row.updatedAt) }}</span>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton size="sm" variant="ghost" @click.stop="router.push(`/workflows/${row.id}/edit`)">{{ $t('workflow.edit') }}</BaseButton>
        <BaseButton v-if="row.status !== 'published'" size="sm" variant="ghost" @click.stop="publishWorkflow(row.id)">{{ $t('workflow.publish') }}</BaseButton>
        <BaseButton v-if="row.status === 'published'" size="sm" variant="ghost" @click.stop="executeWorkflow(row.id)">{{ $t('workflow.execute') }}</BaseButton>
        <BaseButton size="sm" variant="danger" @click.stop="deleteWorkflow(row.id)">{{ $t('common.delete') }}</BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'
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

const columns: TableColumn[] = [
  { key: 'name', label: t('workflow.name') },
  { key: 'status', label: t('workflow.status') },
  { key: 'version', label: t('workflow.version') },
  { key: 'lastRun', label: t('workflow.lastRun') },
  { key: 'updatedAt', label: t('workflow.updatedAt') },
  { key: 'actions', label: t('workflow.actions') },
]

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
.workflow-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}
.workflow-link:hover {
  text-decoration: underline;
}
.workflow-desc {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
.muted-text {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
</style>
