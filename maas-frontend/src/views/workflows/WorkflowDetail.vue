<template>
  <div>
    <div class="header">
      <h1>{{ workflow?.name ?? $t('workflow.detail') }}</h1>
      <div class="header-actions" v-if="workflow">
        <router-link :to="`/workflows/${workflow.id}/edit`" class="btn-primary" v-if="auth.isAdmin">{{ $t('workflow.edit') }}</router-link>
        <router-link :to="`/workflows/${workflow.id}/executions`" class="btn-sm">{{ $t('workflow.executions') }}</router-link>
      </div>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>

    <div v-else-if="workflow" class="detail">
      <div class="info-grid">
        <div class="info-item">
          <label>{{ $t('workflow.name') }}</label>
          <span>{{ workflow.name }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.description') }}</label>
          <span>{{ workflow.description || '-' }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.status') }}</label>
          <span :class="'badge badge-' + workflow.status">{{ $t('workflow.statuses.' + workflow.status) }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.version') }}</label>
          <span>{{ workflow.latestVersion ?? '-' }}</span>
        </div>
        <div class="info-item">
          <label>{{ $t('workflow.lastRun') }}</label>
          <span v-if="workflow.lastRunStatus" :class="'badge badge-run-' + workflow.lastRunStatus">
            {{ $t('workflow.runStatuses.' + workflow.lastRunStatus) }}
          </span>
          <span v-else class="muted">-</span>
        </div>
      </div>

      <section class="section">
        <h2>{{ $t('workflow.versions') }}</h2>
        <div v-if="versions.length === 0" class="empty-section">{{ $t('workflow.noVersions') }}</div>
        <table v-else class="table">
          <thead><tr>
            <th>{{ $t('workflow.version') }}</th>
            <th>{{ $t('workflow.status') }}</th>
            <th>{{ $t('workflow.createdAt') }}</th>
          </tr></thead>
          <tbody>
            <tr v-for="v in versions" :key="v.id">
              <td>v{{ v.version }}</td>
              <td><span :class="'badge badge-' + v.status">{{ $t('workflow.statuses.' + v.status) }}</span></td>
              <td class="muted">{{ formatTime(v.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="section">
        <h2>{{ $t('workflow.recentExecutions') }}</h2>
        <div v-if="executions.length === 0" class="empty-section">{{ $t('workflow.noExecutions') }}</div>
        <table v-else class="table">
          <thead><tr>
            <th>{{ $t('workflow.id') }}</th>
            <th>{{ $t('workflow.status') }}</th>
            <th>{{ $t('workflow.startedAt') }}</th>
            <th>{{ $t('workflow.finishedAt') }}</th>
          </tr></thead>
          <tbody>
            <tr v-for="e in executions" :key="e.id" class="clickable" @click="router.push('/executions/' + e.id)">
              <td class="mono">{{ e.id.slice(0, 8) }}...</td>
              <td><span :class="'badge badge-run-' + e.status">{{ $t('workflow.runStatuses.' + e.status) }}</span></td>
              <td class="muted">{{ e.startedAt ? formatTime(e.startedAt) : '-' }}</td>
              <td class="muted">{{ e.finishedAt ? formatTime(e.finishedAt) : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { workflowApi } from '../../api/workflows'
import type { Workflow, WorkflowVersion, Execution } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const workflow = ref<Workflow | null>(null)
const versions = ref<WorkflowVersion[]>([])
const executions = ref<Execution[]>([])

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
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.detail { max-width: 960px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; background: white; padding: 20px; border-radius: 8px; margin-bottom: 24px; }
.info-item label { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.info-item span { font-size: 14px; }
.section { margin-bottom: 24px; }
.section h2 { font-size: 16px; margin-bottom: 12px; }
.empty-section { text-align: center; padding: 24px; color: #999; background: white; border-radius: 8px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.muted { color: #999; font-size: 12px; }
.mono { font-family: monospace; font-size: 12px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.badge-draft { background: #f3f4f6; color: #6b7280; }
.badge-published { background: #e8f5e9; color: #2e7d32; }
.badge-archived { background: #fef3c7; color: #92400e; }
.badge-run-completed { background: #e8f5e9; color: #2e7d32; }
.badge-run-failed { background: #ffebee; color: #c62828; }
.badge-run-running { background: #e3f2fd; color: #1565c0; }
.badge-run-cancelled { background: #f3f4f6; color: #6b7280; }
.badge-run-pending { background: #fef3c7; color: #92400e; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; font-size: 13px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 13px; }
.clickable { cursor: pointer; }
.clickable:hover { background: #f0f7ff; }
.header-actions { display: flex; gap: 8px; }
</style>
