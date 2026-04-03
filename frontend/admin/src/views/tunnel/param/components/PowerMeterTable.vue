<template>
  <div class="power-meter-table">
    <div v-if="!readOnly" class="table-header">
      <el-button type="success" size="small" @click="handleRefresh">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport">导出</el-button>
      <el-button type="primary" size="small" @click="handleAdd" v-permission="'device:add'">新增</el-button>
    </div>
    <div v-else class="table-header table-header-readonly">
      <el-button type="success" size="small" @click="handleRefresh">刷新</el-button>
    </div>

    <DeviceTableColumnSplit
      v-model="visibleColumns"
      :options="columnOptions"
      storage-key="tunnel-ws-meter"
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
          <el-table-column v-if="visibleColumns.includes('address')" prop="address" label="地址号" min-width="88" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('devicelistId')" prop="devicelistId" label="电能终端设备号" min-width="140" show-overflow-tooltip />
          <!-- <el-table-column v-if="visibleColumns.includes('meterIndex')" prop="meterIndex" label="序号" min-width="80" show-overflow-tooltip /> -->
          <el-table-column v-if="visibleColumns.includes('loopName')" prop="loopName" label="电能终端名称" min-width="140" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('direction')" prop="direction" label="方向" min-width="88">
            <template #default="scope">
              {{ scope.row.direction === 1 ? '右线' : '左线' }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('vendorName')" prop="vendorName" label="电能表厂商" min-width="120" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.vendorName || getVendorName(scope.row.vendorId) }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('isEnabled')" prop="isEnabled" label="是否启用" min-width="96">
            <template #default="scope">
              <el-tag :type="powerRowEnabled(scope.row) ? 'success' : 'danger'">
                {{ powerRowEnabled(scope.row) ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('lastTime')" prop="lastTime" label="最后更新时间" min-width="160" show-overflow-tooltip />
          <el-table-column v-if="!readOnly" label="操作" width="176" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </DeviceTableColumnSplit>

    <!-- 与 zt_tunnel_web table5 setDianbiao1TableColumns 一致：单列、字段顺序与旧版相同 -->
    <el-dialog v-model="dialogVisible" :title="isEditMode ? '修改' : '新增'" width="520px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="120px" class="power-meter-form">
        <el-form-item label="地址号">
          <el-input-number v-model="editForm.address" :min="0" :max="255" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="电能终端设备号">
          <el-select v-model="editForm.devicelistId" placeholder="请选择" style="width: 100%;">
            <el-option
              v-for="item in powerTerminalList"
              :key="item.deviceId"
              :label="String(item.deviceId)"
              :value="Number(item.deviceId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="电能终端名称">
          <el-input v-model="editForm.loopName" />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="editForm.direction" placeholder="请选择" style="width: 100%;">
            <el-option label="左线" :value="2" />
            <el-option label="右线" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="电能表厂商">
          <el-select v-model="editForm.vendorId" placeholder="请选择厂商" clearable style="width: 100%;">
            <el-option v-for="vendor in vendorList" :key="vendor.vendorid" :label="vendor.vendorname" :value="vendor.vendorid" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-select v-model="editForm.is_enabled" style="width: 100%;">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="最后更新时间">
          <el-input v-model="editForm.lastTime" disabled />
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
import { ref, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DeviceTableColumnSplit from '@/components/device/DeviceTableColumnSplit.vue'
import { getPowerEdgeComputingList, saveOrUpdatePower, deletePowerEdgeComputing, getPowerTerminalList } from '@/api/tunnelParam'
import { getPowerVendorList } from '@/api/device'

const props = defineProps({
  tunnelId: {
    type: [Number, String],
    required: true
  },
  readOnly: { type: Boolean, default: false },
  showColumnPanel: { type: Boolean, default: true }
})

const columnOptions = computed(() => [
  { key: 'address', label: '地址号' },
  { key: 'devicelistId', label: '电能终端设备号' },
  // { key: 'meterIndex', label: '序号' },
  { key: 'loopName', label: '电能终端名称' },
  { key: 'direction', label: '方向' },
  { key: 'vendorName', label: '电能表厂商' },
  { key: 'isEnabled', label: '是否启用' },
  { key: 'lastTime', label: '最后更新时间' }
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
const dialogVisible = ref(false)
const isEditMode = ref(false)
const editForm = ref({})
const powerTerminalList = ref([])
const vendorList = ref([])

const getVendorName = (vendorId) => {
  const vendor = vendorList.value.find(v => v.vendorid === vendorId)
  return vendor ? vendor.vendorname : ''
}

/** 列表 is_enabled / isEnabled 兼容（旧版表单字段为 is_enabled） */
const powerRowEnabled = (row) => {
  const v = row.is_enabled !== undefined && row.is_enabled !== null ? row.is_enabled : row.isEnabled
  return Number(v) === 1
}

const loadData = async () => {
  if (!props.tunnelId) return

  loading.value = true
  try {
    const res = await getPowerEdgeComputingList({ tunnelId: props.tunnelId })
    if (res.code === 200) {
      tableData.value = res.data || res.rows || []
    }
  } catch (error) {
    console.error('获取电表列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadPowerTerminalList = async () => {
  if (!props.tunnelId) return
  try {
    const res = await getPowerTerminalList({ tunnelId: props.tunnelId })
    if (res.code === 200) {
      powerTerminalList.value = res.data || []
    }
  } catch (error) {
    console.error('获取电能终端列表失败:', error)
  }
}

const loadVendorList = async () => {
  try {
    const res = await getPowerVendorList()
    if (res.code === 200) {
      vendorList.value = res.data || []
    }
  } catch (error) {
    console.error('获取厂商列表失败:', error)
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

  const headers = ['地址号', '电能终端设备号', '电能终端名称', '方向', '电能表厂商', '是否启用', '最后更新时间']
  const data = tableData.value.map(row => [
    row.address || '',
    row.devicelistId || '',
    row.loopName || '',
    row.direction === 1 ? '右线' : '左线',
    row.vendorName || getVendorName(row.vendorId),
    powerRowEnabled(row) ? '启用' : '禁用',
    row.lastTime || ''
  ])

  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `电表配置_${new Date().toLocaleDateString()}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const formatNowForDisplay = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const handleAdd = () => {
  loadPowerTerminalList()
  isEditMode.value = false
  editForm.value = {
    uniqueId: null,
    tunnelId: props.tunnelId,
    address: 0,
    devicelistId: null,
    loopName: '',
    direction: 1,
    vendorId: null,
    is_enabled: 1,
    lastTime: formatNowForDisplay()
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  loadPowerTerminalList()
  isEditMode.value = true
  const rawEn = row.is_enabled !== undefined && row.is_enabled !== null ? row.is_enabled : row.isEnabled
  const isEn = rawEn !== undefined && rawEn !== null ? Number(rawEn) : 1
  editForm.value = {
    uniqueId: row.uniqueId,
    tunnelId: props.tunnelId,
    address: row.address != null ? Number(row.address) : 0,
    devicelistId: row.devicelistId != null ? Number(row.devicelistId) : null,
    loopName: row.loopName || '',
    direction: row.direction != null ? Number(row.direction) : 1,
    vendorId: row.vendorId != null && row.vendorId !== '' ? Number(row.vendorId) : null,
    is_enabled: isEn === 0 ? 0 : 1,
    lastTime: row.lastTime != null && String(row.lastTime) !== '' ? String(row.lastTime) : '—'
  }
  dialogVisible.value = true
}

/** 与 table5 saveOrUpdatePowerFunction 一致：is_enabled / isEnabled、vendorId；保存走 saveOrUpdatePower */
const buildSavePayload = () => {
  let vendorId = editForm.value.vendorId
  if (vendorId === '' || vendorId === undefined) vendorId = null
  else vendorId = Number(vendorId)
  const is_enabled = Number(editForm.value.is_enabled)
  const payload = {
    tunnelId: Number(props.tunnelId),
    address: Number(editForm.value.address),
    devicelistId: editForm.value.devicelistId != null ? Number(editForm.value.devicelistId) : null,
    loopName: editForm.value.loopName != null ? String(editForm.value.loopName) : '',
    direction: Number(editForm.value.direction) === 2 ? 2 : 1,
    vendorId,
    is_enabled,
    isEnabled: is_enabled
  }
  if (isEditMode.value && editForm.value.uniqueId != null) {
    payload.uniqueId = editForm.value.uniqueId
  }
  return payload
}

const handleSave = async () => {
  if (editForm.value.devicelistId == null || editForm.value.devicelistId === '') {
    ElMessage.warning('请选择电能终端设备号')
    return
  }
  saving.value = true
  try {
    const res = await saveOrUpdatePower(buildSavePayload())
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除地址号为"${row.address}"的电表吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePowerEdgeComputing(row.uniqueId)
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
  loadVendorList()
})

defineExpose({ loadData })
</script>

<style lang="scss" scoped>
.power-meter-table {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  .power-meter-form :deep(.el-input.is-disabled .el-input__inner) {
    color: var(--el-text-color-regular);
  }
  .table-header {
    flex-shrink: 0;
    margin-bottom: 15px;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
