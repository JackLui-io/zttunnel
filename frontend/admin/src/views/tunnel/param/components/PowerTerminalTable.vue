<template>
  <div class="power-terminal-table">
    <div class="table-header">
      <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
      <el-button type="success" size="small" @click="handleRefresh">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport">导出</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="deviceId" label="设备号" width="150" />
      <el-table-column prop="deviceNum" label="设备桩号" width="120" />
      <el-table-column prop="csq" label="信号强度" width="100" />
      <el-table-column prop="online" label="设备状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.online)">
            {{ getStatusText(scope.row.online) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="electricityMeterNum" label="电表数量" width="100">
        <template #default="scope">
          <el-button type="primary" link @click="goToMeterTab(scope.row)">
            {{ scope.row.electricityMeterNum || 0 }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleControl(scope.row)">控制</el-button>
          <el-button type="success" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="warning" size="small" @click="handleIssued(scope.row)">下发</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 控制弹窗 - 基础配置 -->
    <el-dialog v-model="controlDialogVisible" title="基础配置" width="900px" :close-on-click-modal="false" class="control-dialog">
      <div class="config-header">
        <div class="config-title">
          基础配置 <span class="device-id">设备号:{{ currentDevice.deviceId }}</span>
        </div>
        <el-button type="primary" class="restart-btn" @click="handleRestart">重启终端</el-button>
      </div>

      <div class="config-content">
        <!-- 服务器地址 -->
        <div class="config-row">
          <div class="config-item full-width">
            <span class="config-label">服务器地址：</span>
            <el-input v-model="configForm.serverAddress" class="config-input" placeholder="请输入服务器地址" />
          </div>
        </div>

        <!-- 端口 和 心跳周期 -->
        <div class="config-row">
          <div class="config-item">
            <span class="config-label">端口：</span>
            <el-input v-model="configForm.port" class="config-input" placeholder="请输入端口" />
            <div class="btn-group">
              <div class="config-btn read-btn" @click="handleRead(1)">读取</div>
              <div class="config-btn update-btn" @click="handleUpdate(1)">更新</div>
            </div>
          </div>
          <div class="config-item">
            <span class="config-label">心跳周期：</span>
            <el-input v-model="configForm.heartbeatCycle" class="config-input" placeholder="请输入心跳周期" />
            <div class="btn-group">
              <div class="config-btn read-btn" @click="handleRead(2)">读取</div>
              <div class="config-btn update-btn" @click="handleUpdate(2)">更新</div>
            </div>
          </div>
        </div>

        <!-- 读超时 和 写超时 -->
        <div class="config-row">
          <div class="config-item">
            <span class="config-label">读超时：</span>
            <el-input v-model="configForm.readTimeout" class="config-input" placeholder="请输入读超时" />
            <div class="btn-group">
              <div class="config-btn read-btn" @click="handleRead(3)">读取</div>
              <div class="config-btn update-btn" @click="handleUpdate(3)">更新</div>
            </div>
          </div>
          <div class="config-item">
            <span class="config-label">写超时：</span>
            <el-input v-model="configForm.writeTimeout" class="config-input" placeholder="请输入写超时" />
            <div class="btn-group">
              <div class="config-btn read-btn" @click="handleRead(4)">读取</div>
              <div class="config-btn update-btn" @click="handleUpdate(4)">更新</div>
            </div>
          </div>
        </div>

        <!-- 固件信息 -->
        <div class="config-row">
          <div class="config-item">
            <span class="config-label">固件信息：</span>
            <el-input v-model="configForm.firmwareInfo" class="config-input firmware-input" placeholder="固件信息" readonly />
            <div class="btn-group">
              <div class="config-btn firmware-btn" @click="handleRead(5)">读取固件信息</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="handleSaveConfig">参数保存</el-button>
        <el-button @click="controlDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isAdd ? '新增电能终端' : '编辑电能终端'" width="500px" :close-on-click-modal="false">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPowerTerminalList, issuedDevice, updateDevicelist } from '@/api/tunnelParam'
import { addDevicelist } from '@/api/device'
import { getStatusType, getStatusText } from '@/utils/deviceStatus'
import {
  readServerPort,
  updateServerPort,
  readHeartbeat,
  updateHeartbeat,
  readReadTimeout,
  updateReadTimeout,
  readWriteTimeout,
  updateWriteTimeout,
  readFirmware,
  restartDevice
} from '@/utils/deviceCommand'

const props = defineProps({
  tunnelId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['goToMeterTab'])

const loading = ref(false)
const tableData = ref([])
const controlDialogVisible = ref(false)
const currentDevice = ref({})
const configForm = ref({
  serverAddress: '',
  port: '',
  heartbeatCycle: '',
  readTimeout: '',
  writeTimeout: '',
  firmwareInfo: ''
})
const editDialogVisible = ref(false)
const editSaving = ref(false)
const isAdd = ref(false)
const editForm = ref({
  id: null,
  deviceId: '',
  devicePassword: '',
  deviceNum: ''
})

// 将设备桩号转换为里程格式
const formatMileage = (deviceNum) => {
  if (!deviceNum) return null
  const hexString = deviceNum.toString(16).toUpperCase()
  let bigNum = 0
  let smallNum = 0

  if (hexString.startsWith('1') && hexString.length > 2 && hexString.length <= 4) {
    bigNum = 1
    const remainingHex = hexString.substring(1)
    if (remainingHex) {
      smallNum = parseInt(remainingHex, 16)
    }
  } else if (hexString.length === 3 || hexString.length === 4) {
    const result = hexString.substring(hexString.length - 2)
    const big = hexString.substring(0, hexString.indexOf(result))
    bigNum = parseInt(big, 16)
    smallNum = parseInt(result, 16)
  } else if (hexString.length >= 5) {
    const result = hexString.substring(hexString.length - 4)
    const big = hexString.substring(0, hexString.indexOf(result))
    bigNum = parseInt(big, 16)
    smallNum = parseInt(result, 16)
  } else {
    smallNum = parseInt(hexString, 16)
  }

  return `${bigNum}+${smallNum}`
}

const loadData = async () => {
  if (!props.tunnelId) return

  loading.value = true
  try {
    const res = await getPowerTerminalList({ tunnelId: props.tunnelId })
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('获取电能终端列表失败:', error)
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

  const headers = ['设备号', '设备桩号', '信号强度', '设备状态', '电表数量']
  const data = tableData.value.map(row => [
    row.deviceId || '',
    row.deviceNum || '',
    row.csq || '',
    getStatusText(row.online),
    row.electricityMeterNum || 0
  ])

  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `电能终端_${new Date().toLocaleDateString()}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 打开控制弹窗
const handleControl = (row) => {
  currentDevice.value = row
  configForm.value = {
    serverAddress: '',
    port: '',
    heartbeatCycle: '',
    readTimeout: '',
    writeTimeout: '',
    firmwareInfo: ''
  }
  controlDialogVisible.value = true
}

// 读取参数
const handleRead = (type) => {
  const deviceNo = currentDevice.value.deviceId + ''

  switch (type) {
    case 1:
      // 读取服务器地址和端口
      readServerPort(deviceNo, (result) => {
        if (result) {
          configForm.value.serverAddress = result.server || ''
          configForm.value.port = result.port || ''
        }
      })
      break
    case 2:
      // 读取心跳周期
      readHeartbeat(deviceNo, (result) => {
        if (result) {
          configForm.value.heartbeatCycle = result.heartbeat || ''
        }
      })
      break
    case 3:
      // 读取读超时
      readReadTimeout(deviceNo, (result) => {
        if (result) {
          configForm.value.readTimeout = result.readTimeout || ''
        }
      })
      break
    case 4:
      // 读取写超时
      readWriteTimeout(deviceNo, (result) => {
        if (result) {
          configForm.value.writeTimeout = result.writeTimeout || ''
        }
      })
      break
    case 5:
      // 读取固件信息
      readFirmware(deviceNo, (result) => {
        if (result) {
          configForm.value.firmwareInfo = result.firmware || ''
        }
      })
      break
  }
}

// 更新参数
const handleUpdate = (type) => {
  const deviceNo = currentDevice.value.deviceId + ''
  const formData = configForm.value

  switch (type) {
    case 1:
      // 更新服务器地址和端口
      updateServerPort(deviceNo, formData.serverAddress, formData.port)
      break
    case 2:
      // 更新心跳周期
      updateHeartbeat(deviceNo, formData.heartbeatCycle)
      break
    case 3:
      // 更新读超时
      updateReadTimeout(deviceNo, formData.readTimeout)
      break
    case 4:
      // 更新写超时
      updateWriteTimeout(deviceNo, formData.writeTimeout)
      break
  }
}

// 重启终端
const handleRestart = () => {
  ElMessageBox.confirm('确认向终端发送重启指令？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const deviceNo = currentDevice.value.deviceId + ''
    restartDevice(deviceNo)
  }).catch(() => {})
}

// 保存配置
const handleSaveConfig = () => {
  ElMessage.info('参数保存功能需要设备通信支持')
}

// 下发
const handleIssued = async (row) => {
  try {
    const res = await issuedDevice(row.devicelistId || row.id)
    if (res.code === 200) {
      ElMessage.success('下发成功')
    }
  } catch (error) {
    ElMessage.error('下发失败')
  }
}

const goToMeterTab = (row) => {
  emit('goToMeterTab', row.deviceId)
}

// 新增电能终端
const handleAdd = () => {
  isAdd.value = true
  editForm.value = {
    id: null,
    deviceId: '',
    devicePassword: '',
    deviceNum: ''
  }
  editDialogVisible.value = true
}

// 编辑电能终端
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
      deviceType: 2 // 电能终端类型
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
.power-terminal-table {
  .table-header {
    margin-bottom: 15px;
    display: flex;
    gap: 10px;
  }
}

.control-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;

  .config-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;

    .device-id {
      margin-left: 10px;
      font-weight: normal;
      color: #409eff;
    }
  }

  .restart-btn {
    background: #f56c6c;
    border-color: #f56c6c;

    &:hover {
      background: #f78989;
      border-color: #f78989;
    }
  }
}

.config-content {
  .config-row {
    display: flex;
    gap: 30px;
    margin-bottom: 20px;

    .config-item {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 10px;

      &.full-width {
        flex: none;
        width: 100%;
      }

      .config-label {
        flex-shrink: 0;
        min-width: 90px;
        text-align: right;
        color: #606266;
        font-size: 14px;
      }

      .config-input {
        width: 180px;
        flex-shrink: 0;

        &.firmware-input {
          width: 250px;
        }
      }

      .btn-group {
        display: flex;
        gap: 8px;
        flex-shrink: 0;

        .config-btn {
          min-width: 50px;
          height: 32px;
          padding: 0 12px;
          font-size: 13px;
          line-height: 32px;
          text-align: center;
          cursor: pointer;
          border-radius: 4px;
          transition: all 0.3s;

          &.read-btn {
            background: #113f6a;
            color: #fff;

            &:hover {
              background: #1a5a8a;
            }
          }

          &.update-btn {
            border: 1px solid #113f6a;
            color: #113f6a;
            background: transparent;

            &:hover {
              background: #113f6a;
              color: #fff;
            }
          }

          &.firmware-btn {
            background: #113f6a;
            color: #fff;
            min-width: 100px;

            &:hover {
              background: #1a5a8a;
            }
          }
        }
      }
    }
  }
}
</style>
