import request from '@/utils/request'

// 绑定列表：9916 占位边缘控制器 GET /tunnel/bind/placeholder-edge/list
export function getPlaceholderEdgeBindList() {
  return request({
    url: '/tunnel/bind/placeholder-edge/list',
    method: 'get'
  })
}

// 占位 devicelist（设备号十进制以 9916 开头）关联的四级 tunnel_id GET /tunnel/bind/placeholder-tunnel-ids
export function getPlaceholderTunnelIds() {
  return request({
    url: '/tunnel/bind/placeholder-tunnel-ids',
    method: 'get'
  })
}

// 公司列表（level=1）GET /tunnel/company/list
export function getTunnelCompanyList() {
  return request({
    url: '/tunnel/company/list',
    method: 'get'
  })
}

// 新增公司 POST /tunnel/company
export function addTunnelCompany(data) {
  return request({
    url: '/tunnel/company',
    method: 'post',
    data
  })
}

// 获取隧道树（用户权限）- 对接zttunnel后端 /tunnel/tree/list
export function getTunnelTree() {
  return request({
    url: '/tunnel/tree/list',
    method: 'get'
  })
}

// 获取所有隧道树 - 对接zttunnel后端 /tunnel/tree/all/list
export function getAllTunnelTree() {
  return request({
    url: '/tunnel/tree/all/list',
    method: 'get'
  })
}

// 根据父级ID获取子隧道 - 对接zttunnel后端 /tunnel/highroad/tunnel
export function getByParentId(parentId) {
  return request({
    url: '/tunnel/highroad/tunnel',
    method: 'get',
    params: { parentId }
  })
}

// 获取隧道经纬度 - 对接zttunnel后端 /tunnel/longitudeLatitude
export function getLongitudeLatitude(tunnelId, isDown) {
  return request({
    url: '/tunnel/longitudeLatitude',
    method: 'get',
    params: { tunnelId, isDown }
  })
}

// 分页查询隧道信息 - 对接zttunnel后端 /tunnel/tunnel/info
export function getTunnelList(data, params) {
  return request({
    url: '/tunnel/tunnel/info',
    method: 'post',
    data,
    params
  })
}

// 通过id查看隧道信息和设备信息 - 对接zttunnel后端 /tunnel/tunnel/device/infoById
export function getTunnelParamInfo(tunnelId) {
  return request({
    url: '/tunnel/tunnel/device/infoById',
    method: 'get',
    params: { id: tunnelId }
  })
}

// 编辑线路和设备信息 - 对接zttunnel后端 /tunnel/update/tunnel/device/infoById
export function updateTunnelParamInfo(data) {
  return request({
    url: '/tunnel/update/tunnel/device/infoById',
    method: 'post',
    data: data
  })
}

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

// 重新计算隧道里程（如果后端有此接口）
export function recalculateTunnelMileage() {
  return request({
    url: '/tunnel/recalculate/mileage',
    method: 'post'
  })
}


// ========== 隧道增删改 ==========

// 新增隧道子节点 — POST /tunnel/add（TunnelNameController）
export function addTunnel(data) {
  return request({
    url: '/tunnel/add',
    method: 'post',
    data
  })
}

// 更新隧道树节点 — POST /tunnel/update
export function updateTunnel(data) {
  return request({
    url: '/tunnel/update',
    method: 'post',
    data
  })
}

// 删除隧道 - 需要在后端确认接口
export function deleteTunnel(id) {
  return request({
    url: `/tunnel/delete/${id}`,
    method: 'delete'
  })
}

// 复制隧道群（level=3）及左右线、设备占位 — POST /tunnel/copy/tunnelGroup
export function copyTunnelGroup(data) {
  return request({
    url: '/tunnel/copy/tunnelGroup',
    method: 'post',
    data,
    timeout: 120000
  })
}

// Excel 批量新建隧道（对齐 zt_tunnel_web）：POST /easyExcel/excelInput，表单字段名 file
export function importTunnelExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/easyExcel/excelInput',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000
  })
}
