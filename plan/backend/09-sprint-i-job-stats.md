# Sprint I：项目查询加深 + 统计 + 逾期 Job（自学版）

> **状态**：I1–I5a 已落地（I5b 管理端 CRUD/前端后置）  
> **目标**：补齐 pmhub-project 里 E/F 未做的「查询工厂 / 成员 / 我的任务 / 统计」；用定时任务标记逾期（Quartz 或 Spring `@Scheduled` 二选一）。  
> **前置**：Sprint E–F；H 不强制，但若已上审批，逾期 Job 勿覆盖审批中非法状态迁移。  
> **不做**：企微逾期推送、完整看板大屏 UI、generator、微服务 Job 中心。

---

## 0. 范围

```text
I1  QueryProjectFactory：我的项目 / 收藏 / 回收站（或等价 type 分流）
I2  项目成员：列表 / 邀请 / 移除（对照 ProjectMemberController）
I3  任务：queryMyTaskList + situation（进度概况）；burnDownChart 可简化
I4  项目 statistics / doing / select（下拉用）
I5  逾期：扫描到期未完成 → 置逾期状态；可选 sys_job 管理端
```

| 阶段 | 状态 |
|------|------|
| Sprint H | 见 [08-sprint-h-workflow.md](./08-sprint-h-workflow.md) |
| **Sprint I** | 本文件 |
| 再往后 | [Sprint J 系统护栏](./10-sprint-j-system-guard.md) |

---

## 1. I1 — 列表工厂

对照 pmhub `QueryProjectFactory` + Executor：

| type | 含义 |
|------|------|
| 我的 / 参与 | 成员表有当前用户 |
| 收藏 | `pmhub_project_collection` |
| 回收站 | `deleted=1`（若 E 已软删） |

TaskForge：可继续 POST `/project/list` 增加 `type` 字段分流，而不强制抄类名。

### 验收

- [x] 同一用户三种 type 结果集不同且合理  
- [ ] DataScope（若项目列表挂了）行为不回归（未单独回归；admin 冒烟 OK）  

---

## 2. I2 — 成员 API

| 动作 | 建议路径 |
|------|----------|
| 成员列表 | POST `/project/member/list` |
| 邀请 | POST `/project/member/add` |
| 移除 | POST `/project/member/remove` |

写 `inviteMember` 类日志（复用 `ProjectLog`）。  
**不做**：复杂角色（项目经理权限矩阵）可后置。

---

## 3. I3 / I4 — 任务与项目统计

| API | 说明 |
|-----|------|
| `/project/task/queryMyTaskList` | 我执行/我创建的任务分页 |
| `/project/task/situation` | 某项目任务状态计数 |
| `/project/task/burnDownChart` | 可返回日期+剩余简易序列；无数据时空数组 |
| `/project/statistics` | 项目维度汇总 |
| `/project/doing` · `/select` | 进行中列表 / 下拉 |

---

## 4. I5 — 逾期 Job

对照 pmhub `TaskOverdue*Job`：

```text
定时扫描
  → close_time（截止时间）< now 且状态 ∈ {未开始, 进行中, 待认领}
  → 更新为逾期(3)
  → 可选写日志；不发企微
  → 审批中(approved=0)跳过
```

> 口径：逾期看 **`close_time`**，不用 `end_time`（预计结束）；与 pmhub、`statistics.overdueTaskNum` 一致。

**实现选型（已选定）：`taskforge-quartz` + `sys_job`**

| 方案 | 说明 |
|------|------|
| ~~Spring `@Scheduled`~~ | 不做（避免以后再改） |
| **`taskforge-quartz` + `sys_job`** | **本 Sprint I5 采用**：逾期扫描注册为 `sys_job.invoke_target`，管理端可启停/立即执行 |

落地建议分两刀（避免一次抄全若依）：

1. **I5a（本刀）**：精简 Quartz 基建（Scheduler 初始化 + `JobInvokeUtil` + 读 `sys_job` 加载）+ `TaskOverdueJob` 业务 Bean + 插一条 `sys_job`；可选最小 `run` 接口方便验收。  
2. **I5b（可后置）**：补齐 `/monitor/job` CRUD、日志、前端定时任务页（对齐 pmhub-quartz）。

> 备注：pmhub 里 `TaskOverdueStatusJob` 实际是 `@Scheduled`；TaskForge 故意用 Quartz 调 Bean，以后管理端不用再换。

项目逾期同理，可同一 Job 方法里分两段。

### 验收

- [x] 造一条昨天截止的进行中任务，跑 Job 后变逾期  
- [x] 已完成任务不被改成逾期  

---

## 5. 验收总清单

- [x] I1 三列表打通  
- [x] I2 邀请/移除成员 + 列表  
- [x] I3/I4 至少一个统计接口有真实聚合数据  
- [x] I5 逾期扫描可手动触发或等 cron 跑通  

编译：`mvn -pl taskforge-admin -am -DskipTests compile`
