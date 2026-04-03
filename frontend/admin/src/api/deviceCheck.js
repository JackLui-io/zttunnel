import request from '@/utils/request'

// 获取检测设备列表
export function getCheckDeviceList() {
  return request({
    url: '/device/check/list',
    method: 'get'
  })
}

// 添加检测设备（单个）
export function addCheckDevice(data) {
  return request({
    url: '/device/check/add',
    method: 'post',
    data
  })
}

// 批量添加检测设备
export function batchAddCheckDevice(data) {
  return request({
    url: '/device/check/batchAdd',
    method: 'post',
    data
  })
}

// 移除检测设备
export function removeCheckDevice(id) {
  return request({
    url: '/device/check/remove',
    method: 'delete',
    params: { id }
  })
}

// 批量移除检测设备
export function batchRemoveCheckDevice(ids) {
  return request({
    url: '/device/check/batchRemove',
    method: 'delete',
    data: ids
  })
}

// 获取所有边缘控制器
export function getAllEdgeControllers() {
  return request({
    url: '/device/check/devicelist/edge',
    method: 'get'
  })
}

// 获取所有灯具终端
export function getAllLampsTerminal() {
  return request({
    url: '/device/check/lamps/all',
    method: 'get'
  })
}
