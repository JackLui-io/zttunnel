<template>
  <DashboardFrontendCardShell title="设备状态分布">
    <div class="device-chart-wrap">
      <div ref="chartRef" class="device-chart" />
      <div class="card-4-legend">
        <div class="card-4-legend-item">
          <span class="card-4-legend-dot card-4-legend-dot--online" />
          <span>在线 {{ legendOnline }}</span>
        </div>
        <div class="card-4-legend-item">
          <span class="card-4-legend-dot card-4-legend-dot--offline" />
          <span>离线 {{ legendOffline }}</span>
        </div>
      </div>
    </div>
  </DashboardFrontendCardShell>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import DashboardFrontendCardShell from './DashboardFrontendCardShell.vue'

const props = defineProps({
  deviceStatus: {
    type: Object,
    default: null
  }
})

const chartRef = ref(null)
let chart = null
let ro = null

const legendOnline = computed(() => {
  const d = props.deviceStatus
  if (!d) return '(0)'
  const n = Number(d.online ?? 0)
  const p = d.onlinePercent != null ? Number(d.onlinePercent) : null
  if (p != null && !Number.isNaN(p)) {
    return `(${n}) ${p}%`
  }
  const t = Number(d.total ?? 0)
  const pct = t > 0 ? Math.round((n / t) * 1000) / 10 : 0
  return `(${n}) ${pct}%`
})

const legendOffline = computed(() => {
  const d = props.deviceStatus
  if (!d) return '(0)'
  const n = Number(d.offline ?? 0)
  const p = d.offlinePercent != null ? Number(d.offlinePercent) : null
  if (p != null && !Number.isNaN(p)) {
    return `(${n}) ${p}%`
  }
  const t = Number(d.total ?? 0)
  const pct = t > 0 ? Math.round((n / t) * 1000) / 10 : 0
  return `(${n}) ${pct}%`
})

function updateChart() {
  if (!chartRef.value || !chart) return
  const d = props.deviceStatus
  const total = Number(d?.total ?? 0)
  const online = Number(d?.online ?? 0)
  const offline = Number(d?.offline ?? 0)
  const data = [
    { value: online, name: '在线', itemStyle: { color: '#a3ccb5' } },
    { value: offline, name: '离线', itemStyle: { color: '#c9ced1' } }
  ].filter((x) => x.value > 0)

  if (data.length === 0) {
    data.push({ value: 1, name: '无数据', itemStyle: { color: '#e8ece9' } })
  }

  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(90, 120, 110, 0.92)',
      borderColor: 'transparent',
      textStyle: { color: '#f5faf7' }
    },
    series: [
      {
        type: 'pie',
        /* 实心饼状图（非环图） */
        radius: '62%',
        center: ['50%', '50%'],
        startAngle: 90,
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
        label: {
          show: total > 0,
          position: 'outside',
          formatter: '{b} {c}({d}%)',
          fontSize: 12
        },
        labelLine: {
          show: total > 0
        },
        data
      }
    ]
  })
}

function handleResize() {
  chart?.resize()
}

async function initChartIfNeeded() {
  await nextTick()
  const el = chartRef.value
  if (!el || el.clientWidth === 0) {
    await new Promise((r) => requestAnimationFrame(r))
  }
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
    window.addEventListener('resize', handleResize)
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(() => handleResize())
      ro.observe(chartRef.value)
    }
  }
  updateChart()
  requestAnimationFrame(() => chart?.resize())
}

watch(
  () => props.deviceStatus,
  () => {
    initChartIfNeeded()
  },
  { deep: true }
)

onMounted(() => {
  initChartIfNeeded()
})

onBeforeUnmount(() => {
  if (ro && chartRef.value) {
    try {
      ro.unobserve(chartRef.value)
    } catch {
      /* ignore */
    }
  }
  ro = null
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
/* 与 DashboardMonthlyTrendCard .trend-chart-wrap 一致的上下留白 */
.device-chart-wrap {
  padding: 8px 12px 16px;
  box-sizing: border-box;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

/* 320px 与 DashboardMonthlyTrendCard .trend-chart 一致 */
.device-chart {
  width: 100%;
  height: 320px;
  min-height: 320px;
  flex-shrink: 0;
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
  background: #a3ccb5;
}

.card-4-legend-dot--offline {
  background: #c9ced1;
}
</style>
