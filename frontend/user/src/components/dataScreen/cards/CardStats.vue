<template>
  <div class="screen-card screen-card--stats">
    <div class="screen-card-header">
      <span class="screen-card-header-title">统计</span>
    </div>
    <div class="screen-card-body stats-body">
      <div ref="chartRef" class="stats-chart" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(
  defineProps<{
    chart: { labels: string[]; consumption: number[]; saving: number[] }
  }>(),
  {
    chart: () => ({ labels: [], consumption: [], saving: [] }),
  }
)

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

function updateChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const { labels, consumption, saving } = props.chart
  const maxVal = Math.max(
    1,
    ...consumption,
    ...saving
  )
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params: unknown) {
        const p = params as Array<{ axisValue: string; seriesName: string; value: number }>
        if (!p?.length) return ''
        const header = p[0]?.axisValue ?? ''
        const rows = p.map((s) => `${s.seriesName}: ${Number(s.value).toFixed(1)} kWh`)
        return `${header}<br/>${rows.join('<br/>')}`
      },
      backgroundColor: 'transparent',
      borderColor: 'rgba(147, 203, 167, 0.8)',
      borderWidth: 1,
      textStyle: { color: '#4f5352', fontSize: 12 },
    },
    grid: { left: 40, right: 16, top: 16, bottom: 32 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#4f5352', fontFamily: 'Microsoft Ya Hei, sans-serif', fontSize: 12 },
      axisLine: { lineStyle: { color: 'rgba(147, 203, 167, 0.5)' } },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      max: Math.ceil(maxVal * 1.2),
      axisLabel: { color: '#4f5352', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(136, 220, 163, 0.5)', type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
    },
    series: [
      {
        name: '用电',
        type: 'bar',
        data: consumption,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 1,
            x2: 0,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#65a372' },
              { offset: 1, color: '#41b983' },
            ],
          },
        },
        barWidth: '28%',
        barGap: '30%',
      },
      {
        name: '节电',
        type: 'bar',
        data: saving,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 1,
            x2: 0,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#feb603' },
              { offset: 1, color: '#ff7a38' },
            ],
          },
        },
        barWidth: '28%',
        barGap: '30%',
      },
    ],
  })
}

watch(
  () => props.chart,
  () => updateChart(),
  { deep: true }
)

onMounted(() => {
  nextTick(() => updateChart())
})

onBeforeUnmount(() => {
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
@import '../dataScreenCards.css';
</style>

<style scoped>
.screen-card--stats .screen-card-body {
  padding: 8px 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}

.stats-body {
  display: flex;
  width: 100%;
  min-height: 0;
}

.stats-chart {
  width: 100%;
  height: 140px;
  min-height: 100px;
}
</style>
