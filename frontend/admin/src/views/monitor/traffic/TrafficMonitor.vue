<template>
  <div class="traffic-monitor">
    <div class="card">
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="隧道">
            <el-cascader
              :model-value="tunnelStore.currentTunnelPath"
              :options="tunnelStore.cascaderOptions"
              :props="{ expandTrigger: 'hover' }"
              placeholder="请选择隧道"
              clearable
              filterable
              style="width: 400px;"
              @change="handleTunnelChange"
            />
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker v-model="searchForm.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-card">
            <h4>车流量趋势（小时）</h4>
            <div ref="trafficChartRef" class="chart-container"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card">
            <h4>车速趋势（小时）</h4>
            <div ref="speedChartRef" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useTunnelStore } from '@/stores/tunnel'
import { getTrafficByHour, getSpeedByHour } from '@/api/analyze'
import { getCurrentDate } from '@/utils/datetime'

const tunnelStore = useTunnelStore()
const trafficChartRef = ref(null)
const speedChartRef = ref(null)
let trafficChart = null
let speedChart = null

// 当前隧道ID（从store获取）
const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const searchForm = reactive({
  date: getCurrentDate()
})

const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

const initCharts = () => {
  if (trafficChartRef.value) {
    trafficChart = echarts.init(trafficChartRef.value)
    trafficChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: Array.from({ length: 24 }, (_, i) => `${i}:00`) },
      yAxis: { type: 'value', name: '车流量(辆)' },
      series: [{ name: '车流量', type: 'bar', data: [], itemStyle: { color: '#409EFF' } }]
    })
  }
  if (speedChartRef.value) {
    speedChart = echarts.init(speedChartRef.value)
    speedChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['平均车速', '当天最大', '当天最小'] },
      xAxis: { type: 'category', data: Array.from({ length: 24 }, (_, i) => `${i}:00`) },
      yAxis: { type: 'value', name: '车速(km/h)' },
      series: [
        { name: '平均车速', type: 'line', data: [], smooth: true, itemStyle: { color: '#409EFF' } },
        { name: '当天最大', type: 'line', data: [], lineStyle: { type: 'dashed', color: '#E6A23C' }, itemStyle: { color: '#E6A23C' }, symbol: 'none' },
        { name: '当天最小', type: 'line', data: [], lineStyle: { type: 'dashed', color: '#67C23A' }, itemStyle: { color: '#67C23A' }, symbol: 'none' }
      ]
    })
  }
}

// 加载车流车速数据
// 数据来源：tunnel_edge_compute_data 表
// 车流量接口：/analyze/clByHouse
// 车速接口：/analyze/csByHouse
// 数据项：traffic_flow/trafficFlow（车流量）、avgSpeed/speed（车速）
const loadData = async () => {
  if (!currentTunnelId.value) {
    return // 静默返回，不显示警告
  }
  try {
    // 使用 time 参数传递日期，与 web 前端保持一致
    const params = { tunnelId: currentTunnelId.value, time: searchForm.date }

    const [trafficRes, speedRes] = await Promise.all([
      getTrafficByHour(params),
      getSpeedByHour(params)
    ])

    if (trafficRes.code === 200 && trafficChart) {
      const data = trafficRes.data || []
      console.log('车流量原始数据:', JSON.stringify(data))
      const trafficData = Array(24).fill(0)
      data.forEach((item, index) => {
        // 解析小时：优先使用 hour 字段，其次从 time/uploadTime 解析
        let hour = -1
        if (item.hour !== undefined && item.hour !== null) {
          hour = parseInt(item.hour)
        } else if (item.time) {
          const match = item.time.match(/^(\d{1,2})/)
          if (match) hour = parseInt(match[1])
        } else if (item.uploadTime) {
          const match = item.uploadTime.match(/(\d{1,2}):/)
          if (match) hour = parseInt(match[1])
        }
        const h = hour >= 0 ? hour : index
        if (h >= 0 && h < 24) {
          // 兼容多种字段名
          trafficData[h] = Number(item.trafficFlow ?? item.traffic_flow ?? item.count ?? item.avgTrafficFlow ?? item.value ?? 0)
        }
      })
      console.log('车流量处理后数据:', trafficData)
      trafficChart.setOption({ series: [{ data: trafficData }] })
    }

    if (speedRes.code === 200 && speedChart) {
      const data = speedRes.data || []
      console.log('车速原始数据:', JSON.stringify(data))
      const avgData = Array(24).fill(0)
      // 收集所有原始车速值，用于计算当天最大最小
      const allRawSpeedValues = []

      data.forEach((item, index) => {
        // 解析小时
        let hour = -1
        if (item.hour !== undefined && item.hour !== null) {
          hour = parseInt(item.hour)
        } else if (item.time) {
          const match = item.time.match(/^(\d{1,2})/)
          if (match) hour = parseInt(match[1])
        } else if (item.uploadTime) {
          const match = item.uploadTime.match(/(\d{1,2}):/)
          if (match) hour = parseInt(match[1])
        }
        const h = hour >= 0 ? hour : index

        if (h >= 0 && h < 24) {
          // 优先使用后端返回的 speedValues 数组（包含所有原始车速值）
          if (item.speedValues && Array.isArray(item.speedValues) && item.speedValues.length > 0) {
            item.speedValues.forEach(v => {
              if (v > 0) allRawSpeedValues.push(Number(v))
            })
          }
          // 使用后端计算好的平均值
          const speed = Number(item.avgSpeed ?? item.speed ?? 0)
          if (speed > 0) {
            avgData[h] = speed
          }
        }
      })

      // 从原始数据计算当天的最大值和最小值
      const dayMax = allRawSpeedValues.length > 0 ? Math.max(...allRawSpeedValues) : 0
      const dayMin = allRawSpeedValues.length > 0 ? Math.min(...allRawSpeedValues) : 0

      // 生成水平参考线数据（每小时都是相同的值）
      const maxData = Array(24).fill(dayMax)
      const minData = Array(24).fill(dayMin)

      console.log('车速处理后数据:', { avgData, dayMax, dayMin, rawCount: allRawSpeedValues.length })
      speedChart.setOption({ series: [{ data: avgData }, { data: maxData }, { data: minData }] })
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleSearch = () => loadData()

const handleResize = () => {
  trafficChart?.resize()
  speedChart?.resize()
}

// 监听隧道变化，自动加载数据
watch(
  () => tunnelStore.currentTunnelId,
  (newVal) => {
    if (newVal) {
      loadData()
    }
  }
)

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  initCharts()
  window.addEventListener('resize', handleResize)

  // 如果已有选中的隧道，自动加载数据
  if (currentTunnelId.value) {
    loadData()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trafficChart?.dispose()
  speedChart?.dispose()
})
</script>

<style lang="scss" scoped>
.traffic-monitor {
  .search-form { margin-bottom: 20px; padding: 20px; background: #f8f9fa; border-radius: 8px; }
  .chart-card { background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    h4 { margin: 0 0 15px 0; color: #303133; }
    .chart-container { height: 350px; }
  }
}
</style>
