<template>
  <div class="tunnel-template-page tunnel-template-edit">
    <div class="card">
      <div class="page-toolbar">
        <el-button @click="goBack">返回模板列表</el-button>
        <span v-if="headForm.templateName" class="page-toolbar__title">编辑模板：{{ headForm.templateName }}</span>
        <div class="page-toolbar__actions">
          <el-button @click="goDetail">查看详情</el-button>
        </div>
      </div>

      <el-skeleton v-if="pageLoading" :rows="8" animated />
      <template v-else-if="loadError">
        <el-empty :description="loadError" />
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </template>
      <template v-else>
        <el-alert type="info" :closable="false" show-icon class="tunnel-banner-alert">
          <template #title>当前模板：{{ headForm.templateName || '—' }}</template>
        </el-alert>
        <p class="page-sub">
          修改基本信息与各方向元数据、快照后请分别保存。
        </p>
        <el-alert
          v-if="!canTunnelUpdate"
          type="warning"
          :closable="false"
          show-icon
          class="read-only-alert"
          title="当前账号无「system:tunnel:update」权限，表单只读；修改模板请使用具备该权限的账号。"
        />
        <el-card class="template-section-card" shadow="never">
          <template #header>
            <span class="template-section-card__title">基本信息</span>
          </template>
          <el-form label-width="120px" class="head-form" @submit.prevent>
            <el-form-item label="模板名称" required>
              <el-input
                v-model="headForm.templateName"
                maxlength="128"
                show-word-limit
                clearable
                :disabled="!canTunnelUpdate"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="headForm.status" :disabled="!canTunnelUpdate">
                <el-radio :label="0">草稿</el-radio>
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="2">停用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model="headForm.remark"
                type="textarea"
                :rows="2"
                maxlength="512"
                show-word-limit
                :disabled="!canTunnelUpdate"
              />
            </el-form-item>
            <el-form-item v-if="canTunnelUpdate">
              <el-button type="primary" :loading="headSaving" @click="saveHead">保存基本信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card
          v-for="dir in directionForms"
          :key="dir.id"
          class="template-section-card direction-card"
          shadow="never"
        >
          <template #header>
            <span class="template-section-card__title">
              方向 · {{ directionText(dir.direction) }} · {{ dir.lineDisplayName || '未命名' }} · id {{ dir.id }}
            </span>
          </template>
          <el-form label-width="120px" @submit.prevent>
            <el-form-item label="方向">
              <el-radio-group v-model="dir.direction" :disabled="!canTunnelUpdate">
                <el-radio :label="1">右线</el-radio>
                <el-radio :label="2">左线</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number
                v-model="dir.sortOrder"
                :min="0"
                :max="999"
                controls-position="right"
                :disabled="!canTunnelUpdate"
              />
            </el-form-item>
            <el-form-item label="线路名称">
              <el-input
                v-model="dir.lineDisplayName"
                maxlength="128"
                show-word-limit
                clearable
                :disabled="!canTunnelUpdate"
              />
            </el-form-item>
            <el-form-item label="里程(km)">
              <el-input-number
                v-model="dir.lineTunnelMileage"
                :precision="2"
                :step="0.1"
                :min="0"
                style="width: 220px"
                controls-position="right"
                :disabled="!canTunnelUpdate"
              />
            </el-form-item>
            <el-form-item label="线路状态">
              <el-radio-group v-model="dir.lineStatus" :disabled="!canTunnelUpdate">
                <el-radio :label="0">有效</el-radio>
                <el-radio :label="1">失效</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>

          <div class="direction-payload-wrap">
            <TemplateDirectionDetailTabs
              v-if="dir.payload"
              :payload="dir.payload"
              :read-only="!canTunnelUpdate"
            />
          </div>

          <div v-if="canTunnelUpdate" class="direction-save-footer">
            <el-button type="primary" :loading="dir._saving" @click="saveDirection(dir)">保存本方向</el-button>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getTunnelParamTemplateDetail,
  updateTunnelParamTemplate,
  updateTunnelParamTemplateDirection
} from '@/api/tunnel'
import { hasPermission } from '@/utils/permission'
import { parseTemplatePayloadJson, serializeTemplatePayload } from '@/utils/templatePayload'
import {
  peekTemplateParamContextId,
  setTemplateParamContextId,
  clearTemplateParamContextId
} from '@/utils/templateParamContext'
import TemplateDirectionDetailTabs from '@/components/template/TemplateDirectionDetailTabs.vue'

const router = useRouter()

/** @type {import('vue').Ref<number|null>} */
const templateId = ref(peekTemplateParamContextId())

const canTunnelUpdate = computed(() => hasPermission('system:tunnel:update'))

const pageLoading = ref(true)
const loadError = ref('')

const headForm = reactive({
  templateName: '',
  status: 1,
  remark: ''
})

/** @type {import('vue').Ref<Array<Record<string, any>>>} */
const directionForms = ref([])

const headSaving = ref(false)

const directionText = (d) => {
  if (d === 1) return '右线'
  if (d === 2) return '左线'
  return d != null ? String(d) : '—'
}

const goBack = () => {
  clearTemplateParamContextId()
  router.push('/tunnel/param')
}

const goDetail = () => {
  if (templateId.value == null) return
  setTemplateParamContextId(templateId.value)
  router.push('/tunnel/param/detail')
}

const loadDetail = async () => {
  const id = templateId.value
  if (id == null) {
    loadError.value = '请从「模板列表」进入编辑'
    pageLoading.value = false
    return
  }
  pageLoading.value = true
  loadError.value = ''
  try {
    const res = await getTunnelParamTemplateDetail(id)
    if (res.code !== 200 || !res.data?.template) {
      loadError.value = '模板不存在或已删除'
      return
    }
    const t = res.data.template
    if (t?.id != null) {
      setTemplateParamContextId(t.id)
      templateId.value = Number(t.id)
    }
    headForm.templateName = t.templateName || ''
    headForm.status = t.status != null ? t.status : 1
    headForm.remark = t.remark || ''

    const dirs = Array.isArray(res.data.directions) ? res.data.directions : []
    directionForms.value = dirs.map((d) => ({
      id: d.id,
      templateId: d.templateId,
      sortOrder: d.sortOrder != null ? d.sortOrder : 0,
      direction: d.direction != null ? d.direction : 1,
      lineDisplayName: d.lineDisplayName || '',
      lineTunnelMileage:
        d.lineTunnelMileage === null || d.lineTunnelMileage === undefined ? null : Number(d.lineTunnelMileage),
      lineStatus: d.lineStatus != null ? d.lineStatus : 0,
      payload: parseTemplatePayloadJson(d.payloadJson),
      _saving: false
    }))
  } catch (e) {
    console.error(e)
    loadError.value = '加载失败'
  } finally {
    pageLoading.value = false
  }
}

const saveHead = async () => {
  const id = templateId.value
  if (id == null) return
  const name = (headForm.templateName || '').trim()
  if (!name) {
    ElMessage.warning('请填写模板名称')
    return
  }
  headSaving.value = true
  try {
    const res = await updateTunnelParamTemplate(id, {
      templateName: name,
      status: headForm.status,
      remark: headForm.remark || ''
    })
    if (res.code === 200) {
      ElMessage.success(res.msg || '已保存')
      await loadDetail()
    }
  } catch (e) {
    console.error(e)
  } finally {
    headSaving.value = false
  }
}

const saveDirection = async (dir) => {
  const id = templateId.value
  if (id == null || !dir?.id) return
  if (!dir.payload) {
    ElMessage.error('快照数据无效')
    return
  }
  let json
  try {
    json = serializeTemplatePayload(dir.payload)
    JSON.parse(json)
  } catch {
    ElMessage.error('快照序列化失败，请检查表单数据')
    return
  }
  dir._saving = true
  try {
    const res = await updateTunnelParamTemplateDirection(id, dir.id, {
      direction: dir.direction,
      sortOrder: dir.sortOrder,
      lineDisplayName: (dir.lineDisplayName || '').trim(),
      lineTunnelMileage: dir.lineTunnelMileage,
      lineStatus: dir.lineStatus,
      payloadJson: json
    })
    if (res.code === 200) {
      ElMessage.success(res.msg || '本方向已保存')
      await loadDetail()
    }
  } catch (e) {
    console.error(e)
  } finally {
    dir._saving = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>
