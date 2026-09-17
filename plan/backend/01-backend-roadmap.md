# 后端后续路线（A–G 之后）

> 只谈 **Java / API**。前端计划见 `plan/frontend/`，互不抢编号。  
> 对照总图：[00-pmhub-reference-map.md](./00-pmhub-reference-map.md)

---

## 已完成

| Sprint | 内容 |
|--------|------|
| A | 用户 CRUD |
| B | 登录/操作日志 + monitor |
| C | getInfo / Profile / Notice 等 |
| D | DataScope |
| E | 项目第一刀 |
| F | 任务 CRUD + 子任务 + 评论 |
| G | 文件上传 + 任务导入导出 |
| H | Flowable 主路径（H4 收尾可选，见 08 §0 / §5） |
| I | 查询工厂 / 成员 / 统计 / 逾期 Job（I1–I5a） |

模块现状：`common / framework / system / project / workflow / quartz / admin`。

---

## 接下来（按推荐顺序）

```text
✅ H  Flowable 工作流 + 项目/任务审批     → 08-sprint-h-workflow.md（主路径 ✅；H4 收尾可选）
✅ I  查询工厂 / 成员 / 统计 / 逾期 Job   → 09-sprint-i-job-stats.md（I1–I5a）
○ J  验证码 / 在线用户 / 限流 / 防重     → 10-sprint-j-system-guard.md
── 更后置 ──
   OA 企微、代码生成 generator、动态数据源、WebSocket、I5b 定时任务管理端
```

| 顺序 | 文档 | 为何这时做 |
|------|------|------------|
| — | [08-sprint-h-workflow.md](./08-sprint-h-workflow.md) | ✅ 主路径已落地；可选补项目禁改 / 业务 status 回写 |
| — | [09-sprint-i-job-stats.md](./09-sprint-i-job-stats.md) | ✅ 已完成（I1–I5a） |
| **1** | [10-sprint-j-system-guard.md](./10-sprint-j-system-guard.md) | 系统向护栏，可随时插队拆小块 |

---

## 开 Sprint 习惯

1. 左边 pmhub 路径，右边 TaskForge 路径  
2. 写清「不做」  
3. 验收用接口 / 两个 Token  
4. 编译：`mvn -pl taskforge-admin -am -DskipTests compile`  
