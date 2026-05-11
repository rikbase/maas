<template>
  <div>
    <h1>Create API Key</h1>
    <form @submit.prevent="save" class="form">
      <div class="field">
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Type</label>
        <select v-model="form.keyType" required>
          <option value="application">Application</option>
          <option value="team">Team</option>
          <option value="root">Root</option>
        </select>
      </div>
      <div v-if="createdKey" class="created-key">
        <strong>Key created!</strong>
        <p class="warning">Copy this now — it won't be shown again.</p>
        <code>{{ createdKey }}</code>
      </div>
      <button v-if="!createdKey" type="submit" class="btn-primary">Create</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { keyApi } from '../../api/keys'

const form = ref({ name: '', keyType: 'application' as const })
const createdKey = ref<string | null>(null)

async function save() {
  const res = await keyApi.create({ ...form.value })
  createdKey.value = res.data.keyPrefix
}
</script>

<style scoped>
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
.created-key { background: #fff8e1; padding: 12px; border-radius: 4px; margin-bottom: 16px; }
.created-key code { word-break: break-all; font-size: 13px; }
.warning { color: #e65100; font-size: 13px; }
</style>
