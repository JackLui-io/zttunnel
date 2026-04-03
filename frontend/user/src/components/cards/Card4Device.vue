<template>
  <div class="card card-4">
    <div class="card-4-header">
      <img src="/page1/Group68.png" alt="" class="card-4-header-bg" />
      <span class="card-4-header-title">设备状态分布</span>
    </div>
    <div class="card-4-body">
      <div class="card-4-inner">
        <div class="card-4-ring-wrap">
          <div ref="chartRef" class="card-4-chart"></div>
        </div>
        <div class="card-4-legend">
          <div class="card-4-legend-item">
            <span class="card-4-legend-dot card-4-legend-dot--online"></span>
            <span>在线 {{ legendOnline }}</span>
          </div>
          <div class="card-4-legend-item">
            <span class="card-4-legend-dot card-4-legend-dot--offline"></span>
            <span>离线 {{ legendOffline }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import type { DashboardDeviceStatusVo } from '@/api/analyze'

const props = defineProps<{
  deviceStatus?: DashboardDeviceStatusVo | null
}>()

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const legendOnline = computed(() => {
  const d = props.deviceStatus
  if (!d) return '(0)'
  const n = d.online ?? 0
  const p = d.onlinePercent ?? 0
  return `(${n}) ${p}%`
})
const legendOffline = computed(() => {
  const d = props.deviceStatus
  if (!d) return '(0)'
  const n = d.offline ?? 0
  const p = d.offlinePercent ?? 0
  return `(${n}) ${p}%`
})
function updateChart() {
  if (!chartRef.value || !chart) return
  const d = props.deviceStatus
  const total = d?.total ?? 0
  const online = d?.online ?? 0
  const offline = d?.offline ?? 0
  const data = [
    { value: online, name: '在线', itemStyle: { color: '#6bb88d' } },
    { value: offline, name: '离线', itemStyle: { color: '#9e9e9e' } },
  ].filter((x) => x.value > 0)
  if (data.length === 0) {
    data.push({ value: 1, name: '无数据', itemStyle: { color: '#E0E9DA' } })
  }
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: '#2E6A5C',
      borderColor: 'transparent',
      textStyle: { color: '#D0DED4' },
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '50%'],
        startAngle: 110,
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#f0fbef', borderWidth: 2 },
        label: {
          show: total > 0,
          position: 'outside',
          formatter: '{b} {c}({d}%)',
          fontSize: 12,
        },
        labelLine: {
          show: total > 0,
        },
        data,
      },
    ],
  })
}

function handleResize() {
  chart?.resize()
}

watch(
  () => props.deviceStatus,
  () => updateChart(),
  { deep: true }
)

onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    window.addEventListener('resize', handleResize)
    updateChart()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.card-4 {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: visible;
  background: transparent;
  border: none;
  flex: 1;
  min-height: 0;
}

.card-4-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
}
.card-4-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-4-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  line-height: 22px;
  color: #0f7f62;
}

.card-4-body {
  flex: 1;
  min-height: 0;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.card-4-inner {
  padding: 12px 20px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.card-4-ring-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  width: 100%;
}

.card-4-chart {
  width: 100%;
  height: 115px;
  min-height: 90px;
}

.card-4-legend {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 8px;
  flex-shrink: 0;
  padding: 0 8px;
  box-sizing: border-box;
}

.card-4-legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
}
.card-4-legend-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 2px;
  flex-shrink: 0;
}
.card-4-legend-dot--online {
  background: #6bb88d;
}
.card-4-legend-dot--offline {
  background: #9E9E9E;
}
</style>
