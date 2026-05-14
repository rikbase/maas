<template>
  <div class="dashboard">
    <BasePageHeader :title="$t('dashboard.title')" />

    <div v-if="loading" class="dashboard__loading">
      <BaseSpinner size="lg" />
    </div>
    <template v-else>
      <!-- KPI Bar -->
      <div class="section-label">{{ $t('dashboard.systemOverview') }}</div>
      <div class="kpi-bar">
        <div class="kpi-item bg-provider" @click="$router.push('/providers')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M20 17h2v-2h-2v2zm0-10v6h2V7h-2zm-2 12h-2v2h2v-2zM4 5h12v14H4V5zm14 14h2v-2h-2v2zm0-14h2V3h-2v2zM4 3v2h12V3H4zm0 16h12v2H4v-2z" fill="currentColor"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.providers.active }}<span class="kpi-sep">/{{ system.providers.total }}</span></span>
            <span class="kpi-label">{{ $t('dashboard.providers') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-model" @click="$router.push('/models')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" fill="none"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.models }}</span>
            <span class="kpi-label">{{ $t('dashboard.models') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-key" @click="$router.push('/keys')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M12.5 10.5a5 5 0 10-4 4l2.5 2.5 2-2 2 2 2-2-2-2-.5-.5" stroke="currentColor" stroke-width="1.5" fill="none"/><circle cx="8" cy="8" r="1.5" fill="currentColor"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.apiKeys.active }}<span class="kpi-sep">/{{ system.apiKeys.total }}</span></span>
            <span class="kpi-label">{{ $t('dashboard.apiKeys') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-skills" @click="$router.push('/skills')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.skills.published }}<span class="kpi-sep">/{{ system.skills.total }}</span></span>
            <span class="kpi-label">{{ $t('dashboard.skills') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-tools" @click="$router.push('/tools')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.tools.enabled }}<span class="kpi-sep">/{{ system.tools.total }}</span></span>
            <span class="kpi-label">{{ $t('dashboard.tools') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-mcp" @click="$router.push('/mcp/servers')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><rect x="2" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/><rect x="14" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/><rect x="2" y="14" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/><rect x="14" y="14" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.mcpServers.online }}<span class="kpi-sep">/{{ system.mcpServers.total }}</span></span>
            <span class="kpi-label">{{ $t('dashboard.mcpServers') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-dify" @click="$router.push('/dify')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15l-5-5 1.41-1.41L11 14.17l7.59-7.59L20 8l-9 9z" fill="currentColor"/></svg>
          <div class="kpi-body">
            <span class="kpi-value" v-if="system.dify.connected"><span class="kpi-dot kpi-dot--ok"></span></span>
            <span class="kpi-value" v-else-if="system.dify.exists"><span class="kpi-dot kpi-dot--err"></span></span>
            <span class="kpi-value" v-else><span class="kpi-dash">&ndash;</span></span>
            <span class="kpi-label">{{ $t('dashboard.dify') }}</span>
          </div>
        </div>
        <div class="kpi-item bg-security" @click="$router.push('/security/rules')">
          <svg class="kpi-icon" viewBox="0 0 24 24" fill="none"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M12 7v5M12 16h.01" stroke="currentColor" stroke-width="2" fill="none"/></svg>
          <div class="kpi-body">
            <span class="kpi-value">{{ system.security.rules }}</span>
            <span class="kpi-label">{{ $t('dashboard.rules') }}</span>
          </div>
        </div>
      </div>

      <!-- Usage & Login Activity Row -->
      <div class="section-label">{{ $t('dashboard.usageOverview') }}</div>
      <div class="content-row content-row--2col">
        <!-- Left: Usage Cards -->
        <div class="usage-grid">
          <div class="usage-card usage-card--primary">
            <div class="usage-header">
              <svg class="usage-icon" viewBox="0 0 24 24" fill="none"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor" opacity=".3"/><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
              <span class="usage-label">{{ $t('dashboard.requests') }}</span>
            </div>
            <span class="usage-value">{{ formatNum(stats.requestCount) }}</span>
          </div>
          <div class="usage-card usage-card--info">
            <div class="usage-header">
              <svg class="usage-icon" viewBox="0 0 24 24" fill="none"><path d="M12 4v16m8-8H4" stroke="currentColor" stroke-width="1.5"/></svg>
              <span class="usage-label">{{ $t('dashboard.totalTokens') }}</span>
            </div>
            <span class="usage-value">{{ formatNum(stats.totalTokens) }}</span>
            <div class="usage-bar">
              <div class="usage-bar-track">
                <div class="usage-bar-fill usage-bar-fill--prompt" :style="{ width: tokenPromptPct + '%' }"></div>
                <div class="usage-bar-fill usage-bar-fill--completion" :style="{ width: tokenCompletionPct + '%', left: tokenPromptPct + '%' }"></div>
              </div>
              <div class="usage-bar-labels">
                <span>P: {{ formatNum(stats.promptTokens) }}</span>
                <span>C: {{ formatNum(stats.completionTokens) }}</span>
              </div>
            </div>
          </div>
          <div class="usage-card usage-card--warning">
            <div class="usage-header">
              <svg class="usage-icon" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M12 7v5M12 16h.01" stroke="currentColor" stroke-width="2"/></svg>
              <span class="usage-label">{{ $t('dashboard.totalCost') }}</span>
            </div>
            <span class="usage-value">${{ stats.totalCost.toFixed(4) }}</span>
          </div>
          <div class="usage-card usage-card--danger">
            <div class="usage-header">
              <svg class="usage-icon" viewBox="0 0 24 24" fill="none"><path d="M12 2L1 21h22L12 2z" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2"/></svg>
              <span class="usage-label">{{ $t('dashboard.errors') }}</span>
            </div>
            <span class="usage-value" :class="{ 'usage-value--err': stats.errorCount > 0 }">{{ stats.errorCount }}</span>
            <span class="usage-sub">{{ stats.avgLatencyMs.toFixed(0) }}ms {{ $t('dashboard.avgLatency') }}</span>
          </div>
        </div>

        <!-- Right: Login Stats -->
        <BaseCard class="login-stats-card">
          <template #header><h2 class="trend-title">{{ $t('dashboard.loginActivity') }}</h2></template>
          <div class="login-stats-row">
            <div class="login-stat">
              <span class="login-stat-value login-stat-value--ok">{{ loginStats.todaySuccess }}</span>
              <span class="login-stat-label">{{ $t('dashboard.loginSuccess') }}</span>
            </div>
            <div class="login-stat">
              <span class="login-stat-value login-stat-value--fail">{{ loginStats.todayFailed }}</span>
              <span class="login-stat-label">{{ $t('dashboard.loginFailed') }}</span>
            </div>
            <div class="login-stat">
              <span class="login-stat-value">{{ loginStats.last24hAttempts }}</span>
              <span class="login-stat-label">{{ $t('dashboard.requests') }}</span>
            </div>
          </div>
          <div v-if="loginLogs.length > 0" class="login-compact-list">
            <div
              v-for="log in loginLogs.slice(0, 5)"
              :key="log.id"
              class="login-compact-row"
              :class="{ 'login-compact-row--fail': !log.success }"
            >
              <div class="login-compact-left">
                <span class="login-compact-user">{{ log.username }}</span>
                <span class="login-compact-ip">{{ log.ip || '-' }}</span>
              </div>
              <span class="login-compact-time">{{ formatLoginTime(log.createdAt) }}</span>
              <BaseBadge v-if="log.success" variant="success" class="login-compact-badge">{{ $t('dashboard.loginSuccess') }}</BaseBadge>
              <BaseBadge v-else variant="danger" class="login-compact-badge">{{ $t('dashboard.loginFailed') }}</BaseBadge>
            </div>
          </div>
          <BaseEmpty v-else :text="$t('dashboard.noData')" />
        </BaseCard>
      </div>

      <!-- Trends side by side -->
      <div class="trends-row">
        <BaseCard class="trend-card" v-if="trends.length > 1">
          <template #header><h2 class="trend-title">{{ $t('dashboard.tokenTrends') }}</h2></template>
          <div class="chart">
            <div class="chart-bar-group" v-for="t in trends" :key="t.date">
              <div class="chart-bar" :style="{ height: barHeight(t.tokenCount) }" :title="`${t.date}: ${formatNum(t.tokenCount)} tokens`"></div>
              <span class="chart-label">{{ t.date.slice(5) }}</span>
            </div>
          </div>
        </BaseCard>
        <BaseCard class="trend-card" v-if="execTrends.length > 1">
          <template #header><h2 class="trend-title">{{ $t('dashboard.execTrends') }}</h2></template>
          <div class="chart">
            <div class="chart-bar-group" v-for="t in execTrends" :key="t.date">
              <div class="chart-stacked" :style="{ height: execBarHeight(t.totalCount) }">
                <div v-if="t.completedCount > 0" class="chart-segment chart-seg--ok" :style="{ height: execSegPct(t.completedCount) }" :title="`${t.date}: ${t.completedCount} completed`"></div>
                <div v-if="t.runningCount > 0" class="chart-segment chart-seg--run" :style="{ height: execSegPct(t.runningCount) }" :title="`${t.date}: ${t.runningCount} running`"></div>
                <div v-if="t.pendingCount > 0" class="chart-segment chart-seg--pend" :style="{ height: execSegPct(t.pendingCount) }" :title="`${t.date}: ${t.pendingCount} pending`"></div>
                <div v-if="t.failedCount > 0" class="chart-segment chart-seg--fail" :style="{ height: execSegPct(t.failedCount) }" :title="`${t.date}: ${t.failedCount} failed`"></div>
                <div v-if="t.cancelledCount > 0" class="chart-segment chart-seg--cancel" :style="{ height: execSegPct(t.cancelledCount) }" :title="`${t.date}: ${t.cancelledCount} cancelled`"></div>
              </div>
              <span class="chart-label">{{ t.date.slice(5) }}</span>
            </div>
          </div>
          <div class="chart-legend">
            <span><i class="dot dot--ok"></i>{{ $t('workflow.runStatuses.completed') }}</span>
            <span><i class="dot dot--run"></i>{{ $t('workflow.runStatuses.running') }}</span>
            <span><i class="dot dot--pend"></i>{{ $t('workflow.runStatuses.pending') }}</span>
            <span><i class="dot dot--fail"></i>{{ $t('workflow.runStatuses.failed') }}</span>
            <span><i class="dot dot--cancel"></i>{{ $t('workflow.runStatuses.cancelled') }}</span>
          </div>
        </BaseCard>
      </div>

      <!-- Tables -->
      <div class="tables-row">
        <BaseCard class="table-card" no-padding>
          <template #header><h2 class="trend-title">{{ $t('dashboard.topModels') }}</h2></template>
          <!-- Usage-based ranking (when data exists) -->
          <table v-if="models.length > 0" class="dash-table">
            <thead>
              <tr><th>#</th><th>{{ $t('provider.name') }}</th><th class="col-r">{{ $t('dashboard.requests') }}</th><th class="col-r">{{ $t('dashboard.totalTokens') }}</th><th class="col-r">{{ $t('dashboard.totalCost') }}</th></tr>
            </thead>
            <tbody>
              <tr v-for="(m, i) in models.slice(0, 6)" :key="m.name + i">
                <td class="col-rank"><span class="rank-badge" :class="'rank--' + (i + 1)">{{ i + 1 }}</span></td>
                <td>{{ m.name }}</td>
                <td class="col-r">{{ m.requestCount }}</td>
                <td class="col-r">{{ formatNum(m.tokenCount) }}</td>
                <td class="col-r">${{ m.cost.toFixed(4) }}</td>
              </tr>
            </tbody>
          </table>
          <!-- Fallback: all registered models (no usage data yet) -->
          <table v-else-if="allModels.length > 0" class="dash-table">
            <thead>
              <tr><th>#</th><th>{{ $t('provider.name') }}</th><th>{{ $t('model.provider') }}</th><th class="col-r">{{ $t('model.status') }}</th></tr>
            </thead>
            <tbody>
              <tr v-for="(m, i) in allModels.slice(0, 6)" :key="m.id">
                <td class="col-rank"><span class="rank-badge rank--all">{{ i + 1 }}</span></td>
                <td>{{ m.modelName }}</td>
                <td>{{ m.providerName }}</td>
                <td class="col-r"><span class="model-status-dot" :class="m.status === 'active' ? 'msd--on' : 'msd--off'"></span>{{ m.status }}</td>
              </tr>
            </tbody>
          </table>
          <BaseEmpty v-else :text="$t('dashboard.noData')" />
        </BaseCard>
        <BaseCard class="table-card" no-padding>
          <template #header><h2 class="trend-title">{{ $t('dashboard.providerHealth') }}</h2></template>
          <!-- Usage-based health (when data exists) -->
          <table v-if="providers.length > 0" class="dash-table">
            <thead>
              <tr><th>{{ $t('provider.name') }}</th><th class="col-r">{{ $t('dashboard.requests') }}</th><th class="col-r">{{ $t('dashboard.errors') }}</th><th class="col-r">{{ $t('dashboard.avgLatency') }}</th></tr>
            </thead>
            <tbody>
              <tr v-for="p in providers.slice(0, 6)" :key="p.providerName">
                <td>
                  <span class="health-dot" :class="healthDotClass(p)"></span>
                  {{ p.providerName }}
                </td>
                <td class="col-r">{{ p.requestCount }}</td>
                <td class="col-r">
                  <span v-if="p.errorCount > 0" class="err-pill">{{ p.errorCount }}</span>
                  <span v-else class="col-zero">0</span>
                </td>
                <td class="col-r">{{ p.avgLatencyMs.toFixed(0) }}ms</td>
              </tr>
            </tbody>
          </table>
          <!-- Fallback: all registered providers (no usage data yet) -->
          <table v-else-if="allProviders.length > 0" class="dash-table">
            <thead>
              <tr><th>{{ $t('provider.name') }}</th><th>{{ $t('provider.type') }}</th><th class="col-r">{{ $t('provider.status') }}</th><th class="col-r">{{ $t('provider.health') }}</th></tr>
            </thead>
            <tbody>
              <tr v-for="p in allProviders.slice(0, 6)" :key="p.id">
                <td>
                  <span class="health-dot" :class="p.healthStatus === 'healthy' ? 'health-dot--ok' : 'health-dot--warn'"></span>
                  {{ p.name }}
                </td>
                <td>{{ typeLabel(p.type) }}</td>
                <td class="col-r">{{ $t('provider.statuses.' + p.status) }}</td>
                <td class="col-r"><span class="status-pill" :class="p.healthStatus === 'healthy' ? 'sp--ok' : 'sp--warn'">{{ $t('provider.healthStatuses.' + p.healthStatus) }}</span></td>
              </tr>
            </tbody>
          </table>
          <BaseEmpty v-else :text="$t('dashboard.noData')" />
        </BaseCard>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useToast } from '../composables/useToast'
import { useI18n } from 'vue-i18n'
import { providerApi, modelApi } from '../api/providers'
import type { ModelDef } from '../api/providers'
import { keyApi } from '../api/keys'
import { mcpServerApi } from '../api/mcp'
import { difyApi } from '../api/dify'
import { ruleApi, eventApi } from '../api/security'
import { usageApi } from '../api/usage'
import { workflowApi, executionApi } from '../api/workflows'
import { loginLogApi, type LoginLogVO, type LoginStatsVO } from '../api/loginLog'
import { skillApi, toolApi } from '../api/registry'
import type { UsageStats, UsageTrend, UsageRank, ProviderHealth } from '../api/usage'
import type { ExecutionTrend } from '../api/workflows'
import type { Provider } from '../types'
import BasePageHeader from '../components/ui/BasePageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import BaseEmpty from '../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()

const loading = ref(true)

const system = reactive({
  providers: { active: 0, total: 0 },
  models: 0,
  apiKeys: { active: 0, total: 0 },
  mcpServers: { online: 0, total: 0 },
  dify: { exists: false, connected: false },
  security: { rules: 0, events: 0 },
  skills: { published: 0, total: 0 },
  tools: { enabled: 0, total: 0 },
  workflows: { published: 0, total: 0 },
})

const stats = reactive<UsageStats>({
  requestCount: 0, totalCost: 0, totalTokens: 0,
  promptTokens: 0, completionTokens: 0,
  avgLatencyMs: 0, errorCount: 0,
})

const models = ref<UsageRank[]>([])
const allModels = ref<ModelDef[]>([])
const providers = ref<ProviderHealth[]>([])
const allProviders = ref<Provider[]>([])
const trends = ref<UsageTrend[]>([])
const execTrends = ref<ExecutionTrend[]>([])
const loginLogs = ref<LoginLogVO[]>([])
const loginStats = reactive<LoginStatsVO>({ todaySuccess: 0, todayFailed: 0, last24hAttempts: 0 })

const tokenPromptPct = computed(() => {
  const total = stats.promptTokens + stats.completionTokens
  return total > 0 ? (stats.promptTokens / total) * 100 : 0
})
const tokenCompletionPct = computed(() => {
  const total = stats.promptTokens + stats.completionTokens
  return total > 0 ? (stats.completionTokens / total) * 100 : 0
})

onMounted(async () => {
  try {
    const [
      [providersRes],
      [modelsRes],
      [keysRes],
      [mcpRes],
      [difyRes],
      [rulesRes],
      [skillsRes],
      [toolsRes],
      [wfRes],
      [statsRes],
      [modelRankRes],
      [healthRes],
      [trendsRes],
      [execTrendsRes],
      [secStatsRes],
      [loginRecentRes],
      [loginStatsRes],
    ] = await Promise.all([
      providerApi.list().then(r => [r]),
      modelApi.list().then(r => [r]),
      keyApi.list().then(r => [r]),
      mcpServerApi.list().then(r => [r]),
      difyApi.list().then(r => [r]),
      ruleApi.list().then(r => [r]),
      skillApi.list().then(r => [r]),
      toolApi.list().then(r => [r]),
      workflowApi.list().then(r => [r]),
      usageApi.stats().then(r => [r]),
      usageApi.byModel('today').then(r => [r]),
      usageApi.byProvider('today').then(r => [r]),
      usageApi.trends('week').then(r => [r]),
      executionApi.trends('week').then(r => [r]),
      eventApi.stats().then(r => [r]).catch(() => [null]),
      loginLogApi.recent().then(r => [r]).catch(() => [null]),
      loginLogApi.stats().then(r => [r]).catch(() => [null]),
    ])

    const pList: Provider[] = providersRes.data
    system.providers.active = pList.filter(p => p.status === 'enabled').length
    system.providers.total = pList.length
    allProviders.value = pList

    system.models = modelsRes.data?.length ?? 0
    allModels.value = modelsRes.data ?? []

    system.mcpServers.online = mcpRes.data.filter((s: any) => s.status === 'online').length
    system.mcpServers.total = mcpRes.data.length

    const difyList = difyRes.data
    system.dify.exists = difyList.length > 0
    system.dify.connected = difyList.some((c: any) => c.status === 'connected')

    system.apiKeys.total = keysRes.data?.length ?? 0
    system.apiKeys.active = keysRes.data?.filter((k: any) => k.status !== 'revoked').length ?? 0

    system.security.rules = rulesRes.data?.length ?? 0
    system.security.events = secStatsRes?.data?.totalEvents ?? 0

    system.skills.total = skillsRes.data?.length ?? 0
    system.skills.published = skillsRes.data?.filter((s: any) => s.status === 'published').length ?? 0
    system.tools.total = toolsRes.data?.length ?? 0
    system.tools.enabled = toolsRes.data?.filter((t: any) => t.enabled).length ?? 0
    system.workflows.total = wfRes.data?.length ?? 0
    system.workflows.published = wfRes.data?.filter((w: any) => w.status === 'published').length ?? 0

    Object.assign(stats, statsRes.data)
    models.value = modelRankRes.data
    providers.value = healthRes.data
    trends.value = trendsRes.data
    execTrends.value = execTrendsRes.data

    if (loginRecentRes?.data) loginLogs.value = loginRecentRes.data
    if (loginStatsRes?.data) Object.assign(loginStats, loginStatsRes.data)
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

function typeLabel(type: string) {
  const map: Record<string, string> = {
    openai_compatible: 'OpenAI',
    anthropic: 'Anthropic',
    vllm: 'vLLM',
    ollama: 'Ollama',
    custom: 'Custom',
  }
  return map[type] || type
}

function barHeight(tokens: number) {
  const max = Math.max(...trends.value.map(t => t.tokenCount), 1)
  return Math.max(3, (tokens / max) * 48) + 'px'
}

function execBarHeight(total: number) {
  const max = Math.max(...execTrends.value.map(t => t.totalCount), 1)
  return Math.max(3, (total / max) * 48) + 'px'
}

function execSegPct(count: number) {
  const max = Math.max(...execTrends.value.map(t => t.totalCount), 1)
  return Math.max(1, (count / max) * 48) + 'px'
}

function formatLoginTime(iso: string) {
  const d = new Date(iso)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function healthDotClass(p: ProviderHealth) {
  if (p.errorCount > 0) return 'health-dot--warn'
  if (p.avgLatencyMs > 2000) return 'health-dot--warn'
  return 'health-dot--ok'
}
</script>

<style scoped>
.dashboard {
  padding-bottom: 0;
}
.dashboard__loading {
  display: flex;
  justify-content: center;
  padding: var(--space-8) 0;
}

/* ── KPI Bar ── */
.kpi-bar {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: var(--space-1);
  margin-bottom: var(--space-2);
}
.kpi-item {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px var(--space-2);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: transform .15s, box-shadow .15s;
  border: 1px solid var(--color-border);
}
.kpi-item:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}
.bg-provider { background: linear-gradient(135deg, #eef2ff, #e0e7ff); }
.bg-model   { background: linear-gradient(135deg, #f0f9ff, #dbeafe); }
.bg-key     { background: linear-gradient(135deg, #fffbeb, #fef3c7); }
.bg-mcp     { background: linear-gradient(135deg, #ecfdf5, #d1fae5); }
.bg-dify    { background: linear-gradient(135deg, #f5f3ff, #ede9fe); }
.bg-skills  { background: linear-gradient(135deg, #ecfeff, #cffafe); }
.bg-tools   { background: linear-gradient(135deg, #f5f3ff, #e9d5ff); }
.bg-security{ background: linear-gradient(135deg, #fef2f2, #fee2e2); }

.kpi-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  opacity: .7;
}
.bg-provider .kpi-icon { color: var(--color-primary); }
.bg-model .kpi-icon    { color: #2563eb; }
.bg-key .kpi-icon      { color: #d97706; }
.bg-skills .kpi-icon   { color: #0891b2; }
.bg-tools .kpi-icon    { color: #7c3aed; }
.bg-mcp .kpi-icon      { color: #059669; }
.bg-dify .kpi-icon     { color: #7c3aed; }
.bg-security .kpi-icon { color: #dc2626; }

.kpi-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.kpi-value {
  font-size: .929rem;
  font-weight: 800;
  color: var(--color-foreground);
  line-height: 1.2;
}
.kpi-dash {
  font-size: .857rem;
  font-weight: 400;
  color: var(--color-foreground-secondary);
}
.kpi-sep {
  font-size: .786rem;
  font-weight: 500;
  color: var(--color-foreground-secondary);
}
.kpi-label {
  font-size: .643rem;
  font-weight: 500;
  color: var(--color-foreground-secondary);
  text-transform: uppercase;
  letter-spacing: .3px;
}
.kpi-dot {
  display: inline-block;
  width: 10px; height: 10px;
  border-radius: 50%;
  vertical-align: middle;
}
.kpi-dot--ok { background: var(--color-success); box-shadow: 0 0 6px rgba(16,185,129,.4); }
.kpi-dot--err { background: var(--color-danger); box-shadow: 0 0 6px rgba(239,68,68,.4); }

/* ── Usage Cards ── */
.usage-card {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-lg);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
}
.usage-card--primary { border-left: 3px solid var(--color-primary); }
.usage-card--info    { border-left: 3px solid #3b82f6; }
.usage-card--warning { border-left: 3px solid #d97706; }
.usage-card--danger  { border-left: 3px solid #dc2626; }

.usage-header {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-1);
}
.usage-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}
.usage-card--primary .usage-icon { color: var(--color-primary); }
.usage-card--info .usage-icon    { color: #3b82f6; }
.usage-card--warning .usage-icon { color: #d97706; }
.usage-card--danger .usage-icon  { color: #dc2626; }

.usage-label {
  font-size: .786rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .4px;
  color: var(--color-foreground-secondary);
}
.usage-value {
  display: block;
  font-size: 1rem;
  font-weight: 800;
  color: var(--color-foreground);
  line-height: 1.1;
}
.usage-value--err { color: var(--color-danger); }
.usage-sub {
  display: block;
  font-size: .714rem;
  color: var(--color-foreground-secondary);
  margin-top: 1px;
}

/* usage bar */
.usage-bar { margin-top: var(--space-1); }
.usage-bar-track {
  position: relative;
  height: 4px;
  background: var(--color-bg-page);
  border-radius: 3px;
  overflow: hidden;
}
.usage-bar-fill {
  position: absolute;
  top: 0; left: 0;
  height: 100%;
  border-radius: 3px;
  transition: width .4s;
}
.usage-bar-fill--prompt    { background: var(--color-primary); }
.usage-bar-fill--completion { background: #818cf8; }
.usage-bar-labels {
  display: flex;
  justify-content: space-between;
  font-size: .643rem;
  color: var(--color-foreground-secondary);
  margin-top: 2px;
}

/* ── Trends Row ── */
.trends-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}
.trend-card {
  min-height: 0;
}
.trend-title {
  font-size: .857rem;
  font-weight: 700;
  color: var(--color-foreground);
  margin: 0;
}

.chart {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 50px;
  overflow-x: auto;
  padding-top: 2px;
}
.chart-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 16px;
}
.chart-bar {
  width: 100%;
  max-width: 28px;
  background: linear-gradient(to top, var(--color-primary), #a5b4fc);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  min-height: 4px;
  transition: opacity .15s;
}
.chart-bar:hover { opacity: .7; }
.chart-label {
  font-size: .571rem;
  color: var(--color-foreground-secondary);
  margin-top: 2px;
  white-space: nowrap;
}

.chart-stacked {
  width: 100%;
  max-width: 28px;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column-reverse;
  min-height: 4px;
}
.chart-segment {
  width: 100%;
  min-height: 2px;
  transition: opacity .15s;
}
.chart-segment:hover { opacity: .7; }
.chart-seg--ok     { background: var(--color-success); }
.chart-seg--run    { background: #3b82f6; }
.chart-seg--pend   { background: #f59e0b; }
.chart-seg--fail   { background: var(--color-danger); }
.chart-seg--cancel { background: var(--color-foreground-secondary); }

.chart-legend {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 2px;
  font-size: .571rem;
  color: var(--color-foreground-secondary);
}
.dot {
  display: inline-block;
  width: 8px; height: 8px;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
}
.dot--ok     { background: var(--color-success); }
.dot--run    { background: #3b82f6; }
.dot--pend   { background: #f59e0b; }
.dot--fail   { background: var(--color-danger); }
.dot--cancel { background: var(--color-foreground-secondary); }

/* ── Tables Row ── */
.tables-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
}

.dash-table {
  width: 100%;
  border-collapse: collapse;
}
.dash-table th,
.dash-table td {
  padding: 2px var(--space-2);
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  font-size: .786rem;
  color: var(--color-foreground);
}
.dash-table th {
  font-weight: 600;
  font-size: .643rem;
  text-transform: uppercase;
  letter-spacing: .4px;
  color: var(--color-foreground-secondary);
  background: var(--color-bg-page);
}
.dash-table tr:last-child td { border-bottom: none; }
.dash-table tbody tr:hover { background: var(--color-bg-page); }

.col-r { text-align: right; font-variant-numeric: tabular-nums; }
.col-zero { color: var(--color-foreground-secondary); }

/* rank badges */
.col-rank { width: 32px; }
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px; height: 22px;
  border-radius: 6px;
  font-size: .714rem;
  font-weight: 700;
}
.rank--1 { background: linear-gradient(135deg, #fbbf24, #f59e0b); color: #fff; }
.rank--2 { background: linear-gradient(135deg, #e2e8f0, #cbd5e1); color: #475569; }
.rank--3 { background: linear-gradient(135deg, #fdba74, #f97316); color: #fff; }
.rank--4,
.rank--5,
.rank--6,
.rank--7,
.rank--8,
.rank--all { background: var(--color-bg-page); color: var(--color-foreground-secondary); }

/* health dot */
.health-dot {
  display: inline-block;
  width: 8px; height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
.health-dot--ok   { background: var(--color-success); }
.health-dot--warn { background: #f59e0b; }

/* error pill */
/* error pill */
.err-pill {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: .786rem;
  font-weight: 600;
  color: #fff;
  background: var(--color-danger);
}

/* model status dot */
.model-status-dot {
  display: inline-block;
  width: 6px; height: 6px;
  border-radius: 50%;
  margin-right: 5px;
  vertical-align: middle;
}
.msd--on  { background: var(--color-success); }
.msd--off { background: var(--color-foreground-secondary); }

/* status pill */
.status-pill {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: .786rem;
  font-weight: 600;
}
.sp--ok   { background: #d1fae5; color: #065f46; }
.sp--warn { background: #fee2e2; color: #991b1b; }

/* ── Section Labels ── */
.section-label {
  font-size: .643rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .4px;
  color: var(--color-foreground-secondary);
  margin-bottom: var(--space-1);
  margin-top: var(--space-2);
}
.section-label:first-of-type {
  margin-top: 0;
}

/* ── Content Row (2 column) ── */
.content-row--2col {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

/* ── Usage Grid (4 cards inside left column) ── */
.usage-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-1);
}

/* ── Login Stats Card (right column) ── */
.login-stats-card {
  min-height: 0;
}
.login-stats-row {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
  margin-bottom: var(--space-1);
  padding-bottom: var(--space-1);
  border-bottom: 1px solid var(--color-border);
}
.login-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.login-stat-value {
  font-size: 1rem;
  font-weight: 800;
  color: var(--color-foreground);
}
.login-stat-value--ok { color: var(--color-success); }
.login-stat-value--fail { color: var(--color-danger); }
.login-stat-label {
  font-size: .571rem;
  color: var(--color-foreground-secondary);
  text-transform: uppercase;
  letter-spacing: .3px;
  margin-top: 1px;
}
.login-compact-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.login-compact-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-1);
  border-radius: var(--radius-sm);
  font-size: .643rem;
  gap: var(--space-1);
}
.login-compact-row:hover { background: var(--color-bg-page); }
.login-compact-row--fail { background: rgba(239,68,68,.03); }
.login-compact-left {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}
.login-compact-user {
  font-weight: 600;
  color: var(--color-foreground);
  line-height: 1.2;
}
.login-compact-ip {
  font-family: var(--font-mono);
  font-size: .643rem;
  color: var(--color-foreground-secondary);
}
.login-compact-time {
  font-family: var(--font-mono);
  font-size: .643rem;
  color: var(--color-foreground-secondary);
  white-space: nowrap;
}
.login-compact-badge {
  flex-shrink: 0;
}
</style>
