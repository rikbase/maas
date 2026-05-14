<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('security.rules.editTitle') : $t('security.rules.addTitle')" />

    <BaseSpinner v-if="loading" size="lg" />
    <BaseCard v-else>
      <form @submit.prevent="save">
        <BaseFormField :label="$t('security.rules.name')" required>
          <input v-model="form.name" required class="input-field" />
        </BaseFormField>
        <BaseFormField :label="$t('security.rules.description')">
          <textarea v-model="form.description" rows="2" class="input-field"></textarea>
        </BaseFormField>
        <BaseFormField :label="$t('security.rules.type')" required>
          <select v-model="form.detectorType" required :disabled="isEdit" class="input-field">
            <option value="prompt_injection">{{ $t('security.rules.detectorTypes.prompt_injection') }}</option>
            <option value="secret_leak">{{ $t('security.rules.detectorTypes.secret_leak') }}</option>
          </select>
        </BaseFormField>
        <BaseFormField :label="$t('security.rules.severity')">
          <select v-model="form.severity" class="input-field">
            <option value="low">{{ $t('security.rules.severities.low') }}</option>
            <option value="medium">{{ $t('security.rules.severities.medium') }}</option>
            <option value="high">{{ $t('security.rules.severities.high') }}</option>
            <option value="critical">{{ $t('security.rules.severities.critical') }}</option>
          </select>
        </BaseFormField>
        <BaseFormField :label="$t('security.rules.action')">
          <select v-model="form.action" class="input-field">
            <option value="block">{{ $t('security.rules.actionsList.block') }}</option>
            <option value="flag">{{ $t('security.rules.actionsList.flag') }}</option>
            <option value="audit">{{ $t('security.rules.actionsList.audit') }}</option>
          </select>
        </BaseFormField>
        <BaseFormField :label="$t('security.rules.threshold')">
          <input v-model.number="form.threshold" type="number" step="0.01" min="0" max="1" class="input-field" />
        </BaseFormField>
        <BaseFormField :label="$t('security.rules.configJson')">
          <textarea v-model="form.configJson" rows="6" class="input-field"></textarea>
        </BaseFormField>
        <div style="margin-top: var(--space-4);">
          <BaseButton variant="primary" type="submit" :loading="saving" :disabled="saving">
            {{ saving ? '' : (isEdit ? $t('security.rules.update') : $t('security.rules.create')) }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ruleApi } from '../../api/security'
import type { CreateRuleRequest } from '../../api/security'
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
const form = ref<CreateRuleRequest>({
  name: '',
  description: '',
  detectorType: 'prompt_injection',
  severity: 'medium',
  action: 'block',
  threshold: 0.85,
  configJson: '{}',
})

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const res = await ruleApi.get(route.params.id as string)
    const r = res.data
    form.value = {
      name: r.name,
      description: r.description || '',
      detectorType: r.detectorType,
      severity: r.severity,
      action: r.action,
      threshold: r.threshold,
      configJson: r.configJson,
    }
  } catch {
    show(t('common.error'), 'error')
    router.push('/security/rules')
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await ruleApi.update(route.params.id as string, { ...form.value })
      show(t('security.rules.updated'))
    } else {
      await ruleApi.create(form.value)
      show(t('security.rules.created'))
    }
    router.push('/security/rules')
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
form {
  max-width: 560px;
}
.input-field {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  background: var(--color-bg-card);
  width: 100%;
  box-sizing: border-box;
}
.input-field:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
  outline: none;
}
.input-field:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
textarea.input-field {
  font-family: var(--font-mono);
  font-size: 0.857rem;
}
</style>
