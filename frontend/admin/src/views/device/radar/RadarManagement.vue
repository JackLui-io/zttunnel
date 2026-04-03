<template>
  <div class="radar-management">
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
          <el-form-item label="设备名称">
            <el-input v-model="searchForm.keyword" placeholder="请输入设备名称/设备号" clearable />
          </el-form-item>
          <el-form-item label="设备类型">
            <el-select v-model="searchForm.type" placeholder="请选择设备类型" clearable style="width: 200px;">
              <el-option label="洞外雷达" :value="1" />
              <el-option label="洞外亮度传感器" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="设备状态">
            <el-select v-model="searchForm.deviceStatus" placeholder="请选择设备状态" clearable style="width: 180px;">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.label" />
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
          <el-table-column prop="deviceId" label="设备ID" width="120" />
          <el-table-column prop="deviceName" label="设备名称" />
          <el-table-column prop="deviceType" label="设备类型">
            <template #default="scope">
              <el-tag :type="scope.row.deviceType === '洞外雷达' ? 'primary' : 'success'">
                {{ scope.row.deviceType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviceNum" label="设备桩号" />
          <el-table-column prop="loopNumber" label="回路编号" />
          <el-table-column prop="deviceStatus" label="设备状态">
            <template #default="scope">
              <el-tag :type="getDeviceStatusType(scope.row.deviceStatus)">
                {{ scope.row.deviceStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑设备" width="600px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="120px" v-if="currentDevice">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备ID">
              <el-input v-model="editForm.deviceId" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称">
              <el-input v-model="editForm.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备类型">
              <el-select v-model="editForm.type" placeholder="请选择设备类型" style="width: 100%;">
                <el-option label="洞外雷达" :value="1" />
                <el-option label="洞外亮度传感器" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备桩号">
              <el-input v-model="editForm.deviceNum" placeholder="请输入设备桩号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="回路编号">
              <el-input v-model="editForm.loopNumber" placeholder="请输入回路编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备状态">
              <el-select v-model="editForm.deviceStatus" placeholder="请选择设备状态" style="width: 100%;">
                <el-option v-for="item in editStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="editSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog v-model="addDialogVisible" title="新增设备" width="600px" :close-on-click-modal="false">
      <el-form :model="addForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备ID" required>
              <el-input v-model="addForm.deviceId" placeholder="请输入设备ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称">
              <el-input v-model="addForm.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备类型">
              <el-select v-model="addForm.type" placeholder="请选择设备类型" style="width: 100%;">
                <el-option label="洞外雷达" :value="1" />
                <el-option label="洞外亮度传感器" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备桩号">
              <el-input v-model="addForm.deviceNum" placeholder="请输入设备桩号" />
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
            <el-form-item label="设备状态">
              <el-select v-model="addForm.deviceStatus" placeholder="请选择设备状态" style="width: 100%;">
                <el-option v-for="item in editStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceList, exportDevices, deleteDevice, updateDevice, addOutOfRadar } from '@/api/device'
import { useTunnelStore } from '@/stores/tunnel'
import { statusOptions } from '@/utils/deviceStatus'

const tunnelStore = useTunnelStore()

// 编辑弹窗中的状态选项（洞外雷达/亮度传感器只有在线和故障两种状态）
const editStatusOptions = [
  { label: '在线', value: '在线' },
  { label: '故障', value: '故障' }
]
const loading = ref(false)
const tableData = ref([])

// 当前隧道ID（从store获取）
const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const searchForm = reactive({
  keyword: '',
  type: 1,
  deviceStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 设备状态类型（雷达管理使用文本状态）
const getDeviceStatusType = (status) => {
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
    getRadarList()
  } else if (!newVal) {
    tableData.value = []
    pagination.total = 0
  }
})

// 监听设备类型变化，自动刷新列表
watch(() => searchForm.type, () => {
  if (currentTunnelId.value) {
    pagination.page = 1
    getRadarList()
  }
})

const getRadarList = async () => {
  if (!currentTunnelId.value) {
    tableData.value = []
    pagination.total = 0
    return
  }

  loading.value = true
  try {
    const deviceType = searchForm.type === 1 ? '洞外雷达' : '洞外亮度传感器'
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
    console.error('获取雷达列表失败:', error)
    ElMessage.error('获取雷达列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  getRadarList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    type: 1,
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
    const deviceType = searchForm.type === 1 ? '洞外雷达' : '洞外亮度传感器'
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

const editDialogVisible = ref(false)
const editSaving = ref(false)
const currentDevice = ref(null)
const addDialogVisible = ref(false)
const addSaving = ref(false)

const editForm = reactive({
  id: null,
  deviceId: '',
  deviceName: '',
  type: 1,
  deviceNum: '',
  loopNumber: '',
  deviceStatus: '在线'
})

const addForm = reactive({
  deviceId: '',
  deviceName: '',
  type: 1,
  deviceNum: '',
  loopNumber: '',
  deviceStatus: '在线'
})

const handleAdd = () => {
  Object.assign(addForm, {
    deviceId: '',
    deviceName: '',
    type: searchForm.type || 1,
    deviceNum: '',
    loopNumber: '',
    deviceStatus: '在线'
  })
  addDialogVisible.value = true
}

const handleSaveAdd = async () => {
  if (!addForm.deviceId) {
    ElMessage.warning('请输入设备ID')
    return
  }
  addSaving.value = true
  try {
    const res = await addOutOfRadar({
      ...addForm,
      tunnelId: currentTunnelId.value
    })
    if (res.code === 200) {
      ElMessage.success('新增成功')
      addDialogVisible.value = false
      getRadarList()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  } catch (error) {
    ElMessage.error('新增失败')
  } finally {
    addSaving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备 "${row.deviceName || row.deviceId}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deleteDevice(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      getRadarList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleEdit = (row) => {
  currentDevice.value = row
  Object.assign(editForm, {
    id: row.id,
    deviceId: row.deviceId,
    deviceName: row.deviceName || '',
    type: row.type || 1,
    deviceNum: row.deviceNum || '',
    loopNumber: row.loopNumber || '',
    deviceStatus: row.deviceStatus || '在线'
  })
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  editSaving.value = true
  try {
    const res = await updateDevice(editForm)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      getRadarList()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    editSaving.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  getRadarList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getRadarList()
}

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  if (currentTunnelId.value) {
    getRadarList()
  }
})
</script>

<style lang="scss" scoped>
.radar-management {
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




</style>
