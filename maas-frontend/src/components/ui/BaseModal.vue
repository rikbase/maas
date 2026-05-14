<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="onCancel">
        <div class="modal-content" :style="{ maxWidth: maxWidth + 'px' }">
          <div class="modal-header">
            <h3>{{ title }}</h3>
            <button class="modal-close" @click="onCancel">&times;</button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
          <div v-else class="modal-footer">
            <BaseButton variant="ghost" @click="onCancel">{{ cancelText }}</BaseButton>
            <BaseButton v-if="!hideConfirm" :variant="confirmVariant" :loading="confirmLoading" @click="onConfirm">
              {{ confirmText }}
            </BaseButton>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import BaseButton from './BaseButton.vue'

withDefaults(defineProps<{
  visible: boolean
  title?: string
  maxWidth?: number
  cancelText?: string
  confirmText?: string
  confirmVariant?: 'primary' | 'danger'
  confirmLoading?: boolean
  hideConfirm?: boolean
}>(), {
  title: 'Confirm',
  maxWidth: 480,
  cancelText: 'Cancel',
  confirmText: 'Confirm',
  confirmVariant: 'primary',
  confirmLoading: false,
  hideConfirm: false,
})

const emit = defineEmits<{
  confirm: []
  cancel: []
  'update:visible': [v: boolean]
}>()

function onConfirm() { emit('confirm') }
function onCancel() {
  emit('cancel')
  emit('update:visible', false)
}
</script>

<style scoped>
.modal-enter-active, .modal-leave-active {
  transition: all var(--transition-normal);
}
.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: all var(--transition-normal);
}
.modal-enter-from { opacity: 0; }
.modal-leave-to { opacity: 0; }
.modal-enter-from .modal-content { transform: scale(0.95) translateY(10px); }
.modal-leave-to .modal-content { transform: scale(0.95) translateY(10px); }

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
}
.modal-content {
  width: 90%;
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border);
}
.modal-header h3 {
  font-size: 1.071rem;
  font-weight: 700;
  color: var(--color-foreground);
}
.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-muted);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}
.modal-close:hover { color: var(--color-foreground); }
.modal-body {
  padding: var(--space-6);
  color: var(--color-foreground-secondary);
  line-height: 1.6;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-border);
}
</style>
