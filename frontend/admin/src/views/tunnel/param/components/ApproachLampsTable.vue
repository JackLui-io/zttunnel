<template>
  <div class="approach-lamps-table">
    <div class="table-header">
      <el-button type="primary" size="small" @click="handleAdd">新增</el-button>
      <el-button type="success" size="small" @click="handleRefresh">刷新</el-button>
      <el-button type="warning" size="small" @click="handleExport">导出</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="deviceNo" label="设备号" width="120" />
      <el-table-column prop="installationMileage" label="安装里程" width="120" />
      <el-table-column prop="zone" label="区段" width="120">
        <template #default="scope">
          {{ getZoneName(scope.row.zone) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastUpdate" label="最后数据" width="170" />
      <el-table-column prop="bluetoothStrength" label="蓝牙强度" width="80" />
      <el-table-column prop="version" label="版本号" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑引道灯控制器' : '新增引道灯控制器'" width="600px">
      <el-form :model="editForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备号">
              <el-input v-model="editForm.deviceNo" placeholder="请输入设备号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安装里程">
              <el-input v-model="editForm.installationMileage" placeholder="请输入安装里程" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="区段">
              <el-select v-model="editForm.zone" placeholder="请选择区段" style="width: 100%;">
                <el-option v-for="item in zoneOptions" :key="item.value" :label="item.label" :value="Number(item.value)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%;" disabled>
                <el-option label="在线" :value="1" />
                <el-option label="离线" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="蓝牙强度">
              <el-input-number v-model="editForm.bluetoothStrength" :min="0" style="width: 100%;" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="版本号">
              <el-input v-model="editForm.version" style="width: 100%;" disabled />
            </el-form-item>
          </el-col>
        </el-row>
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
import { getApproachLampsList, addApproachLamps, updateApproachLamps, deleteApproachLamps, getZoneList } from '@/api/tunnelParam'

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

// 加载区段选项
const loadZoneOptions = async () => {
  try {
    const res = await getZoneList()
    if (res.code === 200) {
      zoneOptions.value = (res.data || []).map(item => ({
        value: item.value,
        label: item.label
      }))
    }
  } catch (error) {
    console.error('获取区段列表失败:', error)
  }
}

const loadData = async () => {
  if (!props.tunnelId) return

  loading.value = true
  try {
    const res = await getApproachLampsList({ tunnelId: props.tunnelId })
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('获取引道灯控制器列表失败:', error)
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

  const headers = ['设备号', '安装里程', '区段', '状态', '最后数据', '蓝牙强度', '版本号']
  const data = tableData.value.map(row => [
    row.deviceNo || '',
    row.installationMileage || '',
    getZoneName(row.zone),
    row.status === 1 ? '在线' : '离线',
    row.lastUpdate || '',
    row.bluetoothStrength || '',
    row.version || ''
  ])

  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `引道灯控制器_${new Date().toLocaleDateString()}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const handleAdd = () => {
  editForm.value = {
    tunnelId: props.tunnelId,
    deviceNo: '',
    installationMileage: '',
    zone: null,
    status: 0,
    bluetoothStrength: 0,
    version: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    const api = editForm.value.id ? updateApproachLamps : addApproachLamps
    const res = await api(editForm.value)
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
  ElMessageBox.confirm(`确定要删除设备号为"${row.deviceNo}"的引道灯控制器吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteApproachLamps(row.id)
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
.approach-lamps-table {
  .table-header {
    margin-bottom: 15px;
    display: flex;
    gap: 10px;
  }
}
</style>
