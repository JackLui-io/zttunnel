<template>
  <div class="carbon-data full-width-page">
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
            class="tunnel-cascader-wide"
            @change="handleTunnelChange"
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
            @change="handleSearch"
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
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="card">
      <div class="card-header">
        <h3>能碳数据列表</h3>
      </div>
      <div class="card-content table-card-body">
        <div class="table-responsive">
        <el-table
          :data="tableData"
          v-loading="loading"
          style="width: 100%"
          table-layout="fixed"
        >
          <el-table-column prop="uploadTime" label="日期" min-width="110" show-overflow-tooltip />
          <el-table-column prop="dailyPowerConsumption" label="日耗电量(kWh)" min-width="120">
            <template #default="scope">
              {{ scope.row.dailyPowerConsumption?.toFixed(2) || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="theoreticalPowerSavings" label="理论节电量(kWh)" min-width="130">
            <template #default="scope">
              {{ scope.row.theoreticalPowerSavings?.toFixed(2) || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="theoreticalPowerSavingRate" label="理论节电率(%)" min-width="120">
            <template #default="scope">
              {{ scope.row.theoreticalPowerSavingRate?.toFixed(2) || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="theoreticalCarbonEmissions" label="理论碳减排(kg)" min-width="130">
            <template #default="scope">
              {{ scope.row.theoreticalCarbonEmissions?.toFixed(2) || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="cumulativePowerConsumption" label="累计耗电量(kWh)" min-width="136">
            <template #default="scope">
              {{ scope.row.cumulativePowerConsumption?.toFixed(2) || 0 }}
            </template>
          </el-table-column>
        </el-table>
        </div>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSearch"
          @current-change="handleSearch"
          style="margin-top: 20px; text-align: right;"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { getCarbon } from '@/api/analyze'
import { useTunnelStore } from '@/stores/tunnel'
import { formatDate } from '@/utils/datetime'

const tunnelStore = useTunnelStore()

const loading = ref(false)
const tableData = ref([])

const filterForm = reactive({
  tunnelId: null,
  dateRange: []
})

// 当前激活的时间范围
const activeTimeRange = ref('1month')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 隧道选择变化
const handleTunnelChange = (path) => {
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
      startDate.setDate(today.getDate() - 29) // 默认近1个月
  }

  filterForm.dateRange = [formatDate(startDate), formatDate(today)]
  pagination.page = 1 // 重置分页
  handleSearch()
}

// 初始化默认日期范围
const initDateRange = () => {
  setTimeRange('1month')
}

// formatDate函数已从utils/datetime导入

// 加载数据
const handleSearch = async () => {
  if (!filterForm.tunnelId || !filterForm.dateRange || filterForm.dateRange.length < 2) return

  loading.value = true
  try {
    const res = await getCarbon({
      tunnelId: filterForm.tunnelId,
      startTime: filterForm.dateRange[0],
      endTime: filterForm.dateRange[1]
    })
    if (res.code === 200 && res.data) {
      const allData = res.data
      pagination.total = allData.length
      // 前端分页
      const start = (pagination.page - 1) * pagination.size
      const end = start + pagination.size
      tableData.value = allData.slice(start, end)
    }
  } catch (error) {
    console.error('加载能碳数据失败:', error)
  } finally {
    loading.value = false
  }
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
  initDateRange()
  if (tunnelStore.currentTunnelId) {
    filterForm.tunnelId = tunnelStore.currentTunnelId
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
.carbon-data.full-width-page {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.carbon-data {
  .tunnel-cascader-wide {
    width: min(400px, 100%);
    max-width: 100%;
  }

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

  .card {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
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

    .card-content.table-card-body {
      padding: 20px;
      min-width: 0;
    }
  }

  .table-responsive {
    display: block;
    width: 100%;
    min-width: 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;

    :deep(.el-table) {
      width: 100% !important;
      min-width: 720px;
    }
    :deep(.el-table__inner-wrapper),
    :deep(.el-table__body-wrapper),
    :deep(.el-table__header-wrapper) {
      width: 100%;
    }
  }
}
</style>
