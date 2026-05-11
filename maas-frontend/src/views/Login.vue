<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="logo">MaaS Platform</h1>
      <form @submit.prevent="handleLogin">
        <div class="field">
          <label>{{ $t('auth.username') }}</label>
          <input v-model="form.username" type="text" required :disabled="loading" autocomplete="username" />
        </div>
        <div class="field">
          <label>{{ $t('auth.password') }}</label>
          <input v-model="form.password" type="password" required :disabled="loading" autocomplete="current-password" />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? $t('common.loading') : $t('auth.login') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

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
  background: #1a1a2e;
}
.login-card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  width: 360px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
}
.logo {
  text-align: center;
  margin-bottom: 32px;
  font-size: 22px;
  color: #1a1a2e;
}
.field { margin-bottom: 20px; }
.field label { display: block; margin-bottom: 6px; font-weight: 500; color: #333; }
.field input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 15px;
  box-sizing: border-box;
}
.field input:focus { outline: none; border-color: #1976d2; }
.btn-primary {
  width: 100%;
  padding: 10px;
  background: #1976d2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
}
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.error { color: #c62828; font-size: 13px; margin-bottom: 12px; }
</style>
