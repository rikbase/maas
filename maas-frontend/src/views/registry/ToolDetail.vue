<template>
  <div class="detail-page">
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="!tool" class="empty">{{ $t('registry.tools.notFound') }}</div>
    <template v-else>
      <div class="header">
        <router-link to="/tools" class="back">← {{ $t('workflow.back') }}</router-link>
        <h1>{{ tool.name }}</h1>
        <div class="header-actions" v-if="auth.isAdmin">
          <router-link :to="`/tools/${tool.id}/edit`" class="btn btn-edit">{{ $t('provider.edit') }}</router-link>
          <button @click="toggleTool" class="btn" :class="tool.enabled ? 'btn-warn' : 'btn-success'">
            {{ tool.enabled ? $t('security.rules.disable') : $t('security.rules.enable') }}
          </button>
          <button @click="deleteTool" class="btn btn-danger">{{ $t('provider.delete') }}</button>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item"><span class="label">ID</span><span class="value mono">{{ tool.id }}</span></div>
        <div class="info-item"><span class="label">{{ $t('registry.tools.name') }}</span><span class="value">{{ tool.name }}</span></div>
        <div class="info-item"><span class="label">{{ $t('provider.description') }}</span><span class="value">{{ tool.description || '—' }}</span></div>
        <div class="info-item"><span class="label">{{ $t('registry.tools.source') }}</span><span class="value"><span class="badge badge-source">{{ tool.source }}</span></span></div>
        <div class="info-item"><span class="label">{{ $t('registry.tools.sourceRef') }}</span><span class="value mono">{{ tool.sourceRef || '—' }}</span></div>
        <div class="info-item"><span class="label">{{ $t('registry.tools.status') }}</span><span class="value"><span class="badge" :class="tool.enabled ? 'enabled' : 'disabled'">{{ tool.enabled ? 'Enabled' : 'Disabled' }}</span></span></div>
        <div class="info-item"><span class="label">{{ $t('workflow.createdAt') }}</span><span class="value">{{ new Date(tool.createdAt).toLocaleString() }}</span></div>
        <div class="info-item"><span class="label">{{ $t('workflow.updatedAt') }}</span><span class="value">{{ new Date(tool.updatedAt).toLocaleString() }}</span></div>
      </div>

      <div class="section">
        <h2>{{ $t('registry.tools.inputSchema') }}</h2>
        <pre class="json-block">{{ formatJson(tool.inputSchema) }}</pre>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toolApi } from '../../api/registry'
import type { ToolDef } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const tool = ref<ToolDef | null>(null)

onMounted(async () => {
  try {
    const res = await toolApi.get(route.params.id as string)
    tool.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

function formatJson(json: string) {
  try { return JSON.stringify(JSON.parse(json), null, 2) } catch { return json }
}

async function toggleTool() {
  if (!tool.value) return
  try {
    const res = await toolApi.toggle(tool.value.id)
    tool.value = res.data
    show(res.data.enabled ? t('registry.tools.enabledMsg') : t('registry.tools.disabledMsg'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteTool() {
  if (!tool.value || !confirm(t('registry.tools.deleteConfirm'))) return
  try {
    await toolApi.delete(tool.value.id)
    show(t('registry.tools.deleted'))
    router.push('/tools')
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.detail-page { max-width: 800px; margin: 0 auto; }
.loading, .empty { text-align: center; padding: 40px; color: #666; }
.header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.header h1 { margin: 0; font-size: 20px; flex: 1; }
.back { color: #1976d2; text-decoration: none; font-size: 13px; }
.header-actions { display: flex; gap: 6px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.label { font-size: 11px; color: #999; text-transform: uppercase; letter-spacing: 0.5px; }
.value { font-size: 14px; color: #333; }
.mono { font-family: monospace; font-size: 12px; }
.section { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.section h2 { margin: 0 0 12px 0; font-size: 16px; }
.json-block { background: #f5f5f5; padding: 12px; border-radius: 4px; font-size: 12px; overflow-x: auto; margin: 0; }
.btn { padding: 6px 14px; border-radius: 4px; border: none; cursor: pointer; font-size: 12px; text-decoration: none; display: inline-block; }
.btn-edit { background: #e3f2fd; color: #1976d2; }
.btn-success { background: #e8f5e9; color: #2e7d32; }
.btn-warn { background: #fff3e0; color: #e65100; }
.btn-danger { background: #ffebee; color: #c62828; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge-source { background: #e8eaf6; color: #283593; }
.enabled { background: #e8f5e9; color: #2e7d32; }
.disabled { background: #eee; color: #666; }
</style>
