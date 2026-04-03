/** 年度数据概览项 */
export interface OverviewItem {
  label: string
  value: string
  unit: string
  icon: string
}

/** 隧道概况项 */
export interface TunnelSummaryItem {
  label: string
  unit: string
  value: string
  icon: string
}

/** 月度用电/节电数据 */
export interface MonthlyData {
  month: number
  power: number
  saving: number
}

/** 年度数据概览 */
export const overviewData: OverviewItem[] = [
  { label: '年度总耗电量', value: '9.6', unit: '万kwh', icon: 'Group147.png' },
  { label: '年总理论节电量', value: '2235.6', unit: '万kwh', icon: 'Group148.png' },
  { label: '年总理论碳减排率', value: '100', unit: '%', icon: 'Group149.png' },
  { label: '年总理论节电率', value: '100', unit: '%', icon: 'Group150.png' },
]

/** 隧道概况 */
export const tunnelSummaryData: TunnelSummaryItem[] = [
  { label: '高速公路', unit: '(条)', value: '6', icon: 'Group151.png' },
  { label: '隧道总数', unit: '(条)', value: '21', icon: 'Group152.png' },
  { label: '总里程', unit: '(km)', value: '72.4', icon: 'Group153.png' },
]

/** 节能效率 */
export const efficiencyData = {
  powerRate: 99.6,
  carbonRate: 99.6,
  year: 2026,
}

/** 月度用电/节电数据 */
export const monthlyData: MonthlyData[] = [
  { month: 1, power: 8.2, saving: 165.3 },
  { month: 2, power: 9.1, saving: 172.8 },
  { month: 3, power: 10.0, saving: 180.7 },
  { month: 4, power: 11.3, saving: 188.2 },
  { month: 5, power: 9.8, saving: 175.5 },
  { month: 6, power: 10.5, saving: 182.1 },
  { month: 7, power: 12.1, saving: 195.4 },
  { month: 8, power: 10.8, saving: 178.9 },
  { month: 9, power: 9.5, saving: 170.2 },
  { month: 10, power: 11.0, saving: 185.6 },
  { month: 11, power: 10.2, saving: 179.3 },
  { month: 12, power: 10.0, saving: 180.7 },
]

export const MAX_POWER = 20
export const MAX_SAVING = 250
export const BAR_SEGMENTS = 48
