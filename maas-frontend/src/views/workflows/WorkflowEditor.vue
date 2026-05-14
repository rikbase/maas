<template>
  <div class="editor">
    <div class="editor-toolbar">
      <h1>{{ workflowName || $t('workflow.editor') }}</h1>
      <div class="toolbar-actions">
        <button @click="saveDraft" class="btn-primary" :disabled="saving">{{ $t('workflow.save') }}</button>
        <button @click="publishWorkflow" class="btn-ok" :disabled="saving">{{ $t('workflow.publish') }}</button>
        <router-link :to="`/workflows/${workflowId}`" class="btn-sm">{{ $t('common.cancel') }}</router-link>
      </div>
    </div>

    <div class="editor-body">
      <StepPalette />

      <div class="canvas-wrapper" ref="canvasWrapper">
        <VueFlow
          v-model:nodes="flowNodes"
          v-model:edges="flowEdges"
          :node-types="nodeTypes"
          :default-viewport="{ x: 100, y: 100, zoom: 1 }"
          fit-view-on-init
          @node-click="onNodeClick"
          @pane-click="onPaneClick"
          @connect="onConnect"
          @drop.prevent="onDrop"
          @dragover.prevent
        >
          <Controls />
          <MiniMap />
        </VueFlow>
      </div>

      <PropertyPanel
        :node="selectedNode"
        @update="onNodeUpdate"
        @delete="onNodeDelete"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import StepPalette from '../../components/workflow/StepPalette.vue'
import PropertyPanel from '../../components/workflow/PropertyPanel.vue'
import type { EditorNode } from '../../components/workflow/PropertyPanel.vue'
import AiCallNode from '../../components/workflow/nodes/AiCallNode.vue'
import ToolNode from '../../components/workflow/nodes/ToolNode.vue'
import ConditionNode from '../../components/workflow/nodes/ConditionNode.vue'
import LoopNode from '../../components/workflow/nodes/LoopNode.vue'
import { workflowApi } from '../../api/workflows'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()

const nodeTypes: Record<string, any> = {
  ai_call: AiCallNode,
  tool: ToolNode,
  condition: ConditionNode,
  loop: LoopNode,
  skill: ToolNode,
}

const { screenToFlowCoordinate } = useVueFlow()

const workflowId = computed(() => route.params.id as string || 'new')
const workflowName = ref('')
const flowNodes = ref<any[]>([])
const flowEdges = ref<any[]>([])
const selectedNode = ref<EditorNode | null>(null)
const saving = ref(false)
const canvasWrapper = ref<HTMLElement | null>(null)

onMounted(async () => {
  const wid = workflowId.value
  if (wid && wid !== 'new') {
    try {
      const res = await workflowApi.get(wid)
      workflowName.value = res.data.name
      const verRes = await workflowApi.getVersions(wid)
      const latestVer = verRes.data[0]
      if (latestVer?.definitionJson) {
        loadDefinition(latestVer.definitionJson)
      }
    } catch {
      show(t('common.error'), 'error')
    }
  }
})

function loadDefinition(json: string) {
  try {
    const def = JSON.parse(json)
    const nodes: any[] = def.nodes || []
    flowNodes.value = nodes.map((n: any, i: number) => ({
      id: n.id || `node_${i}`,
      type: n.type === 'start' || n.type === 'end' ? 'default' : n.type,
      position: n.position || { x: 100 + i * 200, y: 200 },
      data: { ...(n.data || n.config || {}), label: n.label || n.type },
    }))
    const edges: any[] = def.edges || []
    flowEdges.value = edges.map((e: any, i: number) => ({
      id: `edge_${i}`,
      source: e.from,
      target: e.to,
      sourceHandle: e.sourceHandle || null,
    }))
  } catch {
    // empty canvas
  }
}

function serializeDefinition(): string {
  const nodes: any[] = flowNodes.value.map(n => ({
    id: n.id,
    type: n.type || 'ai_call',
    label: n.data?.label || n.type,
    position: n.position,
    data: n.data || {},
  }))
  const edges: any[] = flowEdges.value.map(e => ({
    from: e.source,
    to: e.target,
    sourceHandle: e.sourceHandle || null,
  }))
  return JSON.stringify({ nodes, edges })
}

async function saveDraft() {
  saving.value = true
  try {
    const definitionJson = serializeDefinition()
    if (workflowId.value && workflowId.value !== 'new') {
      await workflowApi.update(workflowId.value, { definitionJson })
      show(t('workflow.saved'))
    } else {
      const res = await workflowApi.create({
        name: workflowName.value || 'Untitled Workflow',
        definitionJson,
      })
      router.replace(`/workflows/${res.data.id}/edit`)
      workflowName.value = res.data.name
      show(t('workflow.saved'))
    }
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}

async function publishWorkflow() {
  let wid = workflowId.value
  if (!wid || wid === 'new') {
    await saveDraft()
    wid = route.params.id as string
    if (!wid || wid === 'new') return
  }
  saving.value = true
  try {
    await workflowApi.publish(wid)
    show(t('workflow.published_msg'))
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}

function onDrop(event: DragEvent) {
  const type = event.dataTransfer?.getData('application/workflow-node')
  if (!type) return

  const position = screenToFlowCoordinate({
    x: event.clientX,
    y: event.clientY,
  })

  const id = `node_${Date.now()}`
  flowNodes.value.push({
    id,
    type,
    position,
    data: { label: type },
  })
}

function onNodeClick({ node }: { node: any }) {
  selectedNode.value = {
    id: node.id,
    type: node.type || 'ai_call',
    data: { ...(node.data || {}) },
  }
}

function onPaneClick() {
  selectedNode.value = null
}

function onConnect(connection: { source?: string; target?: string; sourceHandle?: string | null }) {
  if (!connection.source || !connection.target) return
  const exists = flowEdges.value.some(
    e => e.source === connection.source && e.target === connection.target
  )
  if (!exists) {
    flowEdges.value.push({
      id: `edge_${Date.now()}`,
      source: connection.source,
      target: connection.target,
      sourceHandle: connection.sourceHandle || null,
    })
  }
}

function onNodeUpdate(nodeId: string, data: Record<string, any>) {
  const node = flowNodes.value.find(n => n.id === nodeId)
  if (node) {
    node.data = { ...data }
  }
}

function onNodeDelete(nodeId: string) {
  flowNodes.value = flowNodes.value.filter(n => n.id !== nodeId)
  flowEdges.value = flowEdges.value.filter(e => e.source !== nodeId && e.target !== nodeId)
  selectedNode.value = null
}
</script>

<style scoped>
.editor { display: flex; flex-direction: column; height: calc(100vh - 80px); }
.editor-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.editor-toolbar h1 { font-size: 18px; margin: 0; }
.toolbar-actions { display: flex; gap: 8px; }
.editor-body { display: flex; gap: 12px; flex: 1; min-height: 0; }
.canvas-wrapper { flex: 1; border-radius: 8px; overflow: hidden; background: #f8f9fa; border: 1px solid #e0e0e0; position: relative; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-ok { padding: 8px 16px; background: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-ok:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-sm { padding: 6px 12px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 13px; }
</style>
