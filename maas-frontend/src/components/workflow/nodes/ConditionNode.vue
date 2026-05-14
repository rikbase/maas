<template>
  <div class="node condition">
    <div class="node-header">
      <span class="node-icon">?</span>
      <span class="node-label">{{ data.label || 'Condition' }}</span>
    </div>
    <div class="node-body">
      <div v-if="data.expression" class="node-field">{{ data.expression }}</div>
    </div>
    <Handle type="target" :position="Position.Left" />
    <Handle type="source" :position="Position.Right" />
    <Handle type="source" :position="Position.Bottom" id="false" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  id: string
  data: {
    label?: string
    expression?: string
  }
}>()
</script>

<style scoped>
.node { min-width: 160px; background: var(--color-bg-card); border: 2px solid #e65100; border-radius: var(--radius-md); font-size: 12px; overflow: hidden; }
.node-header { background: #e65100; color: var(--color-bg-card); padding: 6px 10px; display: flex; align-items: center; gap: 6px; }
.node-icon { font-weight: bold; font-size: 11px; background: rgba(255,255,255,0.2); padding: 2px 6px; border-radius: 3px; }
.node-label { font-weight: 600; }
.node-body { padding: 8px 10px; }
.node-field { color: var(--color-foreground-secondary); font-size: 11px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
