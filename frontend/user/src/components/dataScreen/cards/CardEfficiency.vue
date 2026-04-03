<template>
  <div class="screen-card">
    <div class="screen-card-header">
      <span class="screen-card-header-title">本月效率指标</span>
    </div>
    <div class="screen-card-body">
      <div class="efficiency-indicators">
        <div
          v-for="(item, i) in list"
          :key="i"
          class="efficiency-item"
        >
          <div :class="['efficiency-ring-wrap', `efficiency-ring-wrap--${item.type}`]">
            <div
              :ref="(el) => setChartRef(el as HTMLElement | null, i)"
              class="efficiency-chart"
            />
            <span class="efficiency-value">{{ item.value }}</span>
          </div>
          <span class="efficiency-label">{{ item.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  list: Array<{ value: string; label: string; type: 'carbon' | 'light' | 'power' }>
}>()

const chartRefs = ref<(HTMLElement | null)[]>([])
const charts = ref<(echarts.ECharts | null)[]>([])

function setChartRef(el: HTMLElement | null, i: number) {
  chartRefs.value[i] = el
}

function parsePercent(value: string): number {
  if (!value || value === '--') return 0
  const m = value.match(/(\d+)/)
  return m ? Math.min(100, Math.max(0, Number(m[1]))) : 0
}

function getGradient(type: 'carbon' | 'light' | 'power') {
  if (type === 'carbon') {
    return {
      type: 'linear',
      x: 0,
      y: 0,
      x2: 1,
      y2: 0,
      colorStops: [
        { offset: 0, color: '#b3ebb9' },
        { offset: 0.99, color: '#41b983' },
        { offset: 1, color: '#41b983' },
      ],
    }
  }
  if (type === 'power') {
    return {
      type: 'linear',
      x: 0,
      y: 0,
      x2: 1,
      y2: 0,
      colorStops: [
        { offset: 0, color: '#93c5fd' },
        { offset: 0.99, color: '#3b82f6' },
        { offset: 1, color: '#3b82f6' },
      ],
    }
  }
  return {
    type: 'linear',
    x: 0,
    y: 1,
    x2: 0,
    y2: 0,
    colorStops: [
      { offset: 0, color: '#e1e7df' },
      { offset: 0.01, color: '#e1e7df' },
      { offset: 1, color: '#ff823e' },
    ],
  }
}

function updateChart(el: HTMLElement | null, item: { value: string; type: 'carbon' | 'light' | 'power' }, index: number) {
  if (!el) return
  let chart = charts.value[index]
  if (!chart) {
    chart = echarts.init(el)
    charts.value[index] = chart
  }
  const percent = parsePercent(item.value)
  const fillColor = getGradient(item.type)
  chart.setOption({
    series: [
      {
        type: 'pie',
        radius: ['62%', '75%'],
        center: ['50%', '50%'],
        startAngle: 90,
        clockwise: true,
        data: [
          { value: percent, itemStyle: { color: fillColor } },
          { value: Math.max(0, 100 - percent), itemStyle: { color: '#e1e7df' } },
        ],
        label: { show: false },
        labelLine: { show: false },
        emphasis: { scale: false },
      },
    ],
  })
}

function updateAllCharts() {
  props.list.forEach((item, i) => {
    updateChart(chartRefs.value[i] ?? null, item, i)
  })
}

watch(
  () => props.list,
  () => updateAllCharts(),
  { deep: true }
)

onMounted(() => {
  nextTick(() => updateAllCharts())
})

onBeforeUnmount(() => {
  charts.value.forEach((c) => {
    c?.dispose()
  })
  charts.value = []
})
</script>

<style scoped>
@import '../dataScreenCards.css';
</style>

<style scoped>
.efficiency-ring-wrap {
  position: relative;
  width: 96px;
  height: 96px;
  flex-shrink: 0;
}

.efficiency-chart {
  width: 100%;
  height: 100%;
}

.efficiency-value {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 15px;
  font-weight: bold;
  color: #2d8f52;
  font-family: 'Microsoft Ya Hei', sans-serif;
  pointer-events: none;
}

.efficiency-ring-wrap--light .efficiency-value {
  color: #e66a2e;
}

.efficiency-ring-wrap--power .efficiency-value {
  color: #2563eb;
}
</style>
