<template>
  <span :class="['base-badge', `base-badge--${variant}`]"><slot /></span>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'success' | 'danger' | 'warning' | 'info' | 'neutral' | 'primary'
}>(), {
  variant: 'neutral',
})
</script>

<style scoped>
.base-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 0.786rem;
  font-weight: 600;
  white-space: nowrap;
  line-height: 1.5;
}
.base-badge--success {
  background: var(--color-success-light);
  color: var(--color-success-dark);
}
.base-badge--danger {
  background: var(--color-danger-light);
  color: var(--color-danger-dark);
}
.base-badge--warning {
  background: var(--color-warning-light);
  color: var(--color-warning-dark);
}
.base-badge--info {
  background: var(--color-info-light);
  color: var(--color-info-dark);
}
.base-badge--neutral {
  background: var(--color-bg-muted);
  color: var(--color-foreground-secondary);
}
.base-badge--primary {
  background: var(--color-primary-light);
  color: var(--color-info-dark);
}
</style>
