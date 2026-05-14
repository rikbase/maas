<template>
  <div>
    <div class="header">
      <h1>{{ $t('security.events.title') }}</h1>
      <div class="stats" v-if="stats">
        <span class="stat">{{ $t('security.events.total') }}: <strong>{{ stats.totalEvents }}</strong></span>
        <span class="stat stat-blocked">{{ $t('security.events.blocked') }}: <strong>{{ stats.blockedCount }}</strong></span>
        <span class="stat stat-flagged">{{ $t('security.events.flagged') }}: <strong>{{ stats.flaggedCount }}</strong></span>
        <span class="stat stat-24h">{{ $t('security.events.last24h') }}: <strong>{{ stats.last24hEvents }}</strong></span>
      </div>
    </div>

    <div class="filters">
      <select v-model="filter.severity" @change="loadEvents">
        <option value="">{{ $t('security.events.allSeverities') }}</option>
        <option value="low">{{ $t('security.rules.severities.low') }}</option>
        <option value="medium">{{ $t('security.rules.severities.medium') }}</option>
        <option value="high">{{ $t('security.rules.severities.high') }}</option>
        <option value="critical">{{ $t('security.rules.severities.critical') }}</option>
      </select>
      <select v-model="filter.detectorType" @change="loadEvents">
        <option value="">{{ $t('security.events.allTypes') }}</option>
        <option value="prompt_injection">{{ $t('security.rules.detectorTypes.prompt_injection') }}</option>
        <option value="secret_leak">{{ $t('security.rules.detectorTypes.secret_leak') }}</option>
      </select>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="events.length === 0" class="empty">
      <p>{{ $t('security.events.empty') }}</p>
    </div>
    <table v-else class="table">
      <thead>
        <tr>
          <th>{{ $t('security.events.time') }}</th>
          <th>{{ $t('security.rules.type') }}</th>
          <th>{{ $t('security.rules.severity') }}</th>
          <th>{{ $t('security.events.action') }}</th>
          <th>{{ $t('security.events.model') }}</th>
          <th>{{ $t('security.events.direction') }}</th>
          <th>{{ $t('security.events.summary') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="e in events" :key="e.id">
          <td class="time">{{ formatTime(e.createdAt) }}</td>
          <td><span class="badge badge-type">{{ $t('security.rules.detectorTypes.' + e.detectorType) }}</span></td>
          <td><span :class="'badge badge-' + e.severity">{{ $t('security.rules.severities.' + e.severity) }}</span></td>
          <td><span :class="'badge badge-action-' + e.actionTaken">{{ $t('security.rules.actionsList.' + e.actionTaken) }}</span></td>
          <td>{{ e.model || '-' }}</td>
          <td>{{ e.direction }}</td>
          <td class="summary-cell">{{ truncate(e.requestSummary, 60) }}</td>
        </tr>
      </tbody>
    </table>

    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="page <= 0" @click="page--; loadEvents()" class="btn-sm">{{ $t('security.events.prev') }}</button>
      <span>{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadEvents()" class="btn-sm">{{ $t('security.events.next') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { eventApi } from '../../api/security'
import type { SecurityEvent, SecurityStats } from '../../api/security'
const loading = ref(true)
const events = ref<SecurityEvent[]>([])
const stats = ref<SecurityStats | null>(null)
const page = ref(0)
const totalPages = ref(1)
const filter = reactive({ severity: '', detectorType: '' })

onMounted(async () => {
  await Promise.all([loadEvents(), loadStats()])
})

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
.header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; flex-direction: column; gap: 12px; }
.stats { display: flex; gap: 16px; font-size: 13px; }
.stat { background: white; padding: 8px 16px; border-radius: 6px; }
.stat-blocked strong { color: #c62828; }
.stat-flagged strong { color: #e65100; }
.stat-24h strong { color: #1565c0; }
.filters { display: flex; gap: 8px; margin-bottom: 16px; }
.filters select { padding: 6px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.time { white-space: nowrap; font-size: 12px; color: #666; }
.summary-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.badge-type { background: #e8eaf6; color: #283593; }
.badge-low { background: #e3f2fd; color: #1565c0; }
.badge-medium { background: #fff3e0; color: #e65100; }
.badge-high { background: #ffebee; color: #c62828; }
.badge-critical { background: #fce4ec; color: #880e4f; }
.badge-action-block { background: #ffebee; color: #c62828; }
.badge-action-flag { background: #fff3e0; color: #e65100; }
.badge-action-audit { background: #e8f5e9; color: #2e7d32; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; margin-top: 16px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-sm:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
