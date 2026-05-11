<template>
  <div>
    <h1>{{ $t('key.createTitle') }}</h1>
    <form @submit.prevent="save" class="form">
      <div class="field">
        <label>{{ $t('key.name') }}</label>
        <input v-model="form.name" required :disabled="saving" />
      </div>
      <div class="field">
        <label>{{ $t('key.type') }}</label>
        <select v-model="form.keyType" required :disabled="saving">
          <option value="application">{{ $t('key.types.application') }}</option>
          <option value="team">{{ $t('key.types.team') }}</option>
          <option value="root">{{ $t('key.types.root') }}</option>
        </select>
      </div>
      <div v-if="createdKey" class="created-key">
        <strong>{{ $t('key.keyCreated') }}</strong>
        <p class="warning">{{ $t('key.copyWarning') }}</p>
        <code>{{ createdKey }}</code>
      </div>
      <button v-if="!createdKey" type="submit" class="btn-primary" :disabled="saving">
        {{ saving ? $t('common.loading') : $t('key.create') }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { keyApi } from '../../api/keys'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()

const form = ref({ name: '', keyType: 'application' as const })
const createdKey = ref<string | null>(null)
const saving = ref(false)

async function save() {
  saving.value = true
  try {
    const res = await keyApi.create({ ...form.value })
    createdKey.value = res.data.rawKey || ''
    show(t('key.created'))
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.created-key { background: #fff8e1; padding: 12px; border-radius: 4px; margin-bottom: 16px; }
.created-key code { word-break: break-all; font-size: 13px; }
.warning { color: #e65100; font-size: 13px; }
</style>
