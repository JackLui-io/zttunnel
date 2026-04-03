<template>
  <div class="lamps-management">
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
          <el-form-item label="灯具名称">
            <el-input v-model="searchForm.keyword" placeholder="请输入灯具名称/设备号" clearable />
          </el-form-item>
          <el-form-item label="设备状态">
            <el-select v-model="searchForm.deviceStatus" placeholder="请选择设备状态" clearable style="width: 180px;">
              <el-option label="在线" value="在线" />
              <el-option label="离线" value="离线" />
              <el-option label="故障" value="故障" />
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
          <el-table-column prop="deviceId" label="设备号" width="100" />
          <el-table-column prop="deviceNumStr" label="里程桩号" width="120" />
          <el-table-column prop="loopNumber" label="回路编号" width="100" />
          <el-table-column prop="zone" label="区段" width="100">
            <template #default="scope">
              {{ getZoneName(scope.row.zone) }}
            </template>
          </el-table-column>
          <el-table-column prop="csq" label="信号强度" width="150">
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
          <el-table-column label="通信状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.communicationState === 0 ? 'success' : 'danger'">
                {{ scope.row.communicationState === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="工作状态" width="100">
            <template #default="scope">
              <span v-if="scope.row.workState === 2">/</span>
              <el-tag v-else :type="scope.row.workState === 0 ? 'success' : 'danger'">
                {{ scope.row.workState === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="是否安装雷达" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.ldWhetherInstall === 1 ? 'primary' : 'info'">
                {{ scope.row.ldWhetherInstall === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="雷达状态" width="100">
            <template #default="scope">
              <span v-if="scope.row.ldStatus === 2 || scope.row.ldWhetherInstall !== 1">/</span>
              <el-tag v-else :type="scope.row.ldStatus === 0 ? 'success' : 'danger'">
                {{ scope.row.ldStatus === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviceStatus" label="设备状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.deviceStatus)">
                {{ scope.row.deviceStatus }}
              </el-tag>
            </template>
          </el-table-column>
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
    <el-dialog v-model="detailDialogVisible" title="灯具详情" width="600px" :close-on-click-modal="false">
      <div class="detail-content" v-if="currentDevice">
        <div class="detail-header">
          <div class="device-info">
            <h3>{{ currentDevice.deviceId }}</h3>
            <el-tag :type="getStatusType(currentDevice.deviceStatus)" size="large">
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
                <span class="label">里程桩号</span>
                <span class="value">{{ currentDevice.deviceNumStr || '-' }}</span>
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
                <span class="label">区段</span>
                <span class="value">{{ getZoneName(currentDevice.zone) }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
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
            <el-col :span="12">
              <div class="info-item">
                <span class="label">通信状态</span>
                <el-tag :type="currentDevice.communicationState === 0 ? 'success' : 'danger'" size="small">
                  {{ currentDevice.communicationState === 0 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">工作状态</span>
                <span v-if="currentDevice.workState === 2" class="value">/</span>
                <el-tag v-else :type="currentDevice.workState === 0 ? 'success' : 'danger'" size="small">
                  {{ currentDevice.workState === 0 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">是否安装雷达</span>
                <el-tag :type="currentDevice.ldWhetherInstall === 1 ? 'primary' : 'info'" size="small">
                  {{ currentDevice.ldWhetherInstall === 1 ? '是' : '否' }}
                </el-tag>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">雷达状态</span>
                <span v-if="currentDevice.ldStatus === 2 || currentDevice.ldWhetherInstall !== 1" class="value">/</span>
                <el-tag v-else :type="currentDevice.ldStatus === 0 ? 'success' : 'danger'" size="small">
                  {{ currentDevice.ldStatus === 0 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">蓝牙编号</span>
                <span class="value">{{ currentDevice.bluetoothNum || '-' }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="24">
              <div class="info-item">
                <span class="label">设备属性</span>
                <span class="value">{{ currentDevice.deviceProperty || '-' }}</span>
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

    <!-- 编辑弹窗 - 参考隧道参数中的灯具终端编辑页面 -->
    <el-dialog v-model="configDialogVisible" title="编辑灯具终端" width="600px" :close-on-click-modal="false">
      <el-form :model="configForm" label-width="120px" v-if="currentDevice">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备号">
              <el-input v-model="configForm.deviceId" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安装里程">
              <el-input v-model="configForm.deviceNumStr" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属回路">
              <el-input v-model="configForm.loopNumber" placeholder="请输入所属回路" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区段">
              <el-select v-model="configForm.zone" placeholder="请选择区段" style="width: 100%;">
                <el-option v-for="(name, value) in zoneMap" :key="value" :label="name" :value="Number(value)" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="区段2">
              <el-select v-model="configForm.zone2" placeholder="请选择区段2" style="width: 100%;">
                <el-option v-for="(name, value) in zoneMap" :key="value" :label="name" :value="Number(value)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否安装雷达">
              <el-select v-model="configForm.ldWhetherInstall" placeholder="请选择" style="width: 100%;">
                <el-option label="是" :value="1" />
                <el-option label="否" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调光类型">
              <el-select v-model="configForm.deviceProperty" placeholder="请选择调光类型" style="width: 100%;">
                <el-option label="无极调光" value="无极调光" />
                <el-option label="随车调光" value="随车调光" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="蓝牙编号">
              <el-input v-model="configForm.bluetoothNum" placeholder="请输入蓝牙编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="信号强度">
              <el-input-number v-model="configForm.csq" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="雷达状态">
              <el-select v-model="configForm.ldStatus" placeholder="请选择" style="width: 100%;" :disabled="configForm.ldWhetherInstall !== 1">
                <el-option label="正常" :value="0" />
                <el-option label="异常" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig" :loading="configSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增弹窗：与 zt_tunnel_web table5 灯具字段顺序一致（单列） -->
    <el-dialog v-model="addDialogVisible" title="新增" width="520px" :close-on-click-modal="false">
      <el-form :model="addForm" label-width="100px" class="lamp-add-form">
        <el-form-item label="设备号" required>
          <el-input v-model="addForm.deviceId" placeholder="请输入设备号" />
        </el-form-item>
        <el-form-item label="安装里程">
          <el-input v-model="addForm.deviceNumStr" />
        </el-form-item>
        <el-form-item label="所属回路">
          <el-input v-model="addForm.loopNumber" placeholder="请输入所属回路" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="addForm.communicationState" placeholder="请选择" style="width: 100%;">
            <el-option label="异常" :value="1" />
            <el-option label="正常" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作状态">
          <el-select v-model="addForm.workState" placeholder="请选择" style="width: 100%;">
            <el-option label="异常" :value="1" />
            <el-option label="正常" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="信号强度">
          <el-input v-model="addForm.csq" placeholder="请输入信号强度" />
        </el-form-item>
        <el-form-item label="区段">
          <el-select v-model="addForm.zone" placeholder="请选择区段" clearable style="width: 100%;">
            <el-option v-for="z in zoneOptions" :key="z.value" :label="z.label" :value="Number(z.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="区段2">
          <el-select v-model="addForm.zone2" placeholder="请选择区段2" clearable style="width: 100%;">
            <el-option v-for="z in zoneOptions" :key="`a2-${z.value}`" :label="z.label" :value="Number(z.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否安装雷达">
          <el-select v-model="addForm.ldWhetherInstall" placeholder="请选择" style="width: 100%;">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="调光类型">
          <el-select v-model="addForm.dimmingType" placeholder="请选择" style="width: 100%;">
            <el-option label="无级调光" :value="0" />
            <el-option label="随车调光" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="addForm.ldWhetherInstall === 1" label="雷达状态">
          <el-select v-model="addForm.ldStatus" placeholder="请选择" style="width: 100%;">
            <el-option label="异常" value="1" />
            <el-option label="正常" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="蓝牙编号">
          <el-input v-model="addForm.bluetoothNum" placeholder="格式：xxxx-x-x-x-x" />
        </el-form-item>
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
import { getDeviceList, exportDevices } from '@/api/device'
import { updateLampsTerminal, getZoneList } from '@/api/tunnelParam'
import { mileageStrToDeviceNum } from '@/utils/lampMileage'
import { useTunnelStore } from '@/stores/tunnel'

const tunnelStore = useTunnelStore()
const loading = ref(false)
const tableData = ref([])

// 当前隧道ID（从store获取）
const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const searchForm = reactive({
  keyword: '',
  deviceStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const zoneMap = {
  1: '入口段',
  2: '过渡段1',
  3: '过渡段2',
  4: '中间段',
  5: '出口段'
}

const zoneOptions = ref([])

const loadZoneOptions = async () => {
  try {
    const res = await getZoneList()
    if (res.code === 200) {
      zoneOptions.value = (res.data || []).map((item) => ({
        label: item.nodeName ?? item.label,
        value: item.nodeCode ?? item.value
      }))
    }
  } catch (e) {
    console.error('获取区段列表失败:', e)
  }
}

const getZoneName = (zone) => {
  if (zone == null || zone === '') return '-'
  const o = zoneOptions.value.find((z) => Number(z.value) === Number(zone))
  if (o) return o.label
  return zoneMap[zone] || zone || '-'
}

const getSignalColor = (csq) => {
  if (csq >= 80) return '#67c23a'
  if (csq >= 60) return '#e6a23c'
  if (csq >= 40) return '#f56c6c'
  return '#909399'
}

const getStatusType = (status) => {
  if (status === '在线') return 'success'
  if (status === '离线') return 'info'
  if (status === '故障') return 'danger'
  return 'info'
}

const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

// 监听store中隧道变化
watch(() => tunnelStore.currentTunnelId, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal) {
    pagination.page = 1
    getLampsList()
  } else if (!newVal) {
    tableData.value = []
    pagination.total = 0
  }
})

const getLampsList = async () => {
  if (!currentTunnelId.value) {
    tableData.value = []
    pagination.total = 0
    return
  }

  loading.value = true
  try {
    const res = await getDeviceList({
      tunnelId: currentTunnelId.value,
      deviceType: '灯具终端',
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
    console.error('获取灯具列表失败:', error)
    ElMessage.error('获取灯具列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  getLampsList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
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
    const res = await exportDevices({ tunnelId: currentTunnelId.value, deviceType: '灯具终端' })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '灯具终端.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const detailDialogVisible = ref(false)
const configDialogVisible = ref(false)
const configSaving = ref(false)
const currentDevice = ref(null)
const addDialogVisible = ref(false)
const addSaving = ref(false)

const configForm = reactive({
  deviceId: '',
  deviceNumStr: '',
  loopNumber: '',
  zone: null,
  zone2: null,
  ldWhetherInstall: 0,
  deviceProperty: '',
  bluetoothNum: '',
  csq: 0,
  ldStatus: 0
})

const addForm = reactive({
  deviceId: '',
  deviceNumStr: '',
  loopNumber: '',
  communicationState: 0,
  workState: 0,
  csq: '',
  zone: null,
  zone2: null,
  ldWhetherInstall: 0,
  dimmingType: 0,
  bluetoothNum: '',
  ldStatus: '0'
})

const handleAdd = () => {
  Object.assign(addForm, {
    deviceId: '',
    deviceNumStr: '',
    loopNumber: '',
    communicationState: 0,
    workState: 0,
    csq: '',
    zone: null,
    zone2: null,
    ldWhetherInstall: 0,
    dimmingType: 0,
    bluetoothNum: '',
    ldStatus: '0'
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
    let lampsStatus = 0
    if (addForm.communicationState === 1) lampsStatus |= 0x01
    if (addForm.workState === 1) lampsStatus |= 0x04
    const strTrim = addForm.deviceNumStr != null ? String(addForm.deviceNumStr).trim() : ''
    const deviceNum = mileageStrToDeviceNum(strTrim)
    const dimmingType = Number(addForm.dimmingType) === 1 ? 1 : 0
    const deviceProperty = dimmingType === 1 ? '随车调光' : '无极调光'
    let ldWhetherInstall = addForm.ldWhetherInstall ? 1 : 0
    const payload = {
      tunnelId: currentTunnelId.value,
      deviceId: Number(addForm.deviceId),
      deviceNum,
      ...(strTrim !== '' ? { deviceNumStr: strTrim } : {}),
      loopNumber: addForm.loopNumber,
      zone: addForm.zone,
      zone2: addForm.zone2,
      ldWhetherInstall,
      deviceProperty,
      dimmingType,
      bluetoothNum: addForm.bluetoothNum,
      communicationState: addForm.communicationState,
      workState: addForm.workState,
      csq: addForm.csq === '' || addForm.csq == null ? 0 : Number(addForm.csq) || 0,
      ldStatus: addForm.ldWhetherInstall === 1 ? String(addForm.ldStatus) : '0',
      lampsStatus
    }
    const res = await updateLampsTerminal(payload)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      addDialogVisible.value = false
      getLampsList()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  } catch (error) {
    ElMessage.error('新增失败')
  } finally {
    addSaving.value = false
  }
}

const handleDetail = (row) => {
  currentDevice.value = row
  detailDialogVisible.value = true
}

const handleConfig = (row) => {
  currentDevice.value = row
  // 初始化配置表单 - 参考隧道参数中的灯具终端编辑页面
  Object.assign(configForm, {
    deviceId: row.deviceId,
    deviceNumStr: row.deviceNumStr || '',
    loopNumber: row.loopNumber || '',
    zone: row.zone,
    zone2: row.zone2,
    ldWhetherInstall: row.ldWhetherInstall !== undefined ? row.ldWhetherInstall : 0,
    deviceProperty: row.deviceProperty || '',
    bluetoothNum: row.bluetoothNum || '',
    csq: row.csq || 0,
    ldStatus: row.ldStatus !== undefined && row.ldStatus !== null ? Number(row.ldStatus) : 0
  })
  configDialogVisible.value = true
}

const handleConfigFromDetail = () => {
  detailDialogVisible.value = false
  handleConfig(currentDevice.value)
}

const handleSaveConfig = async () => {
  configSaving.value = true
  try {
    // 构建更新数据
    const updateData = {
      uniqueId: currentDevice.value.id,
      deviceId: configForm.deviceId,
      deviceNum: configForm.deviceNumStr ? parseInt(configForm.deviceNumStr.replace('K', '')) : null,
      loopNumber: configForm.loopNumber,
      zone: configForm.zone,
      zone2: configForm.zone2,
      ldWhetherInstall: configForm.ldWhetherInstall,
      deviceProperty: configForm.deviceProperty,
      bluetoothNum: configForm.bluetoothNum,
      csq: configForm.csq,
      ldStatus: configForm.ldStatus
    }
    const res = await updateLampsTerminal(updateData)
    if (res.code === 200) {
      ElMessage.success('配置保存成功')
      configDialogVisible.value = false
      getLampsList() // 刷新列表
    } else {
      ElMessage.error(res.msg || '配置保存失败')
    }
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    configSaving.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  getLampsList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getLampsList()
}

onMounted(async () => {
  loadZoneOptions()
  await tunnelStore.loadTunnelData()
  if (currentTunnelId.value) {
    getLampsList()
  }
})
</script>

<style lang="scss" scoped>
.lamps-management {
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

  .lamp-add-form :deep(.el-form-item) {
    margin-bottom: 16px;
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

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>
