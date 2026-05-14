<template>
  <div>
    <BaseSpinner v-if="loading" class="spinner" />

    <template v-else-if="skill">
      <BasePageHeader :title="skill.name" :description="'v' + skill.version">
        <template #actions>
          <BaseButton variant="primary" size="sm" @click="router.push(`/skills/${skill.id}/edit`)">
            {{ $t('provider.edit') }}
          </BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard noPadding class="info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item-label">{{ $t('registry.skills.type') }}</span>
            <span class="info-item-value">{{ skill.type || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('registry.skills.status') }}</span>
            <span class="info-item-value">
              <BaseBadge :variant="badgeVariant(skill.status)">{{ $t('registry.skills.statuses.' + skill.status) }}</BaseBadge>
            </span>
          </div>
          <div class="info-item" v-if="skill.description">
            <span class="info-item-label">{{ $t('registry.skills.description') }}</span>
            <span class="info-item-value">{{ skill.description }}</span>
          </div>
          <div class="info-item" v-if="skill.publishNote">
            <span class="info-item-label">{{ $t('registry.skills.publishNote') }}</span>
            <span class="info-item-value">{{ skill.publishNote }}</span>
          </div>
        </div>
      </BaseCard>

      <BaseCard class="section-card">
        <template #header>
          <h2 class="card-section-title">{{ $t('registry.tools.title') }}</h2>
        </template>
        <BaseEmpty v-if="tools.length === 0" :text="$t('registry.tools.empty')" />
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>{{ $t('registry.tools.name') }}</th>
              <th>{{ $t('registry.tools.source') }}</th>
              <th>{{ $t('registry.tools.status') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tools" :key="t.id">
              <td>{{ t.name }}</td>
              <td><BaseBadge variant="info">{{ t.source }}</BaseBadge></td>
              <td>
                <BaseBadge :variant="t.enabled ? 'success' : 'neutral'">
                  {{ t.enabled ? $t('registry.tools.enabled') : $t('registry.tools.disabled') }}
                </BaseBadge>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
    </template>
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
import { skillApi, toolApi } from '../../api/registry'
import type { Skill, ToolDef } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const skill = ref<Skill | null>(null)
const tools = ref<ToolDef[]>([])

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
    const [sRes, tRes] = await Promise.all([
      skillApi.get(route.params.id as string),
      toolApi.listBySkill(route.params.id as string),
    ])
    skill.value = sRes.data
    tools.value = tRes.data
  } catch {
    show(t('common.error'), 'error')
    router.push('/skills')
  } finally {
    loading.value = false
  }
})
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
</style>
