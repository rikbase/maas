<template>
  <div>
    <BaseSpinner v-if="loading" class="spinner" />

    <template v-else-if="execution">
      <BasePageHeader :title="$t('workflow.executionDetail')">
        <template #actions>
          <BaseButton size="sm" variant="ghost" @click="router.push(`/workflows/${execution.workflowId}`)">
            {{ $t('workflow.back') }}
          </BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard noPadding class="info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.status') }}</span>
            <span class="info-item-value">
              <BaseBadge :variant="runStatusVariant(execution.status)">{{ $t('workflow.runStatuses.' + execution.status) }}</BaseBadge>
            </span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.triggerType') }}</span>
            <span class="info-item-value">{{ execution.triggerType || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.startedAt') }}</span>
            <span class="info-item-value">{{ execution.startedAt ? formatTime(execution.startedAt) : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.finishedAt') }}</span>
            <span class="info-item-value">{{ execution.finishedAt ? formatTime(execution.finishedAt) : '-' }}</span>
          </div>
        </div>
      </BaseCard>

      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">{{ $t('workflow.steps') }} ({{ execution.stepExecutions.length }})</h2>
        </template>
        <table class="data-table">
          <thead>
            <tr>
              <th>{{ $t('workflow.stepId') }}</th>
              <th>{{ $t('workflow.stepType') }}</th>
              <th>{{ $t('workflow.status') }}</th>
              <th>{{ $t('workflow.input') }}</th>
              <th>{{ $t('workflow.output') }}</th>
              <th>{{ $t('workflow.error') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in execution.stepExecutions" :key="s.id">
              <td class="text-mono">{{ s.stepId }}</td>
              <td><span class="step-type-badge">{{ s.stepType }}</span></td>
              <td>
                <BaseBadge :variant="stepStatusVariant(s.status)">{{ $t('workflow.stepStatuses.' + s.status) }}</BaseBadge>
              </td>
              <td class="cell-json">{{ truncate(s.input, 60) }}</td>
              <td class="cell-json">{{ truncate(s.output, 60) }}</td>
              <td class="cell-error">{{ s.errorMessage ? truncate(s.errorMessage, 40) : '-' }}</td>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { executionApi } from '../../api/workflows'
import type { Execution } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const execution = ref<Execution | null>(null)

let timer: ReturnType<typeof setInterval> | null = null

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

function stepStatusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = {
    completed: 'success',
    failed: 'danger',
    running: 'info',
    pending: 'neutral',
    skipped: 'neutral',
  }
  return map[status] || 'neutral'
}

async function fetch() {
  try {
    const res = await executionApi.get(route.params.id as string)
    execution.value = res.data
    if (res.data.status !== 'running' && res.data.status !== 'pending') {
      if (timer) { clearInterval(timer); timer = null }
    }
  } catch {
    show(t('common.error'), 'error')
  }
}

onMounted(async () => {
  try {
    const res = await executionApi.get(route.params.id as string)
    execution.value = res.data
    if (res.data.status === 'running' || res.data.status === 'pending') {
      timer = setInterval(fetch, 5000)
    }
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function formatTime(ts: string) {
  return new Date(ts).toLocaleString()
}

function truncate(s: string | null, max: number) {
  if (!s) return '-'
  return s.length > max ? s.slice(0, max) + '...' : s
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
  grid-template-columns: repeat(4, 1fr);
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
.step-type-badge {
  padding: 2px 8px;
  background: var(--color-bg-muted);
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  color: var(--color-foreground);
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
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.75rem;
}
.cell-json {
  font-family: var(--font-mono);
  font-size: 0.7rem;
  color: var(--color-foreground-secondary);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cell-error {
  font-family: var(--font-mono);
  font-size: 0.7rem;
  color: var(--color-danger);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
