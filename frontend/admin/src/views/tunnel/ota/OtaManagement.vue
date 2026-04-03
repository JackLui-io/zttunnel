<template>
  <div class="ota-management">
    <div class="card">
      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="设备类型">
            <el-select v-model="searchForm.deviceType" placeholder="请选择设备类型" clearable style="width: 180px;">
              <el-option label="边缘控制器" value="边缘控制器" />
              <el-option label="电能终端" value="电能终端" />
              <el-option label="中继器" value="中继器" />
              <el-option label="灯具终端" value="灯具终端" />
              <el-option label="引道灯控制器" value="引道灯控制器" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :show-file-list="false"
          accept=".bin,.hex"
        >
          <el-button type="primary">上传OTA文件</el-button>
        </el-upload>
        <el-button type="success" @click="handleBatchUpgrade" :disabled="selectedRows.length === 0">批量升级</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileOldName" label="原文件名" />
        <el-table-column prop="fileNewName" label="新文件名" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="deviceType" label="设备类型" width="120" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="createBy" label="上传人" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleUpgrade(scope.row)">升级</el-button>
              <el-button type="info" size="small" @click="handleViewDevices(scope.row)">设备列表</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 设备列表对话框 -->
    <el-dialog v-model="deviceDialogVisible" title="设备列表" width="700px">
      <el-table :data="deviceList" v-loading="deviceLoading" style="width: 100%">
        <el-table-column prop="deviceId" label="设备号" width="140" />
        <el-table-column prop="nickName" label="设备名称" width="180" />
        <el-table-column prop="version" label="当前版本" width="180" />
        <el-table-column prop="online" label="在线状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.online === 1 ? 'success' : 'danger'">
              {{ scope.row.online === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 升级对话框 -->
    <el-dialog v-model="upgradeDialogVisible" title="OTA升级" width="500px">
      <el-form :model="upgradeForm" label-width="100px">
        <el-form-item label="目标设备">
          <el-select v-model="upgradeForm.deviceId" placeholder="请选择设备" style="width: 100%;">
            <el-option v-for="device in upgradeDeviceList" :key="device.deviceId" :label="`${device.deviceId} - ${device.nickName || ''}`" :value="device.deviceId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="upgradeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpgrade" :loading="upgrading">开始升级</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOtaFileList, deleteOtaFile, startOtaUpgrade, batchStartOtaUpgrade, getOtaDeviceList } from '@/api/tunnelParam'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const deviceDialogVisible = ref(false)
const deviceLoading = ref(false)
const deviceList = ref([])
const upgradeDeviceList = ref([]) // 升级弹窗专用设备列表
const upgradeDialogVisible = ref(false)
const upgrading = ref(false)
const currentOtaFile = ref(null)

const searchForm = reactive({
  deviceType: ''
})

const upgradeForm = reactive({
  deviceId: null
})

// 上传配置
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  return `${baseUrl}/ota/upload`
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const uploadData = computed(() => ({
  deviceType: searchForm.deviceType
}))

// 加载OTA文件列表
const loadOtaList = async () => {
  loading.value = true
  try {
    const res = await getOtaFileList({
      deviceType: searchForm.deviceType
    })
    if (res && res.code === 200) {
      tableData.value = res.data || []
    } else {
      tableData.value = []
    }
  } catch (error) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  loadOtaList()
}

// 重置
const handleReset = () => {
  searchForm.deviceType = ''
  loadOtaList()
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

// 上传前校验
const beforeUpload = (file) => {
  if (!searchForm.deviceType) {
    ElMessage.warning('请先选择设备类型')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    loadOtaList()
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 查看设备列表
const handleViewDevices = async (row) => {
  currentOtaFile.value = row
  deviceDialogVisible.value = true
  deviceLoading.value = true
  try {
    const res = await getOtaDeviceList(row.id)
    if (res.code === 200) {
      deviceList.value = res.data || []
    }
  } catch (error) {
    deviceList.value = []
  } finally {
    deviceLoading.value = false
  }
}

// 升级 - 单独加载设备列表，不显示设备列表弹窗
const handleUpgrade = async (row) => {
  currentOtaFile.value = row
  upgradeForm.deviceId = null
  upgradeDeviceList.value = []
  upgradeDialogVisible.value = true

  // 加载设备列表到升级弹窗
  try {
    const res = await getOtaDeviceList(row.id)
    if (res.code === 200) {
      upgradeDeviceList.value = res.data || []
    }
  } catch (error) {
    upgradeDeviceList.value = []
  }
}

// 确认升级
const confirmUpgrade = async () => {
  if (!upgradeForm.deviceId) {
    ElMessage.warning('请选择目标设备')
    return
  }

  upgrading.value = true
  try {
    const res = await startOtaUpgrade({
      id: currentOtaFile.value.id,
      deviceId: upgradeForm.deviceId
    })
    if (res.code === 200) {
      ElMessage.success('升级指令已发送')
      upgradeDialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error('升级失败')
  } finally {
    upgrading.value = false
  }
}

// 批量升级
const handleBatchUpgrade = () => {
  ElMessageBox.confirm(`确定要对选中的${selectedRows.value.length}个文件进行批量升级吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchStartOtaUpgrade({
        ids: selectedRows.value.map(row => row.id)
      })
      if (res.code === 200) {
        ElMessage.success('批量升级指令已发送')
      }
    } catch (error) {
      ElMessage.error('批量升级失败')
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除文件"${row.fileOldName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteOtaFile(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadOtaList()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadOtaList()
})
</script>

<style lang="scss" scoped>
.ota-management {
  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .action-bar {
    margin-bottom: 15px;
    display: flex;
    gap: 10px;
  }

  .action-buttons {
    display: flex;
    gap: 4px;
    flex-wrap: nowrap;
  }
}
</style>
