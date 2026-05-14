<template>
  <div class="base-empty">
    <div class="base-empty__icon" v-html="icon" />
    <p class="base-empty__text">{{ text }}</p>
    <BaseButton v-if="actionText" size="sm" @click="$emit('action')">{{ actionText }}</BaseButton>
  </div>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'

withDefaults(defineProps<{
  text?: string
  actionText?: string
  icon?: string
}>(), {
  text: 'No data',
  icon: '<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"/><polyline points="13 2 13 9 20 9"/></svg>',
})

defineEmits<{ action: [] }>()
</script>

<style scoped>
.base-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}
.base-empty__icon {
  color: var(--color-border);
  margin-bottom: var(--space-4);
}
.base-empty__text {
  color: var(--color-foreground-secondary);
  font-size: 0.929rem;
  margin-bottom: var(--space-4);
}
</style>
