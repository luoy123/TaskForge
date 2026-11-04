# Sprint U：消息与企微 / OAuth2

> **状态**：排队（无企业微信也可做「站内消息」半刀）  
> **估算**：站内消息 3～4 人日；企微实连 + OAuth2 再加 5～8 人日  
> **目标**：对齐 pmhub-oa 的产品能力：任务指派/逾期/待办提醒，审批结果通知；可选企微回调与 OAuth2 客户端。  
> **前置**：I 逾期 Job、H/P 审批、Q 任务认领。  
> **库表**：`pmhub_project_task_notify`、`pmhub_task_message_deal`、`pmhub_oauth2_*`。  
> **不做**：抄全套企微模板卡片 DTO；RocketMQ 集群（发送口先同步或内存队列，W 再换 MQ）。

## 文件对照

| 看 pmhub | TaskForge |
|----------|-----------|
| `pmhub-project/.../domain/ProjectTaskNotify.java`、`job/TaskNotifyJob.java` | 若 M 已建则 **改** `TaskNotifyJob`，让它调用消息接口；M 没做就本 Sprint **新建** 这两个 |
| `pmhub-workflow/.../domain/WfTaskMessageDeal.java`、`mapper/WfTaskMessageDealMapper.java` | **新建** `taskforge-workflow/.../domain/WfTaskMessageDeal.java` + Mapper（消息处理记录） |
| `pmhub-oa/.../oa/consumer/OAMessageConsumer.java` | **新建模块** `taskforge-oa/`，里面 **新建** `.../service/MessageSender.java`（接口）和 `DbMessageSender.java`（只落库）。先不要抄 consumer 里的 MQ |
| `pmhub-oa/.../domain/workWx/dto/body/notice/TaskAssignRemindDTO.java`、`TaskOverdueRemindDTO.java`、`TodoRemindDTO.java` | 只作字段参考。站内消息用一个 `SiteMessage` 即可，**不必**按 DTO 一个类一个类建 |
| `pmhub-admin/.../oa/workWx/CallbackController.java` | 做 U3 时 **新建** `taskforge-admin/.../controller/oa/WxCallbackController.java` |
| `pmhub-system/.../service/impl/OAuth2ServiceImpl.java` 及 `domain/PmhubOAuth2*.java` | 做 U4 时 **新建** `taskforge-system/.../domain/OAuth2Client.java`、`OAuth2Agree.java`、`service/impl/OAuth2ServiceImpl.java`、`taskforge-admin` 里一个 `OAuth2Controller` |

父 POM 与 admin 依赖加上 `taskforge-oa`。

---

## 0. 过程

没有企业微信时，做到 U2 就停。U3、U4 分开排。

### U1 站内消息

1. 看 `pmhub_project_task_notify` 或 `pmhub_task_message_deal` 哪张更像「收件箱」。选字段能表达接收人、标题、已读、业务 id 的那张，建 `SiteMessage` 实体。两张都别扭就用 `pmhub_task_message_deal`，缺列不要改生产表结构，把内容塞进已有文本列。
2. `MessageSender.send(userId, title, content, bizType, bizId)` 只 insert。
3. `GET /oa/message/my`：当前用户，未读在前。
4. `PUT /oa/message/read`：body 为 id 列表，只能改自己的。

### U2 接到现有业务

1. 任务 add 里如果指定了执行人且不是创建人，`send` 一条，bizType=`task_assign`。
2. `TaskOverdueJob` 把任务改成逾期之后，给执行人 `send`，bizType=`task_overdue`。审批中被跳过的任务不要发。
3. `WfTaskServiceImpl.complete` 成功后给发起人 `send`，bizType=`approve_pass`。reject 同理 `approve_reject`。
4. 这三处都不直接写 Mapper，只调 `MessageSender`。

### U3 企微（可选）

1. 配置项：`wx.cp.enabled`、corpId、secret、agentId。enabled 不是 true 时，企微实现什么都不做。
2. 新建 `WxMessageSender` 实现同一接口：先落库（调用 U1），enabled 时再 HTTP 发一条文本，内容用 title + content。模板卡片先不做。
3. 回调 URL 按 pmhub `CallbackController` 做验签。验签失败返回企微要求的错误，不要 500 堆栈。本 Sprint 回调里只打日志。

### U4 OAuth2（可选）

1. 客户端表用 `pmhub_oauth2_client`：clientId、secret、回调地址。
2. `GET /oauth2/authorize`：已登录则生成一次性 code 放 Redis，5 分钟，重定向到回调地址。
3. `POST /oauth2/token`：code 换一个本系统 JWT（走现有 TokenService）。code 用过即删。
4. 不做刷新令牌，不做第三方用户自动开户。

---

## 1. 模块

```text
taskforge-oa/          # 新建；MessageSender 接口
taskforge-admin/       # /oa/message/* 、/oa/wx/callback 、/oauth2/*
```

project / workflow 只依赖接口，不直接调企微 HTTP。

---

## 2. 验收

- [ ] 指派任务产生一条站内消息，列表可已读  
- [ ] 跑逾期 Job 后，执行人有逾期消息（审批中任务仍跳过改状态）  
- [ ] 开关关闭时不发企微、不报错  
- [ ] （若做 U4）登记客户端后能走通一次授权码换登录用户  

---

## 3. 排期切法

| 刀 | 人日 | 依赖外部环境 |
|----|------|----------------|
| U1–U2 站内 | 3～4 | 无 |
| U3 企微 | 3～5 | 企业微信应用 |
| U4 OAuth2 | 2～3 | 无 |
