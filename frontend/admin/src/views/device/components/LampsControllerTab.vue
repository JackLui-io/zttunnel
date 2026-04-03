<template>
  <div class="lamps-controller-tab">
    <div class="list-toolbar">
      <el-button type="success" size="small" @click="loadData" v-permission="'device:list'">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport" v-permission="'device:export'">导出</el-button>
      <el-button v-if="!readOnly" type="primary" size="small" @click="handleAdd" v-permission="'device:add'">新增</el-button>
    </div>
    <DeviceTableColumnSplit
      v-model="visibleColumns"
      :options="columnOptions"
      storage-key="tunnel-ws-lamps"
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
          <el-table-column v-if="visibleColumns.includes('deviceId')" prop="deviceId" label="设备号" min-width="112" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('deviceName')" prop="deviceName" label="终端名称" min-width="120" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('deviceNum')" prop="deviceNum" label="安装里程" min-width="104" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.deviceNumStr || scope.row.deviceNum || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('position')" prop="position" label="灯具序号" min-width="92" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('loopNumber')" prop="loopNumber" label="回路编号" min-width="96" />
          <el-table-column v-if="visibleColumns.includes('zone')" prop="zone" label="区段" min-width="88">
            <template #default="scope">
              {{ getZoneName(scope.row.zone) }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('zone2')" prop="zone2" label="区段2" min-width="88">
            <template #default="scope">
              {{ getZoneName(scope.row.zone2) }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('deviceStatus')" label="设备状态" min-width="108">
            <template #default="scope">
              <template v-if="scope.row.communicationState == null || scope.row.communicationState === ''">—</template>
              <el-tag v-else :type="getLampCommunicationStateTagType(scope.row)">
                {{ getLampCommunicationStateText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('csq')" prop="csq" label="信号强度" min-width="90" />
          <el-table-column v-if="visibleColumns.includes('bluetoothNum')" prop="bluetoothNum" label="蓝牙编号" min-width="108" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('ldDeviceId')" prop="ldDeviceId" label="雷达设备号" min-width="116" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('workState')" label="工作状态" min-width="96">
            <template #default="scope">
              <span v-if="scope.row.workState === 2">/</span>
              <el-tag v-else :type="scope.row.workState === 0 ? 'success' : 'danger'">
                {{ scope.row.workState === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('ldWhetherInstall')" label="是否安装雷达" min-width="120">
            <template #default="scope">
              <el-tag :type="scope.row.ldWhetherInstall === 1 ? 'primary' : 'info'">
                {{ scope.row.ldWhetherInstall === 1 ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('ldStatus')" label="雷达状态" min-width="96">
            <template #default="scope">
              <span v-if="scope.row.ldWhetherInstall !== 1">/</span>
              <el-tag v-else :type="scope.row.ldStatus === 0 ? 'success' : 'danger'">
                {{ scope.row.ldStatus === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('deviceProperty')" prop="deviceProperty" label="调光类型" min-width="100" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('powerCarrier')" prop="powerCarrier" label="电力载波" min-width="100" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('updateTime')" prop="updateTime" label="更新时间" min-width="160" show-overflow-tooltip>
            <template #default="scope">{{ formatDisplayTime(scope.row.updateTime) }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('version')" prop="version" label="版本号" min-width="88" show-overflow-tooltip />
          <el-table-column v-if="!readOnly" label="操作" width="168" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </DeviceTableColumnSplit>

    <!-- 新增/编辑弹窗（字段顺序与 zt_tunnel_web table5 灯具控制器 tableItemTh 一致，单列） -->
    <el-dialog v-model="editDialogVisible" :title="isAdd ? '新增' : '修改'" width="520px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px" class="lamp-edit-form">
        <el-form-item label="设备号">
          <el-input v-model="editForm.deviceId" placeholder="请输入设备号" />
        </el-form-item>
        <el-form-item label="安装里程">
          <el-input v-model="editForm.deviceNumStr" />
        </el-form-item>
        <el-form-item label="所属回路">
          <el-input v-model="editForm.loopNumber" placeholder="请输入所属回路" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="editForm.communicationState" placeholder="请选择" style="width: 100%;">
            <el-option label="异常" :value="1" />
            <el-option label="正常" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作状态">
          <el-select v-model="editForm.workState" placeholder="请选择" style="width: 100%;">
            <el-option label="异常" :value="1" />
            <el-option label="正常" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="信号强度">
          <el-input v-model="editForm.csq" placeholder="请输入信号强度" />
        </el-form-item>
        <el-form-item label="区段">
          <el-select v-model="editForm.zone" placeholder="请选择区段" clearable style="width: 100%;">
            <el-option v-for="z in zoneOptions" :key="z.value" :label="z.label" :value="Number(z.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="区段2">
          <el-select v-model="editForm.zone2" placeholder="请选择区段2" clearable style="width: 100%;">
            <el-option v-for="z in zoneOptions" :key="`z2-${z.value}`" :label="z.label" :value="Number(z.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否安装雷达">
          <el-select v-model="editForm.ldWhetherInstall" placeholder="请选择" style="width: 100%;">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="调光类型">
          <el-select v-model="editForm.dimmingType" placeholder="请选择" style="width: 100%;">
            <el-option label="无级调光" :value="0" />
            <el-option label="随车调光" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-show="editForm.ldWhetherInstall === 1" label="雷达状态">
          <el-select v-model="editForm.ldStatus" placeholder="请选择" style="width: 100%;">
            <el-option label="异常" value="1" />
            <el-option label="正常" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="蓝牙编号">
          <el-input v-model="editForm.bluetoothNum" placeholder="格式：xxxx-x-x-x-x" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DeviceTableColumnSplit from '@/components/device/DeviceTableColumnSplit.vue'
import { getLampsTerminalList, getZoneList, updateLampsTerminal, deleteLampsTerminal } from '@/api/tunnelParam'
import { mileageStrToDeviceNum } from '@/utils/lampMileage'
import {
  formatDisplayTime,
  getLampCommunicationStateText,
  getLampCommunicationStateTagType
} from '@/utils/deviceStatus'

const props = defineProps({
  tunnelId: { type: [Number, String], required: true },
  readOnly: { type: Boolean, default: false },
  showColumnPanel: { type: Boolean, default: true }
})

const columnOptions = computed(() => [
  { key: 'deviceId', label: '设备号' },
  { key: 'deviceName', label: '终端名称' },
  { key: 'deviceNum', label: '安装里程' },
  { key: 'position', label: '灯具序号' },
  { key: 'loopNumber', label: '回路编号' },
  { key: 'zone', label: '区段' },
  { key: 'zone2', label: '区段2' },
  { key: 'deviceStatus', label: '设备状态' },
  { key: 'csq', label: '信号强度' },
  { key: 'bluetoothNum', label: '蓝牙编号' },
  { key: 'ldDeviceId', label: '雷达设备号' },
  { key: 'workState', label: '工作状态' },
  { key: 'ldWhetherInstall', label: '是否安装雷达' },
  { key: 'ldStatus', label: '雷达状态' },
  { key: 'deviceProperty', label: '调光类型' },
  { key: 'powerCarrier', label: '电力载波' },
  { key: 'updateTime', label: '更新时间' },
  { key: 'version', label: '版本号' }
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
const saving = ref(false)
const tableData = ref([])
const editDialogVisible = ref(false)
const isAdd = ref(false)
const currentDevice = ref({})
const editForm = ref({
  id: null,
  uniqueId: null,
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
  ldStatus: '0',
  bluetoothNum: ''
})

/** 与旧版 getZone 一致：nodeName / nodeCode */
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

const zoneMapFallback = {
  1: '入口段',
  2: '过渡段1',
  3: '过渡段2',
  4: '中间段',
  5: '出口段'
}

const getZoneName = (zone) => {
  if (zone == null || zone === '') return '-'
  const o = zoneOptions.value.find((z) => Number(z.value) === Number(zone))
  if (o) return o.label
  return zoneMapFallback[zone] ?? zone ?? '-'
}

const dimmingTypeFromRow = (row) => {
  if (row.dimmingType !== undefined && row.dimmingType !== null) return Number(row.dimmingType)
  if (row.deviceProperty === '随车调光') return 1
  const merged = row.ldWhetherInstall
  if (typeof merged === 'number' && (merged & 2) !== 0) return 1
  return 0
}

const ldStatusToForm = (row) => {
  if (row.ldWhetherInstall !== 1) return '0'
  const v = row.ldStatus
  if (v === 2 || v === '2') return '0'
  return v === '1' || v === 1 ? '1' : '0'
}

const loadData = async () => {
  if (!props.tunnelId) return
  loading.value = true
  try {
    const res = await getLampsTerminalList({ tunnelId: props.tunnelId })
    if (res.code === 200) tableData.value = res.data || []
  } catch (error) {
    console.error('获取灯具终端列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const headers = ['设备号', '安装里程/桩号', '回路编号', '区段', '信号强度', '设备状态', '工作状态', '是否安装雷达', '雷达状态', '调光类型']
  const data = tableData.value.map(row => [
    row.deviceId || '', row.deviceNumStr || row.deviceNum || '', row.loopNumber || '', getZoneName(row.zone),
    row.csq || '', getLampCommunicationStateText(row),
    row.workState === 2 ? '/' : (row.workState === 0 ? '正常' : '异常'),
    row.ldWhetherInstall === 1 ? '是' : '否',
    row.ldWhetherInstall !== 1 ? '/' : (row.ldStatus === 0 ? '正常' : '异常'),
    row.deviceProperty || ''
  ])
  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `灯具终端_${new Date().toLocaleDateString()}.csv`
  link.click()
  ElMessage.success('导出成功')
}

const handleAdd = () => {
  isAdd.value = true
  currentDevice.value = {}
  editForm.value = {
    id: null,
    uniqueId: null,
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
    ldStatus: '0',
    bluetoothNum: ''
  }
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  isAdd.value = false
  currentDevice.value = row
  const ldRadar =
    row.ldWhetherInstall !== undefined && row.ldWhetherInstall !== null
      ? Number(row.ldWhetherInstall) & 1
      : 0
  editForm.value = {
    id: row.id,
    uniqueId: row.uniqueId,
    deviceId: row.deviceId,
    deviceNumStr:
      row.deviceNumStr != null && row.deviceNumStr !== ''
        ? String(row.deviceNumStr)
        : row.deviceNum != null && row.deviceNum !== ''
          ? String(row.deviceNum)
          : '',
    loopNumber: row.loopNumber || '',
    communicationState: row.communicationState !== undefined ? row.communicationState : 0,
    workState: row.workState !== undefined ? row.workState : 0,
    csq: row.csq != null && row.csq !== '' ? String(row.csq) : '',
    zone: row.zone,
    zone2: row.zone2,
    ldWhetherInstall: ldRadar,
    dimmingType: dimmingTypeFromRow(row),
    ldStatus: ldStatusToForm({ ...row, ldWhetherInstall: ldRadar }),
    bluetoothNum: row.bluetoothNum || ''
  }
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  if (isAdd.value && !editForm.value.deviceId) {
    ElMessage.warning('请输入设备号')
    return
  }
  saving.value = true
  try {
    // 将通信状态和工作状态转换为lampsStatus
    // bit 0: 通信状态 (0=正常, 1=异常)
    // bit 2: 工作状态 (0=正常, 1=异常)
    let lampsStatus = 0
    if (editForm.value.communicationState === 1) {
      lampsStatus |= 0x01 // bit 0
    }
    if (editForm.value.workState === 1) {
      lampsStatus |= 0x04 // bit 2
    }

    const strTrim = editForm.value.deviceNumStr != null ? String(editForm.value.deviceNumStr).trim() : ''
    const deviceNum = mileageStrToDeviceNum(strTrim)
    const dimmingType = Number(editForm.value.dimmingType) === 1 ? 1 : 0
    const deviceProperty = dimmingType === 1 ? '随车调光' : '无极调光'
    let ldWhetherInstall = editForm.value.ldWhetherInstall !== undefined ? Number(editForm.value.ldWhetherInstall) : 0
    ldWhetherInstall = ldWhetherInstall ? 1 : 0
    const submitData = {
      ...(isAdd.value ? {} : { uniqueId: editForm.value.uniqueId ?? editForm.value.id }),
      tunnelId: Number(props.tunnelId),
      deviceId: Number(editForm.value.deviceId),
      deviceNum,
      ...(strTrim !== '' ? { deviceNumStr: strTrim } : {}),
      loopNumber: editForm.value.loopNumber,
      zone: editForm.value.zone,
      zone2: editForm.value.zone2,
      ldWhetherInstall,
      deviceProperty,
      dimmingType,
      bluetoothNum: editForm.value.bluetoothNum,
      communicationState: editForm.value.communicationState,
      workState: editForm.value.workState,
      csq: editForm.value.csq === '' || editForm.value.csq == null ? 0 : Number(editForm.value.csq) || 0,
      ldStatus: editForm.value.ldWhetherInstall === 1 ? String(editForm.value.ldStatus) : '0',
      lampsStatus
    }
    const res = await updateLampsTerminal(submitData)
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
    saving.value = false
  }
}

const handleDelete = (row) => {
  const deleteId = row.id || row.uniqueId
  if (!deleteId) {
    ElMessage.error('无法获取设备ID')
    return
  }
  ElMessageBox.confirm('确认删除该灯具终端？', '警告', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteLampsTerminal(deleteId)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

watch(() => props.tunnelId, (newVal) => { if (newVal) loadData() }, { immediate: true })

onMounted(() => {
  loadZoneOptions()
})

defineExpose({ loadData, handleExport, handleAdd })
</script>

<style lang="scss" scoped>
.lamps-controller-tab {
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;

  .list-toolbar {
    flex-shrink: 0;
    display: flex;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 12px;
  }

  :deep(.device-table-column-split) {
    flex: 1;
    min-height: 0;
    display: flex;
    min-width: 0;
  }

  .lamp-edit-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
