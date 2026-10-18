# Sprint M：项目域加深（阶段 / 工时 / 动态 / 通知）（自学版）

> **状态**：排队  
> **目标**：对照 pmhub-project 未仿完的中层能力：阶段管理、工时、任务动态工厂、字段变更日志、逾期通知表（先落库+API，不接企微）。  
> **前置**：E–F、I（成员/统计已有）。  
> **不做**：物料 materials、网盘 public_file、企微推送实连、完整甘特。

---

## 0. 范围

```text
M1  项目阶段：列表 / 新增 / 重命名 / 删除（有任务占用则拒）+ 切换当前阶段
M2  工时：pmhub_project_task_work_time CRUD（登记 / 列表 / 汇总）
M3  QueryTaskLogFactory：全部 / 动态 / 交付物 / 评论 分流（或 list 增加 type）
M4  任务编辑字段变更日志（@ForUpdate 或手写 diff，写 ProjectLog）
M5  （可选）task_notify：到期前提醒记录表读写；真正发消息留给 OA Sprint
```

库表已有：`pmhub_project_stage`、`pmhub_project_task_work_time`、`pmhub_project_log`、`pmhub_project_task_notify`。

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

- [ ] 阶段增删改查 + 切换当前阶段后新建任务落在新阶段  
- [ ] 工时登记后列表/汇总正确  
- [ ] 日志按类型过滤；编辑任务产生字段级或汇总日志  
- [ ] （若做 M5）notify 表有可读配置/记录  

编译：`mvn -pl taskforge-admin -am -DskipTests compile`

---

## 4. 再往后

- [Sprint N](./14-sprint-n-monitor-server.md)：服务 / 缓存监控  
- [Sprint O](./15-sprint-o-generator.md)：代码生成  
- OA：见路线图后置  
