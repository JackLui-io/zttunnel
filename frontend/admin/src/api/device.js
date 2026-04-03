import request from '@/utils/request'

// 获取设备列表 - 对接zttunnel后端 /device/list
export function getDeviceList(data, params) {
  return request({
    url: '/device/list',
    method: 'post',
    data: data,
    params
  })
}

// 根据隧道ID统计设备 - 对接zttunnel后端 /device/countByTunnelId
export function countByTunnelId(tunnelId) {
  return request({
    url: '/device/countByTunnelId',
    method: 'get',
    params: { tunnelId }
  })
}

// Dashboard 设备状态分布（当前用户可见隧道汇总）- /analyze/dashboard/deviceStatusDistribution
export function getDeviceStatusDistribution() {
  return request({
    url: '/analyze/dashboard/deviceStatusDistribution',
    method: 'get'
  })
}

// 获取设备状态枚举 - 对接zttunnel后端 /device/getDeviceStatus
export function getDeviceStatus() {
  return request({
    url: '/device/getDeviceStatus',
    method: 'get'
  })
}

// 获取设备类型枚举 - 对接zttunnel后端 /device/getDeviceType
export function getDeviceType() {
  return request({
    url: '/device/getDeviceType',
    method: 'get'
  })
}

// 获取区段枚举 - 对接zttunnel后端 /device/getZone
export function getZone() {
  return request({
    url: '/device/getZone',
    method: 'get'
  })
}

// 新增或修改设备 - 对接zttunnel后端 /device/saveOrUpdate
export function saveOrUpdateDevice(data) {
  return request({
    url: '/device/saveOrUpdate',
    method: 'post',
    data: data
  })
}

// 删除设备 - 对接zttunnel后端 /device/delete
export function deleteDevice(id) {
  return request({
    url: '/device/delete',
    method: 'get',
    params: { id }
  })
}

// 导出设备列表 - 对接zttunnel后端 /device/list/export
export function exportDevices(deviceDto) {
  return request({
    url: '/device/list/export',
    method: 'post',
    data: deviceDto,
    responseType: 'blob'
  })
}

/** 设备工作台：多叶子隧道 + deviceType + 分页（返回 total + rows） */
export function getDeviceWorkspaceList(data) {
  return request({
    url: '/device/workspace/list',
    method: 'post',
    data
  })
}

/** 设备工作台导出（筛选条件同 list，全量） */
export function exportDeviceWorkspace(data) {
  return request({
    url: '/device/workspace/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// ========== 边缘控制器/电能终端 ==========

// 获取边缘控制器/电能终端列表 - 对接zttunnel后端 /tunnel/get/devicelist
export function getDevicelist(data) {
  return request({
    url: '/tunnel/get/devicelist',
    method: 'post',
    data
  })
}

// 编辑边缘控制器和电能终端 - 对接zttunnel后端 /tunnel/update/devicelist
export function updateDevicelist(data) {
  return request({
    url: '/tunnel/update/devicelist',
    method: 'post',
    data
  })
}

// ========== 灯具终端 ==========

// 获取灯具终端列表 - 对接zttunnel后端 /tunnel/get/device/lamp
export function getDeviceLamp(data) {
  return request({
    url: '/tunnel/get/device/lamp',
    method: 'post',
    data
  })
}

// 根据灯具终端id获取节点信息 - 对接zttunnel后端 /tunnel/get/device/lamp/node
export function getDeviceLampNode(id) {
  return request({
    url: '/tunnel/get/device/lamp/node',
    method: 'get',
    params: { id }
  })
}

// 编辑灯具终端 - 对接zttunnel后端 /tunnel/update/device/lamp
export function updateDeviceLamp(data) {
  return request({
    url: '/tunnel/update/device/lamp',
    method: 'post',
    data
  })
}

// 编辑灯具终端关联节点 - 对接zttunnel后端 /tunnel/update/device/lamp/node
export function updateDeviceLampNode(data) {
  return request({
    url: '/tunnel/update/device/lamp/node',
    method: 'post',
    data
  })
}

// 删除灯具终端 - 对接zttunnel后端 /tunnel/drop/device/lamp
export function deleteDeviceLamp(id) {
  return request({
    url: '/tunnel/drop/device/lamp',
    method: 'get',
    params: { id }
  })
}

// ========== 洞外雷达/亮度传感器 ==========

// 获取洞外雷达和洞外传感器 - 对接zttunnel后端 /tunnel/get/device/dwld
export function getDeviceDwld(data) {
  return request({
    url: '/tunnel/get/device/dwld',
    method: 'post',
    data
  })
}

// 编辑洞外雷达和洞外传感器 - 对接zttunnel后端 /tunnel/update/device/dwld
export function updateDeviceDwld(data) {
  return request({
    url: '/tunnel/update/device/dwld',
    method: 'post',
    data
  })
}

// 删除洞外雷达和洞外传感器 - 对接zttunnel后端 /tunnel/drop/device/dwld
export function deleteDeviceDwld(id) {
  return request({
    url: '/tunnel/drop/device/dwld',
    method: 'get',
    params: { id }
  })
}

// ========== 电能终端下发 ==========

// 电能终端下发 - 对接zttunnel后端 /tunnel/issued
export function issuedDevice(devicelistId) {
  return request({
    url: '/tunnel/issued',
    method: 'get',
    params: { devicelistId }
  })
}

// 灯具下发 - 对接zttunnel后端 /tunnel/lamp/issued
export function lampIssued(tunnelId) {
  return request({
    url: '/tunnel/lamp/issued',
    method: 'get',
    params: { tunnelId }
  })
}


// ========== 新增设备相关 ==========

// 新增边缘控制器/电能终端 - 需要在后端确认接口
export function addDevicelist(data) {
  return request({
    url: '/device/saveOrUpdate',
    method: 'post',
    data: data
  })
}

// 新增灯具终端 - 与 zt_tunnel_web 一致：POST /tunnel/update/device/lamp（uniqueId 为空时后端走新增分支）
export function addLampsTerminal(data) {
  return request({
    url: '/tunnel/update/device/lamp',
    method: 'post',
    data
  })
}

// 新增洞外雷达/亮度传感器 - 需要在后端确认接口
export function addOutOfRadar(data) {
  return request({
    url: '/device/saveOrUpdate',
    method: 'post',
    data: data
  })
}

// ========== 电能表厂商管理 ==========

// 获取电能表厂商列表 - 对接zttunnel后端 /tunnel/power/vendor/config
export function getPowerVendorList() {
  return request({
    url: '/tunnel/power/vendor/config',
    method: 'get'
  })
}

// 新增电能表厂商 - 需要在后端新增此接口
export function addPowerVendor(data) {
  return request({
    url: '/tunnel/power/vendor/add',
    method: 'post',
    data
  })
}

// 更新电能表厂商 - 需要在后端新增此接口
export function updatePowerVendor(data) {
  return request({
    url: '/tunnel/power/vendor/update',
    method: 'put',
    data
  })
}

// 删除电能表厂商 - 需要在后端新增此接口
export function deletePowerVendor(vendorid) {
  return request({
    url: `/tunnel/power/vendor/delete/${vendorid}`,
    method: 'delete'
  })
}

// 更新设备 - 对接zttunnel后端 /device/saveOrUpdate
export function updateDevice(data) {
  return request({
    url: '/device/saveOrUpdate',
    method: 'post',
    data: data
  })
}


// 根据隧道ID和设备类型统计设备 - 使用 countByTunnelId 接口
export function countByTunnelIdAndType(tunnelId, deviceType) {
  // 后端没有按类型统计的接口，使用 countByTunnelId 获取总体统计
  return request({
    url: '/device/countByTunnelId',
    method: 'get',
    params: { tunnelId }
  })
}
