<template>
  <div class="carbon-statistics">
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
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group class="time-range-buttons">
            <el-button
              :type="activeTimeRange === '7days' ? 'primary' : 'default'"
              @click="setTimeRange('7days')"
            >
              近7天
            </el-button>
            <el-button
              :type="activeTimeRange === '1month' ? 'primary' : 'default'"
              @click="setTimeRange('1month')"
            >
              近1个月
            </el-button>
            <el-button
              :type="activeTimeRange === '3months' ? 'primary' : 'default'"
              @click="setTimeRange('3months')"
            >
              近3个月
            </el-button>
          </el-button-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card saving-rate">
          <div class="stat-value">{{ statistics.theoreticalPowerSavingRate || 0 }}%</div>
          <div class="stat-title">理论节电率</div>
          <div class="stat-detail">
            <span>实际: {{ statistics.actualPowerConsumption || 0 }} kWh</span>
            <span>设计: {{ statistics.originalPowerConsumption || 0 }} kWh</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card power-reduction">
          <div class="stat-value">{{ statistics.theoreticalOperatingPowerReduction || 0 }} kW</div>
          <div class="stat-title">理论总功率降低</div>
          <div class="stat-detail">
            <span>实际: {{ statistics.actualOperatingPower || 0 }} kW</span>
            <span>设计: {{ statistics.originalOperatingPower || 0 }} kW</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card carbon-reduction">
          <div class="stat-value">{{ statistics.theoreticalCarbonEmissionReduction || 0 }}%</div>
          <div class="stat-title">理论碳减排率</div>
          <div class="stat-detail">
            <span>实际: {{ statistics.actualCarbonEmission || 0 }} tCO₂</span>
            <span>设计: {{ statistics.originalCarbonEmission || 0 }} tCO₂</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card light-reduction">
          <div class="stat-value">{{ statistics.theoreticalLightUpTimeReduction || 0 }}%</div>
          <div class="stat-title">理论亮灯时长削减率</div>
          <div class="stat-detail">
            <span>实际: {{ statistics.actualLightUpTime || 0 }} h</span>
            <span>设计: {{ statistics.originalLightUpTime || 0 }} h</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>亮灯/暗灯时长对比</h3>
          </div>
          <div class="card-content">
            <div ref="lightTimeChartRef" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>车流车速趋势</h3>
          </div>
          <div class="card-content">
            <div ref="trafficChartRef" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>洞外亮度与调光比例</h3>
          </div>
          <div class="card-content">
            <div ref="brightnessChartRef" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <div class="card-header">
            <h3>能碳数据趋势</h3>
          </div>
          <div class="card-content">
            <div ref="carbonChartRef" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getStatistics, getTrafficFlow, getInsideOutside, getCarbon } from '@/api/analyze'
import { useTunnelStore } from '@/stores/tunnel'
import { formatDate } from '@/utils/datetime'

const tunnelStore = useTunnelStore()

// 图表引用
const lightTimeChartRef = ref()
const trafficChartRef = ref()
const brightnessChartRef = ref()
const carbonChartRef = ref()

let lightTimeChart = null
let trafficChart = null
let brightnessChart = null
let carbonChart = null

// 筛选表单
const filterForm = reactive({
  tunnelId: null,
  dateRange: []
})

// 当前激活的时间范围
const activeTimeRange = ref('7days')

// 统计数据
const statistics = ref({})

// 隧道级联选择变化
const handleCascaderChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

// 设置时间范围
// 与web前端保持一致：近N天 = 从今天往前(N-1)天，共N天（包含今天）
// 例如：近7天，12月17日 - 6 = 12月11日，12月11日到12月17日共7天
const setTimeRange = (range) => {
  activeTimeRange.value = range
  const today = new Date()
  let startDate = new Date()

  switch (range) {
    case '7days':
      startDate.setDate(today.getDate() - 6)  // 7天 = 今天 - 6天
      break
    case '1month':
      startDate.setDate(today.getDate() - 29) // 30天 = 今天 - 29天
      break
    case '3months':
      startDate.setDate(today.getDate() - 89) // 90天 = 今天 - 89天
      break
    default:
      startDate.setDate(today.getDate() - 6)
  }

  filterForm.dateRange = [formatDate(startDate), formatDate(today)]
  handleSearch()
}

// 初始化默认日期范围
const initDateRange = () => {
  setTimeRange('7days')
}

// formatDate函数已从utils/datetime导入

// 加载统计数据
const loadStatistics = async () => {
  if (!filterForm.tunnelId || !filterForm.dateRange || filterForm.dateRange.length < 2) return
  try {
    const res = await getStatistics({
      tunnelId: filterForm.tunnelId,
      startTime: filterForm.dateRange[0],
      endTime: filterForm.dateRange[1]
    })
    if (res.code === 200 && res.data) {
      // 后端返回的是数组，取第一个元素
      const data = Array.isArray(res.data) ? res.data[0] : res.data
      if (data) {
        statistics.value = {
          // 节电率相关
          theoreticalPowerSavingRate: data.theoreticalPowerSavingRate || 0,
          actualPowerConsumption: data.actualPowerConsumption || 0,
          originalPowerConsumption: data.originalPowerConsumption || 0,
          // 功率降低相关
          theoreticalOperatingPowerReduction: data.theoreticalOperatingPowerReduction || 0,
          actualOperatingPower: data.actualOperatingPower || 0,
          originalOperatingPower: data.originalOperatingPower || 0,
          // 碳减排相关
          theoreticalCarbonEmissionReduction: data.theoreticalCarbonEmissionReduction || 0,
          actualCarbonEmission: data.actualCarbonEmission || 0,
          originalCarbonEmission: data.originalCarbonEmission || 0,
          // 亮灯时长相关
          theoreticalLightUpTimeReduction: data.theoreticalLightUpTimeReduction || 0,
          actualLightUpTime: data.actualLightUpTime || 0,
          originalLightUpTime: data.originalLightUpTime || 0
        }
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载车流车速数据
const loadTrafficData = async () => {
  if (!filterForm.tunnelId || !filterForm.dateRange || filterForm.dateRange.length < 2) return
  try {
    const res = await getTrafficFlow({
      tunnelId: filterForm.tunnelId,
      startTime: filterForm.dateRange[0],
      endTime: filterForm.dateRange[1]
    })
    if (res.code === 200 && res.data) {
      updateTrafficChart(res.data)
    }
  } catch (error) {
    console.error('加载车流车速数据失败:', error)
  }
}

// 加载亮度数据（同时用于亮灯/暗灯时长对比和洞外亮度与调光比例）
// 数据来源：tunnel_inside_outside_day 表
const loadBrightnessData = async () => {
  if (!filterForm.tunnelId || !filterForm.dateRange || filterForm.dateRange.length < 2) return
  try {
    const res = await getInsideOutside({
      tunnelId: filterForm.tunnelId,
      startTime: filterForm.dateRange[0],
      endTime: filterForm.dateRange[1]
    })
    if (res.code === 200 && res.data) {
      updateLightTimeChart(res.data)
      updateBrightnessChart(res.data)
    }
  } catch (error) {
    console.error('加载亮度数据失败:', error)
  }
}

// 加载能碳数据
const loadCarbonData = async () => {
  if (!filterForm.tunnelId || !filterForm.dateRange || filterForm.dateRange.length < 2) return
  try {
    const res = await getCarbon({
      tunnelId: filterForm.tunnelId,
      startTime: filterForm.dateRange[0],
      endTime: filterForm.dateRange[1]
    })
    if (res.code === 200 && res.data) {
      updateCarbonChart(res.data)
    }
  } catch (error) {
    console.error('加载能碳数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  lightTimeChart = echarts.init(lightTimeChartRef.value)
  trafficChart = echarts.init(trafficChartRef.value)
  brightnessChart = echarts.init(brightnessChartRef.value)
  carbonChart = echarts.init(carbonChartRef.value)
}

// 更新亮灯/暗灯时长对比图表
// 数据来源：tunnel_inside_outside_day 表的 light_up 和 light_down 字段
const updateLightTimeChart = (data) => {
  if (!lightTimeChart) return
  const dates = data.map(d => d.uploadTime)
  const lightUpData = data.map(d => Number(d.lightUp) || 0)
  const lightDownData = data.map(d => Number(d.lightDown) || 0)

  lightTimeChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['亮灯时长', '暗灯时长'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '小时' },
    series: [
      { name: '亮灯时长', type: 'bar', stack: 'total', data: lightUpData, itemStyle: { color: '#E6A23C' } },
      { name: '暗灯时长', type: 'bar', stack: 'total', data: lightDownData, itemStyle: { color: '#67C23A' } }
    ]
  })
}

// 更新车流车速图表
const updateTrafficChart = (data) => {
  if (!trafficChart) return
  const dates = data.map(d => d.uploadTime)
  const trafficData = data.map(d => d.trafficFlow || 0)
  const speedData = data.map(d => d.avgSpeed || 0)

  trafficChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['车流量', '平均车速'] },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '车流量(辆)', position: 'left' },
      { type: 'value', name: '车速(km/h)', position: 'right' }
    ],
    series: [
      { name: '车流量', type: 'bar', data: trafficData, itemStyle: { color: '#409EFF' } },
      { name: '平均车速', type: 'line', yAxisIndex: 1, data: speedData, itemStyle: { color: '#F56C6C' } }
    ]
  })
}

// 更新亮度图表
// 数据来源：tunnel_inside_outside_day 表的 avgOutside 和 avgDimmingRadio 字段
const updateBrightnessChart = (data) => {
  if (!brightnessChart) return
  const dates = data.map(d => d.uploadTime)
  const brightnessData = data.map(d => d.avgOutside || 0)
  const dimmingData = data.map(d => d.avgDimmingRadio || 0)

  brightnessChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['洞外亮度', '调光比例'] },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '亮度(cd/m²)', position: 'left' },
      { type: 'value', name: '调光比例(%)', position: 'right', max: 100 }
    ],
    series: [
      { name: '洞外亮度', type: 'line', data: brightnessData, itemStyle: { color: '#E6A23C' } },
      { name: '调光比例', type: 'line', yAxisIndex: 1, data: dimmingData, itemStyle: { color: '#67C23A' } }
    ]
  })
}

// 更新能碳图表
const updateCarbonChart = (data) => {
  if (!carbonChart) return
  const dates = data.map(d => d.uploadTime)
  const consumptionData = data.map(d => d.dailyPowerConsumption || 0)
  const savingsData = data.map(d => d.theoreticalPowerSavings || 0)

  carbonChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['日耗电量', '理论节电量'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: 'kWh' },
    series: [
      { name: '日耗电量', type: 'bar', data: consumptionData, itemStyle: { color: '#E6A23C' } },
      { name: '理论节电量', type: 'bar', data: savingsData, itemStyle: { color: '#67C23A' } }
    ]
  })
}

// 查询
const handleSearch = async () => {
  if (!filterForm.tunnelId) {
    ElMessage.warning('请选择隧道')
    return
  }
  if (!filterForm.dateRange || filterForm.dateRange.length < 2) {
    ElMessage.warning('请选择时间范围')
    return
  }
  await Promise.all([
    loadStatistics(),
    loadTrafficData(),
    loadBrightnessData(),
    loadCarbonData()
  ])
}

// 重置
const handleReset = () => {
  setTimeRange('7days')
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
  initCharts()

  // 先设置隧道ID，再初始化日期范围（initDateRange会触发handleSearch）
  if (tunnelStore.currentTunnelId) {
    filterForm.tunnelId = tunnelStore.currentTunnelId
  }
  initDateRange()
})
</script>

<style lang="scss" scoped>
.carbon-statistics {
  .filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);

    .time-range-buttons {
      margin-right: 10px;

      .el-button {
        padding: 8px 16px;
        font-size: 13px;
        border-radius: 0;

        &:first-child {
          border-top-left-radius: 4px;
          border-bottom-left-radius: 4px;
        }

        &:last-child {
          border-top-right-radius: 4px;
          border-bottom-right-radius: 4px;
        }
      }
    }
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
    }

    &.saving-rate::before { background: linear-gradient(90deg, #67C23A, #85ce61); }
    &.power-reduction::before { background: linear-gradient(90deg, #409EFF, #66b1ff); }
    &.carbon-reduction::before { background: linear-gradient(90deg, #E6A23C, #ebb563); }
    &.light-reduction::before { background: linear-gradient(90deg, #F56C6C, #f78989); }

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 8px;
    }

    .stat-title {
      font-size: 14px;
      color: #606266;
      margin-bottom: 12px;
    }

    .stat-detail {
      font-size: 12px;
      color: #909399;
      display: flex;
      justify-content: space-around;
    }
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
