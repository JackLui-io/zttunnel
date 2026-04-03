<template>
  <div class="card-sa card-sa-monthly">
    <div class="card-sa-row3-header">
      <img src="/page1/Group70.png" alt="" class="card-sa-row3-header-bg" />
      <span class="card-sa-row3-header-title">月度用电/节电对比</span>
    </div>
    <div class="card-sa-body">
      <div ref="chartRef" class="sa-chart-monthly"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { getLightByMonth } from '@/api/analyze'
import type { PowerLightVo } from '@/api/analyze'

const props = defineProps<{
  tunnelId: number | null
}>()

/** 仅显示今年，与原前端 year 传 number 一致 */
const currentYear = new Date().getFullYear()

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

/** 将接口返回按 1-12 月补齐，缺失月份用 0。当年仅保留 1 月至当前月，未来月份置 0（避免后端可能混入往年数据） */
function padTo12Months(list: PowerLightVo[], year: number): { consumeData: number[]; savingData: number[] } {
  const map = new Map<number, PowerLightVo>()
  for (const item of list) {
    const m = item.month ?? 0
    if (m >= 1 && m <= 12) map.set(m, item)
  }
  const now = new Date()
  const isCurrentYear = year === now.getFullYear()
  const maxMonth = isCurrentYear ? now.getMonth() + 1 : 12
  const consumeData: number[] = []
  const savingData: number[] = []
  for (let m = 1; m <= 12; m++) {
    if (isCurrentYear && m > maxMonth) {
      consumeData.push(0)
      savingData.push(0)
    } else {
      const vo = map.get(m)
      consumeData.push(vo?.totalLight ?? 0)
      savingData.push(vo?.totalEconomyLight ?? 0)
    }
  }
  return { consumeData, savingData }
}

function updateChart(consumeData: number[], savingData: number[]) {
  if (!chart) return
  const maxVal = Math.max(...consumeData, ...savingData, 1)
  const interval = maxVal <= 100 ? 20 : maxVal <= 1000 ? 200 : maxVal <= 10000 ? 2000 : 10000
  const max = Math.ceil(maxVal / interval) * interval || 10000
  chart.setOption({
    xAxis: { data: months },
    yAxis: { max, interval },
    series: [
      { data: consumeData },
      { data: savingData },
    ],
  })
}

async function fetchAndRender() {
  if (props.tunnelId == null || !chartRef.value) return
  try {
    const list = await getLightByMonth(props.tunnelId, currentYear)
    const { consumeData, savingData } = padTo12Months(list, currentYear)
    updateChart(consumeData, savingData)
  } catch {
    updateChart(
      Array(12).fill(0),
      Array(12).fill(0)
    )
  }
}

onMounted(() => {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 127, 98, 0.95)',
      borderColor: '#0F7F62',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    legend: {
      data: ['实际用电量', '节电量'],
      top: 0,
      right: 20,
      textStyle: { fontSize: 12, color: '#5F646B' },
      itemWidth: 10,
      itemHeight: 10,
    },
    grid: { left: 50, right: 40, top: 40, bottom: 35 },
    xAxis: {
      type: 'category',
      data: months,
      axisLine: { lineStyle: { color: '#B8E0C8' } },
      axisLabel: { color: '#5F646B', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      name: 'kWh',
      nameTextStyle: { color: '#5F646B', fontSize: 12 },
      min: 0,
      max: 70000,
      interval: 10000,
      axisLine: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: 'rgba(184, 224, 200, 0.6)' } },
      axisLabel: { color: '#5F646B', fontSize: 12 },
    },
    series: [
      {
        name: '实际用电量',
        type: 'bar',
        data: Array(12).fill(0),
        barWidth: 10,
        barGap: '30%',
        barBorderRadius: [2, 2, 0, 0],
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#FCC432' },
              { offset: 1, color: '#DEF0D6' },
            ],
          },
        },
      },
      {
        name: '节电量',
        type: 'bar',
        data: Array(12).fill(0),
        barWidth: 12,
        barGap: '30%',
        barBorderRadius: [2, 2, 0, 0],
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#91CAA7' },
              { offset: 0.5, color: '#BBDF9F' },
              { offset: 1, color: '#DEF0D6' },
            ],
          },
        },
      },
    ],
  })
  fetchAndRender()
  window.addEventListener('resize', handleResize)
})

watch(() => props.tunnelId, fetchAndRender)

function handleResize() {
  chart?.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.card-sa {
  border: none;
  background: transparent;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: visible;
  width: 100%;
  height: 100%;
  min-height: 0;
}
.card-sa-row3-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
  z-index: 1;
  overflow: hidden;
}
.card-sa-row3-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-sa-row3-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  color: #0f7f62;
}
.card-sa-body {
  flex: 1;
  min-height: 200px;
  padding: 12px 16px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 4px 8px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: visible;
}
.sa-chart-monthly {
  width: 100%;
  height: 100%;
  min-height: 240px;
}
</style>
