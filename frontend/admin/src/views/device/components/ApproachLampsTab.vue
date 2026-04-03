<template>
  <div class="approach-lamps-tab">
    <div class="list-toolbar">
      <el-button type="success" size="small" @click="loadData" v-permission="'device:list'">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport" v-permission="'device:export'">导出</el-button>
      <el-button v-if="!readOnly" type="primary" size="small" @click="handleAdd" v-permission="'device:add'">新增</el-button>
    </div>
    <DeviceTableColumnSplit
      v-model="visibleColumns"
      :options="columnOptions"
      storage-key="tunnel-ws-approach"
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
          <el-table-column v-if="visibleColumns.includes('devNo')" label="设备号" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.deviceNo ?? scope.row.deviceId ?? '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('mileage')" label="安装里程" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.installationMileage ?? scope.row.deviceNum ?? '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('zoneName')" prop="zoneName" label="区段" min-width="100" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.zoneName || getZoneName(scope.row.zone) }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('bluetoothStrength')" prop="bluetoothStrength" label="蓝牙强度" min-width="96" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.bluetoothStrength != null ? scope.row.bluetoothStrength : '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('version')" prop="version" label="版本号" min-width="88" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('status')" label="状态" min-width="96">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status ?? scope.row.online)">
                {{ getStatusText(scope.row.status ?? scope.row.online) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('lastUpdate')" label="最后数据" min-width="168" show-overflow-tooltip>
            <template #default="scope">{{ formatDisplayTime(scope.row.lastUpdate || scope.row.lastTime) }}</template>
          </el-table-column>
          <el-table-column v-if="!readOnly" label="操作" width="168" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </DeviceTableColumnSplit>

    <!-- 与 zt_tunnel_web table5 yindao 表单项一致：单列 + 实体字段 deviceNo / installationMileage / status -->
    <el-dialog v-model="editDialogVisible" :title="isAdd ? '添加引道灯控制器' : '编辑引道灯控制器'" width="520px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="设备号" prop="deviceNo">
          <el-input v-model="editForm.deviceNo" :disabled="!isAdd" />
        </el-form-item>
        <el-form-item label="安装里程" prop="installationMileage">
          <el-input v-model="editForm.installationMileage" />
        </el-form-item>
        <el-form-item label="区段" prop="zone">
          <el-select v-model="editForm.zone" placeholder="请选择区段" clearable style="width: 100%;">
            <el-option v-for="z in zoneOptions" :key="z.value" :label="z.label" :value="Number(z.value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in approachStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="蓝牙强度" prop="bluetoothStrength">
          <el-input v-model="editForm.bluetoothStrength" />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="editForm.version" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DeviceTableColumnSplit from '@/components/device/DeviceTableColumnSplit.vue'
import { getApproachLampsList, addApproachLamps, updateApproachLamps, deleteApproachLamps, getZoneList } from '@/api/tunnelParam'
import { getStatusType, getStatusText, formatDisplayTime } from '@/utils/deviceStatus'

/** 旧版 yindao：在线=1，离线=0（与实体 TunnelApproachLampsTerminal.status 一致） */
const approachStatusOptions = [
  { label: '在线', value: 1 },
  { label: '离线', value: 0 }
]

const props = defineProps({
  tunnelId: { type: [Number, String], required: true },
  readOnly: { type: Boolean, default: false },
  showColumnPanel: { type: Boolean, default: true }
})

const columnOptions = computed(() => [
  { key: 'devNo', label: '设备号' },
  { key: 'mileage', label: '安装里程' },
  { key: 'zoneName', label: '区段' },
  { key: 'bluetoothStrength', label: '蓝牙强度' },
  { key: 'version', label: '版本号' },
  { key: 'status', label: '状态' },
  { key: 'lastUpdate', label: '最后数据' }
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
const formRef = ref()
const editForm = ref({
  id: null,
  deviceNo: '',
  installationMileage: '',
  zone: null,
  status: 1,
  bluetoothStrength: '',
  version: ''
})

const rules = {
  deviceNo: [{ required: true, message: '请输入设备号', trigger: 'blur' }]
}

const zoneOptions = ref([])

const zoneMapFallback = {
  1: '入口段',
  2: '过渡段1',
  3: '过渡段2',
  4: '中间段',
  5: '出口段'
}

const getZoneName = (zone) => {
  if (zone == null || zone === '') return '—'
  const fromApi = zoneOptions.value.find((z) => Number(z.value) === Number(zone))
  if (fromApi) return fromApi.label
  return zoneMapFallback[zone] || zone
}

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

const parseDeviceNo = (raw) => {
  const s = String(raw ?? '').trim()
  if (!s) return NaN
  if (/^\d+$/.test(s)) return Number(s)
  const m = s.match(/\d+/)
  return m ? Number(m[0]) : NaN
}

const parseOptionalInt = (raw) => {
  if (raw === '' || raw == null) return null
  const n = parseInt(String(raw).trim(), 10)
  return Number.isNaN(n) ? null : n
}

const buildSubmitPayload = () => {
  const deviceNo = parseDeviceNo(editForm.value.deviceNo)
  if (Number.isNaN(deviceNo)) {
    ElMessage.warning('设备号必须为数字')
    return null
  }
  const payload = {
    tunnelId: Number(props.tunnelId),
    deviceNo,
    installationMileage:
      editForm.value.installationMileage != null ? String(editForm.value.installationMileage).trim() : '',
    zone: editForm.value.zone != null && editForm.value.zone !== '' ? Number(editForm.value.zone) : null,
    status: Number(editForm.value.status) === 0 ? 0 : 1,
    bluetoothStrength: parseOptionalInt(editForm.value.bluetoothStrength),
    version: parseOptionalInt(editForm.value.version)
  }
  if (!isAdd.value && editForm.value.id != null) {
    payload.id = editForm.value.id
  }
  return payload
}

const loadData = async () => {
  if (!props.tunnelId) return
  loading.value = true
  try {
    const res = await getApproachLampsList({ tunnelId: props.tunnelId })
    if (res.code === 200) tableData.value = res.data || []
  } catch (error) {
    console.error('获取引道灯控制器列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const headers = ['设备号', '安装里程', '区段', '蓝牙强度', '版本号', '状态', '最后数据']
  const data = tableData.value.map((row) => [
    row.deviceNo ?? row.deviceId ?? '',
    row.installationMileage ?? row.deviceNum ?? '',
    getZoneName(row.zone),
    row.bluetoothStrength != null ? row.bluetoothStrength : '',
    row.version != null ? row.version : '',
    getStatusText(row.status ?? row.online),
    row.lastUpdate || row.lastTime || ''
  ])
  const csvContent = [headers.join(','), ...data.map((row) => row.join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `引道灯控制器_${new Date().toLocaleDateString()}.csv`
  link.click()
  ElMessage.success('导出成功')
}

const handleAdd = () => {
  isAdd.value = true
  editForm.value = {
    id: null,
    deviceNo: '',
    installationMileage: '',
    zone: null,
    status: 1,
    bluetoothStrength: '',
    version: ''
  }
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  isAdd.value = false
  const st = row.status ?? row.online
  editForm.value = {
    id: row.id,
    deviceNo: row.deviceNo != null && row.deviceNo !== '' ? String(row.deviceNo) : row.deviceId != null ? String(row.deviceId) : '',
    installationMileage: row.installationMileage != null ? String(row.installationMileage) : row.deviceNum != null ? String(row.deviceNum) : '',
    zone: row.zone,
    status: st === 0 || st === '0' ? 0 : 1,
    bluetoothStrength: row.bluetoothStrength != null && row.bluetoothStrength !== '' ? String(row.bluetoothStrength) : '',
    version: row.version != null && row.version !== '' ? String(row.version) : ''
  }
  editDialogVisible.value = true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const payload = buildSubmitPayload()
  if (!payload) return
  saving.value = true
  try {
    const api = isAdd.value ? addApproachLamps : updateApproachLamps
    const res = await api(payload)
    if (res.code === 200) {
      ElMessage.success(isAdd.value ? '添加成功' : '保存成功')
      editDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error(isAdd.value ? '添加失败' : '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该引道灯控制器？', '警告', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteApproachLamps(row.id)
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

watch(() => props.tunnelId, (newVal) => {
  if (newVal) loadData()
}, { immediate: true })

onMounted(() => {
  loadZoneOptions()
})

defineExpose({ loadData, handleExport, handleAdd })
</script>

<style lang="scss" scoped>
.approach-lamps-tab {
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
}
</style>
