import { useUserStore } from '@/stores/user'

/**
 * 检查用户是否有指定权限
 * @param {string|Array} permission - 权限标识，可以是字符串或数组
 * @returns {boolean}
 */
export function hasPermission(permission) {
  const userStore = useUserStore()
  const permissions = userStore.userInfo.permissions || []
  const roles = userStore.userInfo.roles || []
  const username = userStore.userInfo.username || userStore.userInfo.userName || ''

  // 如果没有权限要求，直接返回true
  if (!permission) {
    return true
  }

  // 超级管理员拥有所有权限
  if (roles.includes('admin') || roles.includes('super_admin') || username === 'admin') {
    return true
  }

  if (Array.isArray(permission)) {
    return permission.some(p => permissions.includes(p))
  }

  return permissions.includes(permission)
}

/**
 * 检查用户是否有指定角色
 * @param {string|Array} role - 角色标识，可以是字符串或数组
 * @returns {boolean}
 */
export function hasRole(role) {
  const userStore = useUserStore()
  const roles = userStore.userInfo.roles || []
  const username = userStore.userInfo.username || userStore.userInfo.userName || ''

  if (!role) {
    return true
  }

  // 超级管理员拥有所有角色
  if (roles.includes('admin') || roles.includes('super_admin') || username === 'admin') {
    return true
  }

  if (Array.isArray(role)) {
    return role.some(r => roles.includes(r))
  }

  return roles.includes(role)
}

/**
 * 检查用户是否有任意一个权限
 * @param {Array} permissions - 权限数组
 * @returns {boolean}
 */
export function hasAnyPermission(permissions) {
  if (!Array.isArray(permissions) || permissions.length === 0) {
    return true
  }
  return hasPermission(permissions)
}

/**
 * 检查用户是否拥有所有权限
 * @param {Array} permissions - 权限数组
 * @returns {boolean}
 */
export function hasAllPermissions(permissions) {
  if (!Array.isArray(permissions) || permissions.length === 0) {
    return true
  }

  const userStore = useUserStore()
  const userPermissions = userStore.userInfo.permissions || []
  const roles = userStore.userInfo.roles || []
  const username = userStore.userInfo.username || userStore.userInfo.userName || ''

  // 超级管理员拥有所有权限
  if (roles.includes('admin') || roles.includes('super_admin') || username === 'admin') {
    return true
  }

  return permissions.every(p => userPermissions.includes(p))
}

/**
 * 检查路由权限
 * @param {Object} route - 路由对象
 * @returns {boolean}
 */
export function checkRoutePermission(route) {
  const userStore = useUserStore()
  const userRoles = userStore.userInfo.roles || []
  const username = userStore.userInfo.username || userStore.userInfo.userName || ''

  // 超级管理员跳过所有权限检查
  if (userRoles.includes('admin') || userRoles.includes('super_admin') || username === 'admin') {
    return true
  }

  if (!route.meta) {
    return true
  }

  const { permission, permissions, role, roles } = route.meta

  // 检查单个权限
  if (permission && !hasPermission(permission)) {
    return false
  }

  // 检查多个权限（任意一个）
  if (permissions && !hasAnyPermission(permissions)) {
    return false
  }

  // 检查角色
  if (role && !hasRole(role)) {
    return false
  }

  if (roles && !hasRole(roles)) {
    return false
  }

  return true
}
