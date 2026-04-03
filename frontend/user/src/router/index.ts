import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import SupervisoryControl from '../views/SupervisoryControl.vue'
import StatisticalAnalysis from '../views/StatisticalAnalysis.vue'
import EquipmentManagement from '../views/EquipmentManagement.vue'
import ReportGeneration from '../views/ReportGeneration.vue'
import DataScreen from '../views/DataScreen.vue'

import MainLayout from '../components/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { public: true },
    },
    {
      path: '/dataScreen',
      name: 'dataScreen',
      component: DataScreen,
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: Dashboard,
        },
        {
          path: 'supervisoryControl',
          name: 'supervisoryControl',
          component: SupervisoryControl,
        },
        {
          path: 'statisticalAnalysis',
          name: 'statisticalAnalysis',
          component: StatisticalAnalysis,
        },
        {
          path: 'equipmentManagement',
          name: 'equipmentManagement',
          component: EquipmentManagement,
        },
        {
          path: 'reportGeneration',
          name: 'reportGeneration',
          component: ReportGeneration,
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const token = getToken()
  const isPublic = to.meta.public === true
  if (!token && !isPublic) {
    return { path: '/login', replace: true }
  }
  if (token && to.path === '/login') {
    return { path: '/dashboard', replace: true }
  }
  // 参考原前端：有 token 时先拉取用户信息再放行，避免顶栏用户名显示「用户」
  if (token && !isPublic) {
    const authStore = useAuthStore()
    if (!authStore.user) {
      await authStore.fetchUserInfo()
    }
  }
  return true
})

export default router
