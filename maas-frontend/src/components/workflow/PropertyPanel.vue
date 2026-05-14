<template>
  <div class="panel">
    <h3 class="panel-title">{{ $t('workflow.properties') }}</h3>

    <div v-if="!node" class="panel-empty">
      <p>{{ $t('workflow.selectNode') }}</p>
    </div>

    <div v-else class="panel-form">
      <div class="form-group">
        <label>{{ $t('workflow.nodeName') }}</label>
        <input v-model="editLabel" @input="emitUpdate" class="form-input" />
      </div>

      <template v-if="node.type === 'ai_call'">
        <div class="form-group">
          <label>{{ $t('workflow.model') }}</label>
          <input v-model="editData.model" @input="emitUpdate" class="form-input" placeholder="gpt-4" />
        </div>
        <div class="form-group">
          <label>{{ $t('workflow.provider') }}</label>
          <input v-model="editData.provider" @input="emitUpdate" class="form-input" placeholder="openai" />
        </div>
        <div class="form-group">
          <label>{{ $t('workflow.prompt') }}</label>
          <textarea v-model="editData.prompt" @input="emitUpdate" class="form-textarea" rows="4" placeholder="Enter prompt..." />
        </div>
      </template>

      <template v-if="node.type === 'tool'">
        <div class="form-group">
          <label>{{ $t('workflow.toolId') }}</label>
          <input v-model="editData.toolId" @input="emitUpdate" class="form-input" placeholder="Tool UUID" />
        </div>
      </template>

      <template v-if="node.type === 'skill'">
        <div class="form-group">
          <label>{{ $t('workflow.skillId') }}</label>
          <input v-model="editData.skillId" @input="emitUpdate" class="form-input" placeholder="Skill UUID" />
        </div>
      </template>

      <template v-if="node.type === 'condition'">
        <div class="form-group">
          <label>{{ $t('workflow.expression') }}</label>
          <textarea v-model="editData.expression" @input="emitUpdate" class="form-textarea" rows="3" placeholder="{{node1.output.score}} > 0.8" />
        </div>
      </template>

      <template v-if="node.type === 'loop'">
        <div class="form-group">
          <label>{{ $t('workflow.iterations') }}</label>
          <input v-model.number="editData.iterations" @input="emitUpdate" class="form-input" type="number" min="1" />
        </div>
      </template>

      <button @click="deleteNode" class="btn-danger">{{ $t('provider.delete') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useConfirm } from '../../composables/useConfirm'

export interface EditorNode {
  id: string
  type: string
  data: Record<string, any>
}

const props = defineProps<{
  node: EditorNode | null
}>()

const emit = defineEmits<{
  update: [nodeId: string, data: Record<string, any>]
  delete: [nodeId: string]
}>()

const editLabel = ref('')
const editData = ref<Record<string, any>>({})
const { confirm: confirmDialog } = useConfirm()

watch(() => props.node, (n) => {
  if (n) {
    editLabel.value = n.data?.label || ''
    editData.value = { ...(n.data || {}) }
    delete editData.value.label
  }
}, { immediate: true })

function emitUpdate() {
  if (!props.node) return
  const merged = { ...editData.value, label: editLabel.value }
  emit('update', props.node.id, merged)
}

async function deleteNode() {
  if (props.node && (await confirmDialog('Delete this step?'))) {
    emit('delete', props.node.id)
  }
}
</script>

<style scoped>
.panel { background: var(--color-bg-card); border-radius: var(--radius-md); padding: 12px; width: 240px; border: 1px solid var(--color-border); font-size: 13px; }
.panel-title { font-size: 13px; margin: 0 0 12px; color: var(--color-foreground); }
.panel-empty { color: var(--color-foreground-secondary); text-align: center; padding: 20px; font-size: 12px; }
.panel-form { display: flex; flex-direction: column; gap: 10px; }
.form-group { display: flex; flex-direction: column; gap: 4px; }
.form-group label { font-size: 11px; color: var(--color-foreground-secondary); font-weight: 500; }
.form-input { padding: 6px 8px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: 13px; }
.form-textarea { padding: 6px 8px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: 13px; font-family: var(--font-mono); resize: vertical; }
.btn-danger { padding: 6px 12px; background: var(--color-bg-muted); color: var(--color-danger); border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: 13px; margin-top: 8px; }
.btn-danger:hover { background: var(--color-bg-muted); }
</style>
