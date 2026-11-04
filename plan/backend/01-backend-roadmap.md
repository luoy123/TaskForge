# 后端总排期（对齐 pmhub-boot 单体功能）

> 只谈 **Java / API**。前端另见 `plan/frontend/`。  
> 技术栈保持 Boot 3 + `R<T>`，不 1:1 抄类名。  
> 人日是单人粗估，方便排期，不是承诺工期。  
> 对照图：[00-pmhub-reference-map.md](./00-pmhub-reference-map.md)

做完 **L → W** 后，pmhub-boot 的**业务功能面**基本齐。下面「明确不做」里的项不算缺口。

每个 Sprint 正文里有 **「文件对照」**：左边是 pmhub 里要打开的类，右边是 TaskForge 要新建或修改的类。路径都相对各自仓库根目录，Java 包是 `com.laigeoffer.pmhub` 对 `com.zhq.taskforge`。

---

## 已完成（A–K）

| Sprint | 内容 | 文档 |
|--------|------|------|
| A–D | 用户/角色/菜单/部门/岗位/字典/配置/公告、日志、DataScope | 02–04 |
| E–G | 项目、任务、文件、导入导出 | 05–07 |
| H | Flowable 主路径（内置 BPMN、待办、通过/驳回、启审） | [08](./08-sprint-h-workflow.md) |
| I | 我的/收藏/回收站、成员、统计、燃尽、逾期 Job | [09](./09-sprint-i-job-stats.md) |
| J | 验证码、在线用户、限流、防重 | [10](./10-sprint-j-system-guard.md) |
| K | 定时任务 CRUD + 执行日志 | [11](./11-sprint-k-quartz-admin.md) |

模块：`common / framework / system / project / workflow / quartz / admin`。

---

## 未完成排期（建议顺序）

| 顺序 | Sprint | 估算 | 文档 | 对齐 pmhub 的什么 |
|------|--------|------|------|-------------------|
| 1 | **L** 审批打磨 | 2～3 人日 | [12](./12-sprint-l-workflow-polish.md) | 在审禁改、通过/驳回回写业务状态 |
| 2 | **M** 项目域加深 | 4～6 | [13](./13-sprint-m-project-deepen.md) | 阶段、工时、动态分流、字段日志、提醒记录 |
| 3 | **P** 流程平台 | 8～12 | [16](./16-sprint-p-workflow-platform.md) | 分类/模型/部署/表单/实例/转办委派抄送 |
| 4 | **Q** 系统与任务边角 | 4～6 | [17](./17-sprint-q-system-task-gap.md) | 用户导入导出、注册、头像、任务类型、模板、zip、认领 |
| 5 | **R** 物料 | 5～7 | [18](./18-sprint-r-materials.md) | 物料主数据 + 审批/报废 |
| 6 | **S** 文档 | 3～4 | [19](./19-sprint-s-document.md) | 文档类型与文档数据 |
| 7 | **T** 公共文件 | 3～5 | [20](./20-sprint-t-public-file.md) | 网盘、分享链接 |
| 8 | **N** 监控 | 2～3 | [14](./14-sprint-n-monitor-server.md) | 服务监控、缓存监控、连接池 |
| 9 | **O** 代码生成 | 4～6 | [15](./15-sprint-o-generator.md) | generator 预览/下载 |
| 10 | **U** 消息 / 企微 / OAuth2 | 3～4，企微另加 | [21](./21-sprint-u-oa-message.md) | 站内提醒；企微与 OAuth2 可后挂 |
| 11 | **V** WebSocket | 2～3 | [22](./22-sprint-v-websocket.md) | 在线推送 |
| 12 | **W** 异步投递 | 3～5，可选 | [23](./23-sprint-w-async-mq.md) | `pmhub_async`；默认可不用 RocketMQ |

未完成合计大约 **43～64 人日**（含可选企微与 W）。不含前端。

N、O 不挡业务，可插在任意周末做。R 的审批后半依赖 P。U3 依赖企业微信账号。

---

## 功能覆盖对照（排期用）

| pmhub 能力 | 状态 | Sprint |
|------------|------|--------|
| 用户/部门/岗位/菜单/角色/字典/参数/公告 | ✅ | A–C |
| 操作日志 / 登录日志 / 数据权限 | ✅ | B、D |
| 验证码 / 在线用户 / 限流 / 防重 | ✅ | J |
| 定时任务 + 日志 | ✅ | I5a、K |
| 项目生命周期、成员、收藏、归档、统计 | ✅ | E、I |
| 任务 CRUD、子任务、评论、导入导出、燃尽、逾期 | ✅ | F、G、I |
| 项目/任务附件 | ✅ | G |
| 审批主路径（启流、待办、通过、驳回） | ✅ 半成品 | H；收尾 **L** |
| 阶段 / 工时 / 动态类型 / 字段变更日志 | 未做 | **M** |
| 流程分类、模型、部署、表单、转办、抄送、我的申请 | 未做 | **P** |
| 用户导入导出、注册、头像、任务类型、模板、认领 | 未做 | **Q** |
| 物料 + 物料审批 | 未做 | **R** |
| 文档类型 / 文档 | 未做 | **S** |
| 公共文件 / 分享 | 未做 | **T** |
| 服务监控 / 缓存 / 连接池 | 未做 | **N** |
| 代码生成 | 未做 | **O** |
| 站内消息、企微、OAuth2 | 未做 | **U** |
| WebSocket | 未做 | **V** |
| 异步队列 / RocketMQ | 未做 | **W**（可选） |
| Swagger / OpenAPI | ✅ 已有注解 | 不单开 Sprint |

---

## 明确不做（做完上面也不追求）

这些在 pmhub 仓库或 README 里出现过，**不纳入「几乎所有功能」**：

- 微服务拆分、Nacos、Seata、SkyWalking、Gateway
- Elasticsearch / MongoDB / ChatGPT / 讯飞星火
- 阿里云 OSS（继续本地磁盘）
- 动态多数据源
- 在线表单设计器的拖拽前端（P 只保证表单 JSON/HTML 的存取 API）
- 必须部署 RocketMQ 才算完成（W 用数据库队列即可对齐行为）

---

## 建议排期块

| 块 | Sprint | 大约 | 交付感 |
|----|--------|------|--------|
| 块 1 | L + M | 1～2 周 | 项目审批和任务域跟 pmhub 同级 |
| 块 2 | P | 2 周 | 流程能配置，不再只有内置 BPMN |
| 块 3 | Q + R | 2 周 | 系统边角 + 物料 |
| 块 4 | S + T + N | 1～2 周 | 文档、网盘、监控 |
| 块 5 | O + U + V | 2 周 | 生成器、消息、推送 |
| 块 6 | W | 可砍 | 异步化 |

---

## 开 Sprint 习惯

1. 左边 pmhub 路径，右边 TaskForge 路径  
2. 写清「不做」  
3. 验收用接口 / 两个 Token  
4. 编译：`mvn -pl taskforge-admin -am -DskipTests compile`  
