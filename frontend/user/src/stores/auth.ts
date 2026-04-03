import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/api/request'
import { logout as apiLogout } from '@/api/auth'
import { getInfo, type UserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const user = ref<UserInfo | null>(null)

  function setTokenValue(t: string) {
    token.value = t
    setToken(t)
  }

async function clearAuth() {
  await apiLogout()
  token.value = ''
  user.value = null
}

  async function fetchUserInfo() {
    // 使用 getToken() 确保读取最新 token（登录后 localStorage 已更新，store.token 可能未同步）
    if (!getToken()) return null
    const res = await getInfo()
    user.value = res.user ?? null
    return user.value
  }

  return {
    token,
    user,
    setToken: setTokenValue,
    clearAuth,
    fetchUserInfo,
  }
})
