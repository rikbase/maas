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

    <BaseTable
      :columns="columns"
      :data="filtered"
      :loading="loading"
      :empty-text="$t('registry.tools.empty')"
      :row-click="(t: any) => router.push(`/tools/${t.id}`)"
      card
    >
      <template #cell-name="{ row }">
        <router-link :to="`/tools/${row.id}`" class="link" @click.stop>{{ row.name }}</router-link>
      </template>
      <template #cell-description="{ row }">
        <span class="desc">{{ row.description || '—' }}</span>
      </template>
      <template #cell-source="{ row }">
        <BaseBadge variant="info">{{ row.source }}</BaseBadge>
      </template>
      <template #cell-status="{ row }">
        <BaseButton size="sm" variant="ghost" @click.stop="toggleTool(row)">
          {{ row.enabled ? $t('registry.tools.enabled') : $t('registry.tools.disabled') }}
        </BaseButton>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton size="sm" variant="ghost" @click.stop="router.push(`/tools/${row.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
        <BaseButton size="sm" variant="danger" @click.stop="deleteTool(row.id)">{{ $t('provider.delete') }}</BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'
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

const columns: TableColumn[] = [
  { key: 'name', label: t('registry.tools.name') },
  { key: 'description', label: t('provider.description') },
  { key: 'source', label: t('registry.tools.source') },
  { key: 'status', label: t('registry.tools.status') },
  { key: 'actions', label: t('provider.actions') },
]

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
.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}
.link:hover {
  text-decoration: underline;
}
.desc {
  font-size: 0.8rem;
  color: var(--color-foreground-secondary);
}
</style>
