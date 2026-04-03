<template>
  <div class="card card-rt card-rt-brightness">
    <div class="card-rt-header">
      <img src="/page1/Group70.png" alt="" class="card-rt-header-bg" />
      <span class="card-rt-header-title">亮度对比</span>
    </div>
    <div class="card-rt-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getZdByHouse } from '@/api/analyze'

const emptyChartData = () => ({ timeLabels: [] as string[], dimmingData: [] as number[], brightnessData: [] as number[] })

const props = defineProps<{ tunnelId?: number }>()
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const chartData = ref(emptyChartData())

function updateChart() {
  ensureChartAndSetOption()
}

async function fetchData() {
  if (props.tunnelId == null) {
    chartData.value = emptyChartData()
    updateChart()
    return
  }
  try {
    const list = await getZdByHouse(props.tunnelId)
    const dimmingRadio: number[] = []
    const outside: number[] = []
    const hour: string[] = []
    list.forEach((item) => {
      dimmingRadio.push(Number(item.dimmingRadio) || 0)
      outside.push(Number(item.outside) || 0)
      if (item.hour != null && item.hour !== '') {
        const arr = String(item.hour).split(':')
        if (arr[0] != null && arr[0].length < 2) arr[0] = ('0' + arr[0]).slice(-2)
        if (arr[1] != null && arr[1].length < 2) arr[1] = ('0' + arr[1]).slice(-2)
        hour.push(arr.join(':'))
      } else {
        hour.push('')
      }
    })
    const hasData = dimmingRadio.length > 0 || outside.length > 0
    chartData.value = {
      timeLabels: hasData && hour.length ? hour : [],
      dimmingData: hasData && dimmingRadio.length ? dimmingRadio : [],
      brightnessData: hasData && outside.length ? outside : [],
    }
  } catch {
    chartData.value = emptyChartData()
  }
  updateChart()
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
      backgroundColor: '#2E6A5C',
      borderColor: 'transparent',
      borderRadius: 6,
      padding: [10, 14],
      textStyle: { color: '#D0DED4', fontSize: 12, lineHeight: 16 },
      formatter: (params: unknown) => {
        const p = (params as { dataIndex: number; axisValue: string }[])[0]
        if (!p) return ''
        const idx = p.dataIndex
        const time = p.axisValue || ''
        const dim = chartData.value.dimmingData[idx] ?? 0
        const bright = chartData.value.brightnessData[idx] ?? 0
        return `<div style="font-size:14px;color:#D0DED4;margin-bottom:8px;">${time}</div>
          <table style="border-collapse:collapse;font-size:12px;"><tr>
          <td style="padding:2px 8px 2px 0;vertical-align:middle;"><span style="width:6px;height:6px;border-radius:50%;background:#91CAA7;display:inline-block;"></span></td>
          <td style="padding:2px 0;color:#D0DED4;">调光比例</td>
          <td style="padding:2px 0;color:#91CAA7;text-align:right;padding-left:16px;">${dim}</td></tr><tr>
          <td style="padding:2px 8px 2px 0;vertical-align:middle;"><span style="width:6px;height:6px;border-radius:50%;background:#FEB603;display:inline-block;"></span></td>
          <td style="padding:2px 0;color:#D0DED4;">洞外亮度</td>
          <td style="padding:2px 0;color:#FEB603;text-align:right;padding-left:16px;">${bright}</td></tr></table>`
      },
    },
    legend: {
      data: ['调光比例', '洞外亮度'],
      top: 0,
      textStyle: { fontSize: 12, color: '#878D8E' },
      itemWidth: 10,
      itemHeight: 10,
    },
    grid: { left: 50, right: 50, top: 35, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: d.timeLabels,
      axisLine: { lineStyle: { color: '#B8E0C8' } },
      axisLabel: { color: '#878D8E', fontSize: 12, interval: 'auto' },
    },
    yAxis: [
      {
        type: 'value',
        name: '调光比例(%)',
        min: 0,
        axisLine: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: 'rgba(184, 224, 200, 0.6)' } },
        axisLabel: { color: '#878D8E', fontSize: 12 },
        nameTextStyle: { color: '#878D8E', fontSize: 12 },
      },
      {
        type: 'value',
        name: '洞外亮度(cd/m²)',
        min: 0,
        axisLine: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#878D8E', fontSize: 12 },
        nameTextStyle: { color: '#878D8E', fontSize: 12 },
      },
    ],
    series: [
      {
        name: '调光比例',
        type: 'line',
        yAxisIndex: 0,
        data: d.dimmingData,
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0.0208, color: '#97C89F' },
              { offset: 1, color: 'rgba(255, 255, 255, 0)' },
            ],
          },
        },
        lineStyle: { color: '#97C89F' },
        itemStyle: { color: '#97C89F' },
      },
      {
        name: '洞外亮度',
        type: 'line',
        yAxisIndex: 1,
        data: d.brightnessData,
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0.0208, color: 'rgba(254, 182, 3, 0.7)' },
              { offset: 1, color: 'rgba(255, 255, 255, 0)' },
            ],
          },
        },
        lineStyle: { color: '#FEB603' },
        itemStyle: { color: '#FEB603' },
      },
    ],
  })
  chart.resize()
}

onMounted(() => {
  nextTick(ensureChartAndSetOption)
})

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
