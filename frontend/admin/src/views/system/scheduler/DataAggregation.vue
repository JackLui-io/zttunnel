<template>
  <div class="data-aggregation">
    <div class="page-header">
      <h2>数据汇总</h2>
      <p class="page-desc">将原始数据按日汇总到统计表，供分析接口快速检索。开关控制定时任务是否在每天凌晨自动执行。</p>
    </div>

    <el-row :gutter="20" class="task-cards" v-loading="statusLoading">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="task-card">
          <div class="task-card__icon traffic">
            <el-icon><Van /></el-icon>
          </div>
          <div class="task-card__content">
            <h3 class="task-card__title">同步车流/车速</h3>
            <p class="task-card__desc">将车流车速数据汇总到 tunnel_traffic_flow_day</p>
            <p class="task-card__cron">定时执行：每天 00:30</p>
            <div class="task-card__switch" :class="{ 'is-active': status.syncTrafficFlowDay }">
              <span class="switch-label">{{ status.syncTrafficFlowDay ? '已启动' : '已关闭' }}</span>
              <el-switch
                v-model="status.syncTrafficFlowDay"
                :loading="switchLoading.syncTrafficFlowDay"
                @change="(val) => toggleTask('syncTrafficFlowDay', val)"
                active-color="#67c23a"
              />
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <div class="task-card">
          <div class="task-card__icon brightness">
            <el-icon><Sunny /></el-icon>
          </div>
          <div class="task-card__content">
            <h3 class="task-card__title">同步洞内外亮度</h3>
            <p class="task-card__desc">将洞内外亮度数据汇总到 tunnel_inside_outside_day</p>
            <p class="task-card__cron">定时执行：每天 00:20</p>
            <div class="task-card__switch" :class="{ 'is-active': status.syncInsideOutsideDay }">
              <span class="switch-label">{{ status.syncInsideOutsideDay ? '已启动' : '已关闭' }}</span>
              <el-switch
                v-model="status.syncInsideOutsideDay"
                :loading="switchLoading.syncInsideOutsideDay"
                @change="(val) => toggleTask('syncInsideOutsideDay', val)"
                active-color="#67c23a"
              />
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <div class="task-card">
          <div class="task-card__icon carbon">
            <el-icon><Opportunity /></el-icon>
          </div>
          <div class="task-card__content">
            <h3 class="task-card__title">同步能碳数据</h3>
            <p class="task-card__desc">将能碳数据汇总到 tunnel_carbon_day</p>
            <p class="task-card__cron">定时执行：每天 00:10</p>
            <div class="task-card__switch" :class="{ 'is-active': status.syncCarbonDay }">
              <span class="switch-label">{{ status.syncCarbonDay ? '已启动' : '已关闭' }}</span>
              <el-switch
                v-model="status.syncCarbonDay"
                :loading="switchLoading.syncCarbonDay"
                @change="(val) => toggleTask('syncCarbonDay', val)"
                active-color="#67c23a"
              />
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <div class="task-card">
          <div class="task-card__icon power">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="task-card__content">
            <h3 class="task-card__title">检查电表数据</h3>
            <p class="task-card__desc">检查断电电表并补录数据到 tunnel_power_data</p>
            <p class="task-card__cron">定时执行：每天 00:40</p>
            <div class="task-card__switch" :class="{ 'is-active': status.checkPowerData }">
              <span class="switch-label">{{ status.checkPowerData ? '已启动' : '已关闭' }}</span>
              <el-switch
                v-model="status.checkPowerData"
                :loading="switchLoading.checkPowerData"
                @change="(val) => toggleTask('checkPowerData', val)"
                active-color="#67c23a"
              />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSchedulerStatus, updateSchedulerStatus } from '@/api/scheduler'

const statusLoading = ref(false)
const status = reactive({
  syncTrafficFlowDay: false,
  syncInsideOutsideDay: false,
  syncCarbonDay: false,
  checkPowerData: false
})

const switchLoading = reactive({
  syncTrafficFlowDay: false,
  syncInsideOutsideDay: false,
  syncCarbonDay: false,
  checkPowerData: false
})

const loadStatus = async () => {
  statusLoading.value = true
  try {
    const res = await getSchedulerStatus()
    if (res.code === 200 && res.data) {
      status.syncTrafficFlowDay = res.data.syncTrafficFlowDay ?? false
      status.syncInsideOutsideDay = res.data.syncInsideOutsideDay ?? false
      status.syncCarbonDay = res.data.syncCarbonDay ?? false
      status.checkPowerData = res.data.checkPowerData ?? false
    }
  } catch (error) {
    ElMessage.warning('无法获取任务状态，请确认后端已实现 /analyze/scheduler/status 接口')
  } finally {
    statusLoading.value = false
  }
}

const toggleTask = async (taskKey, enabled) => {
  switchLoading[taskKey] = true
  try {
    const res = await updateSchedulerStatus(taskKey, enabled)
    if (res.code === 200) {
      ElMessage.success(enabled ? '已启动' : '已关闭')
    } else {
      status[taskKey] = !enabled
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    status[taskKey] = !enabled
    ElMessage.error(error.message || '操作失败，请确认后端已实现 /analyze/scheduler/update 接口')
  } finally {
    switchLoading[taskKey] = false
  }
}

onMounted(() => {
  loadStatus()
})
</script>

<style lang="scss" scoped>
.data-aggregation {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .page-desc {
    font-size: 14px;
    color: #909399;
    line-height: 1.6;
  }
}

.task-cards {
  .el-col {
    margin-bottom: 20px;
  }
}

.task-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }

  &__icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
    margin-bottom: 16px;

    &.traffic {
      background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    }

    &.brightness {
      background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
    }

    &.carbon {
      background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
    }

    &.power {
      background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
    }
  }

  &__content {
    flex: 1;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
    margin-bottom: 8px;
  }

  &__cron {
    font-size: 12px;
    color: #909399;
    margin-bottom: 12px;
  }

  &__switch {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 6px;
    transition: background 0.2s;

    .switch-label {
      font-size: 13px;
      color: #606266;
      font-weight: 500;
    }

    &.is-active {
      background: #ecf5ff;
      .switch-label {
        color: #67c23a;
        font-weight: 600;
      }
    }
  }
}
</style>
