<template>
  <DashboardFrontendCardShell title="总体用电/能耗">
    <div class="trend-chart-wrap">
      <div ref="chartRef" class="trend-chart" />
    </div>
  </DashboardFrontendCardShell>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import DashboardFrontendCardShell from './DashboardFrontendCardShell.vue'

const KWH_TO_WAN_KWH = 10000

const props = defineProps({
  /** { month, totalLight, totalEconomyLight } 单位 kWh */
  monthlyRows: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref(null)
let chart = null

/** 12 个月用电/能耗（万 kWh），能耗取接口节电量口径 */
const chartData = computed(() => {
  const list = props.monthlyRows || []
  const categories = []
  const powerWan = []
  const energyWan = []
  for (let m = 1; m <= 12; m++) {
    categories.push(`${m}月`)
    const found = list.find((row) => Number(row.month) === m)
    const powerKwh = Number(found?.totalLight ?? 0)
    const savingKwh = Number(found?.totalEconomyLight ?? 0)
    powerWan.push(Number((powerKwh / KWH_TO_WAN_KWH).toFixed(4)))
    energyWan.push(Number((savingKwh / KWH_TO_WAN_KWH).toFixed(4)))
  }
  return { categories, powerWan, energyWan }
})

function buildOption() {
  const { categories, powerWan, energyWan } = chartData.value

  /* 低饱和度、偏浅的柱状渐变 */
  const orangeGradient = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: '#fff5e6' },
    { offset: 0.45, color: '#f3dcc0' },
    { offset: 1, color: '#dfc4a8' }
  ])

  const greenGradient = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: '#eef6f0' },
    { offset: 0.45, color: '#c9e2d2' },
    { offset: 1, color: '#a8cdb9' }
  ])

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        if (!params?.length) return ''
        let h = `${params[0].axisValue}<br/>`
        for (const p of params) {
          h += `${p.marker}${p.seriesName}: ${p.value} 万kWh<br/>`
        }
        return h
      }
    },
    legend: {
      data: ['用电', '能耗'],
      top: 8,
      right: 16,
      textStyle: { color: '#606266', fontSize: 12 }
    },
    grid: {
      left: 52,
      right: 24,
      top: 48,
      bottom: 32,
      containLabel: false
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#909399', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      name: '万kWh',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } },
      axisLabel: { color: '#909399', fontSize: 11 }
    },
    series: [
      {
        name: '用电',
        type: 'bar',
        barMaxWidth: 22,
        data: powerWan,
        itemStyle: {
          color: orangeGradient,
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '能耗',
        type: 'bar',
        barMaxWidth: 22,
        data: energyWan,
        itemStyle: {
          color: greenGradient,
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
}

function render() {
  if (!chartRef.value || !chart) return
  chart.setOption(buildOption(), true)
}

function handleResize() {
  chart?.resize()
}

watch(
  () => props.monthlyRows,
  () => render(),
  { deep: true }
)

onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    window.addEventListener('resize', handleResize)
    render()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.trend-chart-wrap {
  padding: 8px 12px 16px;
  box-sizing: border-box;
  background: #ffffff;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.trend-chart {
  width: 100%;
  height: 320px;
  min-height: 260px;
}
</style>
