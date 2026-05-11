<template>
  <div>
    <h1>{{ isEdit ? 'Edit Provider' : 'Add Provider' }}</h1>
    <form @submit.prevent="save" class="form">
      <div class="field">
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Type</label>
        <select v-model="form.type" required>
          <option value="openai_compatible">OpenAI Compatible</option>
          <option value="anthropic">Anthropic</option>
          <option value="vllm">vLLM</option>
          <option value="ollama">Ollama</option>
          <option value="custom">Custom</option>
        </select>
      </div>
      <div class="field">
        <label>Config (JSON)</label>
        <textarea v-model="form.configJson" rows="6" required></textarea>
      </div>
      <button type="submit" class="btn-primary">{{ isEdit ? 'Update' : 'Create' }}</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { providerApi } from '../../api/providers'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = ref({ name: '', type: 'openai_compatible', configJson: '{}' })

onMounted(async () => {
  if (isEdit.value) {
    const res = await providerApi.get(route.params.id as string)
    form.value = { name: res.data.name, type: res.data.type, configJson: '{}' }
  }
})

async function save() {
  if (isEdit.value) {
    await providerApi.update(route.params.id as string, { name: form.value.name, configJson: form.value.configJson })
  } else {
    await providerApi.create(form.value)
  }
  router.push('/providers')
}
</script>

<style scoped>
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field select, .field textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
</style>
