import request from '@/utils/request'

/**
 * 数据汇总定时任务
 * 后端需提供：
 * - GET  /analyze/scheduler/status  获取各任务开关状态
 * - POST /analyze/scheduler/update  更新任务开关
 * - POST /analyze/scheduler/xxx     手动执行任务
 */

// 获取各任务开关状态
export function getSchedulerStatus() {
  return request({
    url: '/analyze/scheduler/status',
    method: 'get'
  })
}

// 更新任务开关状态
export function updateSchedulerStatus(taskKey, enabled) {
  return request({
    url: '/analyze/scheduler/update',
    method: 'post',
    data: { taskKey, enabled }
  })
}

// 同步车流/车速到 tunnel_traffic_flow_day
export function runSyncTrafficFlowDay() {
  return request({
    url: '/analyze/scheduler/syncTrafficFlowDay',
    method: 'post',
    timeout: 120000
  })
}

// 同步洞内外亮度到 tunnel_inside_outside_day
export function runSyncInsideOutsideDay() {
  return request({
    url: '/analyze/scheduler/syncInsideOutsideDay',
    method: 'post',
    timeout: 120000
  })
}

// 同步能碳数据到 tunnel_carbon_day
export function runSyncCarbonDay() {
  return request({
    url: '/analyze/scheduler/syncCarbonDay',
    method: 'post',
    timeout: 120000
  })
}

// 检查电表数据并补录到 tunnel_power_data
export function runCheckPowerData() {
  return request({
    url: '/analyze/scheduler/checkPowerData',
    method: 'post',
    timeout: 120000
  })
}
