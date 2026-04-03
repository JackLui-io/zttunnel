<template>
  <div class="screen-card screen-card--radar">
    <div class="screen-card-header">
      <span class="screen-card-header-title">多维度分析</span>
    </div>
    <div class="screen-card-body radar-body">
      <div ref="chartRef" class="radar-chart" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(
  defineProps<{
    /** 6 维 [设备在线率, 覆盖率, 运行效率, 系统稳定性, 碳减排, 节能效率]，0-100 */
    data?: number[]
  }>(),
  { data: () => [] }
)

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const INDICATORS = [
  { name: '设备在线率', max: 100 },
  { name: '覆盖率', max: 100 },
  { name: '运行效率', max: 100 },
  { name: '系统稳定性', max: 100 },
  { name: '碳减排', max: 100 },
  { name: '节能效率', max: 100 },
]

function updateChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  const values = props.data?.length === 6 ? props.data : [0, 0, 0, 0, 0, 0]
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter() {
        return INDICATORS.map((ind, i) => `${ind.name}: ${(values[i] ?? 0).toFixed(1)}%`).join('<br/>')
      },
      backgroundColor: 'transparent',
      borderColor: 'rgba(147, 203, 167, 0.8)',
      borderWidth: 1,
      textStyle: { color: '#4f5352', fontSize: 13 },
    },
    radar: {
      indicator: INDICATORS,
      shape: 'polygon',
      splitNumber: 2,
      axisName: {
        color: '#4f5352',
        fontFamily: 'Microsoft YaHei, sans-serif',
        fontSize: 14,
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(147, 203, 167, 0.1)', 'rgba(147, 203, 167, 0.05)'],
        },
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(147, 203, 167, 0.5)',
          type: 'dashed',
        },
      },
      axisLine: {
        lineStyle: {
          color: 'rgba(147, 203, 167, 0.5)',
          type: 'dashed',
        },
      },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: values,
            areaStyle: {
              color: 'rgba(145, 202, 167, 0.5)',
            },
            lineStyle: {
              color: '#41b983',
              width: 2,
            },
            itemStyle: {
              color: '#41b983',
            },
          },
        ],
      },
    ],
  })
}

watch(
  () => props.data,
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
.screen-card--radar {
  overflow: visible;
}

.screen-card--radar .screen-card-body.radar-body {
  padding: 8px 12px;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-height: 0;
}

.radar-chart {
  width: 100%;
  max-width: min(380px, 100%);
  aspect-ratio: 1;
}

@media (max-width: 768px) {
  .radar-chart {
    max-width: min(280px, 100%);
  }
}
</style>
