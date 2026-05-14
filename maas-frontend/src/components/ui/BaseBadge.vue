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
  color: #065f46;
}
.base-badge--danger {
  background: var(--color-danger-light);
  color: #991b1b;
}
.base-badge--warning {
  background: var(--color-warning-light);
  color: #92400e;
}
.base-badge--info {
  background: var(--color-info-light);
  color: #3730a3;
}
.base-badge--neutral {
  background: var(--color-bg-muted);
  color: var(--color-foreground-secondary);
}
.base-badge--primary {
  background: var(--color-primary-light);
  color: #3730a3;
}
</style>
