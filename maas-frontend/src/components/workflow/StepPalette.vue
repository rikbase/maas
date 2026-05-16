<template>
  <div class="palette">
    <h3 class="palette-title">{{ $t('workflow.stepTypes') }}</h3>
    <div
      v-for="step in stepTypes"
      :key="step.type"
      class="palette-item"
      :class="'palette-' + step.type"
      draggable="true"
      @dragstart="onDragStart($event, step)"
    >
      <span class="palette-icon">{{ step.icon }}</span>
      <span class="palette-label">{{ step.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const stepTypes = [
  { type: 'ai_call', icon: 'AI', label: t('workflow.stepType.aiCall'), color: '#1976d2' },
  { type: 'tool', icon: 'T', label: t('workflow.stepType.tool'), color: '#7b1fa2' },
  { type: 'skill', icon: 'S', label: t('workflow.stepType.skill'), color: '#00796b' },
  { type: 'condition', icon: '?', label: t('workflow.stepType.condition'), color: '#e65100' },
  { type: 'loop', icon: '↻', label: t('workflow.stepType.loop'), color: '#2e7d32' },
]

function onDragStart(event: DragEvent, step: { type: string }) {
  event.dataTransfer?.setData('application/workflow-node', step.type)
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy'
  }
}
</script>

<style scoped>
.palette { background: var(--color-bg-card); border-radius: var(--radius-md); padding: 12px; width: 180px; border: 1px solid var(--color-border); }
.palette-title { font-size: 13px; margin: 0 0 12px; color: var(--color-foreground); }
.palette-item { padding: 8px 10px; margin-bottom: 6px; border-radius: 6px; cursor: grab; display: flex; align-items: center; gap: 8px; font-size: 13px; transition: opacity 0.15s; user-select: none; }
.palette-item:hover { opacity: 0.85; }
.palette-item:active { cursor: grabbing; }
.palette-icon { font-weight: bold; font-size: 11px; background: rgba(0,0,0,0.1); padding: 2px 6px; border-radius: 3px; width: 22px; text-align: center; }
.palette-ai_call { background: rgba(33,150,243,0.2); color: #90caf9; border: 1px solid rgba(33,150,243,0.3); }
.palette-tool { background: rgba(156,39,176,0.2); color: #ce93d8; border: 1px solid rgba(156,39,176,0.3); }
.palette-skill { background: rgba(0,150,136,0.2); color: #80cbc4; border: 1px solid rgba(0,150,136,0.3); }
.palette-condition { background: rgba(255,152,0,0.2); color: #ffcc80; border: 1px solid rgba(255,152,0,0.3); }
.palette-loop { background: rgba(76,175,80,0.2); color: #a5d6a7; border: 1px solid rgba(76,175,80,0.3); }
</style>
