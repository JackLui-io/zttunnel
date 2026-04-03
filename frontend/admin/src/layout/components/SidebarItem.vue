<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <!-- 有子菜单的情况 -->
    <el-sub-menu
      v-if="hasChildren"
      :index="getFullPath(item.path)"
      :popper-append-to-body="true"
    >
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta && item.meta.title }}</span>
      </template>

      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="getFullPath(item.path)"
      />
    </el-sub-menu>

    <!-- 没有子菜单的情况 -->
    <el-menu-item
      v-else
      :index="getFullPath(item.path)"
    >
      <el-icon v-if="item.meta && item.meta.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ item.meta && item.meta.title }}</span>
      </template>
    </el-menu-item>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'
import { checkRoutePermission } from '@/utils/permission'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

// 获取可见的子菜单（过滤权限和隐藏状态）
const visibleChildren = computed(() => {
  const children = props.item.children
  if (!children || children.length === 0) {
    return []
  }

  return children.filter(child => {
    // 过滤隐藏的菜单
    if (child.meta && child.meta.hidden) {
      return false
    }

    // 如果没有权限要求，显示菜单
    if (!child.meta || (!child.meta.permission && !child.meta.permissions && !child.meta.role && !child.meta.roles)) {
      return true
    }

    // 检查权限
    return checkRoutePermission(child)
  })
})

// 判断是否有子菜单
const hasChildren = computed(() => {
  return visibleChildren.value.length > 0
})

// 获取完整路径
const getFullPath = (routePath) => {
  if (!routePath) return props.basePath
  if (isExternal(routePath)) return routePath
  // 如果routePath已经是完整路径（以/开头），直接返回
  if (routePath.startsWith('/')) return routePath
  // 拼接basePath和routePath
  if (!props.basePath) return '/' + routePath
  const base = props.basePath.endsWith('/') ? props.basePath.slice(0, -1) : props.basePath
  return base + '/' + routePath
}
</script>
