<template>
  <div>
    <div class="header">
      <h1>{{ $t('dify.title') }}</h1>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>

    <template v-else>
      <!-- Connection list -->
      <table v-if="connections.length > 0" class="table">
        <thead><tr>
          <th>{{ $t('dify.name') }}</th>
          <th>{{ $t('dify.baseUrl') }}</th>
          <th>{{ $t('dify.adminEmail') }}</th>
          <th>{{ $t('dify.status') }}</th>
          <th>{{ $t('dify.lastTested') }}</th>
          <th v-if="auth.isAdmin">{{ $t('provider.actions') }}</th>
        </tr></thead>
        <tbody>
          <tr v-for="c in connections" :key="c.id">
            <td>{{ c.name }}</td>
            <td class="mono">{{ c.baseUrl }}</td>
            <td class="mono">{{ c.adminEmail }}</td>
            <td><span :class="'badge ' + statusClass(c.status)">{{ $t('dify.' + c.status) }}</span></td>
            <td class="muted">{{ c.lastTestAt ? formatTime(c.lastTestAt) : '-' }}</td>
            <td v-if="auth.isAdmin" class="actions">
              <button @click="openConsole(c)" class="btn-sm btn-console">{{ $t('dify.openConsole') }}</button>
              <button @click="loginToDify(c)" class="btn-sm" :disabled="c._logging">
                {{ c._logging ? $t('dify.loggingIn') : $t('dify.loginToDify') }}
              </button>
              <button @click="testConnection(c)" class="btn-sm" :disabled="c._testing">
                {{ c._testing ? $t('dify.testing') : $t('dify.testConnection') }}
              </button>
              <button @click="editConnection(c)" class="btn-sm">{{ $t('provider.edit') }}</button>
              <button @click="deleteConnection(c.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty">
        <p>{{ $t('dify.empty') }}</p>
      </div>

      <!-- Add / Edit form -->
      <div class="form-section">
        <h2>{{ isEditing ? $t('dify.editConnection') : $t('dify.addConnection') }}</h2>
        <form @submit.prevent="save" class="form">
          <div class="field">
            <label>{{ $t('dify.name') }}</label>
            <input v-model="form.name" class="form-input" required />
          </div>
          <div class="field">
            <label>{{ $t('dify.baseUrl') }}</label>
            <input v-model="form.baseUrl" class="form-input mono" placeholder="http://localhost:5001" required />
          </div>
          <div class="field">
            <label>{{ $t('dify.apiKey') }}</label>
            <input v-model="form.apiKey" class="form-input" type="password" :placeholder="isEditing ? $t('dify.apiKeyPlaceholder') : ''" :required="!isEditing" />
          </div>
          <div class="field">
            <label>{{ $t('dify.adminEmail') }}</label>
            <input v-model="form.adminEmail" class="form-input" type="email" placeholder="admin@example.com" required />
          </div>
          <div class="field">
            <label>{{ $t('dify.adminPassword') }}</label>
            <input v-model="form.adminPassword" class="form-input" type="password" :placeholder="isEditing ? $t('dify.apiKeyPlaceholder') : ''" :required="!isEditing" />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-primary" :disabled="saving">{{ saving ? $t('common.saving') : $t('common.save') }}</button>
            <button v-if="isEditing" type="button" class="btn-sm" @click="cancelEdit">{{ $t('common.cancel') }}</button>
          </div>
        </form>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { difyApi } from '../../api/dify'
import type { DifyConfig } from '../../api/dify'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const connections = ref<(DifyConfig & { _testing?: boolean; _logging?: boolean })[]>([])
const saving = ref(false)
const isEditing = ref(false)
const editingId = ref<string | null>(null)
const form = ref({ name: '', baseUrl: '', apiKey: '', adminEmail: '', adminPassword: '' })

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

function statusClass(status: string) {
  if (status === 'connected') return 'connected'
  if (status === 'error') return 'error'
  return 'disconnected'
}

function formatTime(ts: string) {
  return new Date(ts).toLocaleString()
}

async function openConsole(c: DifyConfig) {
  try {
    const res = await difyApi.oauthAuthorize('dify', c.id)
    window.open(res.data.redirectUri + '?code=' + res.data.code + '&state=' + res.data.state, '_blank')
  } catch {
    show(t('common.error'), 'error')
  }
}

function editConnection(c: DifyConfig) {
  isEditing.value = true
  editingId.value = c.id
  form.value = { name: c.name, baseUrl: c.baseUrl, apiKey: '', adminEmail: c.adminEmail, adminPassword: '' }
}

function cancelEdit() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', baseUrl: '', apiKey: '', adminEmail: '', adminPassword: '' }
}

async function save() {
  saving.value = true
  try {
    if (isEditing.value && editingId.value) {
      const data: any = { name: form.value.name, baseUrl: form.value.baseUrl, adminEmail: form.value.adminEmail }
      if (form.value.apiKey) data.apiKey = form.value.apiKey
      if (form.value.adminPassword) data.adminPassword = form.value.adminPassword
      const res = await difyApi.update(editingId.value, data)
      const idx = connections.value.findIndex(c => c.id === editingId.value)
      if (idx >= 0) connections.value[idx] = { ...res.data, _testing: false, _logging: false }
      show(t('dify.saved'))
      cancelEdit()
    } else {
      const res = await difyApi.create({ ...form.value })
      connections.value.push({ ...res.data, _testing: false, _logging: false })
      show(t('dify.saved'))
      form.value = { name: '', baseUrl: '', apiKey: '', adminEmail: '', adminPassword: '' }
    }
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}

async function deleteConnection(id: string) {
  if (!confirm(t('dify.deleteConfirm'))) return
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

async function loginToDify(c: DifyConfig & { _logging?: boolean }) {
  c._logging = true
  try {
    const res = await difyApi.login(c.id)
    const r = res.data
    if (r.connected) {
      show(t('dify.loginSuccess'))
    } else {
      show(t('dify.loginFailed') + ': ' + r.message, 'error')
    }
    const updated = await difyApi.get(c.id)
    const idx = connections.value.findIndex(x => x.id === c.id)
    if (idx >= 0) connections.value[idx] = { ...updated.data, _logging: false }
  } catch {
    show(t('common.error'), 'error')
    c._logging = false
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }

.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; margin-bottom: 24px; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.table th { background: #f9fafb; font-weight: 600; color: #374151; }

.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; white-space: nowrap; }
.badge.connected { background: #e8f5e9; color: #2e7d32; }
.badge.disconnected { background: #f3f4f6; color: #6b7280; }
.badge.error { background: #ffebee; color: #c62828; }

.mono { font-family: monospace; font-size: 12px; }
.muted { color: #666; font-size: 12px; }
.actions { white-space: nowrap; }

.form-section { background: white; border-radius: 8px; padding: 20px; border: 1px solid #e0e0e0; }
.form-section h2 { font-size: 15px; margin: 0 0 16px; }
.form { display: flex; flex-direction: column; gap: 12px; max-width: 480px; }
.field { display: flex; flex-direction: column; gap: 4px; }
.field label { font-size: 12px; color: #666; font-weight: 500; }
.form-input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
.form-actions { display: flex; gap: 8px; margin-top: 4px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; text-decoration: none; }
.btn-sm:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-danger { background: #ffebee; color: #c62828; }
.btn-console { background: #e8f5e9; color: #2e7d32; }
</style>
