<template>
  <div class="main-layout">
    <div class="main-layout-bg">
      <div class="node-1"></div>
      <div class="node-2"></div>
      <div class="node-3"></div>
    </div>
    <TopBar :show-menu-btn="true" @toggle-menu="showLeftDrawer = !showLeftDrawer" />
    <div v-if="showLeftDrawer" class="main-layout-overlay" @click="showLeftDrawer = false" aria-hidden="true"></div>
    <LeftNav
      :tunnel-data="tunnelData"
      :tunnel-loading="tunnelLoading"
      :drawer-open="showLeftDrawer"
      :selected-tunnel-id="tunnelStore.selectedTunnelId"
      @close="showLeftDrawer = false"
      @select-line="onSelectLine"
    />
    <main class="main-content">
      <router-view />
    </main>
    <aside class="right-sidebar-wrap">
      <RightSidebar />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, provide, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TopBar from './TopBar.vue'
import LeftNav from './LeftNav.vue'
import RightSidebar from './RightSidebar.vue'
import { getTunnelPathById, getFirstLevel4Node } from '@/data/tunnel'
import { getTunnelTree, type TunnelNode } from '@/api/tunnel'
import { useTunnelStore } from '@/stores/tunnel'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const tunnelStore = useTunnelStore()
const authStore = useAuthStore()
const showLeftDrawer = ref(false)
const tunnelData = ref<TunnelNode[]>([])
const tunnelLoading = ref(true)

const selectedTunnelPath = computed(() => {
  const id = tunnelStore.selectedTunnelId
  if (id == null || !tunnelData.value.length) return null
  return getTunnelPathById(tunnelData.value, id)
})

function onSelectLine(payload: { id: number; name: string; parentId?: number }) {
  tunnelStore.setSelectedTunnelId(payload.id, payload.parentId)
  // dashboard 页面点击隧道时跳转到实时监控
  if (route.path === '/dashboard') {
    router.push('/supervisoryControl')
  }
}

onMounted(async () => {
  authStore.fetchUserInfo()
  try {
    tunnelData.value = await getTunnelTree()
    // 非 dashboard 页面：默认选中第一个 level-4（线路），供 supervisoryControl 等页使用
    if (route.path !== '/dashboard' && tunnelStore.selectedTunnelId == null) {
      const first = getFirstLevel4Node(tunnelData.value)
      if (first != null) {
        tunnelStore.setSelectedTunnelId(first.id!, first.parentId)
      }
    }
  } catch {
    tunnelData.value = []
  } finally {
    tunnelLoading.value = false
  }
})

provide('selectedTunnelPath', selectedTunnelPath)
provide('tunnelData', tunnelData)
</script>

<style scoped>
.main-layout {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: #93cba7;
}

.main-layout-bg {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.main-layout-overlay {
  display: none;
}

.node-1 {
  position: absolute;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  background: #93cba7;
  z-index: 1;
}

.node-2 {
  position: absolute;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
  background: #edf2ea;
  z-index: 2;
}

/* 卡片框仅包含隧道列表，不包含标题；标题区仅显示标题图+文字 */
.node-3 {
  position: absolute;
  width: 274px;
  left: 8px;
  top: 139px; /* 90 + 49，标题 49px 在卡片框上方 */
  bottom: 10px;
  background: transparent;
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  z-index: 3;
}

.main-content {
  position: absolute;
  left: 296px;
  top: 90px;
  right: 477px;
  bottom: 10px;
  z-index: 15;
  overflow: hidden;
}

.right-sidebar-wrap {
  position: absolute;
  right: 16px;
  top: 90px;
  bottom: 10px;
  z-index: 15;
  /* padding-bottom: 15px;
  box-sizing: border-box;
  overflow-y: auto; */
}

/* 平板 */
@media (max-width: 1024px) {
  .node-3 {
    width: 220px;
    left: 4px;
    top: 119px; /* 70 + 49，标题在卡片框上方 */
    bottom: 10px;
    height: auto;
  }

  .main-content {
    left: 228px;
    top: 70px;
    right: 340px;
  }

  .right-sidebar-wrap {
    top: 70px;
    right: 8px;
  }
}

/* 移动端 */
@media (max-width: 768px) {
  .main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }

  .main-layout-bg,
  .node-1,
  .node-2,
  .node-3 {
    display: none;
  }

  .main-layout-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: 90;
    transition: opacity 0.3s;
  }

  .main-content {
    position: relative;
    left: 0;
    top: 0;
    right: 0;
    bottom: auto;
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 12px;
  }

  .right-sidebar-wrap {
    position: relative;
    right: 0;
    top: 0;
    padding: 0 12px 12px;
    width: 100%;
  }
}
</style>
