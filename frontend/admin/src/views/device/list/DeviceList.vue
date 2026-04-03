<template>
  <div class="device-management">
    <div class="card workspace-layout">
      <aside class="tree-panel">
      <div class="tree-panel-title">隧道树</div>
      <p class="tree-hint">设备挂在叶子隧道（level=4）；请勾选后点击右侧「查询」加载列表。</p>
      <el-tree
        ref="tunnelTreeRef"
        class="tunnel-tree"
        :data="tunnelStore.tunnelTree"
        node-key="id"
        :props="{ label: 'tunnelName', children: 'children' }"
        show-checkbox
        default-expand-all
      />
      </aside>

      <div class="main-panel">
      <!-- 关键字与查询（与树勾选一并生效） -->
      <div class="search-form workspace-search">
        <el-form :inline="true">
          <el-form-item label="关键字">
            <el-input
              v-model="searchForm.keyword"
              placeholder="设备号/名称/回路/桩号等"
              clearable
              style="width: 220px"
              @keyup.enter="runWorkspaceQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="runWorkspaceQuery">查询</el-button>
            <el-button @click="resetSearch">重置关键字</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-tabs v-model="searchForm.deviceType" class="device-workspace-tabs" @tab-change="onDeviceTabChange">
        <el-tab-pane
          v-for="item in deviceTypeOptions"
          :key="item.value"
          :label="item.label"
          :name="item.value"
        >
          <div class="tab-toolbar">
            <el-button :disabled="!queriedLeafIds.length" @click="refreshAllTabsFromServer">刷新</el-button>
            <el-button type="success" :disabled="!queriedLeafIds.length" @click="handleExportTab">
              导出
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 表格区域：横向滚动 + 列 min-width 适配窄屏与宽屏 -->
      <div class="table-container table-responsive">
        <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="loading"
          table-layout="auto"
        >
          <el-table-column prop="deviceId" label="设备号" min-width="120" show-overflow-tooltip />
          <el-table-column prop="devicePassword" label="设备密码" min-width="100" show-overflow-tooltip v-if="searchForm.deviceType === '边缘控制器' || searchForm.deviceType === '电能终端'" />
          <el-table-column prop="deviceNum" label="设备桩号" min-width="100" show-overflow-tooltip />
          <el-table-column prop="csq" label="信号强度" min-width="90" v-if="searchForm.deviceType !== '洞外雷达' && searchForm.deviceType !== '洞外亮度传感器'" />
          <!-- 设备状态（灯具终端不显示） -->
          <el-table-column label="设备状态" min-width="96" v-if="searchForm.deviceType !== '灯具终端'">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.online)" size="small">
                {{ getStatusText(scope.row.online) }}
              </el-tag>
            </template>
          </el-table-column>
          <!-- 电能终端特有字段 -->
          <el-table-column prop="electricityMeterNum" label="电表数量" min-width="96" v-if="searchForm.deviceType === '电能终端'" />
          <!-- 灯具终端特有字段 -->
          <el-table-column prop="loopNumber" label="回路编号" min-width="96" v-if="searchForm.deviceType === '灯具终端'" />
          <el-table-column label="区段" min-width="88" v-if="searchForm.deviceType === '灯具终端'">
            <template #default="scope">
              {{ getZoneText(scope.row.zone) }}
            </template>
          </el-table-column>
          <el-table-column label="通信状态" min-width="96" v-if="searchForm.deviceType === '灯具终端'">
            <template #default="scope">
              <el-tag :type="scope.row.communicationState === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.communicationState === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="工作状态" min-width="96" v-if="searchForm.deviceType === '灯具终端'">
            <template #default="scope">
              <span v-if="scope.row.workState === 2">/</span>
              <el-tag v-else :type="scope.row.workState === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.workState === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="是否安装雷达" min-width="110" v-if="searchForm.deviceType === '灯具终端'">
            <template #default="scope">
              {{ scope.row.ldWhetherInstall === 1 ? '是' : '否' }}
            </template>
          </el-table-column>
          <el-table-column label="雷达状态" min-width="96" v-if="searchForm.deviceType === '灯具终端'">
            <template #default="scope">
              <span v-if="scope.row.ldWhetherInstall !== 1">/</span>
              <el-tag v-else :type="scope.row.ldStatus === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.ldStatus === 0 ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deviceProperty" label="调光类型" min-width="100" show-overflow-tooltip v-if="searchForm.deviceType === '灯具终端'" />
          <!-- 洞外雷达/亮度传感器特有字段 -->
          <el-table-column prop="loopNumber" label="回路编号" min-width="96" v-if="searchForm.deviceType === '洞外雷达' || searchForm.deviceType === '洞外亮度传感器'" />
          <el-table-column label="更新时间" min-width="160" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.lastUpdate || scope.row.createTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="88" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleDetail(scope.row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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
    </div>

    <!-- 设备表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="deviceFormRef"
        :model="deviceForm"
        :rules="deviceRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="deviceForm.deviceType" placeholder="请选择设备类型" style="width: 100%">
                <el-option label="边缘计算终端" value="edge" />
                <el-option label="灯具终端" value="lamp" />
                <el-option label="雷达设备" value="radar" />
                <el-option label="电能表" value="power" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属隧道" prop="tunnelId">
              <el-cascader
                v-model="deviceForm.tunnelPath"
                :options="tunnelStore.cascaderOptions"
                :props="{ expandTrigger: 'hover' }"
                placeholder="请选择隧道"
                style="width: 100%"
                @change="(val) => deviceForm.tunnelId = val && val.length > 0 ? val[val.length - 1] : null"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区段" prop="zone">
              <el-input v-model="deviceForm.zone" placeholder="请输入区段" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备桩号" prop="deviceNum">
              <el-input v-model="deviceForm.deviceNum" placeholder="请输入设备桩号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回路编号" prop="loopNumber">
              <el-input v-model="deviceForm.loopNumber" placeholder="请输入回路编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设备属性" prop="deviceProperty">
          <el-input
            v-model="deviceForm.deviceProperty"
            type="textarea"
            placeholder="请输入设备属性"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="设备详情" width="600px" :close-on-click-modal="false">
      <div class="detail-content" v-if="currentDevice">
        <div class="detail-header">
          <div class="device-info">
            <h3>{{ currentDevice.deviceName || currentDevice.deviceId }}</h3>
            <el-tag :type="getStatusType(currentDevice.online)" size="large">
              {{ getStatusText(currentDevice.online) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="detail-body">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">设备号</span>
                <span class="value">{{ currentDevice.deviceId }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">设备桩号</span>
                <span class="value">{{ currentDevice.deviceNum || '-' }}</span>
              </div>
            </el-col>
          </el-row>

          <!-- 边缘控制器和电能终端显示设备密码 -->
          <el-row :gutter="20" v-if="searchForm.deviceType === '边缘控制器' || searchForm.deviceType === '电能终端'">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">设备密码</span>
                <span class="value">{{ currentDevice.devicePassword || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">信号强度</span>
                <span class="value">{{ currentDevice.csq || '-' }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">设备类型</span>
                <el-tag :type="getDeviceTypeColor(currentDevice.deviceType)" size="small">
                  {{ currentDevice.deviceType }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12" v-if="searchForm.deviceType !== '边缘控制器' && searchForm.deviceType !== '电能终端'">
              <div class="info-item">
                <span class="label">信号强度</span>
                <span class="value">{{ currentDevice.csq || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12" v-if="searchForm.deviceType !== '灯具终端'">
              <div class="info-item">
                <span class="label">设备状态</span>
                <el-tag :type="getStatusType(currentDevice.online)" size="small">
                  {{ getStatusText(currentDevice.online) }}
                </el-tag>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" v-if="searchForm.deviceType === '灯具终端'">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">回路编号</span>
                <span class="value">{{ currentDevice.loopNumber || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">区段</span>
                <span class="value">{{ getZoneText(currentDevice.zone) }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" v-if="searchForm.deviceType === '灯具终端'">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">通信状态</span>
                <el-tag :type="currentDevice.communicationState === 0 ? 'success' : 'danger'" size="small">
                  {{ currentDevice.communicationState === 0 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">工作状态</span>
                <span v-if="currentDevice.workState === 2" class="value">/</span>
                <el-tag v-else :type="currentDevice.workState === 0 ? 'success' : 'danger'" size="small">
                  {{ currentDevice.workState === 0 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" v-if="searchForm.deviceType === '灯具终端'">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">是否安装雷达</span>
                <span class="value">{{ currentDevice.ldWhetherInstall === 1 ? '是' : '否' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">雷达状态</span>
                <span v-if="currentDevice.ldWhetherInstall !== 1" class="value">/</span>
                <el-tag v-else :type="currentDevice.ldStatus === 0 ? 'success' : 'danger'" size="small">
                  {{ currentDevice.ldStatus === 0 ? '正常' : '异常' }}
                </el-tag>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">更新时间</span>
                <span class="value">{{ currentDevice.lastUpdate || currentDevice.createTime || '-' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceType, getDeviceWorkspaceList, exportDeviceWorkspace } from '@/api/device'
import { getStatusType, getStatusText } from '@/utils/deviceStatus'
import { useTunnelStore } from '@/stores/tunnel'
import { leafTunnelIdsFromTreeChecks } from '@/utils/tunnelTreeLeafIds'

// 获取雷达/亮度仪状态文本（独立状态：正常、异常、未安装）
const getSensorStatusText = (status) => {
  if (status === '正常' || status === 0) return '正常'
  if (status === '异常' || status === 1) return '异常'
  if (status === '未安装' || status === 2) return '未安装'
  if (status === null || status === undefined || status === '') return '未安装'
  return '未安装'
}

// 获取雷达/亮度仪状态标签类型
const getSensorStatusType = (status) => {
  if (status === '正常' || status === 0) return 'success'
  if (status === '异常' || status === 1) return 'danger'
  if (status === '未安装' || status === 2) return 'info'
  if (status === null || status === undefined || status === '') return 'info'
  return 'info'
}

// 隧道store
const tunnelStore = useTunnelStore()

const tunnelTreeRef = ref(null)
/** 最近一次「查询」生效的叶子隧道 id（点击查询后写入） */
const queriedLeafIds = ref([])

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const deviceFormRef = ref()



// 搜索表单
const searchForm = reactive({
  keyword: '',
  deviceType: '边缘控制器'
})

/** 与最近一次查询绑定的关键字快照（分页、刷新、导出沿用） */
const queriedKeyword = ref('')

/** 各设备类型独立缓存：切换 Tab 不重复请求，仅分页/刷新时更新对应类型 */
const workspaceTabCache = reactive({})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])



// 设备类型选项
const deviceTypeOptions = ref([])

// 设备表单
const deviceForm = reactive({
  id: null,
  deviceName: '',
  deviceType: '',
  tunnelId: null,
  zone: '',
  deviceNum: '',
  loopNumber: '',
  deviceProperty: ''
})

// 表单验证规则
const deviceRules = {
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  tunnelId: [
    { required: true, message: '请选择隧道', trigger: 'change' }
  ]
}

// 获取设备类型颜色
const getDeviceTypeColor = (type) => {
  const colors = {
    '边缘控制器': 'primary',
    '电能终端': 'info',
    '灯具终端': 'success',
    '洞外雷达': 'warning',
    '洞外亮度传感器': ''
  }
  return colors[type] || 'info'
}

// 获取区段文本
const getZoneText = (zone) => {
  const texts = {
    1: '入口段',
    2: '过渡1',
    3: '过渡2',
    4: '中间段',
    5: '出口段'
  }
  return texts[zone] || zone || '-'
}



// 获取信号强度颜色
const getSignalColor = (csq) => {
  if (csq >= 80) return '#67c23a'
  if (csq >= 60) return '#e6a23c'
  if (csq >= 40) return '#f56c6c'
  return '#909399'
}



// 加载设备类型选项
const loadDeviceTypeOptions = async () => {
  try {
    const res = await getDeviceType()
    if (res.code === 200 && res.data) {
      // 后端返回的是字符串数组，需要转换为 { value, label } 格式
      if (Array.isArray(res.data)) {
        deviceTypeOptions.value = res.data.map(item => {
          if (typeof item === 'string') {
            return { value: item, label: item }
          }
          return item
        })
      } else {
        deviceTypeOptions.value = res.data
      }
    }
  } catch (error) {
    console.error('加载设备类型选项失败:', error)
    deviceTypeOptions.value = [
      { value: '边缘控制器', label: '边缘控制器' },
      { value: '电能终端', label: '电能终端' },
      { value: '灯具终端', label: '灯具终端' },
      { value: '洞外雷达', label: '洞外雷达' },
      { value: '洞外亮度传感器', label: '洞外亮度传感器' }
    ]
  }
}

const getWorkspaceDeviceTypes = () => {
  if (deviceTypeOptions.value.length) {
    return deviceTypeOptions.value.map((o) => o.value)
  }
  return ['边缘控制器', '电能终端', '灯具终端', '洞外雷达', '洞外亮度传感器']
}

/** 将某一类型的缓存反映到当前表格与分页控件 */
const applyTabSnapshotToView = (deviceType) => {
  const s = workspaceTabCache[deviceType]
  if (!s) {
    tableData.value = []
    pagination.total = 0
    pagination.page = 1
    return
  }
  tableData.value = s.rows
  pagination.total = s.total
  pagination.page = s.page
  pagination.size = s.size
}

/** 点击查询：并行拉取所有设备类型第 1 页（切换 Tab 仅用缓存） */
const fetchAllDeviceTypesOnQuery = async () => {
  const types = getWorkspaceDeviceTypes()
  const pageSize = pagination.size
  loading.value = true
  try {
    const results = await Promise.all(
      types.map((deviceType) =>
        getDeviceWorkspaceList({
          tunnelIds: queriedLeafIds.value,
          deviceType,
          keyword: queriedKeyword.value || undefined,
          pageNum: 1,
          pageSize
        })
      )
    )
    types.forEach((deviceType, i) => {
      const res = results[i]
      if (res.code === 200) {
        workspaceTabCache[deviceType] = {
          rows: res.rows || [],
          total: Number(res.total) || 0,
          page: 1,
          size: pageSize
        }
      }
    })
    applyTabSnapshotToView(searchForm.deviceType)
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

/** 仅请求当前 Tab 当前页（翻页、改每页条数）并写回缓存 */
const fetchCurrentTabPage = async () => {
  if (!queriedLeafIds.value.length) {
    tableData.value = []
    pagination.total = 0
    return
  }
  const dt = searchForm.deviceType
  loading.value = true
  try {
    const res = await getDeviceWorkspaceList({
      tunnelIds: queriedLeafIds.value,
      deviceType: dt,
      keyword: queriedKeyword.value || undefined,
      pageNum: pagination.page,
      pageSize: pagination.size
    })
    if (res.code === 200) {
      workspaceTabCache[dt] = {
        rows: res.rows || [],
        total: Number(res.total) || 0,
        page: pagination.page,
        size: pagination.size
      }
      tableData.value = workspaceTabCache[dt].rows
      pagination.total = workspaceTabCache[dt].total
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

/** 刷新：各 Tab 按各自缓存的页码/每页条数并行重新拉取 */
const refreshAllTabsFromServer = async () => {
  if (!queriedLeafIds.value.length) return
  const types = getWorkspaceDeviceTypes()
  loading.value = true
  try {
    const results = await Promise.all(
      types.map((deviceType) => {
        const prev = workspaceTabCache[deviceType]
        const page = prev?.page ?? 1
        const size = prev?.size ?? pagination.size
        return getDeviceWorkspaceList({
          tunnelIds: queriedLeafIds.value,
          deviceType,
          keyword: queriedKeyword.value || undefined,
          pageNum: page,
          pageSize: size
        }).then((res) => ({ deviceType, res, page, size }))
      })
    )
    results.forEach(({ deviceType, res, page, size }) => {
      if (res.code === 200) {
        workspaceTabCache[deviceType] = {
          rows: res.rows || [],
          total: Number(res.total) || 0,
          page,
          size
        }
      }
    })
    applyTabSnapshotToView(searchForm.deviceType)
  } catch (error) {
    console.error('刷新设备列表失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

// 工作台：树勾选 → 叶子隧道 id → 点查询后请求
const runWorkspaceQuery = () => {
  const keys = tunnelTreeRef.value?.getCheckedKeys?.() ?? []
  const leafIds = leafTunnelIdsFromTreeChecks(keys, tunnelStore.tunnelTree)
  if (!leafIds.length) {
    ElMessage.warning('请勾选至少一个叶子隧道（level=4）')
    return
  }
  queriedLeafIds.value = leafIds
  queriedKeyword.value = (searchForm.keyword || '').trim()
  pagination.page = 1
  Object.keys(workspaceTabCache).forEach((k) => delete workspaceTabCache[k])
  fetchAllDeviceTypesOnQuery()
}

const onDeviceTabChange = (name) => {
  applyTabSnapshotToView(name)
}

/** 仅当前 Tab 列表刷新（删除/对话框等） */
const loadDeviceList = () => {
  fetchCurrentTabPage()
}

const handleExportTab = async () => {
  if (!queriedLeafIds.value.length) {
    ElMessage.warning('请先勾选隧道并点击查询')
    return
  }
  try {
    const res = await exportDeviceWorkspace({
      tunnelIds: queriedLeafIds.value,
      deviceType: searchForm.deviceType,
      keyword: queriedKeyword.value || undefined
    })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${searchForm.deviceType || '设备列表'}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 重置关键字（不清空树勾选；需再次查询才刷新列表）
const resetSearch = () => {
  searchForm.keyword = ''
}

// 新增设备
const handleAdd = () => {
  dialogTitle.value = '新增设备'
  dialogVisible.value = true
  resetForm()
}

// 编辑设备
const handleEdit = (row) => {
  dialogTitle.value = '编辑设备'
  dialogVisible.value = true
  Object.assign(deviceForm, row)
}

const detailDialogVisible = ref(false)
const currentDevice = ref(null)

// 查看详情
const handleDetail = (row) => {
  currentDevice.value = row
  detailDialogVisible.value = true
}

// 删除设备
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除设备"${row.deviceName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    loadDeviceList()
  }).catch(() => {})
}

// 提交表单
const handleSubmit = () => {
  deviceFormRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success(deviceForm.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      loadDeviceList()
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(deviceForm, {
    id: null,
    deviceName: '',
    deviceType: '',
    tunnelId: null,
    zone: '',
    deviceNum: '',
    loopNumber: '',
    deviceProperty: ''
  })
  deviceFormRef.value?.resetFields()
}

// 分页处理（仅更新当前 Tab 的缓存）
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchCurrentTabPage()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchCurrentTabPage()
}

// 初始化
onMounted(async () => {
  await tunnelStore.loadTunnelData()
  await loadDeviceTypeOptions()
})
</script>

<style lang="scss" scoped>
.workspace-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.tree-panel-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.tree-hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin: 0 0 12px;
}

.tunnel-tree {
  max-height: calc(100vh - 220px);
  overflow: auto;
  padding: 8px 0;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.tree-panel {
  width: 300px;
  flex-shrink: 0;
}

.main-panel {
  flex: 1;
  min-width: 0;
}

.workspace-search {
  margin-bottom: 12px;
}

.tab-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.device-workspace-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
}

.device-management {
  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .table-container {
    width: 100%;
    min-width: 0;

    &.table-responsive {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }

    :deep(.el-table) {
      min-width: 720px;
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

// 详情弹窗样式
.detail-content {
  .detail-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    .device-info {
      display: flex;
      align-items: center;
      gap: 16px;

      h3 {
        margin: 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
      }
    }
  }

  .detail-body {
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      min-height: 32px;

      .label {
        width: 120px;
        color: #606266;
        font-size: 14px;
        flex-shrink: 0;
      }

      .value {
        color: #303133;
        font-size: 14px;
        flex: 1;
      }

      .signal-info {
        display: flex;
        align-items: center;
      }
    }
  }
}

// 配置弹窗样式
.param-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;

  h4 {
    margin: 0 0 16px 0;
    color: #303133;
    font-size: 14px;
    font-weight: 600;
  }
}

:deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>
