<template>
  <div>
    <BaseSpinner v-if="loading" class="spinner" />

    <BaseEmpty v-else-if="!tool" :text="$t('registry.tools.notFound')" />

    <template v-else>
      <router-link to="/tools" class="back-link">&larr; {{ $t('workflow.back') }}</router-link>

      <BasePageHeader :title="tool.name">
        <template #actions>
          <BaseButton variant="primary" size="sm" @click="router.push(`/tools/${tool.id}/edit`)">{{ $t('provider.edit') }}</BaseButton>
          <BaseButton size="sm" :variant="tool.enabled ? 'ghost' : 'ghost'" @click="toggleTool">
            {{ tool.enabled ? $t('security.rules.disable') : $t('security.rules.enable') }}
          </BaseButton>
          <BaseButton size="sm" variant="danger" @click="deleteTool">{{ $t('provider.delete') }}</BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard noPadding class="info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-item-label">ID</span>
            <span class="info-item-value text-mono">{{ tool.id }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('registry.tools.name') }}</span>
            <span class="info-item-value">{{ tool.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('provider.description') }}</span>
            <span class="info-item-value">{{ tool.description || '&mdash;' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('registry.tools.source') }}</span>
            <span class="info-item-value"><BaseBadge variant="info">{{ tool.source }}</BaseBadge></span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('registry.tools.sourceRef') }}</span>
            <span class="info-item-value text-mono">{{ tool.sourceRef || '&mdash;' }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('registry.tools.status') }}</span>
            <span class="info-item-value">
              <BaseBadge :variant="tool.enabled ? 'success' : 'neutral'">
                {{ tool.enabled ? $t('registry.tools.enabled') : $t('registry.tools.disabled') }}
              </BaseBadge>
            </span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.createdAt') }}</span>
            <span class="info-item-value">{{ new Date(tool.createdAt).toLocaleString() }}</span>
          </div>
          <div class="info-item">
            <span class="info-item-label">{{ $t('workflow.updatedAt') }}</span>
            <span class="info-item-value">{{ new Date(tool.updatedAt).toLocaleString() }}</span>
          </div>
        </div>
      </BaseCard>

      <BaseCard>
        <template #header>
          <h2 class="card-section-title">{{ $t('registry.tools.inputSchema') }}</h2>
        </template>
        <pre class="json-block">{{ formatJson(tool.inputSchema) }}</pre>
      </BaseCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toolApi } from '../../api/registry'
import type { ToolDef } from '../../api/registry'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const tool = ref<ToolDef | null>(null)

onMounted(async () => {
  try {
    const res = await toolApi.get(route.params.id as string)
    tool.value = res.data
  } catch {
    show(t('common.error'), 'error')
  } finally {
    loading.value = false
  }
})

function formatJson(json: string) {
  try { return JSON.stringify(JSON.parse(json), null, 2) } catch { return json }
}

async function toggleTool() {
  if (!tool.value) return
  try {
    const res = await toolApi.toggle(tool.value.id)
    tool.value = res.data
    show(res.data.enabled ? t('registry.tools.enabledMsg') : t('registry.tools.disabledMsg'))
  } catch {
    show(t('common.error'), 'error')
  }
}

async function deleteTool() {
  if (!tool.value || !(await confirmDialog(t('registry.tools.deleteConfirm')))) return
  try {
    await toolApi.delete(tool.value.id)
    show(t('registry.tools.deleted'))
    router.push('/tools')
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}
.back-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.857rem;
  display: inline-block;
  margin-bottom: var(--space-1);
}
.back-link:hover {
  text-decoration: underline;
}
.info-card {
  margin-bottom: var(--space-5);
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  padding: var(--space-5);
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.info-item-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: var(--color-foreground-secondary);
  letter-spacing: 0.5px;
}
.info-item-value {
  font-size: 0.875rem;
  color: var(--color-foreground);
}
.text-mono {
  font-family: var(--font-mono);
  font-size: 0.75rem;
}
.card-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-foreground);
  margin: 0;
}
.json-block {
  background: var(--color-bg-muted);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  font-family: var(--font-mono);
  font-size: 0.8rem;
  overflow-x: auto;
  margin: 0;
}
</style>
