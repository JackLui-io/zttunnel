import request from '@/utils/request'

/**
 * 分页查询设备告警 - 需要在后端新增此接口
 * 后端路径建议: /device/alarm/page
 */
export function getDeviceAlarmPage(params) {
  return request({
    url: '/device/alarm/page',
    method: 'get',
    params
  })
}

/**
 * 根据设备ID查询告警 - 需要在后端新增此接口
 * 后端路径建议: /device/alarm/device/{deviceId}
 */
export function getDeviceAlarmByDeviceId(deviceId) {
  return request({
    url: `/device/alarm/device/${deviceId}`,
    method: 'get'
  })
}

/**
 * 新增设备告警 - 需要在后端新增此接口
 * 后端路径建议: /device/alarm
 */
export function addDeviceAlarm(data) {
  return request({
    url: '/device/alarm',
    method: 'post',
    data
  })
}

/**
 * 更新设备告警 - 需要在后端新增此接口
 * 后端路径建议: /device/alarm
 */
export function updateDeviceAlarm(data) {
  return request({
    url: '/device/alarm',
    method: 'put',
    data
  })
}

/**
 * 删除设备告警 - 需要在后端新增此接口
 * 后端路径建议: /device/alarm/{id}
 */
export function deleteDeviceAlarm(id) {
  return request({
    url: `/device/alarm/${id}`,
    method: 'delete'
  })
}
