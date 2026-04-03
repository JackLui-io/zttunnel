<template>
  <div class="header-container">
    <div class="header-left">
      <!-- 折叠按钮 -->
      <el-button
        type="text"
        @click="toggleSidebar"
        class="toggle-btn"
      >
        <el-icon size="20">
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
      </el-button>

      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item
          v-for="item in breadcrumbList"
          :key="item.path"
          :to="item.path"
        >
          {{ item.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <!-- 用户信息 -->
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" :src="userInfo.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="username">{{ userInfo.username }}</span>
          <el-icon class="arrow-down"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">系统设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-sidebar'])

const route = useRoute()
const router = useRouter()

import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 用户信息
const userInfo = computed(() => ({
  username: userStore.userInfo?.nickName || userStore.userInfo?.username || '管理员',
  avatar: userStore.userInfo?.avatar || ''
}))

// 面包屑导航
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched
})

// 切换侧边栏
const toggleSidebar = () => {
  emit('toggle-sidebar')
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    ElMessage.success('退出登录成功')
    // 使用store的logout方法，会自动刷新页面
    await userStore.logout()
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.header-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;

  .toggle-btn {
    margin-right: 20px;
    color: #606266;

    &:hover {
      color: #409EFF;
    }
  }

  .breadcrumb {
    font-size: 14px;
  }
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 4px;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }

    .username {
      margin: 0 8px;
      font-size: 14px;
      color: #606266;
    }

    .arrow-down {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
