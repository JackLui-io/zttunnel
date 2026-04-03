<template>
  <div class="notice-list">
    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="搜索">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索通知内容"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 150px">
            <el-option
              v-for="type in noticeTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="filterForm.process" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常状态">
          <el-select v-model="filterForm.exceptionStatus" placeholder="全部异常" clearable style="width: 150px">
            <el-option label="灯具异常" :value="1" />
            <el-option label="异常停车" :value="2" />
            <el-option label="边缘模组" :value="3" />
            <el-option label="串口状态" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleAdd">新增</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in typeStats" :key="stat.type">
        <div class="stat-card" :class="'type-' + stat.type">
          <div class="stat-value">{{ stat.count }}</div>
          <div class="stat-title">{{ stat.typeName }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <div class="card">
      <div class="card-header">
        <h3>通知列表</h3>
      </div>
      <div class="card-content">
        <el-table :data="tableData" v-loading="loading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="content" label="通知内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="type" label="类型" width="120">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)" size="small">
                {{ getTypeName(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="tunnelName" label="隧道" width="150" show-overflow-tooltip />
          <el-table-column prop="exceptionStatus" label="异常状态" width="120">
            <template #default="scope">
              <el-tag
                v-if="scope.row.exceptionStatus"
                :type="getExceptionStatusTagType(scope.row.exceptionStatus)"
                size="small"
              >
                {{ getExceptionStatusName(scope.row.exceptionStatus) }}
              </el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="process" label="处理状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.process === 1 ? 'success' : 'warning'" size="small">
                {{ scope.row.process === 1 ? '已处理' : '未处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <el-button
                :type="scope.row.process === 0 ? 'primary' : 'warning'"
                link
                size="small"
                @click="handleToggleProcess(scope.row)"
              >
                {{ scope.row.process === 0 ? '标记处理' : '标记未处理' }}
              </el-button>
              <el-button
                type="info"
                link
                size="small"
                @click="handleViewDetail(scope.row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSearch"
          @current-change="handleSearch"
          style="margin-top: 20px; text-align: right;"
        />
      </div>
    </div>

    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="通知详情"
      width="700px"
    >
      <div v-if="currentNoticeDetail" class="notice-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="通知ID">
            {{ currentNoticeDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="通知类型">
            <el-tag :type="getTypeTagType(currentNoticeDetail.type)" size="small">
              {{ getTypeName(currentNoticeDetail.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="隧道名称" :span="2">
            {{ currentNoticeDetail.tunnelName || '未知隧道' }}
          </el-descriptions-item>
          <el-descriptions-item label="异常状态" v-if="currentNoticeDetail.exceptionStatus">
            <el-tag
              :type="getExceptionStatusTagType(currentNoticeDetail.exceptionStatus)"
              size="small"
            >
              {{ getExceptionStatusName(currentNoticeDetail.exceptionStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="currentNoticeDetail.process === 1 ? 'success' : 'warning'" size="small">
              {{ currentNoticeDetail.process === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ currentNoticeDetail.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2" v-if="currentNoticeDetail.updateTime">
            {{ currentNoticeDetail.updateTime }}
          </el-descriptions-item>
          <el-descriptions-item label="通知内容" :span="2">
            <div class="notice-content-detail">
              {{ currentNoticeDetail.content }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            :type="currentNoticeDetail?.process === 0 ? 'primary' : 'warning'"
            @click="handleToggleProcess(currentNoticeDetail); detailDialogVisible = false"
          >
            {{ currentNoticeDetail?.process === 0 ? '标记为已处理' : '标记为未处理' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增/编辑通知对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="noticeForm"
        :rules="formRules"
        label-width="100px"
      >
        <!-- 1. 通知类型（最上方） -->
        <el-form-item label="通知类型" prop="type">
          <el-select
            v-model="noticeForm.type"
            placeholder="请选择类型"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option
              v-for="type in noticeTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>

        <!-- 2. 隧道选择（其次） -->
        <el-form-item label="隧道" prop="tunnelId" v-if="noticeForm.type !== 1">
          <el-cascader
            v-model="noticeForm.tunnelPath"
            :options="tunnelCascaderOptions"
            :props="{ expandTrigger: 'hover', checkStrictly: true }"
            placeholder="请选择隧道"
            clearable
            filterable
            style="width: 100%"
            @change="handleNoticeFormTunnelChange"
          />
          <div v-if="tunnelCascaderOptions.length === 0" class="tunnel-loading-tip">
            <el-text type="info" size="small">
              <el-icon><Loading /></el-icon>
              正在加载隧道数据...
            </el-text>
          </div>
        </el-form-item>
        <el-form-item v-if="noticeForm.type === 1">
          <el-alert
            title="系统通知无需选择隧道，将应用于全系统"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form-item>

        <!-- 3. 通知内容（再次） -->
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入通知内容"
          />
          <!-- 模板选择 -->
          <div v-if="availableTemplates.length > 0" class="template-section">
            <el-divider content-position="left">
              <el-text type="primary" size="small">快速模板</el-text>
            </el-divider>
            <div class="template-buttons">
              <el-button
                v-for="template in availableTemplates"
                :key="template.id"
                size="small"
                type="primary"
                plain
                @click="applyTemplate(template)"
              >
                {{ template.name }}
              </el-button>
            </div>
          </div>
        </el-form-item>

        <!-- 4. 异常状态（如果需要） -->
        <el-form-item label="异常状态" v-if="noticeForm.type === 3 || noticeForm.type === 4">
          <el-select v-model="noticeForm.exceptionStatus" placeholder="请选择异常状态" style="width: 100%">
            <el-option label="灯具异常" :value="1" />
            <el-option label="异常停车" :value="2" />
            <el-option label="边缘模组" :value="3" />
            <el-option label="串口状态" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Search } from '@element-plus/icons-vue'
import { getNoticeList, saveOrUpdateNotice } from '@/api/notice'
import { getTunnelTree, getAllTunnelTree } from '@/api/tunnel'
import { useTunnelStore } from '@/stores/tunnel'

const tunnelStore = useTunnelStore()

const loading = ref(false)
const tableData = ref([])
const typeStats = ref([])
const noticeTypes = ref([])

// 隧道信息映射
const tunnelInfoMap = ref(new Map())

// 通知模板数据
const noticeTemplates = ref({
  1: [ // 系统通知模板
    { id: 1, name: '系统维护通知', content: '系统将于今晚22:00-24:00进行维护升级，届时系统将暂停服务，请提前做好相关准备工作。' },
    { id: 2, name: '功能更新通知', content: '系统已完成功能更新，新增了以下功能：1. xxx 2. xxx，请查阅使用手册了解详情。' },
    { id: 3, name: '安全提醒', content: '请各位用户注意账号安全，定期修改密码，不要将账号信息泄露给他人。' },
    { id: 4, name: '节假日通知', content: '根据国家法定节假日安排，系统将于xx月xx日至xx月xx日期间正常运行，如有问题请联系值班人员。' }
  ],
  2: [ // 操作提醒模板
    { id: 5, name: '设备维护提醒', content: '请注意：隧道内设备需要进行定期维护，请相关人员做好准备。' },
    { id: 6, name: '巡检提醒', content: '根据巡检计划，今日需要对隧道设备进行例行巡检，请巡检人员按时到位。' },
    { id: 7, name: '数据备份提醒', content: '请注意进行数据备份操作，确保重要数据的安全性。' }
  ]
})

// 当前可用的模板
const availableTemplates = computed(() => {
  if (!noticeForm.type) {
    return []
  }
  return noticeTemplates.value[noticeForm.type] || []
})

// 隧道级联选择器数据（转换格式）
const tunnelCascaderOptions = computed(() => {
  // 优先使用store中的数据
  if (tunnelStore.tunnelTree && tunnelStore.tunnelTree.length > 0) {
    return convertToCascaderFormat(tunnelStore.tunnelTree)
  }
  // 如果store没有数据，使用本地隧道树数据
  if (localTunnelTree.value && localTunnelTree.value.length > 0) {
    return convertToCascaderFormat(localTunnelTree.value)
  }
  return []
})

// 本地隧道树数据
const localTunnelTree = ref([])

// 转换隧道树为级联选择器格式
const convertToCascaderFormat = (tree) => {
  if (!tree || !Array.isArray(tree)) return []
  return tree.map(node => ({
    value: node.id,
    label: node.tunnelName || node.name,
    children: node.children && node.children.length > 0
      ? convertToCascaderFormat(node.children)
      : undefined
  }))
}

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增通知')
const submitLoading = ref(false)
const formRef = ref()

// 详情对话框
const detailDialogVisible = ref(false)
const currentNoticeDetail = ref(null)

// 自动刷新定时器
let refreshTimer = null
const refreshInterval = 30000 // 30秒自动刷新一次

const filterForm = reactive({
  keyword: '', // 搜索关键词（通知内容）
  type: null,
  process: null,
  exceptionStatus: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 新增通知表单
const noticeForm = reactive({
  content: '',
  type: null,
  tunnelId: null,
  tunnelPath: [],
  exceptionStatus: null,
  process: 0
})

// 表单验证规则
const formRules = reactive({
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  tunnelId: [
    {
      required: true,
      message: '请选择隧道',
      trigger: 'change',
      validator: (_, value, callback) => {
        // 系统通知不需要选择隧道
        if (noticeForm.type === 1) {
          callback()
        } else if (!value) {
          callback(new Error('请选择隧道'))
        } else {
          callback()
        }
      }
    }
  ]
})



// 构建隧道信息映射
const buildTunnelInfoMap = (nodes, map) => {
  nodes.forEach(node => {
    map.set(node.id, {
      name: node.tunnelName,
      level: node.level,
      parentId: node.parentId
    })
    if (node.children && node.children.length > 0) {
      buildTunnelInfoMap(node.children, map)
    }
  })
}

// 根据tunnelId获取显示用的隧道名称
const getTunnelDisplayName = (tunnelId) => {
  const info = tunnelInfoMap.value.get(tunnelId)
  if (!info) {
    return '未知隧道'
  }

  // 如果是level=4（具体隧道左/右线），使用父级level=3的名称
  if (info.level === 4 && info.parentId) {
    const parentInfo = tunnelInfoMap.value.get(info.parentId)
    if (parentInfo && parentInfo.level === 3) {
      return parentInfo.name
    }
  }

  // 其他情况使用自身名称
  return info.name
}

// 初始化通知类型数据
const initNoticeTypes = async () => {
  try {
    // 定义四个固定的通知类型：系统通知、操作提醒、实时警报、设备告警
    noticeTypes.value = [
      { value: 1, label: '系统通知' },
      { value: 2, label: '操作提醒' },
      { value: 3, label: '实时警报' },
      { value: 4, label: '设备告警' }
    ]

    // 初始化统计数据（初始为0，后续通过loadTypeStats更新）
    typeStats.value = noticeTypes.value.map(type => ({
      type: type.value,
      typeName: type.label,
      count: 0
    }))
  } catch (error) {
    console.error('初始化通知类型失败:', error)
    // 使用默认类型
    noticeTypes.value = [
      { value: 1, label: '系统通知' },
      { value: 2, label: '操作提醒' },
      { value: 3, label: '实时警报' },
      { value: 4, label: '设备告警' }
    ]
    typeStats.value = noticeTypes.value.map(type => ({
      type: type.value,
      typeName: type.label,
      count: 0
    }))
  }
}

// 加载类型统计（统计未处理的通知数量）
const loadTypeStats = async () => {
  try {
    console.log('开始加载类型统计...')

    // 重置所有统计为0
    typeStats.value.forEach(stat => {
      stat.count = 0
    })

    // 分别获取每种类型的未处理通知统计数据
    const typePromises = [1, 2, 3, 4].map(async (type) => {
      try {
        const searchParams = {
          type: type,
          process: 0, // 只统计未处理的通知
          pageNum: 1,
          pageSize: 1
        }

        const res = await getNoticeList(searchParams)

        if (res.code === 200) {
          const stat = typeStats.value.find(s => s.type === type)
          if (stat) {
            stat.count = res.total || 0
          }
        }
      } catch (error) {
        console.error(`获取类型${type}统计失败:`, error)
      }
    })

    // 等待所有统计请求完成
    await Promise.all(typePromises)
    console.log('类型统计加载完成:', typeStats.value)
  } catch (error) {
    console.error('加载类型统计失败:', error)
  }
}

// 加载数据
const handleSearch = async () => {
  loading.value = true

  // 判断是否需要前端过滤（关键词搜索或异常状态筛选）
  const needFrontendFilter = (filterForm.keyword && filterForm.keyword.trim()) ||
                             (filterForm.exceptionStatus !== null && filterForm.exceptionStatus !== undefined)

  try {
    const searchParams = {
      // 如果需要前端过滤，获取更多数据；否则使用正常分页
      pageNum: needFrontendFilter ? 1 : pagination.page,
      pageSize: needFrontendFilter ? 10000 : pagination.size
    }

    // 添加筛选条件（只添加有值的参数）
    if (filterForm.type !== null && filterForm.type !== undefined) {
      searchParams.type = filterForm.type
    }
    if (filterForm.process !== null && filterForm.process !== undefined) {
      searchParams.process = filterForm.process
    }

    console.log('查询参数:', searchParams, '需要前端过滤:', needFrontendFilter)

    const res = await getNoticeList(searchParams)
    console.log('查询响应:', res)

    if (res.code === 200) {
      let resultData = res.rows || res.data || []

      // 前端过滤：内容关键词搜索
      if (filterForm.keyword && filterForm.keyword.trim()) {
        const keyword = filterForm.keyword.trim().toLowerCase()
        resultData = resultData.filter(item =>
          item.content && item.content.toLowerCase().includes(keyword)
        )
        console.log('关键词过滤后数量:', resultData.length)
      }

      // 前端过滤：异常状态筛选
      if (filterForm.exceptionStatus !== null && filterForm.exceptionStatus !== undefined) {
        resultData = resultData.filter(item =>
          item.exceptionStatus === filterForm.exceptionStatus
        )
        console.log('异常状态过滤后数量:', resultData.length)
      }

      // 计算总数和分页
      const totalCount = resultData.length
      let displayData = resultData

      // 如果使用了前端过滤，需要手动分页
      if (needFrontendFilter && totalCount > pagination.size) {
        const startIndex = (pagination.page - 1) * pagination.size
        const endIndex = startIndex + pagination.size
        displayData = resultData.slice(startIndex, endIndex)
      }

      // 处理隧道名称显示
      const processedData = displayData.map(item => ({
        ...item,
        tunnelName: item.tunnelName || getTunnelDisplayName(item.tunnelId)
      }))

      tableData.value = processedData
      pagination.total = needFrontendFilter ? totalCount : (res.total || totalCount)
    }
  } catch (error) {
    console.error('加载通知列表失败:', error)
  } finally {
    loading.value = false
  }
  await loadTypeStats()
}

// 启动自动刷新
const startAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  refreshTimer = setInterval(() => {
    handleSearch()
  }, refreshInterval)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}



// 重置
const handleReset = () => {
  filterForm.keyword = ''
  filterForm.type = null
  filterForm.process = null
  filterForm.exceptionStatus = null
  pagination.page = 1
  handleSearch()
}

// 新增通知
const handleAdd = () => {
  dialogTitle.value = '新增通知'
  resetNoticeForm()
  dialogVisible.value = true
}

// 重置表单
const resetNoticeForm = () => {
  noticeForm.content = ''
  noticeForm.type = null
  noticeForm.tunnelId = null
  noticeForm.tunnelPath = []
  noticeForm.exceptionStatus = null
  noticeForm.process = 0
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 处理通知类型变化
const handleTypeChange = (type) => {
  // 如果选择系统通知，清空隧道选择
  if (type === 1) {
    noticeForm.tunnelId = null
    noticeForm.tunnelPath = []
  }
  // 清空异常状态（只有实时警报和设备告警需要）
  if (type !== 3 && type !== 4) {
    noticeForm.exceptionStatus = null
  }
  // 清空通知内容，让用户重新选择模板或输入
  noticeForm.content = ''
}

// 应用模板
const applyTemplate = (template) => {
  noticeForm.content = template.content
  ElMessage.success(`已应用模板：${template.name}`)
}

// 处理通知表单中的隧道选择
const handleNoticeFormTunnelChange = (path) => {
  if (path && path.length > 0) {
    noticeForm.tunnelId = path[path.length - 1]
  } else {
    noticeForm.tunnelId = null
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    // 构建提交数据
    const submitData = {
      content: noticeForm.content,
      type: noticeForm.type,
      exceptionStatus: noticeForm.exceptionStatus,
      process: noticeForm.process
    }

    // 系统通知不需要tunnelId，其他类型需要
    if (noticeForm.type !== 1) {
      submitData.tunnelId = noticeForm.tunnelId
    }

    console.log('提交数据:', submitData)

    const res = await saveOrUpdateNotice(submitData)

    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      handleSearch()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存通知失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  resetNoticeForm()
}



// 获取类型名称
const getTypeName = (type) => {
  const names = {
    1: '系统通知',
    2: '操作提醒',
    3: '实时警报',
    4: '设备告警'
  }
  return names[type] || '未知'
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  const types = {
    1: 'info',     // 系统通知 - 灰色
    2: 'primary',  // 操作提醒 - 蓝色
    3: 'danger',   // 实时警报 - 红色
    4: 'warning'   // 设备告警 - 橙色
  }
  return types[type] || 'info'
}

// 获取异常状态名称
const getExceptionStatusName = (status) => {
  const names = {
    1: '灯具异常',
    2: '异常停车',
    3: '边缘模组',
    4: '串口状态'
  }
  return names[status] || '未知异常'
}

// 获取异常状态标签样式
const getExceptionStatusTagType = (status) => {
  const types = {
    1: 'warning',  // 灯具异常 - 橙色
    2: 'danger',   // 异常停车 - 红色
    3: 'info',     // 边缘模组 - 灰色
    4: 'primary'   // 串口状态 - 蓝色
  }
  return types[status] || 'info'
}

// 查看详情
const handleViewDetail = (row) => {
  currentNoticeDetail.value = { ...row }
  detailDialogVisible.value = true
}

// 切换处理状态
const handleToggleProcess = async (row) => {
  const newStatus = row.process === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '处理' : '未处理'

  try {
    const res = await saveOrUpdateNotice({
      ...row,
      process: newStatus
    })
    if (res.code === 200) {
      ElMessage.success(`标记为${statusText}成功`)
      handleSearch()
    } else {
      ElMessage.error(res.msg || `标记为${statusText}失败`)
    }
  } catch (error) {
    ElMessage.error(`标记为${statusText}失败`)
  }
}

onMounted(async () => {
  // 初始化通知类型
  await initNoticeTypes()

  // 加载隧道数据（带重试机制）
  await loadTunnelDataWithRetry()

  // 页面加载后自动显示所有通知数据
  await handleSearch()
  // 启动自动刷新
  startAutoRefresh()
})

// 带重试机制的隧道数据加载
const loadTunnelDataWithRetry = async (maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      console.log(`尝试加载隧道数据 (${i + 1}/${maxRetries})...`)

      // 先尝试加载store数据
      await tunnelStore.loadTunnelData()

      // 尝试多个API获取隧道树数据
      let tunnelTreeData = null

      // 1. 先尝试 getTunnelTree (用户权限)
      try {
        const res1 = await getTunnelTree()
        console.log('getTunnelTree API响应:', res1)
        if (res1.code === 200 && res1.data && res1.data.length > 0) {
          tunnelTreeData = res1.data
        }
      } catch (e) {
        console.log('getTunnelTree 失败:', e)
      }

      // 2. 如果失败，尝试 getAllTunnelTree (所有隧道)
      if (!tunnelTreeData) {
        try {
          const res2 = await getAllTunnelTree()
          console.log('getAllTunnelTree API响应:', res2)
          if (res2.code === 200 && res2.data && res2.data.length > 0) {
            tunnelTreeData = res2.data
          }
        } catch (e) {
          console.log('getAllTunnelTree 失败:', e)
        }
      }

      // 3. 如果API成功获取数据
      if (tunnelTreeData && tunnelTreeData.length > 0) {
        localTunnelTree.value = tunnelTreeData
        // 构建隧道信息映射
        tunnelInfoMap.value = new Map()
        buildTunnelInfoMap(tunnelTreeData, tunnelInfoMap.value)

        console.log('隧道数据加载成功:', {
          localTunnelTree: localTunnelTree.value.length,
          tunnelInfoMap: tunnelInfoMap.value.size,
          cascaderOptions: tunnelCascaderOptions.value.length
        })
        break
      }

      // 如果store有数据也可以使用
      if (tunnelStore.tunnelTree && tunnelStore.tunnelTree.length > 0) {
        localTunnelTree.value = tunnelStore.tunnelTree
        tunnelInfoMap.value = new Map()
        buildTunnelInfoMap(tunnelStore.tunnelTree, tunnelInfoMap.value)
        console.log('使用store隧道数据:', tunnelStore.tunnelTree.length)
        break
      }

      // 如果没有数据但没有错误，等待一下再重试
      if (i < maxRetries - 1) {
        console.log('隧道数据为空，1秒后重试...')
        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    } catch (error) {
      console.error(`加载隧道数据失败 (${i + 1}/${maxRetries}):`, error)
      if (i < maxRetries - 1) {
        console.log('1秒后重试...')
        await new Promise(resolve => setTimeout(resolve, 1000))
      } else {
        // 如果所有重试都失败，使用模拟数据
        console.log('使用模拟隧道数据...')
        createMockTunnelData()
        ElMessage.warning('隧道数据加载失败，已使用模拟数据')
      }
    }
  }
}

// 创建模拟隧道数据（用于调试）
const createMockTunnelData = () => {
  const mockData = [
    {
      id: 1,
      tunnelName: '云南省交通运输厅',
      level: 1,
      parentId: null,
      children: [
        {
          id: 10,
          tunnelName: '玉楚高速',
          level: 2,
          parentId: 1,
          children: [
            {
              id: 100,
              tunnelName: '测试隧道群A',
              level: 3,
              parentId: 10,
              children: [
                {
                  id: 1001,
                  tunnelName: '测试隧道A-左线',
                  level: 4,
                  parentId: 100,
                  children: []
                },
                {
                  id: 1002,
                  tunnelName: '测试隧道A-右线',
                  level: 4,
                  parentId: 100,
                  children: []
                }
              ]
            },
            {
              id: 101,
              tunnelName: '测试隧道群B',
              level: 3,
              parentId: 10,
              children: [
                {
                  id: 1011,
                  tunnelName: '测试隧道B-左线',
                  level: 4,
                  parentId: 101,
                  children: []
                },
                {
                  id: 1012,
                  tunnelName: '测试隧道B-右线',
                  level: 4,
                  parentId: 101,
                  children: []
                }
              ]
            }
          ]
        }
      ]
    }
  ]

  // 设置本地隧道树数据
  localTunnelTree.value = mockData

  // 构建隧道信息映射
  tunnelInfoMap.value = new Map()
  buildTunnelInfoMap(mockData, tunnelInfoMap.value)

  console.log('模拟数据创建完成:', {
    localTunnelTree: localTunnelTree.value.length,
    cascaderOptions: tunnelCascaderOptions.value.length,
    tunnelInfoMap: tunnelInfoMap.value.size
  })
}

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.notice-list {
  .filter-section {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    border-top: 3px solid #409EFF;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    &.type-1 {
      border-top-color: #909399; // 系统通知 - 灰色
      .stat-value { color: #909399; }
    }
    &.type-2 {
      border-top-color: #409EFF; // 操作提醒 - 蓝色
      .stat-value { color: #409EFF; }
    }
    &.type-3 {
      border-top-color: #F56C6C; // 实时警报 - 红色
      .stat-value { color: #F56C6C; }
    }
    &.type-4 {
      border-top-color: #E6A23C; // 设备告警 - 橙色
      .stat-value { color: #E6A23C; }
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      transition: color 0.3s ease;
    }

    .stat-title {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }
  }

  .card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);

    .card-header {
      padding: 16px 20px;
      border-bottom: 1px solid #ebeef5;

      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }

    .card-content {
      padding: 20px;
    }
  }

  .text-muted {
    color: #909399;
    font-size: 12px;
  }

  .notice-detail {
    .notice-content-detail {
      max-height: 200px;
      overflow-y: auto;
      padding: 10px;
      background: #f5f7fa;
      border-radius: 4px;
      line-height: 1.6;
      word-break: break-all;
    }
  }

  // 模板选择区域样式
  .template-section {
    margin-top: 12px;

    .template-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .el-button {
        margin: 0;
        font-size: 12px;
        padding: 4px 8px;
        height: auto;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
        }
      }
    }
  }

  // 隧道加载提示样式
  .tunnel-loading-tip {
    margin-top: 8px;

    .el-text {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}
</style>
