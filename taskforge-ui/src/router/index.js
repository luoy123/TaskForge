import { createRouter, createWebHistory } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'

/**
 * 常量路由：始终存在。
 * F03 过渡：业务页仍暂时挂在这里，等动态路由联调通过后再删 system/project 子路由。
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
      // —— F03 静态兜底（动态 OK 后可删）——
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理' },
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理' },
      },
      {
        path: 'project/list',
        name: 'ProjectList',
        component: () => import('@/views/project/list/index.vue'),
        meta: { title: '项目列表' },
      },
      {
        path: 'project/task',
        name: 'ProjectTask',
        component: () => import('@/views/project/task/index.vue'),
        meta: { title: '任务列表' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
})

/**
 * 【学员填写 R3】有 token 且动态路由未加载时：generateRoutes，再 next({ ...to, replace: true })
 * 注意：不要在每次导航都 addRoute；失败时别死循环。
 */
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

  // ========== 【学员填写 R3】开始 ==========
  // 提示：
  // try {
  //   const { useUserStore } = await import('@/stores/user')
  //   const userStore = useUserStore()
  //   if (!userStore.name) await userStore.fetchUserInfo()
  //   await permissionStore.generateRoutes()
  //   next({ ...to, replace: true })
  // } catch (e) {
  //   permissionStore.resetRoutes()
  //   localStorage.removeItem('Admin-Token')
  //   next('/login')
  // }
  //
  // 过渡期：先放行静态路由，避免 R1 未填时整站进不去。填完 R1/R3 后改为上面逻辑。
  next()
  // ========== 【学员填写 R3】结束 ==========
})

export default router
