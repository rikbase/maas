<template>
  <div class="page-header">
    <div class="page-header__left">
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="description" class="page-header__desc">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
}
.page-header__title {
  font-size: 1.286rem;
  font-weight: 800;
  color: var(--color-foreground);
  line-height: 1.3;
}
.page-header__desc {
  margin-top: var(--space-1);
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
}
.page-header__actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}
</style>
