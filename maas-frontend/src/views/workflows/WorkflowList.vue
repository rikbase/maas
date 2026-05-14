<template>
  <div>
    <div class="header">
      <h1>{{ $t('workflow.title') }}</h1>
      <router-link v-if="auth.isAdmin" to="/workflows/new" class="btn-primary">{{ $t('workflow.add') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="workflows.length === 0" class="empty">
      <p>{{ $t('workflow.empty') }}</p>
      <p class="hint">{{ $t('workflow.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead>
        <tr>
          <th>{{ $t('workflow.name') }}</th>
          <th>{{ $t('workflow.status') }}</th>
          <th>{{ $t('workflow.version') }}</th>
          <th>{{ $t('workflow.lastRun') }}</th>
          <th>{{ $t('workflow.updatedAt') }}</th>
          <th v-if="auth.isAdmin">{{ $t('workflow.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="w in workflows" :key="w.id">
          <td>
            <router-link :to="`/workflows/${w.id}`" class="link">{{ w.name }}</router-link>
            <span v-if="w.description" class="desc"> — {{ w.description }}</span>
          </td>
          <td><span :class="'badge badge-' + w.status">{{ $t('workflow.statuses.' + w.status) }}</span></td>
          <td>{{ w.latestVersion ?? '-' }}</td>
          <td>
            <span v-if="w.lastRunStatus" :class="'badge badge-run-' + w.lastRunStatus">
              {{ $t('workflow.runStatuses.' + w.lastRunStatus) }}
            </span>
            <span v-else class="muted">-</span>
          </td>
          <td class="muted">{{ formatTime(w.updatedAt) }}</td>
          <td v-if="auth.isAdmin" class="actions">
            <router-link :to="`/workflows/${w.id}/edit`" class="btn-sm">{{ $t('workflow.edit') }}</router-link>
            <button @click="publishWorkflow(w.id)" class="btn-sm btn-ok">{{ $t('workflow.publish') }}</button>
            <button @click="executeWorkflow(w.id)" class="btn-sm">{{ $t('workflow.execute') }}</button>
            <button @click="deleteWorkflow(w.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { workflowApi } from '../../api/workflows'
import type { Workflow } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const workflows = ref<Workflow[]>([])

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
  if (!confirm(t('workflow.deleteConfirm'))) return
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
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.hint { font-size: 13px; color: #999; margin-top: 8px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.actions { display: flex; gap: 4px; flex-wrap: wrap; }
.link { color: #1976d2; text-decoration: none; font-weight: 500; }
.link:hover { text-decoration: underline; }
.desc { color: #666; font-size: 12px; }
.muted { color: #999; font-size: 12px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.badge-draft { background: #f3f4f6; color: #6b7280; }
.badge-published { background: #e8f5e9; color: #2e7d32; }
.badge-archived { background: #fef3c7; color: #92400e; }
.badge-run-completed { background: #e8f5e9; color: #2e7d32; }
.badge-run-failed { background: #ffebee; color: #c62828; }
.badge-run-running { background: #e3f2fd; color: #1565c0; }
.badge-run-cancelled { background: #f3f4f6; color: #6b7280; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; font-size: 13px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
.btn-ok { background: #e8f5e9; color: #2e7d32; }
</style>
