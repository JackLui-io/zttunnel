<template>
  <div class="card card-rt card-rt-speed">
    <div class="card-rt-header">
      <img src="/page1/Group117.png" alt="" class="card-rt-header-bg" />
      <span class="card-rt-header-title">平均车速</span>
    </div>
    <div class="card-rt-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getCsByHouse } from '@/api/analyze'

const emptyChartData = () => ({ timeLabels: [] as string[], speedData: [] as number[] })

const props = defineProps<{ tunnelId?: number }>()
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const chartData = ref(emptyChartData())

function updateChart() {
  ensureChartAndSetOption()
}

function ensureChartAndSetOption() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
    window.addEventListener('resize', handleResize)
  }
  const d = chartData.value
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', crossStyle: { color: '#999' } },
      backgroundColor: 'rgba(15, 127, 98, 0.95)',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    grid: { left: 45, right: 45, top: 35, bottom: 30, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: d.timeLabels,
      axisLine: { lineStyle: { color: '#B8E0C8' } },
      axisLabel: { color: '#878D8E', fontSize: 12, interval: 'auto' },
    },
    yAxis: {
      type: 'value',
      name: 'km/h',
      min: 0,
      axisLine: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: 'rgba(184, 224, 200, 0.6)' } },
      axisLabel: { color: '#878D8E', fontSize: 12 },
      nameTextStyle: { color: '#878D8E', fontSize: 12 },
    },
    series: [
      {
        name: '实时车速',
        type: 'line',
        data: d.speedData,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(145, 202, 167, 0.8)' },
              { offset: 0.5, color: 'rgba(187, 223, 159, 0.8)' },
              { offset: 1, color: 'rgba(222, 240, 214, 0.8)' },
            ],
          },
        },
        emphasis: { focus: 'series' },
        lineStyle: { color: '#91CAA7' },
        itemStyle: { color: '#91CAA7' },
      },
    ],
  })
  chart.resize()
}

async function fetchData() {
  if (props.tunnelId == null) {
    chartData.value = emptyChartData()
    updateChart()
    return
  }
  try {
    const list = await getCsByHouse(props.tunnelId)
    const speedData: number[] = []
    const timeLabels: string[] = []
    list.forEach((item) => {
      speedData.push(Number(item.avgSpeed) || 0)
      if (item.hour != null && item.hour !== '') {
        const arr = String(item.hour).split(':')
        if (arr[0] != null && arr[0].length < 2) arr[0] = ('0' + arr[0]).slice(-2)
        if (arr[1] != null && arr[1].length < 2) arr[1] = ('0' + arr[1]).slice(-2)
        timeLabels.push(arr.join(':'))
      } else {
        timeLabels.push('')
      }
    })
    const hasData = list.length > 0
    chartData.value = {
      timeLabels: hasData && timeLabels.length ? timeLabels : [],
      speedData: hasData && speedData.length ? speedData : [],
    }
  } catch {
    chartData.value = emptyChartData()
  }
  updateChart()
}

function handleResize() {
  chart?.resize()
}

let refreshTimer: ReturnType<typeof setInterval> | null = null
watch(
  () => props.tunnelId,
  (tid) => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
    fetchData()
    if (tid != null) {
      refreshTimer = setInterval(fetchData, 60000)
    }
  },
  { immediate: true }
)

onMounted(() => {
  nextTick(ensureChartAndSetOption)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.card-rt {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: visible;
  padding: 0;
  border: none;
  background: transparent;
  width: 100%;
  height: 100%;
  min-height: 0;
}
.card-rt-header {
  position: relative;
  width: 100%;
  z-index: 1;
  height: 49px;
  flex-shrink: 0;
  overflow: hidden;
}
.card-rt-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-rt-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  color: #0f7f62;
}
.card-rt-body {
  position: relative;
  flex: 1;
  min-height: 200px;
  padding: 12px 16px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 4px 8px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: visible;
  display: flex;
  flex-direction: column;
}
.chart-container {
  flex: 1;
  width: 100%;
  min-height: 180px;
}
</style>
