<template>
  <div class="edge-controller-tab">
    <DeviceTableColumnSplit
      v-model="visibleColumns"
      :options="columnOptions"
      storage-key="tunnel-ws-edge"
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
          <el-table-column v-if="visibleColumns.includes('deviceId')" prop="deviceId" label="设备号" min-width="124" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('devicePassword') && !deviceBindMode" prop="devicePassword" label="设备密码" min-width="100" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('nickName')" prop="nickName" label="终端名称" min-width="120" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('deviceNum')" prop="deviceNum" label="桩号" min-width="96" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('zone')" prop="zone" label="区段" min-width="88" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('loopNumber')" prop="loopNumber" label="回路编号" min-width="96" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('csq')" prop="csq" label="信号强度" min-width="88" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('online')" prop="online" label="在线状态" min-width="92">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.online)">
                {{ getStatusText(scope.row.online) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('deviceStatus')" prop="deviceStatus" label="设备状态" min-width="108" show-overflow-tooltip>
            <template #default="scope">{{ getDevicelistDeviceStatusText(scope.row) }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('lastUpdate')" prop="lastUpdate" label="最后数据" min-width="160" show-overflow-tooltip>
            <template #default="scope">{{ formatDisplayTime(scope.row.lastUpdate) }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('radarStatus')" prop="radarStatus" label="雷达" min-width="80" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.radarStatus || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('brightnessMeterStatus')" prop="brightnessMeterStatus" label="亮度仪" min-width="80" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.brightnessMeterStatus || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('softVer')" prop="softVer" label="主板固件" min-width="110" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.softVer || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('networkInfo')" prop="networkInfo" label="网络信息" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.networkInfo || '—' }}</template>
          </el-table-column>
          <el-table-column v-if="!readOnly" label="操作" :width="deviceBindMode ? 280 : 220" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleControl(scope.row)">控制</el-button>
              <el-button v-if="deviceBindMode" type="warning" size="small" @click="handleBindDevice(scope.row)">绑定</el-button>
              <el-button v-if="deviceBindMode" type="success" size="small" @click="handleEditBindMeta(scope.row)">编辑</el-button>
              <el-button v-if="!deviceBindMode" type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button v-if="!deviceBindMode" type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </DeviceTableColumnSplit>

    <!-- 控制弹窗 -->
    <el-dialog v-model="controlDialogVisible" title="边缘控制器控制" width="1100px" :close-on-click-modal="false">
      <div class="control-tabs">
        <div class="tab-item" :class="{ active: activeTab === 'params' }" @click="activeTab = 'params'">参数</div>
        <div class="tab-item" :class="{ active: activeTab === 'bluetooth' }" @click="activeTab = 'bluetooth'">蓝牙节点</div>
      </div>

      <!-- 参数Tab -->
      <div v-if="activeTab === 'params'" class="params-content">
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">ID：</span>
            <el-input v-model="controlForm.deviceId" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('id')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('id')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">蓝牙ID：</span>
            <el-input v-model="controlForm.bluetoothId" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('bluetoothId')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('bluetoothId')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">边缘状态上报间隔（s）：</span>
            <el-input v-model="controlForm.edgeReportInterval" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('edgeReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('edgeReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具状态上报间隔（s）：</span>
            <el-input v-model="controlForm.lampReportInterval" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampReport')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">灯具与边缘通信超时（s）：</span>
            <el-input v-model="controlForm.commTimeout" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('commTimeout')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('commTimeout')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具终端同步周期（s）：</span>
            <el-input v-model="controlForm.syncPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('syncPeriod')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('syncPeriod')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">设置灯具终端状态上报时间（s）：</span>
            <el-input v-model="controlForm.lampStatusReportTime" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampStatusReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampStatusReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具超时周期：</span>
            <el-input v-model="controlForm.lampTimeoutPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampTimeout')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampTimeout')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">设置工况汇报周期（s）：</span>
            <el-input v-model="controlForm.workReportPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('workReport')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('workReport')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具终端限制设置默认周期：</span>
            <el-input v-model="controlForm.lampLimitPeriod" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn read-btn" @click="handleRead('lampLimit')">读取</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampLimit')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">灯具列表：</span>
            <el-input v-model="controlForm.lampList" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('lampList')">下发</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">灯具参数下发：</span>
            <el-input v-model="controlForm.lampParams" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('lampParams')">下发</div>
              <div class="param-btn update-btn" @click="handleUpdate('lampParams')">更新</div>
            </div>
          </div>
        </div>
        <div class="param-row">
          <div class="param-item">
            <span class="param-label">蓝牙节点：</span>
            <el-input v-model="controlForm.bluetoothNode" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn issue-btn" @click="handleIssue('bluetoothNode')">下发</div>
              <div class="param-btn update-btn" @click="handleUpdate('bluetoothNode')">更新</div>
            </div>
          </div>
          <div class="param-item">
            <span class="param-label">亮度仪波特率：</span>
            <el-input v-model="controlForm.brightnessBaudRate" class="param-input" />
            <div class="param-buttons">
              <div class="param-btn update-btn" @click="handleUpdate('brightnessBaud')">更新</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 蓝牙节点Tab -->
      <div v-else class="bluetooth-content">
        <el-table :data="bluetoothNodes" style="width: 100%" height="300">
          <el-table-column prop="nodeId" label="蓝牙节点" width="400" />
          <el-table-column prop="isDirect" label="是否直连" width="200">
            <template #default="scope">
              {{ scope.row.isDirect ? '是' : '否' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="controlDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveControl">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设备绑定页：仅改设备号 -->
    <el-dialog
      v-model="bindDeviceDialogVisible"
      title="绑定设备号"
      width="440px"
      :close-on-click-modal="false"
      @closed="onBindDialogClosed"
    >
      <p v-if="bindSourceRow" class="bind-hint">
        当前设备号：<strong>{{ bindSourceRow.deviceId ?? '—' }}</strong>。保存后将由服务器在事务中把原设备号替换为新设备号，并同步各业务表中关联的 <strong>devicelist_id</strong>（若库表上仍有外键限制，可能需先与 DBA 对齐）。
      </p>
      <el-form :model="bindDeviceForm" label-width="100px">
        <el-form-item label="绑定设备号" required>
          <el-input v-model="bindDeviceForm.deviceId" placeholder="请输入要绑定的设备号" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDeviceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindDeviceSaving" @click="handleSaveBindDevice">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设备绑定页：维护档案（不改设备号） -->
    <el-dialog
      v-model="bindMetaDialogVisible"
      title="编辑边缘控制器档案"
      width="520px"
      :close-on-click-modal="false"
      @closed="onBindMetaDialogClosed"
    >
      <el-form :model="bindMetaForm" label-width="112px">
        <el-form-item label="当前设备号">
          <el-input :model-value="String(bindMetaSourceRow?.deviceId ?? '')" disabled />
        </el-form-item>
        <el-form-item label="终端名称">
          <el-input v-model="bindMetaForm.nickName" placeholder="nick_name" clearable />
        </el-form-item>
        <el-form-item label="设备密码">
          <el-input v-model="bindMetaForm.devicePasswordInput" placeholder="数字，可为空" clearable />
        </el-form-item>
        <el-form-item label="设备桩号">
          <el-input v-model="bindMetaForm.deviceNumInput" placeholder="device_num" clearable />
        </el-form-item>
        <el-form-item label="区段">
          <el-input v-model="bindMetaForm.zone" placeholder="zone" clearable />
        </el-form-item>
        <el-form-item label="回路编号">
          <el-input v-model="bindMetaForm.loopNumber" placeholder="loop_number" clearable />
        </el-form-item>
        <el-form-item label="设备状态文本">
          <el-input v-model="bindMetaForm.deviceStatus" placeholder="device_status" clearable />
        </el-form-item>
        <el-form-item label="信号强度">
          <el-input-number v-model="bindMetaForm.csq" :min="0" :max="100" style="width: 100%" controls-position="right" />
        </el-form-item>
        <el-form-item label="网络信息">
          <el-input v-model="bindMetaForm.networkInfo" type="textarea" :rows="2" placeholder="NetworkInfo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindMetaDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindMetaSaving" @click="handleSaveBindMeta">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isAdd ? '新增边缘控制器' : '编辑边缘控制器'" width="500px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="设备号">
          <el-input v-model="editForm.deviceId" :disabled="!isAdd" placeholder="请输入设备号" />
        </el-form-item>
        <el-form-item label="设备密码">
          <el-input v-model="editForm.devicePassword" placeholder="请输入设备密码" />
        </el-form-item>
        <el-form-item label="设备桩号">
          <el-input v-model="editForm.deviceNum" placeholder="请输入设备桩号" />
        </el-form-item>
        <el-form-item label="信号强度">
          <el-input-number v-model="editForm.csq" :min="0" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="editForm.online" placeholder="请选择设备状态" style="width: 100%;">
            <el-option v-for="item in deviceStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="editSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DeviceTableColumnSplit from '@/components/device/DeviceTableColumnSplit.vue'
import { getEdgeControllerList, updateDevicelist, rebindDevicelist } from '@/api/tunnelParam'
import { sanitizeDevicelistForUpdate } from '@/utils/devicelistPayload'
import { addDevicelist } from '@/api/device'
import { getStatusType, getStatusText, formatDisplayTime, getDevicelistDeviceStatusText } from '@/utils/deviceStatus'

// 设备在线状态选项（1=在线, 0=离线, 2=故障）
const deviceStatusOptions = [
  { label: '在线', value: 1 },
  { label: '离线', value: 0 },
  { label: '故障', value: 2 }
]

// 雷达状态选项：0=正常, 1=异常, 2=未安装（独立状态，不使用全局通用状态）
const radarStatusOptions = [
  { label: '正常', value: 0 },
  { label: '异常', value: 1 },
  { label: '未安装', value: 2 }
]

// 亮度仪状态选项：0=正常, 1=异常, 2=未安装（独立状态，不使用全局通用状态）
const brightnessStatusOptions = [
  { label: '正常', value: 0 },
  { label: '异常', value: 1 },
  { label: '未安装', value: 2 }
]

// 获取雷达状态文本（支持字符串和数字类型）
const getRadarStatusText = (status) => {
  // 如果是 null/undefined/空字符串，返回未安装
  if (status === null || status === undefined || status === '') return '未安装'
  // 如果是字符串类型（后端返回）
  if (typeof status === 'string') {
    if (status === '正常') return '正常'
    if (status === '异常') return '异常'
    if (status === '未安装') return '未安装'
    return '未安装'
  }
  // 如果是数字类型
  if (status === 0) return '正常'
  if (status === 1) return '异常'
  if (status === 2) return '未安装'
  return '未安装'
}

// 获取雷达状态标签类型
const getRadarStatusType = (status) => {
  // 如果是 null/undefined/空字符串，返回 info
  if (status === null || status === undefined || status === '') return 'info'
  // 如果是字符串类型（后端返回）
  if (typeof status === 'string') {
    if (status === '正常') return 'success'
    if (status === '异常') return 'danger'
    if (status === '未安装') return 'info'
    return 'info'
  }
  // 如果是数字类型
  if (status === 0) return 'success'
  if (status === 1) return 'danger'
  if (status === 2) return 'info'
  return 'info'
}

// 获取亮度仪状态文本（支持字符串和数字类型）
const getBrightnessStatusText = (status) => {
  // 如果是 null/undefined/空字符串，返回未安装
  if (status === null || status === undefined || status === '') return '未安装'
  // 如果是字符串类型（后端返回）
  if (typeof status === 'string') {
    if (status === '正常') return '正常'
    if (status === '异常') return '异常'
    if (status === '未安装') return '未安装'
    return '未安装'
  }
  // 如果是数字类型
  if (status === 0) return '正常'
  if (status === 1) return '异常'
  if (status === 2) return '未安装'
  return '未安装'
}

// 获取亮度仪状态标签类型
const getBrightnessStatusType = (status) => {
  // 如果是 null/undefined/空字符串，返回 info
  if (status === null || status === undefined || status === '') return 'info'
  // 如果是字符串类型（后端返回）
  if (typeof status === 'string') {
    if (status === '正常') return 'success'
    if (status === '异常') return 'danger'
    if (status === '未安装') return 'info'
    return 'info'
  }
  // 如果是数字类型
  if (status === 0) return 'success'
  if (status === 1) return 'danger'
  if (status === 2) return 'info'
  return 'info'
}

// 将字符串状态转换为数字（用于编辑表单）
const statusStringToNumber = (status) => {
  if (status === '正常') return 0
  if (status === '异常') return 1
  if (status === '未安装') return 2
  if (typeof status === 'number') return status
  return 2 // 默认未安装
}

// 将数字状态转换为字符串（用于保存）
const statusNumberToString = (status) => {
  if (status === 0) return '正常'
  if (status === 1) return '异常'
  if (status === 2) return '未安装'
  return '未安装'
}

const props = defineProps({
  tunnelId: { type: [Number, String], required: true },
  /** 设备绑定工作台：列表字段对齐 tunnel_devicelist；操作含「绑定」改设备号、「编辑」改档案 */
  deviceBindMode: { type: Boolean, default: false },
  /** 查看模式：隐藏操作列与控制入口 */
  readOnly: { type: Boolean, default: false },
  /** false=隧道详情内嵌：隐藏左侧「显示列」并默认展示全部可选列 */
  showColumnPanel: { type: Boolean, default: true }
})

const columnOptions = computed(() => {
  const rows = [{ key: 'deviceId', label: '设备号' }]
  if (!props.deviceBindMode) {
    rows.push({ key: 'devicePassword', label: '设备密码' })
  }
  rows.push(
    { key: 'nickName', label: '终端名称' },
    { key: 'deviceNum', label: '桩号' },
    { key: 'zone', label: '区段' },
    { key: 'loopNumber', label: '回路编号' },
    { key: 'csq', label: '信号强度' },
    { key: 'online', label: '在线状态' },
    { key: 'deviceStatus', label: '设备状态' },
    { key: 'lastUpdate', label: '最后数据' },
    { key: 'radarStatus', label: '雷达' },
    { key: 'brightnessMeterStatus', label: '亮度仪' },
    { key: 'softVer', label: '主板固件' },
    { key: 'networkInfo', label: '网络信息' }
  )
  return rows
})

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
const tableData = ref([])
const controlDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editSaving = ref(false)
const isAdd = ref(false)
const activeTab = ref('params')
const currentDevice = ref({})
const controlForm = ref({
  deviceId: '',
  bluetoothId: '',
  edgeReportInterval: '',
  lampReportInterval: '',
  commTimeout: '',
  syncPeriod: '',
  lampStatusReportTime: '',
  lampTimeoutPeriod: '',
  workReportPeriod: '',
  lampLimitPeriod: '',
  lampList: '',
  lampParams: '',
  bluetoothNode: '',
  brightnessBaudRate: ''
})
const editForm = ref({
  id: null,
  deviceId: '',
  devicePassword: '',
  deviceNum: '',
  csq: 0,
  online: 0
})
const bluetoothNodes = ref([])

const bindDeviceDialogVisible = ref(false)
const bindDeviceSaving = ref(false)
const bindSourceRow = ref(null)
const bindDeviceForm = ref({ deviceId: '' })

const bindMetaDialogVisible = ref(false)
const bindMetaSaving = ref(false)
const bindMetaSourceRow = ref(null)
const bindMetaForm = ref({
  nickName: '',
  devicePasswordInput: '',
  deviceNumInput: '',
  zone: '',
  loopNumber: '',
  deviceStatus: '',
  csq: undefined,
  networkInfo: ''
})

const parseOptionalLong = (s) => {
  if (s == null || String(s).trim() === '') return null
  const n = Number(String(s).trim())
  return Number.isNaN(n) ? null : n
}

const parseOptionalDeviceNumInt = (s) => {
  if (s == null || String(s).trim() === '') return null
  const n = parseInt(String(s).trim(), 10)
  return Number.isNaN(n) ? null : n
}

const loadData = async () => {
  if (!props.tunnelId) return
  loading.value = true
  try {
    const res = await getEdgeControllerList({ tunnelId: props.tunnelId })
    if (res.code === 200) tableData.value = res.data || []
  } catch (error) {
    console.error('获取边缘控制器列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleExport = () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const headers = ['设备号', '设备密码', '设备桩号', '信号强度', '设备状态']
  const data = tableData.value.map(row => [
    row.deviceId || '', row.devicePassword || '', row.deviceNum || '', row.csq || '',
    getStatusText(row.online)
  ])
  const csvContent = [headers.join(','), ...data.map(row => row.join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `边缘控制器_${new Date().toLocaleDateString()}.csv`
  link.click()
  ElMessage.success('导出成功')
}

const handleControl = (row) => {
  currentDevice.value = row
  activeTab.value = 'params'
  controlForm.value = {
    deviceId: row.deviceId || '',
    bluetoothId: '',
    edgeReportInterval: '',
    lampReportInterval: '',
    commTimeout: '',
    syncPeriod: '',
    lampStatusReportTime: '',
    lampTimeoutPeriod: '',
    workReportPeriod: '',
    lampLimitPeriod: '',
    lampList: '',
    lampParams: '',
    bluetoothNode: '',
    brightnessBaudRate: ''
  }
  bluetoothNodes.value = []
  controlDialogVisible.value = true
}

const handleAdd = () => {
  isAdd.value = true
  currentDevice.value = {}
  editForm.value = {
    id: null,
    deviceId: '',
    devicePassword: '',
    deviceNum: '',
    csq: 0,
    online: 0
  }
  editDialogVisible.value = true
}

const onBindDialogClosed = () => {
  bindSourceRow.value = null
  bindDeviceForm.value = { deviceId: '' }
}

const handleBindDevice = (row) => {
  bindSourceRow.value = { ...row }
  bindDeviceForm.value = {
    deviceId: row.deviceId != null && row.deviceId !== '' ? String(row.deviceId) : ''
  }
  bindDeviceDialogVisible.value = true
}

const handleSaveBindDevice = async () => {
  const nextIdStr = (bindDeviceForm.value.deviceId || '').trim()
  if (!nextIdStr) {
    ElMessage.warning('请输入绑定设备号')
    return
  }
  const src = bindSourceRow.value
  if (src == null || src.deviceId == null || src.deviceId === '') {
    ElMessage.error('记录缺少设备号，无法保存')
    return
  }
  const oldDeviceId = Number(src.deviceId)
  const newDeviceId = Number(nextIdStr)
  if (Number.isNaN(oldDeviceId) || Number.isNaN(newDeviceId)) {
    ElMessage.error('设备号必须为数字')
    return
  }
  if (oldDeviceId === newDeviceId) {
    bindDeviceDialogVisible.value = false
    return
  }
  bindDeviceSaving.value = true
  try {
    const res = await rebindDevicelist({ oldDeviceId, newDeviceId })
    if (res.code === 200) {
      ElMessage.success('绑定成功')
      bindDeviceDialogVisible.value = false
      bindSourceRow.value = null
      loadData()
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '绑定失败')
  } finally {
    bindDeviceSaving.value = false
  }
}

const onBindMetaDialogClosed = () => {
  bindMetaSourceRow.value = null
  bindMetaForm.value = {
    nickName: '',
    devicePasswordInput: '',
    deviceNumInput: '',
    zone: '',
    loopNumber: '',
    deviceStatus: '',
    csq: undefined,
    networkInfo: ''
  }
}

const handleEditBindMeta = (row) => {
  bindMetaSourceRow.value = { ...row }
  bindMetaForm.value = {
    nickName: row.nickName || '',
    devicePasswordInput:
      row.devicePassword != null && row.devicePassword !== '' ? String(row.devicePassword) : '',
    deviceNumInput: row.deviceNum != null && row.deviceNum !== '' ? String(row.deviceNum) : '',
    zone: row.zone || '',
    loopNumber: row.loopNumber || '',
    deviceStatus: row.deviceStatus || '',
    csq: row.csq != null && row.csq !== '' ? Number(row.csq) : undefined,
    networkInfo: row.networkInfo || ''
  }
  bindMetaDialogVisible.value = true
}

const handleSaveBindMeta = async () => {
  const src = bindMetaSourceRow.value
  if (src == null || src.deviceId == null || src.deviceId === '') {
    ElMessage.error('缺少设备号')
    return
  }
  const pwd = parseOptionalLong(bindMetaForm.value.devicePasswordInput)
  const dNum = parseOptionalDeviceNumInt(bindMetaForm.value.deviceNumInput)
  bindMetaSaving.value = true
  try {
    const payload = sanitizeDevicelistForUpdate(src, {
      deviceId: Number(src.deviceId),
      deviceTypeId: 1,
      nickName: bindMetaForm.value.nickName || null,
      devicePassword: pwd,
      deviceNum: dNum,
      zone: bindMetaForm.value.zone || null,
      loopNumber: bindMetaForm.value.loopNumber || null,
      deviceStatus: bindMetaForm.value.deviceStatus || null,
      csq: bindMetaForm.value.csq != null && bindMetaForm.value.csq !== '' ? bindMetaForm.value.csq : null,
      networkInfo: bindMetaForm.value.networkInfo || null
    })
    const res = await updateDevicelist(payload)
    if (res.code === 200) {
      ElMessage.success('已保存')
      bindMetaDialogVisible.value = false
      loadData()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    bindMetaSaving.value = false
  }
}

const handleEdit = (row) => {
  isAdd.value = false
  currentDevice.value = row
  editForm.value = {
    id: row.id,
    deviceId: row.deviceId,
    devicePassword: row.devicePassword || '',
    deviceNum: row.deviceNum || '',
    csq: row.csq || 0,
    online: row.online !== undefined && row.online !== null ? Number(row.online) : 0
  }
  editDialogVisible.value = true
}

const handleSaveEdit = async () => {
  if (isAdd.value && !editForm.value.deviceId) {
    ElMessage.warning('请输入设备号')
    return
  }
  editSaving.value = true
  try {
    const submitData = isAdd.value
      ? {
          ...editForm.value,
          tunnelId: props.tunnelId,
          deviceType: 1
        }
      : sanitizeDevicelistForUpdate(
          {
            ...editForm.value,
            deviceTypeId: 1
          },
          {}
        )
    const res = isAdd.value ? await addDevicelist(submitData) : await updateDevicelist(submitData)
    if (res.code === 200) {
      ElMessage.success(isAdd.value ? '新增成功' : '保存成功')
      editDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error(isAdd.value ? '新增失败' : '保存失败')
  } finally {
    editSaving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该边缘控制器？', '警告', { type: 'warning' })
    .then(async () => {
      ElMessage.info('删除功能开发中')
    })
    .catch(() => {})
}

const handleRead = (type) => ElMessage.info(`读取${type}参数功能开发中`)
const handleUpdate = (type) => ElMessage.info(`更新${type}参数功能开发中`)
const handleIssue = (type) => ElMessage.info(`下发${type}功能开发中`)
const handleSaveControl = () => {
  ElMessage.info('保存功能开发中')
  controlDialogVisible.value = false
}

watch(() => props.tunnelId, (newVal) => { if (newVal) loadData() }, { immediate: true })

defineExpose({ loadData, handleExport, handleAdd })
</script>

<style lang="scss" scoped>
.edge-controller-tab {
  width: 100%;
  min-width: 0;
}
.control-tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  .tab-item {
    padding: 8px 20px;
    cursor: pointer;
    border-radius: 4px;
    background: #f5f7fa;
    color: #606266;
    &:hover { background: #e6f0ff; }
    &.active { background: #409eff; color: #fff; }
  }
}
.params-content {
  .param-row {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
    .param-item {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;
      .param-label { flex-shrink: 0; min-width: 180px; text-align: right; color: #606266; font-size: 14px; }
      .param-input { width: 120px; flex-shrink: 0; }
      .param-buttons {
        display: flex;
        gap: 6px;
        .param-btn {
          width: 50px;
          height: 32px;
          font-size: 13px;
          line-height: 32px;
          text-align: center;
          cursor: pointer;
          border-radius: 4px;
          &.read-btn { background: #113f6a; color: #ebebeb; }
          &.update-btn { border: 1px solid #113f6a; color: #113f6a; background: transparent; &:hover { background: #113f6a; color: #ebebeb; } }
          &.issue-btn { background: #113f6a; color: #ebebeb; }
        }
      }
    }
  }
}
.bluetooth-content { min-height: 300px; }
.bind-hint {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
