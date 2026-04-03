<template>
  <div class="brightness-monitor">
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
            <h4>洞外亮度趋势（小时）</h4>
            <div ref="brightnessChartRef" class="chart-container"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card">
            <h4>调光比例趋势（小时）</h4>
            <div ref="dimmingChartRef" class="chart-container"></div>
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
import { getBrightnessByHour } from '@/api/analyze'
import { getCurrentDate } from '@/utils/datetime'

const tunnelStore = useTunnelStore()
const brightnessChartRef = ref(null)
const dimmingChartRef = ref(null)
let brightnessChart = null
let dimmingChart = null

// 当前隧道ID（从store获取）
const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const searchForm = reactive({
  date: getCurrentDate()
})

const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

const initCharts = () => {
  if (brightnessChartRef.value) {
    brightnessChart = echarts.init(brightnessChartRef.value)
    brightnessChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['洞外亮度', '当天最大', '当天最小'] },
      xAxis: { type: 'category', data: Array.from({ length: 24 }, (_, i) => `${i}:00`) },
      yAxis: { type: 'value', name: '亮度(cd/m²)' },
      series: [
        { name: '洞外亮度', type: 'line', data: [], smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409EFF' } },
        { name: '当天最大', type: 'line', data: [], lineStyle: { type: 'dashed', color: '#E6A23C' }, itemStyle: { color: '#E6A23C' }, symbol: 'none' },
        { name: '当天最小', type: 'line', data: [], lineStyle: { type: 'dashed', color: '#67C23A' }, itemStyle: { color: '#67C23A' }, symbol: 'none' }
      ]
    })
  }
  if (dimmingChartRef.value) {
    dimmingChart = echarts.init(dimmingChartRef.value)
    dimmingChart.setOption({
      tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: {c}%' },
      xAxis: { type: 'category', data: Array.from({ length: 24 }, (_, i) => `${i}:00`) },
      yAxis: { type: 'value', name: '调光比例(%)', max: 100 },
      series: [{ name: '调光比例', type: 'line', data: [], smooth: true, itemStyle: { color: '#67C23A' }, areaStyle: { opacity: 0.3 } }]
    })
  }
}

// 加载亮度数据
// 数据来源：tunnel_edge_compute_data 表
// 接口：/analyze/zdByHouse
// 数据项：luminance/outsideBrightness/avgOutside（洞外亮度）、dimmingRatio/avgDimmingRadio（调光比例）
const loadData = async () => {
  if (!currentTunnelId.value) {
    return // 静默返回，不显示警告
  }
  try {
    // 使用 time 参数传递日期，与 web 前端保持一致
    const res = await getBrightnessByHour({ tunnelId: currentTunnelId.value, time: searchForm.date })

    if (res.code === 200) {
      const data = res.data || []
      const avgData = Array(24).fill(0), maxData = Array(24).fill(0), minData = Array(24).fill(0), dimmingData = Array(24).fill(0)

      console.log('亮度原始数据:', JSON.stringify(data))

      // 用于存储每小时的所有亮度值
      const hourlyBrightnessValues = Array.from({ length: 24 }, () => [])
      // 收集所有原始亮度值，用于计算当天最大最小
      const allRawBrightnessValues = []

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
        // 如果无法解析小时，使用索引
        const h = hour >= 0 ? hour : index
        if (h >= 0 && h < 24) {
          // 兼容多种字段名：luminance、outsideBrightness、avgOutside、outside
          const brightness = Number(item.luminance ?? item.outsideBrightness ?? item.avgOutside ?? item.outside ?? 0)
          if (brightness > 0) {
            hourlyBrightnessValues[h].push(brightness)
            allRawBrightnessValues.push(brightness)
          }
          // 兼容多种字段名：dimmingRatio、avgDimmingRadio、dimmingRadio
          dimmingData[h] = Number(item.dimmingRatio ?? item.avgDimmingRadio ?? item.dimmingRadio ?? 0)
        }
      })

      // 先从原始数据计算当天的最大值和最小值
      const dayMax = allRawBrightnessValues.length > 0 ? Math.max(...allRawBrightnessValues) : 0
      const dayMin = allRawBrightnessValues.length > 0 ? Math.min(...allRawBrightnessValues) : 0

      // 再计算每小时的平均值
      for (let h = 0; h < 24; h++) {
        const values = hourlyBrightnessValues[h]
        if (values.length > 0) {
          avgData[h] = Number((values.reduce((a, b) => a + b, 0) / values.length).toFixed(1))
        }
      }

      // 生成水平参考线数据（每小时都是相同的值）
      for (let h = 0; h < 24; h++) {
        maxData[h] = dayMax
        minData[h] = dayMin
      }

      console.log('亮度处理后数据:', { avgData, dayMax, dayMin, dimmingData })

      brightnessChart?.setOption({ series: [{ data: avgData }, { data: maxData }, { data: minData }] })
      dimmingChart?.setOption({ series: [{ data: dimmingData }] })
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleSearch = () => loadData()

const handleResize = () => {
  brightnessChart?.resize()
  dimmingChart?.resize()
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
  brightnessChart?.dispose()
  dimmingChart?.dispose()
})
</script>

<style lang="scss" scoped>
.brightness-monitor {
  .search-form { margin-bottom: 20px; padding: 20px; background: #f8f9fa; border-radius: 8px; }
  .chart-card { background: #fff; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    h4 { margin: 0 0 15px 0; color: #303133; }
    .chart-container { height: 350px; }
  }
}
</style>
