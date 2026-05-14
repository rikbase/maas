<template>
  <div>
    <div class="header">
      <h1>{{ $t('security.rules.title') }}</h1>
      <router-link v-if="auth.isAdmin" to="/security/rules/new" class="btn-primary">{{ $t('security.rules.add') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="rules.length === 0" class="empty">
      <p>{{ $t('security.rules.empty') }}</p>
      <p class="hint">{{ $t('security.rules.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead>
        <tr>
          <th>{{ $t('security.rules.name') }}</th>
          <th>{{ $t('security.rules.type') }}</th>
          <th>{{ $t('security.rules.severity') }}</th>
          <th>{{ $t('security.rules.action') }}</th>
          <th>{{ $t('security.rules.status') }}</th>
          <th v-if="auth.isAdmin">{{ $t('security.rules.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in rules" :key="r.id">
          <td>{{ r.name }}</td>
          <td>{{ $t('security.rules.detectorTypes.' + r.detectorType) }}</td>
          <td><span :class="'badge badge-' + r.severity">{{ $t('security.rules.severities.' + r.severity) }}</span></td>
          <td><span :class="'badge badge-action-' + r.action">{{ $t('security.rules.actionsList.' + r.action) }}</span></td>
          <td>
            <span :class="'badge ' + (r.enabled ? 'enabled' : 'disabled')">
              {{ $t('security.rules.statuses.' + (r.enabled ? 'active' : 'inactive')) }}
            </span>
          </td>
          <td v-if="auth.isAdmin" class="actions">
            <router-link :to="`/security/rules/${r.id}/edit`" class="btn-sm">{{ $t('security.rules.edit') }}</router-link>
            <button @click="toggleRule(r)" class="btn-sm" :class="r.enabled ? 'btn-warn' : 'btn-ok'">
              {{ r.enabled ? $t('security.rules.disable') : $t('security.rules.enable') }}
            </button>
            <button @click="deleteRule(r.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ruleApi } from '../../api/security'
import type { SecurityRule } from '../../api/security'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const rules = ref<SecurityRule[]>([])

onMounted(async () => {
  try {
    const res = await ruleApi.list()
    rules.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function toggleRule(r: SecurityRule) {
  try {
    await ruleApi.toggleEnabled(r.id)
    r.enabled = !r.enabled
    show(r.enabled ? t('security.rules.enabled') : t('security.rules.disabled'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteRule(id: string) {
  if (!confirm(t('security.rules.deleteConfirm'))) return
  try {
    await ruleApi.delete(id)
    rules.value = rules.value.filter(r => r.id !== id)
    show(t('security.rules.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.hint { font-size: 13px; color: #999; margin-top: 8px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.actions { display: flex; gap: 4px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.enabled { background: #e8f5e9; color: #2e7d32; }
.badge.disabled { background: #eee; color: #666; }
.badge-low { background: #e3f2fd; color: #1565c0; }
.badge-medium { background: #fff3e0; color: #e65100; }
.badge-high { background: #ffebee; color: #c62828; }
.badge-critical { background: #fce4ec; color: #880e4f; }
.badge-action-block { background: #ffebee; color: #c62828; }
.badge-action-flag { background: #fff3e0; color: #e65100; }
.badge-action-audit { background: #e8f5e9; color: #2e7d32; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; text-decoration: none; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
.btn-warn { background: #fff3e0; color: #e65100; }
.btn-ok { background: #e8f5e9; color: #2e7d32; }
</style>
