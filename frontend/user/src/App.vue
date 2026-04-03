<template>
  <div class="app-root">
    <router-view />
    <footer class="icp-footer" :class="{ 'icp-footer-login': isLoginPage }">
      <div class="icp-content">
        <span>Copyright © {{ currentYear }} 中铁成都科学技术研究院有限公司. </span>
        <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer" class="icp-link">蜀ICP备20003395号</a>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentYear = new Date().getFullYear()
const isLoginPage = computed(() => route.path === '/login')
</script>

<style scoped>
.app-root {
  position: relative;
  min-height: 100vh;
}

.icp-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  background: transparent;
}

.icp-footer-login {
  z-index: 99999;
}

.icp-content {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #5F646B;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
}

.icp-link {
  color: #5F646B;
  text-decoration: none;
  transition: color 0.2s ease;
}

.icp-link:hover {
  color: #4a4f55;
  text-decoration: underline;
}
</style>
