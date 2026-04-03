<template>
  <div class="device-check">
    <div class="card">
      <!-- 顶部操作区域 -->
      <div class="header-section">
        <div class="action-buttons">
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
          <el-button type="info" @click="handleSelectAll">
            {{ isAllSelected ? '取消全选' : '全选' }}
          </el-button>
          <el-button type="success" @click="handleBatchAdd" :disabled="selectedDevices.length === 0">
            添加检测 ({{ selectedDevices.length }})
          </el-button>
          <el-button type="warning" @click="showCheckList">检测列表</el-button>
        </div>
      </div>

      <!-- Tab切换区域 -->
      <div class="tab-section">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 边缘控制器 -->
          <el-tab-pane label="边缘控制器" name="edgeController">
            <div class="table-container">
              <el-table 
                ref="edgeTableRef"
                :data="edgePageData" 
                style="width: 100%" 
                v-loading="edgeLoading"
                @selection-change="handleEdgeSelectionChange"
                :row-key="row => row.deviceId"
              >
                <el-table-column type="selection" width="50" :reserve-selection="true" />
                <el-table-column prop="deviceId" label="设备号" min-width="140" />
                <el-table-column prop="devicePassword" label="设备密码" min-width="100" />
                <el-table-column prop="deviceNum" label="设备桩号" min-width="100" />
                <el-table-column prop="csq" label="信号强度" min-width="80" />
                <el-table-column prop="nickName" label="终端名称" min-width="140" />
                <el-table-column label="设备状态" min-width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.online === 0 ? 'success' : 'danger'" size="small">
                      {{ scope.row.online === 0 ? '在线' : '离线' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="更新时间" min-width="160">
                  <template #default="scope">
                    {{ formatTime(scope.row.lastUpdate) }}
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="edgePagination.page"
                v-model:page-size="edgePagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="edgePagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleEdgeSizeChange"
                @current-change="handleEdgePageChange"
              />
            </div>
          </el-tab-pane>

          <!-- 灯具终端 -->
          <el-tab-pane label="灯具终端" name="lampsTerminal">
            <div class="table-container">
              <el-table 
                ref="lampsTableRef"
                :data="lampsPageData" 
                style="width: 100%" 
                v-loading="lampsLoading"
                @selection-change="handleLampsSelectionChange"
                :row-key="row => row.uniqueId"
              >
                <el-table-column type="selection" width="50" :reserve-selection="true" />
                <el-table-column prop="deviceId" label="设备号" min-width="100" />
                <el-table-column prop="deviceName" label="设备名称" min-width="120" />
                <el-table-column prop="deviceNum" label="里程桩号" min-width="90" />
                <el-table-column prop="position" label="灯具序号" min-width="80" />
                <el-table-column prop="loopNumber" label="回路编号" min-width="90" />
                <el-table-column prop="zone" label="区段" min-width="60" />
                <el-table-column prop="csq" label="信号强度" min-width="80" />
                <el-table-column label="灯具状态" min-width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.lampsStatus === 0 ? 'success' : 'danger'" size="small">
                      {{ scope.row.lampsStatus === 0 ? '正常' : '异常' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="雷达" min-width="70">
                  <template #default="scope">
                    {{ scope.row.ldWhetherInstall === 1 ? '已装' : '无' }}
                  </template>
                </el-table-column>
                <el-table-column label="更新时间" min-width="150">
                  <template #default="scope">
                    {{ formatTime(scope.row.updateTime) }}
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                v-model:current-page="lampsPagination.page"
                v-model:page-size="lampsPagination.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="lampsPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleLampsSizeChange"
                @current-change="handleLampsPageChange"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 检测列表弹窗 -->
    <el-dialog v-model="checkListVisible" title="检测列表" width="900px" destroy-on-close>
      <el-table :data="checkListPageData" style="width: 100%" v-loading="checkListLoading">
        <el-table-column prop="deviceId" label="设备标识" min-width="130" />
        <el-table-column prop="deviceName" label="设备名称" min-width="140" />
        <el-table-column prop="deviceNum" label="设备桩号" min-width="100" />
        <el-table-column label="设备类型" min-width="100">
          <template #default="scope">
            {{ scope.row.deviceType === 0 ? '边缘控制器' : '灯具终端' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column label="添加时间" min-width="150">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleRemoveCheck(scope.row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="checkListPagination.page"
        v-model:page-size="checkListPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="checkListPagination.total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 15px; justify-content: center;"
        @size-change="handleCheckListSizeChange"
        @current-change="handleCheckListPageChange"
      />
    </el-dialog>

    <!-- 添加检测备注弹窗 -->
    <el-dialog v-model="addDialogVisible" title="添加检测" width="450px" destroy-on-close>
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="已选设备">
          <span>{{ selectedDevices.length }} 个设备</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.remark" type="textarea" :rows="3" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchAdd" :loading="addLoading">确认添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCheckDeviceList,
  batchAddCheckDevice,
  removeCheckDevice,
  getAllEdgeControllers,
  getAllLampsTerminal
} from '@/api/deviceCheck'

// Tab状态
const activeTab = ref('edgeController')

// 表格引用
const edgeTableRef = ref(null)
const lampsTableRef = ref(null)

// 原始数据列表
const edgeControllerData = ref([])
const lampsTerminalData = ref([])
const checkListData = ref([])

// 分页配置
const edgePagination = reactive({ page: 1, size: 10, total: 0 })
const lampsPagination = reactive({ page: 1, size: 10, total: 0 })
const checkListPagination = reactive({ page: 1, size: 10, total: 0 })

// 分页后的数据
const edgePageData = computed(() => {
  const start = (edgePagination.page - 1) * edgePagination.size
  return edgeControllerData.value.slice(start, start + edgePagination.size)
})

const lampsPageData = computed(() => {
  const start = (lampsPagination.page - 1) * lampsPagination.size
  return lampsTerminalData.value.slice(start, start + lampsPagination.size)
})

const checkListPageData = computed(() => {
  const start = (checkListPagination.page - 1) * checkListPagination.size
  return checkListData.value.slice(start, start + checkListPagination.size)
})

// 加载状态
const edgeLoading = ref(false)
const lampsLoading = ref(false)
const checkListLoading = ref(false)
const addLoading = ref(false)

// 选中的设备
const edgeSelected = ref([])
const lampsSelected = ref([])

// 弹窗状态
const checkListVisible = ref(false)
const addDialogVisible = ref(false)

// 添加表单
const addForm = ref({ remark: '' })

// 判断当前Tab是否全选
const isAllSelected = computed(() => {
  if (activeTab.value === 'edgeController') {
    return edgeControllerData.value.length > 0 && edgeSelected.value.length === edgeControllerData.value.length
  } else {
    return lampsTerminalData.value.length > 0 && lampsSelected.value.length === lampsTerminalData.value.length
  }
})

// 计算所有选中的设备
const selectedDevices = computed(() => {
  const devices = []
  edgeSelected.value.forEach(item => {
    devices.push({
      deviceId: String(item.deviceId),
      deviceNum: item.deviceNum ? String(item.deviceNum) : null,
      deviceType: 0,
      deviceName: item.nickName
    })
  })
  lampsSelected.value.forEach(item => {
    devices.push({
      // 灯具终端使用uniqueId作为唯一标识，因为device_id可能重复
      deviceId: String(item.uniqueId),
      deviceNum: item.deviceNum ? String(item.deviceNum) : null,
      deviceType: 1,
      deviceName: item.deviceName || `灯具${item.deviceId}-${item.position}`
    })
  })
  return devices
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

// 加载边缘控制器数据
const loadEdgeControllers = async () => {
  edgeLoading.value = true
  try {
    const res = await getAllEdgeControllers()
    if (res.code === 200) {
      edgeControllerData.value = res.data || []
      edgePagination.total = edgeControllerData.value.length
      edgePagination.page = 1
    }
  } catch (error) {
    console.error('加载边缘控制器失败:', error)
  } finally {
    edgeLoading.value = false
  }
}

// 加载灯具终端数据
const loadLampsTerminals = async () => {
  lampsLoading.value = true
  try {
    const res = await getAllLampsTerminal()
    if (res.code === 200) {
      lampsTerminalData.value = res.data || []
      lampsPagination.total = lampsTerminalData.value.length
      lampsPagination.page = 1
    }
  } catch (error) {
    console.error('加载灯具终端失败:', error)
  } finally {
    lampsLoading.value = false
  }
}

// 加载检测列表
const loadCheckList = async () => {
  checkListLoading.value = true
  try {
    const res = await getCheckDeviceList()
    if (res.code === 200) {
      checkListData.value = res.data || []
      checkListPagination.total = checkListData.value.length
      checkListPagination.page = 1
    }
  } catch (error) {
    console.error('加载检测列表失败:', error)
  } finally {
    checkListLoading.value = false
  }
}

// Tab切换处理
const handleTabChange = () => {}

// 选择变化处理
const handleEdgeSelectionChange = (selection) => {
  edgeSelected.value = selection
}

const handleLampsSelectionChange = (selection) => {
  lampsSelected.value = selection
}

// 全选/取消全选
const handleSelectAll = () => {
  if (activeTab.value === 'edgeController') {
    if (isAllSelected.value) {
      edgeTableRef.value?.clearSelection()
      edgeSelected.value = []
    } else {
      edgeControllerData.value.forEach(row => {
        edgeTableRef.value?.toggleRowSelection(row, true)
      })
      edgeSelected.value = [...edgeControllerData.value]
    }
  } else {
    if (isAllSelected.value) {
      lampsTableRef.value?.clearSelection()
      lampsSelected.value = []
    } else {
      lampsTerminalData.value.forEach(row => {
        lampsTableRef.value?.toggleRowSelection(row, true)
      })
      lampsSelected.value = [...lampsTerminalData.value]
    }
  }
}

// 分页处理
const handleEdgeSizeChange = (size) => { edgePagination.size = size; edgePagination.page = 1 }
const handleEdgePageChange = (page) => { edgePagination.page = page }
const handleLampsSizeChange = (size) => { lampsPagination.size = size; lampsPagination.page = 1 }
const handleLampsPageChange = (page) => { lampsPagination.page = page }
const handleCheckListSizeChange = (size) => { checkListPagination.size = size; checkListPagination.page = 1 }
const handleCheckListPageChange = (page) => { checkListPagination.page = page }

// 刷新数据
const handleRefresh = () => {
  loadEdgeControllers()
  loadLampsTerminals()
}

// 批量添加检测
const handleBatchAdd = () => {
  if (selectedDevices.value.length === 0) {
    ElMessage.warning('请先选择要添加的设备')
    return
  }
  addForm.value.remark = ''
  addDialogVisible.value = true
}

// 确认批量添加
const confirmBatchAdd = async () => {
  addLoading.value = true
  try {
    const devices = selectedDevices.value.map(item => ({ ...item, remark: addForm.value.remark }))
    const res = await batchAddCheckDevice(devices)
    if (res.code === 200) {
      ElMessage.success(`成功添加 ${res.data} 个设备到检测列表`)
      addDialogVisible.value = false
      edgeTableRef.value?.clearSelection()
      lampsTableRef.value?.clearSelection()
      edgeSelected.value = []
      lampsSelected.value = []
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error('批量添加失败:', error)
    ElMessage.error('添加失败')
  } finally {
    addLoading.value = false
  }
}

// 显示检测列表
const showCheckList = () => {
  checkListVisible.value = true
  loadCheckList()
}

// 移除检测设备
const handleRemoveCheck = async (row) => {
  try {
    await ElMessageBox.confirm('确定要从检测列表中移除该设备吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const res = await removeCheckDevice(row.id)
    if (res.code === 200) {
      ElMessage.success('移除成功')
      loadCheckList()
    } else {
      ElMessage.error(res.msg || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') console.error('移除失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadEdgeControllers()
  loadLampsTerminals()
})
</script>

<style scoped>
.device-check {
  padding: 15px;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-section {
  margin-bottom: 15px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.tab-section {
  margin-top: 5px;
}

.table-container {
  margin-top: 10px;
}

.table-container .el-pagination {
  margin-top: 15px;
  justify-content: center;
}

:deep(.el-tabs__item) {
  font-size: 14px;
}

:deep(.el-table) {
  font-size: 13px;
}

:deep(.el-table th) {
  padding: 8px 0;
}

:deep(.el-table td) {
  padding: 6px 0;
}

:deep(.el-table .cell) {
  padding: 0 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
