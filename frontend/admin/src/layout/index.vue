<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <Sidebar :collapsed="isCollapsed" />
    </div>

    <!-- 主容器 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="header">
        <Header @toggle-sidebar="toggleSidebar" :collapsed="isCollapsed" />
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'

const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 250px;
  background: #304156;
  transition: all 0.3s cubic-bezier(0.2, 0, 0, 1);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.sidebar.collapsed {
  width: 64px;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 999;
  flex-shrink: 0;
}

.content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #f0f2f5;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
  }

  .sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .main-container {
    margin-left: 0;
  }
}
</style>
