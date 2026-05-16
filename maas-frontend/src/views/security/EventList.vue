<template>
  <div>
    <BasePageHeader :title="$t('security.events.title')">
      <template #actions>
        <div v-if="stats" class="stats-bar">
          <span class="stat-item">{{ $t('security.events.total') }}: <strong>{{ stats.totalEvents }}</strong></span>
          <span class="stat-item stat-blocked">{{ $t('security.events.blocked') }}: <strong>{{ stats.blockedCount }}</strong></span>
          <span class="stat-item stat-flagged">{{ $t('security.events.flagged') }}: <strong>{{ stats.flaggedCount }}</strong></span>
          <span class="stat-item stat-24h">{{ $t('security.events.last24h') }}: <strong>{{ stats.last24hEvents }}</strong></span>
        </div>
      </template>
    </BasePageHeader>

    <div class="filters">
      <select v-model="filter.severity" @change="loadEvents" class="input-field">
        <option value="">{{ $t('security.events.allSeverities') }}</option>
        <option value="low">{{ $t('security.rules.severities.low') }}</option>
        <option value="medium">{{ $t('security.rules.severities.medium') }}</option>
        <option value="high">{{ $t('security.rules.severities.high') }}</option>
        <option value="critical">{{ $t('security.rules.severities.critical') }}</option>
      </select>
      <select v-model="filter.detectorType" @change="loadEvents" class="input-field">
        <option value="">{{ $t('security.events.allTypes') }}</option>
        <option value="prompt_injection">{{ $t('security.rules.detectorTypes.prompt_injection') }}</option>
        <option value="secret_leak">{{ $t('security.rules.detectorTypes.secret_leak') }}</option>
      </select>
    </div>

    <BaseTable
      :columns="columns"
      :data="events"
      :loading="loading"
      :empty-text="$t('security.events.empty')"
      card
    >
      <template #cell-time="{ row }">
        <span class="time-cell">{{ formatTime(row.createdAt) }}</span>
      </template>
      <template #cell-type="{ row }">
        <BaseBadge variant="info">{{ $t('security.rules.detectorTypes.' + row.detectorType) }}</BaseBadge>
      </template>
      <template #cell-severity="{ row }">
        <BaseBadge :variant="severityVariant(row.severity)">
          {{ $t('security.rules.severities.' + row.severity) }}
        </BaseBadge>
      </template>
      <template #cell-action="{ row }">
        <BaseBadge :variant="actionVariant(row.actionTaken)">
          {{ $t('security.rules.actionsList.' + row.actionTaken) }}
        </BaseBadge>
      </template>
      <template #cell-model="{ row }">
        {{ row.model || '-' }}
      </template>
      <template #cell-direction="{ row }">
        {{ row.direction }}
      </template>
      <template #cell-summary="{ row }">
        <span class="summary-cell">{{ truncate(row.requestSummary, 60) }}</span>
      </template>
    </BaseTable>

    <div v-if="totalPages > 1" class="pagination-wrap">
      <BasePagination :page="page + 1" :page-size="20" :total="totalPages * 20" @update:page="p => { page = p - 1; loadEvents() }" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { eventApi } from '../../api/security'
import type { SecurityEvent, SecurityStats } from '../../api/security'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'
import BasePagination from '../../components/ui/BasePagination.vue'

const loading = ref(true)
const events = ref<SecurityEvent[]>([])
const stats = ref<SecurityStats | null>(null)
const page = ref(0)
const totalPages = ref(1)
const filter = reactive({ severity: '', detectorType: '' })

const columns: TableColumn[] = [
  { key: 'time', label: 'Time' },
  { key: 'type', label: 'Type' },
  { key: 'severity', label: 'Severity' },
  { key: 'action', label: 'Action' },
  { key: 'model', label: 'Model' },
  { key: 'direction', label: 'Direction' },
  { key: 'summary', label: 'Summary' },
]

onMounted(async () => {
  await Promise.all([loadEvents(), loadStats()])
})

function severityVariant(severity: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { low: 'info', medium: 'warning', high: 'danger', critical: 'danger' }
  return map[severity] || 'info'
}

function actionVariant(action: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { block: 'danger', flag: 'warning', audit: 'success' }
  return map[action] || 'neutral'
}

async function loadEvents() {
  loading.value = true
  try {
    const res = await eventApi.list({
      severity: filter.severity || undefined,
      detectorType: filter.detectorType || undefined,
      page: page.value,
      size: 20,
    })
    events.value = res.data.content || res.data
    totalPages.value = res.data.totalPages || 1
  } catch {
    // silent
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await eventApi.stats()
    stats.value = res.data
  } catch {
    // silent
  }
}

function formatTime(ts: string) {
  const d = new Date(ts)
  return d.toLocaleString()
}

function truncate(s: string | null, max: number) {
  if (!s) return '-'
  return s.length > max ? s.substring(0, max) + '...' : s
}
</script>

<style scoped>
.stats-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.stat-item {
  background: var(--color-bg-card);
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 0.857rem;
  border: 1px solid var(--color-border);
  white-space: nowrap;
}
.stat-item strong {
  font-weight: 600;
}
.stat-blocked strong { color: var(--color-danger); }
.stat-flagged strong { color: var(--color-warning); }
.stat-24h strong { color: var(--color-primary); }
.filters {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.input-field {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  background: var(--color-bg-card);
}
.input-field:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
  outline: none;
}
.time-cell {
  white-space: nowrap;
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
}
.summary-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-4);
}
</style>
