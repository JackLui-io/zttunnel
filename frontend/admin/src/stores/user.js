import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref({
    id: null,
    username: '',
    nickname: '',
    avatar: '',
    roles: [],
    permissions: []
  })

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 清除token
  const removeToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = {
      ...userInfo.value,
      ...info
    }
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = {
      id: null,
      username: '',
      nickname: '',
      avatar: '',
      roles: [],
      permissions: []
    }
  }

  // 登录
  const login = async (loginForm) => {
    try {
      const res = await loginApi({
        username: loginForm.username,
        password: loginForm.password
      })
      if (res.code === 200 && res.token) {
        setToken(res.token)
        return Promise.resolve(res)
      } else {
        return Promise.reject(new Error(res.msg || '登录失败'))
      }
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const res = await getInfo()
      if (res.code === 200) {
        // 后端返回格式: { code: 200, user: {...}, roles: [...], permissions: [...] }
        const userData = res.user || res.data || {}
        setUserInfo({
          userId: userData.userId,
          id: userData.userId,
          username: userData.userName,
          userName: userData.userName,
          nickname: userData.nickName,
          nickName: userData.nickName,
          avatar: userData.avatar,
          email: userData.email,
          phonenumber: userData.phonenumber,
          sex: userData.sex,
          deptName: userData.dept?.deptName || '',
          roleName: userData.roles?.[0]?.roleName || '',
          roles: res.roles || userData.roles || [],
          permissions: res.permissions || userData.permissions || []
        })
        return Promise.resolve(userData)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  // 登出
  const logout = async () => {
    // 先清除本地状态，避免API调用失败时状态不一致
    removeToken()
    clearUserInfo()

    try {
      // 尝试调用后端登出接口，但不等待结果
      logoutApi().catch(() => {
        // 忽略登出接口的错误，因为本地状态已经清除
      })
    } catch (error) {
      // 忽略错误
    }

    // 立即跳转，不等待API调用完成
    window.location.href = '/login'
  }

  return {
    token,
    userInfo,
    setToken,
    removeToken,
    setUserInfo,
    clearUserInfo,
    login,
    logout,
    getUserInfo
  }
})
