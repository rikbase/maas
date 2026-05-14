<template>
  <div>
    <div v-if="loading" class="loading">{{ $t('common.loading') }}</div>
    <template v-else-if="skill">
      <div class="header">
        <h1>{{ skill.name }} <span class="ver">v{{ skill.version }}</span></h1>
        <div>
          <router-link v-if="auth.isAdmin" :to="`/skills/${skill.id}/edit`" class="btn-primary">{{ $t('provider.edit') }}</router-link>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="label">{{ $t('registry.skills.type') }}</span>
          <span class="value">{{ skill.type || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">{{ $t('registry.skills.status') }}</span>
          <span class="value"><span :class="'badge badge-' + skill.status">{{ $t('registry.skills.statuses.' + skill.status) }}</span></span>
        </div>
        <div class="info-item" v-if="skill.description">
          <span class="label">{{ $t('registry.skills.description') }}</span>
          <span class="value">{{ skill.description }}</span>
        </div>
        <div class="info-item" v-if="skill.publishNote">
          <span class="label">{{ $t('registry.skills.publishNote') }}</span>
          <span class="value">{{ skill.publishNote }}</span>
        </div>
      </div>

      <div class="section">
        <h2>{{ $t('registry.tools.title') }}</h2>
        <div v-if="tools.length === 0" class="empty">{{ $t('registry.tools.empty') }}</div>
        <table v-else class="table">
          <thead><tr>
            <th>{{ $t('registry.tools.name') }}</th>
            <th>{{ $t('registry.tools.source') }}</th>
            <th>{{ $t('registry.tools.status') }}</th>
          </tr></thead>
          <tbody>
            <tr v-for="t in tools" :key="t.id">
              <td>{{ t.name }}</td>
              <td><span class="badge badge-source">{{ t.source }}</span></td>
              <td><span class="badge" :class="t.enabled ? 'enabled' : 'disabled'">{{ t.enabled ? 'Enabled' : 'Disabled' }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { skillApi, toolApi } from '../../api/registry'
import type { Skill, ToolDef } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../../stores/auth'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const loading = ref(true)
const skill = ref<Skill | null>(null)
const tools = ref<ToolDef[]>([])

onMounted(async () => {
  try {
    const [sRes, tRes] = await Promise.all([
      skillApi.get(route.params.id as string),
      toolApi.listBySkill(route.params.id as string),
    ])
    skill.value = sRes.data
    tools.value = tRes.data
  } catch {
    show(t('common.error'), 'error')
    router.push('/skills')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading { color: #666; padding: 20px; text-align: center; }
.empty { text-align: center; padding: 20px; color: #666; font-size: 13px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.ver { font-size: 14px; color: #999; font-weight: 400; }
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-bottom: 24px; }
.info-item { background: white; padding: 12px 16px; border-radius: 8px; }
.label { display: block; font-size: 11px; color: #888; text-transform: uppercase; margin-bottom: 4px; }
.value { font-size: 14px; }
.section { margin-top: 16px; }
.section h2 { font-size: 16px; margin-bottom: 12px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge-draft { background: #eee; color: #666; }
.badge-published { background: #e8f5e9; color: #2e7d32; }
.badge-deprecated { background: #fff3e0; color: #e65100; }
.badge-retired { background: #ffebee; color: #c62828; }
.badge-source { background: #e8eaf6; color: #283593; }
.badge.enabled { background: #e8f5e9; color: #2e7d32; }
.badge.disabled { background: #eee; color: #666; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
</style>
