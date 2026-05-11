import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'
import type { UserVO } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('maas_token'))
  const user = ref<UserVO | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  async function login(username: string, password: string) {
    const res = await authApi.login(username, password)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('maas_token', res.data.token)
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('maas_token')
  }

  async function fetchMe() {
    if (!token.value) return
    try {
      const res = await authApi.me()
      user.value = res.data
    } catch {
      logout()
    }
  }

  return { token, user, isAuthenticated, isAdmin, login, logout, fetchMe }
})
