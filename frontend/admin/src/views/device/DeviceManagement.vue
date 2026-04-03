<template>
  <div class="device-management">
    <div class="card">
      <!-- 顶部选择区域 -->
      <div class="header-section">
        <div v-if="!embedded" class="tunnel-selector">
          <el-form :inline="true">
            <el-form-item label="选择隧道">
              <el-cascader
                :model-value="tunnelStore.currentTunnelPath"
                :options="tunnelStore.cascaderOptions"
                :props="{ expandTrigger: 'hover' }"
                placeholder="请选择隧道"
                clearable
                filterable
                style="width: min(400px, 100%); max-width: 100%"
                @change="handleTunnelChange"
              />
            </el-form-item>
          </el-form>
        </div>
        <div class="action-buttons">
          <el-button type="success" @click="handleAdd" v-permission="'device:add'">新增</el-button>
          <el-button type="primary" @click="handleRefresh" v-permission="'device:list'">刷新</el-button>
          <el-button type="warning" @click="handleExport" v-permission="'device:export'">导出</el-button>
        </div>
      </div>

      <!-- 与设备列表页一致的类型切换按钮 -->
      <div v-if="currentTunnelId" class="device-type-tabs">
        <div
          v-for="item in managementTabItems"
          :key="item.name"
          class="type-tab-item"
          :class="{ active: activeTab === item.name }"
          @click="activeTab = item.name"
        >
          {{ item.label }}
        </div>
      </div>

      <div v-if="currentTunnelId" class="tab-panels">
        <EdgeControllerTab
          v-show="activeTab === 'edgeController'"
          :tunnel-id="currentTunnelId"
          ref="edgeControllerRef"
          class="tab-panel"
        />
        <PowerTerminalTab
          v-show="activeTab === 'powerTerminal'"
          :tunnel-id="currentTunnelId"
          ref="powerTerminalRef"
          class="tab-panel"
        />
        <LampsControllerTab
          v-show="activeTab === 'lampsController'"
          :tunnel-id="currentTunnelId"
          ref="lampsControllerRef"
          class="tab-panel"
        />
        <ApproachLampsTab
          v-show="activeTab === 'approachLamps'"
          :tunnel-id="currentTunnelId"
          ref="approachLampsRef"
          class="tab-panel"
        />
      </div>

      <div class="empty-tip" v-else>
        <el-empty description="请先选择一个隧道" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTunnelStore } from '@/stores/tunnel'
import EdgeControllerTab from './components/EdgeControllerTab.vue'
import PowerTerminalTab from './components/PowerTerminalTab.vue'
import LampsControllerTab from './components/LampsControllerTab.vue'
import ApproachLampsTab from './components/ApproachLampsTab.vue'

const props = defineProps({
  /** 嵌入隧道列表子视图时隐藏级联选择，由外层固定 tunnelId */
  embedded: { type: Boolean, default: false }
})

const route = useRoute()
const tunnelStore = useTunnelStore()
const activeTab = ref('edgeController')

const TAB_KEYS = ['edgeController', 'powerTerminal', 'lampsController', 'approachLamps']

const managementTabItems = [
  { name: 'edgeController', label: '边缘控制器' },
  { name: 'powerTerminal', label: '电能终端' },
  { name: 'lampsController', label: '灯具终端' },
  { name: 'approachLamps', label: '引道灯控制器' }
]

const edgeControllerRef = ref()
const powerTerminalRef = ref()
const lampsControllerRef = ref()
const approachLampsRef = ref()

const currentTunnelId = computed(() => tunnelStore.currentTunnelId)

const handleTunnelChange = (path) => {
  tunnelStore.setCurrentTunnelPath(path)
}

const handleAdd = () => {
  switch (activeTab.value) {
    case 'edgeController':
      edgeControllerRef.value?.handleAdd()
      break
    case 'powerTerminal':
      powerTerminalRef.value?.handleAdd()
      break
    case 'lampsController':
      lampsControllerRef.value?.handleAdd()
      break
    case 'approachLamps':
      approachLampsRef.value?.handleAdd()
      break
  }
}

const handleRefresh = () => {
  switch (activeTab.value) {
    case 'edgeController':
      edgeControllerRef.value?.loadData()
      break
    case 'powerTerminal':
      powerTerminalRef.value?.loadData()
      break
    case 'lampsController':
      lampsControllerRef.value?.loadData()
      break
    case 'approachLamps':
      approachLampsRef.value?.loadData()
      break
  }
}

const handleExport = () => {
  switch (activeTab.value) {
    case 'edgeController':
      edgeControllerRef.value?.handleExport()
      break
    case 'powerTerminal':
      powerTerminalRef.value?.handleExport()
      break
    case 'lampsController':
      lampsControllerRef.value?.handleExport()
      break
    case 'approachLamps':
      approachLampsRef.value?.handleExport()
      break
  }
}

onMounted(async () => {
  await tunnelStore.loadTunnelData()
  const q = route.query.tunnelId
  if (q != null && String(q).length > 0) {
    const id = Number(q)
    if (!Number.isNaN(id)) {
      tunnelStore.setCurrentTunnelId(id)
    }
  }
  const tab = route.query.tab
  if (tab != null && TAB_KEYS.includes(String(tab))) {
    activeTab.value = String(tab)
  }
})
</script>

<style lang="scss" scoped>
.device-management {
  width: 100%;
  min-width: 0;

  /* 保证主内容区 flex 下占满宽度，避免表格挤在左侧 */
  :deep(.card) {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
  }

  .header-section {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    padding: 15px 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .device-type-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .type-tab-item {
      padding: 10px 24px;
      font-size: 14px;
      color: #606266;
      background: #f5f7fa;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 1px solid transparent;

      &:hover {
        color: #409eff;
        background: #ecf5ff;
      }

      /** 激活态与 /device/list 一致 */
      &.active {
        color: #fff;
        background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
        border-color: #409eff;
        font-weight: 500;
      }
    }
  }

  .tab-panels {
    width: 100%;
    min-width: 0;
    min-height: 55vh;
    display: flex;
    flex-direction: column;
  }

  .tab-panel {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    min-height: 0;
    width: 100%;
  }

  .empty-tip {
    padding: 100px 0;
  }
}
</style>
