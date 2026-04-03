<template>
  <div class="statistical-analysis-main">
      <div class="sa-header">
        <CentralNavTabs />
        <CentralTunnelLabel />
      </div>
      <div class="sa-cell sa-cell-filter">
        <CardSaFilter
          v-model:model-value="dateRange"
          v-model:active-func="activeFunc"
          @export="handleExport"
        />
      </div>
      <template v-if="activeFunc === 'overall'">
        <div class="sa-cell sa-cell-metric-1">
          <CardSaMetric :metric="metrics[0]" />
        </div>
        <div class="sa-cell sa-cell-metric-2">
          <CardSaMetric :metric="metrics[1]" />
        </div>
        <div class="sa-cell sa-cell-metric-3">
          <CardSaMetric :metric="metrics[2]" />
        </div>
        <div class="sa-cell sa-cell-metric-4">
          <CardSaMetric :metric="metrics[3]" />
        </div>
        <div class="sa-cell sa-cell-monthly">
          <CardSaMonthly :tunnel-id="queryForm.tunnelId" />
        </div>
      </template>
      <template v-else-if="activeFunc === 'traffic'">
        <div class="sa-cell sa-cell-traffic-flow">
          <CardSaTrafficFlow :data="trafficFlowData" />
        </div>
        <div class="sa-cell sa-cell-avg-speed">
          <CardSaAvgSpeed :data="trafficFlowData" />
        </div>
        <div class="sa-cell sa-cell-traffic-table">
          <CardSaTrafficTable :data="trafficFlowData" />
        </div>
      </template>
      <template v-else-if="activeFunc === 'brightness'">
        <div class="sa-cell sa-cell-brightness">
          <CardSaBrightness :data="brightnessData" />
        </div>
        <div class="sa-cell sa-cell-light-duration">
          <CardSaLightDuration :data="brightnessData" />
        </div>
        <div class="sa-cell sa-cell-brightness-table">
          <CardSaBrightnessTable :data="brightnessData" />
        </div>
      </template>
      <template v-else-if="activeFunc === 'energy'">
        <div class="sa-cell sa-cell-power-rate">
          <CardSaPowerRate :data="carbonData" />
        </div>
        <div class="sa-cell sa-cell-carbon">
          <CardSaCarbon :data="carbonStatsData" />
        </div>
        <div class="sa-cell sa-cell-carbon-table">
          <CardSaCarbonTable :data="carbonData" />
        </div>
      </template>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import CentralTunnelLabel from '@/components/layout/CentralTunnelLabel.vue'
import CentralNavTabs from '@/components/layout/CentralNavTabs.vue'
import CardSaFilter from '@/components/cards/CardSaFilter.vue'
import CardSaMetric from '@/components/cards/CardSaMetric.vue'
import CardSaMonthly from '@/components/cards/CardSaMonthly.vue'
import CardSaTrafficFlow from '@/components/cards/CardSaTrafficFlow.vue'
import CardSaAvgSpeed from '@/components/cards/CardSaAvgSpeed.vue'
import CardSaTrafficTable from '@/components/cards/CardSaTrafficTable.vue'
import CardSaBrightness from '@/components/cards/CardSaBrightness.vue'
import CardSaLightDuration from '@/components/cards/CardSaLightDuration.vue'
import CardSaBrightnessTable from '@/components/cards/CardSaBrightnessTable.vue'
import CardSaPowerRate from '@/components/cards/CardSaPowerRate.vue'
import CardSaCarbon from '@/components/cards/CardSaCarbon.vue'
import CardSaCarbonTable from '@/components/cards/CardSaCarbonTable.vue'
import { useTunnelStore } from '@/stores/tunnel'
import { getRangeNear7Days } from '@/utils/saDate'
import {
  getAnalyzeStatistics,
  statisticsExport,
  getTrafficFlowOrSpeed,
  trafficFlowOrSpeedExport,
  trafficFlowOrSpeedListExport,
  getAnalyzeInsideAndOutside,
  insideAndOutsideExport,
  insideAndOutsideListExport,
  getAnalyzeCarbon,
  getCarbonByStatistics,
  carbonExport,
} from '@/api/analyze'
import { statisticsVoToMetrics } from '@/utils/saMetrics'

const tunnelStore = useTunnelStore()
const dateRange = ref(getRangeNear7Days())
const activeFunc = ref<'overall' | 'traffic' | 'brightness' | 'energy'>('overall')
const statisticsData = ref<Awaited<ReturnType<typeof getAnalyzeStatistics>>>(null)
const trafficFlowData = ref<Awaited<ReturnType<typeof getTrafficFlowOrSpeed>>>([])
const brightnessData = ref<Awaited<ReturnType<typeof getAnalyzeInsideAndOutside>>>([])
const carbonData = ref<Awaited<ReturnType<typeof getAnalyzeCarbon>>>([])
const carbonStatsData = ref<Awaited<ReturnType<typeof getCarbonByStatistics>>>([])

const queryForm = computed(() => ({
  startTime: dateRange.value.startTime,
  endTime: dateRange.value.endTime,
  tunnelId: tunnelStore.selectedTunnelId,
}))

const metrics = computed(() => statisticsVoToMetrics(statisticsData.value))

async function fetchStatistics() {
  const form = queryForm.value
  if (form.tunnelId == null) {
    statisticsData.value = null
    return
  }
  try {
    statisticsData.value = await getAnalyzeStatistics({
      ...form,
      tunnelId: form.tunnelId,
    })
  } catch {
    statisticsData.value = null
  }
}

watch([() => dateRange.value, () => tunnelStore.selectedTunnelId], fetchStatistics, { immediate: true })

async function fetchTrafficFlow() {
  const form = queryForm.value
  if (form.tunnelId == null) {
    trafficFlowData.value = []
    return
  }
  try {
    trafficFlowData.value = await getTrafficFlowOrSpeed({
      ...form,
      tunnelId: form.tunnelId,
    })
  } catch {
    trafficFlowData.value = []
  }
}

watch(
  [() => dateRange.value, () => tunnelStore.selectedTunnelId, activeFunc],
  () => {
    if (activeFunc.value === 'traffic') fetchTrafficFlow()
    if (activeFunc.value === 'brightness') fetchBrightness()
    if (activeFunc.value === 'energy') fetchCarbon()
  },
  { immediate: true }
)

async function fetchBrightness() {
  const form = queryForm.value
  if (form.tunnelId == null) {
    brightnessData.value = []
    return
  }
  try {
    brightnessData.value = await getAnalyzeInsideAndOutside({
      ...form,
      tunnelId: form.tunnelId,
    })
  } catch {
    brightnessData.value = []
  }
}

async function fetchCarbon() {
  const form = queryForm.value
  if (form.tunnelId == null) {
    carbonData.value = []
    carbonStatsData.value = []
    return
  }
  try {
    const [carbon, stats] = await Promise.all([
      getAnalyzeCarbon({ ...form, tunnelId: form.tunnelId }),
      getCarbonByStatistics({ ...form, tunnelId: form.tunnelId }),
    ])
    carbonData.value = carbon
    carbonStatsData.value = stats
  } catch {
    carbonData.value = []
    carbonStatsData.value = []
  }
}

async function handleExport() {
  const form = queryForm.value
  if (form.tunnelId == null) return
  try {
    if (activeFunc.value === 'overall') {
      await statisticsExport({ ...form, tunnelId: form.tunnelId }, '统计分析.xlsx')
    } else if (activeFunc.value === 'traffic') {
      await trafficFlowOrSpeedExport({ ...form, tunnelId: form.tunnelId }, '车流车速.xlsx')
      await trafficFlowOrSpeedListExport({ ...form, tunnelId: form.tunnelId }, '车流车速列表导出.xlsx')
    } else if (activeFunc.value === 'brightness') {
      await insideAndOutsideExport({ ...form, tunnelId: form.tunnelId }, '洞内外照度.xlsx')
      await insideAndOutsideListExport({ ...form, tunnelId: form.tunnelId }, '洞内外照度列表.xlsx')
    } else if (activeFunc.value === 'energy') {
      await carbonExport({ ...form, tunnelId: form.tunnelId }, '能碳数据.xlsx')
    }
  } catch (e) {
    console.error('导出失败', e)
  }
}
</script>

<style scoped>
.statistical-analysis-main {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  grid-template-rows: auto auto 1fr 1fr;
  gap: 15px;
  width: 100%;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
  /* padding-bottom: 12px; 为底部卡片阴影留出空间 */
}

.sa-header {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 49px;
}

.sa-cell {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.sa-cell-filter {
  grid-column: 1 / -1;
  min-height: 120px;
}

.sa-cell-metric-1,
.sa-cell-metric-2,
.sa-cell-metric-3,
.sa-cell-metric-4 {
  min-height: 300px;
}
.sa-cell-metric-1 {
  grid-column: 1;
  grid-row: 3;
}
.sa-cell-metric-2 {
  grid-column: 2;
  grid-row: 3;
}
.sa-cell-metric-3 {
  grid-column: 3;
  grid-row: 3;
}
.sa-cell-metric-4 {
  grid-column: 4;
  grid-row: 3;
}

.sa-cell-monthly {
  grid-column: 1 / -1;
  grid-row: 4;
  min-height: 250px;
}

/* 车流/车速 布局：车流量与平均速度并排，各占一半，占满一行 */
.sa-cell-traffic-flow {
  grid-column: 1 / 3;
  grid-row: 3;
  min-height: 250px;
}
.sa-cell-avg-speed {
  grid-column: 3 / 5;
  grid-row: 3;
  min-height: 250px;
}
.sa-cell-traffic-table {
  grid-column: 1 / -1;
  grid-row: 4;
  min-height: 280px;
}

/* 洞内外亮度 布局：亮度对比与亮灯/暗灯时长对比并排，各占一半，占满一行 */
.sa-cell-brightness {
  grid-column: 1 / 3;
  grid-row: 3;
  min-height: 250px;
}
.sa-cell-light-duration {
  grid-column: 3 / 5;
  grid-row: 3;
  min-height: 250px;
}
.sa-cell-brightness-table {
  grid-column: 1 / -1;
  grid-row: 4;
  min-height: 280px;
}

/* 能碳数据 布局：耗电量与节电率、碳排放量与理论碳减排量并排，各占一半，占满一行 */
.sa-cell-power-rate {
  grid-column: 1 / 3;
  grid-row: 3;
  min-height: 250px;
}
.sa-cell-carbon {
  grid-column: 3 / 5;
  grid-row: 3;
  min-height: 250px;
}
.sa-cell-carbon-table {
  grid-column: 1 / -1;
  grid-row: 4;
  min-height: 280px;
}

/* 平板：2 列 */
@media (max-width: 1024px) {
  .statistical-analysis-main {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto auto auto auto 1fr;
    gap: 12px;
  }

  .sa-cell-metric-1 {
    grid-column: 1;
    grid-row: 3;
  }
  .sa-cell-metric-2 {
    grid-column: 2;
    grid-row: 3;
  }
  .sa-cell-metric-3 {
    grid-column: 1;
    grid-row: 4;
  }
  .sa-cell-metric-4 {
    grid-column: 2;
    grid-row: 4;
  }

  .sa-cell-monthly {
    grid-row: 5;
    min-height: 220px;
  }

  .sa-cell-traffic-flow {
    grid-column: 1;
    grid-row: 3;
  }
  .sa-cell-avg-speed {
    grid-column: 2;
    grid-row: 3;
  }
  .sa-cell-traffic-table {
    grid-row: 4;
  }

  .sa-cell-brightness {
    grid-column: 1;
    grid-row: 3;
  }
  .sa-cell-light-duration {
    grid-column: 2;
    grid-row: 3;
  }
  .sa-cell-brightness-table {
    grid-row: 4;
  }

  .sa-cell-power-rate {
    grid-column: 1;
    grid-row: 3;
  }
  .sa-cell-carbon {
    grid-column: 2;
    grid-row: 3;
  }
  .sa-cell-carbon-table {
    grid-row: 4;
  }
}

/* 移动端：单列 */
@media (max-width: 768px) {
  .statistical-analysis-main {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto auto auto 1fr;
    gap: 12px;
    min-height: auto;
  }

  .sa-header {
    flex-wrap: wrap;
  }

  .sa-cell-filter {
    min-height: 100px;
  }

  .sa-cell-metric-1,
  .sa-cell-metric-2,
  .sa-cell-metric-3,
  .sa-cell-metric-4 {
    grid-column: 1;
    grid-row: auto;
    min-height: 280px;
  }

  .sa-cell-monthly {
    grid-row: auto;
    min-height: 280px;
  }

  .sa-cell-traffic-flow,
  .sa-cell-avg-speed,
  .sa-cell-traffic-table,
  .sa-cell-brightness,
  .sa-cell-light-duration,
  .sa-cell-brightness-table,
  .sa-cell-power-rate,
  .sa-cell-carbon,
  .sa-cell-carbon-table {
    grid-column: 1;
    grid-row: auto;
    min-height: 250px;
  }
}
</style>
