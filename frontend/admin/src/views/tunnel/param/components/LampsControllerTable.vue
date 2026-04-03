<template>
  <div class="lamps-controller-table">
    <div class="table-header">
      <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
      <el-button type="success" size="small" @click="handleRefresh">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport">导出</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="deviceId" label="设备号" width="100" />
      <el-table-column prop="deviceNum" label="安装里程" width="120" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.deviceNumStr || scope.row.deviceNum || '—' }}</template>
      </el-table-column>
      <el-table-column prop="loopNumber" label="所属回路" width="100" />
      <el-table-column prop="communicationState" label="设备状态" width="100">
        <template #default="scope">
          <template v-if="scope.row.communicationState == null || scope.row.communicationState === ''">—</template>
          <el-tag v-else :type="getLampCommunicationStateTagType(scope.row)">
            {{ getLampCommunicationStateText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workState" label="工作状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.workState === 0 ? 'success' : 'danger'">
            {{ scope.row.workState === 0 ? '正常' : '异常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="csq" label="信号强度" width="80" />
      <el-table-column prop="zone" label="区段" width="120">
        <template #default="scope">
          {{ getZoneName(scope.row.zone) }}
        </template>
      </el-table-column>
      <el-table-column prop="zone2" label="区段2" width="120">
        <template #default="scope">
          {{ getZoneName(scope.row.zone2) }}
        </template>
      </el-table-column>
      <el-table-column prop="ldWhetherInstall" label="是否安装雷达" width="100">
        <template #default="scope">
          {{ scope.row.ldWhetherInstall === 1 ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="ldStatus" label="雷达状态" width="80">
        <template #default="scope">
          <span v-if="scope.row.ldWhetherInstall !== 1">/</span>
          <el-tag v-else :type="scope.row.ldStatus === '0' ? 'success' : 'danger'">
            {{ scope.row.ldStatus === '0' ? '正常' : '异常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deviceProperty" label="调光类型" width="100">
        <template #default="scope">
          {{ getDimmingTypeName(scope.row.deviceProperty) }}
        </template>
      </el-table-column>
      <el-table-column prop="bluetoothNum" label="蓝牙编号" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 与 zt_tunnel_web table5 灯具弹窗字段顺序一致，单列 -->
    <el-dialog v-model="dialogVisible" :title="editForm.uniqueId || editForm.id ? '修改' : '新增'" width="520px">
      <el-form :model="editForm" label-width="100px" class="lamp-edit-form">
        <el-form-item label="设备号">
          <el-input v-model="editForm.deviceId" :disabled="!!(editForm.uniqueId || editForm.id)" placeholder="请输入设备号" />
        </el-form-item>
        <el-form-item label="安装里程">
          <el-input v-model="editForm.deviceNumStr" placeholder="" />
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
            <el-option v-for="item in zoneOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="区段2">
          <el-select v-model="editForm.zone2" placeholder="请选择区段2" clearable style="width: 100%;">
            <el-option v-for="item in zoneOptions" :key="`z2-${item.value}`" :label="item.label" :value="Number(item.value)" />
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLampsTerminalList, updateLampsTerminal, deleteLampsTerminal, getZoneList } from '@/api/tunnelParam'
import { getLampCommunicationStateText, getLampCommunicationStateTagType } from '@/utils/deviceStatus'
import { mileageStrToDeviceNum } from '@/utils/lampMileage'

const props = defineProps({
  tunnelId: {
    type: [Number, String],
    required: true
  }
})

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editForm = ref({})
const zoneOptions = ref([])

// 获取区段名称
const getZoneName = (zone) => {
  if (zone === null || zone === undefined) return ''
  const option = zoneOptions.value.find(item => Number(item.value) === Number(zone))
  return option ? option.label : zone
}

// 获取调光类型名称
const getDimmingTypeName = (type) => {
  if (!type) return ''
  return type
}

// 加载区段选项
const loadZoneOptions = async () => {
  try {
    const res = await getZoneList()
    if (res.code === 200) {
      zoneOptions.value = (res.data || []).map((item) => ({
        value: item.nodeCode ?? item.value,
        label: item.nodeName ?? item.label
      }))
    }
  } catch (error) {
    console.error('获取区段列表失败:', error)
  }
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
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('获取灯具终端列表失败:', error)
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

  const headers = ['设备号', '安装里程', '所属回路', '设备状态', '工作状态', '信号强度', '区段', '区段2', '是否安装雷达', '雷达状态', '调光类型', '蓝牙编号']
  const data = tableData.value.map(row => [
    row.deviceId || '',
    row.deviceNumStr || row.deviceNum || '',
    row.loopNumber || '',
    row.communicationState === 0 ? '正常' : '异常',
    row.workState === 0 ? '正常' : '异常',
    row.csq || '',
    getZoneName(row.zone),
    getZoneName(row.zone2),
    row.ldWhetherInstall === 1 ? '是' : '否',
    row.ldWhetherInstall !== 1 ? '/' : (row.ldStatus === '0' ? '正常' : '异常'),
    row.deviceProperty || '',
    row.bluetoothNum || ''
  ])

  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `灯具终端_${new Date().toLocaleDateString()}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 新增灯具终端
const handleAdd = () => {
  editForm.value = {
    tunnelId: props.tunnelId,
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
  dialogVisible.value = true
}

const handleEdit = (row) => {
  const ldRadar =
    row.ldWhetherInstall !== undefined && row.ldWhetherInstall !== null
      ? Number(row.ldWhetherInstall) & 1
      : 0
  editForm.value = {
    tunnelId: props.tunnelId,
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
  dialogVisible.value = true
}

const handleSave = async () => {
  // 验证必填字段
  if (!editForm.value.deviceId) {
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
    const isAdd = !editForm.value.uniqueId && !editForm.value.id
    const submitData = {
      ...(isAdd ? {} : { uniqueId: editForm.value.uniqueId ?? editForm.value.id }),
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
      ElMessage.success(isAdd ? '新增成功' : '保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || (isAdd ? '新增失败' : '保存失败'))
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除设备号为"${row.deviceId}"的灯具终端吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteLampsTerminal(row.uniqueId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

watch(() => props.tunnelId, (newVal) => {
  if (newVal) loadData()
}, { immediate: true })

onMounted(() => {
  loadZoneOptions()
})

defineExpose({ loadData, handleAdd })
</script>

<style lang="scss" scoped>
.lamps-controller-table {
  .table-header {
    margin-bottom: 15px;
    display: flex;
    gap: 10px;
  }

  .lamp-edit-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
