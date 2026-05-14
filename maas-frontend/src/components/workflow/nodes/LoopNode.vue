<template>
  <div class="node loop">
    <div class="node-header">
      <span class="node-icon">&#x21bb;</span>
      <span class="node-label">{{ data.label || 'Loop' }}</span>
    </div>
    <div class="node-body">
      <div v-if="data.iterations" class="node-field"> x{{ data.iterations }}</div>
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
    iterations?: number
  }
}>()
</script>

<style scoped>
.node { min-width: 160px; background: var(--color-bg-card); border: 2px solid var(--color-success); border-radius: var(--radius-md); font-size: 12px; overflow: hidden; }
.node-header { background: var(--color-success); color: var(--color-bg-card); padding: 6px 10px; display: flex; align-items: center; gap: 6px; }
.node-icon { font-weight: bold; font-size: 14px; background: rgba(255,255,255,0.2); padding: 2px 6px; border-radius: 3px; }
.node-label { font-weight: 600; }
.node-body { padding: 8px 10px; }
.node-field { color: var(--color-foreground-secondary); font-size: 11px; margin-top: 2px; }
</style>
