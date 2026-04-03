import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import router from '@/router'

const TOKEN_KEY = 'token'

/** 后端统一响应格式 */
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data?: T
  token?: string
}

/** 分页响应 */
export interface PageResponse<T> {
  code: number
  msg: string
  total: number
  rows: T[]
}

/** 获取存储的 Token */
export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/** 设置 Token */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 清除 Token */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const request: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：注入 Token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 401、业务错误
request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse
    if (res.code === 401) {
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(res.msg || '登录已过期'))
    }
    if (res.code !== 200 && res.code !== 0 && res.code !== undefined) {
      return Promise.reject(new Error((res as { msg?: string; message?: string }).msg || (res as { msg?: string; message?: string }).message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
    }
    const msg = error.response?.data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export default request
