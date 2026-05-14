<template>
  <div>
    <BasePageHeader :title="$t('key.title')">
      <template #actions>
        <BaseButton variant="primary" @click="$router.push('/keys/new')">
          {{ $t('key.create') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseSpinner v-if="loading" />

    <BaseEmpty
      v-else-if="keys.length === 0"
      :text="$t('key.empty')"
    />

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>{{ $t('key.name') }}</th>
          <th>{{ $t('key.type') }}</th>
          <th>{{ $t('key.keySuffix') }}</th>
          <th>{{ $t('key.status') }}</th>
          <th>{{ $t('key.expires') }}</th>
          <th>{{ $t('key.actions') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="k in keys" :key="k.id">
          <td>{{ k.name }}</td>
          <td>{{ $t('key.types.' + k.keyType) }}</td>
          <td><code>...{{ k.keyPrefix }}</code></td>
          <td>
            <BaseBadge :variant="k.status === 'active' ? 'success' : 'danger'">
              {{ $t('key.statuses.' + k.status) }}
            </BaseBadge>
          </td>
          <td>{{ k.expiresAt ? new Date(k.expiresAt).toLocaleDateString() : $t('key.never') }}</td>
          <td>
            <BaseButton
              v-if="k.status === 'active'"
              variant="secondary"
              size="sm"
              @click="revokeKey(k.id)"
            >
              {{ $t('key.revoke') }}
            </BaseButton>
            <BaseButton variant="danger" size="sm" @click="deleteKey(k.id)">
              {{ $t('key.delete') }}
            </BaseButton>
          </td>
        </tr>
      </tbody>
    </table>
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
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const keys = ref<ApiKey[]>([])

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
.data-table code {
  font-family: var(--font-mono);
  background: var(--color-bg-muted);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-size: 0.857rem;
}
</style>
