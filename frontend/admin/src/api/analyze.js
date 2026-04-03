import request from '@/utils/request'

// 获取月度用电/节电数据 - 对接zttunnel后端 /analyze/lightByMonth
export function getLightByMonth(data) {
  return request({
    url: '/analyze/lightByMonth',
    method: 'post',
    data
  })
}

// Dashboard 用电/节电概览（年度+月度）- 对接 /analyze/dashboard/userPowerOverview
export function getUserPowerOverview(data) {
  return request({
    url: '/analyze/dashboard/userPowerOverview',
    method: 'post',
    data
  })
}

// 获取统计分析数据 - 对接zttunnel后端 /analyze/statistics
export function getStatistics(data) {
  return request({
    url: '/analyze/statistics',
    method: 'post',
    data
  })
}

// 获取车流车速数据 - 对接zttunnel后端 /analyze/trafficFlowOrSpeed
export function getTrafficFlow(data) {
  return request({
    url: '/analyze/trafficFlowOrSpeed',
    method: 'post',
    data
  })
}

// 获取车流车速数据（按小时统计）- 对接zttunnel后端 /analyze/trafficFlowOrSpeedByHour
export function getTrafficFlowByHour(data) {
  return request({
    url: '/analyze/trafficFlowOrSpeedByHour',
    method: 'post',
    data
  })
}

// 获取洞内外亮度数据 - 对接zttunnel后端 /analyze/insideAndOutside
export function getInsideOutside(data) {
  return request({
    url: '/analyze/insideAndOutside',
    method: 'post',
    data
  })
}

// 获取洞内外亮度数据（按小时）- 对接zttunnel后端 /analyze/insideAndOutsideByHour
export function getInsideOutsideByHour(data) {
  return request({
    url: '/analyze/insideAndOutsideByHour',
    method: 'post',
    data
  })
}

// 获取能碳数据 - 对接zttunnel后端 /analyze/carbon
export function getCarbon(data) {
  return request({
    url: '/analyze/carbon',
    method: 'post',
    data
  })
}

// 获取能碳数据（按小时）- 对接zttunnel后端 /analyze/carbonByHour
export function getCarbonByHour(data) {
  return request({
    url: '/analyze/carbonByHour',
    method: 'post',
    data
  })
}

// 获取碳排放量统计 - 对接zttunnel后端 /analyze/getCarbonByStatistics
export function getCarbonByStatistics(data) {
  return request({
    url: '/analyze/getCarbonByStatistics',
    method: 'post',
    data
  })
}

// 获取实时亮度数据（照度对比--小时统计）- 对接zttunnel后端 /analyze/zdByHouse
export function getRealTimeBrightness(data) {
  return request({
    url: '/analyze/zdByHouse',
    method: 'post',
    data
  })
}

// 获取实时车流量数据（小时统计）- 对接zttunnel后端 /analyze/clByHouse
export function getRealTimeTraffic(data) {
  return request({
    url: '/analyze/clByHouse',
    method: 'post',
    data
  })
}

// 获取实时车速数据（小时统计）- 对接zttunnel后端 /analyze/csByHouse
export function getRealTimeSpeed(data) {
  return request({
    url: '/analyze/csByHouse',
    method: 'post',
    data
  })
}

// 获取实时电能数据（小时统计）- 对接zttunnel后端 /analyze/dnByHouse
export function getRealTimeEnergy(data) {
  return request({
    url: '/analyze/dnByHouse',
    method: 'post',
    data
  })
}

// 获取当前调光模式 - 对接zttunnel后端 /analyze/get/current/model
export function getCurrentModel(tunnelId) {
  return request({
    url: '/analyze/get/current/model',
    method: 'get',
    params: { tunnelId }
  })
}

// 设置调光模式 - 对接zttunnel后端 /analyze/model
export function setModel(data) {
  return request({
    url: '/analyze/model',
    method: 'post',
    data
  })
}


// ========== 别名函数（兼容旧的调用方式）==========

// 获取车流量数据（小时级）- 别名
export function getTrafficByHour(data) {
  return getRealTimeTraffic(data)
}

// 获取车速数据（小时级）- 别名
export function getSpeedByHour(data) {
  return getRealTimeSpeed(data)
}

// 获取亮度数据（小时级）- 别名
export function getBrightnessByHour(data) {
  return getRealTimeBrightness(data)
}
