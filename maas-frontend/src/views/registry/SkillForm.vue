<template>
  <div>
    <h1>{{ isEdit ? $t('registry.skills.editTitle') : $t('registry.skills.addTitle') }}</h1>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <form v-else @submit.prevent="save" class="form">
      <div class="field">
        <label>{{ $t('registry.skills.name') }}</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>{{ $t('registry.skills.description') }}</label>
        <textarea v-model="form.description" rows="3"></textarea>
      </div>
      <div class="field">
        <label>{{ $t('registry.skills.type') }}</label>
        <input v-model="form.type" placeholder="e.g. translation, summarization" />
      </div>
      <div class="field">
        <label>{{ $t('registry.skills.configJson') }}</label>
        <textarea v-model="form.configJson" rows="4"></textarea>
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
.loading { color: #666; padding: 20px; text-align: center; }
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
