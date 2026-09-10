# 前端 Sprint 02：项目列表 + 任务列表（自学版）

> **编号**：`plan/frontend/02`（前端独立编号，不与后端抢号）。  
> **目标**：在 `taskforge-ui` 落地「我的项目」列表与「项目下任务」最小闭环，对接后端 Sprint E/F 接口。  
> **前置**：前端 Sprint 01（登录、静态菜单、系统管理 CRUD）已完成。  
> **不做**：动态路由、文件上传/导入导出 UI、成员邀请、收藏/归档完整运营能力（可后置）。  
> **协作**：API + 页面壳由助手写；**列表/提交/删除等关键逻辑**由你填（搜 `【学员填写】`）。

---

## 0. 范围与进度

| 阶段 | 内容 | 状态 |
|------|------|------|
| **F02-1** | 菜单 + 路由：`/project/list`、`/project/task` | **完成** |
| **F02-2** | 项目列表：分页查询 / 新增 / 编辑 / 删除 | **完成** |
| **F02-3** | 任务列表：按 `projectId` 查询 / 新增 / 编辑 / 删除 | **完成** |
| **F02-4** | 自测联调 | **通过**（2026-09-10：API 全绿；UI 列表/新增项目/进任务/新增任务/无 id 提示通过。删项目若仍有任务需先删任务） |

```text
侧栏「项目管理」
  ├── 项目列表  /project/list
  └── 任务列表  /project/task?projectId=xxx   （也可从项目行「任务」跳入）
```

---

## 1. 后端接口（注意：多为 POST + Body）

与系统管理（GET query）不同，项目/任务接口以 **POST JSON** 为主。

### 项目 `/project`

| 动作 | 方法 | 路径 | Body 要点 |
|------|------|------|-----------|
| 列表 | POST | `/project/list` | `{ pageNum, pageSize, keyword? }` → `R<Page>`，`records`/`total` |
| 新增 | POST | `/project/add` | `Project`（至少 `projectName`） |
| 编辑 | POST | `/project/edit` | 含 `id`/`projectId` 等后端实体字段 |
| 删除 | DELETE | `/project/delete` | `{ projectId }` |
| 详情 | POST | `/project/detail` | `{ projectId }`（本 Sprint 可后置） |

列表出参字段参考：`projectId`、`projectName`、`status`/`statusName`、`stageName`、`nickName`、`createdTime` 等（`ProjectResVO`）。

### 任务 `/project/task`

| 动作 | 方法 | 路径 | Body 要点 |
|------|------|------|-----------|
| 列表 | POST | `/project/task/list` | `{ projectId, pageNum, pageSize }` |
| 新增 | POST | `/project/task/add` | `{ projectId, taskName, ... }` |
| 编辑 | POST | `/project/task/edit` | `{ taskId, taskName, ... }` |
| 删除 | POST | `/project/task/delete` | `{ taskId }` |

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/project/
    project.js
    task.js
  views/project/
    list/index.vue      # 项目列表
    task/index.vue      # 某项目下的任务
  router/index.js       # 增加两条 children
  layout/index.vue      # 侧栏「项目管理」分组
```

### axios 注意

```js
// 系统用户：GET + params
request.get('/system/user/list', { params })

// 项目列表：POST + data
request.post('/project/list', { pageNum, pageSize, keyword })
```

---

## 3. 学员填写清单

### 项目页 `views/project/list/index.vue`

- [ ] **P-C** `getList`：`listProject(query)` → `records` / `total`
- [ ] **P-D** `submitForm`：有 id 则 `editProject`，否则 `addProject`
- [ ] **P-E** `handleDelete`：`delProject({ projectId })`
- [ ] **P-T**（可选）跳转任务：`router.push({ path: '/project/task', query: { projectId } })`

### 任务页 `views/project/task/index.vue`

- [ ] **T-C** `getList`：必须带 `route.query.projectId`
- [ ] **T-D** `submitForm`：新增时写入 `projectId`
- [ ] **T-E** `handleDelete`

### 进度勾选

- [x] F02-1 菜单路由可点开两页
- [x] F02-2 项目 CRUD 联调通过
- [x] F02-3 任务 CRUD 联调通过（指定 projectId）
- [x] F02-4 自测：无 projectId 进任务页有友好提示

---

## 4. 验收标准

- [ ] 侧栏能进项目列表，表格有后端数据
- [ ] 能新增/编辑/删除项目（勿乱删生产重要数据；学习库随意）
- [ ] 从项目点「任务」进入任务页，能增删改任务
- [ ] 请求走 `/dev-api`，业务 `code === 200`

---

## 5. 再往后（前端 03 候选）

- `getRouters` 动态菜单  
- 项目归档/收藏、任务评论  
- 文件上传 UI（对照后端 Sprint G）
