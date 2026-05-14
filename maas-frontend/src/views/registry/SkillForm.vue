<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('registry.skills.editTitle') : $t('registry.skills.addTitle')" />

    <BaseSpinner v-if="loading" class="spinner" />

    <BaseCard v-else>
      <form @submit.prevent="save">
        <BaseFormField :label="$t('registry.skills.name')" required>
          <input v-model="form.name" required class="form-input" />
        </BaseFormField>

        <BaseFormField :label="$t('registry.skills.description')">
          <textarea v-model="form.description" rows="3" class="form-input"></textarea>
        </BaseFormField>

        <BaseFormField :label="$t('registry.skills.type')">
          <input v-model="form.type" placeholder="e.g. translation, summarization" class="form-input" />
        </BaseFormField>

        <BaseFormField :label="$t('registry.skills.configJson')">
          <textarea v-model="form.configJson" rows="4" class="form-input form-input-code"></textarea>
        </BaseFormField>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
            {{ saving ? $t('common.loading') : (isEdit ? $t('provider.update') : $t('provider.create')) }}
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { skillApi } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const loading = ref(isEdit.value)
const saving = ref(false)
const form = ref({ name: '', description: '', type: '', configJson: '{}' })

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const res = await skillApi.get(route.params.id as string)
    const s = res.data
    form.value = { name: s.name, description: s.description || '', type: s.type || '', configJson: s.configJson }
  } catch {
    show(t('common.error'), 'error')
    router.push('/skills')
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await skillApi.update(route.params.id as string, { ...form.value })
      show(t('registry.skills.updated'))
    } else {
      await skillApi.create({ ...form.value })
      show(t('registry.skills.created'))
    }
    router.push('/skills')
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
