<template>
  <div>
    <div class="header">
      <h1>{{ $t('registry.tools.title') }}</h1>
      <router-link v-if="auth.isAdmin" to="/tools/new" class="btn btn-add">{{ $t('registry.tools.add') }}</router-link>
    </div>

    <div class="filters">
      <input v-model="searchQ" :placeholder="$t('registry.tools.searchPlaceholder')" class="input search-input" @input="onSearch" />
      <select v-model="filterSource" class="input filter-select" @change="onSearch">
        <option value="">{{ $t('registry.tools.allSources') }}</option>
        <option value="built_in">built_in</option>
        <option value="mcp">mcp</option>
        <option value="api">api</option>
      </select>
      <select v-model="filterEnabled" class="input filter-select" @change="onSearch">
        <option value="">{{ $t('registry.tools.allStatuses') }}</option>
        <option value="true">{{ $t('registry.tools.enabled') }}</option>
        <option value="false">{{ $t('registry.tools.disabled') }}</option>
      </select>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="filtered.length === 0" class="empty">
      <p>{{ $t('registry.tools.empty') }}</p>
    </div>
    <table v-else class="table">
      <thead><tr>
        <th>{{ $t('registry.tools.name') }}</th>
        <th>{{ $t('provider.description') }}</th>
        <th>{{ $t('registry.tools.source') }}</th>
        <th>{{ $t('registry.tools.status') }}</th>
        <th v-if="auth.isAdmin">{{ $t('provider.actions') }}</th>
      </tr></thead>
      <tbody>
        <tr v-for="t in filtered" :key="t.id">
          <td>
            <router-link :to="`/tools/${t.id}`" class="link">{{ t.name }}</router-link>
          </td>
          <td><span class="desc">{{ t.description || '—' }}</span></td>
          <td><span class="badge badge-source">{{ t.source }}</span></td>
          <td>
            <button v-if="auth.isAdmin" @click="toggleTool(t)" class="badge-btn" :class="t.enabled ? 'enabled' : 'disabled'">
              {{ t.enabled ? $t('registry.tools.enabled') : $t('registry.tools.disabled') }}
            </button>
            <span v-else class="badge" :class="t.enabled ? 'enabled' : 'disabled'">
              {{ t.enabled ? 'Enabled' : 'Disabled' }}
            </span>
          </td>
          <td v-if="auth.isAdmin">
            <router-link :to="`/tools/${t.id}/edit`" class="btn-sm">{{ $t('provider.edit') }}</router-link>
            <button @click="deleteTool(t.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { toolApi } from '../../api/registry'
import type { ToolDef } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const allTools = ref<ToolDef[]>([])
const searchQ = ref('')
const filterSource = ref('')
const filterEnabled = ref('')

const filtered = computed(() => {
  let list = allTools.value
  const q = searchQ.value.toLowerCase()
  if (q) list = list.filter(t => t.name.toLowerCase().includes(q) || (t.description && t.description.toLowerCase().includes(q)))
  if (filterSource.value) list = list.filter(t => t.source === filterSource.value)
  if (filterEnabled.value !== '') list = list.filter(t => String(t.enabled) === filterEnabled.value)
  return list
})

onMounted(async () => {
  await loadTools()
})

async function loadTools() {
  try {
    const res = await toolApi.list()
    allTools.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    try {
      const params: any = {}
      if (searchQ.value) params.q = searchQ.value
      if (filterSource.value) params.source = filterSource.value
      if (filterEnabled.value !== '') params.enabled = filterEnabled.value === 'true'
      const res = await toolApi.list(Object.keys(params).length > 0 ? params : undefined)
      allTools.value = res.data
    } catch {
      show(t('common.error'), 'error')
    }
  }, 300)
}

async function toggleTool(item: ToolDef) {
  try {
    const res = await toolApi.toggle(item.id)
    const idx = allTools.value.findIndex(x => x.id === item.id)
    if (idx >= 0) allTools.value[idx] = res.data
    show(res.data.enabled ? t('registry.tools.enabledMsg') : t('registry.tools.disabledMsg'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteTool(id: string) {
  if (!confirm(t('registry.tools.deleteConfirm'))) return
  try {
    await toolApi.delete(id)
    allTools.value = allTools.value.filter(t => t.id !== id)
    show(t('registry.tools.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.filters { display: flex; gap: 8px; margin-bottom: 16px; }
.search-input { flex: 1; max-width: 300px; }
.filter-select { width: 140px; }
.input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.desc { color: #666; font-size: 12px; }
.link { color: #1976d2; text-decoration: none; font-weight: 600; }
.link:hover { text-decoration: underline; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge-source { background: #e8eaf6; color: #283593; }
.enabled { background: #e8f5e9; color: #2e7d32; }
.disabled { background: #eee; color: #666; }
.badge-btn { padding: 2px 8px; border-radius: 4px; font-size: 12px; border: none; cursor: pointer; }
.btn-add { padding: 8px 16px; background: #1976d2; color: white; border-radius: 4px; text-decoration: none; font-size: 13px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; text-decoration: none; display: inline-block; margin-right: 4px; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
