import request from '@/utils/request'

/**
 * 根据设备ID查询参数 - 对接zttunnel后端 /tunnelDeviceParam/info/{id}
 */
export function getDeviceParamByDeviceId(devicelistId) {
  return request({
    url: `/tunnelDeviceParam/info/${devicelistId}`,
    method: 'get'
  })
}

/**
 * 保存或更新设备参数 - 需要在后端新增此接口
 * 后端路径建议: /tunnelDeviceParam/saveOrUpdate
 */
export function saveOrUpdateDeviceParam(data) {
  return request({
    url: '/tunnelDeviceParam/saveOrUpdate',
    method: 'post',
    data
  })
}

/**
 * 删除设备参数 - 需要在后端新增此接口
 * 后端路径建议: /tunnelDeviceParam/{id}
 */
export function deleteDeviceParam(id) {
  return request({
    url: `/tunnelDeviceParam/${id}`,
    method: 'delete'
  })
}
