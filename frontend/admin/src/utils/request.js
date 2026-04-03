import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // api的base_url
  timeout: 60000 // 请求超时时间
})

// request拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error)
    Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    const res = response.data

    // 如果是blob类型（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return res
    }

    // Spring void / 无响应体：HTTP 200 时 axios 得到 null、undefined 或空字符串，无法读取 { code }
    if (
      response.status === 200 &&
      (res === '' || res === null || typeof res === 'undefined')
    ) {
      return { code: 200, msg: 'success', data: null }
    }

    // 少数环境下 void 接口被解析为空对象且不含业务 code
    const reqUrl = response.config?.url || ''
    if (
      response.status === 200 &&
      res &&
      typeof res === 'object' &&
      !Array.isArray(res) &&
      !('code' in res) &&
      reqUrl.includes('/easyExcel/excelInput')
    ) {
      return { code: 200, msg: 'success', data: null }
    }

    // 如果自定义代码不是200，则判断为错误
    if (res.code !== 200) {
      let errorMsg = res.msg || res.message || ''
      if (!errorMsg || errorMsg === 'No message available') {
        errorMsg = '操作失败'
      }
      console.error('API错误响应:', {
        url: response.config.url,
        method: response.config.method,
        code: res.code,
        msg: errorMsg,
        data: res
      })
      
      ElMessage({
        message: errorMsg,
        type: 'error',
        duration: 5 * 1000
      })

      // 401: 未授权
      if (res.code === 401) {
        localStorage.removeItem('token')
        // 避免在logout页面重复跳转
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login'
        }
      }

      // 403: 无权限
      if (res.code === 403) {
        console.error('无权限访问:', response.config.url)
      }

      return Promise.reject(new Error(errorMsg))
    } else {
      return res
    }
  },
  error => {
    // 对响应错误做点什么
    console.log('err' + error)

    // 处理网络错误或HTTP状态码错误
    let errorMsg = error.message || '网络错误'
    if (error.response) {
      const { status, data } = error.response
      let m = data?.msg || data?.message
      if (!m || m === 'No message available') {
        const errPart = data?.error ? String(data.error) : ''
        const pathPart = data?.path ? ` ${data.path}` : ''
        m = (errPart + pathPart).trim() || null
      }
      errorMsg = m || `请求失败(${status})`
      
      if (status === 401) {
        localStorage.removeItem('token')
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
      
      if (status === 403) {
        errorMsg = '没有操作权限'
      }
      
      if (status === 500) {
        errorMsg = data?.msg || '服务器内部错误'
      }
    }

    ElMessage({
      message: errorMsg,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(new Error(errorMsg))
  }
)

export default service
