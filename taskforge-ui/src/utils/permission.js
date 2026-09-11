/**
 * RouterVo → Vue Router 记录：组件字符串解析、别名映射。
 * 后端 component 例：Layout / system/user/index / pmhub-project/my-project
 */

const viewModules = import.meta.glob('../views/**/*.vue')

/** 库里仍是 pmhub 路径时，映射到本仓库已有页面 */
export const COMPONENT_ALIAS = {
  'pmhub-project/my-project': 'project/list/index',
  'pmhub-project/my-task': 'project/task/index',
}

/**
 * 【学员填写 R2】根据 component 字符串返回异步组件
 * @param {string} component 后端 RouterVo.component
 * @returns {() => Promise<any>}
 *
 * 规则：
 * 1) "Layout" → () => import('@/layout/index.vue')
 * 2) "ParentView" → () => import('@/layout/ParentView.vue')
 * 3) 其它：先查 COMPONENT_ALIAS，再拼 views 路径
 * 4) 找不到 → () => import('@/views/error/building.vue')
 *
 * glob 用法：
 *   const key = `../views/${path}.vue`
 *   return viewModules[key] || (() => import('@/views/error/building.vue'))
 */
export function loadView(component) {
  // ========== 【学员填写 R2】开始 ==========
  // 在下面写（写完后删掉 throw）：
  // if (component === 'Layout') return () => import('@/layout/index.vue')
  // if (component === 'ParentView') return () => import('@/layout/ParentView.vue')
  // const path = COMPONENT_ALIAS[component] || component
  // const key = `../views/${path}.vue`
  // return viewModules[key] || (() => import('@/views/error/building.vue'))
  if (component === 'Layout') return () => import('@/layout/index.vue')
  if (component === 'ParentView') return () => import('@/layout/ParentView.vue')
  const path = COMPONENT_ALIAS[component] || component
  const key = `../views/${path}.vue`
  return viewModules[key] || (() => import('@/views/error/building.vue'))

  // ========== 【学员填写 R2】结束 ==========
}

/**
 * 把后端树转成 vue-router 可用的路由数组（会改 component 为函数）。
 * 嵌套目录若仍是 Layout，改成 ParentView，避免套两层侧栏。
 */
export function filterAsyncRouter(routers, isTop = true) {
  return routers
    .filter((r) => r && !r.hidden)
    .map((route) => {
      const r = { ...route }
      if (r.meta) {
        r.meta = { ...r.meta, component: route.component }
      }
      if (r.component === 'Layout' && !isTop) {
        r.component = 'ParentView'
      }
      if (typeof r.component === 'string') {
        r.component = loadView(r.component)
      }
      if (r.children?.length) {
        r.children = filterAsyncRouter(r.children, false)
      } else {
        delete r.children
      }
      return r
    })
}
