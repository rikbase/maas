<template>
  <div>
    <BasePageHeader :title="isGlobal ? $t('workflow.executionList') : (workflowName || $t('workflow.executions'))">
      <template #actions>
        <BaseButton v-if="workflowId" size="sm" variant="ghost" @click="router.push(`/workflows/${workflowId}`)">
          {{ $t('workflow.back') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <div class="filter-bar">
      <select v-model="filterStatus" class="filter-select" @change="fetch">
        <option value="">{{ $t('workflow.allStatuses') }}</option>
        <option v-for="s in statuses" :key="s" :value="s">
          {{ $t('workflow.runStatuses.' + s) }}
        </option>
      </select>
      <input type="date" v-model="dateFrom" class="filter-input" @change="fetch" />
      <span class="filter-sep">&mdash;</span>
      <input type="date" v-model="dateTo" class="filter-input" @change="fetch" />
      <BaseButton size="sm" variant="secondary" @click="fetch">{{ $t('workflow.refresh') }}</BaseButton>
      <label class="live-toggle">
        <input type="checkbox" v-model="liveEnabled" />
        <span class="live-dot" :class="{ active: liveEnabled }"></span>
        {{ $t('workflow.autoRefresh') }}
      </label>
    </div>

    <template v-if="page">
      <BaseTable
        :columns="columns"
        :data="page.content"
        :loading="loading"
        :empty-text="$t('workflow.noExecutions')"
        card
        :row-click="go"
      >
        <template #cell-id="{ row }">
          <span class="text-mono">{{ row.id.slice(0, 8) }}...</span>
        </template>
        <template #cell-workflowName="{ row }">
          {{ row.workflowName || '-' }}
        </template>
        <template #cell-status="{ row }">
          <BaseBadge :variant="runStatusVariant(row.status)">{{ $t('workflow.runStatuses.' + row.status) }}</BaseBadge>
        </template>
        <template #cell-triggerType="{ row }">
          {{ row.triggerType || '-' }}
        </template>
        <template #cell-startedAt="{ row }">
          <span class="muted-text">{{ row.startedAt ? formatTime(row.startedAt) : '-' }}</span>
        </template>
        <template #cell-finishedAt="{ row }">
          <span class="muted-text">{{ row.finishedAt ? formatTime(row.finishedAt) : '-' }}</span>
        </template>
      </BaseTable>

      <div v-if="page.content.length > 0" class="pagination-bar">
        <select v-model.number="pageSize" class="page-size-select" @change="onPageSizeChange">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <BasePagination
          :page="page.number + 1"
          :page-size="pageSize"
          :total="page.totalElements"
          @update:page="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BasePagination from '../../components/ui/BasePagination.vue'
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'
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

const columns = computed<TableColumn[]>(() => {
  const cols: TableColumn[] = [
    { key: 'id', label: t('workflow.id') },
  ]
  if (isGlobal.value) {
    cols.push({ key: 'workflowName', label: t('workflow.workflowName') })
  }
  cols.push(
    { key: 'status', label: t('workflow.status') },
    { key: 'triggerType', label: t('workflow.triggerType') },
    { key: 'startedAt', label: t('workflow.startedAt') },
    { key: 'finishedAt', label: t('workflow.finishedAt') },
  )
  return cols
})

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

function handlePageChange(n: number) {
  if (!page.value) return
  page.value.number = n - 1
  fetch()
}

function onPageSizeChange() {
  if (!page.value) return
  page.value.number = 0
  fetch()
}

function go(row: any) {
  router.push(`/executions/${row.id}`)
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
.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
}
.filter-select,
.filter-input {
  padding: 6px 10px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 0.857rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
}
.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.filter-sep {
  color: var(--color-foreground-secondary);
}
.live-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: 0.857rem;
  cursor: pointer;
  color: var(--color-foreground);
  user-select: none;
}
.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-muted);
  display: inline-block;
}
.live-dot.active {
  background: var(--color-success);
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
.muted-text {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.75rem;
}
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-4);
  padding: var(--space-3);
}
.page-size-select {
  padding: 6px 10px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 0.857rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
}
.page-size-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
</style>
