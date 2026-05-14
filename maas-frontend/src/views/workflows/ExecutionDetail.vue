<template>
  <div>
    <div class="header">
      <h1>{{ $t('workflow.executionDetail') }}</h1>
      <router-link :to="`/workflows/${execution?.workflowId}`" class="btn-sm">{{ $t('workflow.back') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>

    <div v-else-if="execution" class="detail">
      <div class="info-grid">
        <div class="info-item">
          <label>{{ $t('workflow.status') }}</label>
          <span :class="'badge badge-run-' + execution.status">{{ $t('workflow.runStatuses.' + execution.status) }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.triggerType') }}</label>
          <span>{{ execution.triggerType || '-' }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.startedAt') }}</label>
          <span>{{ execution.startedAt ? formatTime(execution.startedAt) : '-' }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.finishedAt') }}</label>
          <span>{{ execution.finishedAt ? formatTime(execution.finishedAt) : '-' }}</span>
        </div>
      </div>

      <section class="section">
        <h2>{{ $t('workflow.steps') }} ({{ execution.stepExecutions.length }})</h2>
        <table class="table">
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
              <td class="mono">{{ s.stepId }}</td>
              <td><span class="step-type">{{ s.stepType }}</span></td>
              <td><span :class="'badge badge-step-' + s.status">{{ $t('workflow.stepStatuses.' + s.status) }}</span></td>
              <td class="cell-json">{{ truncate(s.input, 60) }}</td>
              <td class="cell-json">{{ truncate(s.output, 60) }}</td>
              <td class="cell-error">{{ s.errorMessage ? truncate(s.errorMessage, 40) : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { executionApi } from '../../api/workflows'
import type { Execution } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()

const loading = ref(true)
const execution = ref<Execution | null>(null)

let timer: ReturnType<typeof setInterval> | null = null

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
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.info-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; background: white; padding: 20px; border-radius: 8px; margin-bottom: 24px; }
.info-item label { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.info-item span { font-size: 14px; }
.section { margin-bottom: 24px; }
.section h2 { font-size: 16px; margin-bottom: 12px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.mono { font-family: monospace; font-size: 12px; }
.cell-json { font-family: monospace; font-size: 11px; color: #666; max-width: 200px; }
.cell-error { font-family: monospace; font-size: 11px; color: #c62828; max-width: 150px; }
.step-type { padding: 2px 8px; background: #f3f4f6; border-radius: 4px; font-size: 12px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.badge-run-completed { background: #e8f5e9; color: #2e7d32; }
.badge-run-failed { background: #ffebee; color: #c62828; }
.badge-run-running { background: #e3f2fd; color: #1565c0; }
.badge-run-cancelled { background: #f3f4f6; color: #6b7280; }
.badge-run-pending { background: #fef3c7; color: #92400e; }
.badge-step-completed { background: #e8f5e9; color: #2e7d32; }
.badge-step-failed { background: #ffebee; color: #c62828; }
.badge-step-running { background: #e3f2fd; color: #1565c0; }
.badge-step-pending { background: #f3f4f6; color: #6b7280; }
.badge-step-skipped { background: #f3f4f6; color: #9e9e9e; }
.btn-sm { padding: 6px 12px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 13px; }
</style>
