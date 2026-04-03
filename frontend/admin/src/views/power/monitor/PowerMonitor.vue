<template>
  <div class="power-monitor">
    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="隧道">
          <el-cascader
            :model-value="tunnelStore.currentTunnelPath"
            :options="tunnelStore.cascaderOptions"
            :props="{ expandTrigger: 'hover' }"
            placeholder="请选择隧道"
            clearable
            filterable
            style="width: 400px"
            @change="handleTunnelChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">刷新数据</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 实时电能图表 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="card">
          <div class="card-header">
            <h3>今日电能参数（按小时）</h3>
          </div>
          <div class="card-content">
            <div ref="energyChartRef" style="height: 400px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getRealTimeEnergy } from '@/api/analyze'
import { useTunnelStore } from '@/stores/tunnel'
import { getCurrentDate } from '@/utils/datetime'

const tunnelStore = useTunnelStore()

const energyChartRef = ref()
let energyChart = null

const filterForm = reactive({
  tunnelId: null
})

const hourLabels = Array.from({ length: 24 }, (_, i) => `${i}:00`)

// 隧道选择变化
const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

// 初始化图表
const initChart = () => {
  energyChart = echarts.init(energyChartRef.value)
  energyChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['耗电量', '理论节电量'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: hourLabels },
    yAxis: { type: 'value', name: 'kWh' },
    series: [
      { name: '耗电量', type: 'bar', data: new Array(24).fill(0), itemStyle: { color: '#E6A23C' } },
      { name: '理论节电量', type: 'bar', data: new Array(24).fill(0), itemStyle: { color: '#67C23A' } }
    ]
  })
}

// 加载数据
const handleSearch = async () => {
  if (!filterForm.tunnelId) return

  try {
    // 添加 time 参数，与 web 前端保持一致
    const res = await getRealTimeEnergy({ 
      tunnelId: filterForm.tunnelId,
      time: getCurrentDate()
    })
    if (res.code === 200 && res.data) {
      updateChart(res.data)
    }
  } catch (error) {
    console.error('加载电能数据失败:', error)
  }
}

// 更新图表
const updateChart = (data) => {
  if (!energyChart) return

  const consumptionData = new Array(24).fill(0)
  const savingData = new Array(24).fill(0)

  data.forEach(item => {
    const hour = item.hour || 0
    if (hour >= 0 && hour < 24) {
      consumptionData[hour] = item.powerConsumption || 0
      // 修复字段名：后端返回的是 theoreticalPowerSavings，不是 powerSaving
      savingData[hour] = item.theoreticalPowerSavings || 0
    }
  })

  energyChart.setOption({
    series: [
      { name: '耗电量', data: consumptionData },
      { name: '理论节电量', data: savingData }
    ]
  })
}

// 监听隧道变化
watch(
  () => tunnelStore.currentTunnelId,
  (newVal) => {
    if (newVal) {
      filterForm.tunnelId = newVal
      handleSearch()
    }
  }
)

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  await nextTick()
  initChart()
  if (tunnelStore.currentTunnelId) {
    filterForm.tunnelId = tunnelStore.currentTunnelId
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
.power-monitor {
  .filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);

    .card-header {
      padding: 16px 20px;
      border-bottom: 1px solid #ebeef5;

      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }

    .card-content {
      padding: 20px;
    }
  }
}
</style>
