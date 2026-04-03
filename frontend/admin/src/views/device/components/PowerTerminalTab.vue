<template>
  <div class="power-terminal-tab">
    <DeviceTableColumnSplit
      v-model="visibleColumns"
      :options="columnOptions"
      storage-key="tunnel-ws-power"
      :show-column-panel="showColumnPanel"
    >
      <div class="table-wrapper table-responsive">
        <el-table
          :data="tableData"
          v-loading="loading"
          table-layout="fixed"
          style="width: 100%"
          height="100%"
          flexible
          scrollbar-always-on
        >
          <el-table-column v-if="visibleColumns.includes('deviceId')" prop="deviceId" label="设备号" min-width="120" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('devicePassword')" prop="devicePassword" label="安装里程" min-width="100" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('nickName')" prop="nickName" label="终端名称" min-width="120" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('deviceNum')" prop="deviceNum" label="设备桩号" min-width="100" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('zone')" prop="zone" label="区段" min-width="88" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('loopNumber')" prop="loopNumber" label="回路编号" min-width="96" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('csq')" prop="csq" label="信号强度" min-width="90" />
          <el-table-column v-if="visibleColumns.includes('online')" prop="online" label="在线状态" min-width="96">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.online)">
                {{ getStatusText(scope.row.online) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('deviceStatus')" prop="deviceStatus" label="设备状态" min-width="108" show-overflow-tooltip>
            <template #default="scope">{{ getDevicelistDeviceStatusText(scope.row) }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('lastUpdate')" prop="lastUpdate" label="最后数据" min-width="160" show-overflow-tooltip>
            <template #default="scope">{{ formatDisplayTime(scope.row.lastUpdate) }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('softVer')" prop="softVer" label="固件版本" min-width="108" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.softVer || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('networkInfo')" prop="networkInfo" label="网络信息" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.networkInfo || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('electricityMeterNum')" prop="electricityMeterNum" label="电表数量" min-width="96" />
          <el-table-column v-if="!readOnly" label="操作" width="268" fixed="right" align="center">
            <template #default="scope">
              <div class="action-buttons">
                <el-button type="primary" size="small" @click="handleControl(scope.row)">控制</el-button>
                <el-button type="warning" size="small" @click="handleIssued(scope.row)">下发</el-button>
                <el-button type="info" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </DeviceTableColumnSplit>

    <!-- 控制弹窗 - 基础配置 -->
    <el-dialog v-model="controlDialogVisible" title="基础配置" width="900px" :close-on-click-modal="false">
      <div class="config-header">
        <div class="config-title">基础配置 <span class="device-id">设备号:{{ currentDevice.deviceId }}</span></div>
        <el-button type="danger" @click="handleRestart">重启终端</el-button>
      </div>
      <div class="config-content">
        <div class="config-row">
          <div class="config-item full-width">
            <span class="config-label">服务器地址：</span>
            <el-input v-model="configForm.serverAddress" class="config-input" placeholder="请输入服务器地址" />
          </div>
        </div>
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
        <el-form-item label="安装里程">
          <el-input v-model="editForm.devicePassword" placeholder="请输入安装里程（存于 device_password）" />
        </el-form-item>
        <el-form-item label="设备桩号">
          <el-input v-model="editForm.deviceNum" placeholder="请输入设备桩号" />
        </el-form-item>
        <el-form-item label="信号强度">
          <el-input-number v-model="editForm.csq" :min="0" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="editForm.online" placeholder="请选择设备状态" style="width: 100%;">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DeviceTableColumnSplit from '@/components/device/DeviceTableColumnSplit.vue'
import { getPowerTerminalList, issuedDevice, updateDevicelist } from '@/api/tunnelParam'
import { addDevicelist } from '@/api/device'
import { getStatusType, getStatusText, statusOptions, formatDisplayTime, getDevicelistDeviceStatusText } from '@/utils/deviceStatus'

const props = defineProps({
  readOnly: { type: Boolean, default: false },
  tunnelId: { type: [Number, String], required: true },
  showColumnPanel: { type: Boolean, default: true }
})

const columnOptions = computed(() => [
  { key: 'deviceId', label: '设备号' },
  { key: 'devicePassword', label: '安装里程' },
  { key: 'nickName', label: '终端名称' },
  { key: 'deviceNum', label: '设备桩号' },
  { key: 'zone', label: '区段' },
  { key: 'loopNumber', label: '回路编号' },
  { key: 'csq', label: '信号强度' },
  { key: 'online', label: '在线状态' },
  { key: 'deviceStatus', label: '设备状态' },
  { key: 'lastUpdate', label: '最后数据' },
  { key: 'softVer', label: '固件版本' },
  { key: 'networkInfo', label: '网络信息' },
  { key: 'electricityMeterNum', label: '电表数量' }
])

const visibleColumns = ref([])

watch(
  columnOptions,
  (opts) => {
    const allow = new Set(opts.map((o) => o.key))
    let next = visibleColumns.value.filter((k) => allow.has(k))
    for (const o of opts) {
      if (!next.includes(o.key)) next.push(o.key)
    }
    if (next.length === 0 && opts[0]) next = [opts[0].key]
    visibleColumns.value = next
  },
  { immediate: true }
)

const loading = ref(false)
const tableData = ref([])
const controlDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editSaving = ref(false)
const isAdd = ref(false)
const currentDevice = ref({})
const editForm = ref({
  id: null,
  deviceId: '',
  devicePassword: '',
  deviceNum: '',
  csq: 0,
  online: 1
})
const configForm = ref({
  serverAddress: '',
  port: '',
  heartbeatCycle: '',
  readTimeout: '',
  writeTimeout: '',
  firmwareInfo: ''
})

const loadData = async () => {
  if (!props.tunnelId) return
  loading.value = true
  try {
    const res = await getPowerTerminalList({ tunnelId: props.tunnelId })
    if (res.code === 200) tableData.value = res.data || []
  } catch (error) {
    console.error('获取电能终端列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const headers = ['设备号', '设备密码', '设备桩号', '信号强度', '设备状态', '电表数量']
  const data = tableData.value.map(row => [
    row.deviceId || '', row.devicePassword || '', row.deviceNum || '', row.csq || '',
    getStatusText(row.online), row.electricityMeterNum || 0
  ])
  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `电能终端_${new Date().toLocaleDateString()}.csv`
  link.click()
  ElMessage.success('导出成功')
}

const handleControl = (row) => {
  currentDevice.value = row
  configForm.value = { serverAddress: '', port: '', heartbeatCycle: '', readTimeout: '', writeTimeout: '', firmwareInfo: '' }
  controlDialogVisible.value = true
}

const handleRead = (type) => ElMessage.info(`读取参数功能需要设备通信支持`)
const handleUpdate = (type) => ElMessage.info(`更新参数功能需要设备通信支持`)
const handleSaveConfig = () => ElMessage.info('参数保存功能需要设备通信支持')

const handleRestart = () => {
  ElMessageBox.confirm('确认向终端发送重启指令？', '警告', { type: 'warning' })
    .then(() => ElMessage.info('重启终端功能需要设备通信支持'))
    .catch(() => {})
}

const handleIssued = async (row) => {
  try {
    const res = await issuedDevice(row.devicelistId || row.id)
    if (res.code === 200) ElMessage.success('下发成功')
  } catch (error) {
    ElMessage.error('下发失败')
  }
}

const handleAdd = () => {
  isAdd.value = true
  currentDevice.value = {}
  editForm.value = {
    id: null,
    deviceId: '',
    devicePassword: '',
    deviceNum: '',
    csq: 0,
    online: 1
  }
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  isAdd.value = false
  currentDevice.value = row
  editForm.value = {
    id: row.id,
    deviceId: row.deviceId,
    devicePassword: row.devicePassword || '',
    deviceNum: row.deviceNum || '',
    csq: row.csq || 0,
    online: row.online !== undefined && row.online !== null ? row.online : 1
  }
  editDialogVisible.value = true
}

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
    const res = isAdd.value ? await addDevicelist(submitData) : await updateDevicelist(editForm.value)
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

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该电能终端？', '警告', { type: 'warning' })
    .then(async () => {
      ElMessage.info('删除功能开发中')
    })
    .catch(() => {})
}

watch(() => props.tunnelId, (newVal) => { if (newVal) loadData() }, { immediate: true })

defineExpose({ loadData, handleExport, handleAdd })
</script>

<style lang="scss" scoped>
.power-terminal-tab {
  width: 100%;
  min-width: 0;
}
.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
  .config-title { font-size: 16px; font-weight: bold; .device-id { margin-left: 10px; font-weight: normal; color: #409eff; } }
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
      &.full-width { flex: none; width: 100%; }
      .config-label { flex-shrink: 0; min-width: 90px; text-align: right; color: #606266; font-size: 14px; }
      .config-input { width: 180px; &.firmware-input { width: 250px; } }
      .btn-group {
        display: flex;
        gap: 8px;
        .config-btn {
          min-width: 50px;
          height: 32px;
          padding: 0 12px;
          font-size: 13px;
          line-height: 32px;
          text-align: center;
          cursor: pointer;
          border-radius: 4px;
          &.read-btn { background: #113f6a; color: #fff; }
          &.update-btn { border: 1px solid #113f6a; color: #113f6a; &:hover { background: #113f6a; color: #fff; } }
          &.firmware-btn { background: #113f6a; color: #fff; min-width: 100px; }
        }
      }
    }
  }
}
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  .el-button { margin: 0; }
}
</style>
