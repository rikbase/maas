<template>
  <div class="base-form-field">
    <label v-if="label" :for="inputId" class="base-form-field__label">
      {{ label }}
      <span v-if="required" class="base-form-field__required">*</span>
    </label>
    <div class="base-form-field__control">
      <slot />
    </div>
    <p v-if="error" class="base-form-field__error">{{ error }}</p>
    <p v-else-if="help" class="base-form-field__help">{{ help }}</p>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  label?: string
  required?: boolean
  error?: string
  help?: string
  inputId?: string
}>(), {
  required: false,
})
</script>

<style scoped>
.base-form-field {
  margin-bottom: var(--space-4);
}
.base-form-field__label {
  display: block;
  margin-bottom: var(--space-2);
  font-size: 0.929rem;
  font-weight: 600;
  color: var(--color-foreground);
}
.base-form-field__required {
  color: var(--color-danger);
  margin-left: 2px;
}
.base-form-field__control {
  position: relative;
}
.base-form-field__error {
  margin-top: var(--space-1);
  font-size: 0.857rem;
  color: var(--color-danger);
}
.base-form-field__help {
  margin-top: var(--space-1);
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
}
</style>
