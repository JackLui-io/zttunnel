import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { hasPermission, hasRole, hasAnyPermission, hasAllPermissions } from '@/utils/permission'

/**
 * 权限检查组合式函数
 */
export function usePermission() {
  const userStore = useUserStore()

  // 用户权限列表
  const permissions = computed(() => userStore.userInfo.permissions || [])

  // 用户角色列表
  const roles = computed(() => userStore.userInfo.roles || [])

  // 检查权限的方法
  const checkPermission = (permission) => {
    return hasPermission(permission)
  }

  // 检查角色的方法
  const checkRole = (role) => {
    return hasRole(role)
  }

  // 检查任意权限
  const checkAnyPermission = (permissionList) => {
    return hasAnyPermission(permissionList)
  }

  // 检查所有权限
  const checkAllPermissions = (permissionList) => {
    return hasAllPermissions(permissionList)
  }

  // 是否为超级管理员
  const isSuperAdmin = computed(() => {
    return roles.value.includes('admin') || roles.value.includes('super_admin')
  })

  // 是否为管理员
  const isAdmin = computed(() => {
    return roles.value.includes('admin')
  })

  return {
    permissions,
    roles,
    checkPermission,
    checkRole,
    checkAnyPermission,
    checkAllPermissions,
    isSuperAdmin,
    isAdmin
  }
}

/**
 * 页面权限检查组合式函数
 */
export function usePagePermission(requiredPermission) {
  const { checkPermission } = usePermission()

  const hasPagePermission = computed(() => {
    if (!requiredPermission) return true
    return checkPermission(requiredPermission)
  })

  return {
    hasPagePermission
  }
}
