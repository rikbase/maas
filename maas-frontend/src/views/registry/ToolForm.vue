<template>
  <div class="form-page">
    <h1>{{ isEdit ? $t('registry.tools.editTitle') : $t('registry.tools.addTitle') }}</h1>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <form v-else @submit.prevent="handleSubmit" class="form">
      <div class="field">
        <label>{{ $t('registry.tools.name') }} <span class="req">*</span></label>
        <input v-model="form.name" required class="input" />
      </div>
      <div class="field">
        <label>{{ $t('provider.description') }}</label>
        <textarea v-model="form.description" class="input" rows="2"></textarea>
      </div>
      <div class="field">
        <label>{{ $t('registry.tools.source') }}</label>
        <select v-model="form.source" class="input">
          <option value="built_in">built_in</option>
          <option value="mcp">mcp</option>
          <option value="api">api</option>
        </select>
      </div>
      <div class="field">
        <label>{{ $t('registry.tools.sourceRef') }}</label>
        <input v-model="form.sourceRef" class="input" :placeholder="$t('registry.tools.sourceRefHint')" />
      </div>
      <div class="field">
        <label>{{ $t('registry.tools.inputSchema') }}</label>
        <textarea v-model="form.inputSchema" class="input code" rows="6"></textarea>
      </div>
      <div class="actions">
        <button type="submit" class="btn btn-primary" :disabled="saving">
          {{ saving ? $t('common.loading') : $t('common.save') }}
        </button>
        <router-link to="/tools" class="btn btn-cancel">{{ $t('common.cancel') }}</router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
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
    const t = res.data
    form.value = {
      name: t.name,
      description: t.description || '',
      source: t.source,
      sourceRef: t.sourceRef || '',
      inputSchema: t.inputSchema || '{}',
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
.form-page { max-width: 600px; margin: 0 auto; }
.loading { text-align: center; padding: 40px; color: #666; }
.form { background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 4px; color: #333; }
.req { color: #c62828; }
.input { width: 100%; padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; box-sizing: border-box; }
.input.code { font-family: monospace; font-size: 12px; }
textarea.input { resize: vertical; }
.actions { display: flex; gap: 8px; margin-top: 20px; }
.btn { padding: 8px 20px; border-radius: 4px; border: none; cursor: pointer; font-size: 13px; text-decoration: none; display: inline-block; }
.btn-primary { background: #1976d2; color: white; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { background: #f5f5f5; color: #333; border: 1px solid #ddd; }
</style>
