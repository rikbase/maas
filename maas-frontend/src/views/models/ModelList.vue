<template>
  <div>
    <h1>Models</h1>
    <p class="hint">Models are auto-synced from enabled providers.</p>
    <table class="table">
      <thead><tr><th>Model Name</th><th>Provider</th><th>Status</th></tr></thead>
      <tbody>
        <tr v-for="m in models" :key="m.id">
          <td>{{ m.name }}</td>
          <td>{{ m.providerName }}</td>
          <td><span :class="'badge ' + m.status">{{ m.status }}</span></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../../api/providers'

const models = ref<Array<{ id: string; name: string; providerName: string; status: string }>>([])

onMounted(async () => {
  const res = await providerApi.list()
  models.value = res.data.flatMap(p => [{ id: p.id, name: p.name, providerName: p.name, status: 'active' }])
})
</script>

<style scoped>
.hint { color: #666; font-size: 14px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.active { background: #e8f5e9; color: #2e7d32; }
</style>
