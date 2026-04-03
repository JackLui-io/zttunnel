import request from '@/utils/request'

// 获取隧道参数信息 - 对接zttunnel后端 /tunnel/tunnel/device/infoById
export function getTunnelParamInfo(tunnelId) {
  return request({
    url: '/tunnel/tunnel/device/infoById',
    method: 'get',
    params: { id: tunnelId }
  })
}

// 更新隧道参数信息 - 对接zttunnel后端 /tunnel/update/tunnel/device/infoById
export function updateTunnelParamInfo(data) {
  return request({
    url: '/tunnel/update/tunnel/device/infoById',
    method: 'post',
    data: data
  })
}

// 获取边缘控制器列表 - 对接zttunnel后端 /tunnel/get/devicelist
export function getEdgeControllerList(data) {
  return request({
    url: '/tunnel/get/devicelist',
    method: 'post',
    data: { ...data, type: 1 }
  })
}

// 获取电能终端列表 - 对接zttunnel后端 /tunnel/get/devicelist
export function getPowerTerminalList(data) {
  return request({
    url: '/tunnel/get/devicelist',
    method: 'post',
    data: { ...data, type: 2 }
  })
}

// 更新边缘控制器/电能终端 - 对接zttunnel后端 /tunnel/update/devicelist
export function updateDevicelist(data) {
  return request({
    url: '/tunnel/update/devicelist',
    method: 'post',
    data
  })
}

/** 设备号主键替换（占位→真实）：POST /tunnel/rebind/devicelist */
export function rebindDevicelist(data) {
  return request({
    url: '/tunnel/rebind/devicelist',
    method: 'post',
    data
  })
}

// 获取灯具终端列表 - 对接zttunnel后端 /tunnel/get/device/lamp
export function getLampsTerminalList(data) {
  return request({
    url: '/tunnel/get/device/lamp',
    method: 'post',
    data
  })
}

// 更新灯具终端 - 对接zttunnel后端 /tunnel/update/device/lamp
export function updateLampsTerminal(data) {
  return request({
    url: '/tunnel/update/device/lamp',
    method: 'post',
    data
  })
}

// 删除灯具终端 - 对接zttunnel后端 /tunnel/drop/device/lamp
export function deleteLampsTerminal(id) {
  return request({
    url: '/tunnel/drop/device/lamp',
    method: 'get',
    params: { id }
  })
}

// 获取引道灯控制器列表 - 对接zttunnel后端 /approachLamps/list
export function getApproachLampsList(params) {
  return request({
    url: '/approachLamps/list',
    method: 'get',
    params
  })
}

// 新增引道灯控制器 - 对接zttunnel后端 /approachLamps/add
export function addApproachLamps(data) {
  return request({
    url: '/approachLamps/add',
    method: 'post',
    data
  })
}

// 更新引道灯控制器 - 对接zttunnel后端 /approachLamps/update
export function updateApproachLamps(data) {
  return request({
    url: '/approachLamps/update',
    method: 'post',
    data
  })
}

// 删除引道灯控制器 - DELETE /approachLamps/delete，后端 @RequestParam List<Long> idList
export function deleteApproachLamps(id) {
  return request({
    url: `/approachLamps/delete?idList=${encodeURIComponent(id)}`,
    method: 'delete'
  })
}

// 获取电表列表 - 对接zttunnel后端 /tunnel/power/list
export function getPowerList(deviceListId) {
  return request({
    url: '/tunnel/power/list',
    method: 'get',
    params: { deviceListId }
  })
}

// 新增和修改电表 - 对接zttunnel后端 /tunnel/saveOrUpdate/power
export function saveOrUpdatePower(data) {
  return request({
    url: '/tunnel/saveOrUpdate/power',
    method: 'post',
    data
  })
}

// 删除电表 - 对接zttunnel后端 /tunnel/drop/power
export function deletePower(id) {
  return request({
    url: '/tunnel/drop/power',
    method: 'get',
    params: { id }
  })
}

// 获取厂商配置列表 - 对接zttunnel后端 /tunnel/power/vendor/config
export function getPowerVendorConfig() {
  return request({
    url: '/tunnel/power/vendor/config',
    method: 'get'
  })
}

// 电能终端下发 - 对接zttunnel后端 /tunnel/issued
export function issuedDevice(devicelistId) {
  return request({
    url: '/tunnel/issued',
    method: 'get',
    params: { devicelistId }
  })
}

// ========== OTA文件管理 ==========

// 获取OTA文件列表 - 对接zttunnel后端 /ota/file/list
export function getOtaFileList(params) {
  return request({
    url: '/ota/file/list',
    method: 'get',
    params
  })
}

// 删除OTA文件 - 对接zttunnel后端 /ota/del
export function deleteOtaFile(id) {
  return request({
    url: '/ota/del',
    method: 'get',
    params: { id }
  })
}

// 启动OTA升级 - 对接zttunnel后端 /ota/open
export function startOtaUpgrade(data) {
  return request({
    url: '/ota/open',
    method: 'post',
    data
  })
}

// 批量启动OTA升级 - 对接zttunnel后端 /ota/batchOpen
export function batchStartOtaUpgrade(data) {
  return request({
    url: '/ota/batchOpen',
    method: 'post',
    data
  })
}

// 根据OTA文件ID获取设备列表 - 对接zttunnel后端 /ota/device/list
export function getOtaDeviceList(id) {
  return request({
    url: '/ota/device/list',
    method: 'get',
    params: { id }
  })
}

// ========== KML文件管理 ==========

// 上传KML文件 - 对接zttunnel后端 /tunnel/uoload/kml
export function uploadKmlFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/tunnel/uoload/kml',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取经纬度数据 - 对接zttunnel后端 /tunnel/longitudeLatitude
export function getLongitudeLatitude(tunnelId, isDown) {
  return request({
    url: '/tunnel/longitudeLatitude',
    method: 'get',
    params: { tunnelId, isDown }
  })
}

// 获取区段列表 - 对接zttunnel后端 /device/getZone
export function getZoneList() {
  return request({
    url: '/device/getZone',
    method: 'get'
  })
}


// ========== 电表配置管理 ==========

// 获取电表配置列表 - 对接zttunnel后端 /tunnel/power/list
export function getPowerEdgeComputingList(params) {
  return request({
    url: '/tunnel/power/list',
    method: 'get',
    params
  })
}

// 新增电表配置 - 对接zttunnel后端 /tunnel/saveOrUpdate/power
export function addPowerEdgeComputing(data) {
  return request({
    url: '/tunnel/saveOrUpdate/power',
    method: 'post',
    data
  })
}

// 更新电表配置 - 对接zttunnel后端 /tunnel/saveOrUpdate/power
export function updatePowerEdgeComputing(data) {
  return request({
    url: '/tunnel/saveOrUpdate/power',
    method: 'post',
    data
  })
}

// 删除电表配置 - 对接zttunnel后端 /tunnel/drop/power
export function deletePowerEdgeComputing(id) {
  return request({
    url: '/tunnel/drop/power',
    method: 'get',
    params: { id }
  })
}

// 电表配置高级查询 - 需要在后端确认接口
export function searchPowerEdgeComputing(data) {
  return request({
    url: '/tunnel/power/edge/computing/search',
    method: 'post',
    data
  })
}

// 全隧道查询 - 按电能终端ID查询电力数据
export function searchPowerDataByDevicelistId(devicelistId, limit = 100) {
  return request({
    url: '/tunnel/power/data/by-devicelist',
    method: 'get',
    params: { devicelistId, limit }
  })
}

// 全隧道查询 - 按电能终端ID和时间范围查询
export function searchPowerDataByDevicelistIdAndTime(devicelistId, startTime, endTime) {
  return request({
    url: '/tunnel/power/data/by-devicelist-and-time',
    method: 'get',
    params: { devicelistId, startTime, endTime }
  })
}

// 全隧道查询 - 按名称查询
export function searchPowerDataByName(name) {
  return request({
    url: '/tunnel/power/data/by-name',
    method: 'get',
    params: { name }
  })
}
