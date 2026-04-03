import { useUserStore } from '@/stores/user'

/**
 * 调试权限信息
 * 在浏览器控制台中调用 window.debugPermission() 来查看当前用户权限
 */
export function debugPermission() {
  const userStore = useUserStore()
  const userInfo = userStore.userInfo

  console.group('🔍 权限调试信息')
  console.log('📋 用户信息:', userInfo)
  console.log('👤 用户名:', userInfo.username || userInfo.userName)
  console.log('🎭 角色列表:', userInfo.roles)
  console.log('🔑 权限列表:', userInfo.permissions)
  console.log('🏷️ 角色名称:', userInfo.roleName)
  console.log('🆔 用户ID:', userInfo.userId || userInfo.id)

  // 检查是否为管理员
  const isAdmin = userInfo.roles?.includes('admin') ||
                  userInfo.roles?.includes('super_admin') ||
                  userInfo.username === 'admin' ||
                  userInfo.userName === 'admin'

  console.log('👑 是否为管理员:', isAdmin)

  if (!userInfo.permissions || userInfo.permissions.length === 0) {
    console.warn('⚠️ 权限列表为空，这可能是问题所在！')
  }

  if (!userInfo.roles || userInfo.roles.length === 0) {
    console.warn('⚠️ 角色列表为空，这可能是问题所在！')
  }

  console.groupEnd()

  return {
    userInfo,
    isAdmin,
    hasPermissions: userInfo.permissions?.length > 0,
    hasRoles: userInfo.roles?.length > 0
  }
}

// 将调试函数挂载到全局对象上
if (typeof window !== 'undefined') {
  window.debugPermission = debugPermission
}
