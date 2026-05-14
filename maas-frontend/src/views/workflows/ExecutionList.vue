<template>
  <div>
    <div class="header">
      <h1>{{ isGlobal ? $t('workflow.executionList') : (workflowName || $t('workflow.executions')) }}</h1>
      <div class="header-actions">
        <router-link v-if="workflowId" :to="`/workflows/${workflowId}`" class="btn-sm">
          {{ $t('workflow.back') }}
        </router-link>
      </div>
    </div>

    <div class="filters">
      <select v-model="filterStatus" class="input" @change="fetch">
        <option value="">{{ $t('workflow.allStatuses') }}</option>
        <option v-for="s in statuses" :key="s" :value="s">
          {{ $t('workflow.runStatuses.' + s) }}
        </option>
      </select>
      <input type="date" v-model="dateFrom" class="input" :placeholder="$t('workflow.dateRange')" @change="fetch" />
      <span class="sep">—</span>
      <input type="date" v-model="dateTo" class="input" @change="fetch" />
      <button class="btn-sm" @click="fetch">{{ $t('workflow.refresh') }}</button>
      <label class="live-toggle">
        <input type="checkbox" v-model="liveEnabled" />
        <span class="live-dot" :class="{ active: liveEnabled }"></span>
        {{ $t('workflow.autoRefresh') }}
      </label>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>

    <template v-else-if="page">
      <table class="table">
        <thead>
          <tr>
            <th>{{ $t('workflow.id') }}</th>
            <th v-if="isGlobal">{{ $t('workflow.workflowName') }}</th>
            <th>{{ $t('workflow.status') }}</th>
            <th>{{ $t('workflow.triggerType') }}</th>
            <th>{{ $t('workflow.startedAt') }}</th>
            <th>{{ $t('workflow.finishedAt') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in page.content" :key="e.id" class="clickable" @click="go(e.id)">
            <td class="mono">{{ e.id.slice(0, 8) }}...</td>
            <td v-if="isGlobal">{{ e.workflowName || '-' }}</td>
            <td><span :class="'badge badge-run-' + e.status">{{ $t('workflow.runStatuses.' + e.status) }}</span></td>
            <td>{{ e.triggerType || '-' }}</td>
            <td class="muted">{{ e.startedAt ? formatTime(e.startedAt) : '-' }}</td>
            <td class="muted">{{ e.finishedAt ? formatTime(e.finishedAt) : '-' }}</td>
          </tr>
          <tr v-if="page.content.length === 0">
            <td :colspan="isGlobal ? 6 : 5" class="empty-cell">{{ $t('workflow.noExecutions') }}</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span class="page-info">{{ $t('workflow.page') }} {{ page.number + 1 }} / {{ page.totalPages || 1 }}</span>
        <select v-model.number="pageSize" class="input page-size" @change="onPageSizeChange">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <div class="page-btns">
          <button :disabled="page.first" @click="goPage(0)" class="btn-sm">«</button>
          <button :disabled="page.first" @click="goPage(page.number - 1)" class="btn-sm">{{ $t('workflow.prev') }}</button>
          <button :disabled="page.last" @click="goPage(page.number + 1)" class="btn-sm">{{ $t('workflow.next') }}</button>
          <button :disabled="page.last" @click="goPage(page.totalPages - 1)" class="btn-sm">»</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { workflowApi, executionApi } from '../../api/workflows'
import type { ExecutionPage } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const workflowId = computed(() => route.params.id as string | undefined)
const isGlobal = computed(() => !workflowId.value)

const statuses = ['pending', 'running', 'completed', 'failed', 'cancelled']

const loading = ref(true)
const page = ref<ExecutionPage | null>(null)
const workflowName = ref('')
const filterStatus = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const pageSize = ref(10)
const liveEnabled = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

async function fetch() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      size: pageSize.value,
    }
    if (page.value) params.page = page.value.number
    if (filterStatus.value) params.status = filterStatus.value
    if (dateFrom.value) params.start = new Date(dateFrom.value).toISOString()
    if (dateTo.value) {
      const end = new Date(dateTo.value)
      end.setDate(end.getDate() + 1)
      params.end = end.toISOString()
    }

    if (isGlobal.value) {
      const res = await executionApi.list(params)
      page.value = res.data
    } else {
      const res = await workflowApi.getExecutions(workflowId.value!, params)
      page.value = res.data
      if (!workflowName.value) {
        const wf = await workflowApi.get(workflowId.value!)
        workflowName.value = wf.data.name
      }
    }
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
}

function goPage(n: number) {
  if (!page.value) return
  page.value.number = n
  fetch()
}

function onPageSizeChange() {
  if (!page.value) return
  page.value.number = 0
  fetch()
}

function go(id: string) {
  router.push(`/executions/${id}`)
}

function formatTime(ts: string) {
  return new Date(ts).toLocaleString()
}

function startPolling() {
  stopPolling()
  timer = setInterval(fetch, 5000)
}

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(() => {
  fetch()
})

onUnmounted(() => {
  stopPolling()
})

// Live polling: enable when any running/pending executions exist
const hasLive = computed(() => {
  return page.value?.content.some(e => e.status === 'running' || e.status === 'pending') ?? false
})

watch(hasLive, (v) => {
  if (v && liveEnabled.value) startPolling()
  else if (!v) stopPolling()
})

watch(liveEnabled, (v) => {
  if (v && hasLive.value) startPolling()
  else if (!v) stopPolling()
})
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header h1 { margin: 0; }
.header-actions { display: flex; gap: 8px; }
.loading { color: #666; padding: 20px; text-align: center; }

.filters { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.input { padding: 6px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
.sep { color: #999; }

.live-toggle { display: flex; align-items: center; gap: 4px; font-size: 13px; cursor: pointer; }
.live-dot { width: 8px; height: 8px; border-radius: 50%; background: #ccc; display: inline-block; }
.live-dot.active { background: #4caf50; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.table th { background: #f9fafb; font-weight: 600; color: #374151; }
.table tr.clickable { cursor: pointer; }
.table tr.clickable:hover { background: #f0f7ff; }
.empty-cell { text-align: center; color: #999; padding: 32px; }
.mono { font-family: monospace; font-size: 12px; }
.muted { color: #666; font-size: 12px; }

.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.badge-run-completed { background: #e8f5e9; color: #2e7d32; }
.badge-run-failed { background: #ffebee; color: #c62828; }
.badge-run-running { background: #e3f2fd; color: #1565c0; }
.badge-run-cancelled { background: #f3f4f6; color: #6b7280; }
.badge-run-pending { background: #fef3c7; color: #92400e; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; margin-top: 16px; padding: 12px; }
.page-info { font-size: 13px; color: #666; }
.page-size { width: 70px; }
.page-btns { display: flex; gap: 4px; }
.btn-sm { padding: 6px 12px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; text-decoration: none; }
.btn-sm:disabled { opacity: 0.4; cursor: default; }
</style>
