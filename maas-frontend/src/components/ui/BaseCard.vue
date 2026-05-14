<template>
  <div :class="['base-card', { 'base-card--padding': !noPadding }]">
    <div v-if="$slots.header" class="base-card__header">
      <slot name="header" />
    </div>
    <div class="base-card__body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="base-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  noPadding?: boolean
}>(), {
  noPadding: false,
})
</script>

<style scoped>
.base-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-normal);
}
.base-card:hover {
  box-shadow: var(--shadow-md);
}
.base-card--padding { padding: var(--space-4); }
.base-card__header {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
}
.base-card__body {
  padding: var(--space-4);
}
.base-card:not(.base-card--padding) .base-card__body { padding: 0; }
.base-card--padding .base-card__header + .base-card__body { padding-top: var(--space-3); }
.base-card__footer {
  padding: var(--space-2) var(--space-4);
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-muted);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}
</style>
