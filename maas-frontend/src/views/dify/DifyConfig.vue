<template>
  <div>
    <BasePageHeader :title="$t('dify.title')" />

    <div v-if="loading" class="loading">
      <BaseSpinner size="lg" />
    </div>
    <template v-else>
      <!-- Connection cards -->
      <div v-if="connections.length > 0" class="card-grid">
        <div
          v-for="c in connections"
          :key="c.id"
          class="dify-card"
          @click="editConnection(c)"
        >
          <div class="dify-card__top">
            <div class="dify-card__info">
              <span class="dify-card__name">{{ c.name }}</span>
              <span class="dify-card__url mono">{{ c.baseUrl }}</span>
            </div>
            <BaseBadge :variant="statusBadgeVariant(c.status)">
              {{ $t('dify.' + c.status) }}
            </BaseBadge>
          </div>
          <div class="dify-card__meta">
            <span class="muted">{{ $t('dify.lastTested') }}: {{ c.lastTestAt ? formatTime(c.lastTestAt) : '-' }}</span>
          </div>
          <div class="dify-card__actions" @click.stop>
            <BaseButton size="sm" variant="secondary" @click="openConsole(c)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px;vertical-align:-2px"><polyline points="9 18 15 12 9 6"/></svg>
              {{ $t('dify.openConsole') }}
            </BaseButton>
            <BaseButton size="sm" variant="secondary" :disabled="c._testing" :loading="c._testing" @click="testConnection(c)">
              {{ c._testing ? $t('dify.testing') : $t('dify.testConnection') }}
            </BaseButton>
            <BaseButton size="sm" variant="secondary" @click="editConnection(c)">
              {{ $t('provider.edit') }}
            </BaseButton>
            <BaseButton size="sm" variant="danger" @click="deleteConnection(c.id)">
              {{ $t('provider.delete') }}
            </BaseButton>
          </div>
        </div>
      </div>

      <BaseEmpty v-else :text="$t('dify.empty')" />

      <!-- Add / Edit form -->
      <BaseCard class="form-card">
        <template #header>
          <h2 class="form-title">{{ isEditing ? $t('dify.editConnection') : $t('dify.addConnection') }}</h2>
        </template>
        <form @submit.prevent="save" class="form">
          <BaseFormField :label="$t('dify.name')" required>
            <input v-model="form.name" class="form-input" required />
          </BaseFormField>
          <BaseFormField :label="$t('dify.baseUrl')" required>
            <input v-model="form.baseUrl" class="form-input mono" placeholder="http://localhost:5001" required />
          </BaseFormField>
          <BaseFormField :label="$t('dify.authCode')" :required="!isEditing">
            <input v-model="form.authCode" class="form-input" type="password" :placeholder="isEditing ? $t('dify.authCodePlaceholder') : ''" :required="!isEditing" />
          </BaseFormField>
          <BaseFormField :label="$t('dify.adminEmail')">
            <input v-model="form.adminEmail" class="form-input" type="email" placeholder="admin@example.com" />
          </BaseFormField>
          <BaseFormField :label="$t('dify.adminPassword')">
            <input v-model="form.adminPassword" class="form-input" type="password" :placeholder="isEditing ? $t('dify.adminPasswordPlaceholder') : ''" />
          </BaseFormField>
          <div class="form-actions">
            <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
              {{ saving ? $t('common.saving') : $t('common.save') }}
            </BaseButton>
            <BaseButton v-if="isEditing" type="button" variant="secondary" @click="cancelEdit">
              {{ $t('common.cancel') }}
            </BaseButton>
          </div>
        </form>
      </BaseCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { difyApi } from '../../api/dify'
import type { DifyConfig } from '../../api/dify'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'

import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'
import BaseFormField from '../../components/ui/BaseFormField.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const connections = ref<(DifyConfig & { _testing?: boolean })[]>([])
const saving = ref(false)
const isEditing = ref(false)
const editingId = ref<string | null>(null)
const form = ref({ name: '', baseUrl: '', authCode: '', adminEmail: '', adminPassword: '' })

onMounted(async () => {
  try {
    const res = await difyApi.list()
    connections.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

function statusBadgeVariant(status: string): 'success' | 'danger' | 'neutral' {
  if (status === 'connected') return 'success'
  if (status === 'error') return 'danger'
  return 'neutral'
}

function formatTime(ts: string) {
  return new Date(ts).toLocaleString()
}

async function openConsole(c: DifyConfig) {
  try {
    const res = await difyApi.oauthAuthorize('dify', c.id)
    const data = res.data
    window.open(data.redirectUri + '?code=' + data.code + '&state=' + data.state, '_blank')
  } catch {
    show(t('common.error'), 'error')
  }
}

function editConnection(c: DifyConfig) {
  isEditing.value = true
  editingId.value = c.id
  form.value = { name: c.name, baseUrl: c.baseUrl, authCode: '', adminEmail: c.adminEmail || '', adminPassword: '' }
}

function cancelEdit() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', baseUrl: '', authCode: '', adminEmail: '', adminPassword: '' }
}

async function save() {
  saving.value = true
  try {
    const payload: any = { name: form.value.name, baseUrl: form.value.baseUrl }
    if (form.value.authCode) payload.authCode = form.value.authCode
    if (form.value.adminEmail) payload.adminEmail = form.value.adminEmail
    if (form.value.adminPassword) payload.adminPassword = form.value.adminPassword

    if (isEditing.value && editingId.value) {
      const res = await difyApi.update(editingId.value, payload)
      const idx = connections.value.findIndex(c => c.id === editingId.value)
      if (idx >= 0) connections.value[idx] = { ...res.data, _testing: false }
      show(t('dify.saved'))
      cancelEdit()
    } else {
      payload.authCode = form.value.authCode
      const res = await difyApi.create(payload)
      connections.value.push({ ...res.data, _testing: false })
      show(t('dify.saved'))
      cancelEdit()
    }
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}

async function deleteConnection(id: string) {
  if (!(await confirmDialog(t('dify.deleteConfirm')))) return
  try {
    await difyApi.delete(id)
    connections.value = connections.value.filter(c => c.id !== id)
    if (editingId.value === id) cancelEdit()
    show(t('dify.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function testConnection(c: DifyConfig & { _testing?: boolean }) {
  c._testing = true
  try {
    const res = await difyApi.test(c.id)
    const r = res.data
    if (r.connected) {
      show(t('dify.testSuccess') + (r.appName ? ': ' + r.appName : ''))
    } else {
      show(t('dify.testFailed') + ': ' + r.message, 'error')
    }
    const updated = await difyApi.get(c.id)
    const idx = connections.value.findIndex(x => x.id === c.id)
    if (idx >= 0) connections.value[idx] = { ...updated.data, _testing: false }
  } catch {
    show(t('common.error'), 'error')
    c._testing = false
  }
}
</script>

<style scoped>
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--space-12);
}

/* Card grid */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.dify-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.dify-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.1);
  transform: translateY(-1px);
}

.dify-card__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.dify-card__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.dify-card__name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-foreground);
}

.dify-card__url {
  font-size: 0.786rem;
  color: var(--color-foreground-secondary);
  word-break: break-all;
}

.dify-card__meta {
  margin-bottom: var(--space-3);
}

.mono {
  font-family: var(--font-mono);
  font-size: 0.857rem;
}

.muted {
  color: var(--color-foreground-secondary);
  font-size: 0.786rem;
}

.dify-card__actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
}

/* Form */
.form-card {
  margin-top: var(--space-6);
}

.form-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-foreground);
  margin: 0;
}

.form {
  display: flex;
  flex-direction: column;
  max-width: 480px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  font-family: var(--font-sans);
  color: var(--color-foreground);
  background: var(--color-bg-card);
  box-sizing: border-box;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.form-input::placeholder {
  color: var(--color-muted);
}

.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>
