<template>
  <div class="dashboard">
    <div class="dash-content">
      <BasePageHeader :title="$t('dashboard.title')" class="anim-section" :style="{ '--anim-delay': '0ms' }" />

      <template v-if="loading">
        <div class="dash-loading">
          <BaseSpinner size="lg" />
        </div>
      </template>
      <template v-else>
        <!-- ==================== KPI Grid ==================== -->
        <div class="section-label anim-section" :style="{ '--anim-delay': '60ms' }">{{ $t('dashboard.systemOverview') }}</div>
        <div class="kpi-grid anim-section" :style="{ '--anim-delay': '100ms' }">
          <div class="kpi-card kpi-card--provider" @click="$router.push('/providers')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M20 17h2v-2h-2v2zm0-10v6h2V7h-2zm-2 12h-2v2h2v-2zM4 5h12v14H4V5zm14 14h2v-2h-2v2zm0-14h2V3h-2v2zM4 3v2h12V3H4zm0 16h12v2H4v-2z" fill="currentColor"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countProviders }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.providers') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--model" @click="$router.push('/models')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" fill="none"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countModels }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.models') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--key" @click="$router.push('/keys')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12.5 10.5a5 5 0 10-4 4l2.5 2.5 2-2 2 2 2-2-2-2-.5-.5" stroke="currentColor" stroke-width="1.5" fill="none"/><circle cx="8" cy="8" r="1.5" fill="currentColor"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countKeys }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.apiKeys') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--skill" @click="$router.push('/skills')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countSkills }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.skills') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--tool" @click="$router.push('/tools')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countTools }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.tools') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--mcp" @click="$router.push('/mcp/servers')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><rect x="2" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/><rect x="14" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/><rect x="2" y="14" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/><rect x="14" y="14" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countMcp }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.mcpServers') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--dify" @click="$router.push('/dify')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15l-5-5 1.41-1.41L11 14.17l7.59-7.59L20 8l-9 9z" fill="currentColor"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">
                  <span v-if="system.dify.connected" class="status-glow-sm status-glow-sm--ok"></span>
                  <span v-else-if="system.dify.exists" class="status-glow-sm status-glow-sm--warn"></span>
                  <span v-else class="kpi-card__dash">&ndash;</span>
                </span>
                <span class="kpi-card__label">{{ $t('dashboard.dify') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--security" @click="$router.push('/security/rules')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M12 7v5M12 16h.01" stroke="currentColor" stroke-width="2" fill="none"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countRules }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.rules') }}</span>
              </div>
            </div>
          </div>
          <div class="kpi-card kpi-card--user" @click="$router.push('/users')">
            <div class="kpi-card__bar"></div>
            <div class="kpi-card__inner">
              <div class="kpi-card__icon-wrap">
                <svg class="kpi-card__icon" viewBox="0 0 24 24" fill="none"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.5" fill="none"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
              </div>
              <div class="kpi-card__body">
                <span class="kpi-card__value">{{ countUsers }}</span>
                <span class="kpi-card__label">{{ $t('dashboard.users') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- ==================== Usage & Login ==================== -->
        <div class="section-label anim-section" :style="{ '--anim-delay': '140ms' }">{{ $t('dashboard.usageOverview') }}</div>
        <div class="content-row anim-section" :style="{ '--anim-delay': '180ms' }">
          <div class="usage-grid">
            <div class="glass-card usage-card usage-card--primary">
              <div class="usage-card__bar"></div>
              <div class="usage-card__inner">
                <div class="usage-card__icon-wrap">
                  <svg class="usage-card__icon" viewBox="0 0 24 24" fill="none"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" fill="currentColor" opacity=".3"/><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" stroke="currentColor" stroke-width="1.5" fill="none"/></svg>
                </div>
                <span class="usage-card__value">{{ formatNum(stats.requestCount) }}</span>
                <span class="usage-card__label">{{ $t('dashboard.requests') }}</span>
              </div>
            </div>
            <div class="glass-card usage-card usage-card--info">
              <div class="usage-card__bar"></div>
              <div class="usage-card__inner">
                <div class="usage-card__icon-wrap">
                  <svg class="usage-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12 4v16m8-8H4" stroke="currentColor" stroke-width="1.5"/></svg>
                </div>
                <span class="usage-card__value">{{ formatNum(stats.totalTokens) }}</span>
                <span class="usage-card__label">{{ $t('dashboard.totalTokens') }}</span>
                <div class="usage-card__tokens-bar">
                  <div class="uctb__track">
                    <div class="uctb__fill uctb__fill--prompt" :style="{ width: tokenPromptPct + '%' }"></div>
                    <div class="uctb__fill uctb__fill--completion" :style="{ width: tokenCompletionPct + '%', left: tokenPromptPct + '%' }"></div>
                  </div>
                  <div class="uctb__labels">
                    <span>{{ formatNum(stats.promptTokens) }}</span>
                    <span>{{ formatNum(stats.completionTokens) }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="glass-card usage-card usage-card--warning">
              <div class="usage-card__bar"></div>
              <div class="usage-card__inner">
                <div class="usage-card__icon-wrap">
                  <svg class="usage-card__icon" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M12 7v5M12 16h.01" stroke="currentColor" stroke-width="2"/></svg>
                </div>
                <span class="usage-card__value">${{ stats.totalCost.toFixed(4) }}</span>
                <span class="usage-card__label">{{ $t('dashboard.totalCost') }}</span>
              </div>
            </div>
            <div class="glass-card usage-card usage-card--danger">
              <div class="usage-card__bar"></div>
              <div class="usage-card__inner">
                <div class="usage-card__icon-wrap">
                  <svg class="usage-card__icon" viewBox="0 0 24 24" fill="none"><path d="M12 2L1 21h22L12 2z" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2"/></svg>
                </div>
                <span class="usage-card__value" :class="{ 'usage-card__value--err': stats.errorCount > 0 }">{{ stats.errorCount }}</span>
                <span class="usage-card__label">{{ stats.avgLatencyMs.toFixed(0) }}ms {{ $t('dashboard.avgLatency') }}</span>
              </div>
            </div>
          </div>

          <!-- Login Activity -->
          <div class="glass-card login-card">
            <div class="login-card__header">
              <svg class="login-card__header-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M13 12H3"/>
              </svg>
              <span>{{ $t('dashboard.loginActivity') }}</span>
            </div>
            <div class="login-card__stats">
              <div class="login-card__stat">
                <span class="login-card__stat-value login-card__stat-value--ok">{{ loginStats.todaySuccess }}</span>
                <span class="login-card__stat-label">{{ $t('dashboard.loginSuccess') }}</span>
              </div>
              <div class="login-card__stat">
                <span class="login-card__stat-value login-card__stat-value--fail">{{ loginStats.todayFailed }}</span>
                <span class="login-card__stat-label">{{ $t('dashboard.loginFailed') }}</span>
              </div>
              <div class="login-card__stat">
                <span class="login-card__stat-value">{{ loginStats.last24hAttempts }}</span>
                <span class="login-card__stat-label">{{ $t('dashboard.last24h') }}</span>
              </div>
            </div>
            <div v-if="loginLogs.length > 0" class="login-card__log-list">
              <div v-for="log in loginLogs.slice(0, 5)" :key="log.id" class="login-card__log-row" :class="{ 'login-card__log-row--fail': !log.success }">
                <div class="login-card__log-left">
                  <span class="login-card__log-user">{{ log.username }}</span>
                  <span class="login-card__log-ip">{{ log.ip || '-' }}</span>
                </div>
                <span class="login-card__log-time">{{ formatLoginTime(log.createdAt) }}</span>
                <BaseBadge v-if="log.success" variant="success">{{ $t('dashboard.loginSuccess') }}</BaseBadge>
                <BaseBadge v-else variant="danger">{{ $t('dashboard.loginFailed') }}</BaseBadge>
              </div>
            </div>
            <BaseEmpty v-else :text="$t('dashboard.noData')" />
          </div>
        </div>

        <!-- ==================== Trends ==================== -->
        <div class="trends-row anim-section" :style="{ '--anim-delay': '240ms' }">
          <div class="glass-card trend-card" v-if="trends.length > 1">
            <div class="trend-card__header">{{ $t('dashboard.tokenTrends') }}</div>
            <div class="trend-card__chart">
              <div class="trend-card__bar-group" v-for="t in trends" :key="t.date">
                <div class="trend-card__bar" :style="{ height: barHeight(t.tokenCount) }" :title="`${t.date}: ${formatNum(t.tokenCount)} tokens`"></div>
                <span class="trend-card__label">{{ t.date.slice(5) }}</span>
              </div>
            </div>
          </div>
          <div class="glass-card trend-card" v-if="execTrends.length > 1">
            <div class="trend-card__header">{{ $t('dashboard.execTrends') }}</div>
            <div class="trend-card__chart">
              <div class="trend-card__bar-group" v-for="t in execTrends" :key="t.date">
                <div class="trend-card__stacked" :style="{ height: execBarHeight(t.totalCount) }">
                  <div v-if="t.completedCount > 0" class="trend-card__segment trend-card__seg--ok" :style="{ height: execSegPct(t.completedCount) }" :title="`${t.date}: ${t.completedCount} completed`"></div>
                  <div v-if="t.runningCount > 0" class="trend-card__segment trend-card__seg--run" :style="{ height: execSegPct(t.runningCount) }" :title="`${t.date}: ${t.runningCount} running`"></div>
                  <div v-if="t.pendingCount > 0" class="trend-card__segment trend-card__seg--pend" :style="{ height: execSegPct(t.pendingCount) }" :title="`${t.date}: ${t.pendingCount} pending`"></div>
                  <div v-if="t.failedCount > 0" class="trend-card__segment trend-card__seg--fail" :style="{ height: execSegPct(t.failedCount) }" :title="`${t.date}: ${t.failedCount} failed`"></div>
                  <div v-if="t.cancelledCount > 0" class="trend-card__segment trend-card__seg--cancel" :style="{ height: execSegPct(t.cancelledCount) }" :title="`${t.date}: ${t.cancelledCount} cancelled`"></div>
                </div>
                <span class="trend-card__label">{{ t.date.slice(5) }}</span>
              </div>
            </div>
            <div class="trend-card__legend">
              <span><i class="trend-card__legend-dot trend-card__legend-dot--ok"></i>{{ $t('workflow.runStatuses.completed') }}</span>
              <span><i class="trend-card__legend-dot trend-card__legend-dot--run"></i>{{ $t('workflow.runStatuses.running') }}</span>
              <span><i class="trend-card__legend-dot trend-card__legend-dot--pend"></i>{{ $t('workflow.runStatuses.pending') }}</span>
              <span><i class="trend-card__legend-dot trend-card__legend-dot--fail"></i>{{ $t('workflow.runStatuses.failed') }}</span>
              <span><i class="trend-card__legend-dot trend-card__legend-dot--cancel"></i>{{ $t('workflow.runStatuses.cancelled') }}</span>
            </div>
          </div>
        </div>

        <!-- ==================== Tables ==================== -->
        <div class="tables-row anim-section" :style="{ '--anim-delay': '320ms' }">
          <div class="glass-card table-card">
            <div class="table-card__header">{{ $t('dashboard.topModels') }}</div>
            <table v-if="models.length > 0" class="table-card__table">
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
            <table v-else-if="allModels.length > 0" class="table-card__table">
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
          </div>
          <div class="glass-card table-card">
            <div class="table-card__header">{{ $t('dashboard.providerHealth') }}</div>
            <table v-if="providers.length > 0" class="table-card__table">
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
            <table v-else-if="allProviders.length > 0" class="table-card__table">
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
          </div>
        </div>
      </template>
    </div>
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
import { userApi } from '../api/users'
import { skillApi, toolApi } from '../api/registry'
import type { UsageStats, UsageTrend, UsageRank, ProviderHealth } from '../api/usage'
import type { ExecutionTrend } from '../api/workflows'
import type { Provider } from '../types'
import BasePageHeader from '../components/ui/BasePageHeader.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import BaseEmpty from '../components/ui/BaseEmpty.vue'
import { useCountUp } from '../composables/useCountUp'

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
  users: { active: 0, total: 0 },
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

// Animated KPI counters
const animProvidersActive = computed(() => system.providers.active)
const animModels = computed(() => system.models)
const animKeysActive = computed(() => system.apiKeys.active)
const animSkillsPublished = computed(() => system.skills.published)
const animToolsEnabled = computed(() => system.tools.enabled)
const animMcpOnline = computed(() => system.mcpServers.online)
const animRules = computed(() => system.security.rules)
const animUsers = computed(() => system.users.total)
const countProviders = useCountUp(animProvidersActive)
const countModels = useCountUp(animModels)
const countKeys = useCountUp(animKeysActive)
const countSkills = useCountUp(animSkillsPublished)
const countTools = useCountUp(animToolsEnabled)
const countMcp = useCountUp(animMcpOnline)
const countRules = useCountUp(animRules)
const countUsers = useCountUp(animUsers)

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
      [usersRes],
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
      userApi.list().then(r => [r]).catch(() => [null]),
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

    if (usersRes?.data) {
      system.users.total = usersRes.data.length
      system.users.active = usersRes.data.filter((u: any) => u.status === 'active').length
    }

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
/* ── Dashboard Container ── */
.dashboard {
  --glass-bg: rgba(12, 12, 35, 0.55);
  --glass-bg-hover: rgba(20, 20, 50, 0.70);
  --glass-border: rgba(99, 102, 241, 0.15);
  --glass-border-hover: rgba(99, 102, 241, 0.40);
  --glass-text: rgba(255, 255, 255, 0.92);
  --glass-text-secondary: rgba(255, 255, 255, 0.65);
  --glass-text-muted: rgba(255, 255, 255, 0.45);

  /* ── HUD Corner Brackets ── */
  --hud-corner-size: 12px;
  --hud-bracket-w: 2px;
  --hud-bracket-color: var(--card-accent, rgba(99,102,241,0.4));

  /* ── Neon Glow ── */
  --neon-box-glow: 0 0 24px color-mix(in srgb, var(--card-accent) 25%, transparent);
  --neon-text-glow: 0 0 12px color-mix(in srgb, var(--card-accent) 30%, transparent);

  /* ── Dot Grid ── */
  --dot-grid-color: rgba(99, 102, 241, 0.06);
  --dot-grid-size: 24px;

  /* ── Live Indicator ── */
  --live-dot-color: #10b981;
  --live-dot-glow: 0 0 8px #10b981;

  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* ── Dot Grid Background ── */
.dashboard::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background-image: radial-gradient(circle, var(--dot-grid-color) 1px, transparent 1px);
  background-size: var(--dot-grid-size) var(--dot-grid-size);
}

.dash-content {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  gap: var(--space-2);
}

/* ── Loading ── */
.dash-loading {
  display: flex;
  justify-content: center;
  padding: var(--space-8) 0;
}

/* ── Section Labels ── */
.section-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--glass-text-muted);
  margin-bottom: var(--space-3);
  margin-top: var(--space-4);
}

/* Live indicator dot */
.section-label::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--live-dot-color);
  box-shadow: var(--live-dot-glow);
  animation: livePulse 2s ease-in-out infinite;
  flex-shrink: 0;
}

@keyframes livePulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.85); }
}
.section-label {
  margin-top: 0;
}

/* ── Glass Card Base ── */
.glass-card {
  --card-accent: var(--color-primary);
  background: var(--glass-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease, border-color 0.3s ease;
  overflow: hidden;
  position: relative;
}

.glass-card::before {
  content: '';
  position: absolute;
  inset: 6px;
  z-index: 1;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
  background:
    /* top-left */
    linear-gradient(to right, var(--hud-bracket-color), var(--hud-bracket-color)) 0 0 / var(--hud-corner-size) var(--hud-bracket-w) no-repeat,
    linear-gradient(to bottom, var(--hud-bracket-color), var(--hud-bracket-color)) 0 0 / var(--hud-bracket-w) var(--hud-corner-size) no-repeat,
    /* top-right */
    linear-gradient(to left, var(--hud-bracket-color), var(--hud-bracket-color)) 100% 0 / var(--hud-corner-size) var(--hud-bracket-w) no-repeat,
    linear-gradient(to bottom, var(--hud-bracket-color), var(--hud-bracket-color)) 100% 0 / var(--hud-bracket-w) var(--hud-corner-size) no-repeat,
    /* bottom-left */
    linear-gradient(to right, var(--hud-bracket-color), var(--hud-bracket-color)) 0 100% / var(--hud-corner-size) var(--hud-bracket-w) no-repeat,
    linear-gradient(to top, var(--hud-bracket-color), var(--hud-bracket-color)) 0 100% / var(--hud-bracket-w) var(--hud-corner-size) no-repeat,
    /* bottom-right */
    linear-gradient(to left, var(--hud-bracket-color), var(--hud-bracket-color)) 100% 100% / var(--hud-corner-size) var(--hud-bracket-w) no-repeat,
    linear-gradient(to top, var(--hud-bracket-color), var(--hud-bracket-color)) 100% 100% / var(--hud-bracket-w) var(--hud-corner-size) no-repeat;
}
.glass-card:hover::before {
  opacity: 1;
}

.glass-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(0,0,0,0.45);
  border-color: var(--glass-border-hover);
}

/* ── KPI Grid ── */

/* Variant accent colors */
.kpi-card--provider { --card-accent: var(--color-primary); }
.kpi-card--model    { --card-accent: var(--color-chart-2); }
.kpi-card--key      { --card-accent: var(--color-chart-3); }
.kpi-card--skill    { --card-accent: var(--color-chart-4); }
.kpi-card--tool     { --card-accent: var(--color-chart-5); }
.kpi-card--mcp      { --card-accent: var(--color-accent); }
.kpi-card--dify     { --card-accent: #a78bfa; }
.kpi-card--security { --card-accent: var(--color-danger); }
.kpi-card--user     { --card-accent: #f472b6; }

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.kpi-card {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease, border-color 0.3s ease;
}
.kpi-card:hover {
  transform: translateY(-6px) scale(1.02);
  border-color: var(--glass-border-hover);
  box-shadow:
    0 16px 48px rgba(0,0,0,0.45),
    var(--neon-box-glow);
}

.kpi-card:hover::before {
  opacity: 1;
}

.kpi-card:hover .kpi-card__icon-wrap {
  transform: scale(1.15);
  box-shadow: 0 0 20px color-mix(in srgb, var(--card-accent) 30%, transparent);
}

.kpi-card:hover .kpi-card__value {
  text-shadow: var(--neon-text-glow);
}

.kpi-card:hover .kpi-card__label {
  color: var(--glass-text-secondary);
}

.kpi-card__bar {
  height: 4px;
  width: 100%;
  background: var(--card-accent);
  box-shadow: 0 1px 8px color-mix(in srgb, var(--card-accent) 50%, transparent);
  position: relative;
  overflow: hidden;
}

.kpi-card__bar::after {
  content: '';
  position: absolute;
  inset: 0;
  width: 50%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
  pointer-events: none;
}

.kpi-card:hover .kpi-card__bar::after {
  transform: translateX(200%);
}

.kpi-card__inner {
  padding: var(--space-4) var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.kpi-card__icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--card-accent) 15%, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 0 12px color-mix(in srgb, var(--card-accent) 15%, transparent);
}

.kpi-card__icon {
  width: 20px;
  height: 20px;
  color: var(--card-accent);
}

.kpi-card__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.kpi-card__value {
  font-size: var(--text-2xl);
  font-weight: 800;
  line-height: 1.2;
  background: linear-gradient(135deg, var(--card-accent), color-mix(in srgb, var(--card-accent) 70%, white));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.kpi-card__dash {
  background: none;
  -webkit-text-fill-color: var(--glass-text-muted);
  color: var(--glass-text-muted);
  font-size: 1.2rem;
  font-weight: 400;
}

.kpi-card__label {
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--glass-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  white-space: nowrap;
}

.status-glow-sm {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  vertical-align: middle;
}
.status-glow-sm--ok  { background: var(--color-success); box-shadow: 0 0 10px var(--color-success); }
.status-glow-sm--warn { background: var(--color-chart-3); box-shadow: 0 0 10px var(--color-chart-3); }

/* ── Content Row (Usage + Login) ── */
.content-row {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: var(--space-3);
  margin-bottom: 0;
  flex: 0 0 auto;
}

/* ── Usage Grid ── */
.usage-card--primary { --card-accent: var(--color-primary); }
.usage-card--info    { --card-accent: var(--color-chart-2); }
.usage-card--warning { --card-accent: var(--color-chart-3); }
.usage-card--danger  { --card-accent: var(--color-danger); }

.usage-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}

.usage-card__bar {
  height: 4px;
  width: 100%;
  background: linear-gradient(90deg, var(--card-accent), color-mix(in srgb, var(--card-accent) 20%, transparent));
  position: relative;
  overflow: hidden;
}

.usage-card__bar::after {
  content: '';
  position: absolute;
  inset: 0;
  width: 50%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.25), transparent);
  transform: translateX(-100%);
  transition: transform 0.5s ease;
  pointer-events: none;
}

.usage-card:hover .usage-card__bar::after {
  transform: translateX(200%);
}

.usage-card__inner {
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.usage-card__icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--card-accent) 15%, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-1);
  box-shadow: 0 0 12px color-mix(in srgb, var(--card-accent) 12%, transparent);
}

.usage-card__icon {
  width: 22px;
  height: 22px;
  color: var(--card-accent);
}

.usage-card__value {
  font-size: var(--text-2xl);
  font-weight: 800;
  line-height: 1.1;
  background: linear-gradient(135deg, var(--card-accent), color-mix(in srgb, var(--card-accent) 70%, white));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.usage-card__value--err {
  background: var(--color-danger);
  -webkit-text-fill-color: var(--color-danger);
  color: var(--color-danger);
}

.usage-card__label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--glass-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.8px;
}

/* Token bar inside usage card */
.usage-card__tokens-bar {
  margin-top: var(--space-2);
}

.uctb__track {
  position: relative;
  height: 8px;
  background: rgba(255,255,255,0.08);
  border-radius: 4px;
  overflow: hidden;
}

.uctb__fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.uctb__fill--prompt {
  background: linear-gradient(90deg, var(--color-primary), #818cf8);
}

.uctb__fill--completion {
  background: linear-gradient(90deg, var(--color-chart-2), #60a5fa);
}

.uctb__labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.65rem;
  font-family: var(--font-mono);
  color: var(--glass-text-muted);
  margin-top: var(--space-1);
}

.uctb__labels span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.uctb__labels span::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
.uctb__labels span:first-child::before { background: var(--color-primary); }
.uctb__labels span:last-child::before  { background: var(--color-chart-2); }

/* ── Login Card ── */
.login-card {
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.login-card__header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.929rem;
  font-weight: 700;
  color: var(--glass-text);
}

.login-card__header-icon {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
  flex-shrink: 0;
  animation: iconSoftPulse 3s ease-in-out infinite;
}

@keyframes iconSoftPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.login-card__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-2);
  padding: var(--space-2) 0;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.login-card__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.login-card__stat-value {
  font-size: var(--text-lg);
  font-weight: 800;
  color: var(--glass-text);
}
.login-card__stat-value--ok   { color: var(--color-success); }
.login-card__stat-value--fail { color: var(--color-danger); }

.login-card__stat-label {
  font-size: 0.55rem;
  font-weight: 600;
  color: var(--glass-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.login-card__log-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.login-card__log-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px var(--space-2);
  border-radius: var(--radius-md);
  font-size: 0.7rem;
  gap: var(--space-2);
  transition: background 0.2s;
  border-left: 3px solid var(--color-success);
  animation: logRowSlide 0.3s cubic-bezier(0.16, 1, 0.3, 1) both;
}
.login-card__log-row:nth-child(1) { animation-delay: 0.00s; }
.login-card__log-row:nth-child(2) { animation-delay: 0.06s; }
.login-card__log-row:nth-child(3) { animation-delay: 0.12s; }
.login-card__log-row:nth-child(4) { animation-delay: 0.18s; }
.login-card__log-row:nth-child(5) { animation-delay: 0.24s; }

@keyframes logRowSlide {
  from { opacity: 0; transform: translateX(-8px); }
  to   { opacity: 1; transform: translateX(0); }
}
.login-card__log-row--fail {
  border-left-color: var(--color-danger);
  background: color-mix(in srgb, var(--color-danger) 6%, transparent);
}
.login-card__log-row:hover {
  background: rgba(255,255,255,0.05);
}

.login-card__log-left {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.login-card__log-user {
  font-weight: 600;
  color: var(--glass-text);
  line-height: 1.3;
}

.login-card__log-ip {
  font-family: var(--font-mono);
  font-size: 0.6rem;
  color: var(--glass-text-muted);
}

.login-card__log-time {
  font-family: var(--font-mono);
  font-size: 0.6rem;
  color: var(--glass-text-muted);
  white-space: nowrap;
}

/* ── Trends Row ── */
.trends-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
  margin-bottom: 0;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.trend-card {
  padding: var(--space-4);
}

.trend-card__header {
  font-size: 0.929rem;
  font-weight: 700;
  color: var(--glass-text);
  margin-bottom: var(--space-3);
}

.trend-card__chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 72px;
  overflow-x: auto;
}

.trend-card__bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 20px;
}

.trend-card__bar-group {
  transform-origin: bottom;
  animation: barGrowIn 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

.trend-card__bar-group:nth-child(1)  { animation-delay: 0.00s; }
.trend-card__bar-group:nth-child(2)  { animation-delay: 0.04s; }
.trend-card__bar-group:nth-child(3)  { animation-delay: 0.08s; }
.trend-card__bar-group:nth-child(4)  { animation-delay: 0.12s; }
.trend-card__bar-group:nth-child(5)  { animation-delay: 0.16s; }
.trend-card__bar-group:nth-child(6)  { animation-delay: 0.20s; }
.trend-card__bar-group:nth-child(7)  { animation-delay: 0.24s; }
.trend-card__bar-group:nth-child(8)  { animation-delay: 0.28s; }
.trend-card__bar-group:nth-child(9)  { animation-delay: 0.32s; }
.trend-card__bar-group:nth-child(10) { animation-delay: 0.36s; }
.trend-card__bar-group:nth-child(11) { animation-delay: 0.40s; }
.trend-card__bar-group:nth-child(12) { animation-delay: 0.44s; }
.trend-card__bar-group:nth-child(13) { animation-delay: 0.48s; }
.trend-card__bar-group:nth-child(14) { animation-delay: 0.52s; }
.trend-card__bar-group:nth-child(15) { animation-delay: 0.56s; }

.trend-card__bar {
  width: 100%;
  max-width: 28px;
  background: linear-gradient(to top, rgba(99,102,241,0.95), rgba(129,140,248,0.6), rgba(99,102,241,0.3));
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  min-height: 4px;
  transition: opacity 0.2s, transform 0.2s, box-shadow 0.2s;
}

.trend-card__bar-group:hover .trend-card__bar {
  transform: scaleY(1.15);
  filter: brightness(1.3);
  box-shadow: 0 0 12px rgba(99, 102, 241, 0.4);
}

.trend-card__chart:hover .trend-card__bar-group {
  opacity: 0.5;
  transition: opacity 0.2s;
}
.trend-card__chart:hover .trend-card__bar-group:hover {
  opacity: 1;
}

.trend-card__label {
  font-size: 0.6rem;
  color: var(--glass-text-muted);
  margin-top: 4px;
  white-space: nowrap;
}

.trend-card__stacked {
  width: 100%;
  max-width: 28px;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column-reverse;
  min-height: 4px;
}

.trend-card__segment {
  width: 100%;
  min-height: 2px;
  transition: opacity 0.2s;
}
.trend-card__segment:hover { opacity: 0.85; }

.trend-card__seg--ok     { background: var(--color-success); }
.trend-card__seg--run    { background: var(--color-chart-2); }
.trend-card__seg--pend   { background: var(--color-chart-3); }
.trend-card__seg--fail   { background: var(--color-danger); }
.trend-card__seg--cancel { background: var(--glass-text-muted); }

.trend-card__legend {
  display: flex;
  gap: var(--space-3);
  justify-content: center;
  flex-wrap: wrap;
  margin-top: var(--space-2);
  font-size: 0.6rem;
  color: var(--glass-text-muted);
}

.trend-card__legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
}

.trend-card__legend-dot--ok     { background: var(--color-success); }
.trend-card__legend-dot--run    { background: var(--color-chart-2); }
.trend-card__legend-dot--pend   { background: var(--color-chart-3); }
.trend-card__legend-dot--fail   { background: var(--color-danger); }
.trend-card__legend-dot--cancel { background: var(--glass-text-muted); }

/* ── Tables Row ── */
.tables-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.table-card {
  padding: 0;
  overflow: hidden;
}
.table-card__header {
  font-size: 0.929rem;
  font-weight: 700;
  color: var(--glass-text);
  padding: var(--space-4) var(--space-5) var(--space-2);
}

.table-card__table {
  width: 100%;
  border-collapse: collapse;
}

.table-card__table th,
.table-card__table td {
  padding: var(--space-2) var(--space-4);
  text-align: left;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  font-size: var(--text-sm);
  color: var(--glass-text);
}

.table-card__table th {
  font-weight: 600;
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--glass-text-muted);
  background: rgba(255,255,255,0.06);
  position: sticky;
  top: 0;
  z-index: 1;
  border-bottom: 1px solid rgba(99,102,241,0.2);
}

.table-card__table thead {
  position: relative;
}
.table-card__table thead::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--color-primary), transparent);
  opacity: 0.3;
  pointer-events: none;
}

.table-card__table tbody tr {
  transition: background 0.15s;
}

.table-card__table tbody tr:nth-child(even) {
  background: rgba(255,255,255,0.02);
}

.table-card__table tbody tr {
  border-left: 2px solid transparent;
  transition: border-color 0.2s, background 0.15s;
}

.table-card__table tbody tr:hover {
  background: rgba(99,102,241,0.08);
  border-left-color: var(--color-primary);
}

.table-card__table tr:last-child td {
  border-bottom: none;
}

.col-r { text-align: right; font-variant-numeric: tabular-nums; }
.col-zero { color: var(--glass-text-muted); }

.col-rank { width: 36px; }

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: 700;
}
.rank--1 {
  background: linear-gradient(135deg, rgba(245,158,11,0.4), rgba(217,119,6,0.3));
  color: #fbbf24;
  box-shadow: 0 0 10px rgba(245,158,11,0.25);
}
.rank--2 {
  background: rgba(255,255,255,0.08);
  color: var(--glass-text-secondary);
}
.rank--3 {
  background: linear-gradient(135deg, rgba(253,186,116,0.3), rgba(249,115,22,0.25));
  color: #fdba74;
  box-shadow: 0 0 8px rgba(249,115,22,0.15);
}
.rank--4,
.rank--5,
.rank--6,
.rank--7,
.rank--8,
.rank--all {
  background: rgba(255,255,255,0.04);
  color: var(--glass-text-muted);
}

/* health dot */
.health-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
.health-dot--ok   { background: var(--color-success); box-shadow: 0 0 6px var(--color-success); animation: healthPulse 2s ease-in-out infinite; }
.health-dot--warn { background: var(--color-chart-3); box-shadow: 0 0 6px var(--color-chart-3); }

/* error pill */
.err-pill {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 600;
  color: #fff;
  background: var(--color-danger);
}

/* model status dot */
.model-status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 5px;
  vertical-align: middle;
}
.msd--on  { background: var(--color-success); box-shadow: 0 0 6px var(--color-success); animation: healthPulse 2s ease-in-out infinite; }
.msd--off { background: var(--glass-text-muted); }

/* status pill */
.status-pill {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 0.78rem;
  font-weight: 600;
}
.sp--ok   { background: rgba(16,185,129,0.2); color: #34d399; }
.sp--warn { background: rgba(239,68,68,0.2); color: #fca5a5; }

/* ── Entry Animations ── */
@keyframes barGrowIn {
  from { opacity: 0; transform: scaleY(0); }
  to   { opacity: 1; transform: scaleY(1); }
}

@keyframes healthPulse {
  0%, 100% { box-shadow: 0 0 6px var(--color-success); }
  50% { box-shadow: 0 0 12px var(--color-success), 0 0 24px color-mix(in srgb, var(--color-success) 30%, transparent); }
}

@keyframes barFlow {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
    filter: blur(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}

.anim-section {
  animation: fadeInUp 0.45s cubic-bezier(0.16, 1, 0.3, 1) both;
  animation-delay: var(--anim-delay, 0ms);
}

/* ── Reduced Motion ── */
@media (prefers-reduced-motion: reduce) {
  .anim-section {
    animation: none;
    opacity: 1;
  }
  .kpi-card:hover,
  .trend-card:hover,
  .table-card:hover,
  .login-card:hover {
    transform: none;
  }
}
</style>
