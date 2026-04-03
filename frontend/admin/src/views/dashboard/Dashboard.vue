<template>
  <div class="dashboard">
    <!-- 顶部操作栏 -->
    <div class="dashboard-toolbar">
      <div class="toolbar-title">
        <h2>隧道管控平台</h2>
      </div>
      <div class="toolbar-actions">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="dashboard-header">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon tunnel">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">隧道总数</div>
            <div class="stat-value">{{ stats.tunnelCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon device">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">设备总数</div>
            <div class="stat-value">{{ stats.deviceCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon online">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">在线设备</div>
            <div class="stat-value">{{ stats.onlineCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon power">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">年累计节电量(万kWh)</div>
            <div class="stat-value">{{ stats.powerSaving }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-content dashboard-fe-cards">
      <el-col :xs="24" :lg="9">
        <DashboardDeviceStatusCard :device-status="deviceStatusVo" />
      </el-col>
      <el-col :xs="24" :lg="15">
        <DashboardMonthlyTrendCard :monthly-rows="monthlyChartRows" />
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="8">
        <div class="card">
          <div class="card-header">
            <h3>最新通知</h3>
          </div>
          <div class="card-content">
            <el-timeline>
              <el-timeline-item
                v-for="notice in notices"
                :key="notice.id"
                :timestamp="notice.time"
                :type="notice.type"
              >
                {{ notice.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </el-col>
      <el-col :span="16">
        <div class="card">
          <div class="card-header">
            <h3>隧道列表</h3>
          </div>
          <div class="card-content">
            <el-table :data="tunnels" style="width: 100%">
              <el-table-column prop="name" label="隧道名称" />
              <el-table-column prop="mileage" label="里程(km)" />
              <el-table-column prop="deviceCount" label="设备数量" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewTunnel(scope.row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAllTunnelTree } from '@/api/tunnel'
import { getDeviceStatusDistribution } from '@/api/device'
import { getNoticeList } from '@/api/notice'
import { getUserPowerOverview } from '@/api/analyze'
import DashboardDeviceStatusCard from '@/components/dashboard/DashboardDeviceStatusCard.vue'
import DashboardMonthlyTrendCard from '@/components/dashboard/DashboardMonthlyTrendCard.vue'

const router = useRouter()
import { getCurrentDateTime } from '@/utils/datetime'

/** 设备状态分布（与 frontend Card4 同源字段） */
const deviceStatusVo = ref(null)
/** 月度用电/节电 kWh，{ month, totalLight, totalEconomyLight } */
const monthlyChartRows = ref([])

// 统计数据
const stats = ref({
  tunnelCount: 0,
  deviceCount: 0,
  onlineCount: 0,
  powerSaving: '0.00'
})

// 通知数据
const notices = ref([])

// 隧道数据
const tunnels = ref([])

// 加载隧道数据
const loadTunnelData = async () => {
  try {
    const res = await getAllTunnelTree()
    if (res.code === 200 && res.data) {
      // 统计隧道数量（level=3的是具体隧道，且status=0为有效状态）
      const countTunnels = (list) => {
        let count = 0
        for (const item of list) {
          if (item.level === 3 && item.status === 0) {
            count++
          }
          if (item.children && item.children.length > 0) {
            count += countTunnels(item.children)
          }
        }
        return count
      }
      stats.value.tunnelCount = countTunnels(res.data)

      // 提取level=3且status=0的有效隧道用于列表显示
      const extractTunnels = (list, result = []) => {
        for (const item of list) {
          if (item.level === 3 && item.status === 0) {
            result.push({
              id: item.id,
              name: item.tunnelName,
              mileage: item.tunnelMileage || 0,
              deviceCount: 0,
              status: '正常'
            })
          }
          if (item.children && item.children.length > 0) {
            extractTunnels(item.children, result)
          }
        }
        return result
      }
      tunnels.value = extractTunnels(res.data).slice(0, 5) // 只显示前5个
    }
  } catch (error) {
    console.error('加载隧道数据失败:', error)
  }
}

// 加载设备统计数据（后端按当前用户可见隧道汇总，无单独故障字段）
const loadDeviceStats = async () => {
  try {
    const res = await getDeviceStatusDistribution()
    if (res.code === 200 && res.data) {
      const d = res.data
      const total = Number(d.total ?? 0)
      const online = Number(d.online ?? 0)
      const offline = Number(d.offline ?? 0)
      stats.value.deviceCount = total
      stats.value.onlineCount = online
      deviceStatusVo.value = d
    }
  } catch (error) {
    console.error('加载设备统计失败:', error)
    deviceStatusVo.value = null
  }
}

// 加载通知数据
const loadNoticeData = async () => {
  try {
    const res = await getNoticeList({ pageNum: 1, pageSize: 5 })
    if (res.code === 200) {
      const rows = res.rows || []
      notices.value = rows.map(item => ({
        id: item.id,
        content: item.content,
        time: item.createTime,
        type: getNoticeType(item.type)
      }))
    }
  } catch (error) {
    console.error('加载通知数据失败:', error)
    // 使用默认数据
    notices.value = [
      { id: 1, content: '系统运行正常', time: getCurrentDateTime(), type: 'success' }
    ]
  }
}

// 加载月度能耗数据（userPowerOverview：月度趋势 + 年累计节电量）
const loadMonthlyData = async () => {
  try {
    const currentYear = String(new Date().getFullYear())
    const res = await getUserPowerOverview({ year: currentYear })
    if (res.code === 200 && res.data) {
      const monthly = res.data.monthlyData || []
      const chartRows = monthly.map((m) => ({
        month: m.month,
        totalLight: Number(m.consumption ?? 0),
        totalEconomyLight: Number(m.saving ?? 0)
      }))
      monthlyChartRows.value = chartRows
      const totalSaving = res.data.annualOverview?.totalSaving
      const kwh =
        totalSaving != null && totalSaving !== '' ? Number(totalSaving) : 0
      // 后端为 kWh，展示为万 kWh（1 万 kWh = 10^4 kWh）
      stats.value.powerSaving = Number.isFinite(kwh)
        ? (kwh / 10000).toFixed(2)
        : '0.00'
    }
  } catch (error) {
    console.error('加载月度数据失败:', error)
    monthlyChartRows.value = []
  }
}

const getNoticeType = (type) => {
  const typeMap = {
    1: 'info',
    2: 'primary',
    3: 'danger',
    4: 'warning'
  }
  return typeMap[type] || 'info'
}

// 查看隧道详情
const viewTunnel = (tunnel) => {
  router.push({ path: '/tunnel/list', query: { id: tunnel.id } })
}

// 刷新数据
const refreshData = async () => {
  ElMessage.info('正在刷新数据...')
  try {
    await Promise.all([
      loadTunnelData(),
      loadDeviceStats(),
      loadNoticeData(),
      loadMonthlyData()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  }
}

onMounted(async () => {
  await Promise.all([
    loadTunnelData(),
    loadDeviceStats(),
    loadNoticeData(),
    loadMonthlyData()
  ])
})
</script>

<style lang="scss" scoped>
.dashboard {
  .dashboard-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .toolbar-title {
      h2 {
        margin: 0;
        color: #303133;
        font-size: 20px;
        font-weight: 600;
      }
    }

    .toolbar-actions {
      display: flex;
      gap: 12px;
    }
  }

  .dashboard-header {
    margin-bottom: 20px;
  }

  .dashboard-content {
    margin-bottom: 20px;
  }

  /* 设备分布 / 用电能耗 两卡同高，环图容器有明确高度避免 ECharts 不绘制 */
  .dashboard-fe-cards {
    margin-bottom: 20px;
    align-items: stretch;

    :deep(.el-col) {
      display: flex;
      align-items: stretch;
    }

    :deep(.el-col > *) {
      flex: 1;
      width: 100%;
      min-width: 0;
    }

    :deep(.fe-card) {
      height: 100%;
      margin-bottom: 0;
      min-height: 0;
    }
  }
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;

    .el-icon {
      font-size: 24px;
      color: #fff;
    }

    &.tunnel {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.device {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.online {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    &.power {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }

  .stat-content {
    flex: 1;

    .stat-title {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
  }
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;

  .card-header {
    padding: 20px 20px 0;
    border-bottom: 1px solid #ebeef5;

    h3 {
      margin: 0 0 20px 0;
      font-size: 16px;
      color: #303133;
    }
  }

  .card-content {
    padding: 20px;
  }
}
</style>
