<template>
  <div class="screen-viewport">
    <div class="screen-page">
      <DataScreenHeader />
      <DataScreenFooter />
      <main class="screen-main">
        <DataScreenLeft :screen-data="screenData" :loading="loading" />
        <DataScreenCenter :screen-data="screenData" :loading="loading" :tunnel-tree="tunnelTree" />
        <DataScreenRight :screen-data="screenData" :loading="loading" />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import DataScreenHeader from './DataScreenHeader.vue'
import DataScreenFooter from './DataScreenFooter.vue'
import DataScreenLeft from './DataScreenLeft.vue'
import DataScreenCenter from './DataScreenCenter.vue'
import DataScreenRight from './DataScreenRight.vue'
import { defaultScreenData, type DataScreenInfo } from './dataScreenTypes'
import {
  getTunnelOverview,
  getPendingCounts,
  getUserPowerOverview,
  getDeviceStatusDistribution,
  getTodayPowerSummary,
  getLightUpReductionRate,
} from '@/api/analyze'
import { getTunnelTree, type TunnelNode } from '@/api/tunnel'

const loading = ref(true)
const tunnelTree = ref<TunnelNode[] | null>(null)
const tunnelOverview = ref<{ tunnelCount?: number; totalMileage?: number } | null>(null)
const pendingCounts = ref<{ deviceAlarm?: number; realtimeAlarm?: number } | null>(null)
const todayPower = ref<{ todayConsumptionKwh?: number; todaySavingKwh?: number; todayCarbonReductionKg?: number } | null>(null)
const powerOverview = ref<{
  annualOverview?: { powerSavingRate?: number; carbonReductionRate?: number }
  monthlyData?: Array<{ month?: number; consumption?: number; saving?: number; lightUpReductionRate?: number }>
} | null>(null)
/** 去年用电概览，用于统计卡片跨年月份（如 3 月时 12 月为去年） */
const powerOverviewPrevYear = ref<{
  monthlyData?: Array<{ month?: number; consumption?: number; saving?: number; lightUpReductionRate?: number }>
} | null>(null)
const deviceStatus = ref<{ total?: number; online?: number; offline?: number; fault?: number; onlinePercent?: number } | null>(null)
const lightUpReduction = ref<{ lightUpReductionRate?: number } | null>(null)

const screenData = computed((): DataScreenInfo => {
  const fallback = defaultScreenData
  const currentMonth = new Date().getMonth() + 1

  // 中央信息框：今日用电/节电/碳减排 来自 todayPowerSummary；隧道总数、总里程、设备警告 来自 API
  const deviceWarning = (pendingCounts.value?.deviceAlarm ?? 0) + (pendingCounts.value?.realtimeAlarm ?? 0)
  const tp = todayPower.value
  const infoBoxes = [
    { value: tp?.todayConsumptionKwh != null ? String(Number(tp.todayConsumptionKwh).toFixed(1)) : '--', label: '今日用电量（kWh）', icon: 1, warn: false },
    { value: tp?.todaySavingKwh != null ? String(Number(tp.todaySavingKwh).toFixed(1)) : '--', label: '今日节电量（kWh）', icon: 2, warn: false },
    { value: tp?.todayCarbonReductionKg != null ? String(Number(tp.todayCarbonReductionKg).toFixed(1)) : '--', label: '今日碳减排（kg）', icon: 3, warn: false },
    { value: String(tunnelOverview.value?.tunnelCount ?? '--'), label: '隧道总数（条）', icon: 4, warn: false },
    {
      value:
        tunnelOverview.value?.totalMileage != null
          ? String(Number(tunnelOverview.value.totalMileage).toFixed(1))
          : '--',
      label: '总里程（km）',
      icon: 5,
      warn: false,
    },
    { value: String(deviceWarning), label: '设备警告', icon: 6, warn: deviceWarning > 0 },
  ]

  // 本月效率指标：碳减排率/节电率来自 userPowerOverview；亮灯时长削减率来自 lightUpReductionRate 接口
  const monthItem = powerOverview.value?.monthlyData?.find((m) => m.month === currentMonth)
  const consumption = Number(monthItem?.consumption ?? 0)
  const saving = Number(monthItem?.saving ?? 0)
  const total = consumption + saving
  const rate = total > 0 ? Math.round((saving / total) * 100) : 0
  const lightUpRate = lightUpReduction.value?.lightUpReductionRate != null
    ? Number(lightUpReduction.value.lightUpReductionRate).toFixed(1)
    : null
  const efficiency = [
    { value: `${rate}%`, label: '本月理论碳减排率', type: 'carbon' as const },
    { value: lightUpRate != null ? `${lightUpRate}%` : '--', label: '本月理论亮灯时长\n削减率', type: 'light' as const },
    { value: `${rate}%`, label: '本月理论亮节电率', type: 'power' as const },
  ]

  // 右侧设备状态：来自 API
  const status = deviceStatus.value
  const deviceStatusData = status
    ? {
        total: Number(status.total ?? 0),
        online: Number(status.online ?? 0),
        offline: Number(status.offline ?? 0),
        fault: Number(status.fault ?? 0),
      }
    : fallback.deviceStatus

  // 左侧隧道能耗排行：从 userPowerOverview 的 monthlyData 按用电量总和排序取前 5 个月
  // 接口已改为 kWh 单位，直接使用
  const monthly = powerOverview.value?.monthlyData ?? []
  const sorted = [...monthly]
    .filter((m) => m.month != null && (m.consumption ?? 0) > 0)
    .sort((a, b) => (b.consumption ?? 0) - (a.consumption ?? 0))
    .slice(0, 5)
  const energyRankList = sorted.map((m, i) => {
    const r = i + 1
    const consumptionKwh = Number(m.consumption ?? 0)
    const savingKwh = Number(m.saving ?? 0)
    const total = consumptionKwh + savingKwh
    const consumptionPercent = total > 0 ? Math.round((consumptionKwh / total) * 100) : 0
    const savingPercent = total > 0 ? Math.round((savingKwh / total) * 100) : 0
    return {
      rank: r,
      month: `${m.month}月`,
      consumptionPercent,
      savingPercent,
      value: `${consumptionKwh.toFixed(2)} kWh`,
      type: (r === 1 ? 'orange' : r === 2 ? 'yellow' : 'green') as 'orange' | 'yellow' | 'green',
    }
  })

  // 左侧月度节能统计：从 userPowerOverview 的 monthlyData 按节电量排序取前 5 个月
  const savingSorted = [...monthly]
    .filter((m) => m.month != null && (m.saving ?? 0) > 0)
    .sort((a, b) => (b.saving ?? 0) - (a.saving ?? 0))
    .slice(0, 5)
  const maxSaving = savingSorted[0]?.saving ?? 1
  const monthlySavingList = savingSorted.map((m, i) => {
    const r = i + 1
    const savingKwh = Number(m.saving ?? 0)
    const percent = maxSaving > 0 ? Math.round((savingKwh / maxSaving) * 100) : 0
    return {
      rank: r,
      month: `${m.month}月`,
      percent: Math.min(100, Math.max(0, percent)),
      value: `${savingKwh.toFixed(2)} kWh`,
      type: (r === 1 ? 'orange' : r === 2 ? 'yellow' : 'green') as 'orange' | 'yellow' | 'green',
    }
  })

  // 多维度分析雷达图：设备在线率、覆盖率、运行效率(=节能)、系统稳定性(=在线率)、碳减排、节能效率
  const ds = deviceStatus.value
  const onlineRate =
    ds?.onlinePercent ?? (ds && (ds.total ?? 0) > 0 ? ((ds.online ?? 0) / (ds.total ?? 1)) * 100 : 0)
  const powerRate = Number(powerOverview.value?.annualOverview?.powerSavingRate ?? 0)
  const carbonRate = Number(powerOverview.value?.annualOverview?.carbonReductionRate ?? 0)
  const clamp = (v: number) => Math.min(100, Math.max(0, v))
  const radarData: number[] = [
    clamp(onlineRate),
    clamp(onlineRate),
    clamp(powerRate),
    clamp(onlineRate),
    clamp(carbonRate),
    clamp(powerRate),
  ]

  // 统计卡片：近 4 个月用电/节电对比（含当月及前 3 个月，跨年时 12 月取去年数据）
  const monthlyPrev = powerOverviewPrevYear.value?.monthlyData ?? []
  const last4Months: number[] = []
  for (let i = 3; i >= 0; i--) {
    let m = currentMonth - i
    if (m <= 0) m += 12
    last4Months.push(m)
  }
  const getMonthData = (m: number) => {
    const fromPrev = m > currentMonth
    const list = fromPrev ? monthlyPrev : monthly
    const d = list.find((x) => x.month === m)
    return { consumption: Number(d?.consumption ?? 0), saving: Number(d?.saving ?? 0) }
  }
  const statsChart = {
    labels: last4Months.map((m) => `${m}月`),
    consumption: last4Months.map((m) => getMonthData(m).consumption),
    saving: last4Months.map((m) => getMonthData(m).saving),
  }

  return {
    ...fallback,
    infoBoxes,
    efficiency,
    deviceStatus: deviceStatusData,
    energyRank: energyRankList,
    monthlySaving: monthlySavingList,
    radarData,
    statsChart,
  }
})

function fetchData() {
  loading.value = true
  const year = new Date().getFullYear()

  const p1 = getTunnelOverview()
  const p2 = getPendingCounts()
  const p3 = getUserPowerOverview(year)
  const p4 = getUserPowerOverview(year - 1)
  const p5 = getDeviceStatusDistribution()
  const p6 = getTodayPowerSummary()
  const p7 = getLightUpReductionRate()
  const p8 = getTunnelTree()

  p1.then((v) => { tunnelOverview.value = v ?? null }).catch(() => { tunnelOverview.value = null })
  p2.then((v) => { pendingCounts.value = v ?? null }).catch(() => { pendingCounts.value = null })
  p3.then((v) => { powerOverview.value = v ?? null }).catch(() => { powerOverview.value = null })
  p4.then((v) => { powerOverviewPrevYear.value = v ?? null }).catch(() => { powerOverviewPrevYear.value = null })
  p5.then((v) => { deviceStatus.value = v ?? null }).catch(() => { deviceStatus.value = null })
  p6.then((v) => { todayPower.value = v ?? null }).catch(() => { todayPower.value = null })
  p7.then((v) => { lightUpReduction.value = v ?? null }).catch(() => { lightUpReduction.value = null })
  p8.then((v) => { tunnelTree.value = v }).catch(() => { tunnelTree.value = null })

  Promise.allSettled([p1, p2, p3, p4, p5, p6, p7, p8]).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  document.body.classList.add('data-screen-active')
  fetchData()
})
onBeforeUnmount(() => {
  document.body.classList.remove('data-screen-active')
})
</script>

<style scoped>
/* 与 dashboard 一致的页面背景色 */
.screen-viewport {
  width: 100%;
  min-height: 100vh;
  background: #93cba7;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.screen-page {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  /* 全国地图移至 DataScreenCenter 热点层，避免与点击区域错位 */
  background: #edf2ea;
  margin: 0;
}

.screen-main {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr) minmax(0, 1fr);
  grid-template-rows: 1fr;
  gap: 16px;
  padding: 100px 24px 37px;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
}

.screen-main > * {
  min-width: 0;
}

/* 中间栏（含地图信息框）盖在左右栏之上，避免侧栏压住弹层 */
.screen-main > .screen-center {
  position: relative;
  z-index: 20;
}

/* 大屏：限制侧栏最大宽度，避免过度拉伸 */
@media (min-width: 1600px) {
  .screen-main {
    grid-template-columns: minmax(280px, 1fr) minmax(0, 2fr) minmax(280px, 1fr);
  }
}

/* 平板 */
@media (max-width: 1024px) {
  .screen-main {
    grid-template-columns: minmax(0, 1fr) minmax(0, 2fr) minmax(0, 1fr);
    gap: 12px;
    padding: 80px 16px 24px;
  }
}

/* 移动端 */
@media (max-width: 768px) {
  .screen-main {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 70px 12px 20px;
    overflow-y: auto;
  }

  .screen-main > * {
    min-width: auto;
  }
}
</style>
