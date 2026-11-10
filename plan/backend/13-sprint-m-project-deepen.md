# Sprint M：项目域加深（阶段 / 工时 / 动态 / 通知）（自学版）

> **状态**：✅ 已完成（2026-11-10）  
> **估算**：4～6 人日  
> **目标**：对照 pmhub-project 未仿完的中层能力：阶段管理、工时、任务动态工厂、字段变更日志、逾期通知表（先落库+API，不接企微）。  
> **前置**：E–F、I（成员/统计已有）。  
> **不做**：物料 materials、网盘 public_file、企微推送实连、完整甘特。

## 文件对照

| 看 pmhub | TaskForge |
|----------|-----------|
| `pmhub-admin/.../project/ProjectStageController.java` | **新建** `taskforge-admin/.../controller/project/ProjectStageController.java` |
| `pmhub-project/.../service/impl/ProjectStageServiceImpl.java` | **新建** `.../service/IProjectStageService.java`、`.../service/impl/ProjectStageServiceImpl.java` |
| `pmhub-project/.../domain/ProjectStage.java`、`mapper/ProjectStageMapper.java` | **已有**，不新建 |
| `pmhub-project/.../domain/ProjectTaskWorkTime.java`、`mapper/ProjectTaskWorkTimeMapper.java` | **新建** 同名实体 + Mapper；Service 与接口挂在 `ProjectTaskController`（pmhub 工时没有单独 Controller） |
| `pmhub-project/.../service/task/QueryTaskLogFactory.java` | **新建** `taskforge-project/.../service/task/QueryTaskLogFactory.java`（或只在现有 `log/list` 加 type，二选一） |
| `pmhub-project/.../domain/vo/project/log/ForUpdate.java`、`FieldUtils.java` | **新建** `taskforge-project/.../domain/vo/log/ForUpdate.java`、`FieldUtils.java` |
| `pmhub-project/.../domain/ProjectTaskNotify.java`、`mapper/ProjectTaskNotifyMapper.java`、`job/TaskNotifyJob.java` | **新建** 实体、Mapper、`taskforge-project/.../job/TaskNotifyJob.java` |
| `pmhub-admin/.../project/ProjectLogController.java` | 项目维度日志若要独立接口，**新建** `ProjectLogController.java`；任务日志继续改 `ProjectTaskController` |

包路径前缀：`src/main/java/com/zhq/taskforge/`。

---

## 0. 过程

库表都已有，不要新建表。字段以 `sql/pmhub_20240305.sql` 为准，实体只映射表里有的列。

### M1 阶段

1. 打开 pmhub 的 `ProjectStageController`、`ProjectStageServiceImpl`，看 list/add/edit/delete 的入参。
2. 新建 Service：`list(projectId)` 按 `stageCode` 升序。
3. `add`：同一项目下 `stageName` 不重复；`stageCode` 取当前最大值 +1。
4. `edit`：只允许改名称。
5. `delete`：先 `selectCount` 任务表 `project_stage_id = 该阶段`。大于 0 就拒绝：「阶段下仍有任务」。同时禁止删掉项目当前阶段。
6. `setCurrent`：把 `pmhub_project.project_stage_id` 更新为该阶段 id。之后走一遍现有「新建任务」，确认新任务的 `projectStageId` 是这个阶段。
7. Controller 挂到 `/project/stage`，权限字自己加一串，例如 `project:stage:list`。

### M2 工时

时长单位用**小时**，一位小数，和表字段类型不一致时在计划旁注一行再写代码。

> 旁注：表字段 `work_time decimal(5,2)`、`user_id varchar(64)`，无 `deleted`。约定：实体 `workTime` 用 `BigDecimal`、`userId` 用 `Long`；后端只收最多一位小数且 `0 < workTime <= 24`；删除为物理删除；`projectId` 由任务反查，不收前端值。pmhub 只有实体 + Mapper，无 Service / 接口。

1. 实体、Mapper 映射 `pmhub_project_task_work_time`。
2. `POST /project/task/workTime/add`：body 有 `taskId`、`hours`。`userId` 用当前登录用户，不信前端传的 userId。`hours <= 0` 拒绝。
3. `POST /project/task/workTime/list`：按 `taskId` 分页，带上登记人昵称（join `sys_user`）。
4. 同接口或单独字段返回 `sum(hours)`。
5. `delete`：只允许删自己的记录，超管除外。

### M3 动态分流

1. 看现有 `POST /project/task/log/list` 怎么查 `pmhub_project_log`。
2. body 增加 `logType`：空=全部，`dynamic` / `deliverable` / `comment` 三种。对照 pmhub `QueryTaskLogFactory` 每种 type 过滤哪个 `operateType`。
3. 在 Service 里用 if/switch 拼 wrapper 即可。工厂类可以建，但不要为了类名卡住。
4. 评论接口已经会 insert 日志的话，用 `logType=comment` 能查到刚写的那条。

### M4 编辑字段日志

1. 在 `ProjectTaskServiceImpl.edit` 里，更新前先 `selectById` 拿到旧任务。
2. 对比白名单：`taskName`、`status`、`executor`（或执行人字段）、`closeTime`。变了的字段拼成一行中文，例如「任务名称：A → B」。
3. insert 一条 `ProjectLog`，`type=task`，`operateType=editTask`，内容用上面的差异。没变化就不写日志。
4. `@ForUpdate` 可以后补，这版用手写对比就行。

> 旁注：现有 `edit` 不支持改 `closeTime`（`TaskReqVO` 没这个字段），白名单改为 `taskName`、`status`、`userId`（执行人，显示昵称）、`taskPriority`。只对比「前端传了（非 null）且和旧值不同」的字段；多个变化合成一条日志，用「；」分隔，`remark` 仍为「编辑了任务」；content 用纯文本（前端 `TaskDetailDrawer` 直接显示），不学 pmhub 存 JSON。`edit` 补 `@Transactional`。

### M5 提醒记录（可选）

1. 实体映射 `pmhub_project_task_notify`。
2. 提供 list：某任务的提醒配置。
3. `TaskNotifyJob` 只插入「待发送」记录，不调企微。发送留给 Sprint U。

> 旁注：表 `pmhub_project_task_notify` 只有 `id, task_id, user_id(int), user_wx_name, project_id, overdue, close_time, task_name`，**没有发送状态 / 创建时间列**，所以它在 pmhub 里是「已提醒过」的去重记录，不是配置表；提醒配置在 `pmhub_project.msg_notify`（开关）+ `notify_day`（提前几天）。约定：
> - 拆两步：① 实体 + Mapper + `POST /project/task/notify/list`（按 taskId 查记录，复用 `PROJECT_TASK_DETAIL`）；② `TaskNotifyJob`。
> - Job 用 TaskForge 现有风格：`@Component("taskNotifyTask")` + 方法，由 Quartz `sys_job.invoke_target` 调用，不用 `@Scheduled`。
> - 选任务条件：项目 `deleted=0`、`msg_notify=1`、非暂停；任务 `deleted=0`、`close_time` 非空、`status` 不是已完成；`今天到截止日的天数 == notify_day`。
> - 去重：同一 `task_id + close_time + overdue=0` 已有记录就跳过（比 pmhub 只按 task_id 多带 close_time，截止时间改了还能再提醒）。
> - 不过滤 `user_wx_name`（TaskForge 不接企微），只打日志、落记录。

---

## 1. API 建议

| 前缀 | 能力 |
|------|------|
| `/project/stage/*` | list / add / edit / delete / 设为当前 |
| `/project/task/workTime/*` | add / list / delete；按 taskId |
| `/project/task/log/list` | 已有则加 `logType` 分流 |
| `/project/task/notify/*` | 可选：配置天数、列表待提醒 |

---

## 2. 设计注意

- 阶段删除：`project_task.project_stage_id` 引用检查。  
- 工时：`userId` 默认当前用户；时长单位与 pmhub 对齐（小时/分钟，计划里写死）。  
- 日志工厂：不必抄类名，`type` 参数 + Executor/switch 即可。  
- ForUpdate：可用注解扫变更字段，MVP 也可对白名单字段手写对比。

---

## 3. 验收

- [x] 阶段增删改查 + 切换当前阶段后新建任务落在新阶段  
- [x] 工时登记后列表/汇总正确  
- [x] 日志按类型过滤；编辑任务产生字段级或汇总日志  
- [x] （若做 M5）notify 表有可读配置/记录  

编译：`mvn -pl taskforge-admin -am -DskipTests compile`

---

## 4. 再往后

- [Sprint N](./14-sprint-n-monitor-server.md)：服务 / 缓存监控  
- [Sprint O](./15-sprint-o-generator.md)：代码生成  
- OA：见路线图后置  
