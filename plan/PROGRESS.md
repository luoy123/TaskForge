# 当前学习进度

> 每个新对话 AI 先读本文件，再读「正在做」指向的计划。  
> 每完成一个步骤（如 M1、L2、F08-3），就更新本文件；只保留当前状态，历史看 git log。  
> 参照项目：`E:\pmhub-boot`（只读对照，不改）；写代码：`E:\TaskForge`。

## 正在做

- **后端**：下一个 Sprint 待选：[O 代码生成](./backend/15-sprint-o-generator.md)（排期顺序）或 [P 流程平台](./backend/16-sprint-p-workflow-platform.md)（业务上更优先）。
- **当前步骤**：Sprint N 与前端 15 已完成并提交（2026-11-13），还没开始新 Sprint。
- **写到哪**：—

## 已完成

- **Sprint N 服务 / 缓存监控**（2026-11-13 验收）：
  - N1 `GET /monitor/server`：Oshi 6.6.5，`Server.copyTo()` 汇总 CPU（两次采样）/ 内存 / JVM / 系统 / 磁盘。
  - N2 `GET /monitor/cache`：`serverCommands().info()` 挑 4 个字段 + `dbSize` + 三个前缀用 SCAN 计数，不用 KEYS。
  - N3 `DELETE /monitor/cache/clear?prefix=`：白名单精确匹配，SCAN 每 100 个删一批，单独权限 `monitor:cache:remove`（菜单 2179），`@Log` 用 CLEAN。
  - N4 `GET /monitor/server/pool`：Hikari `HikariPoolMXBean`，返回 active/idle/total/waiting/max。不引入 Druid。
- **前端 15 监控页**（2026-11-13 联调通过）：`views/monitor/server`（CPU、内存、服务器、连接池、JVM、磁盘）、`views/monitor/cache`（Redis 概况 + 前缀数量）；路由加了不带 name 的 404 兜底页，无权限用户直接输入地址不再白屏。

- **Sprint M 项目域加深**（2026-11-10 验收）：
  - M1 阶段管理：`/project/stage/*`，阶段增删改查 + 设为当前阶段。
  - M2 工时：`/project/task/workTime/add|list|delete`，单条 0 < h ≤ 24、一位小数，list 带昵称 + 总工时。
  - M3 日志分流：`/project/task/log/list` 加 `logType`（null / -1 全部，1 动态仅 editTask，2 交付物，3 评论），返回 `TaskLogVO`（含 nickName / logTypeName）。
  - M4 编辑字段日志：`edit` 手写白名单对比（名称 / 状态 / 执行人昵称 / 优先级），多项合成一条，无变化不写。
  - M5 提醒记录：`ProjectTaskNotify` + `POST /project/task/notify/list`；`TaskNotifyJob`（`taskNotifyTask.notifyDueSoon()`，SQL 内 DATEDIFF = notify_day + not exists 去重 task_id+close_time+overdue=0），只落记录不发企微。冒烟：执行两次只 1 条、截止时间改后再提醒、对照任务不提醒。sys_job 里**没有留**正式定时任务，要用时在「定时任务」页新增，cron `0 0 9 * * ?`。
- **前端 09 阶段 / 工时 / 动态**（2026-11-10 联调通过）：项目行「阶段」抽屉（增删改、设当前）；任务列表加「阶段」列、编辑框加优先级；任务抽屉动态四个筛选 + 「工时」页签（登记 / 列表 / 合计 / 删自己的）。顺手修了 `/project/task` 路由丢失（前端 04 起任务页空白）。
- 已定设计：删除阶段时只统计 `deleted = 0` 的任务。以后做「恢复任务」时，若原阶段已删，改挂到项目当前阶段。

## 待收尾

- N 遗留：
  - 无权限返回 `code=500 "系统内部错误: Access Denied"`，不是 403。在 `GlobalExceptionHandler` 单独处理 `AccessDeniedException` 即可，影响所有接口，改完要先 install framework。
  - N4 格式小点：`HiKari` 拼写、`//2.获取MaxBean` 注释、第 51 行缩进、变量名 `linkedHashMap`；`CacheController` 里「不允许清楚改前缀」文案。
  - 菜单 114「缓存列表」（`monitor/cache/list`）前后端都没做，仍是建设中页。

- 前端 09 工时「只能删自己的」只用 admin 验过，普通用户登录时可补测一次。
- M 遗留小点（不影响验收，有空再改）：
  - `ProjectStageServiceImpl.setCurrent` 第 226 行应比 `getProjectStageId()`。
  - 任务 `edit` 不校验 `status` / `taskPriority` 取值（传 99 会落库），`userId` 不校验存在。
  - `TaskNotifyJob` 日志只打第一条 taskId。
  - 可选加分：M4 手写白名单重构成注解 + 反射通用版（修掉 pmhub `FieldUtils` 的坑），拿「编辑项目」做第二个使用者。
- 前端 08：代码已完成，待联调 → [frontend/08](./frontend/08-sprint-workflow-polish.md) §4

## 后续项目

- TaskForge 完成后开工 **FlashTix 闪票**（高并发演出票务 + 营销交易平台），资料统一放在 `E:\FlashTix\docs\`，总览在 `E:\FlashTix\docs\01-总览\FlashTix项目总览.md`

## 卡住的点 / 下次要问的

- —

## 构建与启动注意

- 只改 `src/` 下的文件，**不要改 `target/`**（clean / install 会清空重建，改动会丢）。
- mvn 输出「Nothing to compile」时用 `clean compile`，否则 IDE 生成的 class 会掩盖编译错误。
- **IDE 会自动编译到 `target/classes`，写到一半生成的坏 class 会被 `mvn install` 直接打进 jar**（运行时报 `Unresolved compilation problems`）。改完代码后用 `mvn clean install -DskipTests` 最稳。
- 改了 admin 以外的模块（project / common 等）后，先在根目录 install，再 `mvn -pl taskforge-admin spring-boot:run`；否则 admin 用 `D:\maven\repository` 里的旧 jar，报 ClassNotFoundException。
- 远程 MySQL 在 192.168.5.16:3306，连不上时先 `Test-NetConnection 192.168.5.16 -Port 3306`，再去 Ubuntu 上启动 MySQL。

## 学习偏好

- 一次只推进一个小步骤：先讲 pmhub 里对应的写法和思路，再让我自己写，写完帮我检查。
- 我说「直接写」时才整段代码给出；骨架可由助手写，关键逻辑留 `【学员填写】`。
- 思考题直接附答案（放在题目后面）。
- 从 N2 起，Controller 也由我自己写；助手只讲思路、给提示、写完后检查，不再写骨架。测试也是我自己来。
