<template>
  <div class="card card-rt card-rt-traffic">
    <div class="card-rt-header">
      <img src="/page1/Group115.png" alt="" class="card-rt-header-bg" />
      <span class="card-rt-header-title">车流量统计</span>
    </div>
    <div class="card-rt-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getClByHouse } from '@/api/analyze'

const emptyChartData = () => ({ hours: [] as number[], hourlyData: [] as number[], cumulativeData: [] as number[] })

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
      borderColor: '#0F7F62',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    legend: {
      data: ['每小时车流量', '累计车流量'],
      top: 0,
      textStyle: { fontSize: 12, color: '#878D8E' },
      itemWidth: 10,
      itemHeight: 10,
    },
    grid: { left: 45, right: 45, top: 35, bottom: 30, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: d.hours,
      axisLine: { lineStyle: { color: '#B8E0C8' } },
      axisLabel: { color: '#878D8E', fontSize: 12 },
    },
    yAxis: [
      {
        type: 'value',
        name: '辆/h',
        min: 0,
        axisLine: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: 'rgba(184, 224, 200, 0.6)' } },
        axisLabel: { color: '#878D8E', fontSize: 12 },
        nameTextStyle: { color: '#878D8E', fontSize: 12 },
      },
      {
        type: 'value',
        name: '辆',
        min: 0,
        axisLine: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#878D8E', fontSize: 12 },
        nameTextStyle: { color: '#878D8E', fontSize: 12 },
      },
    ],
    series: [
      {
        name: '每小时车流量',
        type: 'bar',
        yAxisIndex: 0,
        data: d.hourlyData,
        barWidth: 'auto',
        barMaxWidth: 12,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#91CAA7' },
              { offset: 0.5, color: '#BBDF9F' },
              { offset: 1, color: '#DEF0D6' },
            ],
          },
        },
        emphasis: { focus: 'series' },
      },
      {
        name: '累计车流量',
        type: 'line',
        yAxisIndex: 1,
        data: d.cumulativeData,
        emphasis: { focus: 'series' },
        lineStyle: { color: '#FEB603' },
        itemStyle: { color: '#FEB603' },
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
    const list = await getClByHouse(props.tunnelId)
    const hourlyData: number[] = []
    const cumulativeData: number[] = []
    const hours: number[] = []
    list.forEach((item) => {
      hourlyData.push(Number(item.trafficFlow) || 0)
      cumulativeData.push(Number(item.totalFlow) || 0)
      hours.push(typeof item.hour === 'number' ? item.hour : 0)
    })
    const hasData = list.length > 0
    chartData.value = {
      hours: hasData && hours.length ? hours : [],
      hourlyData: hasData && hourlyData.length ? hourlyData : [],
      cumulativeData: hasData && cumulativeData.length ? cumulativeData : [],
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
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.chart-container {
  flex: 1;
  width: 100%;
  min-height: 180px;
}
</style>
