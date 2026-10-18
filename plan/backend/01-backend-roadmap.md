# 后端后续路线（对照 pmhub-boot）

> 只谈 **Java / API**。前端见 `plan/frontend/`。  
> 对照总图：[00-pmhub-reference-map.md](./00-pmhub-reference-map.md)

---

## 已完成

| Sprint | 内容 |
|--------|------|
| A–G | 系统 / DataScope / 项目 / 任务 / 文件 |
| H | Flowable 主路径（H4 收尾 → L） |
| I | 查询工厂 / 成员 / 统计 / 逾期 Job（I5a） |
| J | 验证码 / 在线用户 / 限流 / 防重 |
| K | 定时任务管理端 CRUD + 日志（I5b） |

模块：`common / framework / system / project / workflow / quartz / admin`。

---

## 接下来

```text
✅ K  定时任务管理端（I5b CRUD + 日志）     → 11-sprint-k-quartz-admin.md
○ L  工作流打磨（H4：禁改 / status 回写） → 12-sprint-l-workflow-polish.md  ← 后端下一刀
○ M  项目域加深（阶段 / 工时 / 动态）     → 13-sprint-m-project-deepen.md
○ N  服务 / 缓存监控                     → 14-sprint-n-monitor-server.md
○ O  代码生成 generator                  → 15-sprint-o-generator.md
── 后置 ──
   OA 企微（无企业微信可跳过）
   动态数据源 / WebSocket
```

| 顺序 | 文档 | 为何这时做 |
|------|------|------------|
| — | [11-sprint-k-quartz-admin.md](./11-sprint-k-quartz-admin.md) | ✅ API 已齐；前端 job 页已有 |
| **1** | [12-sprint-l-workflow-polish.md](./12-sprint-l-workflow-polish.md) | 收掉 H4：项目在审禁改、业务 status 回写 |
| **2** | [13-sprint-m-project-deepen.md](./13-sprint-m-project-deepen.md) | 阶段/工时/日志工厂，对齐 pmhub-project 中层 |
| **3** | [14-sprint-n-monitor-server.md](./14-sprint-n-monitor-server.md) | 运维演示向 |
| **4** | [15-sprint-o-generator.md](./15-sprint-o-generator.md) | 学习向，非业务阻断 |

---

## 开 Sprint 习惯

1. 左边 pmhub 路径，右边 TaskForge 路径  
2. 写清「不做」  
3. 验收用接口 / 两个 Token  
4. 编译：`mvn -pl taskforge-admin -am -DskipTests compile`  
