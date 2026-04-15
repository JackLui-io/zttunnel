<template>
  <div class="tunnel-template-page tunnel-template-detail">
    <div class="card">
      <div class="page-toolbar">
        <el-button @click="goBack">返回模板列表</el-button>
        <span v-if="head?.templateName" class="page-toolbar__title">查看模板：{{ head.templateName }}</span>
        <div class="page-toolbar__actions">
          <el-button v-if="head && canTunnelUpdate" type="primary" @click="goEdit">进入编辑</el-button>
        </div>
      </div>

      <el-skeleton v-if="pageLoading" :rows="6" animated />
      <template v-else-if="loadError">
        <el-empty :description="loadError" />
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </template>
      <template v-else>
        <el-alert type="info" :closable="false" show-icon class="tunnel-banner-alert">
          <template #title>当前模板：{{ head?.templateName || '—' }}</template>
        </el-alert>
        <p class="page-sub">
          与「隧道列表」中查看隧道一致：按页签浏览该方向下的隧道参数与各设备快照（只读）。修改请使用「进入编辑」。
        </p>

        <el-descriptions :column="2" border size="small" class="head-desc">
          <el-descriptions-item label="模板名称">{{ head?.templateName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(head?.status)" size="small">{{ statusText(head?.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(head?.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ head?.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="directions-title">方向一览</div>
        <el-table :data="directions" border size="small" class="dir-summary-table" max-height="240">
          <el-table-column label="顺序" prop="sortOrder" width="64" />
          <el-table-column label="方向" width="88">
            <template #default="{ row }">{{ directionText(row.direction) }}</template>
          </el-table-column>
          <el-table-column label="线路名称" prop="lineDisplayName" min-width="120" show-overflow-tooltip />
          <el-table-column label="里程(km)" width="100">
            <template #default="{ row }">{{ row.lineTunnelMileage ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="设备数" width="88">
            <template #default="{ row }">{{ devCountFromPayloadJson(row.payloadJson) }}</template>
          </el-table-column>
        </el-table>

        <div v-if="directions.length" class="direction-tabs-wrap">
          <el-tabs v-model="activeDirectionKey" type="border-card">
            <el-tab-pane
              v-for="d in directions"
              :key="d.id"
              :name="String(d.id)"
              :label="`${directionText(d.direction)} · ${d.lineDisplayName || '未命名'}`"
            >
              <div class="direction-inner">
                <TemplateDirectionDetailTabs v-if="payloadByDirId[d.id]" :payload="payloadByDirId[d.id]" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTunnelParamTemplateDetail } from '@/api/tunnel'
import { formatDateTime } from '@/utils/datetime'
import { hasPermission } from '@/utils/permission'
import {
  peekTemplateParamContextId,
  setTemplateParamContextId,
  clearTemplateParamContextId
} from '@/utils/templateParamContext'
import { parseTemplatePayloadJson } from '@/utils/templatePayload'
import TemplateDirectionDetailTabs from '@/components/template/TemplateDirectionDetailTabs.vue'

const router = useRouter()

/** @type {import('vue').Ref<number|null>} */
const templateId = ref(peekTemplateParamContextId())

const canTunnelUpdate = computed(() => hasPermission('system:tunnel:update'))

const pageLoading = ref(true)
const loadError = ref('')
const detailVo = ref(null)
const payloadByDirId = ref({})
const activeDirectionKey = ref('')

const head = computed(() => detailVo.value?.template || null)
const directions = computed(() => detailVo.value?.directions || [])

const statusText = (s) => {
  if (s === 0) return '草稿'
  if (s === 1) return '启用'
  if (s === 2) return '停用'
  return '—'
}

const statusTagType = (s) => {
  if (s === 1) return 'success'
  if (s === 0) return 'info'
  if (s === 2) return 'danger'
  return 'info'
}

const directionText = (d) => {
  if (d === 1) return '右线'
  if (d === 2) return '左线'
  return d != null ? String(d) : '—'
}

const devCountFromPayloadJson = (json) => {
  try {
    const o = JSON.parse(String(json || '{}'))
    const n = (o.devicelists || []).length
    return Number.isFinite(n) ? n : '—'
  } catch {
    return '—'
  }
}

const rebuildPayloadMap = (vo) => {
  const map = {}
  const dirs = vo?.directions
  if (Array.isArray(dirs)) {
    for (const d of dirs) {
      if (d?.id != null) {
        map[d.id] = parseTemplatePayloadJson(d.payloadJson)
      }
    }
  }
  payloadByDirId.value = map
  const first = dirs?.[0]
  activeDirectionKey.value = first?.id != null ? String(first.id) : ''
}

const loadDetail = async () => {
  const id = templateId.value
  if (id == null) {
    loadError.value = '请从「模板列表」进入详情'
    pageLoading.value = false
    return
  }
  pageLoading.value = true
  loadError.value = ''
  try {
    const res = await getTunnelParamTemplateDetail(id)
    if (res.code !== 200 || !res.data?.template) {
      loadError.value = '模板不存在或已删除'
      detailVo.value = null
      payloadByDirId.value = {}
      return
    }
    const tid = res.data.template?.id
    if (tid != null) {
      setTemplateParamContextId(tid)
      templateId.value = Number(tid)
    }
    detailVo.value = res.data
    rebuildPayloadMap(res.data)
  } catch (e) {
    console.error(e)
    loadError.value = '加载失败'
    detailVo.value = null
  } finally {
    pageLoading.value = false
  }
}

const goBack = () => {
  clearTemplateParamContextId()
  router.push('/tunnel/param')
}

const goEdit = () => {
  if (templateId.value == null) return
  setTemplateParamContextId(templateId.value)
  router.push('/tunnel/param/edit')
}

onMounted(() => {
  loadDetail()
})
</script>
