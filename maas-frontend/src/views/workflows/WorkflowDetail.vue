<template>
  <div>
    <BaseSpinner v-if="loading" class="spinner" />

    <template v-else-if="workflow">
      <BasePageHeader :title="workflow.name ?? $t('workflow.detail')">
        <template #actions>
          <BaseButton variant="primary" size="sm" @click="router.push(`/workflows/${workflow.id}/edit`)">{{ $t('workflow.edit') }}</BaseButton>
          <BaseButton variant="secondary" size="sm" @click="router.push(`/workflows/${workflow.id}/executions`)">{{ $t('workflow.executions') }}</BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard noPadding class="info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.name') }}</span>
            <span class="info-item-value">{{ workflow.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.description') }}</span>
            <span class="info-item-value">{{ workflow.description || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.status') }}</span>
            <span class="info-item-value">
              <BaseBadge :variant="statusVariant(workflow.status)">{{ $t('workflow.statuses.' + workflow.status) }}</BaseBadge>
            </span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.version') }}</span>
            <span class="info-item-value">{{ workflow.latestVersion ?? '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.lastRun') }}</span>
            <span class="info-item-value">
              <BaseBadge v-if="workflow.lastRunStatus" :variant="runStatusVariant(workflow.lastRunStatus)">
                {{ $t('workflow.runStatuses.' + workflow.lastRunStatus) }}
              </BaseBadge>
              <span v-else class="muted-text">-</span>
            </span>
          </div>
        </div>
      </BaseCard>

      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">{{ $t('workflow.versions') }}</h2>
        </template>
        <BaseEmpty v-if="versions.length === 0" :text="$t('workflow.noVersions')" />
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>{{ $t('workflow.version') }}</th>
              <th>{{ $t('workflow.status') }}</th>
              <th>{{ $t('workflow.createdAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in versions" :key="v.id">
              <td>v{{ v.version }}</td>
              <td>
                <BaseBadge :variant="statusVariant(v.status)">{{ $t('workflow.statuses.' + v.status) }}</BaseBadge>
              </td>
              <td class="muted-text">{{ formatTime(v.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </BaseCard>

      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">{{ $t('workflow.recentExecutions') }}</h2>
        </template>
        <BaseEmpty v-if="executions.length === 0" :text="$t('workflow.noExecutions')" />
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>{{ $t('workflow.id') }}</th>
              <th>{{ $t('workflow.status') }}</th>
              <th>{{ $t('workflow.startedAt') }}</th>
              <th>{{ $t('workflow.finishedAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in executions" :key="e.id" class="clickable-row" @click="router.push('/executions/' + e.id)">
              <td class="text-mono">{{ e.id.slice(0, 8) }}...</td>
              <td>
                <BaseBadge :variant="runStatusVariant(e.status)">{{ $t('workflow.runStatuses.' + e.status) }}</BaseBadge>
              </td>
              <td class="muted-text">{{ e.startedAt ? formatTime(e.startedAt) : '-' }}</td>
              <td class="muted-text">{{ e.finishedAt ? formatTime(e.finishedAt) : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { workflowApi } from '../../api/workflows'
import type { Workflow, WorkflowVersion, Execution } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const workflow = ref<Workflow | null>(null)
const versions = ref<WorkflowVersion[]>([])
const executions = ref<Execution[]>([])

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
  const id = route.params.id as string
  try {
    const [res, verRes, execRes] = await Promise.all([
      workflowApi.get(id),
      workflowApi.getVersions(id),
      workflowApi.getExecutions(id, { size: 10 }),
    ])
    workflow.value = res.data
    versions.value = verRes.data
    executions.value = execRes.data.content
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

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
.info-card {
  margin-bottom: var(--space-5);
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  padding: var(--space-5);
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.info-item-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: var(--color-foreground-secondary);
  letter-spacing: 0.5px;
}
.info-item-value {
  font-size: 0.875rem;
  color: var(--color-foreground);
}
.section-card {
  margin-bottom: var(--space-5);
}
.card-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-foreground);
  margin: 0;
}
.muted-text {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.75rem;
}
.clickable-row {
  cursor: pointer;
}
.clickable-row:hover {
  background: var(--color-bg-muted);
}
.data-table {
  width: 100%;
  border-collapse: collapse;
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
</style>
