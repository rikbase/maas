<template>
  <div>
    <h1>{{ isEdit ? $t('provider.editTitle') : $t('provider.addTitle') }}</h1>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <form v-else @submit.prevent="save" class="form">
      <div class="field">
        <label>{{ $t('provider.name') }}</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>{{ $t('provider.type') }}</label>
        <select v-model="form.type" required>
          <option value="openai_compatible">{{ $t('provider.types.openai_compatible') }}</option>
          <option value="anthropic">{{ $t('provider.types.anthropic') }}</option>
          <option value="vllm">{{ $t('provider.types.vllm') }}</option>
          <option value="ollama">{{ $t('provider.types.ollama') }}</option>
          <option value="custom">{{ $t('provider.types.custom') }}</option>
        </select>
      </div>
      <div class="field">
        <label>{{ $t('provider.configJson') }}</label>
        <textarea v-model="form.configJson" rows="6" required></textarea>
      </div>
      <button type="submit" class="btn-primary" :disabled="saving">
        {{ saving ? $t('common.loading') : (isEdit ? $t('provider.update') : $t('provider.create')) }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { providerApi } from '../../api/providers'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const loading = ref(isEdit.value)
const saving = ref(false)
const form = ref({ name: '', type: 'openai_compatible', configJson: '{}' })

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const res = await providerApi.get(route.params.id as string)
    form.value = { name: res.data.name, type: res.data.type, configJson: '{}' }
  } catch {
    show(t('common.error'), 'error')
    router.push('/providers')
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await providerApi.update(route.params.id as string, { name: form.value.name, configJson: form.value.configJson })
      show(t('provider.updated'))
    } else {
      await providerApi.create({ ...form.value, type: form.value.type, configJson: form.value.configJson })
      show(t('provider.created'))
    }
    router.push('/providers')
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
