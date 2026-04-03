<template>
  <div class="terminal-management">
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
          <el-form-item label="终端名称">
            <el-input v-model="searchForm.keyword" placeholder="请输入终端名称/设备号" clearable />
          </el-form-item>
          <el-form-item label="设备类型">
            <el-select v-model="searchForm.deviceTypeId" placeholder="请选择设备类型" clearable style="width: 180px;">
              <el-option label="边缘计算终端" :value="1" />
              <el-option label="电能表采集终端" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="在线状态">
            <el-select v-model="searchForm.deviceStatus" placeholder="请选择在线状态" clearable style="width: 180px;">
              <el-option label="在线" value="在线" />
              <el-option label="离线" value="离线" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="primary" @click="handleAdd" :disabled="!currentTunnelId">新增</el-button>
            <el-button type="success" @click="handleExport" :disabled="!currentTunnelId">导出</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="deviceId" label="设备号" width="120" />
          <el-table-column prop="nickName" label="终端名称" />
          <el-table-column label="设备类型">
            <template #default="scope">
              <el-tag :type="scope.row.deviceType === '边缘控制器' ? 'primary' : 'success'">
                {{ scope.row.deviceType === '边缘控制器' ? '边缘计算终端' : '电能表采集终端' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="zone" label="区段">
            <template #default="scope">
              {{ getZoneName(scope.row.zone) }}
            </template>
          </el-table-column>
          <el-table-column prop="loopNumber" label="回路编号" />
          <el-table-column prop="csq" label="信号强度">
            <template #default="scope">
              <el-progress
                :percentage="scope.row.csq || 0"
                :color="getSignalColor(scope.row.csq)"
                :show-text="false"
                style="width: 80px"
              />
              <span style="margin-left: 8px">{{ scope.row.csq || 0 }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="deviceStatus" label="在线状态">
            <template #default="scope">
              <el-tag :type="scope.row.deviceStatus === '在线' ? 'success' : 'danger'">
                {{ scope.row.deviceStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastUpdate" label="最后更新" width="170" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleDetail(scope.row)">
                详情
              </el-button>
              <el-button type="info" size="small" @click="handleConfig(scope.row)">
                配置
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="终端详情" width="600px" :close-on-click-modal="false">
      <div class="detail-content" v-if="currentDevice">
        <div class="detail-header">
          <div class="device-info">
            <h3>{{ currentDevice.nickName || currentDevice.deviceId }}</h3>
            <el-tag :type="currentDevice.deviceStatus === '在线' ? 'success' : 'danger'" size="large">
              {{ currentDevice.deviceStatus }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="detail-body">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">设备号</span>
                <span class="value">{{ currentDevice.deviceId }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">终端名称</span>
                <span class="value">{{ currentDevice.nickName || '-' }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">设备类型</span>
                <span class="value">{{ currentDevice.deviceTypeId === 1 ? '边缘计算终端' : '电能表采集终端' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">区段</span>
                <span class="value">{{ getZoneName(currentDevice.zone) }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">回路编号</span>
                <span class="value">{{ currentDevice.loopNumber || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">信号强度</span>
                <div class="signal-info">
                  <el-progress
                    :percentage="currentDevice.csq || 0"
                    :color="getSignalColor(currentDevice.csq)"
                    :show-text="false"
                    style="width: 100px"
                  />
                  <span style="margin-left: 8px">{{ currentDevice.csq || 0 }}%</span>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">固件版本</span>
                <span class="value">{{ currentDevice.softVer || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">网络信息</span>
                <span class="value">{{ currentDevice.networkInfo || '-' }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="24">
              <div class="info-item">
                <span class="label">最后更新时间</span>
                <span class="value">{{ currentDevice.lastUpdate || '-' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleConfigFromDetail">配置设备</el-button>
      </template>
    </el-dialog>

    <!-- 控制弹窗 - 参考隧道参数中的边缘控制器/电能终端控制页面 -->
    <el-dialog v-model="configDialogVisible" :title="currentDevice?.deviceType === '边缘控制器' ? '边缘控制器控制' : '电能终端控制'" width="1100px" :close-on-click-modal="false">
      <div class="control-tabs">
        <div class="tab-item" :class="{ active: activeConfigTab === 'params' }" @click="activeConfigTab = 'params'">参数</div>
        <div class="tab-item" :class="{ active: activeConfigTab === 'bluetooth' }" @click="activeConfigTab = 'bluetooth'">蓝牙节点</div>
      </div>

      <!-- 参数Tab -->
      <div v-if="activeConfigTab === 'params'" class="params-content">
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">ID：</span>
            <el-input v-model="configForm.deviceId" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('id')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('id')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">蓝牙ID：</span>
            <el-input v-model="configForm.bluetoothId" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('bluetoothId')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('bluetoothId')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">边缘状态上报间隔（s）：</span>
            <el-input v-model="configForm.edgeReportInterval" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('edgeReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('edgeReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具状态上报间隔（s）：</span>
            <el-input v-model="configForm.lampReportInterval" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampReport')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">灯具与边缘通信超时（s）：</span>
            <el-input v-model="configForm.commTimeout" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('commTimeout')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('commTimeout')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具终端同步周期（s）：</span>
            <el-input v-model="configForm.syncPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('syncPeriod')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('syncPeriod')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">设置灯具终端状态上报时间（s）：</span>
            <el-input v-model="configForm.lampStatusReportTime" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampStatusReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampStatusReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具超时周期：</span>
            <el-input v-model="configForm.lampTimeoutPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampTimeout')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampTimeout')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">设置工况汇报周期（s）：</span>
            <el-input v-model="configForm.workReportPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('workReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('workReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具终端限制设置默认周期：</span>
            <el-input v-model="configForm.lampLimitPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampLimit')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampLimit')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">灯具列表：</span>
            <el-input v-model="configForm.lampList" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('lampList')">下发</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具参数下发：</span>
            <el-input v-model="configForm.lampParams" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('lampParams')">下发</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampParams')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">蓝牙节点：</span>
            <el-input v-model="configForm.bluetoothNode" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('bluetoothNode')">下发</div>
              <div class="param-btn update-btn" @click="handleUpdate('bluetoothNode')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">亮度仪波特率：</span>
            <el-input v-model="configForm.brightnessBaudRate" class="param-input" />
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
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig" :loading="configSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog v-model="addDialogVisible" :title="addForm.deviceTypeId === 1 ? '新增边缘计算终端' : '新增电能表采集终端'" width="600px" :close-on-click-modal="false">
      <el-form :model="addForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备号" required>
              <el-input v-model="addForm.deviceId" placeholder="请输入设备号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备密码">
              <el-input v-model="addForm.devicePassword" placeholder="请输入设备密码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备类型">
              <el-select v-model="addForm.deviceTypeId" placeholder="请选择设备类型" style="width: 100%;">
                <el-option label="边缘计算终端" :value="1" />
                <el-option label="电能表采集终端" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="终端名称">
              <el-input v-model="addForm.nickName" placeholder="请输入终端名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备桩号">
              <el-input v-model="addForm.deviceNum" placeholder="请输入设备桩号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区段">
              <el-select v-model="addForm.zone" placeholder="请选择区段" style="width: 100%;">
                <el-option v-for="(name, value) in zoneMap" :key="value" :label="name" :value="Number(value)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="回路编号">
              <el-input v-model="addForm.loopNumber" placeholder="请输入回路编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="信号强度">
              <el-input-number v-model="addForm.csq" :min="0" :max="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAdd" :loading="addSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDeviceList, exportDevices, addDevicelist } from '@/api/device'
import { useTunnelStore } from '@/stores/tunnel'

const tunnelStore = useTunnelStore()
const loading = ref(false)
const tableData = ref([])
const detailDialogVisible = ref(false)
const configDialogVisible = ref(false)
const configSaving = ref(false)
const currentDevice = ref(null)
const activeConfigTab = ref('basic')
const addDialogVisible = ref(false)
const addSaving = ref(false)

const addForm = reactive({
  deviceId: '',
  devicePassword: '',
  deviceTypeId: 1,
  nickName: '',
  deviceNum: '',
  zone: null,
  loopNumber: '',
  csq: 0
})

const handleAdd = () => {
  Object.assign(addForm, {
    deviceId: '',
    devicePassword: '',
    deviceTypeId: searchForm.deviceTypeId || 1,
    nickName: '',
    deviceNum: '',
    zone: null,
    loopNumber: '',
    csq: 0
  })
  addDialogVisible.value = true
}

const handleSaveAdd = async () => {
  if (!addForm.deviceId) {
    ElMessage.warning('请输入设备号')
    return
  }
  addSaving.value = true
  try {
    const res = await addDevicelist({
      ...addForm,
      tunnelId: currentTunnelId.value
    })
    if (res.code === 200) {
      ElMessage.success('新增成功')
      addDialogVisible.value = false
      getTerminalList()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  } catch (error) {
    ElMessage.error('新增失败')
  } finally {
    addSaving.value = false
  }
}

// 当前隧道ID（从store获取）
const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const searchForm = reactive({
  keyword: '',
  deviceTypeId: 1,
  deviceStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const configForm = reactive({
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

const handleRead = (type) => {
  ElMessage.info(`读取${type}参数功能开发中`)
}

const handleUpdate = (type) => {
  ElMessage.info(`更新${type}参数功能开发中`)
}

const handleIssue = (type) => {
  ElMessage.info(`下发${type}功能开发中`)
}

const zoneMap = {
  1: '入口段',
  2: '过渡段1',
  3: '过渡段2',
  4: '中间段',
  5: '出口段'
}

const getZoneName = (zone) => {
  return zoneMap[zone] || zone || '-'
}

const getSignalColor = (csq) => {
  if (csq >= 80) return '#67c23a'
  if (csq >= 60) return '#e6a23c'
  if (csq >= 40) return '#f56c6c'
  return '#909399'
}

const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

// 监听store中隧道变化
watch(() => tunnelStore.currentTunnelId, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal) {
    pagination.page = 1
    getTerminalList()
  } else if (!newVal) {
    tableData.value = []
    pagination.total = 0
  }
})

const getTerminalList = async () => {
  if (!currentTunnelId.value) {
    tableData.value = []
    pagination.total = 0
    return
  }

  loading.value = true
  try {
    const deviceType = searchForm.deviceTypeId === 1 ? '边缘控制器' : '电能终端'
    const res = await getDeviceList({
      tunnelId: currentTunnelId.value,
      deviceType: deviceType,
      keyword: searchForm.keyword,
      deviceStatus: searchForm.deviceStatus,
      pageNum: pagination.page,
      pageSize: pagination.size
    })
    if (res.code === 200) {
      tableData.value = res.rows || []
      pagination.total = res.total || 0
    }
  } catch (error) {
    console.error('获取终端列表失败:', error)
    ElMessage.error('获取终端列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  getTerminalList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    deviceTypeId: 1,
    deviceStatus: ''
  })
  handleSearch()
}

const handleExport = async () => {
  if (!currentTunnelId.value) {
    ElMessage.warning('请先选择隧道')
    return
  }
  try {
    const deviceType = searchForm.deviceTypeId === 1 ? '边缘控制器' : '电能终端'
    const res = await exportDevices({ tunnelId: currentTunnelId.value, deviceType })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = deviceType + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const handleDetail = (row) => {
  currentDevice.value = row
  detailDialogVisible.value = true
}

const handleConfig = (row) => {
  currentDevice.value = row
  // 初始化配置表单
  Object.assign(configForm, {
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
  })
  bluetoothNodes.value = []
  activeConfigTab.value = 'params'
  configDialogVisible.value = true
}

const handleConfigFromDetail = () => {
  detailDialogVisible.value = false
  handleConfig(currentDevice.value)
}

const handleSaveConfig = async () => {
  configSaving.value = true
  try {
    // 这里调用保存配置的API
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟API调用
    ElMessage.success('配置保存成功')
    configDialogVisible.value = false
    getTerminalList() // 刷新列表
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    configSaving.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  getTerminalList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getTerminalList()
}

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  if (currentTunnelId.value) {
    getTerminalList()
  }
})
</script>

<style lang="scss" scoped>
.terminal-management {
  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .table-container {
    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

// 详情弹窗样式
.detail-content {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    .device-info {
      display: flex;
      align-items: center;
      gap: 16px;

      h3 {
        margin: 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
      }
    }
  }

  .detail-body {
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      min-height: 32px;

      .label {
        width: 120px;
        color: #606266;
        font-size: 14px;
        flex-shrink: 0;
      }

      .value {
        color: #303133;
        font-size: 14px;
        flex: 1;
      }

      .signal-info {
        display: flex;
        align-items: center;
      }
    }
  }
}

// 控制弹窗样式
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
