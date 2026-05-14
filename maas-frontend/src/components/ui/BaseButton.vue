<template>
  <button
    :class="['base-btn', `base-btn--${variant}`, `base-btn--${size}`, { 'base-btn--loading': loading }]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="base-btn__spinner" />
    <slot />
  </button>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'primary' | 'danger' | 'ghost' | 'secondary'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
}>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
})

defineEmits<{ click: [e: MouseEvent] }>()
</script>

<style scoped>
.base-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  user-select: none;
}
.base-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.base-btn--sm { padding: 4px 10px; font-size: 0.857rem; }
.base-btn--md { padding: 7px 16px; font-size: 0.929rem; }
.base-btn--lg { padding: 10px 22px; font-size: 1rem; }

.base-btn--primary {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.base-btn--primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.base-btn--danger {
  background: var(--color-danger);
  color: #fff;
  border-color: var(--color-danger);
}
.base-btn--danger:hover:not(:disabled) {
  background: #dc2626;
  border-color: #dc2626;
}

.base-btn--secondary {
  background: var(--color-bg-card);
  color: var(--color-foreground);
  border-color: var(--color-border);
}
.base-btn--secondary:hover:not(:disabled) {
  background: var(--color-bg-muted);
}

.base-btn--ghost {
  background: transparent;
  color: var(--color-foreground-secondary);
  border-color: transparent;
}
.base-btn--ghost:hover:not(:disabled) {
  background: var(--color-bg-muted);
  color: var(--color-foreground);
}

.base-btn--loading { position: relative; }
.base-btn__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: btn-spin 0.6s linear infinite;
}
.base-btn--ghost .base-btn__spinner,
.base-btn--secondary .base-btn__spinner {
  border-color: var(--color-border);
  border-top-color: var(--color-primary);
}
@keyframes btn-spin { to { transform: rotate(360deg); } }
</style>
