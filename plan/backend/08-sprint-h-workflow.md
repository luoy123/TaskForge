# Sprint H：Flowable 工作流 + 项目/任务审批联动（自学版）

> **状态**：✅ **主路径已落地**（尚余 H4 收尾项，见 §0）  
> **目标**：新建 `taskforge-workflow`，接入 Flowable；打通「发起项目/任务审批 → 待办办理 → 回写状态」；审批中禁止手改状态（对齐 pmhub `approved`）。  
> **前置**：Sprint E–G（项目 / 任务 / 文件主路径）。  
> **不做**：企微待办、完整表单设计器、RocketMQ、Seata、微服务拆分、前端待办页（本 Sprint 只保证 API + curl/Swagger 可验；UI → [frontend/05](../frontend/05-sprint-workflow-ui.md)）。

---

## 0. 进度与范围

| 阶段 | 状态 |
|------|------|
| Sprint A–G | ✅ |
| **Sprint H** | **H1–H3 / H5 ✅；H4 大半 ✅，见下方缺口** |
| 再往后 | [Sprint I](./09-sprint-i-job-stats.md) ✅ I1–I5a → [Sprint J](./10-sprint-j-system-guard.md) ○ |

```text
H1  Maven：taskforge-workflow + Flowable 配置     ✅
H2  内置 bpmn + deployBuiltin / latestDefinition ✅
H3  运行时：start / todo / finished / complete / reject  ✅
H4  与 project 联动                              ⚠ 大半有，见缺口
H5  Controller + 权限字                          ✅（挂在 /workflow/*）
```

**代码落位（对照仓库）：**

| 能力 | 位置 | 现状 |
|------|------|------|
| 模块 / `FlowableConfig` | `taskforge-workflow` | ✅ |
| 内置 BPMN 部署 | `WfProcessServiceImpl#deployBuiltinTaskApprove` + `resources/bpmn/task-approve.bpmn20.xml` | ✅ |
| start / todo / finished | `WfProcessServiceImpl` + `WfProcessController` | ✅ |
| complete / reject / startFirstTask | `WfTaskServiceImpl` + `WfTaskController` | ✅ |
| `startTaskApprove` / `startProjectApprove` | 启流 + 写 `pmhub_project_task_process`（`approved=0`） | ✅ |
| 任务 `edit` 审批中禁改状态 | `ProjectTaskServiceImpl#edit` + `selectApproved` | ✅ |
| complete/reject 回写 `approved` | complete→`"1"`；reject→`"2"` 并清 instance | ✅ |
| 分类 / 模型 CRUD | — | ○ 计划已砍（MVP 不写） |
| **项目** `edit` 审批中禁改 | `ProjectServiceImpl` | ⬜ 未做 |
| 启流前校验任务/项目存在 | `start*Approve` | ⬜ MVP 跳过（注释写明） |
| 通过/驳回回写业务 **status** | 任务/项目实体状态字段 | ⬜ 只动关联表 `approved`，未改业务状态 |

对照习惯：左边 pmhub，右边 TaskForge；返回继续 `R<T>`。  
编译：`mvn -pl taskforge-admin -am -DskipTests compile`

---

## 0.1 模块与依赖

```text
pmhub：
  pmhub-workflow  → Flowable 封装
  pmhub-project   → 依赖 workflow（启动审批）
  pmhub-admin     → Wf* + Project* Controller

TaskForge：
  taskforge-workflow  → 只依赖 taskforge-common（+ Flowable）
  taskforge-project   → 尽量不直接依赖 workflow 实现类
                        推荐：project 只写「审批意图」接口 / 事件；
                        或 admin 编排：调 project 校验 → 调 workflow 启流 → 回写 project
  taskforge-admin     → 依赖 workflow + project，挂 Controller
```

**依赖方向建议（避免 project ↔ workflow 环）：**

```text
common ← workflow
common ← project
common ← system ← framework ← admin
                 ↑            ↑
              project      workflow
```

若必须在 `ProjectTaskServiceImpl.edit` 里查「是否在审」，可：
- project 内读 `pmhub_project_task_process`（表属项目域），或
- 抽极薄 `IApprovalQuery` 接口放 common，workflow 实现、project 注入接口。

---

## 1. 领域心智

```text
部署定义（BPMN）
  └── 流程实例 processInstanceId
        └── 用户任务 task（待办）
              complete / reject → 监听或业务回调
                    └── 回写 Project / ProjectTask 状态 + approved
```

与项目联动表（复用库内 `pmhub_project_task_process` 等，字段以 SQL 为准）：

| 概念 | 作用 |
|------|------|
| process 关联 | 业务 id（项目/任务）↔ Flowable `processInstanceId` |
| `approved` | 0 审批中不可手改状态；≠0 可按原规则编辑 |
| 启动入口 | `startProjectApprove` / `startTaskApprove` |

对照 pmhub：`WfTaskServiceImpl.complete` / `claim`；`ProjectTaskServiceImpl.edit` 对 `approved` 的校验。

---

## 2. H1 — 模块骨架 + Flowable 配置

### 落位

```text
taskforge-workflow/
  pom.xml
  config/FlowableConfig.java      # 对照 pmhub：databaseSchemaUpdate、异步、IDM
  ...

父 pom <modules> 增加 workflow；admin 增加依赖。
```

### 决策（写进计划，实现时二选一写死）

| 项 | 建议 |
|----|------|
| 表前缀 | Flowable 默认 `ACT_*`；业务自定义表继续 `pmhub_wf_*` 或缩成 `tf_wf_*`（计划里显式选一种） |
| schemaUpdate | 本地 `true`；文档注明生产勿盲目开 |
| 身份 | 关掉 Flowable IDM，用系统 `sys_user` / 当前登录用户 id 当 assignee |

### 验收

- [x] 启动 admin 无报错，库中出现 Flowable 表（或确认已有）
- [x] `mvn -pl taskforge-admin -am -DskipTests compile` 通过

---

## 3. H2 — 分类 / 模型 / 部署（最小）

对照 pmhub：`WfCategory` / `WfModel` / `WfDeploy`（不必一次抄全字段）。

| API（建议前缀） | 能力 | 现状 |
|-----------------|------|------|
| `/workflow/category/*` | 分类 CRUD | ○ 已砍 |
| `/workflow/model/*` | 模型列表 / 保存 / 部署 | ○ 已砍 |
| `/workflow/process/deployBuiltin` | 部署内置 BPMN | ✅ |
| `/workflow/process/definition/{key}` | 查最新定义 | ✅ |

**MVP 已采用**：仓库放 `resources/bpmn/task-approve.bpmn20.xml`，管理接口「部署内置定义」。

### 验收

- [x] 能部署至少 1 个流程定义，`repositoryService` 能查到
- [ ] 分类或模型有一条可查数据（若做了 CRUD）— **不做**

---

## 4. H3 — 启动实例 + 待办办理

| API | 能力 | 现状 |
|-----|------|------|
| `GET .../todoList` | 当前用户待办 | ✅ |
| `GET .../finishedList` | 已办（可简化） | ✅ |
| `POST .../complete` | 通过 | ✅ |
| `POST .../reject` | 驳回：结束实例 + `approved=2` | ✅ |
| `POST .../start` | 按 key 纯 Flowable 启流 | ✅ |

assignee：用 **登录用户 id 字符串**；BPMN 变量 `approver` 指定审批人。

### 验收（curl）

- [x] 代码路径具备：启流后指定办理人待办有数据
- [x] complete 后待办消失；关联表 `approved` 回写（业务 status 回写见 H4 缺口）

---

## 5. H4 — 与 project 联动（本 Sprint 核心）

### 必须做（对照代码）

1. ✅ 实体/Mapper：`WfTaskProcess` → `pmhub_project_task_process`
2. ✅ `startTaskApprove(taskId, approver)`：启流 → 写关联 → `approved=0`（**未**校验任务实体存在）
3. ✅ `startProjectApprove(projectId, approver)`：同构，`type=project`
4. ⚠ `edit`：任务改状态时若 `approved=0` 拒绝 ✅；**项目**侧未做 ⬜
5. ⚠ 审批通过/驳回：只更新关联表 `approved`（`1`/`2`）✅；**未**回写任务/项目业务 `status` ⬜

### 明确不做（H4）

- 审批附件与文件模块深度绑定  
- 多级会签 / 并行网关复杂图（bpmn 保持线性：提交 → 审批人 → 结束）

### 验收

- [x] 任务启动审批后，直接 `edit` 改状态失败  
- [ ] complete 通过后**业务状态**按约定更新，并可再编辑（目前仅 `approved` 放开，业务 status 未自动改）  
- [x] 关联表能查到 `instanceId` ↔ `extraId`（task/project）

### H4 收尾建议（若继续补）

```text
1. ProjectServiceImpl.edit：改 status 时查 selectApproved(projectId, "project")
2. complete / reject 末尾：按 type 更新 ProjectTask / Project 的 status（约定写死一种即可）
3. （可选）start*Approve 前校验业务实体存在（需 admin 编排或薄查询接口，避免 project↔workflow 环）
```

---

## 6. H5 — Controller 落位与权限

```text
taskforge-admin/
  web/controller/workflow/
    WfProcessController.java   # deployBuiltin / definition / start / todo / finished  ✅
    WfTaskController.java      # complete / reject / startTaskApprove / startProjectApprove  ✅
  # 实际入口在 /workflow/*，未挂到 ProjectController / ProjectTaskController（可接受）
```

权限字：`workflow:process:list|deploy|start`、`workflow:task:complete|reject`、`project:task:approve`、`project:manage:approve` 等（见 `PermissionConstants`）。  
`@PreAuthorize` 与 `@Log` 已按现有习惯挂上。

---

## 7. 验收总清单

- [x] H1 模块可编译、可启动  
- [x] H2 至少 1 个流程定义可用  
- [x] H3 待办 complete / reject 闭环（Flowable + approved 回写）  
- [x] H4 任务审批中禁改状态；通过/驳回后 `approved` 放开  
- [ ] H4 收尾：项目禁改 + 业务 status 回写（可选加强）  
- [x] 项目审批启流与任务同构（`startProjectApprove` 已有）  
- [ ] 两个 Token：办理人能办、无关用户待办为空（联调时再勾）  

---

## 8. pmhub 源码锚点

| 主题 | 路径（pmhub-boot） |
|------|-------------------|
| 任务办理 | `pmhub-workflow/.../WfTaskServiceImpl` |
| 启动流程 | `WfProcessServiceImpl` |
| 任务审批入口 | `ProjectTaskServiceImpl` + `startTaskApprove` |
| 关联表 | `sql/pmhub_20240305.sql` 中 `pmhub_project_task_process`、`pmhub_wf_*` |
| Flowable 配置 | workflow 模块 config |

---

## 9. 再往后

- [Sprint I](./09-sprint-i-job-stats.md)：逾期 Job、列表工厂、统计/燃尽  
- [Sprint J](./10-sprint-j-system-guard.md)：验证码、在线用户、限流、防重提交  
- OA / 代码生成：继续后置  
