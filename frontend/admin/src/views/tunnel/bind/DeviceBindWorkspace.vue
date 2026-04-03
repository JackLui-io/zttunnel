<template>
  <div class="device-bind-workspace device-bind-workspace--fill-height">
    <div class="card device-bind-workspace-card">
      <div v-if="!embedded" class="page-header">
        <div class="head-text">
          <h2 class="title">{{ workspaceTitle }}</h2>
          <p class="sub">
            {{ workspaceSub }}
          </p>
        </div>
        <el-button @click="goBack">返回隧道列表</el-button>
      </div>

      <el-alert
        v-if="displayTunnelBanner"
        type="info"
        :closable="false"
        show-icon
        class="tunnel-banner-alert"
      >
        <template #title>当前隧道：{{ displayTunnelBanner }}</template>
      </el-alert>

      <el-alert
        v-if="tunnelId == null"
        type="warning"
        title="未选择隧道或会话已失效，请从隧道列表进入"
        :closable="false"
        show-icon
      />

      <template v-else>
        <el-tabs v-model="activeTab" type="border-card" class="bind-tabs">
          <el-tab-pane label="隧道参数" name="param" class="tab-pane--param">
            <div class="param-tab-layout">
              <div ref="paramTabTopRef" class="param-tab-top-anchor" aria-hidden="true" />
              <div class="param-tab-scroll">
                <TunnelParamForm ref="paramFormRef" :tunnel-id="tunnelId" :read-only="readOnly" />
              </div>
              <div v-if="!readOnly" class="param-tab-footer">
                <el-button type="primary" :loading="savingParam" @click="submitParamTab">提交</el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="边缘控制器" name="edge">
            <EdgeControllerTab
              ref="edgeRef"
              :tunnel-id="tunnelId"
              :device-bind-mode="edgeDeviceBindMode"
              :read-only="readOnly"
              :show-column-panel="deviceTableShowColumnPanel"
            />
          </el-tab-pane>

          <el-tab-pane label="电能终端" name="power">
            <PowerTerminalTab
              ref="powerRef"
              :tunnel-id="tunnelId"
              :read-only="readOnly"
              :show-column-panel="deviceTableShowColumnPanel"
            />
          </el-tab-pane>

          <el-tab-pane label="灯具终端" name="lamps">
            <LampsControllerTab
              ref="lampsRef"
              :tunnel-id="tunnelId"
              :read-only="readOnly"
              :show-column-panel="deviceTableShowColumnPanel"
            />
          </el-tab-pane>

          <el-tab-pane label="引道灯控制器" name="approach">
            <ApproachLampsTab
              ref="approachRef"
              :tunnel-id="tunnelId"
              :read-only="readOnly"
              :show-column-panel="deviceTableShowColumnPanel"
            />
          </el-tab-pane>

          <el-tab-pane label="电表" name="meter">
            <PowerMeterTable
              ref="meterRef"
              :tunnel-id="tunnelId"
              :read-only="readOnly"
              :show-column-panel="deviceTableShowColumnPanel"
            />
          </el-tab-pane>
        </el-tabs>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  peekDeviceBindContextTunnelId,
  peekDeviceBindTunnelName,
  peekDeviceBindTunnelMeta,
  setDeviceBindContextTunnelId,
  clearDeviceBindContextTunnelId
} from '@/utils/deviceBindContext'
import { peekTunnelDetailWorkspace } from '@/utils/tunnelDetailContext'
import TunnelParamForm from '@/views/tunnel/param/components/TunnelParamForm.vue'
import EdgeControllerTab from '@/views/device/components/EdgeControllerTab.vue'
import PowerTerminalTab from '@/views/device/components/PowerTerminalTab.vue'
import LampsControllerTab from '@/views/device/components/LampsControllerTab.vue'
import ApproachLampsTab from '@/views/device/components/ApproachLampsTab.vue'
import PowerMeterTable from '@/views/tunnel/param/components/PowerMeterTable.vue'

const props = defineProps({
  /** 嵌入隧道列表时隐藏顶栏，由外层提供「返回」 */
  embedded: { type: Boolean, default: false },
  /** 嵌入时由列表页传入隧道 ID；设备绑定用 session，隧道查看/编辑用 query.tunnelId */
  tunnelIdProp: { type: [Number, String], default: null },
  /** 仅浏览：表单禁用、设备表无操作列 */
  readOnly: { type: Boolean, default: false },
  /** true=占位绑定工作台（边缘 Tab 为绑定样式）；false=与设备管理一致的边缘表 */
  edgeDeviceBindMode: { type: Boolean, default: true },
  /** 默认选中的 Tab：隧道查看/编辑用 param，设备绑定用 edge */
  defaultActiveTab: { type: String, default: 'edge' },
  /** 顶部提示：当前隧道名称（不含 ID，可与 session 一致） */
  tunnelBannerName: { type: String, default: '' },
  /** 每次查看/编辑进入时递增，用于将「隧道参数」Tab 滚到顶部（绑定页勿传或传 0） */
  scrollParamTopKey: { type: Number, default: 0 }
})

const route = useRoute()
const router = useRouter()

/** 设备绑定页保留「显示列」；隧道查看/编辑（嵌入且非绑定模式）隐藏并默认全列 */
const deviceTableShowColumnPanel = computed(
  () => !(props.embedded && !props.edgeDeviceBindMode)
)

const tunnelId = computed(() => {
  if (props.tunnelIdProp != null && props.tunnelIdProp !== '') {
    const n = Number(props.tunnelIdProp)
    if (!Number.isNaN(n)) return n
  }
  if (props.embedded) return null
  const q = route.query.tunnelId ?? route.query.tunnel_id
  if (q != null && q !== '') {
    const n = Number(q)
    if (!Number.isNaN(n)) return n
  }
  return peekDeviceBindContextTunnelId()
})

const displayTunnelBanner = computed(() => {
  if (props.tunnelBannerName) return props.tunnelBannerName
  if (!props.embedded) {
    const bindMeta = peekDeviceBindTunnelMeta()
    if (bindMeta.pathFromRoot) return bindMeta.pathFromRoot
    return peekDeviceBindTunnelName()
  }
  const detail = peekTunnelDetailWorkspace()
  if (detail?.pathFromRoot) return detail.pathFromRoot
  if (detail?.tunnelName) return detail.tunnelName
  return ''
})

const workspaceTitle = computed(() => {
  if (props.readOnly) return '查看隧道'
  if (props.edgeDeviceBindMode && !props.embedded) return '设备绑定'
  if (!props.edgeDeviceBindMode) return '编辑隧道'
  return '设备绑定'
})

const workspaceSub = computed(() => {
  if (props.readOnly) {
    return '与后台隧道管理「查看」一致：通过 Tab 浏览隧道参数及各设备列表，不可保存。'
  }
  if (!props.edgeDeviceBindMode) {
    return '与后台隧道管理「编辑」一致：可保存隧道参数，并在各设备 Tab 中进行控制、编辑等操作。'
  }
  return '将占位设备号改为真实设备号并核对隧道参数。隧道参数在页签内提交；各设备 Tab 在表格行内操作或刷新列表。'
})

/** 独立页：若地址栏带 tunnelId，迁入 session 后去掉查询参数（设备绑定独立页） */
onMounted(() => {
  if (props.embedded) return
  const raw = route.query.tunnelId ?? route.query.tunnel_id
  if (raw == null || raw === '') return
  const n = Number(raw)
  if (!Number.isNaN(n)) setDeviceBindContextTunnelId(n)
  const nextQuery = { ...route.query }
  delete nextQuery.tunnelId
  delete nextQuery.tunnel_id
  router.replace({ path: route.path, query: nextQuery })
})

const BIND_TAB_NAMES = new Set(['param', 'edge', 'power', 'lamps', 'approach', 'meter'])
const activeTab = ref('param')

const syncActiveTab = () => {
  const q = route.query.bindTab
  if (q != null && q !== '' && BIND_TAB_NAMES.has(String(q))) {
    activeTab.value = String(q)
    return
  }
  if (props.defaultActiveTab && BIND_TAB_NAMES.has(props.defaultActiveTab)) {
    activeTab.value = props.defaultActiveTab
  }
}

watch(
  () => [route.query.bindTab, props.defaultActiveTab],
  () => syncActiveTab(),
  { immediate: true }
)

const paramTabTopRef = ref(null)

const scrollParamTabToTop = () => {
  nextTick(() => {
    requestAnimationFrame(() => {
      paramTabTopRef.value?.scrollIntoView({ block: 'start', behavior: 'auto' })
    })
  })
}

watch(
  () => [props.scrollParamTopKey, tunnelId.value],
  ([k, id]) => {
    if (!k || !props.embedded || id == null) return
    if (props.defaultActiveTab !== 'param') return
    nextTick(() => {
      syncActiveTab()
      activeTab.value = 'param'
      scrollParamTabToTop()
    })
  },
  { flush: 'post' }
)

const paramFormRef = ref()
const edgeRef = ref()
const powerRef = ref()
const lampsRef = ref()
const approachRef = ref()
const meterRef = ref()

const savingParam = ref(false)

const emit = defineEmits(['close'])

const goBack = () => {
  if (props.embedded) {
    emit('close')
    return
  }
  clearDeviceBindContextTunnelId()
  router.replace({ path: '/tunnel/list', query: {} })
}

const submitParamTab = async () => {
  if (props.readOnly) return
  if (!paramFormRef.value) return
  savingParam.value = true
  try {
    await paramFormRef.value.saveParams()
    ElMessage.success('隧道参数已保存')
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    savingParam.value = false
  }
}

</script>

<style scoped lang="scss">
/* 独立页：占满 .content 可视高度（由父级 flex 撑满时也可用 flex:1，见 TunnelList 嵌入） */
.device-bind-workspace--fill-height {
  flex: 1;
  min-height: 0;
  height: calc(100vh - 60px - 40px);
  display: flex;
  flex-direction: column;
  min-width: 0;
  box-sizing: border-box;

  .device-bind-workspace-card {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-sizing: border-box;

    > .page-header {
      flex-shrink: 0;
    }

    > .tunnel-banner-alert,
    > .el-alert {
      flex-shrink: 0;
    }
  }
}

.device-bind-workspace {
  .tunnel-banner-alert {
    margin-bottom: 12px;
    :deep(.el-alert__title) {
      white-space: normal;
      line-height: 1.45;
    }
  }
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: rgba(0, 234, 255, 0.06);
    border-radius: 8px;
    border: 1px solid rgba(0, 234, 255, 0.2);
  }
  .head-text .title {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
  }
  .head-text .sub {
    margin: 0;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    line-height: 1.5;
    max-width: 820px;
  }
  .bind-tabs {
    margin-top: 8px;
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;

    :deep(.el-tabs__header) {
      flex-shrink: 0;
    }

    :deep(.el-tabs__content) {
      width: 100%;
      flex: 1;
      min-height: 0;
      overflow: hidden;
    }

    :deep(.el-tab-pane) {
      min-width: 0;
      height: 100%;
      max-height: 100%;
      box-sizing: border-box;
    }

    :deep(.el-tab-pane.tab-pane--param) {
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    :deep(.el-tab-pane:not(.tab-pane--param)) {
      overflow: hidden;
      padding-right: 4px;
      min-width: 0;
      display: flex;
      flex-direction: column;
      align-items: stretch;

      & > * {
        flex: 1;
        min-width: 0;
        min-height: 0;
        display: flex;
        flex-direction: column;
      }
    }
  }

  .param-tab-layout {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .param-tab-scroll {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
    padding-right: 4px;
  }

  .param-tab-footer {
    flex-shrink: 0;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 12px 4px 4px;
    margin-top: auto;
    border-top: 1px solid var(--el-border-color-lighter);
    background: var(--el-bg-color);
  }

  .param-tab-top-anchor {
    flex-shrink: 0;
    height: 0;
    overflow: hidden;
  }
}
</style>
