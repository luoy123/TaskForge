# Sprint K：定时任务管理端补齐（I5b · 自学版）

> **状态**：✅ **API 已落地**（`SysJobController` / `SysJobLogController`；前端 `views/monitor/job` 已有页）  
> **目标**：在已有 `taskforge-quartz` + `PUT /monitor/job/run`（I5a）之上，补齐若依/pmhub 风格的 **Job CRUD、启停、日志**，管理端可运维逾期扫描等任务。  
> **前置**：Sprint I5a（Scheduler 初始化、`JobInvokeUtil`、`TaskOverdueJob`、`sys_job` 至少一条）。  
> **不做**：集群调度、错失补偿策略大全、改逾期业务规则。

---

## 0. 为何是这一刀


| 已有                                              | 缺口                              |
| ----------------------------------------------- | ------------------------------- |
| `taskforge-quartz` 模块、逾期 Job、`/monitor/job/run` | 无 list / add / edit / delete    |
| `ScheduleUtils` 能 pause                         | 无管理端 changeStatus / 修改 cron 热更新 |
| —                                               | 无 `sys_job_log` 执行日志查询          |


对照 pmhub / RuoYi：`SysJobController` + `SysJobLogController`。

```text
K1  sys_job：分页列表 / 详情 / 新增 / 修改 / 删除
K2  启停 changeStatus + 修改后 reschedule；保留 run
K3  sys_job_log：写入（AbstractQuartzJob 已有则接线）+ 列表 / 清空 / 删
K4  权限字 + @Log + 联调（含立即执行逾期 Job）
```

---

## 1. 落位

```text
taskforge-quartz/
  domain/SysJob.java              # 已有，按表字段补齐
  domain/SysJobLog.java           # 新建（若无）
  mapper/SysJobMapper + LogMapper
  service/ISysJobService          # 扩 CRUD + changeStatus + 校验 cron
  service/ISysJobLogService
  util/AbstractQuartzJob          # 成功/失败写日志

taskforge-admin/
  web/controller/monitor/
    SysJobController.java         # 从「仅 run」扩成完整
    SysJobLogController.java
```

表：复用库内 `sys_job` / `sys_job_log`（以本地 SQL 为准）。

---

## 2. API 建议


| 方法     | 路径                                       | 说明                                   |
| ------ | ---------------------------------------- | ------------------------------------ |
| GET    | `/monitor/job/list`                      | 分页；query：jobName / jobGroup / status |
| GET    | `/monitor/job/{jobId}`                   | 详情                                   |
| POST   | `/monitor/job`                           | 新增并 schedule                         |
| PUT    | `/monitor/job`                           | 修改并 reschedule                       |
| PUT    | `/monitor/job/changeStatus`              | 启用 / 暂停                              |
| PUT    | `/monitor/job/run`                       | 已有，保留                                |
| DELETE | `/monitor/job/{jobIds}`                  | 删库 + 从 Scheduler 移除                  |
| GET    | `/monitor/jobLog/list`                   | 执行日志分页                               |
| DELETE | `/monitor/jobLog/{jobLogIds}` · `/clean` | 删 / 清空                               |


校验：`CronExpression.isValidExpression`；`invoke_target` 白名单或禁止危险调用（对齐 pmhub 的 `JobInvokeUtil` 限制即可）。

---

## 3. 验收

- 列表能看到逾期扫描那条 `sys_job`
- 暂停后再 `run` / 到点不执行；启用后恢复
- 改 cron 保存后 Scheduler 中触发时间变化
- 执行后 `sys_job_log` 有成功或失败记录
- `mvn -pl taskforge-admin -am -DskipTests compile` 通过

---

## 4. 再往后

- [Sprint L](./12-sprint-l-workflow-polish.md)：审批回写 / 项目禁改等 H4 收尾  
- [Sprint M](./13-sprint-m-project-deepen.md)：阶段 / 工时 / 动态工厂  
- 前端缺页：[../frontend/07-sprint-system-pages.md](../frontend/07-sprint-system-pages.md)（岗位/字典/日志等）

