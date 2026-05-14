<template>
  <div>
    <h1>{{ $t('dashboard.title') }}</h1>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <template v-else>
      <!-- Stats Cards -->
      <div class="cards">
        <div class="card">
          <span class="card-label">{{ $t('dashboard.requests') }}</span>
          <span class="card-value">{{ stats.requestCount }}</span>
        </div>
        <div class="card">
          <span class="card-label">{{ $t('dashboard.totalTokens') }}</span>
          <span class="card-value">{{ formatNum(stats.totalTokens) }}</span>
          <span class="card-sub">{{ $t('dashboard.promptTokens') }}: {{ formatNum(stats.promptTokens) }} / {{ $t('dashboard.completionTokens') }}: {{ formatNum(stats.completionTokens) }}</span>
        </div>
        <div class="card">
          <span class="card-label">{{ $t('dashboard.totalCost') }}</span>
          <span class="card-value">${{ stats.totalCost.toFixed(4) }}</span>
        </div>
        <div class="card">
          <span class="card-label">{{ $t('dashboard.avgLatency') }}</span>
          <span class="card-value">{{ stats.avgLatencyMs.toFixed(0) }}ms</span>
        </div>
        <div class="card">
          <span class="card-label">{{ $t('dashboard.errors') }}</span>
          <span class="card-value" :class="{ 'text-danger': stats.errorCount > 0 }">{{ stats.errorCount }}</span>
        </div>
        <div class="card">
          <span class="card-label">{{ $t('dashboard.providers') }}</span>
          <span class="card-value">{{ statsProviders.activeCount }} <span class="unit">{{ $t('dashboard.active') }}</span></span>
        </div>
      </div>

      <!-- Model Ranking -->
      <div class="section">
        <h2>{{ $t('dashboard.topModels') }}</h2>
        <div v-if="models.length === 0" class="empty">{{ $t('dashboard.noData') }}</div>
        <table v-else class="table">
          <thead><tr>
            <th>{{ $t('provider.name') }}</th>
            <th>{{ $t('dashboard.requests') }}</th>
            <th>{{ $t('dashboard.tokens') }}</th>
            <th>{{ $t('dashboard.totalCost') }}</th>
          </tr></thead>
          <tbody>
            <tr v-for="m in models" :key="m.name">
              <td>{{ m.name }}</td>
              <td>{{ m.requestCount }}</td>
              <td>{{ formatNum(m.tokenCount) }}</td>
              <td>${{ m.cost.toFixed(4) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Provider Health -->
      <div class="section">
        <h2>{{ $t('dashboard.providerHealth') }}</h2>
        <div v-if="providers.length === 0" class="empty">{{ $t('dashboard.noData') }}</div>
        <table v-else class="table">
          <thead><tr>
            <th>{{ $t('provider.name') }}</th>
            <th>{{ $t('dashboard.requests') }}</th>
            <th>{{ $t('dashboard.errors') }}</th>
            <th>{{ $t('dashboard.errorRate') }}</th>
            <th>{{ $t('dashboard.avgLatency') }}</th>
          </tr></thead>
          <tbody>
            <tr v-for="p in providers" :key="p.providerName">
              <td>{{ p.providerName }}</td>
              <td>{{ p.requestCount }}</td>
              <td><span :class="{ 'text-danger': p.errorCount > 0 }">{{ p.errorCount }}</span></td>
              <td>{{ (p.requestCount > 0 ? (p.errorCount / p.requestCount * 100) : 0).toFixed(1) }}%</td>
              <td>{{ p.avgLatencyMs.toFixed(0) }}ms</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Trend Sparkline (simple bar chart) -->
      <div class="section" v-if="trends.length > 1">
        <h2>{{ $t('dashboard.trends') }}</h2>
        <div class="trend-chart">
          <div class="trend-bar-wrapper" v-for="t in trends" :key="t.date">
            <div class="trend-bar" :style="{ height: barHeight(t.tokenCount) }" :title="`${t.date}: ${formatNum(t.tokenCount)} tokens`"></div>
            <span class="trend-label">{{ t.date.slice(5) }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useToast } from '../composables/useToast'
import { useI18n } from 'vue-i18n'
import { providerApi } from '../api/providers'
import { usageApi } from '../api/usage'
import type { UsageStats, UsageTrend, UsageRank, ProviderHealth } from '../api/usage'
import type { Provider } from '../types'

const { t } = useI18n()
const { show } = useToast()

const loading = ref(true)
const stats = reactive<UsageStats>({
  requestCount: 0, totalCost: 0, totalTokens: 0,
  promptTokens: 0, completionTokens: 0,
  avgLatencyMs: 0, errorCount: 0,
})
const statsProviders = reactive({ activeCount: 0, totalCount: 0 })
const models = ref<UsageRank[]>([])
const providers = ref<ProviderHealth[]>([])
const trends = ref<UsageTrend[]>([])

onMounted(async () => {
  try {
    const [[providersRes], [statsRes], [modelsRes], [healthRes], [trendsRes]] = await Promise.all([
      providerApi.list().then(r => [r]),
      usageApi.stats().then(r => [r]),
      usageApi.byModel('today').then(r => [r]),
      usageApi.byProvider('today').then(r => [r]),
      usageApi.trends('week').then(r => [r]),
    ])

    const pList: Provider[] = providersRes.data
    statsProviders.activeCount = pList.filter(p => p.status === 'enabled').length
    statsProviders.totalCount = pList.length

    Object.assign(stats, statsRes.data)
    models.value = modelsRes.data
    providers.value = healthRes.data
    trends.value = trendsRes.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

function formatNum(n: number) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M'
  if (n >= 1_000) return (n / 1_000).toFixed(1) + 'K'
  return String(n)
}

function barHeight(tokens: number) {
  const max = Math.max(...trends.value.map(t => t.tokenCount), 1)
  return Math.max(4, (tokens / max) * 120) + 'px'
}
</script>

<style scoped>
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 20px; color: #666; font-size: 13px; }

/* Cards */
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 16px 0; }
.card { background: white; padding: 16px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.card-label { display: block; font-size: 12px; color: #888; text-transform: uppercase; margin-bottom: 4px; }
.card-value { display: block; font-size: 24px; font-weight: 600; }
.card-sub { display: block; font-size: 11px; color: #999; margin-top: 4px; }
.unit { font-size: 13px; font-weight: 400; color: #888; margin-left: 4px; }
.text-danger { color: #c62828; }

/* Section */
.section { margin-top: 24px; }
.section h2 { font-size: 16px; margin-bottom: 12px; color: #333; }

/* Table */
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.table th { font-weight: 600; color: #666; font-size: 12px; text-transform: uppercase; }

/* Trend Chart */
.trend-chart { display: flex; align-items: flex-end; gap: 4px; height: 140px; background: white; padding: 12px; border-radius: 8px; overflow-x: auto; }
.trend-bar-wrapper { display: flex; flex-direction: column; align-items: center; flex: 1; min-width: 24px; }
.trend-bar { width: 100%; max-width: 32px; background: #1976d2; border-radius: 3px 3px 0 0; min-height: 4px; }
.trend-label { font-size: 10px; color: #999; margin-top: 4px; white-space: nowrap; }
</style>
