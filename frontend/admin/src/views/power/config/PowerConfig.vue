<template>
  <div class="power-config">
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
              class="tunnel-cascader-wide"
              @change="handleTunnelChange"
            />
          </el-form-item>
          <el-form-item label="电能终端">
            <el-select
              v-model="searchForm.devicelistId"
              placeholder="请选择电能终端"
              style="width: 200px"
              filterable
              @change="handleDeviceChange"
            >
              <el-option label="全部" value="" />
              <el-option
                v-for="item in deviceOptions"
                :key="item.deviceId"
                :label="item.deviceId"
                :value="item.deviceId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="handleAdd" :disabled="!currentTunnelId">新增</el-button>
            <el-button type="info" @click="handleRefresh" :disabled="!currentTunnelId">刷新</el-button>
            <el-button type="warning" @click="handleExport" style="margin-right: 20px;">导出</el-button>
            <el-button type="primary" @click="showQueryDialog">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container table-responsive">
        <el-table
          :data="tableData"
          style="width: 100%"
          table-layout="fixed"
          v-loading="loading"
        >
          <el-table-column prop="address" label="地址号" min-width="72" />
          <el-table-column prop="devicelistId" label="电能终端设备号" min-width="120" show-overflow-tooltip />
          <el-table-column prop="loopName" label="电能终端名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="direction" label="方向" min-width="72">
            <template #default="scope">
              {{ scope.row.direction === 1 ? '右线' : '左线' }}
            </template>
          </el-table-column>
          <el-table-column prop="vendorName" label="电能表厂商" min-width="110" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.vendorName || getVendorName(scope.row.vendorId) }}
            </template>
          </el-table-column>
          <el-table-column prop="isEnabled" label="是否启用" min-width="88">
            <template #default="scope">
              <el-tag :type="scope.row.isEnabled === 1 ? 'success' : 'danger'">
                {{ scope.row.isEnabled === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastTime" label="最后更新时间" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" min-width="152" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 全隧道查询弹窗 - 参考Query_SQL项目样式 -->
    <el-dialog
      v-model="queryDialogVisible"
      title=""
      width="1000px"
      :close-on-click-modal="true"
      class="query-dialog"
      :show-close="true"
    >
      <div class="query-container">
        <!-- 标签页切换 -->
        <div class="tabs">
          <button
            class="tab-btn"
            :class="{ active: activeQueryTab === 'basic' }"
            @click="activeQueryTab = 'basic'"
          >基础搜索</button>
          <button
            class="tab-btn"
            :class="{ active: activeQueryTab === 'advanced' }"
            @click="activeQueryTab = 'advanced'"
          >高级搜索</button>
        </div>

        <!-- 基础搜索 -->
        <div v-show="activeQueryTab === 'basic'" class="tab-content">
          <h2 class="section-title">隧道电力数据查询</h2>
          <div class="search-section">
            <input
              type="text"
              v-model="basicQueryForm.devicelistId"
              placeholder="请输入电能终端ID (devicelist_id)"
              class="search-input"
            />
            <select v-model="basicQueryForm.queryLimit" class="search-select">
              <option value="100">100条</option>
              <option value="200">200条</option>
              <option value="300">300条</option>
              <option value="500">500条</option>
              <option value="1000">1000条</option>
              <option value="5000">5000条</option>
              <option value="10000">10000条</option>
              <option value="-1">不限</option>
            </select>
            <button class="query-btn" @click="handleBasicQuery" :disabled="queryLoading">
              {{ queryLoading ? '查询中...' : '查询' }}
            </button>
            <button class="export-btn" style="margin-left: 20px;" @click="exportQueryResultsToExcel" :disabled="queryResults.length === 0">
              导出为EXCEL
            </button>
          </div>

          <!-- 加载提示 -->
          <div v-if="queryLoading" class="loading-spinner">正在查询，请稍等...</div>

          <!-- 数据表格 -->
          <table class="data-table" v-if="!queryLoading">
            <thead>
              <tr>
                <th>名称</th>
                <th>值</th>
                <th>电能终端ID</th>
                <th>地址号</th>
                <th>上传时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="paginatedQueryResults.length === 0">
                <td colspan="5" style="text-align: center; color: #999;">暂无数据</td>
              </tr>
              <tr v-for="(item, index) in paginatedQueryResults" :key="index">
                <td>{{ item.name || '-' }}</td>
                <td>{{ item.value || '-' }}</td>
                <td>{{ item.devicelistId || '-' }}</td>
                <td>{{ item.address !== null && item.address !== undefined ? item.address : '-' }}</td>
                <td>{{ formatUploadTime(item.uploadTime || item.lastTime) }}</td>
              </tr>
            </tbody>
          </table>

          <!-- 分页信息 -->
          <div v-if="queryResults.length > 0" class="pagination-info">
            第 {{ queryCurrentPage }} / {{ totalPages }} 页，共 {{ queryResults.length }} 条数据
          </div>

          <!-- 分页按钮 -->
          <div v-if="queryResults.length > queryPageSize" class="pagination">
            <button @click="queryCurrentPage = Math.max(1, queryCurrentPage - 1)" :disabled="queryCurrentPage === 1">上一页</button>
            <button
              v-for="page in displayedPages"
              :key="page"
              :class="{ active: page === queryCurrentPage }"
              @click="typeof page === 'number' && (queryCurrentPage = page)"
              :disabled="typeof page !== 'number'"
            >{{ page }}</button>
            <button @click="queryCurrentPage = Math.min(totalPages, queryCurrentPage + 1)" :disabled="queryCurrentPage === totalPages">下一页</button>
          </div>
        </div>

        <!-- 高级搜索 -->
        <div v-show="activeQueryTab === 'advanced'" class="tab-content">
          <h2 class="section-title">高级搜索</h2>

          <div class="form-group">
            <label>按电能终端ID和时间范围查询</label>
          </div>
          <div class="time-range-group">
            <input
              type="text"
              v-model="advancedQueryForm.devicelistId"
              placeholder="电能终端ID"
              class="search-input"
              style="max-width: 200px;"
            />
            <select v-model="timeRangeSelect" @change="handleTimeRangeChange" class="search-select">
              <option value="">快速选择时间</option>
              <option value="today">今天</option>
              <option value="3days">近3天</option>
              <option value="7days">近7天</option>
            </select>
          </div>
          <div class="search-section">
            <input
              type="datetime-local"
              v-model="advancedQueryForm.startTime"
              class="search-input"
            />
            <input
              type="datetime-local"
              v-model="advancedQueryForm.endTime"
              class="search-input"
            />
            <button class="query-btn" @click="handleAdvancedQuery" :disabled="queryLoading">搜索</button>
          </div>

          <div class="form-group" style="margin-top: 20px;">
            <label>按名称查询</label>
          </div>
          <div class="search-section">
            <input
              type="text"
              v-model="advancedQueryForm.name"
              placeholder="请输入名称"
              class="search-input"
            />
            <button class="query-btn" @click="handleSearchByName" :disabled="queryLoading">搜索</button>
            <button class="export-btn" @click="exportQueryResultsToExcel" :disabled="queryResults.length === 0">
              导出为EXCEL
            </button>
          </div>

          <!-- 加载提示 -->
          <div v-if="queryLoading" class="loading-spinner">正在查询，请稍等...</div>

          <!-- 数据表格 -->
          <table class="data-table" v-if="!queryLoading">
            <thead>
              <tr>
                <th>名称</th>
                <th @click="setSortColumn('value')">
                  值
                  <span class="sort-icon" :class="sortColumn === 'value' ? sortOrder : ''"></span>
                </th>
                <th @click="setSortColumn('devicelistId')">
                  电能终端ID
                  <span class="sort-icon" :class="sortColumn === 'devicelistId' ? sortOrder : ''"></span>
                </th>
                <th>地址号</th>
                <th @click="setSortColumn('uploadTime')">
                  上传时间
                  <span class="sort-icon" :class="sortColumn === 'uploadTime' ? sortOrder : ''"></span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="paginatedQueryResults.length === 0">
                <td colspan="5" style="text-align: center; color: #999;">暂无数据</td>
              </tr>
              <tr v-for="(item, index) in paginatedQueryResults" :key="index">
                <td>{{ item.name || '-' }}</td>
                <td>{{ item.value || '-' }}</td>
                <td>{{ item.devicelistId || '-' }}</td>
                <td>{{ item.address !== null && item.address !== undefined ? item.address : '-' }}</td>
                <td>{{ formatUploadTime(item.uploadTime || item.lastTime) }}</td>
              </tr>
            </tbody>
          </table>

          <!-- 分页信息 -->
          <div v-if="queryResults.length > 0" class="pagination-info">
            第 {{ queryCurrentPage }} / {{ totalPages }} 页，共 {{ queryResults.length }} 条数据
          </div>

          <!-- 分页按钮 -->
          <div v-if="queryResults.length > queryPageSize" class="pagination">
            <button @click="queryCurrentPage = Math.max(1, queryCurrentPage - 1)" :disabled="queryCurrentPage === 1">上一页</button>
            <button
              v-for="page in displayedPages"
              :key="page"
              :class="{ active: page === queryCurrentPage }"
              @click="typeof page === 'number' && (queryCurrentPage = page)"
              :disabled="typeof page !== 'number'"
            >{{ page }}</button>
            <button @click="queryCurrentPage = Math.min(totalPages, queryCurrentPage + 1)" :disabled="queryCurrentPage === totalPages">下一页</button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editForm.uniqueId ? '编辑电表' : '新增电表'" width="600px">
      <el-form :model="editForm" label-width="140px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地址号">
              <el-input-number v-model="editForm.address" :min="0" :max="255" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电能终端设备号">
              <el-select v-model="editForm.devicelistId" placeholder="请选择" style="width: 100%" filterable allow-create>
                <el-option
                  v-for="item in deviceOptions"
                  :key="item.deviceId"
                  :label="item.deviceId"
                  :value="item.deviceId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电能终端名称">
              <el-input v-model="editForm.loopName" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="方向">
              <el-select v-model="editForm.direction" placeholder="请选择方向" style="width: 100%">
                <el-option label="右线" :value="1" />
                <el-option label="左线" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="序号">
              <el-input-number v-model="editForm.meterIndex" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电能表厂商">
              <el-select v-model="editForm.vendorId" placeholder="请选择厂商" style="width: 100%">
                <el-option
                  v-for="vendor in vendorList"
                  :key="vendor.vendorid"
                  :label="vendor.vendorname"
                  :value="vendor.vendorid"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否启用">
              <el-select v-model="editForm.isEnabled" placeholder="请选择" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否虚拟表">
              <el-select v-model="editForm.isVirtual" placeholder="请选择" style="width: 100%">
                <el-option label="是" :value="1" />
                <el-option label="否" :value="0" />
              </el-select>
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { useTunnelStore } from '@/stores/tunnel'
import { getPowerVendorList } from '@/api/device'
import { dateToDatetimeLocal } from '@/utils/datetime'
import {
  getPowerTerminalList,
  getPowerList,
  addPowerEdgeComputing,
  updatePowerEdgeComputing,
  deletePowerEdgeComputing,
  searchPowerDataByDevicelistId,
  searchPowerDataByDevicelistIdAndTime,
  searchPowerDataByName
} from '@/api/tunnelParam'

const tunnelStore = useTunnelStore()
const loading = ref(false)
const saving = ref(false)
const allTableData = ref([]) // 全部电表数据
const tableData = ref([]) // 显示的电表数据（筛选后）
const deviceOptions = ref([]) // 电能终端设备号选项（从电表数据中提取）
const vendorList = ref([])
const dialogVisible = ref(false)
const editForm = ref({})

// 查询弹窗相关
const queryDialogVisible = ref(false)
const activeQueryTab = ref('basic')
const queryLoading = ref(false)
const queryResults = ref([])
const queryCurrentPage = ref(1)
const queryPageSize = ref(10)
const timeRangeSelect = ref('')
const sortColumn = ref('')
const sortOrder = ref('asc')

// 基础查询表单
const basicQueryForm = ref({
  devicelistId: '',
  queryLimit: '100'
})

// 高级查询表单
const advancedQueryForm = ref({
  devicelistId: '',
  startTime: '',
  endTime: '',
  name: ''
})

// 当前隧道ID（从store获取）
const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const searchForm = reactive({ devicelistId: '' })

// 查询结果分页数据
const paginatedQueryResults = computed(() => {
  const start = (queryCurrentPage.value - 1) * queryPageSize.value
  const end = start + queryPageSize.value
  return queryResults.value.slice(start, end)
})

// 总页数
const totalPages = computed(() => {
  return Math.ceil(queryResults.value.length / queryPageSize.value) || 1
})

// 显示的页码
const displayedPages = computed(() => {
  const total = totalPages.value
  const current = queryCurrentPage.value
  const pages = []

  if (total <= 5) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, '...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...', total - 2, total - 1, total)
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total)
    }
  }
  return pages
})

// 获取厂商名称
const getVendorName = (vendorId) => {
  const vendor = vendorList.value.find((v) => v.vendorid === vendorId)
  return vendor ? vendor.vendorname : ''
}

// 格式化上传时间，将"T"替换为空格
const formatUploadTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').substring(0, 19)
}

const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

// 加载电能终端列表
const loadDeviceOptions = async () => {
  if (!currentTunnelId.value) return

  try {
    // 使用 /tunnel/get/devicelist 接口，type: 2 表示电能终端
    const res = await getPowerTerminalList({ tunnelId: currentTunnelId.value })
    if (res.code === 200) {
      // 保存设备信息，deviceId就是设备号
      deviceOptions.value = (res.data || []).map((item) => ({
        deviceId: item.deviceId // 设备号（用于显示和筛选）
      }))
      console.log('电能终端列表:', deviceOptions.value)
    }
  } catch (error) {
    console.error('获取电能终端列表失败:', error)
  }
}

// 加载全部电表数据
const loadAllData = async () => {
  if (!currentTunnelId.value) return

  loading.value = true
  allTableData.value = []
  
  try {
    // 先获取电能终端列表
    const terminalRes = await getPowerTerminalList({ tunnelId: currentTunnelId.value })
    if (terminalRes.code === 200 && terminalRes.data && terminalRes.data.length > 0) {
      // 遍历每个电能终端，获取其下的电表列表
      for (const terminal of terminalRes.data) {
        try {
          const powerRes = await getPowerList(terminal.deviceId)
          if (powerRes.code === 200 && powerRes.data && powerRes.data.length > 0) {
            // 将电表数据添加到列表中
            powerRes.data.forEach(item => {
              allTableData.value.push({
                ...item,
                devicelistId: terminal.deviceId // 确保有电能终端ID
              })
            })
          }
        } catch (e) {
          console.error(`获取电能终端 ${terminal.deviceId} 的电表列表失败:`, e)
        }
      }
      console.log('电表列表:', allTableData.value)
    }
    // 显示全部数据
    tableData.value = [...allTableData.value]
  } catch (error) {
    console.error('获取电表列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听隧道变化，加载电能终端列表和电表数据
watch(
  () => tunnelStore.currentTunnelId,
  async (newVal, oldVal) => {
    // 重置筛选条件和数据
    searchForm.devicelistId = ''
    deviceOptions.value = []
    allTableData.value = []
    tableData.value = []

    if (newVal) {
      // 加载电能终端列表
      await loadDeviceOptions()
      // 加载全部电表数据
      await loadAllData()
    }
  },
  { immediate: true }
)

// 电能终端选择变化时自动筛选
const handleDeviceChange = () => {
  if (searchForm.devicelistId) {
    // 将选中的设备号和电表数据中的devicelistId都转为字符串进行比较
    const selectedDeviceId = String(searchForm.devicelistId)
    tableData.value = allTableData.value.filter((item) => String(item.devicelistId) === selectedDeviceId)
  } else {
    tableData.value = [...allTableData.value]
  }
}

const handleSearch = () => {
  if (!currentTunnelId.value) {
    ElMessage.warning('请先选择隧道')
    return
  }
  // 根据选择的电能终端进行前端筛选
  handleDeviceChange()
}

const loadData = async () => {
  await loadAllData()
  // 如果有筛选条件，应用筛选
  if (searchForm.devicelistId) {
    tableData.value = allTableData.value.filter((item) => item.devicelistId === searchForm.devicelistId)
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

  const headers = [
    '地址号',
    '电能终端设备号',
    '电能终端名称',
    '方向',
    '电能表厂商',
    '是否启用',
    '最后更新时间'
  ]
  const data = tableData.value.map((row) => [
    row.address || '',
    row.devicelistId || '',
    row.loopName || '',
    row.direction === 1 ? '右线' : '左线',
    row.vendorName || getVendorName(row.vendorId),
    row.isEnabled === 1 ? '启用' : '禁用',
    row.lastTime || ''
  ])

  const csvContent = [headers.join(','), ...data.map((row) => row.join(','))].join('\n')
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

const handleAdd = () => {
  editForm.value = {
    tunnelId: currentTunnelId.value,
    address: 0,
    devicelistId: searchForm.devicelistId || null, // 使用选中的设备号
    loopName: '',
    direction: 1,
    meterIndex: 0,
    vendorId: null,
    isEnabled: 1,
    isVirtual: 0
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
    const api = editForm.value.uniqueId ? updatePowerEdgeComputing : addPowerEdgeComputing
    const res = await api(editForm.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || '保存失败')
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
  })
    .then(async () => {
      try {
        const res = await deletePowerEdgeComputing(row.uniqueId)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 加载厂商列表
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

// 显示查询弹窗
const showQueryDialog = () => {
  queryDialogVisible.value = true
  queryResults.value = []
  queryCurrentPage.value = 1
  activeQueryTab.value = 'basic'
}

// 基础查询 - 全隧道查询，按电能终端ID查询
const handleBasicQuery = async () => {
  const devicelistId = basicQueryForm.value.devicelistId
  if (!devicelistId) {
    ElMessage.warning('请输入电能终端ID')
    return
  }

  queryLoading.value = true
  try {
    const limit = basicQueryForm.value.queryLimit === '-1' ? 99999 : parseInt(basicQueryForm.value.queryLimit)
    const res = await searchPowerDataByDevicelistId(devicelistId, limit)
    if (res.code === 200) {
      queryResults.value = res.data || []
      queryCurrentPage.value = 1
      ElMessage.success(`查询成功，共 ${queryResults.value.length} 条数据`)
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
    console.error('基础查询失败:', error)
  } finally {
    queryLoading.value = false
  }
}

// 高级查询 - 按电能终端ID和时间范围查询
const handleAdvancedQuery = async () => {
  const { devicelistId, startTime, endTime } = advancedQueryForm.value
  if (!devicelistId || !startTime || !endTime) {
    ElMessage.warning('请填写所有字段')
    return
  }

  queryLoading.value = true
  try {
    const res = await searchPowerDataByDevicelistIdAndTime(devicelistId, startTime, endTime)
    if (res.code === 200) {
      queryResults.value = res.data || []
      queryCurrentPage.value = 1
      ElMessage.success(`查询成功，共 ${queryResults.value.length} 条数据`)
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
    console.error('高级查询失败:', error)
  } finally {
    queryLoading.value = false
  }
}

// 按名称查询
const handleSearchByName = async () => {
  const name = advancedQueryForm.value.name
  if (!name) {
    ElMessage.warning('请输入名称')
    return
  }

  queryLoading.value = true
  try {
    const res = await searchPowerDataByName(name)
    if (res.code === 200) {
      queryResults.value = res.data || []
      queryCurrentPage.value = 1
      ElMessage.success(`查询成功，共 ${queryResults.value.length} 条数据`)
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
    console.error('按名称查询失败:', error)
  } finally {
    queryLoading.value = false
  }
}

// 快速时间选择
const handleTimeRangeChange = () => {
  const now = new Date()
  let startTime, endTime

  switch (timeRangeSelect.value) {
    case 'today':
      startTime = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
      break
    case '3days':
      startTime = new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000)
      endTime = new Date(now.getTime() + 24 * 60 * 60 * 1000)
      break
    case '7days':
      startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      endTime = new Date(now.getTime() + 24 * 60 * 60 * 1000)
      break
    default:
      return
  }

  // 格式化为datetime-local格式
  advancedQueryForm.value.startTime = dateToDatetimeLocal(startTime)
  advancedQueryForm.value.endTime = dateToDatetimeLocal(endTime)
}

// 排序
const setSortColumn = (column) => {
  if (sortColumn.value === column) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortColumn.value = column
    sortOrder.value = 'asc'
  }

  // 对数据进行排序
  queryResults.value.sort((a, b) => {
    let aVal = a[column]
    let bVal = b[column]

    if (column === 'uploadTime' || column === 'lastTime') {
      aVal = new Date(aVal).getTime()
      bVal = new Date(bVal).getTime()
    }

    if (sortOrder.value === 'asc') {
      return aVal > bVal ? 1 : -1
    } else {
      return aVal < bVal ? 1 : -1
    }
  })

  queryCurrentPage.value = 1
}

// 导出查询结果为Excel
const exportQueryResultsToExcel = () => {
  if (queryResults.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }

  const headers = ['名称', '值', '电能终端ID', '地址号', '上传时间']
  const data = queryResults.value.map(row => [
    row.name || '',
    row.value || '',
    row.devicelistId || '',
    row.address || '',
    row.uploadTime || row.lastTime || ''
  ])

  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `tunnel_power_data_${new Date().getTime()}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  await loadVendorList()

  // 注意：不需要在这里手动加载数据
  // watch 监听 currentTunnelId 变化时会自动触发数据加载
  // 如果在这里再次加载，会导致数据重复
})
</script>

<style lang="scss" scoped>
.power-config {
  width: 100%;
  min-width: 0;

  :deep(.card) {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
  }

  .tunnel-cascader-wide {
    width: min(400px, 100%);
    max-width: 100%;
  }

  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .table-container.table-responsive {
    display: block;
    width: 100%;
    min-width: 0;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;

    :deep(.el-table) {
      width: 100% !important;
      min-width: 640px;
    }
    :deep(.el-table__inner-wrapper),
    :deep(.el-table__body-wrapper),
    :deep(.el-table__header-wrapper) {
      width: 100%;
    }
  }
}

// Query_SQL 项目样式
.query-container {
  padding: 20px;

  .tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 30px;
    border-bottom: 2px solid #eee;
  }

  .tab-btn {
    padding: 10px 20px;
    background: transparent;
    color: #999;
    border: none;
    border-bottom: 3px solid transparent;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.3s;

    &.active {
      color: #667eea;
      border-bottom-color: #667eea;
    }

    &:hover {
      color: #667eea;
    }
  }

  .tab-content {
    min-height: 400px;
  }

  .section-title {
    color: #667eea;
    margin-bottom: 20px;
    font-size: 18px;
    text-align: center;
  }

  .form-group {
    margin-bottom: 15px;

    label {
      display: block;
      margin-bottom: 8px;
      color: #555;
      font-weight: 500;
    }
  }

  .search-section {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .time-range-group {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
    align-items: center;
  }

  .search-input {
    flex: 1;
    min-width: 200px;
    padding: 12px;
    border: 1px solid #ddd;
    border-radius: 5px;
    font-size: 14px;
    transition: border-color 0.3s;

    &:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 5px rgba(102, 126, 234, 0.1);
    }
  }

  .search-select {
    min-width: 120px;
    padding: 12px;
    border: 1px solid #ddd;
    border-radius: 5px;
    font-size: 14px;
    background: white;
    cursor: pointer;

    &:focus {
      outline: none;
      border-color: #667eea;
    }
  }

  .query-btn {
    padding: 12px 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 5px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s, opacity 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    &:active {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }
  }

  .export-btn {
    padding: 12px 30px;
    background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
    color: white;
    border: none;
    border-radius: 5px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s, opacity 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }
  }

  .loading-spinner {
    text-align: center;
    padding: 20px;
    color: #667eea;
    font-weight: 600;
  }

  .data-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;

    th {
      background: #f5f5f5;
      padding: 12px;
      text-align: left;
      color: #333;
      font-weight: 600;
      border-bottom: 2px solid #ddd;
      cursor: pointer;
      user-select: none;
      position: relative;

      &:hover {
        background: #f0f0f0;
      }
    }

    td {
      padding: 12px;
      border-bottom: 1px solid #eee;
    }

    tr:hover td {
      background: #f9f9f9;
    }
  }

  .sort-icon {
    display: inline-block;
    width: 0;
    height: 0;
    margin-left: 6px;
    vertical-align: middle;
    transition: all 0.2s;

    &.asc {
      border-left: 5px solid transparent;
      border-right: 5px solid transparent;
      border-bottom: 6px solid #667eea;
    }

    &.desc {
      border-left: 5px solid transparent;
      border-right: 5px solid transparent;
      border-top: 6px solid #667eea;
    }
  }

  .pagination-info {
    text-align: center;
    margin-top: 15px;
    color: #666;
    font-size: 14px;
  }

  .pagination {
    display: flex;
    justify-content: center;
    gap: 5px;
    margin-top: 15px;
    flex-wrap: wrap;

    button {
      padding: 8px 12px;
      background: #f0f0f0;
      color: #333;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.2s;

      &:hover:not(:disabled) {
        background: #e0e0e0;
      }

      &.active {
        background: #667eea;
        color: white;
        border-color: #667eea;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
}

// 弹窗样式覆盖
:deep(.query-dialog) {
  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}
</style>
