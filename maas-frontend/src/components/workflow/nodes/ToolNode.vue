<template>
  <div class="node tool">
    <div class="node-header">
      <span class="node-icon">T</span>
      <span class="node-label">{{ data.label || 'Tool' }}</span>
    </div>
    <div class="node-body">
      <div v-if="data.toolId" class="node-field">Tool: {{ data.toolId }}</div>
    </div>
    <Handle type="target" :position="Position.Left" />
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  id: string
  data: {
    label?: string
    toolId?: string
  }
}>()
</script>

<style scoped>
.node { min-width: 160px; background: white; border: 2px solid #7b1fa2; border-radius: 8px; font-size: 12px; overflow: hidden; }
.node-header { background: #7b1fa2; color: white; padding: 6px 10px; display: flex; align-items: center; gap: 6px; }
.node-icon { font-weight: bold; font-size: 11px; background: rgba(255,255,255,0.2); padding: 2px 6px; border-radius: 3px; }
.node-label { font-weight: 600; }
.node-body { padding: 8px 10px; }
.node-field { color: #666; font-size: 11px; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
