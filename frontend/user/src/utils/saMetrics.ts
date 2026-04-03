/**
 * StatisticsVo 到 CardSaMetric 的映射 - 与原前端 table1.vue 计算逻辑完全一致
 */

import type { StatisticsVo } from '@/api/analyze'

export interface SaMetricRow {
  label: string
  data?: string
  val?: string
  barWidth?: number
}

export interface SaMetric {
  icon: string
  value: string
  title: string
  rows: SaMetricRow[]
}

const ICONS = {
  power: '/page1/Exclude1.png',
  powerReduction: '/page1/Exclude2.png',
  carbon: '/page1/Exclude3.png',
  lightTime: '/page1/Exclude4.png',
}

function num(v: number | undefined | null): number {
  return v != null && !Number.isNaN(Number(v)) ? Number(v) : 0
}

/** 空占位 metric */
function emptyMetric(icon: string, title: string): SaMetric {
  return {
    icon,
    value: '--',
    title,
    rows: [
      { label: '--', data: '--' },
      { label: '--', data: '--' },
      { label: '--', val: '--', barWidth: 0 },
    ],
  }
}

/** 与原前端 table1 一致：4 个 KPI 卡片的映射 */
export function statisticsVoToMetrics(vo: StatisticsVo | null): [SaMetric, SaMetric, SaMetric, SaMetric] {
  if (!vo) {
    return [
      emptyMetric(ICONS.power, '理论节电率'),
      emptyMetric(ICONS.powerReduction, '理论总功率削减'),
      emptyMetric(ICONS.carbon, '理论碳减排率'),
      emptyMetric(ICONS.lightTime, '理论亮灯时间削减'),
    ]
  }

  const actualPower = num(vo.actualPowerConsumption)
  const originalPower = num(vo.originalPowerConsumption)
  const rate1 = num(vo.theoreticalPowerSavingRate)
  const theoryPowerSave = originalPower - actualPower

  const actualOp = num(vo.actualOperatingPower)
  const originalOp = num(vo.originalOperatingPower)
  const rate2 = num(vo.theoreticalTotalPowerReduction)
  const theoryOpReduction = num(vo.theoreticalOperatingPowerReduction)

  const actualCarbon = num(vo.actualCarbonEmission)
  const originalCarbon = num(vo.originalCarbonEmission)
  const rate3 = num(vo.theoreticalCarbonEmissionReduction)
  const theoryCarbonSave = originalCarbon - actualCarbon

  const actualLight = num(vo.actualLightUpTime)
  const originalLight = num(vo.originalLightUpTime)
  const rate4 = num(vo.theoreticalLightUpTimeReduction)
  const theoryLightSave = originalLight - actualLight

  return [
    {
      icon: ICONS.power,
      value: rate1 > 0 ? `${rate1}%` : '--',
      title: '理论节电率',
      rows: [
        { label: '实际耗电量', data: `${actualPower}kwh` },
        { label: '原设计耗电量', data: `${originalPower}kwh` },
        {
          label: '理论节电',
          val: `${theoryPowerSave.toFixed(2)}kwh`,
          barWidth: Math.min(100, Math.max(0, rate1)),
        },
      ],
    },
    {
      icon: ICONS.powerReduction,
      value: rate2 > 0 ? `${rate2}%` : '--',
      title: '理论总功率削减',
      rows: [
        { label: '实际运行功率', data: `${actualOp}kw` },
        { label: '设计运行功率', data: `${originalOp}kw` },
        {
          label: '理论削减功率',
          val: `${theoryOpReduction}kw`,
          barWidth: Math.min(100, Math.max(0, rate2)),
        },
      ],
    },
    {
      icon: ICONS.carbon,
      value: rate3 > 0 ? `${rate3}%` : '--',
      title: '理论碳减排率',
      rows: [
        { label: '实际碳排放量', data: `${actualCarbon}tCO₂` },
        { label: '原设计碳排放量', data: `${originalCarbon}tCO₂` },
        {
          label: '理论碳减排',
          val: `${theoryCarbonSave.toFixed(2)}tCO₂`,
          barWidth: Math.min(100, Math.max(0, rate3)),
        },
      ],
    },
    {
      icon: ICONS.lightTime,
      value: rate4 > 0 ? `${rate4}%` : '--',
      title: '理论亮灯时间削减',
      rows: [
        { label: '实际亮灯时间', data: `${actualLight}h` },
        { label: '原设计亮灯时间', data: `${originalLight}h` },
        {
          label: '理论亮灯时间削减',
          val: `${theoryLightSave.toFixed(2)}h`,
          barWidth: Math.min(100, Math.max(0, rate4)),
        },
      ],
    },
  ]
}
