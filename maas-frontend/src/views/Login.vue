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
  background: linear-gradient(135deg, var(--sidebar-bg, #0f172a) 0%, #1e293b 100%);
}

.login-card {
  background: var(--color-bg-card);
  padding: var(--space-8);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 420px;
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--color-border);
}

.login-logo {
  text-align: center;
  margin: 0 0 var(--space-8);
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-foreground);
  letter-spacing: -0.02em;
}

.login-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  font-family: var(--font-sans);
  color: var(--color-foreground);
  background: var(--color-bg-card);
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

.login-error {
  color: var(--color-danger);
  font-size: 0.857rem;
  margin: 0 0 var(--space-4);
  padding: var(--space-2) var(--space-3);
  background: var(--color-danger-light);
  border-radius: var(--radius-sm);
}

.login-btn {
  width: 100%;
}
</style>
