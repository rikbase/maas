<template>
  <div>
    <BaseSpinner v-if="loading" class="spinner" />

    <template v-else-if="workflow">
      <BasePageHeader :title="workflow.name ?? $t('workflow.detail')">
        <template #actions>
          <BaseButton variant="primary" size="sm" @click="router.push(`/workflows/${workflow.id}/edit`)">{{ $t('workflow.edit') }}</BaseButton>
          <BaseButton variant="secondary" size="sm" @click="router.push(`/workflows/${workflow.id}/executions`)">{{ $t('workflow.executions') }}</BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard noPadding class="info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.name') }}</span>
            <span class="info-item-value">{{ workflow.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.description') }}</span>
            <span class="info-item-value">{{ workflow.description || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.status') }}</span>
            <span class="info-item-value">
              <BaseBadge :variant="statusVariant(workflow.status)">{{ $t('workflow.statuses.' + workflow.status) }}</BaseBadge>
            </span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.version') }}</span>
            <span class="info-item-value">{{ workflow.latestVersion ?? '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.lastRun') }}</span>
            <span class="info-item-value">
              <BaseBadge v-if="workflow.lastRunStatus" :variant="runStatusVariant(workflow.lastRunStatus)">
                {{ $t('workflow.runStatuses.' + workflow.lastRunStatus) }}
              </BaseBadge>
              <span v-else class="muted-text">-</span>
            </span>
          </div>
        </div>
      </BaseCard>

      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">{{ $t('workflow.versions') }}</h2>
        </template>
        <BaseEmpty v-if="versions.length === 0" :text="$t('workflow.noVersions')" />
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>{{ $t('workflow.version') }}</th>
              <th>{{ $t('workflow.status') }}</th>
              <th>{{ $t('workflow.createdAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in versions" :key="v.id">
              <td>v{{ v.version }}</td>
              <td>
                <BaseBadge :variant="statusVariant(v.status)">{{ $t('workflow.statuses.' + v.status) }}</BaseBadge>
              </td>
              <td class="muted-text">{{ formatTime(v.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </BaseCard>

      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">{{ $t('workflow.recentExecutions') }}</h2>
        </template>
        <BaseEmpty v-if="executions.length === 0" :text="$t('workflow.noExecutions')" />
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>{{ $t('workflow.id') }}</th>
              <th>{{ $t('workflow.status') }}</th>
              <th>{{ $t('workflow.startedAt') }}</th>
              <th>{{ $t('workflow.finishedAt') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in executions" :key="e.id" class="clickable-row" @click="router.push('/executions/' + e.id)">
              <td class="text-mono">{{ e.id.slice(0, 8) }}...</td>
              <td>
                <BaseBadge :variant="runStatusVariant(e.status)">{{ $t('workflow.runStatuses.' + e.status) }}</BaseBadge>
              </td>
              <td class="muted-text">{{ e.startedAt ? formatTime(e.startedAt) : '-' }}</td>
              <td class="muted-text">{{ e.finishedAt ? formatTime(e.finishedAt) : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
      <!-- Triggers -->
      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">Triggers</h2>
          <div class="header-actions">
            <BaseButton size="sm" variant="primary" @click="showAddTrigger = true">+ Add</BaseButton>
          </div>
        </template>
        <BaseEmpty v-if="triggers.length === 0" text="No triggers configured" />
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>Type</th>
              <th>Config</th>
              <th>Status</th>
              <th>Last Fired</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in triggers" :key="t.id">
              <td><BaseBadge :variant="t.triggerType === 'webhook' ? 'info' : 'warning'">{{ t.triggerType }}</BaseBadge></td>
              <td>
                <span v-if="t.triggerType === 'cron'" class="mono-text">{{ t.cronExpression }}</span>
                <span v-else class="mono-text text-sm">POST /api/webhook/{{ t.id }}</span>
              </td>
              <td>
                <BaseBadge :variant="t.status === 'active' ? 'success' : 'neutral'">{{ t.status }}</BaseBadge>
              </td>
              <td class="muted-text">{{ t.lastFiredAt ? formatTime(t.lastFiredAt) : '-' }}</td>
              <td>
                <BaseButton v-if="t.status === 'active'" size="sm" variant="secondary" @click="toggleTrigger(t)">Disable</BaseButton>
                <BaseButton v-else size="sm" variant="secondary" @click="toggleTrigger(t)">Enable</BaseButton>
                <BaseButton size="sm" variant="danger" @click="deleteTrigger(t)">Del</BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
    </template>

    <!-- Add Trigger Modal -->
    <div v-if="showAddTrigger" class="modal-overlay" @click.self="showAddTrigger = false">
      <div class="modal-card">
        <h3 class="modal-title">Add Trigger</h3>
        <div class="modal-body">
          <div class="field-group">
            <label class="field-label">Type</label>
            <select v-model="newTriggerType" class="form-input">
              <option value="cron">Cron (Schedule)</option>
              <option value="webhook">Webhook</option>
            </select>
          </div>
          <div v-if="newTriggerType === 'cron'" class="field-group">
            <label class="field-label">Cron Expression</label>
            <input v-model="newCronExpr" class="form-input" placeholder="30 9 (9:30 daily) or every_30_minutes" />
            <p class="field-hint">Format: "M H" (daily), or "every_X_minutes" / "every_X_hours"</p>
          </div>
          <div v-if="newTriggerType === 'webhook'" class="field-group">
            <label class="field-label">Secret (optional)</label>
            <input v-model="newWebhookSecret" class="form-input" placeholder="Leave blank for no auth" />
          </div>
          <div class="form-actions">
            <BaseButton variant="primary" :loading="savingTrigger" :disabled="savingTrigger" @click="saveTrigger">
              {{ savingTrigger ? 'Creating...' : 'Create' }}
            </BaseButton>
            <BaseButton variant="secondary" @click="showAddTrigger = false">Cancel</BaseButton>
          </div>
        </div>
        <button class="modal-close" @click="showAddTrigger = false">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { workflowApi, triggerApi } from '../../api/workflows'
import type { Workflow, WorkflowVersion, Execution, WorkflowTrigger } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'
const { confirm: confirmDialog } = useConfirm()
const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const workflow = ref<Workflow | null>(null)
const versions = ref<WorkflowVersion[]>([])
const executions = ref<Execution[]>([])
const triggers = ref<WorkflowTrigger[]>([])
const showAddTrigger = ref(false)
const newTriggerType = ref('cron')
const newCronExpr = ref('')
const newWebhookSecret = ref('')
const savingTrigger = ref(false)

onMounted(async () => {
  const id = route.params.id as string
  try {
    const [res, verRes, execRes, trigRes] = await Promise.all([
      workflowApi.get(id),
      workflowApi.getVersions(id),
      workflowApi.getExecutions(id, { size: 10 }),
      triggerApi.list(id),
    ])
    workflow.value = res.data
    versions.value = verRes.data
    executions.value = execRes.data.content
    triggers.value = trigRes.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function saveTrigger() {
  savingTrigger.value = true
  try {
    const data: any = { triggerType: newTriggerType.value }
    if (newTriggerType.value === 'cron') data.cronExpression = newCronExpr.value
    if (newTriggerType.value === 'webhook') data.webhookSecret = newWebhookSecret.value || null
    const res = await triggerApi.create(route.params.id as string, data)
    triggers.value.push(res.data)
    showAddTrigger.value = false
    newCronExpr.value = ''
    newWebhookSecret.value = ''
    show('Trigger created')
  } catch {
    show(t('common.error'), 'error')
  } finally {
    savingTrigger.value = false
  }
}

async function toggleTrigger(trig: WorkflowTrigger) {
  const newStatus = trig.status === 'active' ? 'disabled' : 'active'
  try {
    const res = await triggerApi.update(route.params.id as string, trig.id, { status: newStatus })
    const idx = triggers.value.findIndex(x => x.id === trig.id)
    if (idx >= 0) triggers.value[idx] = res.data
    show(newStatus === 'active' ? 'Trigger enabled' : 'Trigger disabled')
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteTrigger(trig: WorkflowTrigger) {
  if (!(await confirmDialog('Delete this trigger?'))) return
  try {
    await triggerApi.delete(route.params.id as string, trig.id)
    triggers.value = triggers.value.filter(x => x.id !== trig.id)
    show('Trigger deleted')
  } catch {
    show(t('common.error'), 'error')
  }
}

function statusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = {
    draft: 'neutral',
    published: 'success',
    archived: 'warning',
  }
  return map[status] || 'neutral'
}

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

function formatTime(ts: string) {
  return new Date(ts).toLocaleString()
}
</script>

<style scoped>
.spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}
.info-card {
  margin-bottom: var(--space-5);
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  padding: var(--space-5);
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.info-item-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: var(--color-foreground-secondary);
  letter-spacing: 0.5px;
}
.info-item-value {
  font-size: 0.875rem;
  color: var(--color-foreground);
}
.section-card {
  margin-bottom: var(--space-5);
}
.card-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-foreground);
  margin: 0;
}
.muted-text {
  color: var(--color-foreground-secondary);
  font-size: 0.8rem;
}
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.75rem;
}
.clickable-row {
  cursor: pointer;
}
.clickable-row:hover {
  background: var(--color-bg-muted);
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th {
  text-transform: uppercase;
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
  font-weight: 600;
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
}
.data-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  font-size: 0.875rem;
  color: var(--color-foreground);
}
.data-table tbody tr:hover {
  background: var(--color-bg-muted);
}
.data-table tbody tr:last-child td {
  border-bottom: none;
}

/* Header actions */
.header-actions {
  display: flex;
  gap: var(--space-2);
  margin-left: auto;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  width: 480px;
  max-width: 90vw;
  position: relative;
  box-shadow: var(--shadow-xl);
}
.modal-title {
  margin: 0 0 var(--space-4);
  font-size: 1.143rem;
  font-weight: 700;
}
.modal-close {
  position: absolute;
  top: var(--space-3);
  right: var(--space-4);
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-foreground-secondary);
  cursor: pointer;
  line-height: 1;
}
.modal-close:hover { color: var(--color-foreground); }

.field-group {
  margin-bottom: var(--space-3);
}
.field-label {
  display: block;
  font-size: 0.786rem;
  font-weight: 600;
  color: var(--color-foreground-secondary);
  margin-bottom: var(--space-1);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.field-hint {
  font-size: 0.714rem;
  color: var(--color-foreground-secondary);
  margin: var(--space-1) 0 0;
}
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-family: var(--font-mono);
  font-size: 0.857rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
  box-sizing: border-box;
}
.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
}
.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.mono-text {
  font-family: var(--font-mono);
  font-size: 0.857rem;
}
.text-sm {
  font-size: 0.786rem;
}
</style>
