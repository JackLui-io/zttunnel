import request from './request'
import type { ApiResponse } from './request'

/** 统计分析查询参数（与原前端 form 一致） */
export interface SaQueryForm {
  startTime: string | null
  endTime: string | null
  tunnelId: number | null
}

/** 总体分析 StatisticsVo（与后端 StatisticsVo 字段一致，BigDecimal 可能序列化为 number） */
export interface StatisticsVo {
  actualPowerConsumption?: number
  actualUnitPowerConsumption?: number
  actualOperatingPower?: number
  actualLightUpTime?: number
  actualCarbonEmission?: number
  originalPowerConsumption?: number
  originalUnitPowerConsumption?: number
  originalOperatingPower?: number
  originalLightUpTime?: number
  originalCarbonEmission?: number
  theoreticalPowerSavingRate?: number
  theoreticalTotalPowerReduction?: number
  theoreticalOperatingPowerReduction?: number
  theoreticalLightUpTimeReduction?: number
  theoreticalCarbonEmissionReduction?: number
}

/** Dashboard 用户用电/节电概览（当前用户已分配隧道的年度+月度） */
export interface DashboardPowerOverviewVo {
  annualOverview?: {
    totalConsumption?: number
    totalSaving?: number
    powerSavingRate?: number
    carbonReductionRate?: number
  }
  monthlyData?: Array<{ month?: number; consumption?: number; saving?: number; lightUpReductionRate?: number }>
}

export function getUserPowerOverview(year: number): Promise<DashboardPowerOverviewVo | null> {
  return request
    .post('/analyze/dashboard/userPowerOverview', { year: String(year) })
    .then((res) => extractData<DashboardPowerOverviewVo | null>(res, null))
}

/** Dashboard 隧道概况 */
export interface DashboardTunnelOverviewVo {
  highwayCount?: number
  tunnelCount?: number
  totalMileage?: number
}

export function getTunnelOverview(): Promise<DashboardTunnelOverviewVo | null> {
  return request
    .get('/analyze/dashboard/tunnelOverview')
    .then((res) => extractData<DashboardTunnelOverviewVo | null>(res, null))
}

/** Dashboard 设备状态分布（total、online、offline、fault 对应设备总数、在线、离线、故障） */
export interface DashboardDeviceStatusVo {
  total?: number
  online?: number
  offline?: number
  fault?: number
  onlinePercent?: number
  offlinePercent?: number
}

export function getDeviceStatusDistribution(): Promise<DashboardDeviceStatusVo | null> {
  return request
    .get('/analyze/dashboard/deviceStatusDistribution')
    .then((res) => extractData<DashboardDeviceStatusVo | null>(res, null))
}

/** Dashboard 今日数据汇总（今日用电量、今日节电量、今日碳减排） */
export interface DashboardTodayPowerVo {
  todayConsumptionKwh?: number
  todaySavingKwh?: number
  todayCarbonReductionKg?: number
}

export function getTodayPowerSummary(): Promise<DashboardTodayPowerVo | null> {
  return request
    .get('/analyze/dashboard/todayPowerSummary')
    .then((res) => extractData<DashboardTodayPowerVo | null>(res, null))
}

/** Dashboard 本月理论亮灯时长削减率（用户管理所有隧道汇总） */
export interface DashboardLightUpReductionVo {
  lightUpReductionRate?: number
}

export function getLightUpReductionRate(): Promise<DashboardLightUpReductionVo | null> {
  return request
    .get('/analyze/dashboard/lightUpReductionRate')
    .then((res) => extractData<DashboardLightUpReductionVo | null>(res, null))
}

/** Dashboard 待处理统计（RightSidebar 待处理卡片） */
export interface DashboardPendingCountsVo {
  systemNotice?: number
  operationNotice?: number
  realtimeAlarm?: number
  deviceAlarm?: number
}

export function getPendingCounts(): Promise<DashboardPendingCountsVo | null> {
  return request
    .get('/analyze/dashboard/pendingCounts')
    .then((res) => extractData<DashboardPendingCountsVo | null>(res, null))
}

/** Dashboard 消息通知（RightSidebar 消息通知卡片） */
export interface DashboardMessageItemVo {
  time?: string
  text?: string
  type?: number
}

export interface DashboardMessageCategoryVo {
  name?: string
  count?: number
  items?: DashboardMessageItemVo[]
}

export function getMessageNotifications(limit?: number): Promise<DashboardMessageCategoryVo[]> {
  const params = limit != null ? { limit } : {}
  return request
    .get('/analyze/dashboard/messageNotifications', { params })
    .then((res) => {
      const list = extractData<DashboardMessageCategoryVo[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 总体分析 - tunnelId 为 null 时后端按当前用户权限返回其可见所有隧道的汇总数据 */
export function getAnalyzeStatistics(form: SaQueryForm): Promise<StatisticsVo | null> {
  return request
    .post('/analyze/statistics', {
      startTime: form.startTime,
      endTime: form.endTime,
      tunnelId: form.tunnelId,
    })
    .then((res) => {
      const list = extractData<StatisticsVo[]>(res, [])
      const arr = Array.isArray(list) ? list : []
      return arr[0] ?? null
    })
}

/** 总体分析导出 - 与原前端 statisticsExport 一致 */
export function statisticsExport(form: SaQueryForm, fileName: string): Promise<void> {
  return downloadBlob('/analyze/statisticsExport', form, fileName)
}

/** 车流/车速 - 与原前端 getAnalyzeTrafficFlowOrSpeed 一致 */
export interface TrafficFlowOrSpeedVo {
  uploadTime?: string
  trafficFlow?: number
  maxTrafficFlow?: number
  minTrafficFlow?: number
  avgTrafficFlow?: number
  maxSpeed?: number
  minSpeed?: number
  avgSpeed?: number
}

/** 与原前端 getAnalyzeTrafficFlowOrSpeed 一致：POST /analyze/trafficFlowOrSpeed */
export function getTrafficFlowOrSpeed(form: SaQueryForm): Promise<TrafficFlowOrSpeedVo[]> {
  if (form.tunnelId == null) return Promise.resolve([])
  return request
    .post('/analyze/trafficFlowOrSpeed', {
      startTime: form.startTime,
      endTime: form.endTime,
      tunnelId: form.tunnelId,
    })
    .then((res) => {
      const list = extractData<TrafficFlowOrSpeedVo[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 与原前端 getAnalyzeTrafficFlowOrSpeed 同名导出 */
export const getAnalyzeTrafficFlowOrSpeed = getTrafficFlowOrSpeed

/** 车流车速导出（与原前端 trafficFlowOrSpeedExport 一致） */
export function trafficFlowOrSpeedExport(form: SaQueryForm, fileName: string): Promise<void> {
  return downloadBlob('/analyze/trafficFlowOrSpeedExport', form, fileName)
}

/** 车流车速列表导出（与原前端 trafficFlowOrSpeedListExport 一致） */
export function trafficFlowOrSpeedListExport(form: SaQueryForm, fileName: string): Promise<void> {
  return downloadBlob('/analyze/trafficFlowOrSpeedListExport', form, fileName)
}

/** 洞内外亮度 - 与原前端 getAnalyzeInsideAndOutside 一致 */
export interface InsideAndOutsideVo {
  uploadTime?: string
  maxOutside?: number
  minOutside?: number
  avgOutside?: number
  maxDimmingRadio?: number
  minDimmingRadio?: number
  avgDimmingRadio?: number
  lightRadio?: string
  lightUp?: number
  lightDown?: number
}

/** 与原前端 getAnalyzeInsideAndOutside 一致：POST /analyze/insideAndOutside */
export function getAnalyzeInsideAndOutside(form: SaQueryForm): Promise<InsideAndOutsideVo[]> {
  if (form.tunnelId == null) return Promise.resolve([])
  return request
    .post('/analyze/insideAndOutside', {
      startTime: form.startTime,
      endTime: form.endTime,
      tunnelId: form.tunnelId,
    })
    .then((res) => {
      const list = extractData<InsideAndOutsideVo[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 洞内外亮度导出（与原前端 insideAndOutsideExport 一致） */
export function insideAndOutsideExport(form: SaQueryForm, fileName: string): Promise<void> {
  return downloadBlob('/analyze/insideAndOutsideExport', form, fileName)
}

/** 洞内外亮度列表导出（与原前端 insideAndOutsideListExport 一致） */
export function insideAndOutsideListExport(form: SaQueryForm, fileName: string): Promise<void> {
  return downloadBlob('/analyze/insideAndOutsideListExport', form, fileName)
}

/** 能碳数据 - 与原前端 getAnalyzeCarbon 一致 */
export interface MeterReadingVo {
  loopName?: string
  dataValue?: number | string
}

export interface CarbonVo {
  uploadTime?: string
  dailyPowerConsumption?: number
  cumulativePowerConsumption?: number
  theoreticalPowerSavings?: number
  theoreticalPowerSavingRate?: number
  meterReadingVos?: MeterReadingVo[]
  [key: string]: unknown
}

/** 与原前端 getAnalyzeCarbon 一致：POST /analyze/carbon */
export function getAnalyzeCarbon(form: SaQueryForm): Promise<CarbonVo[]> {
  if (form.tunnelId == null) return Promise.resolve([])
  return request
    .post('/analyze/carbon', {
      startTime: form.startTime,
      endTime: form.endTime,
      tunnelId: form.tunnelId,
    })
    .then((res) => {
      const list = extractData<CarbonVo[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 碳排放统计 - 与原前端 getCarbonByStatistics 一致 */
export interface CarbonByStatisticsVo {
  uploadTime?: string
  actualCarbonEmission?: number
  originalCarbonEmission?: number
}

/** 与原前端 getCarbonByStatistics 一致：POST /analyze/getCarbonByStatistics */
export function getCarbonByStatistics(form: SaQueryForm): Promise<CarbonByStatisticsVo[]> {
  if (form.tunnelId == null) return Promise.resolve([])
  return request
    .post('/analyze/getCarbonByStatistics', {
      startTime: form.startTime,
      endTime: form.endTime,
      tunnelId: form.tunnelId,
    })
    .then((res) => {
      const list = extractData<CarbonByStatisticsVo[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 能碳数据导出（与原前端 carbonExport 一致） */
export function carbonExport(form: SaQueryForm, fileName: string): Promise<void> {
  return downloadBlob('/analyze/carbonExport', form, fileName)
}

/** 月度用电/节电 - 与原前端 getLightByMonth 一致，首页接口 */
export interface PowerLightVo {
  month?: number
  totalLight?: number
  totalEconomyLight?: number
  theoreticalCarbonEmissionReduction?: number
}

/** 与原前端 getLightByMonth 一致：tunnelId、year（number，如 2026） */
export function getLightByMonth(tunnelId: number, year: number): Promise<PowerLightVo[]> {
  const body = { tunnelId, year }
  if (import.meta.env.DEV) console.debug('[lightByMonth] 请求参数:', body)
  return request
    .post('/analyze/lightByMonth', body)
    .then((res) => {
      const list = extractData<PowerLightVo[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 通用 blob 下载（form-urlencoded，与原前端 AutoeeUtil.download 一致） */
function downloadBlob(url: string, params: SaQueryForm, fileName: string): Promise<void> {
  const qs = new URLSearchParams()
  if (params.startTime) qs.append('startTime', params.startTime)
  if (params.endTime) qs.append('endTime', params.endTime)
  if (params.tunnelId != null) qs.append('tunnelId', String(params.tunnelId))
  return request
    .post(url, qs.toString(), {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      responseType: 'blob',
      transformRequest: [(data: unknown) => data],
    })
    .then((res) => {
      const blob = res.data instanceof Blob ? res.data : new Blob([res.data as BlobPart])
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = fileName
      a.click()
      URL.revokeObjectURL(a.href)
    })
}

/** 当前模式（入口/出口里程等） */
export interface CurrentModel {
  id?: number
  tunnelId?: number
  mode?: number
  inMileageNum?: number
  outMileageNum?: number
  [key: string]: unknown
}

/** 亮度数据项 */
export interface ZdByHouseItem {
  dimmingRadio?: number
  outside?: number
  hour?: string
}

/** 车流量数据项 */
export interface ClByHouseItem {
  totalFlow?: number
  trafficFlow?: number
  hour?: number
}

/** 车速数据项（后端 avgSpeed 为 String） */
export interface CsByHouseItem {
  avgSpeed?: number | string
  hour?: string
}

/** 电能数据项（后端 powerConsumption、theoreticalPowerSavings 为 String） */
export interface DnByHouseItem {
  powerConsumption?: number | string
  theoreticalPowerSavings?: number | string
  hour?: number
}

/** 提取后端 data 字段（兼容 code/msg/data 及直接返回 data 的情况） */
function extractData<T>(res: { data?: unknown }, fallback: T): T {
  const body = res.data as Record<string, unknown> | undefined
  if (!body) return fallback
  if (Array.isArray(body)) return body as unknown as T
  if (body.data !== undefined && body.data !== null) return body.data as T
  if (body.code === 200 || body.code === 0 || body.code === undefined) return body as unknown as T
  return fallback
}

/** 获取当前模式（含入口/出口里程） */
export function getCurrentModel(tunnelId: number) {
  return request
    .get('/analyze/get/current/model', { params: { tunnelId } })
    .then((res) => extractData(res, null) as CurrentModel | null)
}

/** 亮度对比 - 调光比例、洞外亮度 */
export function getZdByHouse(tunnelId: number, time?: string) {
  const t = time ?? formatDate(new Date())
  return request
    .post('/analyze/zdByHouse', { tunnelId, time: t })
    .then((res) => {
      const list = extractData<ZdByHouseItem[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 车流量统计 */
export function getClByHouse(tunnelId: number, time?: string) {
  const t = time ?? formatDate(new Date())
  return request
    .post('/analyze/clByHouse', { tunnelId, time: t })
    .then((res) => {
      const list = extractData<ClByHouseItem[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 平均车速 */
export function getCsByHouse(tunnelId: number, time?: string) {
  const t = time ?? formatDate(new Date())
  return request
    .post('/analyze/csByHouse', { tunnelId, time: t })
    .then((res) => {
      const list = extractData<CsByHouseItem[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

/** 每小时电能参数 */
export function getDnByHouse(tunnelId: number, time?: string) {
  const t = time ?? formatDate(new Date())
  return request
    .post('/analyze/dnByHouse', { tunnelId, time: t })
    .then((res) => {
      const list = extractData<DnByHouseItem[]>(res, [])
      return Array.isArray(list) ? list : []
    })
}

function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 里程转 328+296 格式（参考 zt_tunnel_web decToHex） */
export function decToHexMileage(val: number | undefined): string {
  if (val == null) return '--'
  const hex = val.toString(16)
  let big: string
  let small: string
  if (hex.length >= 5) {
    big = hex.slice(0, hex.length - 4)
    small = hex.slice(-4)
  } else {
    big = '000'
    small = hex
  }
  const bigNum = parseInt(big, 16)
  const smallNum = parseInt(small, 16)
  return `${bigNum}+${smallNum}`
}

/**
 * 后端里程整数转可比较数值（参考 zt_tunnel_web tunnelDh decToHex）
 * 用于灯具位置比例计算：deviceNum、inMileageNum、outMileageNum 需统一转换后再算 ratio
 * 后端格式：高4位+低4位 hex 拼接，如 "0148"+"0128" -> 0x1480128
 */
export function decToHexNum(val: number | undefined): number {
  if (val == null || typeof val !== 'number') return 0
  const hex = val.toString(16).padStart(8, '0')
  if (hex.length >= 8) {
    const big = hex.slice(0, 4)
    const small = hex.slice(4)
    const b = parseInt(big, 16)
    const m = parseInt(small, 16)
    if (Number.isNaN(b) || Number.isNaN(m)) return 0
    return b * 1000 + m
  }
  const big = hex.slice(0, 3) || '0'
  const min = hex.length >= 5 ? hex.slice(4) : '0'
  const b = parseInt(big, 16)
  const m = parseInt(min, 16)
  if (Number.isNaN(b) || Number.isNaN(m)) return 0
  return b * 1000 + m
}
