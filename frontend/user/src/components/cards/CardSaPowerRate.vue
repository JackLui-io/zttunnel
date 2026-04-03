<template>
  <div class="card-sa card-sa-power-rate">
    <div class="card-sa-row3-header">
      <img src="/page1/Group71.png" alt="" class="card-sa-row3-header-bg" />
      <span class="card-sa-row3-header-title">耗电量与节电率</span>
    </div>
    <div class="card-sa-body">
      <div ref="chartRef" class="sa-chart-power-rate"></div>
      <div v-if="(props.data ?? []).length === 0" class="sa-chart-empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import type { CarbonVo } from '@/api/analyze'

const props = defineProps<{
  data: CarbonVo[]
}>()

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const list = props.data ?? []
  if (list.length === 0) {
    chart.clear()
    return
  }
  const reversed = [...list].reverse()
  const xAxis = reversed.map((i) => i.uploadTime ?? '')
  const dailyData = reversed.map((i) => i.dailyPowerConsumption ?? 0)
  const savingData = reversed.map((i) => i.theoreticalPowerSavings ?? 0)
  const rateData = reversed.map((i) => i.theoreticalPowerSavingRate ?? 0)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', crossStyle: { color: '#999' } },
      backgroundColor: 'rgba(15, 127, 98, 0.95)',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    legend: {
      data: ['耗电量', '理论节电量', '理论节电率'],
      top: 0,
      textStyle: { fontSize: 12, color: '#5F646B' },
      itemWidth: 12,
      itemHeight: 8,
    },
    grid: { left: 50, right: 45, top: 40, bottom: 35 },
    xAxis: {
      type: 'category',
      boundaryGap: true,
      data: xAxis,
      axisLine: { lineStyle: { color: '#B8E0C8' } },
      axisLabel: { color: '#5F646B', fontSize: 12 },
    },
    yAxis: [
      {
        type: 'value',
        name: '耗电量(kWh)',
        splitLine: { lineStyle: { type: 'dashed', color: 'rgba(184, 224, 200, 0.6)' } },
        axisLabel: { color: '#5F646B', fontSize: 12 },
        nameTextStyle: { color: '#5F646B', fontSize: 12 },
      },
      {
        type: 'value',
        name: '理论节电率',
        min: 0,
        max: 100,
        interval: 20,
        axisLabel: { formatter: '{value} %', color: '#5F646B', fontSize: 12 },
        splitLine: { lineStyle: { type: 'dashed', color: 'transparent' } },
        nameTextStyle: { color: '#5F646B', fontSize: 12 },
      },
    ],
    series: [
      {
        name: '耗电量',
        type: 'bar',
        stack: 'Total1',
        barMaxWidth: 10,
        yAxisIndex: 0,
        emphasis: { focus: 'series' },
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#FCC432' },
              { offset: 1, color: 'rgba(254, 196, 50, 0.4)' },
            ],
          },
        },
        data: dailyData,
      },
      {
        name: '理论节电量',
        type: 'bar',
        stack: 'Total2',
        barMaxWidth: 10,
        yAxisIndex: 0,
        emphasis: { focus: 'series' },
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#91CAA7' },
              { offset: 1, color: 'rgba(145, 202, 167, 0.4)' },
            ],
          },
        },
        data: savingData,
      },
      {
        name: '理论节电率',
        type: 'line',
        yAxisIndex: 1,
        emphasis: { focus: 'series' },
        itemStyle: { color: '#6EB5FF' },
        lineStyle: { color: '#6EB5FF' },
        data: rateData,
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
  left: 58px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  color: #0f7f62;
}
.card-sa-body {
  position: relative;
  flex: 1;
  min-height: 200px;
  padding: 12px 16px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}
.sa-chart-power-rate {
  width: 100%;
  height: 100%;
  min-height: 200px;
}
.sa-chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f646b;
  font-size: 14px;
  pointer-events: none;
}
</style>
