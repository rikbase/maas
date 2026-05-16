<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('registry.tools.editTitle') : $t('registry.tools.addTitle')" />

    <BaseSpinner v-if="loading" class="spinner" />

    <BaseCard v-else>
      <form @submit.prevent="handleSubmit">
        <BaseFormField :label="$t('registry.tools.name')" required>
          <input v-model="form.name" required class="form-input" />
        </BaseFormField>

        <BaseFormField :label="$t('provider.description')">
          <textarea v-model="form.description" rows="2" class="form-input"></textarea>
        </BaseFormField>

        <BaseFormField :label="$t('registry.tools.source')">
          <select v-model="form.source" class="form-input">
            <option value="built_in">built_in</option>
            <option value="mcp">mcp</option>
            <option value="api">api</option>
          </select>
        </BaseFormField>

        <BaseFormField :label="$t('registry.tools.sourceRef')" :help="$t('registry.tools.sourceRefHint')">
          <input v-model="form.sourceRef" class="form-input" :placeholder="$t('registry.tools.sourceRefHint')" />
        </BaseFormField>

        <BaseFormField :label="$t('registry.tools.inputSchema')">
          <textarea v-model="form.inputSchema" rows="6" class="form-input form-input-code"></textarea>
        </BaseFormField>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
            {{ saving ? $t('common.loading') : $t('common.save') }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseFormField from '../../components/ui/BaseFormField.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toolApi } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const isEdit = !!route.params.id
const loading = ref(isEdit)
const saving = ref(false)

const form = ref({
  name: '',
  description: '',
  source: 'built_in',
  sourceRef: '',
  inputSchema: '{}',
})

onMounted(async () => {
  if (!isEdit) return
  try {
    const res = await toolApi.get(route.params.id as string)
    const tt = res.data
    form.value = {
      name: tt.name,
      description: tt.description || '',
      source: tt.source,
      sourceRef: tt.sourceRef || '',
      inputSchema: tt.inputSchema || '{}',
    }
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  saving.value = true
  try {
    if (isEdit) {
      await toolApi.update(route.params.id as string, form.value)
      show(t('registry.tools.updated'))
    } else {
      await toolApi.create(form.value)
      show(t('registry.tools.created'))
    }
    router.push('/tools')
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}
.form-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-5);
}
.form-input {
  padding: 8px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  font-size: 0.875rem;
  font-family: var(--font-sans);
  color: var(--color-foreground);
  width: 100%;
  box-sizing: border-box;
  background: var(--color-bg-card);
  transition: border-color 0.15s, box-shadow 0.15s;
}
.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.form-input-code {
  font-family: var(--font-mono);
  font-size: 0.8rem;
}
textarea.form-input {
  resize: vertical;
}
</style>
