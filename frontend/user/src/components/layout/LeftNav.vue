<template>
  <nav class="left-nav" :class="{ 'is-drawer-open': drawerOpen }">
    <div class="left-nav-header">
      <img src="/page1/Group95.svg" alt="" class="left-nav-header-bg" width="274" height="49" />
      <span class="left-nav-header-title">隧道选择</span>
      <button v-if="drawerOpen" type="button" class="left-nav-close" aria-label="关闭" @click="$emit('close')">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" />
        </svg>
      </button>
    </div>
    <div class="left-nav-bg"></div>
    <div class="tunnel-tree">
      <div v-if="props.tunnelLoading" class="tunnel-loading">加载中...</div>
      <div v-else-if="!props.tunnelData.length" class="tunnel-empty">暂无隧道数据</div>
      <TunnelItem
        v-else
        v-for="(item, idx) in props.tunnelData"
        :key="idx"
        :item="item"
        :level="1"
        :company-idx="idx"
        :default-expanded="idx === 0"
        :is-company-selected="expandedCompany === idx"
        :expand-all-in-company="expandedCompany === idx"
        :selected-tunnel-id="props.selectedTunnelId ?? null"
        @company-expand="expandedCompany = $event"
        @select-line="selectLine"
      />
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { TunnelNode } from '@/data/tunnel'
import TunnelItem from './TunnelItem.vue'

const props = defineProps<{
  tunnelData: TunnelNode[]
  tunnelLoading?: boolean
  drawerOpen?: boolean
  selectedTunnelId?: number | null
}>()
const emit = defineEmits<{ close: []; selectLine: [payload: { id: number; name: string; parentId?: number }] }>()
const expandedCompany = ref(0)

function selectLine(payload: { id: number; name: string }) {
  emit('selectLine', payload)
}
</script>

<style scoped>
.left-nav {
  position: absolute;
  left: 8px;
  top: 90px;
  bottom: 15px;
  width: 274px;
  z-index: 20;
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.left-nav-header {
  position: absolute;
  left: 0;
  top: 0;
  width: 274px;
  height: 49px;
  z-index: 6;
}
.left-nav-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.left-nav-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  line-height: 18px;
  color: #0f7f62;
}

.left-nav-bg {
  position: absolute;
  width: 274px;
  height: 832px;
  left: 0;
  top: 49px;
  z-index: 6;
}

.left-nav .tunnel-tree {
  position: absolute;
  left: 0;
  top: 49px;
  right: 0;
  bottom: 0;
  width: 274px;
  z-index: 10;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: #91CAA7 transparent;
}
.left-nav .tunnel-tree::-webkit-scrollbar {
  width: 8px;
}
.left-nav .tunnel-tree::-webkit-scrollbar-track {
  background: transparent;
}
.left-nav .tunnel-tree::-webkit-scrollbar-thumb {
  background: #91CAA7;
  border-radius: 4px;
}
.tunnel-loading,
.tunnel-empty {
  padding: 20px 16px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: #5f646b;
}

.left-nav-close {
  display: none;
}

@media (max-width: 768px) {
  .left-nav {
    position: fixed;
    left: 0;
    top: 0;
    width: 280px;
    max-width: 85vw;
    min-height: 100vh;
    z-index: 95;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    box-shadow: 4px 0 12px rgba(0, 0, 0, 0.15);
  }

  .left-nav.is-drawer-open {
    transform: translateX(0);
  }

  .left-nav-header {
    position: relative;
    width: 100%;
    display: flex;
    align-items: center;
  }

  .left-nav-close {
    display: flex;
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    width: 40px;
    height: 40px;
    padding: 0;
    border: none;
    background: none;
    cursor: pointer;
    color: #0f7f62;
    align-items: center;
    justify-content: center;
  }

  .left-nav-bg {
    display: none;
  }

  .left-nav .tunnel-tree {
    width: 100%;
    padding-bottom: 24px;
    overflow-y: auto;
  }
}
</style>
