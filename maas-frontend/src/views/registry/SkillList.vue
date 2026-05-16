<template>
  <div>
    <BasePageHeader :title="$t('registry.skills.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/skills/new')">
          {{ $t('registry.skills.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="skills"
      :loading="loading"
      :empty-text="$t('registry.skills.empty')"
      :row-click="(s: any) => router.push(`/skills/${s.id}`)"
      card
    >
      <template #cell-name="{ row }">
        <router-link :to="`/skills/${row.id}`" class="link" @click.stop>{{ row.name }}</router-link>
        <span v-if="row.description" class="desc">{{ row.description }}</span>
      </template>
      <template #cell-type="{ row }">
        {{ row.type || '-' }}
      </template>
      <template #cell-version="{ row }">
        v{{ row.version }}
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="badgeVariant(row.status)">{{ $t('registry.skills.statuses.' + row.status) }}</BaseBadge>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton size="sm" variant="ghost" @click.stop="router.push(`/skills/${row.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
        <BaseButton v-if="row.status !== 'published'" size="sm" variant="ghost" @click.stop="publish(row)">{{ $t('registry.skills.publish') }}</BaseButton>
        <BaseButton size="sm" variant="danger" @click.stop="deleteSkill(row.id)">{{ $t('provider.delete') }}</BaseButton>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { skillApi } from '../../api/registry'
import type { Skill } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'

const { t } = useI18n()
const { show } = useToast()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const skills = ref<Skill[]>([])

const columns: TableColumn[] = [
  { key: 'name', label: t('registry.skills.name') },
  { key: 'type', label: t('registry.skills.type') },
  { key: 'version', label: t('registry.skills.version') },
  { key: 'status', label: t('registry.skills.status') },
  { key: 'actions', label: t('registry.skills.actions') },
]

function badgeVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = {
    draft: 'neutral',
    published: 'success',
    deprecated: 'warning',
    retired: 'danger',
  }
  return map[status] || 'neutral'
}

onMounted(async () => {
  try {
    const res = await skillApi.list()
    skills.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function publish(s: Skill) {
  try {
    const res = await skillApi.publish(s.id)
    Object.assign(s, res.data)
    show(t('registry.skills.published'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteSkill(id: string) {
  if (!(await confirmDialog(t('registry.skills.deleteConfirm')))) return
  try {
    await skillApi.delete(id)
    skills.value = skills.value.filter(s => s.id !== id)
    show(t('registry.skills.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}
.link:hover {
  text-decoration: underline;
}
.desc {
  display: block;
  font-size: 0.8rem;
  color: var(--color-foreground-secondary);
  margin-top: 2px;
}
</style>
