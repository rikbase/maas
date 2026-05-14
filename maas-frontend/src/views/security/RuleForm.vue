<template>
  <div>
    <h1>{{ isEdit ? $t('security.rules.editTitle') : $t('security.rules.addTitle') }}</h1>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <form v-else @submit.prevent="save" class="form">
      <div class="field">
        <label>{{ $t('security.rules.name') }}</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>{{ $t('security.rules.description') }}</label>
        <textarea v-model="form.description" rows="2"></textarea>
      </div>
      <div class="field">
        <label>{{ $t('security.rules.type') }}</label>
        <select v-model="form.detectorType" required :disabled="isEdit">
          <option value="prompt_injection">{{ $t('security.rules.detectorTypes.prompt_injection') }}</option>
          <option value="secret_leak">{{ $t('security.rules.detectorTypes.secret_leak') }}</option>
        </select>
      </div>
      <div class="field">
        <label>{{ $t('security.rules.severity') }}</label>
        <select v-model="form.severity">
          <option value="low">{{ $t('security.rules.severities.low') }}</option>
          <option value="medium">{{ $t('security.rules.severities.medium') }}</option>
          <option value="high">{{ $t('security.rules.severities.high') }}</option>
          <option value="critical">{{ $t('security.rules.severities.critical') }}</option>
        </select>
      </div>
      <div class="field">
        <label>{{ $t('security.rules.action') }}</label>
        <select v-model="form.action">
          <option value="block">{{ $t('security.rules.actionsList.block') }}</option>
          <option value="flag">{{ $t('security.rules.actionsList.flag') }}</option>
          <option value="audit">{{ $t('security.rules.actionsList.audit') }}</option>
        </select>
      </div>
      <div class="field">
        <label>{{ $t('security.rules.threshold') }}</label>
        <input v-model.number="form.threshold" type="number" step="0.01" min="0" max="1" />
      </div>
      <div class="field">
        <label>{{ $t('security.rules.configJson') }}</label>
        <textarea v-model="form.configJson" rows="6"></textarea>
      </div>
      <button type="submit" class="btn-primary" :disabled="saving">
        {{ saving ? $t('common.loading') : (isEdit ? $t('security.rules.update') : $t('security.rules.create')) }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ruleApi } from '../../api/security'
import type { CreateRuleRequest } from '../../api/security'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

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
.loading { color: #666; padding: 20px; text-align: center; }
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field select, .field textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
