# 前端 Sprint 03：动态路由 + 侧栏（自学版）

> **编号**：`plan/frontend/03`  
> **目标**：登录后调用 `GET /getRouters`，用后端菜单生成侧栏与 `vue-router` 动态路由，逐步替代写死的系统/项目菜单。  
> **前置**：前端 Sprint 01–02 完成（静态路由、系统管理、项目/任务页可用）。  
> **不做**：完整抄 pmhub 的所有布局变体、按钮级 `v-hasPermi`（可后置）、工作流菜单专项。  
> **协作**：permission store / 路由转换壳由助手写；**组件映射、动态 `addRoute`、侧栏渲染**等关键段由你填（搜 `【学员填写】`）。

---

## 0. 范围与进度

| 阶段 | 内容 | 状态 |
|------|------|------|
| **F03-1** | 心智：RouterVo → Vue Router / 侧栏 | **壳已说明（本文 §1–2）** |
| **F03-2** | `stores/permission.js`：拉 `getRouters`、转成路由表 | **壳已落地 · 待填 R1** |
| **F03-3** | 登录后 `addRoute` + 静态常量路由（login/home）保留 | **守卫壳已落地 · 待填 R3** |
| **F03-4** | `layout` 侧栏改为根据路由/菜单树渲染 | **SidebarItem 壳 · 待填 R4-path** |
| **F03-5** | 视图映射：`system/user/index` → `@/views/...`；缺页用占位 | **待填 R2**（`building.vue` + alias 已备） |
| **F03-6** | 自测：改菜单可见性/权限后重新登录，侧栏变化 | 待做 |

```text
登录成功
  → getInfo（已有）
  → getRouters（本 Sprint）
  → 转成 routes + 侧栏数据
  → router.addRoute(...)
  → layout 按树渲染 el-menu
```

---

## 1. 后端返回长什么样

`GET /getRouters` → `R<List<RouterVo>>`

`RouterVo` 主要字段：

| 字段 | 含义 |
|------|------|
| `name` | 路由 name |
| `path` | 路径（顶级目录常带 `/`） |
| `component` | `"Layout"` 或如 `system/user/index` |
| `hidden` | 是否侧栏隐藏 |
| `redirect` / `alwaysShow` | 目录行为 |
| `meta.title` / `meta.icon` | 侧栏标题、图标 |
| `children` | 子路由 |

目录 `menuType=M` → `component = "Layout"`；菜单 `C` → `component` 为视图路径字符串。

**注意：** 库里菜单 `component` 若仍是 pmhub 路径（如 `system/user/index`），需与 `taskforge-ui/src/views` 对齐；对不上的先映射到占位页，避免白屏。

---

## 2. 前端落位（建议）

```text
src/
  api/auth.js                 # 已有 getRouters
  stores/permission.js        # 新增：路由生成状态
  router/
    index.js                  # 常量路由 + 守卫里等动态路由就绪
    constantRoutes.js         # login / 404 等（可选拆分）
  utils/permission.js         # RouterVo → route 记录、组件 resolve
  layout/
    index.vue                 # 侧栏改递归/根据 sidebarRouters 渲染
    SidebarItem.vue           # 可选：递归菜单项
  views/...                   # 已有 system/*、project/* 供映射
```

### 守卫心智（简化版）

```text
beforeEach:
  无 token → login
  有 token 且动态路由未加载 → fetch getRouters → addRoute → next({ ...to, replace: true })
  否则 next()
```

避免：每次导航都重复 `addRoute`；刷新页面要能重新拉路由。

---

## 3. 与静态菜单的关系

| 阶段 | 做法 |
|------|------|
| F03 初期 | 常量路由保留 `home`；业务页主要靠动态路由 |
| 过渡 | 静态「系统管理/项目」可先留着对比，确认动态 OK 再删 |
| 完成 | 侧栏只渲染 `getRouters` 结果（+ 首页） |

学习项目推荐：**先并行对比 → 再删静态菜单**，出问题好回退。

---

## 4. 学员填写清单（搜 `【学员填写】`）

建议顺序：**R2 → R1 → R4-path → R3**（先能解析组件，再 addRoute，再侧栏 path，最后打开守卫）。

- [ ] **R2**：`src/utils/permission.js` → `loadView`
- [ ] **R1**：`src/stores/permission.js` → `generateRoutes`
- [ ] **R4-path**：`src/layout/SidebarItem.vue` → `resolvePath`
- [ ] **R3**：`src/router/index.js` → `beforeEach` 里真正调用 `generateRoutes`（去掉过渡期直接 `next()`）
- [ ] **自测**：Network 有 `/getRouters`；侧栏出现「系统管理」等；点「用户管理」能开页；未知菜单进占位页
- [ ] **收尾（可选）**：删掉常量路由里的 system/project 静态子路由 + layout 里静态兜底菜单

**说明：** 库里项目管理仍是 `pmhub-project/my-project` 等路径，已在 `COMPONENT_ALIAS` 映射到 `project/list`、`project/task`；未映射的 workflow 等会进 `views/error/building.vue`。

---

## 5. 验收标准

- [ ] 登录后 Network 有 `/dev-api/getRouters` 且 `code===200`
- [ ] 刷新 `/system/user` 不丢路由（守卫会补加载）
- [ ] 侧栏标题来自 `meta.title`，点击能打开对应页
- [ ] 未知 `component` 不导致整站崩溃（占位或过滤）

---

## 6. 再往后（04 候选）

- 按钮权限指令 `v-hasPermi`
- 角色分配菜单 UI 联调加深
- 文件上传页（后端 Sprint G）
