import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import { checkRoutePermission } from '@/utils/permission'
import { ElMessage } from 'element-plus'
import { setTemplateParamContextId } from '@/utils/templateParamContext'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页', icon: 'House' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Cpu' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/UserManagement.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          permission: 'system:user:list'
        }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/RoleManagement.vue'),
        meta: {
          title: '角色管理',
          icon: 'UserFilled',
          permission: 'system:role:list'
        }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/DeptManagement.vue'),
        meta: {
          title: '部门管理',
          icon: 'OfficeBuilding',
          permission: 'system:dept:list'
        }
      },
      {
        path: 'scheduler',
        name: 'DataAggregation',
        component: () => import('@/views/system/scheduler/DataAggregation.vue'),
        meta: {
          title: '数据汇总',
          icon: 'DataAnalysis',
          role: 'admin'
        }
      }
    ]
  },
  {
    path: '/company',
    component: Layout,
    redirect: '/company/list',
    meta: { title: '公司管理', icon: 'Briefcase' },
    children: [
      {
        path: 'list',
        name: 'CompanyList',
        component: () => import('@/views/tunnel/company/TunnelCompanyList.vue'),
        meta: {
          title: '公司列表',
          icon: 'OfficeBuilding',
          permission: 'tunnel:list:view'
        }
      }
    ]
  },
  {
    path: '/tunnel',
    component: Layout,
    redirect: '/tunnel/list',
    meta: { title: '隧道管理', icon: 'Connection' },
    children: [
      {
        path: 'list',
        name: 'TunnelList',
        component: () => import('@/views/tunnel/list/TunnelList.vue'),
        meta: {
          title: '隧道列表',
          icon: 'List',
          permission: 'tunnel:list:view'
        }
      },
      {
        path: 'device-bind',
        name: 'TunnelDeviceBind',
        component: () => import('@/views/tunnel/bind/DeviceBindWorkspace.vue'),
        meta: {
          title: '设备绑定',
          icon: 'Link',
          permission: 'tunnel:param:view',
          hidden: true
        }
      },
      {
        path: 'param/edit/:id',
        name: 'TunnelParamTemplateEditLegacy',
        meta: { hidden: true },
        beforeEnter: (to) => {
          setTemplateParamContextId(to.params.id)
          return { path: '/tunnel/param/edit', replace: true }
        }
      },
      {
        path: 'param/detail/:id',
        name: 'TunnelParamTemplateDetailLegacy',
        meta: { hidden: true },
        beforeEnter: (to) => {
          setTemplateParamContextId(to.params.id)
          return { path: '/tunnel/param/detail', replace: true }
        }
      },
      {
        path: 'param/edit',
        name: 'TunnelParamTemplateEdit',
        component: () => import('@/views/tunnel/template/TunnelTemplateEdit.vue'),
        meta: {
          title: '模板编辑',
          hidden: true,
          activeMenu: '/tunnel/param',
          permission: 'tunnel:param:view'
        }
      },
      {
        path: 'param/detail',
        name: 'TunnelParamTemplateDetail',
        component: () => import('@/views/tunnel/template/TunnelTemplateDetail.vue'),
        meta: {
          title: '模板详情',
          hidden: true,
          activeMenu: '/tunnel/param',
          permission: 'tunnel:param:view'
        }
      },
      {
        path: 'param',
        name: 'TunnelParamTemplateList',
        component: () => import('@/views/tunnel/template/TunnelTemplateList.vue'),
        meta: {
          title: '模板列表',
          icon: 'DocumentCopy',
          permission: 'tunnel:param:view'
        }
      },
      {
        path: 'ota',
        name: 'OtaManagement',
        component: () => import('@/views/tunnel/ota/OtaManagement.vue'),
        meta: {
          title: 'OTA管理',
          icon: 'Upload',
          permission: 'tunnel:ota:view'
        }
      }
    ]
  },
  {
    path: '/device',
    component: Layout,
    redirect: '/device/list',
    meta: { title: '设备管理', icon: 'Monitor' },
    children: [
      {
        path: 'list',
        name: 'DeviceList',
        component: () => import('@/views/device/list/DeviceList.vue'),
        meta: {
          title: '设备列表',
          icon: 'List',
          permission: 'device:list:view'
        }
      },
      {
        path: 'check',
        name: 'DeviceCheck',
        component: () => import('@/views/device/check/DeviceCheck.vue'),
        meta: {
          title: '设备检测',
          icon: 'Search',
          permission: 'device:check:view',
          hidden: true // 侧栏暂不展示，需恢复时去掉此项
        }
      }
    ]
  },
  {
    path: '/power',
    component: Layout,
    redirect: '/power/monitor',
    meta: { title: '电能管理', icon: 'Lightning' },
    children: [
      {
        path: 'monitor',
        name: 'PowerMonitor',
        component: () => import('@/views/power/monitor/PowerMonitor.vue'),
        meta: {
          title: '电能监测',
          icon: 'Monitor',
          permission: 'power:monitor:view'
        }
      },
      {
        path: 'config',
        name: 'PowerConfig',
        component: () => import('@/views/power/config/PowerConfig.vue'),
        meta: {
          title: '电表配置',
          icon: 'Setting',
          permission: 'power:config:view'
        }
      },
      {
        path: 'vendor',
        name: 'PowerVendor',
        component: () => import('@/views/power/vendor/VendorConfig.vue'),
        meta: {
          title: '厂商配置',
          icon: 'Shop',
          permission: 'power:vendor:view'
        }
      }
    ]
  },
  {
    path: '/carbon',
    component: Layout,
    redirect: '/carbon/data',
    meta: { title: '能碳管理', icon: 'Opportunity' },
    children: [
      {
        path: 'data',
        name: 'CarbonData',
        component: () => import('@/views/carbon/data/CarbonData.vue'),
        meta: {
          title: '能碳数据',
          icon: 'DataAnalysis',
          permission: 'carbon:data:view'
        }
      },
      {
        path: 'statistics',
        name: 'CarbonStatistics',
        component: () => import('@/views/carbon/statistics/CarbonStatistics.vue'),
        meta: {
          title: '统计分析',
          icon: 'TrendCharts',
          permission: 'carbon:statistics:view'
        }
      }
    ]
  },
  {
    path: '/monitor',
    component: Layout,
    redirect: '/monitor/realtime',
    meta: { title: '监控管理', icon: 'View' },
    children: [
      {
        path: 'realtime',
        name: 'RealtimeMonitor',
        component: () => import('@/views/monitor/realtime/RealtimeMonitor.vue'),
        meta: {
          title: '实时监控',
          icon: 'Monitor',
          permission: 'monitor:realtime:view'
        }
      },
      {
        path: 'traffic',
        name: 'TrafficMonitor',
        component: () => import('@/views/monitor/traffic/TrafficMonitor.vue'),
        meta: {
          title: '车流监控',
          icon: 'Van',
          permission: 'monitor:traffic:view'
        }
      },
      {
        path: 'brightness',
        name: 'BrightnessMonitor',
        component: () => import('@/views/monitor/brightness/BrightnessMonitor.vue'),
        meta: {
          title: '亮度监控',
          icon: 'Sunny',
          permission: 'monitor:brightness:view'
        }
      }
    ]
  },
  {
    path: '/notice',
    component: Layout,
    redirect: '/notice/list',
    meta: { title: '通知管理', icon: 'Bell' },
    children: [
      {
        path: 'list',
        name: 'NoticeList',
        component: () => import('@/views/notice/list/NoticeList.vue'),
        meta: {
          title: '通知列表',
          icon: 'List',
          permission: 'notice:list:view'
        }
      }
    ]
  },
  {
    path: '/log',
    component: Layout,
    redirect: '/log/operation',
    meta: { title: '日志管理', icon: 'Document' },
    children: [
      {
        path: 'operation',
        name: 'OperationLog',
        component: () => import('@/views/log/operation/OperationLog.vue'),
        meta: {
          title: '操作日志',
          icon: 'EditPen',
          permission: 'log:operation:view'
        }
      },
      {
        path: 'login',
        name: 'LoginLog',
        component: () => import('@/views/log/login/LoginLog.vue'),
        meta: {
          title: '登录日志',
          icon: 'Key',
          permission: 'log:login:view'
        }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    redirect: '/profile/index',
    meta: { title: '个人中心', hidden: true },
    children: [
      {
        path: 'index',
        name: 'UserProfile',
        component: () => import('@/views/profile/UserProfile.vue'),
        meta: { title: '个人中心', icon: 'User' }
      }
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  // 添加调试日志
  console.log('路由守卫执行:', {
    to: to.path,
    from: from.path,
    token: token ? `存在token: ${token.substring(0, 20)}...` : '无token',
    tokenLength: token ? token.length : 0
  })

  if (to.path === '/login') {
    if (token && token.trim() !== '' && token !== 'null' && token !== 'undefined') {
      console.log('已有有效token，跳转到首页')
      next('/')
    } else {
      console.log('无有效token，进入登录页')
      next()
    }
  } else {
    if (token && token.trim() !== '' && token !== 'null' && token !== 'undefined') {
      console.log('有token，检查用户信息和权限')
      // 如果有token但用户信息为空，则获取用户信息
      const { useUserStore } = await import('@/stores/user')
      const userStore = useUserStore()

      if (!userStore.userInfo?.userId) {
        console.log('用户信息为空，获取用户信息')
        try {
          await userStore.getUserInfo()
          console.log('用户信息获取成功:', userStore.userInfo)
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，清除token并跳转到登录页
          localStorage.removeItem('token')
          console.log('清除无效token，跳转登录页')
          next('/login')
          return
        }
      }

      // 检查路由权限
      if (!checkRoutePermission(to)) {
        console.log('无权限访问:', to.path)
        ElMessage.error('您没有访问该页面的权限')
        next('/dashboard')
        return
      }

      console.log('权限检查通过，允许访问')
      next()
    } else {
      console.log('无token，跳转登录页')
      next('/login')
    }
  }
})

export default router
