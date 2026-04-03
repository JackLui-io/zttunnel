import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { hasPermission, hasRole } from '@/utils/permission'

/**
 * 权限检查工具类
 */
export class AuthUtils {

  /**
   * 检查权限并显示提示
   * @param {string|Array} permission 权限标识
   * @param {string} message 自定义提示信息
   * @returns {boolean}
   */
  static checkPermissionWithTip(permission, message = '您没有执行此操作的权限') {
    if (!hasPermission(permission)) {
      ElMessage.error(message)
      return false
    }
    return true
  }

  /**
   * 检查角色并显示提示
   * @param {string|Array} role 角色标识
   * @param {string} message 自定义提示信息
   * @returns {boolean}
   */
  static checkRoleWithTip(role, message = '您没有执行此操作的权限') {
    if (!hasRole(role)) {
      ElMessage.error(message)
      return false
    }
    return true
  }

  /**
   * 权限确认对话框
   * @param {string|Array} permission 权限标识
   * @param {string} title 对话框标题
   * @param {string} content 对话框内容
   * @param {Function} callback 确认后的回调函数
   */
  static confirmWithPermission(permission, title, content, callback) {
    if (!hasPermission(permission)) {
      ElMessage.error('您没有执行此操作的权限')
      return
    }

    ElMessageBox.confirm(content, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      if (typeof callback === 'function') {
        callback()
      }
    }).catch(() => {
      // 用户取消操作
    })
  }

  /**
   * 获取当前用户信息
   * @returns {Object}
   */
  static getCurrentUser() {
    const userStore = useUserStore()
    return userStore.userInfo
  }

  /**
   * 获取当前用户权限列表
   * @returns {Array}
   */
  static getCurrentPermissions() {
    const userStore = useUserStore()
    return userStore.userInfo.permissions || []
  }

  /**
   * 获取当前用户角色列表
   * @returns {Array}
   */
  static getCurrentRoles() {
    const userStore = useUserStore()
    return userStore.userInfo.roles || []
  }

  /**
   * 是否为超级管理员
   * @returns {boolean}
   */
  static isSuperAdmin() {
    const roles = this.getCurrentRoles()
    return roles.includes('admin') || roles.includes('super_admin')
  }

  /**
   * 检查是否有任意一个权限
   * @param {Array} permissions 权限数组
   * @returns {boolean}
   */
  static hasAnyPermission(permissions) {
    if (!Array.isArray(permissions) || permissions.length === 0) {
      return true
    }

    const userPermissions = this.getCurrentPermissions()
    return permissions.some(permission => userPermissions.includes(permission))
  }

  /**
   * 检查是否拥有所有权限
   * @param {Array} permissions 权限数组
   * @returns {boolean}
   */
  static hasAllPermissions(permissions) {
    if (!Array.isArray(permissions) || permissions.length === 0) {
      return true
    }

    const userPermissions = this.getCurrentPermissions()
    return permissions.every(permission => userPermissions.includes(permission))
  }

  /**
   * 权限装饰器 - 用于方法权限检查
   * @param {string|Array} permission 权限标识
   * @param {string} message 提示信息
   */
  static requirePermission(permission, message) {
    return function(target, propertyKey, descriptor) {
      const originalMethod = descriptor.value

      descriptor.value = function(...args) {
        if (!hasPermission(permission)) {
          ElMessage.error(message || '您没有执行此操作的权限')
          return
        }
        return originalMethod.apply(this, args)
      }

      return descriptor
    }
  }
}

/**
 * 权限检查装饰器
 * 使用方式：@requirePermission('system:user:add')
 */
export function requirePermission(permission, message) {
  return AuthUtils.requirePermission(permission, message)
}

/**
 * 快捷权限检查方法
 */
export const auth = {
  check: AuthUtils.checkPermissionWithTip,
  checkRole: AuthUtils.checkRoleWithTip,
  confirm: AuthUtils.confirmWithPermission,
  hasAny: AuthUtils.hasAnyPermission,
  hasAll: AuthUtils.hasAllPermissions,
  isSuperAdmin: AuthUtils.isSuperAdmin,
  user: AuthUtils.getCurrentUser,
  permissions: AuthUtils.getCurrentPermissions,
  roles: AuthUtils.getCurrentRoles
}
