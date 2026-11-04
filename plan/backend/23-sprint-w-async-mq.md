# Sprint W：异步消息（可替换 RocketMQ）

> **状态**：排队，可选  
> **估算**：3～5 人日（进程内队列）；若上真 RocketMQ 再加环境成本  
> **目标**：对照 pmhub `pmhub_async` + OA consumer，把指派/逾期/审批通知从同步调用改成异步投递，失败可重试、可查。  
> **前置**：U 的 MessageSender。  
> **不做**：必须安装 RocketMQ 才算完成。默认用 DB 表 + 定时扫描，接口形态对齐「发得出、消费完、失败可重放」。

## 文件对照

| 看 pmhub | TaskForge |
|----------|-----------|
| `pmhub-system/.../service/impl/PmhubAsyncServiceImpl.java`、`IPmhubAsyncService.java` | **新建** `taskforge-system/.../domain/PmhubAsync.java`、`mapper/PmhubAsyncMapper.java`、`service/impl/PmhubAsyncServiceImpl.java` |
| `pmhub-admin/.../tool/AsyncController.java` | **新建** `taskforge-admin/.../controller/tool/AsyncController.java`（列表、重放） |
| `pmhub-oa/.../oa/consumer/OAMessageConsumer.java` | **新建** `taskforge-oa/.../job/AsyncMessageJob.java`，用 Quartz 或 `@Scheduled` 扫表，不要先上 RocketMQ |
| 表 `pmhub_async`（`sql/pmhub_20240305.sql`） | 不新建表，实体字段按这张表 |

**改** U 的 `MessageSender`：业务侧改为先 `insert pmhub_async`，由 Job 再真正发站内信/WebSocket。

---

## 0. 过程

W4 这版不做。业务代码只依赖「入队」，不出现 RocketMQ 的类名。

### W1 入队

1. 实体对齐 `pmhub_async`：至少要有 id、topic、body、status、retryCount、错误信息、创建时间。列名以 SQL 为准。
2. status：`0` 待处理，`1` 成功，`2` 失败。
3. 把 U 的 `MessageSender` 实现改成：把 title、userId 等序列化成 JSON，insert 一行，topic 固定 `site_message`。接口马上返回，此时消息表还没有收件行。

### W2 消费

1. `AsyncMessageJob` 每 10 秒（或挂到已有 Quartz）拉取 status=0 或（status=2 且 retryCount<3）的 20 条。
2. 每条：解析 JSON，调用原来的落库发送（U1 那个 insert 收件箱）。成功则 status=1。
3. 抛错：retryCount+1，写入错误信息。到 3 次仍失败则保持 status=2，不再自动扫。
4. 同一行用更新条件 `status in (0,2)` 抢占，避免两个调度叠在一起发两遍。做不到就先单线程，注释标明。

### W3 管理

1. `GET /tool/async/list?status=2` 分页。
2. `PUT /tool/async/retry/{id}`：把 status 改回 0、retryCount 改 0，等 Job 再跑。不要在这个请求里同步发送。

---

## 1. 验收

- [ ] 指派任务后接口立即返回，异步任务表出现一行并最终成功  
- [ ] 消费抛错时状态为失败且次数 +1  
- [ ] 手工重放失败记录可再次成功  

---

## 2. 说明

这一刀是 **架构对齐**，不是新业务菜单。排期靠后；不做也不影响 L～V 的功能验收。
