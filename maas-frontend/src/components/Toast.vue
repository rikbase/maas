<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div v-for="t in toasts" :key="t.id" :class="'toast toast-' + t.type">
        {{ t.text }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import type { ToastMessage } from '../composables/useToast'

defineProps<{ toasts: ToastMessage[] }>()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.toast {
  padding: 12px 20px;
  border-radius: 6px;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  color: white;
  min-width: 200px;
}
.toast-success { background: #2e7d32; }
.toast-error { background: #c62828; }

.toast-enter-active, .toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from { opacity: 0; transform: translateX(30px); }
.toast-leave-to { opacity: 0; transform: translateX(30px); }
</style>
