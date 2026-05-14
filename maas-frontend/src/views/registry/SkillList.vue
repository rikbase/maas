<template>
  <div>
    <div class="header">
      <h1>{{ $t('registry.skills.title') }}</h1>
      <router-link v-if="auth.isAdmin" to="/skills/new" class="btn-primary">{{ $t('registry.skills.add') }}</router-link>
    </div>

    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <div v-else-if="skills.length === 0" class="empty">
      <p>{{ $t('registry.skills.empty') }}</p>
      <p class="hint">{{ $t('registry.skills.emptyHint') }}</p>
    </div>
    <table v-else class="table">
      <thead><tr>
        <th>{{ $t('registry.skills.name') }}</th>
        <th>{{ $t('registry.skills.type') }}</th>
        <th>{{ $t('registry.skills.version') }}</th>
        <th>{{ $t('registry.skills.status') }}</th>
        <th v-if="auth.isAdmin">{{ $t('registry.skills.actions') }}</th>
      </tr></thead>
      <tbody>
        <tr v-for="s in skills" :key="s.id">
          <td>
            <router-link :to="`/skills/${s.id}`" class="link">{{ s.name }}</router-link>
            <span v-if="s.description" class="desc">{{ s.description }}</span>
          </td>
          <td>{{ s.type || '-' }}</td>
          <td>v{{ s.version }}</td>
          <td><span :class="'badge badge-' + s.status">{{ $t('registry.skills.statuses.' + s.status) }}</span></td>
          <td v-if="auth.isAdmin">
            <router-link :to="`/skills/${s.id}/edit`" class="btn-sm">{{ $t('provider.edit') }}</router-link>
            <button v-if="s.status !== 'published'" @click="publish(s)" class="btn-sm btn-ok">{{ $t('registry.skills.publish') }}</button>
            <button @click="deleteSkill(s.id)" class="btn-sm btn-danger">{{ $t('provider.delete') }}</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { skillApi } from '../../api/registry'
import type { Skill } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const auth = useAuthStore()

const loading = ref(true)
const skills = ref<Skill[]>([])

onMounted(async () => {
  try {
    const res = await skillApi.list()
    skills.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

async function publish(s: Skill) {
  try {
    const res = await skillApi.publish(s.id)
    Object.assign(s, res.data)
    show(t('registry.skills.published'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteSkill(id: string) {
  if (!confirm(t('registry.skills.deleteConfirm'))) return
  try {
    await skillApi.delete(id)
    skills.value = skills.value.filter(s => s.id !== id)
    show(t('registry.skills.deleted'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 40px; color: #666; }
.hint { font-size: 13px; color: #999; margin-top: 8px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.link { color: #1976d2; text-decoration: none; font-weight: 500; }
.link:hover { text-decoration: underline; }
.desc { display: block; font-size: 12px; color: #999; margin-top: 2px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge-draft { background: #eee; color: #666; }
.badge-published { background: #e8f5e9; color: #2e7d32; }
.badge-deprecated { background: #fff3e0; color: #e65100; }
.badge-retired { background: #ffebee; color: #c62828; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; text-decoration: none; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
.btn-ok { background: #e8f5e9; color: #2e7d32; }
</style>
