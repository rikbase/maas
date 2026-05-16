<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-logo">MaaS Platform</h1>
      <form @submit.prevent="handleLogin">
        <BaseFormField :label="$t('auth.username')" required>
          <input
            v-model="form.username"
            type="text"
            class="login-input"
            required
            :disabled="loading"
            autocomplete="username"
          />
        </BaseFormField>
        <BaseFormField :label="$t('auth.password')" required>
          <input
            v-model="form.password"
            type="password"
            class="login-input"
            required
            :disabled="loading"
            autocomplete="current-password"
          />
        </BaseFormField>
        <p v-if="error" class="login-error">{{ error }}</p>
        <BaseButton
          type="submit"
          variant="primary"
          size="lg"
          :loading="loading"
          :disabled="loading"
          class="login-btn"
        >
          {{ loading ? $t('common.loading') : $t('auth.login') }}
        </BaseButton>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

import BaseButton from '../components/ui/BaseButton.vue'
import BaseFormField from '../components/ui/BaseFormField.vue'

const router = useRouter()
const auth = useAuthStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    await auth.login(form.value.username, form.value.password)
    router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0414, #1a0a2e, #0f0b29);
}

.login-card {
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  padding: var(--space-8);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
  border: 1px solid rgba(255,255,255,0.08);
}

.login-logo {
  text-align: center;
  margin: 0 0 var(--space-8);
  font-size: 1.5rem;
  font-weight: 800;
  color: rgba(255,255,255,0.9);
  letter-spacing: -0.02em;
}

/* Override BaseFormField label color for dark login page */
.login-card :deep(.base-form-field__label) {
  color: rgba(255,255,255,0.7);
}

.login-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  font-family: var(--font-sans);
  color: rgba(255,255,255,0.9);
  background: rgba(255,255,255,0.06);
  box-sizing: border-box;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.login-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.login-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-input::placeholder {
  color: rgba(255,255,255,0.3);
}

.login-error {
  color: #fca5a5;
  font-size: 0.857rem;
  margin: 0 0 var(--space-4);
  padding: var(--space-2) var(--space-3);
  background: rgba(239,68,68,0.15);
  border-radius: var(--radius-sm);
}

.login-btn {
  width: 100%;
}
</style>
