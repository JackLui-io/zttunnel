import request from '@/utils/request'

// 登录 - 对接zttunnel后端 /login
export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    data: data
  })
}

// 获取用户信息 - 对接zttunnel后端 /getInfo
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 退出登录 - 对接zttunnel后端 /logout
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码 - 对接zttunnel后端 /captchaImage
export function getVerifyCodeImg() {
  return request({
    url: '/captchaImage',
    method: 'get',
    timeout: 20000
  })
}
