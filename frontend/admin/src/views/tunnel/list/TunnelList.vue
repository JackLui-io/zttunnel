<template>
  <div class="tunnel-management" :class="{ 'tunnel-sublayout-fill': !!listSubPanel }">
    <div v-show="!listSubPanel" class="tunnel-list-main">
      <div class="card">
        <div class="table-container">
          <el-table
            ref="tableRef"
            :data="rawData"
            style="width: 100%"
            v-loading="loading"
            row-key="id"
            :row-class-name="tunnelTableRowClassName"
            :default-expand-all="allExpanded"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column prop="tunnelName" label="隧道名称" width="300">
              <template #default="scope">
                <span
                  :style="{
                    paddingLeft: (scope.row.level - 1) * 20 + 'px',
                    cursor: scope.row.children && scope.row.children.length > 0 ? 'pointer' : 'default'
                  }"
                  @click="toggleRowExpansion(scope.row)"
                >
                  {{ scope.row.tunnelName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="层级">
              <template #default="scope">
                <el-tag :type="getLevelType(scope.row.level)">
                  {{ getLevelText(scope.row.level) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="tunnelMileage" label="里程(km)">
              <template #default="scope">
                {{ scope.row.tunnelMileage || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                  {{ scope.row.status === 0 ? '有效' : '失效' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
            <el-table-column label="操作" width="460" fixed="right">
              <template #default="scope">
                <el-space :size="8" wrap class="tunnel-list-ops">
                  <template v-if="isAddChildLevel(scope.row) && canTunnelUpdate">
                    <el-button type="success" size="small" plain @click="openAddChild(scope.row)">
                      {{ addChildButtonText(scope.row) }}
                    </el-button>
                  </template>
                  <template v-if="scope.row.level === 3 && canTunnelUpdate">
                    <el-button type="primary" size="small" @click="openEditTunnelGroupDialog(scope.row)">
                      编辑
                    </el-button>
                    <el-button type="primary" size="small" @click="openCopyTunnelGroupDialog(scope.row)">
                      复制
                    </el-button>
                  </template>
                  <template v-if="scope.row.level === 4">
                    <el-button type="primary" size="small" plain @click="goTunnelView(scope.row)">
                      查看
                    </el-button>
                    <el-button type="primary" size="small" plain @click="goTunnelEdit(scope.row)">
                      编辑
                    </el-button>
                    <el-button
                      v-if="isPlaceholderTunnelRow(scope.row)"
                      type="warning"
                      size="small"
                      plain
                      @click="goDeviceBind(scope.row)"
                    >
                      绑定
                    </el-button>
                  </template>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <el-dialog
        v-model="copyTunnelGroupDialogVisible"
        title="复制隧道群"
        width="440px"
        destroy-on-close
        @closed="resetCopyTunnelGroupForm"
      >
        <el-form label-width="100px" @submit.prevent>
          <el-form-item label="源隧道群">
            <el-input :model-value="copySourceTunnelGroup?.tunnelName || ''" disabled />
          </el-form-item>
          <el-form-item label="新隧道名称" required>
            <el-input
              v-model="copyNewTunnelName"
              placeholder="复制后的 level-3 隧道群名称"
              maxlength="128"
              show-word-limit
              clearable
            />
          </el-form-item>
          <p class="copy-tunnel-hint">
            将复制该隧道群下的左右线（level-4）及关联设备占位数据；新名称在同级（同一条高速）下需唯一。
          </p>
        </el-form>
        <template #footer>
          <el-button @click="copyTunnelGroupDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="copyTunnelGroupSubmitting" @click="submitCopyTunnelGroup">
            确定复制
          </el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="editTunnelGroupDialogVisible"
        title="编辑隧道群"
        width="440px"
        destroy-on-close
        @closed="resetEditTunnelGroupForm"
      >
        <el-form label-width="100px" @submit.prevent>
          <el-form-item label="隧道群名称" required>
            <el-input
              v-model="editTunnelGroupName"
              placeholder="请输入隧道群名称"
              maxlength="128"
              show-word-limit
              clearable
            />
          </el-form-item>
          <el-form-item label="里程(km)">
            <el-input-number
              v-model="editTunnelGroupMileage"
              :precision="2"
              :step="0.1"
              :min="0"
              style="width: 100%"
              controls-position="right"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="editTunnelGroupStatus">
              <el-radio :label="0">有效</el-radio>
              <el-radio :label="1">失效</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editTunnelGroupDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editTunnelGroupSubmitting" @click="submitEditTunnelGroup">
            保存
          </el-button>
        </template>
      </el-dialog>

    </div>

    <div v-if="listSubPanel" class="list-subview-toolbar">
      <el-button type="primary" size="small" plain @click="closeListSubview">返回隧道列表</el-button>
      <span class="list-subview-title">{{ subviewTitle }}</span>
    </div>

    <DeviceBindWorkspace
      v-if="listSubPanel === 'device-bind'"
      embedded
      :tunnel-id-prop="deviceBindTunnelId"
      :tunnel-banner-name="workspaceTunnelBanner"
      :read-only="false"
      :edge-device-bind-mode="true"
      default-active-tab="edge"
      @close="closeListSubview"
    />

    <DeviceBindWorkspace
      v-if="listSubPanel === 'tunnel-detail'"
      embedded
      :tunnel-id-prop="subTunnelId"
      :tunnel-banner-name="workspaceTunnelBanner"
      :scroll-param-top-key="tunnelDetailOpenKey"
      :read-only="tunnelDetailIsView"
      :edge-device-bind-mode="false"
      default-active-tab="param"
      @close="closeListSubview"
    />

    <div v-show="listSubPanel === 'edit'" class="card list-subview-card">
      <el-form
        ref="tunnelFormEmbedRef"
        :model="tunnelForm"
        :rules="tunnelRules"
        label-width="100px"
        class="tunnel-edit-embed-form"
      >
        <el-form-item :label="tunnelFormNameLabel" prop="tunnelName">
          <el-input v-model="tunnelForm.tunnelName" :placeholder="tunnelFormNamePlaceholder" />
        </el-form-item>
        <el-form-item :label="parentTunnelFormLabel" prop="parentId" v-if="tunnelForm.parentId">
          <el-select v-model="tunnelForm.parentId" placeholder="请选择父级隧道" style="width: 100%" disabled>
            <el-option
              v-for="tunnel in parentOptions"
              :key="tunnel.id"
              :label="tunnel.tunnelName"
              :value="tunnel.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="层级" prop="level">
          <el-select v-model="tunnelForm.level" placeholder="请选择层级" style="width: 100%" disabled>
            <el-option label="管理单位" :value="1" />
            <el-option label="高速公路" :value="2" />
            <el-option label="隧道群" :value="3" />
            <el-option label="具体隧道" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="里程(km)" prop="tunnelMileage" v-if="tunnelForm.level >= 3">
          <el-input-number
            v-model="tunnelForm.tunnelMileage"
            :precision="2"
            :step="0.1"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="tunnelForm.status">
            <el-radio :label="0">有效</el-radio>
            <el-radio :label="1">失效</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div class="list-subview-footer">
        <el-button @click="closeListSubview">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAllTunnelTree,
  getPlaceholderTunnelIds,
  addTunnel,
  updateTunnel,
  copyTunnelGroup
} from '@/api/tunnel'
import { hasPermission } from '@/utils/permission'
import {
  setDeviceBindContextTunnelId,
  clearDeviceBindContextTunnelId,
  peekDeviceBindContextTunnelId,
  peekDeviceBindTunnelName,
  peekDeviceBindTunnelMeta
} from '@/utils/deviceBindContext'
import {
  setTunnelDetailWorkspace,
  clearTunnelDetailWorkspace,
  peekTunnelDetailWorkspace
} from '@/utils/tunnelDetailContext'
import { buildTunnelWorkspaceMeta } from '@/utils/tunnelTree'
import DeviceBindWorkspace from '@/views/tunnel/bind/DeviceBindWorkspace.vue'

const route = useRoute()
const router = useRouter()

const listSubPanel = computed(() => (route.query.panel || '').toString())

const tunnelDetailContext = ref(null)
const tunnelDetailOpenKey = ref(0)
const deviceBindBannerName = ref('')

const subTunnelId = computed(() => {
  if (listSubPanel.value !== 'tunnel-detail') return null
  const id = tunnelDetailContext.value?.id
  return id != null && !Number.isNaN(Number(id)) ? Number(id) : null
})

const workspaceTunnelBanner = computed(() => {
  if (listSubPanel.value === 'tunnel-detail') {
    const w = tunnelDetailContext.value
    if (w?.namePath) return w.namePath
    return w?.name || ''
  }
  if (listSubPanel.value === 'device-bind') {
    const m = peekDeviceBindTunnelMeta()
    if (m.pathFromRoot) return m.pathFromRoot
    return deviceBindBannerName.value || peekDeviceBindTunnelName()
  }
  return ''
})

const tunnelDetailIsView = computed(() => (route.query.mode || '').toString() === 'view')

const subviewTitle = computed(() => {
  if (listSubPanel.value === 'tunnel-detail') {
    return tunnelDetailIsView.value ? '查看隧道' : '编辑隧道'
  }
  if (listSubPanel.value === 'edit') {
    if (tunnelForm.id) return '编辑隧道'
    // 与列表按钮一致：仅 L1/L2 可打开新增子级嵌入页
    if (tunnelForm.level === 2) return '添加高速'
    if (tunnelForm.level === 3) return '添加隧道'
    return '新增子级'
  }
  const m = { 'device-bind': '设备绑定' }
  return m[listSubPanel.value] || ''
})

/** 内嵌表单：名称字段标签/占位（L1→添加高速 对应 level=2） */
const tunnelFormNameLabel = computed(() => {
  const lv = tunnelForm.level
  if (lv === 2) return '高速公路名称'
  if (lv === 3) return '隧道群名称'
  if (lv === 4) return '隧道名称'
  return '隧道名称'
})

const tunnelFormNamePlaceholder = computed(() => {
  const lv = tunnelForm.level
  if (lv === 2) return '请输入高速公路名称'
  if (lv === 3) return '请输入隧道群名称'
  if (lv === 4) return '请输入隧道名称'
  return '请输入隧道名称'
})

const parentTunnelFormLabel = computed(() => {
  const lv = tunnelForm.level
  if (lv === 2) return '所属管理单位'
  if (lv === 3) return '所属高速公路'
  if (lv === 4) return '所属隧道群'
  return '父级隧道'
})

const deviceBindTunnelId = computed(() => {
  if (listSubPanel.value !== 'device-bind') return null
  return peekDeviceBindContextTunnelId()
})

const loading = ref(false)
const tunnelFormEmbedRef = ref()
const tableRef = ref()
const allExpanded = ref(true)

const rawData = ref([])
/** 四级隧道 id（字符串），与 GET /tunnel/bind/placeholder-tunnel-ids 对齐，避免树节点 id 与接口 number/string 不一致导致无法匹配 */
const placeholderTunnelIdSet = ref(new Set())

const canTunnelUpdate = computed(() => hasPermission('system:tunnel:update'))

/** L1→高速公路(L2)；L2→隧道群(L3)。L3 不再提供列表「新增子级」入口（具体隧道由其它流程维护） */
const isAddChildLevel = (row) => row && [1, 2].includes(row.level)

const addChildButtonText = (row) => {
  if (row.level === 1) return '添加高速'
  return '添加隧道'
}

const isPlaceholderTunnelRow = (row) =>
  row?.level === 4 && row.id != null && placeholderTunnelIdSet.value.has(String(row.id))

/** 路由带入或复制成功后的短暂行高亮（含居中滚动） */
const flashHighlightId = ref(null)
let flashHighlightClearTimer = null

const copyTunnelGroupDialogVisible = ref(false)
const copyTunnelGroupSubmitting = ref(false)
const copySourceTunnelGroup = ref(null)
const copyNewTunnelName = ref('')

/** L3 弹窗：名称 / 里程 / 状态；与 L4 工作台编辑无关 */
const editTunnelGroupDialogVisible = ref(false)
const editTunnelGroupSubmitting = ref(false)
const editTunnelGroupRow = ref(null)
const editTunnelGroupName = ref('')
const editTunnelGroupMileage = ref(null)
const editTunnelGroupStatus = ref(0)

const openCopyTunnelGroupDialog = (row) => {
  copySourceTunnelGroup.value = row
  const base = (row?.tunnelName || '').trim()
  copyNewTunnelName.value = base ? `${base}复制` : '复制'
  copyTunnelGroupDialogVisible.value = true
}

const resetCopyTunnelGroupForm = () => {
  copySourceTunnelGroup.value = null
  copyNewTunnelName.value = ''
}

const openEditTunnelGroupDialog = (row) => {
  if (!row?.id || row.level !== 3) return
  editTunnelGroupRow.value = row
  editTunnelGroupName.value = (row.tunnelName || '').trim()
  const m = row.tunnelMileage
  editTunnelGroupMileage.value = m === null || m === undefined || m === '' ? null : Number(m)
  editTunnelGroupStatus.value = row.status != null ? row.status : 0
  editTunnelGroupDialogVisible.value = true
}

const resetEditTunnelGroupForm = () => {
  editTunnelGroupRow.value = null
  editTunnelGroupName.value = ''
  editTunnelGroupMileage.value = null
  editTunnelGroupStatus.value = 0
}

const submitEditTunnelGroup = async () => {
  const name = (editTunnelGroupName.value || '').trim()
  if (!name) {
    ElMessage.warning('请输入隧道群名称')
    return
  }
  const row = editTunnelGroupRow.value
  if (!row?.id) {
    ElMessage.warning('隧道群数据无效')
    return
  }
  const origStatus = row.status != null ? row.status : 0
  const newStatus = editTunnelGroupStatus.value
  if (origStatus !== newStatus && row.children?.length) {
    const statusText = newStatus === 0 ? '有效' : '失效'
    const confirmed = await ElMessageBox.confirm(
      `修改状态为「${statusText}」后，该隧道群下的所有子级状态也将同步变为「${statusText}」，是否继续？`,
      '状态变更提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    ).catch(() => false)
    if (!confirmed) return
  }
  editTunnelGroupSubmitting.value = true
  try {
    const res = await updateTunnel({
      id: row.id,
      tunnelName: name,
      parentId: row.parentId,
      level: row.level,
      tunnelMileage: editTunnelGroupMileage.value,
      status: newStatus
    })
    if (res.code === 200) {
      ElMessage.success(res.msg || '保存成功')
      editTunnelGroupDialogVisible.value = false
      await getTunnelList()
    }
  } catch (e) {
    console.error(e)
  } finally {
    editTunnelGroupSubmitting.value = false
  }
}

const submitCopyTunnelGroup = async () => {
  const name = (copyNewTunnelName.value || '').trim()
  if (!name) {
    ElMessage.warning('请输入新隧道名称')
    return
  }
  const src = copySourceTunnelGroup.value
  if (!src?.id) {
    ElMessage.warning('源隧道群无效')
    return
  }
  copyTunnelGroupSubmitting.value = true
  try {
    const res = await copyTunnelGroup({
      sourceTunnelGroupId: src.id,
      newTunnelName: name
    })
    if (res.code === 200) {
      const vo = res.data || {}
      const newGroupId = vo.newTunnelGroupId
      ElMessage.success({
        message: `${res.msg || '复制成功'}。请展开新隧道群并为左右线完善参数或绑定设备`,
        duration: 6500
      })
      copyTunnelGroupDialogVisible.value = false
      await getTunnelList()
      if (newGroupId != null) {
        await focusTreeRowById(newGroupId)
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    copyTunnelGroupSubmitting.value = false
  }
}

const openAddChild = (row) => {
  if (!row?.id || !isAddChildLevel(row)) return
  router.push({
    path: '/tunnel/list',
    query: { panel: 'edit', parentId: String(row.id) }
  })
}

const findPathToRow = (nodes, targetIdStr, path = []) => {
  if (!nodes?.length) return null
  for (const n of nodes) {
    const nextPath = [...path, n]
    if (String(n.id) === targetIdStr) return nextPath
    if (n.children?.length) {
      const hit = findPathToRow(n.children, targetIdStr, nextPath)
      if (hit) return hit
    }
  }
  return null
}

const scrollTableRowIntoView = (rowKeyStr) => {
  const wrap = tableRef.value?.$el?.querySelector?.('.el-table__body-wrapper')
  if (!wrap) return
  const rowEl = wrap.querySelector(`tr[data-row-key="${rowKeyStr}"]`)
  rowEl?.scrollIntoView({ block: 'center', behavior: 'smooth' })
}

const focusTreeRowById = async (rawId) => {
  const sid = String(rawId)
  if (!rawData.value?.length || !tableRef.value) return
  const path = findPathToRow(rawData.value, sid)
  if (!path?.length) return
  await nextTick()
  for (const node of path) {
    if (node.children?.length) {
      tableRef.value.toggleRowExpansion(node, true)
    }
  }
  await nextTick()
  scrollTableRowIntoView(sid)
  if (flashHighlightClearTimer) {
    clearTimeout(flashHighlightClearTimer)
    flashHighlightClearTimer = null
  }
  flashHighlightId.value = sid
  flashHighlightClearTimer = setTimeout(() => {
    flashHighlightId.value = null
    flashHighlightClearTimer = null
  }, 4500)
}

const tunnelTableRowClassName = ({ row }) => {
  if (flashHighlightId.value != null && String(row.id) === String(flashHighlightId.value)) {
    return 'tunnel-list-row--flash'
  }
  return ''
}

const toggleRowExpansion = (row) => {
  if (row.children && row.children.length > 0) tableRef.value?.toggleRowExpansion(row)
}

const expandAllTreeRows = () => {
  if (!allExpanded.value || !tableRef.value) return
  const walk = (rows) => {
    if (!rows?.length) return
    rows.forEach((row) => {
      if (row.children?.length) {
        tableRef.value.toggleRowExpansion(row, true)
        walk(row.children)
      }
    })
  }
  walk(rawData.value)
}

const parentOptions = ref([])

const tunnelForm = reactive({
  id: null,
  tunnelName: '',
  parentId: null,
  level: 1,
  tunnelMileage: null,
  status: 0
})

const tunnelRules = computed(() => ({
  tunnelName: [
    {
      required: true,
      message:
        tunnelForm.level === 2
          ? '请输入高速公路名称'
          : tunnelForm.level === 3
            ? '请输入隧道群名称'
            : '请输入隧道名称',
      trigger: 'blur'
    }
  ],
  level: [{ required: true, message: '请选择层级', trigger: 'change' }]
}))

const getLevelType = (level) => {
  const types = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return types[level] || 'info'
}

const getLevelText = (level) => {
  const texts = { 1: '管理单位', 2: '高速公路', 3: '隧道群', 4: '具体隧道' }
  return texts[level] || '未知'
}

const findTunnelNameInTree = (nodes, id) => {
  if (!nodes?.length) return ''
  const sid = String(id)
  for (const n of nodes) {
    if (String(n.id) === sid) return n.tunnelName || ''
    const hit = findTunnelNameInTree(n.children, id)
    if (hit) return hit
  }
  return ''
}

const findNodeById = (nodes, id) => {
  const sid = String(id)
  for (const node of nodes) {
    if (String(node.id) === sid) return node
    if (node.children?.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

const closeListSubview = () => {
  clearTunnelDetailWorkspace()
  clearDeviceBindContextTunnelId()
  tunnelDetailContext.value = null
  deviceBindBannerName.value = ''
  router.replace({ path: '/tunnel/list', query: {} })
}

const syncTunnelDetailContextFromSession = () => {
  const w = peekTunnelDetailWorkspace()
  if (!w) {
    tunnelDetailContext.value = null
    return
  }
  tunnelDetailContext.value = {
    id: w.tunnelId,
    name: w.tunnelName,
    namePath: w.pathFromRoot || w.tunnelName || ''
  }
}

const goTunnelView = (row) => {
  const meta = buildTunnelWorkspaceMeta(rawData.value, row)
  setTunnelDetailWorkspace(row.id, row.tunnelName, meta)
  tunnelDetailOpenKey.value += 1
  syncTunnelDetailContextFromSession()
  router.push({ path: '/tunnel/list', query: { panel: 'tunnel-detail', mode: 'view' } })
}

const goTunnelEdit = (row) => {
  const meta = buildTunnelWorkspaceMeta(rawData.value, row)
  setTunnelDetailWorkspace(row.id, row.tunnelName, meta)
  tunnelDetailOpenKey.value += 1
  syncTunnelDetailContextFromSession()
  router.push({ path: '/tunnel/list', query: { panel: 'tunnel-detail', mode: 'edit' } })
}

const goDeviceBind = (row) => {
  const meta = buildTunnelWorkspaceMeta(rawData.value, row)
  setDeviceBindContextTunnelId(row.id, row.tunnelName, meta)
  deviceBindBannerName.value = meta.pathFromRoot || row.tunnelName || ''
  router.push({ path: '/tunnel/list', query: { panel: 'device-bind' } })
}

watch(
  () => listSubPanel.value,
  (p, oldP) => {
    if (oldP === 'device-bind' && p !== 'device-bind') {
      clearDeviceBindContextTunnelId()
      deviceBindBannerName.value = ''
    }
    if (oldP === 'tunnel-detail' && p !== 'tunnel-detail') {
      clearTunnelDetailWorkspace()
      tunnelDetailContext.value = null
    }
    if (p === 'tunnel-detail') syncTunnelDetailContextFromSession()
    if (p === 'device-bind') {
      deviceBindBannerName.value = peekDeviceBindTunnelName() || deviceBindBannerName.value
    }
  },
  { immediate: true }
)

watch(
  () => [route.query.panel, route.query.tunnelId, route.query.mode, rawData.value],
  () => {
    if ((route.query.panel || '').toString() !== 'tunnel-detail') return
    const q = route.query.tunnelId
    if (q == null || q === '') return
    const id = Number(q)
    if (Number.isNaN(id)) return
    const name = findTunnelNameInTree(rawData.value, id) || ''
    const row = findNodeById(rawData.value, id)
    const meta = row ? buildTunnelWorkspaceMeta(rawData.value, row) : { pathFromRoot: '', level: 4 }
    setTunnelDetailWorkspace(id, name, { ...meta, pathFromRoot: meta.pathFromRoot || name })
    syncTunnelDetailContextFromSession()
    router.replace({
      path: '/tunnel/list',
      query: { panel: 'tunnel-detail', mode: (route.query.mode || 'edit').toString() }
    })
  }
)

watch(
  () => [route.query.panel, route.query.editId, route.query.parentId, rawData.value.length],
  () => {
    if (route.query.panel !== 'edit') return

    const editIdQ = route.query.editId
    if (editIdQ != null && editIdQ !== '') {
      const id = Number(editIdQ)
      if (Number.isNaN(id)) return
      const node = findNodeById(rawData.value, id)
      if (!node) return
      parentOptions.value = node.parentId
        ? (() => {
            const p = findNodeById(rawData.value, node.parentId)
            return p ? [p] : []
          })()
        : []
      Object.assign(tunnelForm, {
        id: node.id,
        tunnelName: node.tunnelName,
        parentId: node.parentId,
        level: node.level,
        tunnelMileage: node.tunnelMileage,
        status: node.status
      })
      return
    }

    const pidQ = route.query.parentId
    if (pidQ == null || pidQ === '') return
    const pid = Number(pidQ)
    if (Number.isNaN(pid)) return
    const parent = findNodeById(rawData.value, pid)
    if (!parent) return
    if (![1, 2].includes(parent.level)) {
      router.replace({ path: '/tunnel/list', query: {} })
      return
    }
    parentOptions.value = [parent]
    Object.assign(tunnelForm, {
      id: null,
      tunnelName: '',
      parentId: parent.id,
      level: parent.level + 1,
      tunnelMileage: null,
      status: 0
    })
  },
  { immediate: true }
)

watch(
  () => [
    route.query.focusCompanyId,
    route.query.focusTunnelId,
    rawData.value,
    loading.value
  ],
  async () => {
    const fid = route.query.focusCompanyId ?? route.query.focusTunnelId
    if (fid == null || fid === '' || loading.value) return
    if (!rawData.value?.length) return
    await focusTreeRowById(fid)
    const q = { ...route.query }
    delete q.focusCompanyId
    delete q.focusTunnelId
    const keys = Object.keys(q).filter((k) => q[k] != null && q[k] !== '')
    if (keys.length === 0) {
      await router.replace({ path: '/tunnel/list' })
    } else {
      const nextQ = {}
      keys.forEach((k) => {
        nextQ[k] = q[k]
      })
      await router.replace({ path: '/tunnel/list', query: nextQ })
    }
  }
)

const getTunnelList = async () => {
  loading.value = true
  try {
    const [treeRes, phRes] = await Promise.all([getAllTunnelTree(), getPlaceholderTunnelIds()])
    if (treeRes.code === 200 && treeRes.data) rawData.value = treeRes.data
    if (phRes.code === 200 && Array.isArray(phRes.data)) {
      placeholderTunnelIdSet.value = new Set(
        phRes.data.filter((x) => x != null && x !== '').map((x) => String(x))
      )
    } else {
      placeholderTunnelIdSet.value = new Set()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取隧道列表失败')
  } finally {
    loading.value = false
    await nextTick()
    expandAllTreeRows()
  }
}

const handleSubmit = async () => {
  const formEl = tunnelFormEmbedRef.value
  try {
    await formEl?.validate()
    const submitData = {
      ...tunnelForm,
      parentId: tunnelForm.parentId || 0,
      level: tunnelForm.level,
      tunnelMileage: tunnelForm.tunnelMileage,
      status: tunnelForm.status
    }

    if (tunnelForm.id) {
      const originalRow =
        rawData.value.find((item) => item.id === tunnelForm.id) ||
        findNodeById(rawData.value, tunnelForm.id)
      if (originalRow && originalRow.status !== tunnelForm.status && originalRow.children?.length) {
        const statusText = tunnelForm.status === 0 ? '有效' : '失效'
        const confirmed = await ElMessageBox.confirm(
          `修改状态为「${statusText}」后，该隧道下的所有子级状态也将同步变为「${statusText}」，是否继续？`,
          '状态变更提示',
          { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        ).catch(() => false)
        if (!confirmed) return
      }
    }

    let res
    if (tunnelForm.id) {
      submitData.id = tunnelForm.id
      res = await updateTunnel(submitData)
    } else {
      res = await addTunnel(submitData)
    }

    if (res.code === 200) {
      ElMessage.success(tunnelForm.id ? '保存成功' : '新增成功')
      if (route.query.panel === 'edit') {
        await router.replace({ path: '/tunnel/list', query: {} })
      }
      await getTunnelList()
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => getTunnelList())

onBeforeUnmount(() => {
  if (flashHighlightClearTimer) {
    clearTimeout(flashHighlightClearTimer)
    flashHighlightClearTimer = null
  }
})
</script>

<style lang="scss" scoped>
.tunnel-management {
  /* 嵌入查看/编辑/绑定时：占满主内容区高度，禁止整页滚动，仅卡片/Tab 内滚动 */
  &.tunnel-sublayout-fill {
    height: calc(100vh - 60px - 40px);
    max-height: calc(100vh - 60px - 40px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-sizing: border-box;

    .list-subview-toolbar {
      flex-shrink: 0;
      margin-bottom: 12px;
    }

    :deep(.device-bind-workspace--fill-height) {
      flex: 1;
      min-height: 0;
      height: auto !important;
      max-height: none;
    }

    .list-subview-card {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      margin-bottom: 0;

      .tunnel-edit-embed-form {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        padding-right: 4px;
      }

      .list-subview-footer {
        flex-shrink: 0;
      }
    }
  }

  .list-subview-toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
  }

  .list-subview-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .list-subview-card {
    margin-bottom: 16px;
    padding: 20px;
  }

  .list-subview-footer {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    display: flex;
    gap: 12px;
  }

  .tunnel-edit-embed-form {
    max-width: 640px;
  }

  .table-container {
    :deep(.el-table__body-wrapper) {
      .el-table__row .el-table__cell .cell span {
        transition: color 0.2s ease;
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .copy-tunnel-hint {
    margin: 0 0 0 100px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.55;
    max-width: 360px;
  }

  :deep(tr.tunnel-list-row--flash > td.el-table__cell) {
    background-color: var(--el-color-primary-light-9) !important;
    transition: background-color 0.25s ease;
  }
}
</style>
