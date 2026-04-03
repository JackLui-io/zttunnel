<template>
  <div class="realtime-monitor">
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
            @change="handleCascaderChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>亮度对比（当天）</h3>
          </div>
          <div class="card-content">
            <div ref="brightnessChartRef" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>车流量统计（当天）</h3>
          </div>
          <div class="card-content">
            <div ref="trafficChartRef" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>平均车速（当天）</h3>
          </div>
          <div class="card-content">
            <div ref="speedChartRef" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>每小时电能参数（当天）</h3>
          </div>
          <div class="card-content">
            <div ref="energyChartRef" style="height: 300px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getRealTimeBrightness, getRealTimeTraffic, getRealTimeSpeed, getRealTimeEnergy } from '@/api/analyze'
import { useTunnelStore } from '@/stores/tunnel'
import { getCurrentDate } from '@/utils/datetime'

const tunnelStore = useTunnelStore()

// 图表引用
const brightnessChartRef = ref()
const trafficChartRef = ref()
const speedChartRef = ref()
const energyChartRef = ref()

let brightnessChart = null
let trafficChart = null
let speedChart = null
let energyChart = null
let refreshTimer = null

// 筛选表单
const filterForm = reactive({
  tunnelId: null
})

// 小时标签
const hourLabels = Array.from({ length: 24 }, (_, i) => `${i}:00`)

// 隧道级联选择变化
const handleCascaderChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

// getCurrentDate函数已从utils/datetime导入

// 加载亮度数据
// 数据来源：tunnel_edge_compute_data 表
// 接口：/analyze/zdByHouse
const loadBrightnessData = async () => {
  if (!filterForm.tunnelId) return
  try {
    const timeParam = getCurrentDate()
    const res = await getRealTimeBrightness({ tunnelId: filterForm.tunnelId, time: timeParam })
    if (res.code === 200 && res.data) {
      updateBrightnessChart(res.data)
    }
  } catch (error) {
    console.error('加载亮度数据失败:', error)
  }
}

// 加载车流量数据
// 数据来源：tunnel_edge_compute_data 表
// 接口：/analyze/clByHouse
const loadTrafficData = async () => {
  if (!filterForm.tunnelId) return
  try {
    const timeParam = getCurrentDate()
    const res = await getRealTimeTraffic({ tunnelId: filterForm.tunnelId, time: timeParam })
    if (res.code === 200 && res.data) {
      updateTrafficChart(res.data)
    }
  } catch (error) {
    console.error('加载车流量数据失败:', error)
  }
}

// 加载车速数据
// 数据来源：tunnel_edge_compute_data 表
// 接口：/analyze/csByHouse
const loadSpeedData = async () => {
  if (!filterForm.tunnelId) return
  try {
    const timeParam = getCurrentDate()
    const res = await getRealTimeSpeed({ tunnelId: filterForm.tunnelId, time: timeParam })
    if (res.code === 200 && res.data) {
      updateSpeedChart(res.data)
    }
  } catch (error) {
    console.error('加载车速数据失败:', error)
  }
}

// 加载电能数据
// 数据来源：tunnel_power_data 表
// 接口：/analyze/dnByHouse
const loadEnergyData = async () => {
  if (!filterForm.tunnelId) return
  try {
    const timeParam = getCurrentDate()
    const res = await getRealTimeEnergy({ tunnelId: filterForm.tunnelId, time: timeParam })
    if (res.code === 200 && res.data) {
      updateEnergyChart(res.data)
    }
  } catch (error) {
    console.error('加载电能数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  brightnessChart = echarts.init(brightnessChartRef.value)
  trafficChart = echarts.init(trafficChartRef.value)
  speedChart = echarts.init(speedChartRef.value)
  energyChart = echarts.init(energyChartRef.value)

  // 设置默认配置
  brightnessChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['洞外亮度', '调光比例'] },
    xAxis: { type: 'category', data: hourLabels },
    yAxis: [
      { type: 'value', name: 'cd/m²', position: 'left' },
      { type: 'value', name: '%', position: 'right', max: 100 }
    ],
    series: [
      { name: '洞外亮度', type: 'line', data: [], itemStyle: { color: '#E6A23C' } },
      { name: '调光比例', type: 'line', yAxisIndex: 1, data: [], itemStyle: { color: '#67C23A' } }
    ]
  })

  trafficChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['每小时车流量', '累计车流量'] },
    xAxis: { type: 'category', data: hourLabels },
    yAxis: [
      { type: 'value', name: '辆/h', position: 'left' },
      { type: 'value', name: '累计(辆)', position: 'right' }
    ],
    series: [
      { name: '每小时车流量', type: 'bar', yAxisIndex: 0, data: [], itemStyle: { color: '#409EFF' } },
      { name: '累计车流量', type: 'line', yAxisIndex: 1, data: [], itemStyle: { color: '#F56C6C' } }
    ]
  })

  speedChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均车速'] },
    xAxis: { type: 'category', data: hourLabels },
    yAxis: { type: 'value', name: 'km/h' },
    series: [
      { name: '平均车速', type: 'line', data: [], smooth: true, itemStyle: { color: '#67C23A' }, areaStyle: { color: 'rgba(103, 194, 58, 0.2)' } }
    ]
  })

  energyChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['耗电量', '理论节电量'] },
    xAxis: { type: 'category', data: hourLabels },
    yAxis: { type: 'value', name: 'kWh' },
    series: [
      { name: '耗电量', type: 'bar', data: [], itemStyle: { color: '#E6A23C' } },
      { name: '理论节电量', type: 'bar', data: [], itemStyle: { color: '#67C23A' } }
    ]
  })
}

// 从数据项中提取小时数
const extractHour = (item) => {
  // 优先使用 hour 字段（数字或字符串）
  if (item.hour !== undefined && item.hour !== null) {
    const h = parseInt(item.hour)
    if (!isNaN(h)) return h
  }
  // 尝试从 time 字段解析（格式：HH:mm 或 HH:mm:ss 或 H:00）
  if (item.time) {
    const match = item.time.match(/^(\d{1,2})/)
    if (match) return parseInt(match[1])
  }
  // 尝试从 uploadTime 字段解析（格式：YYYY-MM-DD HH:mm:ss 或 HH:mm:ss）
  if (item.uploadTime) {
    // 先尝试匹配日期时间格式
    let match = item.uploadTime.match(/\s(\d{1,2}):/)
    if (match) return parseInt(match[1])
    // 再尝试匹配纯时间格式
    match = item.uploadTime.match(/^(\d{1,2}):/)
    if (match) return parseInt(match[1])
  }
  return -1
}

// 更新亮度图表
// 数据来源：tunnel_edge_compute_data 表
// 字段：luminance（洞外亮度cd/m²）、dimming_ratio（调光比例%）
const updateBrightnessChart = (data) => {
  if (!brightnessChart) return
  const brightnessData = new Array(24).fill(0)
  const dimmingData = new Array(24).fill(0)

  console.log('亮度原始数据:', JSON.stringify(data))

  // 如果数据是数组，按小时填充
  if (Array.isArray(data)) {
    data.forEach((item, index) => {
      const hour = extractHour(item)
      const h = hour >= 0 ? hour : index
      if (h >= 0 && h < 24) {
        // 兼容多种字段名：后端可能返回 outsideBrightness/luminance/avgOutside 等
        brightnessData[h] = Number(item.outsideBrightness ?? item.luminance ?? item.avgOutside ?? item.outside ?? item.maxOutside ?? 0)
        dimmingData[h] = Number(item.dimmingRatio ?? item.avgDimmingRadio ?? item.dimmingRadio ?? item.dimming_ratio ?? 0)
      }
    })
  }

  console.log('亮度处理后数据:', { brightnessData, dimmingData })

  brightnessChart.setOption({
    series: [
      { name: '洞外亮度', data: brightnessData },
      { name: '调光比例', data: dimmingData }
    ]
  })
}

// 更新车流量图表
// 数据来源：tunnel_edge_compute_data 表
// 字段：traffic_flow（车流量）
const updateTrafficChart = (data) => {
  if (!trafficChart) return
  const hourlyData = new Array(24).fill(0)
  const cumulativeData = new Array(24).fill(0)

  console.log('车流量原始数据:', JSON.stringify(data))

  // 如果数据是数组，按小时填充
  if (Array.isArray(data)) {
    data.forEach((item, index) => {
      const hour = extractHour(item)
      const h = hour >= 0 ? hour : index
      if (h >= 0 && h < 24) {
        // 兼容多种字段名：count/avgTrafficFlow/trafficFlow/traffic_flow
        hourlyData[h] = Number(item.count ?? item.avgTrafficFlow ?? item.trafficFlow ?? item.traffic_flow ?? 0)
        cumulativeData[h] = Number(item.totalFlow ?? item.cumulativeTrafficFlow ?? item.cumulative_traffic_flow ?? 0)
      }
    })
  }

  // 如果没有累计数据，自动计算
  if (cumulativeData.every(v => v === 0) && hourlyData.some(v => v > 0)) {
    let cumulative = 0
    for (let i = 0; i < 24; i++) {
      cumulative += hourlyData[i]
      cumulativeData[i] = cumulative
    }
  }

  console.log('车流量处理后数据:', { hourlyData, cumulativeData })

  trafficChart.setOption({
    series: [
      { name: '每小时车流量', data: hourlyData },
      { name: '累计车流量', data: cumulativeData }
    ]
  })
}

// 更新车速图表
// 数据来源：tunnel_edge_compute_data 表
// 字段：speed1-20（车速数据，取平均值）
const updateSpeedChart = (data) => {
  if (!speedChart) return
  const speedData = new Array(24).fill(0)

  console.log('车速原始数据:', JSON.stringify(data))

  // 如果数据是数组，按小时填充
  if (Array.isArray(data)) {
    data.forEach((item, index) => {
      const hour = extractHour(item)
      const h = hour >= 0 ? hour : index
      if (h >= 0 && h < 24) {
        // 兼容多种字段名：speed/avgSpeed/avg_speed
        const val = Number(item.speed ?? item.avgSpeed ?? item.avg_speed ?? 0)
        speedData[h] = isNaN(val) ? 0 : Number(val.toFixed(1))
      }
    })
  }

  console.log('车速处理后数据:', speedData)

  speedChart.setOption({
    series: [{ name: '平均车速', data: speedData }]
  })
}

// 更新电能图表
// 数据来源：tunnel_power_data 表
// 字段：IMPEP电表数据的小时差值
const updateEnergyChart = (data) => {
  if (!energyChart) return
  const consumptionData = new Array(24).fill(0)
  const savingData = new Array(24).fill(0)

  console.log('电能原始数据:', JSON.stringify(data))

  // 如果数据是数组，按小时填充
  if (Array.isArray(data)) {
    data.forEach((item, index) => {
      const hour = extractHour(item)
      const h = hour >= 0 ? hour : index
      if (h >= 0 && h < 24) {
        // 兼容多种字段名：power/powerConsumption/dailyPowerConsumption
        consumptionData[h] = Number(item.power ?? item.powerConsumption ?? item.power_consumption ?? item.dailyPowerConsumption ?? 0)
        savingData[h] = Number(item.theoreticalPowerSavings ?? item.theoretical_power_savings ?? item.powerSaving ?? 0)
      }
    })
  }

  console.log('电能处理后数据:', { consumptionData, savingData })

  energyChart.setOption({
    series: [
      { name: '耗电量', data: consumptionData },
      { name: '理论节电量', data: savingData }
    ]
  })
}

// 刷新数据
const refreshData = async () => {
  await Promise.all([
    loadBrightnessData(),
    loadTrafficData(),
    loadSpeedData(),
    loadEnergyData()
  ])
}

// 监听隧道变化
watch(
  () => tunnelStore.currentTunnelId,
  (newVal) => {
    if (newVal) {
      filterForm.tunnelId = newVal
      refreshData()
    }
  }
)

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  await nextTick()
  initCharts()
  if (tunnelStore.currentTunnelId) {
    filterForm.tunnelId = tunnelStore.currentTunnelId
    refreshData()
  }

  // 每5分钟自动刷新
  refreshTimer = setInterval(refreshData, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.realtime-monitor {
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
