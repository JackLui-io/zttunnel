<template>
  <div class="card-sa card-sa-avg-speed">
    <div class="card-sa-row3-header">
      <img src="/page1/Group71.png" alt="" class="card-sa-row3-header-bg" />
      <span class="card-sa-row3-header-title">平均速度</span>
    </div>
    <div class="card-sa-body">
      <div ref="chartRef" class="sa-chart-speed"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import type { TrafficFlowOrSpeedVo } from '@/api/analyze'

const props = defineProps<{
  data: TrafficFlowOrSpeedVo[]
}>()

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

function renderChart() {
  if (!chart || !chartRef.value) return
  const list = props.data ?? []
  const reversed = [...list].reverse()
  const xAxis = reversed.map((i) => i.uploadTime ?? '')
  const seriesData = reversed.map((i) => (i.avgSpeed ?? 0) as number)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 127, 98, 0.95)',
      borderColor: '#0F7F62',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    grid: { left: 50, right: 30, top: 30, bottom: 35 },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: xAxis,
      axisLine: { lineStyle: { color: '#B8E0C8' } },
      axisLabel: { color: '#5F646B', fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      name: 'km/h',
      nameTextStyle: { color: '#5F646B', fontSize: 12 },
      splitLine: { lineStyle: { type: 'dashed', color: 'rgba(184, 224, 200, 0.6)' } },
      axisLabel: { color: '#5F646B', fontSize: 12 },
    },
    series: [
      {
        name: '行车速度',
        type: 'line',
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(254, 182, 3, 0.6)' },
              { offset: 1, color: 'rgba(254, 182, 3, 0.1)' },
            ],
          },
        },
        lineStyle: { color: '#FEB603' },
        itemStyle: { color: '#FEB603' },
        data: seriesData,
      },
    ],
  })
}

onMounted(() => {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  renderChart()
  window.addEventListener('resize', () => chart?.resize())
})

watch(() => props.data, renderChart, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => chart?.resize())
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
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}
.sa-chart-speed {
  width: 100%;
  height: 100%;
  min-height: 200px;
}
</style>
