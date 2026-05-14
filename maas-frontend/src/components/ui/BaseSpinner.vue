<template>
  <span :class="['base-spinner', `base-spinner--${size}`]" />
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg'
}>(), {
  size: 'md',
})
</script>

<style scoped>
.base-spinner {
  display: inline-block;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
.base-spinner--sm { width: 14px; height: 14px; border-width: 2px; }
.base-spinner--md { width: 24px; height: 24px; }
.base-spinner--lg { width: 36px; height: 36px; border-width: 4px; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
