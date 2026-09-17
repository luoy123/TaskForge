import { createRouter, createWebHistory } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'

/**
 * 常量路由：只保留公开页 + 布局壳 + 首页。
 * 业务页全部走 getRouters 动态挂载（F04-2）。
 */
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    name: 'RootLayout',
    component: () => import('@/layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
})

router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('Admin-Token')
  if (to.meta.public) {
    next()
    return
  }
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  const permissionStore = usePermissionStore()
  if (permissionStore.routesLoaded) {
    next()
    return
  }

  try {
    const { useUserStore } = await import('@/stores/user')
    const userStore = useUserStore()
    if (!userStore.name) await userStore.fetchUserInfo()
    await permissionStore.generateRoutes()
    next({ ...to, replace: true })
  } catch (e) {
    permissionStore.resetRoutes()
    localStorage.removeItem('Admin-Token')
    next('/login')
  }
})

export default router
