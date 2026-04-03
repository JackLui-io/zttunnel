<template>
  <div class="edge-controller-table">
    <div class="table-header">
      <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
      <el-button type="success" size="small" @click="handleRefresh">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport">导出</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="deviceId" label="设备号" width="150" />
      <el-table-column prop="online" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.online)">
            {{ getStatusText(scope.row.online) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastUpdate" label="最后数据" width="170" />
      <el-table-column prop="softVer" label="主板固件" width="120" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleControl(scope.row)">控制</el-button>
          <el-button type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 控制弹窗 -->
    <el-dialog v-model="controlDialogVisible" title="边缘控制器控制" width="1100px" :close-on-click-modal="false">
      <div class="control-tabs">
        <div class="tab-item" :class="{ active: activeTab === 'params' }" @click="activeTab = 'params'">参数</div>
        <div class="tab-item" :class="{ active: activeTab === 'bluetooth' }" @click="activeTab = 'bluetooth'">蓝牙节点</div>
      </div>

      <!-- 参数Tab -->
      <div v-if="activeTab === 'params'" class="params-content">
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">ID：</span>
            <el-input v-model="controlForm.deviceId" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('id')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('id')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">蓝牙ID：</span>
            <el-input v-model="controlForm.bluetoothId" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('bluetoothId')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('bluetoothId')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">边缘状态上报间隔（s）：</span>
            <el-input v-model="controlForm.edgeReportInterval" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('edgeReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('edgeReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具状态上报间隔（s）：</span>
            <el-input v-model="controlForm.lampReportInterval" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampReport')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">灯具与边缘通信超时（s）：</span>
            <el-input v-model="controlForm.commTimeout" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('commTimeout')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('commTimeout')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具终端同步周期（s）：</span>
            <el-input v-model="controlForm.syncPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('syncPeriod')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('syncPeriod')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">设置灯具终端状态上报时间（s）：</span>
            <el-input v-model="controlForm.lampStatusReportTime" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampStatusReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampStatusReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具超时周期：</span>
            <el-input v-model="controlForm.lampTimeoutPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampTimeout')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampTimeout')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">设置工况汇报周期（s）：</span>
            <el-input v-model="controlForm.workReportPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('workReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('workReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具终端限制设置默认周期：</span>
            <el-input v-model="controlForm.lampLimitPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampLimit')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampLimit')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">灯具列表：</span>
            <el-input v-model="controlForm.lampList" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('lampList')">下发</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具参数下发：</span>
            <el-input v-model="controlForm.lampParams" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('lampParams')">下发</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampParams')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">蓝牙节点：</span>
            <el-input v-model="controlForm.bluetoothNode" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('bluetoothNode')">下发</div>
              <div class="param-btn update-btn" @click="handleUpdate('bluetoothNode')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">亮度仪波特率：</span>
            <el-input v-model="controlForm.brightnessBaudRate" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn update-btn" @click="handleUpdate('brightnessBaud')">更新</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 蓝牙节点Tab -->
      <div v-else class="bluetooth-content">
        <el-table :data="bluetoothNodes" style="width: 100%" height="300">
          <el-table-column prop="nodeId" label="蓝牙节点" width="400" />
          <el-table-column prop="isDirect" label="是否直连" width="200">
            <template #default="scope">
              {{ scope.row.isDirect ? '是' : '否' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="controlDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveControl">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isAdd ? '新增边缘控制器' : '编辑边缘控制器'" width="500px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="设备号">
          <el-input v-model="editForm.deviceId" :disabled="!isAdd" placeholder="请输入设备号" />
        </el-form-item>
        <el-form-item label="设备密码">
          <el-input v-model="editForm.devicePassword" placeholder="请输入设备密码" />
        </el-form-item>
        <el-form-item label="设备桩号">
          <el-input v-model="editForm.deviceNum" placeholder="请输入设备桩号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="editSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getEdgeControllerList, updateDevicelist } from '@/api/tunnelParam'
import { addDevicelist } from '@/api/device'
import { getStatusType, getStatusText } from '@/utils/deviceStatus'

// 获取雷达/亮度仪状态文本（独立状态：正常、异常、未安装）
const getSensorStatusText = (status) => {
  if (status === null || status === undefined || status === '') return '未安装'
  if (status === '正常' || status === 0) return '正常'
  if (status === '异常' || status === 1) return '异常'
  if (status === '未安装' || status === 2) return '未安装'
  return '未安装'
}

// 获取雷达/亮度仪状态标签类型
const getSensorStatusType = (status) => {
  if (status === null || status === undefined || status === '') return 'info'
  if (status === '正常' || status === 0) return 'success'
  if (status === '异常' || status === 1) return 'danger'
  if (status === '未安装' || status === 2) return 'info'
  return 'info'
}

const props = defineProps({
  tunnelId: {
    type: [Number, String],
    required: true
  }
})

const loading = ref(false)
const tableData = ref([])
const controlDialogVisible = ref(false)
const activeTab = ref('params')
const currentDevice = ref({})
const controlForm = ref({
  deviceId: '',
  bluetoothId: '',
  edgeReportInterval: '',
  lampReportInterval: '',
  commTimeout: '',
  syncPeriod: '',
  lampStatusReportTime: '',
  lampTimeoutPeriod: '',
  workReportPeriod: '',
  lampLimitPeriod: '',
  lampList: '',
  lampParams: '',
  bluetoothNode: '',
  brightnessBaudRate: ''
})
const bluetoothNodes = ref([])
const editDialogVisible = ref(false)
const editSaving = ref(false)
const isAdd = ref(false)
const editForm = ref({
  id: null,
  deviceId: '',
  devicePassword: '',
  deviceNum: ''
})

// 将字符串状态转换为数字
const statusStringToNumber = (status) => {
  if (status === '正常') return 0
  if (status === '异常') return 1
  if (status === '未安装') return 2
  if (typeof status === 'number') return status
  return 2
}

// 将数字状态转换为字符串
const statusNumberToString = (status) => {
  if (status === 0) return '正常'
  if (status === 1) return '异常'
  if (status === 2) return '未安装'
  return '未安装'
}

const loadData = async () => {
  if (!props.tunnelId) return

  loading.value = true
  try {
    const res = await getEdgeControllerList({ tunnelId: props.tunnelId })
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('获取边缘控制器列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  loadData()
}

// 导出Excel
const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  // 新数据库缺失字段：雷达状态、亮度仪状态，已从导出中移除
  const headers = ['设备号', '状态', '最后数据', '主板固件']
  const data = tableData.value.map(row => [
    row.deviceId || '',
    getStatusText(row.online),
    row.lastUpdate || '',
    row.softVer || ''
  ])

  // 生成CSV内容
  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `边缘控制器_${new Date().toLocaleDateString()}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const handleControl = (row) => {
  currentDevice.value = row
  activeTab.value = 'params'
  controlForm.value = {
    deviceId: row.deviceId || '',
    bluetoothId: '',
    edgeReportInterval: '',
    lampReportInterval: '',
    commTimeout: '',
    syncPeriod: '',
    lampStatusReportTime: '',
    lampTimeoutPeriod: '',
    workReportPeriod: '',
    lampLimitPeriod: '',
    lampList: '',
    lampParams: '',
    bluetoothNode: '',
    brightnessBaudRate: ''
  }
  bluetoothNodes.value = []
  controlDialogVisible.value = true
}

const handleRead = (type) => {
  ElMessage.info(`读取${type}参数功能开发中`)
}

const handleUpdate = (type) => {
  ElMessage.info(`更新${type}参数功能开发中`)
}

const handleIssue = (type) => {
  ElMessage.info(`下发${type}功能开发中`)
}

const handleSaveControl = () => {
  ElMessage.info('保存功能开发中')
  controlDialogVisible.value = false
}

// 新增边缘控制器
const handleAdd = () => {
  isAdd.value = true
  editForm.value = {
    id: null,
    deviceId: '',
    devicePassword: '',
    deviceNum: ''
    // 新数据库缺失以下字段，已移除
    // radarStatus: 2,
    // brightnessMeterStatus: 2
  }
  editDialogVisible.value = true
}

// 编辑边缘控制器
const handleEdit = (row) => {
  isAdd.value = false
  editForm.value = {
    id: row.id,
    deviceId: row.deviceId,
    devicePassword: row.devicePassword || '',
    deviceNum: row.deviceNum || ''
  }
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  if (isAdd.value && !editForm.value.deviceId) {
    ElMessage.warning('请输入设备号')
    return
  }
  editSaving.value = true
  try {
    const submitData = {
      ...editForm.value,
      tunnelId: props.tunnelId,
      deviceType: 1
    }
    const res = isAdd.value ? await addDevicelist(submitData) : await updateDevicelist(submitData)
    if (res.code === 200) {
      ElMessage.success(isAdd.value ? '新增成功' : '保存成功')
      editDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || (isAdd.value ? '新增失败' : '保存失败'))
    }
  } catch (error) {
    ElMessage.error(isAdd.value ? '新增失败' : '保存失败')
  } finally {
    editSaving.value = false
  }
}

watch(() => props.tunnelId, (newVal) => {
  if (newVal) {
    loadData()
  }
}, { immediate: true })

defineExpose({ loadData, handleAdd })
</script>

<style lang="scss" scoped>
.edge-controller-table {
  .table-header {
    margin-bottom: 15px;
    display: flex;
    gap: 10px;
  }
}

.control-tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;

  .tab-item {
    padding: 8px 20px;
    cursor: pointer;
    border-radius: 4px;
    background: #f5f7fa;
    color: #606266;
    transition: all 0.3s;

    &:hover {
      background: #e6f0ff;
    }

    &.active {
      background: #409eff;
      color: #fff;
    }
  }
}

.params-content {
  .param-row {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;

    .param-item {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 0;

      .param-label {
        flex-shrink: 0;
        min-width: 180px;
        text-align: right;
        color: #606266;
        font-size: 14px;
      }

      .param-input {
        width: 120px;
        flex-shrink: 0;
      }

      .param-buttons {
        display: flex;
        gap: 6px;
        flex-shrink: 0;

        .param-btn {
          width: 50px;
          height: 32px;
          font-size: 13px;
          line-height: 32px;
          text-align: center;
          cursor: pointer;
          border-radius: 4px;

          &.read-btn {
            background: #113f6a;
            color: #ebebeb;
          }

          &.update-btn {
            border: 1px solid #113f6a;
            color: #113f6a;
            background: transparent;

            &:hover {
              background: #113f6a;
              color: #ebebeb;
            }
          }

          &.issue-btn {
            background: #113f6a;
            color: #ebebeb;
          }
        }
      }
    }
  }
}

.bluetooth-content {
  min-height: 300px;
}
</style>
