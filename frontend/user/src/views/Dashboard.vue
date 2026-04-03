<template>
  <div class="dashboard-grid">
      <div class="dashboard-card dashboard-card-1">
        <Card1Overview :statistics="sharedStatistics" />
      </div>
      <div class="dashboard-card dashboard-card-3">
        <Card3Tunnel :tunnelSummary="tunnelSummary" />
      </div>
      <div class="dashboard-card dashboard-card-4">
        <Card4Device :deviceStatus="deviceStatus" />
      </div>
      <div class="dashboard-card dashboard-card-5">
        <Card5Efficiency :statistics="sharedStatistics" />
      </div>
      <div class="dashboard-card dashboard-card-6">
        <Card6Trend :monthlyData="monthlyData" />
      </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import Card1Overview from '@/components/cards/Card1Overview.vue'
import Card3Tunnel from '@/components/cards/Card3Tunnel.vue'
import Card4Device from '@/components/cards/Card4Device.vue'
import Card5Efficiency from '@/components/cards/Card5Efficiency.vue'
import Card6Trend from '@/components/cards/Card6Trend.vue'
import {
  getUserPowerOverview,
  getTunnelOverview,
  getDeviceStatusDistribution,
  type DashboardPowerOverviewVo,
  type DashboardTunnelOverviewVo,
  type DashboardDeviceStatusVo,
  type StatisticsVo,
} from '@/api/analyze'

const tunnelOverview = ref<DashboardTunnelOverviewVo | null>(null)
const tunnelSummary = computed(() => {
  const o = tunnelOverview.value
  return [
    { label: '高速公路', unit: '(条)', value: String(o?.highwayCount ?? 0), icon: 'Group151.png' },
    { label: '隧道总数', unit: '(条)', value: String(o?.tunnelCount ?? 0), icon: 'Group152.png' },
    { label: '总里程', unit: '(km)', value: String(o?.totalMileage ?? 0), icon: 'Group153.png' },
  ]
})

const deviceStatus = ref<DashboardDeviceStatusVo | null>(null)

const year = ref(new Date().getFullYear())
const powerOverview = ref<DashboardPowerOverviewVo | null>(null)

// 转换为 Card1/Card5 所需的 StatisticsVo 格式（接口返回 kWh，Card1 内部会除以 10000 转为万kWh 展示）
const sharedStatistics = computed<StatisticsVo | null>(() => {
  const o = powerOverview.value?.annualOverview
  if (!o) return null
  const consumption = Number(o.totalConsumption ?? 0)
  const saving = Number(o.totalSaving ?? 0)
  return {
    actualPowerConsumption: consumption,
    originalPowerConsumption: consumption + saving,
    theoreticalPowerSavingRate: o.powerSavingRate ?? 0,
    theoreticalCarbonEmissionReduction: o.carbonReductionRate ?? 0,
  }
})

// 转换为 Card6 所需的月度数据 { month, power, saving }，单位 kWh，Card6 内部换算为万kWh 展示
const monthlyData = computed(() => {
  const list = powerOverview.value?.monthlyData ?? []
  return list.map((d) => ({
    month: d.month ?? 0,
    power: Number(d.consumption ?? 0),
    saving: Number(d.saving ?? 0),
  }))
})

watch(
  year,
  async (y) => {
    try {
      powerOverview.value = await getUserPowerOverview(y)
    } catch {
      powerOverview.value = null
    }
  },
  { immediate: true }
)

// 隧道概况、设备状态分布
onMounted(async () => {
  try {
    tunnelOverview.value = await getTunnelOverview()
  } catch {
    tunnelOverview.value = null
  }
  try {
    deviceStatus.value = await getDeviceStatusDistribution()
  } catch {
    deviceStatus.value = null
  }
})
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 49px 91px 240px 1fr;
  gap: 20px;
  width: 100%;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

/* 年度数据概览：标题行 49px（与隧道选择框一致）+ 内容区 91px */
.dashboard-card-1 {
  grid-column: 1 / 4;
  grid-row: 1 / 3;
}

/* 隧道概况、设备状态、节能效率：各占1列，等高且底部水平线对齐 */
.dashboard-card-3,
.dashboard-card-4,
.dashboard-card-5 {
  margin-top: 3px;
  align-self: stretch;
}
.dashboard-card-3 {
  grid-column: 1;
  grid-row: 3;
}
.dashboard-card-4 {
  grid-column: 2;
  grid-row: 3;
}
.dashboard-card-5 {
  grid-column: 3;
  grid-row: 3;
}

/* 月度趋势：占前3列，下移 10px，占满剩余高度（第二行减 15px 后此处多出空间） */
.dashboard-card-6 {
  grid-column: 1 / 4;
  grid-row: 4;
  margin-top: -5px;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.dashboard-card {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) {
  .dashboard-grid {
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: auto;
    padding: 0;
  }

  .dashboard-card-1 {
    grid-column: unset;
    grid-row: unset;
    margin-top: 0;
    min-height: 120px;
  }

  .dashboard-card-3,
  .dashboard-card-4,
  .dashboard-card-5 {
    grid-column: unset;
    grid-row: unset;
    margin-top: 0;
    min-height: 200px;
  }

  .dashboard-card-6 {
    grid-column: unset;
    grid-row: unset;
    margin-top: 0;
    min-height: 400px;
    flex: 1;
  }
}
</style>
