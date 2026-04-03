<template>
  <div class="sidebar-container">
    <!-- Logo区域 -->
    <div class="logo-container" @click="goToDashboard">
      <img src="/favicon.png" alt="logo" class="logo-img" v-if="!collapsed">
      <span class="logo-text" v-if="!collapsed">隧道管理系统</span>
      <img src="/favicon.png" alt="logo" class="logo-img-mini" v-else>
    </div>

    <!-- 菜单区域 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :unique-opened="false"
      :collapse-transition="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#ffffff"
      router
      class="sidebar-menu"
    >
      <sidebar-item
        v-for="route in routes"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { checkRoutePermission } from '@/utils/permission'
import SidebarItem from './SidebarItem.vue'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const router = useRouter()

// 获取当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 获取路由配置
const routes = computed(() => {
  return router.getRoutes().filter(route => {
    // 基本过滤条件
    if (route.path === '/login' || !route.meta || !route.meta.title || route.meta.hidden) {
      return false
    }

    // 只显示有children的路由
    if (!route.children || route.children.length === 0) {
      return false
    }

    // 权限过滤：检查用户是否有权限访问该路由或其子路由
    return hasAccessToRoute(route)
  })
})

// 检查用户是否有权限访问路由
const hasAccessToRoute = (route) => {
  // 如果父路由有权限要求，检查父路由权限
  if (route.meta && (route.meta.permission || route.meta.permissions || route.meta.role || route.meta.roles)) {
    if (!checkRoutePermission(route)) {
      return false
    }
  }

  // 检查是否有任何子路由可以访问
  if (route.children && route.children.length > 0) {
    return route.children.some(child => {
      // 跳过隐藏的子路由
      if (child.meta && child.meta.hidden) {
        return false
      }

      // 如果子路由没有权限要求，则可以访问
      if (!child.meta || (!child.meta.permission && !child.meta.permissions && !child.meta.role && !child.meta.roles)) {
        return true
      }

      // 检查子路由权限
      return checkRoutePermission(child)
    })
  }

  return true
}

// 点击Logo返回首页
const goToDashboard = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b2f3a;
  border-bottom: 1px solid #3a3f51;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: #363c4a;
  }

  .logo-img {
    width: 32px;
    height: 32px;
    margin-right: 12px;
    transition: all 0.3s;
  }

  .logo-img-mini {
    width: 32px;
    height: 32px;
    transition: all 0.3s;
  }

  .logo-text {
    font-size: 16px;
    font-weight: bold;
    color: #fff;
    white-space: nowrap;
    transition: all 0.3s;
  }
}

.sidebar-menu {
  flex: 1;
  border: none;
  overflow-x: hidden;
  overflow-y: auto;

  // 一级菜单项
  :deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
    border-radius: 0;
    transition: all 0.3s ease;

    &:hover {
      background-color: #263445 !important;
      color: #ffffff !important;
    }

    &.is-active {
      background: linear-gradient(90deg, #409EFF 0%, rgba(64, 158, 255, 0.9) 100%) !important;
      color: #ffffff !important;
      position: relative;
      font-weight: 600;
      box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: #ffffff;
        box-shadow: 0 0 8px rgba(255, 255, 255, 0.3);
      }

      .el-icon {
        color: #ffffff !important;
      }
    }
  }

  // 子菜单标题
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
    border-radius: 0;
    transition: all 0.3s ease;

    &:hover {
      background-color: #263445 !important;
      color: #ffffff !important;
    }

    &.is-active {
      background-color: rgba(64, 158, 255, 0.2) !important;
      color: #409EFF !important;
      font-weight: 600;
    }
  }

  // 子菜单项
  :deep(.el-sub-menu .el-menu-item) {
    height: 45px;
    line-height: 45px;
    background-color: #1f2d3d !important;
    padding-left: 50px !important;
    transition: all 0.3s ease;

    &:hover {
      background-color: #263445 !important;
      color: #ffffff !important;
    }

    &.is-active {
      background: linear-gradient(90deg, #409EFF 0%, rgba(64, 158, 255, 0.9) 100%) !important;
      color: #ffffff !important;
      position: relative;
      font-weight: 600;
      box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: #ffffff;
        box-shadow: 0 0 8px rgba(255, 255, 255, 0.3);
      }

      .el-icon {
        color: #ffffff !important;
      }
    }
  }

  // 收起状态下的样式调整
  &.el-menu--collapse {
    .el-sub-menu {
      .el-sub-menu__title {
        padding: 0 20px !important;
      }
    }

    .el-menu-item {
      padding: 0 20px !important;
      text-align: center;
    }
  }

  // 子菜单展开动画
  :deep(.el-sub-menu__icon-arrow) {
    transition: transform 0.3s;
    font-size: 12px;
  }

  :deep(.el-sub-menu.is-opened .el-sub-menu__icon-arrow) {
    transform: rotateZ(180deg);
  }

  // 滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #2b2f3a;
  }

  &::-webkit-scrollbar-thumb {
    background: #48576a;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #5a6c7d;
  }
}
</style>
