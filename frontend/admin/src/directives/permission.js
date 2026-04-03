import { hasPermission, hasRole } from '@/utils/permission'

/**
 * 权限指令
 * 使用方式：v-permission="'system:user:add'"
 * 或：v-permission="['system:user:add', 'system:user:edit']"
 */
export const permission = {
  mounted(el, binding) {
    const { value } = binding

    if (value && !hasPermission(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  },
  updated(el, binding) {
    const { value, oldValue } = binding

    if (value !== oldValue) {
      if (value && !hasPermission(value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

/**
 * 角色指令
 * 使用方式：v-role="'admin'"
 * 或：v-role="['admin', 'user']"
 */
export const role = {
  mounted(el, binding) {
    const { value } = binding

    if (value && !hasRole(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  },
  updated(el, binding) {
    const { value, oldValue } = binding

    if (value !== oldValue) {
      if (value && !hasRole(value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

/**
 * 权限显示/隐藏指令（不删除DOM元素）
 * 使用方式：v-permission-show="'system:user:add'"
 */
export const permissionShow = {
  mounted(el, binding) {
    const { value } = binding

    if (value && !hasPermission(value)) {
      el.style.display = 'none'
    }
  },
  updated(el, binding) {
    const { value, oldValue } = binding

    if (value !== oldValue) {
      if (value && !hasPermission(value)) {
        el.style.display = 'none'
      } else {
        el.style.display = ''
      }
    }
  }
}

// 注册所有权限指令
export default {
  install(app) {
    app.directive('permission', permission)
    app.directive('role', role)
    app.directive('permission-show', permissionShow)
  }
}
