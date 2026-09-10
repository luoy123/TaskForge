import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getRouters } from '@/api/auth'
import { filterAsyncRouter } from '@/utils/permission'
import router from '@/router'

export const usePermissionStore = defineStore('permission', () => {
  /** 侧栏用的菜单树（后端 RouterVo） */
  const sidebarRouters = ref([])
  /** 已 addRoute 的顶层路由 name，退出时 remove */
  const addedRouteNames = ref([])
  const routesLoaded = ref(false)

  /**
   * 【学员填写 R1】拉 getRouters → 转换 → 写入 sidebar → addRoute
   *
   * 步骤：
   * 1) const { data: res } = await getRouters()
   * 2) const raw = res.data || []
   * 3) sidebarRouters.value = raw
   * 4) const accessRoutes = filterAsyncRouter(raw)
   * 5) accessRoutes.forEach((r) => { router.addRoute(r); if (r.name) addedRouteNames.value.push(r.name) })
   * 6) routesLoaded.value = true
   * 7) return accessRoutes
   */
  async function generateRoutes() {
    // ========== 【学员填写 R1】开始 ==========
    // 在下面写：
    const { data: res } = await getRouters()
    const raw = res.data || []
    sidebarRouters.value = raw
    const accessRoutes = filterAsyncRouter(raw)
    accessRoutes.forEach((r) => {
      router.addRoute(r); if (r.name) addedRouteNames.value.push(r.name)
    })
    routesLoaded.value = true
    return accessRoutes
    // ========== 【学员填写 R1】结束 ==========
  }

  function resetRoutes() {
    addedRouteNames.value.forEach((name) => {
      if (router.hasRoute(name)) {
        router.removeRoute(name)
      }
    })
    addedRouteNames.value = []
    sidebarRouters.value = []
    routesLoaded.value = false
  }

  return { sidebarRouters, addedRouteNames, routesLoaded, generateRoutes, resetRoutes }
})
