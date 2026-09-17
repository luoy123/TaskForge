# Sprint H：Flowable 工作流 + 项目/任务审批联动（自学版）

> **状态**：**当前焦点**（后端 A–G 已完成）  
> **目标**：新建 `taskforge-workflow`，接入 Flowable；打通「发起项目/任务审批 → 待办办理 → 回写状态」；审批中禁止手改状态（对齐 pmhub `approved`）。  
> **前置**：Sprint E–G（项目 / 任务 / 文件主路径）。  
> **不做**：企微待办、完整表单设计器、RocketMQ、Seata、微服务拆分、前端待办页（本 Sprint 只保证 API + curl/Swagger 可验）。

---

## 0. 进度与范围

| 阶段 | 状态 |
|------|------|
| Sprint A–G | ✅ |
| **Sprint H** | **H1 骨架已落地（模块/配置/BPMN/Controller/TODO）；H2 部署已实现；H3–H5 业务留给你** |
| 再往后 | [Sprint I 逾期 Job / 统计](./09-sprint-i-job-stats.md) → [Sprint J 系统护栏](./10-sprint-j-system-guard.md) |

```text
H1  Maven：taskforge-workflow + Flowable 配置  ✅ 骨架
H2  内置 bpmn + deployBuiltin / latestDefinition  ✅
H3  运行时：start / todo / complete / reject     ⬜ TODO in Wf*ServiceImpl
H4  与 project 联动：startTaskApprove + edit 禁改 ⬜ TODO
H5  Controller + 权限字                          ✅ 入口已挂，实现依赖 H3/H4
```

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

- [ ] 启动 admin 无报错，库中出现 Flowable 表（或确认已有）
- [ ] `mvn -pl taskforge-admin -am -DskipTests compile` 通过

---

## 3. H2 — 分类 / 模型 / 部署（最小）

对照 pmhub：`WfCategory` / `WfModel` / `WfDeploy`（不必一次抄全字段）。

| API（建议前缀） | 能力 |
|-----------------|------|
| `/workflow/category/*` | 分类 CRUD |
| `/workflow/model/*` | 模型列表 / 保存 / 部署 |
| `/workflow/deploy/*` | 部署列表 / 激活暂停（可后置） |

**MVP 可砍法**：先不写可视化建模，仓库放 `resources/bpmn/task-approve.bpmn20.xml`，启动时或管理接口「部署内置定义」。

### 验收

- [ ] 能部署至少 1 个流程定义，`repositoryService` 能查到
- [ ] 分类或模型有一条可查数据（若做了 CRUD）

---

## 4. H3 — 启动实例 + 待办办理

| API | 能力 |
|-----|------|
| 待办列表 | 当前用户 `taskService` 查询 |
| 已办列表 | historic 查询（可简化） |
| complete | 通过并带可选 comment |
| reject | 驳回（对齐 pmhub 策略：结束或回退，本 Sprint 选一种写清） |

assignee / candidate：先用 **固定登录用户 id 字符串**，角色候选人可后置。

### 验收（curl）

- [ ] 用 Token A 启动实例后，A（或指定办理人）待办列表有数据
- [ ] complete 后待办消失，业务回调可先打日志

---

## 5. H4 — 与 project 联动（本 Sprint 核心）

### 必须做

1. 实体/Mapper：`ProjectTaskProcess`（或等价）映射关联表  
2. `startTaskApprove(taskId)`：校验任务 → 启流 → 写关联 → `approved=0`（或项目侧等价字段）  
3. `startProjectApprove(projectId)`：同上（可与任务共用一套 process 类型字段）  
4. `edit` 任务/项目状态：若在审则拒绝（抛业务异常，返回 `R` 失败码）  
5. 审批通过回调：更新任务/项目状态 + `approved` 放开；驳回：按选定策略恢复

### 明确不做（H4）

- 审批附件与文件模块深度绑定  
- 多级会签 / 并行网关复杂图（bpmn 保持线性：提交 → 审批人 → 结束）

### 验收

- [ ] 任务启动审批后，直接 `edit` 改状态失败  
- [ ] complete 通过后状态按约定更新，并可再编辑  
- [ ] 关联表能查到 `processInstanceId` ↔ `taskId`/`projectId`

---

## 6. H5 — Controller 落位与权限

```text
taskforge-admin/
  web/controller/workflow/
    WfProcessController.java      # 待办/已办/complete/reject
    WfModelController.java        # 可选
    WfCategoryController.java     # 可选
  # 在 ProjectController / ProjectTaskController 增加：
  #   POST /project/startProjectApprove
  #   POST /project/task/startTaskApprove
```

权限字示例（与菜单可后补）：`workflow:process:list`、`project:task:approve`。  
`@PreAuthorize` 与 `@Log` 按现有习惯挂上。

---

## 7. 验收总清单

- [ ] H1 模块可编译、可启动  
- [ ] H2 至少 1 个流程定义可用  
- [ ] H3 待办 complete / reject 闭环  
- [ ] H4 任务（建议先做）审批中禁改状态，通过后恢复  
- [ ] 项目审批可与任务同构，做完任务再补项目  
- [ ] 两个 Token：办理人能办、无关用户待办为空  

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
