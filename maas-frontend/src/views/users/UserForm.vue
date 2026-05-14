<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('user.editTitle') : $t('user.addTitle')">
      <template #actions>
        <BaseButton variant="secondary" size="sm" @click="$router.push('/users')">
          {{ $t('common.cancel') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseCard>
      <BaseSpinner v-if="loading" size="lg" class="loading-spinner" />
      <form v-else @submit.prevent="save" class="user-form">
        <BaseFormField :label="$t('user.username')" required>
          <input
            v-model="form.username"
            class="form-input"
            required
            :disabled="isEdit || saving"
            :placeholder="$t('user.username')"
          />
        </BaseFormField>

        <BaseFormField :label="$t('user.displayName')">
          <input
            v-model="form.displayName"
            class="form-input"
            :placeholder="$t('user.displayName')"
            :disabled="saving"
          />
        </BaseFormField>

        <BaseFormField :label="$t('user.password')" :required="!isEdit">
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            :placeholder="isEdit ? $t('user.passwordPlaceholder') : ''"
            :required="!isEdit"
            :disabled="saving"
          />
        </BaseFormField>

        <BaseFormField :label="$t('user.role')" required>
          <select v-model="form.role" class="form-input" required :disabled="saving">
            <option value="admin">{{ $t('user.roles.admin') }}</option>
            <option value="viewer">{{ $t('user.roles.viewer') }}</option>
          </select>
        </BaseFormField>

        <BaseFormField v-if="isEdit" :label="$t('user.status')" required>
          <select v-model="form.status" class="form-input" required :disabled="saving">
            <option value="active">{{ $t('user.statuses.active') }}</option>
            <option value="disabled">{{ $t('user.statuses.disabled') }}</option>
          </select>
        </BaseFormField>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
            {{ saving ? $t('common.saving') : (isEdit ? $t('common.save') : $t('user.create')) }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '../../api/users'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseFormField from '../../components/ui/BaseFormField.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const loading = ref(isEdit.value)
const saving = ref(false)

const form = ref({
  username: '',
  displayName: '',
  password: '',
  role: 'viewer',
  status: 'active',
})

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const res = await userApi.get(route.params.id as string)
    form.value = {
      username: res.data.username,
      displayName: res.data.displayName || '',
      password: '',
      role: res.data.role,
      status: res.data.status,
    }
  } catch {
    show(t('common.error'), 'error')
    router.push('/users')
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      const payload: any = {}
      if (form.value.displayName) payload.displayName = form.value.displayName
      if (form.value.password) payload.password = form.value.password
      payload.role = form.value.role
      payload.status = form.value.status
      await userApi.update(route.params.id as string, payload)
      show(t('user.updated'))
    } else {
      await userApi.create({
        username: form.value.username,
        password: form.value.password,
        displayName: form.value.displayName || undefined,
        role: form.value.role,
      })
      show(t('user.created'))
    }
    router.push('/users')
  } catch (e: any) {
    show(e.message || t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.loading-spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}
.user-form {
  max-width: 480px;
}
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
  box-sizing: border-box;
}
.form-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}
select.form-input {
  cursor: pointer;
}
.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>
