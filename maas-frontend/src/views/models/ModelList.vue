<template>
  <div>
    <BasePageHeader :title="$t('model.title')">
      <template #actions>
        <BaseButton variant="primary" @click="router.push('/models/new')">
          {{ $t('model.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="models"
      :loading="loading"
      :empty-text="$t('model.empty')"
      card
    >
      <template #cell-modelName="{ row }">
        <strong>{{ row.modelName }}</strong>
      </template>
      <template #cell-provider="{ row }">
        {{ row.providerName }}
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="row.status === 'active' ? 'success' : 'neutral'">
          {{ $t('model.statuses.' + row.status) }}
        </BaseBadge>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton size="sm" variant="ghost" @click="router.push(`/models/${row.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
        <BaseButton size="sm" variant="danger" @click="deleteModel(row.id)">{{ $t('provider.delete') }}</BaseButton>
      </template>
    </BaseTable>
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
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'

const { t } = useI18n()
const { show } = useToast()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const models = ref<ModelDef[]>([])

const columns: TableColumn[] = [
  { key: 'modelName', label: t('model.modelName') },
  { key: 'provider', label: t('model.provider') },
  { key: 'status', label: t('model.status') },
  { key: 'actions', label: t('provider.actions') },
]

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
