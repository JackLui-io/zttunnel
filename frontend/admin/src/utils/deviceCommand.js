/**
 * 设备指令通信工具
 * 用于与电能终端、边缘控制器等设备进行WebSocket通信
 */

import { ElLoading, ElMessage } from 'element-plus'

// WebSocket连接状态
const WS_STATE = {
  INIT: 0,
  CONNECTING: 1,
  CONNECTED: 2,
  CLOSED: 3
}

// 指令超时时间（毫秒）
const CMD_TIMEOUT = 5000

// WebSocket实例
let ws = null
let wsState = WS_STATE.INIT
let clientId = null
let messageCallbacks = new Map()
let reconnectTimer = null

/**
 * 获取WebSocket服务地址
 */
function getWsUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  // 生成唯一客户端ID
  if (!clientId) {
    clientId = 'web_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  }
  return `${protocol}//${host}/ws/device/${clientId}`
}

/**
 * 初始化WebSocket连接
 */
export function initWebSocket() {
  if (ws && wsState === WS_STATE.CONNECTED) {
    return Promise.resolve()
  }

  return new Promise((resolve, reject) => {
    try {
      wsState = WS_STATE.CONNECTING
      ws = new WebSocket(getWsUrl())

      ws.onopen = () => {
        console.log('WebSocket连接成功')
        wsState = WS_STATE.CONNECTED
        resolve()
      }

      ws.onmessage = (event) => {
        handleMessage(event.data)
      }

      ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        wsState = WS_STATE.CLOSED
        reject(error)
      }

      ws.onclose = () => {
        console.log('WebSocket连接关闭')
        wsState = WS_STATE.CLOSED
        // 尝试重连
        scheduleReconnect()
      }
    } catch (error) {
      wsState = WS_STATE.CLOSED
      reject(error)
    }
  })
}

/**
 * 计划重连
 */
function scheduleReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
  reconnectTimer = setTimeout(() => {
    console.log('尝试重新连接WebSocket...')
    initWebSocket().catch(() => {
      // 重连失败，继续尝试
      scheduleReconnect()
    })
  }, 3000)
}

/**
 * 处理WebSocket消息
 */
function handleMessage(data) {
  try {
    const msg = JSON.parse(data)
    const type = msg.type

    // 查找对应的回调
    if (type === 'readResponse' || type === 'writeResponse' || type === 'restartResponse') {
      const paramType = msg.paramType || type
      const callback = messageCallbacks.get(paramType)
      if (callback) {
        callback(msg)
        messageCallbacks.delete(paramType)
      }
    } else if (type === 'error') {
      ElMessage.error(msg.message || '操作失败')
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
  }
}

/**
 * 发送WebSocket消息
 */
function sendWsMessage(message) {
  return new Promise((resolve, reject) => {
    if (!ws || wsState !== WS_STATE.CONNECTED) {
      // 尝试连接
      initWebSocket()
        .then(() => {
          ws.send(JSON.stringify(message))
          resolve()
        })
        .catch(reject)
    } else {
      ws.send(JSON.stringify(message))
      resolve()
    }
  })
}

/**
 * 发送设备指令
 * @param {string} deviceNo 设备号
 * @param {string} type 指令类型 (read/write/restart)
 * @param {string} paramType 参数类型
 * @param {any} value 参数值（写入时使用）
 * @param {Function} callback 回调函数
 */
export function sendCommand(deviceNo, type, paramType, value, callback) {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '指令发送中,请稍候......',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  // 设置超时
  const timeoutId = setTimeout(() => {
    loadingInstance.close()
    messageCallbacks.delete(paramType)
    ElMessage.error('等待回复超时!')
    if (callback) callback({ success: false, message: '超时' })
  }, CMD_TIMEOUT)

  // 注册回调
  messageCallbacks.set(paramType, (response) => {
    clearTimeout(timeoutId)
    loadingInstance.close()
    if (callback) callback(response)
  })

  // 发送消息
  const message = {
    type,
    deviceNo,
    paramType,
    value
  }

  sendWsMessage(message).catch((error) => {
    clearTimeout(timeoutId)
    loadingInstance.close()
    messageCallbacks.delete(paramType)
    ElMessage.error('发送指令失败!')
    if (callback) callback({ success: false, message: error.message })
  })
}

/**
 * 读取服务器地址和端口
 */
export function readServerPort(deviceNo, callback) {
  sendCommand(deviceNo, 'read', 'serverPort', null, (response) => {
    if (response.data) {
      callback(response.data)
    } else {
      callback(null)
    }
  })
}

/**
 * 更新服务器地址和端口
 */
export function updateServerPort(deviceNo, server, port, callback) {
  if (!server || !port) {
    ElMessage.error('请输入服务器地址和端口!')
    return
  }

  const portNum = parseInt(port)
  if (portNum >= 65535 || portNum === 0) {
    ElMessage.error('服务端口号错误!')
    return
  }

  sendCommand(deviceNo, 'write', 'serverPort', { server, port: portNum }, (response) => {
    if (response.success) {
      ElMessage.success('更新成功!')
    }
    if (callback) callback(response)
  })
}

/**
 * 读取心跳周期
 */
export function readHeartbeat(deviceNo, callback) {
  sendCommand(deviceNo, 'read', 'heartbeat', null, (response) => {
    if (response.data) {
      callback(response.data)
    } else {
      callback(null)
    }
  })
}

/**
 * 更新心跳周期
 */
export function updateHeartbeat(deviceNo, heartbeat, callback) {
  if (!heartbeat) {
    ElMessage.error('请输入心跳包周期!')
    return
  }

  sendCommand(deviceNo, 'write', 'heartbeat', { heartbeat: parseInt(heartbeat) }, (response) => {
    if (response.success) {
      ElMessage.success('更新成功!')
    }
    if (callback) callback(response)
  })
}

/**
 * 读取读超时
 */
export function readReadTimeout(deviceNo, callback) {
  sendCommand(deviceNo, 'read', 'readTimeout', null, (response) => {
    if (response.data) {
      callback(response.data)
    } else {
      callback(null)
    }
  })
}

/**
 * 更新读超时
 */
export function updateReadTimeout(deviceNo, timeout, callback) {
  if (!timeout) {
    ElMessage.error('请输入读超时时间!')
    return
  }

  sendCommand(deviceNo, 'write', 'readTimeout', { readTimeout: parseInt(timeout) }, (response) => {
    if (response.success) {
      ElMessage.success('更新成功!')
    }
    if (callback) callback(response)
  })
}

/**
 * 读取写超时
 */
export function readWriteTimeout(deviceNo, callback) {
  sendCommand(deviceNo, 'read', 'writeTimeout', null, (response) => {
    if (response.data) {
      callback(response.data)
    } else {
      callback(null)
    }
  })
}

/**
 * 更新写超时
 */
export function updateWriteTimeout(deviceNo, timeout, callback) {
  if (!timeout) {
    ElMessage.error('请输入写超时时间!')
    return
  }

  sendCommand(deviceNo, 'write', 'writeTimeout', { writeTimeout: parseInt(timeout) }, (response) => {
    if (response.success) {
      ElMessage.success('更新成功!')
    }
    if (callback) callback(response)
  })
}

/**
 * 读取固件信息
 */
export function readFirmware(deviceNo, callback) {
  sendCommand(deviceNo, 'read', 'firmware', null, (response) => {
    if (response.data) {
      callback(response.data)
    } else {
      callback(null)
    }
  })
}

/**
 * 重启终端
 */
export function restartDevice(deviceNo, callback) {
  sendCommand(deviceNo, 'restart', 'restartResponse', null, (response) => {
    if (response.success) {
      ElMessage.success('收到终端回复!')
    }
    if (callback) callback(response)
  })
}

/**
 * 关闭WebSocket连接
 */
export function closeWebSocket() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.close()
    ws = null
  }
  wsState = WS_STATE.CLOSED
}

export default {
  initWebSocket,
  closeWebSocket,
  sendCommand,
  readServerPort,
  updateServerPort,
  readHeartbeat,
  updateHeartbeat,
  readReadTimeout,
  updateReadTimeout,
  readWriteTimeout,
  updateWriteTimeout,
  readFirmware,
  restartDevice
}
