<template>
  <div>
    <BasePageHeader :title="$t('registry.tools.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/tools/new')">
          {{ $t('registry.tools.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <div class="filter-bar">
      <input v-model="searchQ" :placeholder="$t('registry.tools.searchPlaceholder')" class="form-input filter-search" @input="onSearch" />
      <select v-model="filterSource" class="filter-select" @change="onSearch">
        <option value="">{{ $t('registry.tools.allSources') }}</option>
        <option value="built_in">built_in</option>
        <option value="mcp">mcp</option>
        <option value="api">api</option>
      </select>
      <select v-model="filterEnabled" class="filter-select" @change="onSearch">
        <option value="">{{ $t('registry.tools.allStatuses') }}</option>
        <option value="true">{{ $t('registry.tools.enabled') }}</option>
        <option value="false">{{ $t('registry.tools.disabled') }}</option>
      </select>
    </div>

    <BaseSpinner v-if="loading" class="spinner" />

    <BaseEmpty v-else-if="filtered.length === 0" :text="$t('registry.tools.empty')" />

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>{{ $t('registry.tools.name') }}</th>
          <th>{{ $t('provider.description') }}</th>
          <th>{{ $t('registry.tools.source') }}</th>
          <th>{{ $t('registry.tools.status') }}</th>
          <th>{{ $t('provider.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="t in filtered" :key="t.id">
          <td>
            <router-link :to="`/tools/${t.id}`" class="data-table-link">{{ t.name }}</router-link>
          </td>
          <td><span class="data-table-desc">{{ t.description || '—' }}</span></td>
          <td><BaseBadge variant="info">{{ t.source }}</BaseBadge></td>
          <td>
            <BaseButton size="sm" variant="ghost" @click="toggleTool(t)">
              {{ t.enabled ? $t('registry.tools.enabled') : $t('registry.tools.disabled') }}
            </BaseButton>
          </td>
          <td>
            <BaseButton size="sm" variant="ghost" @click="router.push(`/tools/${t.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
            <BaseButton size="sm" variant="danger" @click="deleteTool(t.id)">{{ $t('provider.delete') }}</BaseButton>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { toolApi } from '../../api/registry'
import type { ToolDef } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'

const { t } = useI18n()
const { show } = useToast()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const allTools = ref<ToolDef[]>([])
const searchQ = ref('')
const filterSource = ref('')
const filterEnabled = ref('')

const filtered = computed(() => {
  let list = allTools.value
  const q = searchQ.value.toLowerCase()
  if (q) list = list.filter(tt => tt.name.toLowerCase().includes(q) || (tt.description && tt.description.toLowerCase().includes(q)))
  if (filterSource.value) list = list.filter(tt => tt.source === filterSource.value)
  if (filterEnabled.value !== '') list = list.filter(tt => String(tt.enabled) === filterEnabled.value)
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
      const params: Record<string, any> = {}
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
  if (!(await confirmDialog(t('registry.tools.deleteConfirm')))) return
  try {
    await toolApi.delete(id)
    allTools.value = allTools.value.filter(tt => tt.id !== id)
    show(t('registry.tools.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}
.filter-bar {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
}
.filter-search {
  flex: 1;
  max-width: 300px;
}
.filter-select {
  padding: 8px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 0.857rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
  width: 140px;
}
.filter-select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.form-input {
  padding: 8px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 0.875rem;
  color: var(--color-foreground);
  width: 100%;
  box-sizing: border-box;
  background: var(--color-bg-card);
  transition: border-color 0.15s, box-shadow 0.15s;
}
.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
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
.data-table-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}
.data-table-link:hover {
  text-decoration: underline;
}
.data-table-desc {
  font-size: 0.8rem;
  color: var(--color-foreground-secondary);
}
</style>
