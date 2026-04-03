/**
 * 数据看板 - 数据结构定义与占位数据
 */

export interface DataScreenInfo {
  infoBoxes: Array<{ value: string; label: string; icon: number; warn: boolean }>
  energyRank: Array<{
    rank: number
    month: string
    consumptionPercent: number
    savingPercent: number
    value: string
    type: 'orange' | 'yellow' | 'green'
  }>
  monthlySaving: Array<{ rank: number; month: string; percent: number; value: string; type: 'orange' | 'yellow' | 'green' }>
  efficiency: Array<{ value: string; label: string; type: 'carbon' | 'light' | 'power' }>
  deviceStatus: { total: number; online: number; offline: number; fault?: number }
  /** 多维度分析雷达图：6 维 [设备在线率, 覆盖率, 运行效率, 系统稳定性, 碳减排, 节能效率]，0-100 */
  radarData: number[]
  /** 近4月用电/节电对比：labels 如 ["1月","2月"]，consumption/saving 为 kWh */
  statsChart: { labels: string[]; consumption: number[]; saving: number[] }
}

/** 未对接接口的占位数据 */
export const defaultScreenData: DataScreenInfo = {
  infoBoxes: [],
  energyRank: [],
  monthlySaving: [],
  efficiency: [],
  deviceStatus: { total: 0, online: 0, offline: 0, fault: 0 },
  radarData: [0, 0, 0, 0, 0, 0],
  statsChart: { labels: [], consumption: [], saving: [] },
}
