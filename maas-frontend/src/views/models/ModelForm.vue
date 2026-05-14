<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('model.editTitle') : $t('model.addTitle')">
      <template #actions>
        <BaseButton variant="secondary" size="sm" @click="$router.push('/models')">
          {{ $t('common.cancel') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseCard>
      <BaseSpinner v-if="loading" size="lg" class="loading-spinner" />
      <form v-else @submit.prevent="save" class="model-form">
        <BaseFormField :label="$t('model.modelName')" required>
          <input v-model="form.modelName" class="form-input" required />
        </BaseFormField>

        <BaseFormField :label="$t('model.provider')" required>
          <select v-model="form.providerId" class="form-input" required :disabled="isEdit">
            <option v-for="p in providers" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </BaseFormField>

        <BaseFormField :label="$t('model.status')">
          <select v-model="form.status" class="form-input">
            <option value="active">{{ $t('model.statuses.active') }}</option>
          </select>
        </BaseFormField>

        <BaseFormField :label="$t('model.capabilities')" :help="$t('model.capabilitiesHelp')">
          <textarea v-model="form.capabilities" class="form-input form-textarea" rows="4"></textarea>
        </BaseFormField>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
            {{ saving ? $t('common.saving') : (isEdit ? $t('provider.update') : $t('provider.create')) }}
          </BaseButton>
          <BaseButton variant="secondary" @click="$router.push('/models')">
            {{ $t('common.cancel') }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { providerApi, modelApi } from '../../api/providers'
import type { Provider } from '../../types'
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
const providers = ref<Provider[]>([])
const form = ref({ modelName: '', providerId: '', status: 'active', capabilities: '[]' })

onMounted(async () => {
  try {
    const pRes = await providerApi.list()
    providers.value = pRes.data
    if (isEdit.value) {
      const res = await modelApi.get(route.params.id as string)
      form.value = {
        modelName: res.data.modelName,
        providerId: res.data.providerId,
        status: res.data.status || 'active',
        capabilities: res.data.capabilities || '[]',
      }
    }
  } catch {
    show(t('common.error'), 'error')
    router.push('/models')
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await modelApi.update(route.params.id as string, {
        modelName: form.value.modelName,
        status: form.value.status,
        capabilities: form.value.capabilities,
      })
      show(t('model.updated'))
    } else {
      await modelApi.create(form.value)
      show(t('model.created'))
    }
    router.push('/models')
  } catch {
    show(t('common.error'), 'error')
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
.model-form {
  max-width: 520px;
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
.form-textarea {
  min-height: 80px;
  resize: vertical;
  font-family: var(--font-mono);
  font-size: 0.857rem;
}
.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>
