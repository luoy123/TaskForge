# 前端 Sprint 03：动态路由 + 侧栏（自学版）

> **编号**：`plan/frontend/03`  
> **状态**：**当前焦点**（01 / 02 已完成）  
> **目标**：登录后调用 `GET /getRouters`，用后端菜单生成侧栏与 `vue-router` 动态路由，逐步替代写死的系统/项目菜单。  
> **前置**：前端 Sprint 01–02 完成（静态路由、系统管理、项目/任务页可用）。  
> **不做**：完整抄 pmhub 的所有布局变体、按钮级 `v-hasPermi`（可后置）、工作流菜单专项。

---

## 协作分工（先看这里）

| 谁 | 做什么 |
|----|--------|
| **助手（已完成）** | permission store / `filterAsyncRouter` / 路由守卫壳 / SidebarItem 递归壳 / Layout 动态侧栏挂载 / `building` 占位 / pmhub 路径别名 |
| **你（本 Sprint）** | 搜 `【学员填写】`，按顺序填 **R2 → R1 → R4-path → R3** |

完成后：Network 能看到 `/getRouters`，侧栏标题来自后端 `meta.title`，点菜单能进对应页；未知菜单进「建设中」。

---

## 0. 范围与进度

| 阶段 | 内容 | 状态 |
|------|------|------|
| **F03-1** | 心智：RouterVo → Vue Router / 侧栏 | **已说明（本文 §1–2）** |
| **F03-2** | `stores/permission.js`：拉 `getRouters`、转成路由表 | **R1 已填** |
| **F03-3** | 登录后 `addRoute` + 静态常量路由（login/home）保留 | **R3 已填** |
| **F03-4** | `layout` 侧栏改为根据路由/菜单树渲染 | **R4-path 已填** |
| **F03-5** | 视图映射：`system/user/index` → `@/views/...`；缺页用占位 | **R2 已填** |
| **F03-6** | 自测：改菜单可见性/权限后重新登录，侧栏变化 | **通过**（2026-09-11） |

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

**注意：** 库里菜单 `component` 若仍是 pmhub 路径（如 `pmhub-project/my-project`），已在 `COMPONENT_ALIAS` 映射到本仓库页面；对不上的落到 `views/error/building.vue`，避免白屏。

可先用接口确认（带 Token）：

```bash
curl -s http://localhost:1234/getRouters -H "Authorization: Bearer <token>"
```

期望：`code === 200`，`data` 是菜单树数组。

---

## 2. 前端落位（助手已搭好）

```text
src/
  api/auth.js                 # getRouters 已有
  stores/permission.js        # generateRoutes【学员填写 R1】
  utils/permission.js         # loadView【学员填写 R2】+ filterAsyncRouter（助手）
  router/index.js             # 常量路由 + beforeEach【学员填写 R3】
  layout/
    index.vue                 # 动态侧栏 + 静态兜底（过渡）
    SidebarItem.vue           # 递归菜单【学员填写 R4-path】
    ParentView.vue            # 嵌套目录用
  views/error/building.vue    # 未知 component 占位
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
| F03 初期 | 常量路由保留 `home` + system/project 兜底；业务页仍可静态打开 |
| 过渡 | 静态「系统管理/项目」与动态侧栏并行对比（`sidebarRouters` 为空时才显示静态兜底） |
| 完成 | 侧栏只渲染 `getRouters`；可选删掉常量路由里的 system/project 子路由 |

学习项目推荐：**先并行对比 → 再删静态菜单**，出问题好回退。

---

## 4. 学员填写清单（搜 `【学员填写】`）

建议顺序：**R2 → R1 → R4-path → R3**（先能解析组件，再 addRoute，再侧栏 path，最后打开守卫）。

| 标记 | 文件 | 你要做什么 | 状态 |
|------|------|------------|------|
| **R2** | `src/utils/permission.js` → `loadView` | Layout / ParentView / alias / glob / building | 已填 |
| **R1** | `src/stores/permission.js` → `generateRoutes` | getRouters → sidebar → filterAsyncRouter → addRoute | 已填 |
| **R4-path** | `src/layout/SidebarItem.vue` → `resolvePath` | 拼 el-menu 的完整 path | 已填 |
| **R3** | `src/router/index.js` → `beforeEach` | 调 `generateRoutes`，去掉过渡期直接 `next()` | 已填 |

### 进度勾选

- [x] **R2** `loadView`
- [x] **R1** `generateRoutes`
- [x] **R4-path** `resolvePath`
- [x] **R3** 守卫真正加载动态路由
- [x] **自测**：Network 有 `/getRouters`；侧栏出现「系统管理」等；点「用户管理」能开页；未知菜单进占位页
- [ ] **收尾（可选）**：删掉常量路由里的 system/project 静态子路由 + layout 里静态兜底菜单

**自测记录（2026-09-11 Playwright）**

- 登录 → `/dev-api/getRouters` 200；侧栏出现项目管理/系统管理等（静态兜底已消失）
- 点「用户管理」→ `/system/user` 列表正常；刷新后再次拉 getRouters，页面不丢
- 「岗位管理」→ 建设中占位（`system/post/index`）
- 「我的项目」→ 别名映射到项目列表页，表格有数据
- 未映射菜单（流程/监控等）进侧栏但不白屏

**说明：** 库里项目管理仍是 `pmhub-project/my-project` 等路径，已在 `COMPONENT_ALIAS` 映射到 `project/list`、`project/task`；未映射的 workflow 等会进 `views/error/building.vue`。

### 自测步骤（填完后）

1. 起后端 `:1234` + `npm run dev`
2. 登录 → Network 应有 `/dev-api/getRouters`，`code===200`
3. 侧栏出现后端菜单标题（不再只靠静态兜底）
4. 点「用户管理」等 → 中间区域正常
5. 刷新当前业务 URL（如 `/system/user`）→ 不丢路由、不白屏
6. 退出再登录 → 路由会重新生成（`resetRoutes` 已接在 logout）

---

## 5. 验收标准

- [x] 登录后 Network 有 `/dev-api/getRouters` 且 `code===200`
- [x] 刷新 `/system/user` 不丢路由（守卫会补加载）
- [x] 侧栏标题来自 `meta.title`，点击能打开对应页
- [x] 未知 `component` 不导致整站崩溃（占位或过滤）

---

## 6. 再往后（04 候选）

- 按钮权限指令 `v-hasPermi`
- 角色分配菜单 UI 联调加深
- 文件上传页（后端 Sprint G）
- 删掉 F03 过渡期静态 system/project 路由与兜底菜单
