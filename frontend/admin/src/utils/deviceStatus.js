/**
 * 设备状态统一工具函数
 * 状态值: 1=在线, 0=离线, 2=故障
 */

// 获取状态标签类型
export const getStatusType = (status) => {
  const statusNum = Number(status)
  if (statusNum === 1) return 'success'
  if (statusNum === 0) return 'info'
  if (statusNum === 2) return 'danger'
  return 'info'
}

// 获取状态文本
export const getStatusText = (status) => {
  const statusNum = Number(status)
  if (statusNum === 1) return '在线'
  if (statusNum === 0) return '离线'
  if (statusNum === 2) return '故障'
  return '未知'
}

// 状态选项列表
export const statusOptions = [
  { label: '在线', value: 1 },
  { label: '离线', value: 0 },
  { label: '故障', value: 2 }
]

// 根据状态文本获取状态值
export const getStatusValue = (text) => {
  if (text === '在线') return 1
  if (text === '离线') return 0
  if (text === '故障') return 2
  return null
}

/** 列表中的时间展示（兼容字符串 / 时间戳 / Date） */
export const formatDisplayTime = (val) => {
  if (val == null || val === '') return '—'
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/**
 * tunnel_devicelist 列表「设备状态」：接口里 deviceStatus 常为 null，
 * 与 zt_tunnel_web 一致时在文本为空时按 online 回退 1→正常、0→异常。
 */
export const getDevicelistDeviceStatusText = (row) => {
  const t = row?.deviceStatus
  if (t != null && String(t).trim() !== '') return String(t)
  const n = Number(row?.online)
  if (n === 1) return '正常'
  if (n === 0) return '异常'
  return '—'
}

/**
 * 灯具终端「设备状态」（zt_tunnel_web table5 dengju）：字段 communicationState
 * 0=正常，1=异常；空为 —；其它值原样展示（对齐旧版 getSee 未命中枚举时）。
 */
export const getLampCommunicationStateText = (row) => {
  const v = row?.communicationState
  if (v == null || v === '') return '—'
  const n = Number(v)
  if (n === 0) return '正常'
  if (n === 1) return '异常'
  return String(v)
}

/** Element Plus tag：0 success，1 danger，其余 info */
export const getLampCommunicationStateTagType = (row) => {
  const v = row?.communicationState
  if (v == null || v === '') return 'info'
  const n = Number(v)
  if (n === 0) return 'success'
  if (n === 1) return 'danger'
  return 'info'
}
