import request from '@/utils/request'

// 获取通知列表 - 对接zttunnel后端 /notice/list
export function getNoticeList(data, params) {
  return request({
    url: '/notice/list',
    method: 'post',
    data,
    params
  })
}

// 通知公告数量统计 - 对接zttunnel后端 /notice/countByTunnel
export function countByTunnel(data) {
  return request({
    url: '/notice/countByTunnel',
    method: 'post',
    data
  })
}

// 通过id查询类型个数 - 对接zttunnel后端 /notice/countTypeByTunnelId
export function countTypeByTunnelId(tunnelId) {
  return request({
    url: '/notice/countTypeByTunnelId',
    method: 'get',
    params: { tunnelId }
  })
}

// 保存或更新通知 - 对接zttunnel后端 /notice/saveOrUpdate
export function saveOrUpdateNotice(data) {
  return request({
    url: '/notice/saveOrUpdate',
    method: 'post',
    data
  })
}

// 删除通知 - 对接zttunnel后端 /notice/delete
export function deleteNotice(id) {
  return request({
    url: '/notice/delete',
    method: 'get',
    params: { id }
  })
}

// 获取通知类型 - 对接zttunnel后端 /notice/getNoticeType
export function getNoticeType() {
  return request({
    url: '/notice/getNoticeType',
    method: 'get'
  })
}


// 获取通知类型统计 - 别名（兼容旧的调用方式）
export function getNoticeTypeCount(tunnelId) {
  return countTypeByTunnelId(tunnelId)
}
