<template>
  <div>
    <BasePageHeader :title="$t('user.title')">
      <template #actions>
        <BaseButton variant="primary" @click="$router.push('/users/new')">
          {{ $t('user.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseTable
      :columns="columns"
      :data="users"
      :loading="loading"
      :empty-text="$t('user.empty')"
      card
    >
      <template #cell-username="{ row }">
        <strong>{{ row.username }}</strong>
      </template>
      <template #cell-displayName="{ row }">
        {{ row.displayName || '-' }}
      </template>
      <template #cell-role="{ row }">
        <BaseBadge :variant="row.role === 'admin' ? 'primary' : 'neutral'">
          {{ $t('user.roles.' + row.role) }}
        </BaseBadge>
      </template>
      <template #cell-status="{ row }">
        <BaseBadge :variant="row.status === 'active' ? 'success' : 'danger'">
          {{ $t('user.statuses.' + row.status) }}
        </BaseBadge>
      </template>
      <template #cell-createdAt="{ row }">
        <span class="text-mono">{{ formatDate(row.createdAt) }}</span>
      </template>
      <template #cell-actions="{ row }">
        <BaseButton variant="secondary" size="sm" @click="$router.push('/users/' + row.id + '/edit')">
          {{ $t('user.edit') }}
        </BaseButton>
        <BaseButton
          v-if="row.username !== currentUsername"
          variant="danger"
          size="sm"
          @click="deleteUser(row.id)"
        >
          {{ $t('common.delete') }}
        </BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi, type UserVO } from '../../api/users'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'
import { useAuthStore } from '../../stores/auth'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseTable from '../../components/ui/BaseTable.vue'
import type { TableColumn } from '../../components/ui/BaseTable.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()
const auth = useAuthStore()

const loading = ref(true)
const users = ref<UserVO[]>([])
const currentUsername = auth.user?.username || ''

const columns: TableColumn[] = [
  { key: 'username', label: t('user.username') },
  { key: 'displayName', label: t('user.displayName') },
  { key: 'role', label: t('user.role') },
  { key: 'status', label: t('user.status') },
  { key: 'createdAt', label: t('user.createdAt') },
  { key: 'actions', label: t('user.actions') },
]

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString()
}

onMounted(async () => {
  try {
    const res = await userApi.list()
    users.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function deleteUser(id: string) {
  if (!(await confirmDialog(t('user.deleteConfirm')))) return
  try {
    await userApi.delete(id)
    users.value = users.value.filter(u => u.id !== id)
    show(t('user.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.857rem;
}
</style>
