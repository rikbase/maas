<template>
  <div>
    <BasePageHeader :title="$t('registry.skills.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/skills/new')">
          {{ $t('registry.skills.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" class="spinner" />

    <BaseEmpty v-else-if="skills.length === 0" :text="$t('registry.skills.empty')" />

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>{{ $t('registry.skills.name') }}</th>
          <th>{{ $t('registry.skills.type') }}</th>
          <th>{{ $t('registry.skills.version') }}</th>
          <th>{{ $t('registry.skills.status') }}</th>
          <th>{{ $t('registry.skills.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in skills" :key="s.id">
          <td>
            <router-link :to="`/skills/${s.id}`" class="data-table-link">{{ s.name }}</router-link>
            <span v-if="s.description" class="data-table-desc">{{ s.description }}</span>
          </td>
          <td>{{ s.type || '-' }}</td>
          <td>v{{ s.version }}</td>
          <td>
            <BaseBadge :variant="badgeVariant(s.status)">{{ $t('registry.skills.statuses.' + s.status) }}</BaseBadge>
          </td>
          <td>
            <BaseButton size="sm" variant="ghost" @click="router.push(`/skills/${s.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
            <BaseButton v-if="s.status !== 'published'" size="sm" variant="ghost" @click="publish(s)">{{ $t('registry.skills.publish') }}</BaseButton>
            <BaseButton size="sm" variant="danger" @click="deleteSkill(s.id)">{{ $t('provider.delete') }}</BaseButton>
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
.spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
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
  display: block;
  font-size: 0.8rem;
  color: var(--color-foreground-secondary);
  margin-top: 2px;
}
</style>
