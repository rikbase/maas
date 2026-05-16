<template>
  <div>
    <BasePageHeader :title="$t('key.title')">
      <template #actions>
        <BaseButton variant="primary" @click="$router.push('/keys/new')">
          {{ $t('key.create') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="keys"
      :loading="loading"
      :empty-text="$t('key.empty')"
      card
    >
      <template #cell-name="{ row }">
        <strong>{{ row.name }}</strong>
      </template>
      <template #cell-type="{ row }">
        {{ $t('key.types.' + row.keyType) }}
      </template>
      <template #cell-keySuffix="{ row }">
        <code class="key-suffix">...{{ row.keyPrefix }}</code>
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="row.status === 'active' ? 'success' : 'danger'">
          {{ $t('key.statuses.' + row.status) }}
        </BaseBadge>
      </template>
      <template #cell-expires="{ row }">
        {{ row.expiresAt ? new Date(row.expiresAt).toLocaleDateString() : $t('key.never') }}
      </template>
      <template #cell-actions="{ row }">
        <BaseButton
          v-if="row.status === 'active'"
          variant="secondary"
          size="sm"
          @click="revokeKey(row.id)"
        >
          {{ $t('key.revoke') }}
        </BaseButton>
        <BaseButton variant="danger" size="sm" @click="deleteKey(row.id)">
          {{ $t('key.delete') }}
        </BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { keyApi } from '../../api/keys'
import type { ApiKey } from '../../types'
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
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const keys = ref<ApiKey[]>([])

const columns: TableColumn[] = [
  { key: 'name', label: t('key.name') },
  { key: 'type', label: t('key.type') },
  { key: 'keySuffix', label: t('key.keySuffix') },
  { key: 'status', label: t('key.status') },
  { key: 'expires', label: t('key.expires') },
  { key: 'actions', label: t('key.actions') },
]

onMounted(async () => {
  try {
    const res = await keyApi.list()
    keys.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function revokeKey(id: string) {
  if (!(await confirmDialog(t('key.revokeConfirm')))) return
  try {
    await keyApi.revoke(id)
    keys.value = keys.value.map(k => k.id === id ? { ...k, status: 'revoked' } : k)
    show(t('key.revoked'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteKey(id: string) {
  if (!(await confirmDialog(t('key.deleteConfirm')))) return
  try {
    await keyApi.delete(id)
    keys.value = keys.value.filter(k => k.id !== id)
    show(t('key.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.key-suffix {
  font-family: var(--font-mono);
  background: var(--color-bg-muted);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-size: 0.857rem;
}
</style>
