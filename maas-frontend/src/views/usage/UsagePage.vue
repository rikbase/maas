<template>
  <div>
    <BasePageHeader :title="$t('usage.title')" :description="$t('usage.subtitle')">
      <template #actions>
        <div class="range-selector">
          <button
            v-for="r in ranges"
            :key="r.key"
            class="range-btn"
            :class="{ 'range-btn--active': range === r.key }"
            @click="setRange(r.key)"
          >{{ r.label }}</button>
        </div>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" size="lg" class="page-loading" />
    <template v-else>
      <!-- KPI Bar -->
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon--primary">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          </div>
          <div class="kpi-body">
            <span class="kpi-value">{{ formatNum(stats.requestCount) }}</span>
            <span class="kpi-label">{{ $t('usage.requests') }}</span>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon--indigo">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div class="kpi-body">
            <span class="kpi-value">{{ formatNum(stats.totalTokens) }}</span>
            <span class="kpi-label">{{ $t('usage.totalTokens') }}</span>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon--emerald">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          </div>
          <div class="kpi-body">
            <span class="kpi-value">${{ formatCost(stats.totalCost) }}</span>
            <span class="kpi-label">{{ $t('usage.totalCost') }}</span>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon--amber">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          </div>
          <div class="kpi-body">
            <span class="kpi-value">{{ stats.avgLatencyMs.toFixed(0) }}ms</span>
            <span class="kpi-label">{{ $t('usage.avgLatency') }}</span>
          </div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon kpi-icon--rose">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          </div>
          <div class="kpi-body">
            <span class="kpi-value">{{ stats.errorCount }}</span>
            <span class="kpi-label">{{ $t('usage.errors') }}</span>
          </div>
        </div>
      </div>

      <!-- Token breakdown -->
      <div class="token-breakdown">
        <BaseCard class="token-card">
          <h3 class="card-title">{{ $t('usage.promptTokens') }}</h3>
          <span class="stat-number">{{ formatNum(stats.promptTokens) }}</span>
        </BaseCard>
        <BaseCard class="token-card">
          <h3 class="card-title">{{ $t('usage.completionTokens') }}</h3>
          <span class="stat-number">{{ formatNum(stats.completionTokens) }}</span>
        </BaseCard>
        <BaseCard class="token-card">
          <h3 class="card-title">{{ $t('usage.tokenRatio') }}</h3>
          <span class="stat-number">{{ promptRatio }}%</span>
        </BaseCard>
      </div>

      <!-- Trends Chart -->
      <BaseCard v-if="trends.length > 0" class="section-card">
        <template #header>
          <h3 class="card-title">{{ $t('usage.tokenTrends') }}</h3>
        </template>
        <div class="bar-chart">
          <div
            v-for="t in trends"
            :key="t.date"
            class="bar-col"
            :title="`${t.date}: ${formatNum(t.tokenCount)} tokens`"
          >
            <div class="bar-fill" :style="{ height: barHeight(t.tokenCount) + '%' }" />
            <span class="bar-label">{{ formatDay(t.date) }}</span>
          </div>
        </div>
      </BaseCard>

      <!-- Model Rank + Provider Health -->
      <div class="tables-row">
        <BaseCard class="table-card">
          <template #header>
            <h3 class="card-title">{{ $t('usage.topModels') }}</h3>
          </template>
          <table v-if="models.length > 0" class="mini-table">
            <thead>
              <tr>
                <th>#</th>
                <th>{{ $t('usage.modelName') }}</th>
                <th class="num">{{ $t('usage.requests') }}</th>
                <th class="num">{{ $t('usage.tokens') }}</th>
                <th class="num">{{ $t('usage.cost') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(m, i) in models" :key="m.id || i">
                <td class="rank">{{ i + 1 }}</td>
                <td class="model-cell">{{ m.name }}</td>
                <td class="num">{{ formatNum(m.requestCount) }}</td>
                <td class="num">{{ formatNum(m.tokenCount) }}</td>
                <td class="num">${{ formatCost(m.cost) }}</td>
              </tr>
              <tr v-if="models.length === 0">
                <td colspan="5" class="empty-cell">{{ $t('common.empty') }}</td>
              </tr>
            </tbody>
          </table>
          <BaseEmpty v-else :text="$t('usage.noData')" />
        </BaseCard>

        <BaseCard class="table-card">
          <template #header>
            <h3 class="card-title">{{ $t('usage.providerHealth') }}</h3>
          </template>
          <table v-if="providers.length > 0" class="mini-table">
            <thead>
              <tr>
                <th>{{ $t('usage.providerName') }}</th>
                <th class="num">{{ $t('usage.requests') }}</th>
                <th class="num">{{ $t('usage.latency') }}</th>
                <th class="num">{{ $t('usage.errorRate') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in providers" :key="p.providerName">
                <td>{{ p.providerName }}</td>
                <td class="num">{{ formatNum(p.requestCount) }}</td>
                <td class="num">{{ p.avgLatencyMs.toFixed(0) }}ms</td>
                <td class="num">
                  <span class="err-rate" :class="errorRateClass(p)">{{ errorRate(p) }}%</span>
                </td>
              </tr>
              <tr v-if="providers.length === 0">
                <td colspan="4" class="empty-cell">{{ $t('common.empty') }}</td>
              </tr>
            </tbody>
          </table>
          <BaseEmpty v-else :text="$t('usage.noData')" />
        </BaseCard>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { usageApi } from '../../api/usage'
import type { UsageStats, UsageTrend, UsageRank, ProviderHealth } from '../../api/usage'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()

const ranges = [
  { key: 'today', label: t('usage.today') },
  { key: '7d', label: t('usage.last7d') },
  { key: '30d', label: t('usage.last30d') },
  { key: '90d', label: t('usage.last90d') },
]
const range = ref('today')

const loading = ref(true)
const stats = reactive<UsageStats>({
  requestCount: 0, totalCost: 0, totalTokens: 0,
  promptTokens: 0, completionTokens: 0,
  avgLatencyMs: 0, errorCount: 0,
})
const trends = ref<UsageTrend[]>([])
const models = ref<UsageRank[]>([])
const providers = ref<ProviderHealth[]>([])

const promptRatio = computed(() => {
  const total = stats.promptTokens + stats.completionTokens
  return total > 0 ? ((stats.promptTokens / total) * 100).toFixed(1) : '0'
})

function setRange(key: string) {
  range.value = key
  fetch()
}

async function fetch() {
  loading.value = true
  try {
    const rangeParam = range.value === 'today' ? 'today'
      : range.value === '7d' ? 'week'
      : range.value === '30d' ? 'month'
      : 'quarter'

    const [statsRes, trendsRes, modelsRes, providersRes] = await Promise.all([
      usageApi.stats().then(r => r.data),
      usageApi.trends(rangeParam).then(r => r.data),
      usageApi.byModel(rangeParam).then(r => r.data),
      usageApi.byProvider(rangeParam).then(r => r.data),
    ])

    Object.assign(stats, statsRes)
    trends.value = trendsRes || []
    models.value = modelsRes || []
    providers.value = providersRes || []
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
}

function fetchInitial() {
  fetch()
}

fetchInitial()

function formatNum(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M'
  if (n >= 1_000) return (n / 1_000).toFixed(1) + 'K'
  return n.toLocaleString()
}

function formatCost(n: number | string): string {
  const v = typeof n === 'string' ? parseFloat(n) : n
  return v.toFixed(4)
}

function barHeight(tokens: number): number {
  const max = Math.max(...trends.value.map(t => t.tokenCount), 1)
  return (tokens / max) * 100
}

function formatDay(date: string): string {
  const d = new Date(date)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function errorRate(p: ProviderHealth): string {
  return p.requestCount > 0
    ? ((p.errorCount / p.requestCount) * 100).toFixed(1)
    : '0'
}

function errorRateClass(p: ProviderHealth): string {
  const rate = p.requestCount > 0 ? p.errorCount / p.requestCount : 0
  if (rate > 0.1) return 'err-rate--high'
  if (rate > 0.02) return 'err-rate--mid'
  return 'err-rate--low'
}
</script>

<style scoped>
.page-loading {
  display: flex;
  justify-content: center;
  padding: var(--space-12);
}

/* Range Selector */
.range-selector {
  display: flex;
  gap: 2px;
  background: var(--color-bg-muted);
  padding: 3px;
  border-radius: var(--radius-md);
}
.range-btn {
  padding: 5px 14px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-foreground-secondary);
  font-size: 0.857rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.range-btn:hover {
  color: var(--color-foreground);
}
.range-btn--active {
  background: var(--color-bg-card);
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

/* KPI Grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.kpi-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-icon--primary { background: var(--color-primary-light); color: var(--color-primary); }
.kpi-icon--indigo { background: #eef2ff; color: #4f46e5; }
.kpi-icon--emerald { background: var(--color-success-light); color: var(--color-success); }
.kpi-icon--amber { background: var(--color-warning-light); color: var(--color-warning); }
.kpi-icon--rose { background: var(--color-danger-light); color: var(--color-danger); }
.kpi-body { display: flex; flex-direction: column; min-width: 0; }
.kpi-value { font-size: var(--text-xl); font-weight: 800; color: var(--color-foreground); line-height: 1.2; }
.kpi-label { font-size: var(--text-xs); color: var(--color-foreground-secondary); font-weight: 500; }

/* Token breakdown */
.token-breakdown {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.token-card {
  text-align: center;
  padding: var(--space-4);
}
.stat-number {
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--color-foreground);
}
.card-title {
  font-size: var(--text-sm);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.4px;
  color: var(--color-foreground-secondary);
  margin: 0;
}

/* Sections */
.section-card {
  margin-bottom: var(--space-4);
}

/* Bar Chart */
.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 160px;
  padding-top: var(--space-3);
}
.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
  gap: 4px;
}
.bar-fill {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(to top, var(--color-primary), var(--color-secondary));
  border-radius: 3px 3px 0 0;
  transition: height var(--transition-normal);
  min-height: 4px;
}
.bar-label {
  font-size: 0.643rem;
  color: var(--color-foreground-secondary);
  white-space: nowrap;
}

/* Tables */
.tables-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.table-card {
  overflow: hidden;
}
.mini-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.857rem;
}
.mini-table th {
  text-align: left;
  padding: var(--space-2) var(--space-3);
  font-weight: 700;
  font-size: 0.786rem;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  color: var(--color-foreground-secondary);
  border-bottom: 1px solid var(--color-border);
}
.mini-table th.num,
.mini-table td.num {
  text-align: right;
}
.mini-table td {
  padding: var(--space-2) var(--space-3);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-foreground);
}
.mini-table tr:last-child td {
  border-bottom: none;
}
.rank {
  font-weight: 700;
  color: var(--color-muted);
  width: 28px;
}
.model-cell {
  font-weight: 600;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.empty-cell {
  text-align: center;
  color: var(--color-foreground-secondary);
  padding: var(--space-6) !important;
}
.err-rate {
  font-weight: 700;
}
.err-rate--low { color: var(--color-success); }
.err-rate--mid { color: var(--color-warning); }
.err-rate--high { color: var(--color-danger); }
</style>
