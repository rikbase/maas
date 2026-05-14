<template>
  <div>
    <BasePageHeader :title="$t('user.title')">
      <template #actions>
        <BaseButton variant="primary" @click="$router.push('/users/new')">
          {{ $t('user.add') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" />

    <BaseEmpty
      v-else-if="users.length === 0"
      :text="$t('user.empty')"
    />

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>{{ $t('user.username') }}</th>
          <th>{{ $t('user.displayName') }}</th>
          <th>{{ $t('user.role') }}</th>
          <th>{{ $t('user.status') }}</th>
          <th>{{ $t('user.createdAt') }}</th>
          <th>{{ $t('user.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.username }}</td>
          <td>{{ u.displayName || '-' }}</td>
          <td>
            <BaseBadge :variant="u.role === 'admin' ? 'primary' : 'neutral'">
              {{ $t('user.roles.' + u.role) }}
            </BaseBadge>
          </td>
          <td>
            <BaseBadge :variant="u.status === 'active' ? 'success' : 'danger'">
              {{ $t('user.statuses.' + u.status) }}
            </BaseBadge>
          </td>
          <td class="text-mono">{{ formatDate(u.createdAt) }}</td>
          <td>
            <BaseButton variant="secondary" size="sm" @click="$router.push('/users/' + u.id + '/edit')">
              {{ $t('user.edit') }}
            </BaseButton>
            <BaseButton
              v-if="u.username !== currentUsername"
              variant="danger"
              size="sm"
              @click="deleteUser(u.id)"
            >
              {{ $t('common.delete') }}
            </BaseButton>
          </td>
        </tr>
      </tbody>
    </table>
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
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()
const auth = useAuthStore()

const loading = ref(true)
const users = ref<UserVO[]>([])
const currentUsername = auth.user?.username || ''

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
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th {
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
  border-bottom: 2px solid var(--color-border);
  padding: 10px 12px;
  text-align: left;
  font-weight: 600;
}
.data-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
}
.data-table tbody tr:hover td {
  background: var(--color-bg-muted);
}
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.857rem;
}
</style>
