# Sprint F：任务模块（第一刀 · 自学版）

> **目标**：在已有 `taskforge-project` 上打通「任务」主路径：创建（成员+日志）、详情、列表、编辑、软删、子任务、评论与任务动态。  
> **前置**：Sprint E 完成（项目 CRUD / 归档退出收藏 / `ProjectMember`·`ProjectLog` 多态已可用）。  
> **库表**：复用 `pmhub_project_task`（及已有 member/log）；**不新建** `tf_` 任务表。  
> **不做**：Flowable 审批、企微/OA 指派提醒、导入导出、燃尽图、文件/模板上传、工时、`QueryTaskLogFactory` 全套工厂（可后置）。

---

## 0. 进度与本 Sprint 范围


| 阶段 | 状态 |
|------|------|
| Sprint A–E | 完成 |
| **Sprint F** | **本文件：F1 → F2 → F3 → F4** |
| 再往后 | [文件/导入导出 Sprint G](./07-sprint-g-file.md) →（可选）工作流 H |


```text
F1  任务实体 / 枚举 / Mapper + Controller 骨架  ✅
F2  创建任务 add（成员 + 日志）+ 基础详情  ✅
F3  列表（项目下任务 / 可先做我的任务）+ 编辑 + 软删  ✅
F4  子任务 + 评论 + 任务动态列表  ✅
```

对照习惯：左边 pmhub，右边 TaskForge；返回 `R<T>`；分页用 MyBatis-Plus `Page`。

---

## 0.1 模块与依赖

**不新建 Maven 模块。** 任务落在现有 `taskforge-project`：

```text
taskforge-project/
  domain/ProjectTask.java
  domain/vo/task/          # TaskReqVO / TaskResVO / TaskCommentVO …
  mapper/ProjectTaskMapper.java
  resources/mapper/project/ProjectTaskMapper.xml
  service/IProjectTaskService.java
  service/impl/ProjectTaskServiceImpl.java

taskforge-admin/
  web/controller/project/ProjectTaskController.java   # /project/task/*
```

依赖方向不变：`project → common`；查用户昵称继续 **Mapper join `sys_user`**，不要为了任务去拉 `oa` / `workflow`。

成员 / 日志继续复用：

| 表 | type 取值 | pt_id |
|----|-----------|-------|
| `pmhub_project_member` | `"task"`（`ProjectStatusEnum.TASK`） | 任务 id |
| `pmhub_project_log` | `"task"` | 任务 id；`project_id` 必填 |

---

## 1. 领域心智（先读再写）

```text
Project
  └── ProjectTask          挂 project_id；task_pid 非空 = 子任务
        ├── ProjectMember  type=task，创建者 creator=1；执行人可再插一条
        └── ProjectLog     addTask / invitePartakeTask / comment / edit …
```

**任务状态**（对照 `ProjectTaskStatusEnum`，以库内实际取值为准）：

```text
未开始(0) → 进行中(1) → 已完成(2)
旁路：逾期(3) / 待认领(4) 等 —— 先能读写，不必一次做完所有流转规则
```

**关键字段**（`pmhub_project_task`）：

| 字段 | 含义 |
|------|------|
| `id` | UUID |
| `project_id` | 归属项目 |
| `task_name` / `description` | 名称与描述 |
| `user_id` | 执行人 |
| `project_stage_id` | 所属阶段（可先用项目当前阶段） |
| `task_pid` | 父任务 id；顶级任务为空 |
| `status` / `execute_status` / `task_process` | 状态与进度 |
| `task_priority` | 优先级 |
| `begin_time` / `end_time` / `close_time` | 时间 |
| `deleted` / `deleted_time` | 软删（注意 `@TableLogic` 与项目同样问题 → 优先自定义 softDelete） |

### 创建任务副作用（必须写进 F2）

对照 `ProjectTaskServiceImpl.add`：

1. 校验归属项目存在且**未暂停**（`ProjectStatusEnum.PAUSE`）
2. insert 任务（`ASSIGN_UUID`；审计字段；默认 `deleted=0` 等）
3. `insertMember(taskId, creator=1, 当前用户)`，`type=task`
4. 写日志 `operateType=addTask`，`logType=动态`，`type=task`
5. 若指定执行人 `userId` 且 ≠ 当前用户：再插成员 `creator=0` + 日志 `invitePartakeTask`
6. **跳过**企微 / RocketMQ 指派提醒（pmhub 里也是 TODO 关闭）

子任务：`addChildTask` 本质是 `taskPid = 父任务 id` 再走同一套 add（或复用 add，由入参带父 id）。

---

## 2. F1 — 实体 / 枚举 / Mapper / 骨架

### 参考

- `pmhub-project/.../domain/ProjectTask.java`
- `pmhub-common/.../enums/ProjectTaskStatusEnum.java`、`ProjectTaskPriorityEnum.java`
- 库表：`pmhub_project_task`（DBX / Navicat）

### 你要做

1. common 增加枚举：`ProjectTaskStatusEnum`、`ProjectTaskPriorityEnum`（状态码照抄 pmhub）。
2. `ProjectTask` 实体：`@TableName("pmhub_project_task")`，主键 `ASSIGN_UUID`；时间用 `LocalDateTime`；`@TableLogic` 谨慎（删除建议学项目的 `softDelete` XML）。
3. `ProjectTaskMapper` + 空 XML；`IProjectTaskService` / Impl 空壳；`ProjectTaskController` 先挂路由占位。
4. 权限常量对齐菜单（库里已有）：`project:task:add` / `detail` / `list` / `edit` / `delete` / `addChildTask` / `addComment` / `logList` …

### 验收 F1

```bash
mvn -pl taskforge-admin -am -DskipTests compile
```

---

## 3. F2 — 创建 + 详情

### 参考

- `ProjectTaskServiceImpl.add` / `detail`
- `ProjectTaskController` → `POST /project/task/add`、`POST /project/task/detail`

### 你要做


| 接口（建议） | 权限 | 行为 |
|--------------|------|------|
| `POST /project/task/add` | `project:task:add` | 全套副作用；可返回 `taskId` |
| `POST /project/task/detail` | `project:task:detail` | 详情；补 statusName / 执行人昵称等 |


入参可用 `TaskReqVO`（至少：`projectId`、`taskName`、`userId`、阶段/时间可选）。  
详情可用 Mapper XML join `sys_user` + 阶段名。

### 验收 F2

1. 创建成功；DB：`pmhub_project_task` 1 行；`member` 至少创建者 1 条且 `type=task`；`log` 有 `addTask`。
2. 指定其他执行人时：成员 ≥2，日志含 `invitePartakeTask`。
3. 项目 `status=暂停` 时新增应失败。
4. detail 能读出名称、项目、状态名、执行人昵称。

---

## 4. F3 — 列表 + 编辑 + 删除

### 参考

- `ProjectTaskServiceImpl.list` / `edit` / `deleteTask`
- Controller：`/task/list`、`/task/edit`、`DELETE /task/delete`

### 列表策略（本 Sprint 固定）

**第一版（必做）**：按 `projectId` 查该项目下未删任务，分页；可加 keyword / status 筛选。  
**鼓励**：再做「我的任务」`queryMyTaskList`（`user_id = 当前用户` 或 member 关联），别被 pmhub 首页统计拖住。

### 编辑规则（先对齐核心，审批后置）


| 点 | 处理 |
|----|------|
| 项目已暂停 | 禁止编辑 |
| 改 `status` | pmhub：若任务需审批则禁止手改；本 Sprint **可先跳过 approved 查询**，只保留「仅创建人可改状态」或先全部放开并在计划里记 TODO |
| 换执行人 | 可补成员 + 日志（对照 edit 后半段，能写多少写多少） |
| 字段变更日志 | pmhub 有 `@ForUpdate`；本 Sprint 可只写一条总 `editTask` 日志，细粒度后置 |


### 删除

- 软删；**务必**用自定义 SQL / `softDelete`（与项目同样踩过 `@TableLogic` + `updateById` 写不进 `deleted`）。
- 有子任务时：禁止删 或 级联软删——二选一并在代码注释写清；建议 **有未删子任务则禁止**。
- 回头补：项目 `delete` 里「存在任务则禁止」TODO 可在本切片接上。

### 验收 F3

- 列表只看到未删任务；分页正常。  
- 编辑后 detail 变化且有日志。  
- 删除后列表不可见，`deleted=1`；再删应「不存在」。

---

## 5. F4 — 子任务 + 评论 + 任务动态

### 参考

- `addChildTask`、`queryChildTask`、`addComment`、`queryTaskLogList`
- Controller 对应路径

### 你要做


| 接口 | 行为 |
|------|------|
| `POST /project/task/addChildTask` | 设 `taskPid=父任务id`，复用 add 副作用 |
| `POST /project/task/queryChildTask` | 按父 id 列子任务（可先不分页） |
| `POST /project/task/addComment` | insert `ProjectLog`：`logType=评论`，`operateType=comment`，`content=评论内容` |
| `POST /project/task/log/list` | 按 taskId 查日志；**第一版**可不过滤 logType，或简单 `if` 分流；工厂后置 |


### 验收 F4

- 子任务 `task_pid` 正确；查子任务列表可见。  
- 评论写入后，动态列表能看到 `comment`。  
- 两个账号：执行人可见自己相关任务（若做了我的任务列表）。

---

## 6. 推荐动手顺序


| 天 | 任务 | 完成标志 |
|----|------|----------|
| Day1 | F1 | 实体枚举编译过；Controller 能扫到 |
| Day2 | F2 | 创建副作用齐全；详情可读 |
| Day3 | F3 | 列表+编辑+软删可用；项目删前校验可接上 |
| Day4 | F4 | 子任务+评论+任务日志可演示 |


每完成一块：

```bash
mvn -pl taskforge-admin -am -DskipTests compile
```

建议联调顺序：`/project/add`（已有）→ `/project/task/add` → `detail` → `list` → `edit` → `delete` → `addChildTask` → `addComment` → `log/list`。

---

## 7. 常见坑


| 坑 | 处理 |
|----|------|
| member/log 的 `type` 写成 `"project"` | 任务侧必须 `"task"` |
| 软删 `deleted` 写不进 | 学 E3：`softDelete` XML，别只靠 `updateById` |
| 暂停项目仍能加任务 | add/edit 开头查项目 status |
| 子任务删了父还在列表算「无子」 | 子任务查询带 `deleted=0` |
| 一次抄完审批 + ForUpdate + 工厂 | 先主路径，审批/细粒度日志/工厂后置 |
| 权限 403 | 菜单已有 `project:task:*`；确认角色有授权 |

---

## 8. 本 Sprint 明确不做

- `startTaskApprove` / `pmhub_project_task_process` / Flowable  
- 企微指派提醒、逾期 Job  
- 任务导入导出、燃尽图、首页 situation 统计加深  
- 文件 / 模板 `UploadFileFactory`  
- `QueryTaskLogFactory` 三档工厂（F4 用简单查询即可）  
- 工时 `pmhub_project_task_work_time`

F1–F4 过关后，下一站见 **[Sprint G：文件上传 + 导入导出](./07-sprint-g-file.md)**。

---

## 9. 卡住时怎么问

贴：**接口路径 + 请求体 + task/member/log 相关行 + 期望 vs 实际**。  
例如「add 后 member 的 type 仍是 project」比整文件粘贴更好定位。

---

## 10. 关键源码锚点


| 主题 | 路径 |
|------|------|
| 任务实体 | `pmhub-project/.../domain/ProjectTask.java` |
| 增删改查/评论 | `.../service/impl/ProjectTaskServiceImpl.java` |
| Controller | `pmhub-admin/.../controller/project/ProjectTaskController.java` |
| 状态/优先级枚举 | `pmhub-common/.../enums/ProjectTaskStatusEnum.java` 等 |
| 表结构 | 库表 `pmhub_project_task` 或 `sql/pmhub_20240305.sql` |
| 总对照 | [00-pmhub-reference-map.md](./00-pmhub-reference-map.md) |
| 上一切片 | [05-sprint-e-project.md](./05-sprint-e-project.md) |
