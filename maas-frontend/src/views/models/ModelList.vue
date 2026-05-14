<template>
  <div>
    <BasePageHeader :title="$t('model.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/models/new')">
          {{ $t('model.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" class="spinner" />

    <BaseEmpty v-else-if="models.length === 0" :text="$t('model.empty')" />

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>{{ $t('model.modelName') }}</th>
          <th>{{ $t('model.provider') }}</th>
          <th>{{ $t('model.status') }}</th>
          <th>{{ $t('provider.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in models" :key="m.id">
          <td>{{ m.modelName }}</td>
          <td>{{ m.providerName }}</td>
          <td>
            <BaseBadge :variant="m.status === 'active' ? 'success' : 'neutral'">
              {{ $t('model.statuses.' + m.status) }}
            </BaseBadge>
          </td>
          <td class="action-cell">
            <BaseButton size="sm" variant="ghost" @click="router.push(`/models/${m.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
            <BaseButton size="sm" variant="danger" @click="deleteModel(m.id)">{{ $t('provider.delete') }}</BaseButton>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { modelApi } from '../../api/providers'
import type { ModelDef } from '../../api/providers'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const models = ref<ModelDef[]>([])

onMounted(async () => {
  try {
    const res = await modelApi.list()
    models.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function deleteModel(id: string) {
  if (!(await confirmDialog(t('model.deleteConfirm')))) return
  try {
    await modelApi.delete(id)
    models.value = models.value.filter(m => m.id !== id)
    show(t('model.deleted'))
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
.action-cell {
  display: flex;
  gap: var(--space-1);
}
</style>
