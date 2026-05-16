<template>
  <div>
    <BaseSpinner v-if="loading" size="lg" />
    <template v-else-if="server">
      <BasePageHeader :title="server.name">
        <template #actions>
          <BaseButton variant="primary" size="sm" @click="$router.push(`/mcp/servers/${server.id}/edit`)">
            {{ $t('provider.edit') }}
          </BaseButton>
        </template>
      </BasePageHeader>

      <BaseCard>
        <template #header>{{ $t('mcp.servers.details') }}</template>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">{{ $t('mcp.servers.transport') }}</span>
            <span class="value"><BaseBadge variant="info">{{ server.transport }}</BaseBadge></span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('mcp.servers.status') }}</span>
            <span class="value">
              <BaseBadge :variant="statusVariant(server.status)">
                {{ $t('mcp.servers.statuses.' + server.status) }}
              </BaseBadge>
            </span>
          </div>
          <div class="info-item" v-if="server.command">
            <span class="label">{{ $t('mcp.servers.command') }}</span>
            <span class="value mono">{{ server.command }}</span>
          </div>
          <div class="info-item" v-if="server.url">
            <span class="label">{{ $t('mcp.servers.url') }}</span>
            <span class="value mono">{{ server.url }}</span>
          </div>
        </div>
      </BaseCard>

      <!-- Tools -->
      <BaseCard style="margin-top: var(--space-4);">
        <template #header>{{ $t('mcp.servers.tools') }} ({{ tools.length }})</template>
        <template v-if="tools.length === 0">
          <BaseEmpty :text="$t('mcp.servers.noTools')" />
        </template>
        <table v-else class="design-table">
          <thead>
            <tr>
              <th>{{ $t('mcp.servers.toolName') }}</th>
              <th>{{ $t('mcp.servers.description') }}</th>
              <th>{{ $t('mcp.servers.autoRegister') }}</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tools" :key="t.id">
              <td><strong>{{ t.name }}</strong></td>
              <td class="desc-cell">{{ t.description || '-' }}</td>
              <td>
                <BaseBadge :variant="t.autoRegister ? 'success' : 'neutral'">
                  {{ t.autoRegister ? 'Yes' : 'No' }}
                </BaseBadge>
              </td>
              <td>
                <BaseButton variant="danger" size="sm" @click="deleteTool(t.id)">
                  {{ $t('provider.delete') }}
                </BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>

      <!-- Runtime Tools -->
      <BaseCard style="margin-top: var(--space-4);">
        <template #header>
          <span>Runtime Tools</span>
          <BaseButton size="sm" variant="secondary" :loading="rtLoading" @click="fetchRuntimeTools" style="margin-left:auto">
            {{ rtLoading ? 'Loading...' : 'Refresh' }}
          </BaseButton>
        </template>
        <div v-if="rtError" class="rt-error">{{ rtError }}</div>
        <div v-else-if="runtimeTools.length === 0">
          <BaseEmpty text="No runtime tools. Click Refresh to connect." />
        </div>
        <div v-else class="rt-grid">
          <div v-for="tool in runtimeTools" :key="tool.name" class="rt-card">
            <div class="rt-card__header">
              <span class="rt-card__name">{{ tool.name }}</span>
              <BaseButton size="sm" variant="primary" @click="openExecutePanel(tool)">Execute</BaseButton>
            </div>
            <p class="rt-card__desc">{{ tool.description || 'No description' }}</p>
            <div v-if="tool.inputSchema" class="rt-card__schema">
              <code class="mono-sm">{{ formatSchema(tool.inputSchema) }}</code>
            </div>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- Execution Modal -->
    <div v-if="executingTool" class="modal-overlay" @click.self="closeExecutePanel">
      <div class="modal-card">
        <h3 class="modal-title">Execute: {{ executingTool.name }}</h3>
        <div class="modal-body">
          <div class="modal-field">
            <label class="field-label">Arguments (JSON)</label>
            <textarea v-model="execArgs" class="form-input exec-input" rows="6" placeholder='{"key": "value"}'></textarea>
          </div>
          <BaseButton variant="primary" :loading="execLoading" :disabled="execLoading" @click="runTool">
            {{ execLoading ? 'Executing...' : 'Run' }}
          </BaseButton>
          <div v-if="execResult !== null" class="exec-result">
            <span class="field-label">Result</span>
            <pre class="exec-pre">{{ JSON.stringify(execResult, null, 2) }}</pre>
          </div>
        </div>
        <button class="modal-close" @click="closeExecutePanel">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { mcpServerApi } from '../../api/mcp'
import type { McpServer, McpTool, McpToolDefinition } from '../../api/mcp'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '../../composables/useConfirm'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseEmpty from '../../components/ui/BaseEmpty.vue'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const { confirm: confirmDialog } = useConfirm()

const loading = ref(true)
const server = ref<McpServer | null>(null)
const tools = ref<McpTool[]>([])
const runtimeTools = ref<McpToolDefinition[]>([])
const rtLoading = ref(false)
const rtError = ref('')
const executingTool = ref<McpToolDefinition | null>(null)
const execArgs = ref('{}')
const execLoading = ref(false)
const execResult = ref<any>(null)

onMounted(async () => {
  try {
    const [sRes, tRes] = await Promise.all([
      mcpServerApi.get(route.params.id as string),
      mcpServerApi.listTools(route.params.id as string),
    ])
    server.value = sRes.data
    tools.value = tRes.data
  } catch {
    show(t('common.error'), 'error')
    router.push('/mcp/servers')
  } finally {
    loading.value = false
  }
})

async function fetchRuntimeTools() {
  rtLoading.value = true
  rtError.value = ''
  try {
    const res = await mcpServerApi.listRuntimeTools(route.params.id as string)
    runtimeTools.value = res.data
  } catch (e: any) {
    rtError.value = e.message || 'Failed to connect to MCP server'
  } finally {
    rtLoading.value = false
  }
}

function openExecutePanel(tool: McpToolDefinition) {
  executingTool.value = tool
  execArgs.value = tool.inputSchema ? JSON.stringify(sampleArgs(tool.inputSchema), null, 2) : '{}'
  execResult.value = null
}

function closeExecutePanel() {
  executingTool.value = null
  execArgs.value = '{}'
  execResult.value = null
}

async function runTool() {
  if (!executingTool.value) return
  execLoading.value = true
  try {
    const args = JSON.parse(execArgs.value)
    const res = await mcpServerApi.executeTool(route.params.id as string, executingTool.value.name, args)
    execResult.value = res.data
  } catch (e: any) {
    execResult.value = { error: e.message || 'Execution failed' }
  } finally {
    execLoading.value = false
  }
}

function sampleArgs(schema: Record<string, any>): Record<string, any> {
  if (!schema.properties) return {}
  const sample: Record<string, any> = {}
  for (const [key, prop] of Object.entries(schema.properties) as [string, any][]) {
    if (prop.type === 'string') sample[key] = prop.default || prop.enum?.[0] || '...'
    else if (prop.type === 'number' || prop.type === 'integer') sample[key] = prop.default || 0
    else if (prop.type === 'boolean') sample[key] = prop.default ?? false
    else if (prop.type === 'array') sample[key] = prop.default || []
    else if (prop.type === 'object') sample[key] = prop.default || {}
    else sample[key] = null
  }
  return sample
}

function formatSchema(schema: Record<string, any>): string {
  const props = schema.properties ? Object.keys(schema.properties).join(', ') : ''
  return `{ ${props} }`
}

function statusVariant(status: string): 'success' | 'danger' | 'warning' | 'info' | 'neutral' {
  const map: Record<string, 'success' | 'danger' | 'warning' | 'info' | 'neutral'> = { connected: 'success', online: 'success', error: 'danger', offline: 'neutral' }
  return map[status] || 'neutral'
}

async function deleteTool(toolId: string) {
  if (!(await confirmDialog('Delete this tool?'))) return
  try {
    await mcpServerApi.deleteTool(route.params.id as string, toolId)
    tools.value = tools.value.filter(t => t.id !== toolId)
    show('Tool deleted')
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.label {
  font-size: 0.786rem;
  color: var(--color-foreground-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}
.value {
  font-size: 0.929rem;
  color: var(--color-foreground);
}
.mono {
  font-family: var(--font-mono);
  font-size: 0.857rem;
  word-break: break-all;
}
.design-table {
  width: 100%;
  border-collapse: collapse;
}
.design-table th {
  font-size: 0.857rem;
  font-weight: 600;
  color: var(--color-foreground-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid var(--color-border);
  padding: 10px 12px;
  text-align: left;
}
.design-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
}
.design-table tr:hover td {
  background: var(--color-bg-muted);
}
.desc-cell {
  color: var(--color-foreground-secondary);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Runtime Tools */
.rt-error {
  color: var(--color-danger);
  font-size: 0.857rem;
  padding: var(--space-3);
  background: var(--color-danger-light);
  border-radius: var(--radius-md);
}
.rt-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-3);
}
.rt-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-3);
}
.rt-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}
.rt-card__name {
  font-weight: 700;
  font-size: 0.929rem;
  color: var(--color-foreground);
}
.rt-card__desc {
  font-size: 0.786rem;
  color: var(--color-foreground-secondary);
  margin: 0 0 var(--space-2);
}
.rt-card__schema {
  background: var(--color-bg-muted);
  padding: var(--space-2);
  border-radius: var(--radius-sm);
}
.mono-sm {
  font-family: var(--font-mono);
  font-size: 0.714rem;
  color: var(--color-foreground-secondary);
  word-break: break-all;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  width: 560px;
  max-width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  box-shadow: var(--shadow-xl);
}
.modal-title {
  margin: 0 0 var(--space-4);
  font-size: 1.143rem;
  font-weight: 700;
}
.modal-close {
  position: absolute;
  top: var(--space-3);
  right: var(--space-4);
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-foreground-secondary);
  cursor: pointer;
  line-height: 1;
}
.modal-close:hover { color: var(--color-foreground); }
.modal-field { margin-bottom: var(--space-3); }
.field-label {
  display: block;
  font-size: 0.786rem;
  font-weight: 600;
  color: var(--color-foreground-secondary);
  margin-bottom: var(--space-1);
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-family: var(--font-mono);
  font-size: 0.857rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
  box-sizing: border-box;
  resize: vertical;
}
.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
}
.exec-result {
  margin-top: var(--space-3);
}
.exec-pre {
  background: #1e293b;
  color: #e2e8f0;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  font-size: 0.786rem;
  overflow-x: auto;
  max-height: 240px;
}
</style>
