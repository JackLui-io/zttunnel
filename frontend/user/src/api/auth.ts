import request, { type ApiResponse, getToken, setToken, removeToken } from './request'

/** 验证码响应（后端将 captchaOnOff、uuid、img 放在根级别） */
export interface CaptchaResult {
  code?: number
  msg?: string
  captchaOnOff?: boolean
  uuid?: string
  img?: string
}

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
  code: string
  uuid: string
}

/** 用户信息 */
export interface UserInfo {
  userId?: number
  userName?: string
  nickName?: string
  avatar?: string
  [key: string]: unknown
}

/** 获取验证码（数学计算型：显示题目，用户输入答案；img 为 base64 图片） */
export async function getCaptcha(): Promise<CaptchaResult> {
  const res = await request.get<CaptchaResult>('/captchaImage')
  return res.data as CaptchaResult
}

/** 登录 */
export function login(params: LoginParams) {
  return request.post<ApiResponse & { token?: string }>('/login', params).then((res) => {
    const data = res.data
    if (data.token) {
      setToken(data.token)
    }
    return data
  })
}

/** 获取当前用户信息 */
export function getInfo() {
  return request.get<ApiResponse & { user?: UserInfo; roles?: string[]; permissions?: string[] }>('/getInfo').then((res) => res.data)
}

/** 获取路由/菜单 */
export function getRouters() {
  return request.get<ApiResponse<unknown[]>>('/getRouters').then((res) => res.data)
}

/** 登出：调用后端 /logout 清除服务端 Token，再清除本地 */
export async function logout() {
  try {
    await request.post('/logout')
  } catch {
    // 忽略错误，确保本地 Token 被清除
  } finally {
    removeToken()
  }
}

export { getToken, removeToken }
