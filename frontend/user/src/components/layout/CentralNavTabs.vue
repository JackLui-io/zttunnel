<template>
  <div class="central-nav-tabs">
    <template v-for="tab in tabs" :key="tab.path">
      <router-link
        v-if="!tab.disabled"
        :to="tab.path"
        class="central-tab-btn"
        :class="{ active: isActive(tab.path) }"
        :aria-label="tab.label"
      >
        <span class="central-tab-label">{{ tab.label }}</span>
      </router-link>
      <span
        v-else
        class="central-tab-btn disabled"
        :aria-label="tab.label"
      >
        <span class="central-tab-label">{{ tab.label }}</span>
      </span>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

defineProps<{ activePage?: string }>()

const route = useRoute()

const tabs = [
  { path: '/supervisoryControl', label: '实时监控' },
  { path: '/statisticalAnalysis', label: '统计分析' },
  { path: '/reportGeneration', label: '报表生成', disabled: true },
  { path: '/equipmentManagement', label: '设备管理', disabled: true },
]

function isActive(path: string) {
  return route.path === path
}
</script>

<style scoped>
.central-nav-tabs {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.central-tab-btn {
  position: relative;
  padding: 0;
  border: none;
  background: url('/page1/btn_default.png') center / contain no-repeat;
  cursor: pointer;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 144px;
  height: 42px;
  box-sizing: border-box;
}
.central-tab-btn:hover {
  opacity: 0.9;
}
.central-tab-btn.disabled {
  cursor: not-allowed;
  opacity: 0.5;
  pointer-events: none;
}
.central-tab-btn.disabled:hover {
  opacity: 0.5;
}
.central-tab-btn.active {
  background-image: url('/page1/btn_selected.png');
  opacity: 1;
}
.central-tab-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 14px;
  line-height: 21px;
  color: rgb(15, 127, 98);
  pointer-events: none;
}
.central-tab-btn.active .central-tab-label {
  color: #FFFFFF;
}

@media (max-width: 768px) {
  .central-nav-tabs {
    gap: 8px;
  }
  .central-tab-btn {
    width: 120px;
    height: 36px;
  }
  .central-tab-label {
    font-size: 12px;
    line-height: 18px;
  }
}
</style>
