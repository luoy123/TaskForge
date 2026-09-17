# TaskForge 计划索引

计划按端拆开，避免前后端 Sprint 挤在同一层：

```text
plan/
  README.md                 ← 本文件
  backend/                  ← Java / API（对照 pmhub-boot）
  frontend/                 ← taskforge-ui（Vue 3）
```

仿写原则：技术栈是 **Boot 3 + R\<T\>**（前端 Vue3），心智对齐 pmhub，不 1:1 抄类名。细节见 [backend/00-pmhub-reference-map.md](./backend/00-pmhub-reference-map.md)。

## 后端 `backend/`（当前主线）

```text
✅ A–G   系统 / DataScope / 项目 / 任务 / 文件导入导出
✅ H      Flowable 审批联动（主路径 ✅；H4 收尾可选）
✅ I      查询工厂 / 成员 / 统计 / 逾期 Job（I1–I5a）
○ J      验证码 / 在线用户 / 限流 / 防重
后置     OA / generator / I5b 定时任务管理端
```

总览：[backend/01-backend-roadmap.md](./backend/01-backend-roadmap.md)

| 文档 | 内容 | 状态 |
|------|------|------|
| [00-pmhub-reference-map.md](./backend/00-pmhub-reference-map.md) | pmhub 对照知识图 | 活文档 |
| [01-backend-roadmap.md](./backend/01-backend-roadmap.md) | A–G 之后后端路线 | 活文档 |
| [02-sprint-b-monitor-logging.md](./backend/02-sprint-b-monitor-logging.md) | Sprint B 监控日志 | ✅ |
| [03-sprint-c-system-polish.md](./backend/03-sprint-c-system-polish.md) | Sprint C 系统打磨 | ✅ |
| [04-sprint-d-datascope.md](./backend/04-sprint-d-datascope.md) | Sprint D 数据权限 | ✅ |
| [05-sprint-e-project.md](./backend/05-sprint-e-project.md) | Sprint E 项目 | ✅ |
| [06-sprint-f-task.md](./backend/06-sprint-f-task.md) | Sprint F 任务 | ✅ |
| [07-sprint-g-file.md](./backend/07-sprint-g-file.md) | Sprint G 文件 / 导入导出 | ✅ |
| [08-sprint-h-workflow.md](./backend/08-sprint-h-workflow.md) | Sprint H Flowable | ✅ 主路径（H4 收尾可选） |
| [09-sprint-i-job-stats.md](./backend/09-sprint-i-job-stats.md) | Sprint I 统计 / 逾期 Job | ✅ I1–I5a |
| [10-sprint-j-system-guard.md](./backend/10-sprint-j-system-guard.md) | Sprint J 系统护栏 | ○ |

## 前端 `frontend/`

单独编号，不与后端抢号。已完成 01–04。

```text
✅ 01–03  登录 / 系统 CRUD / 项目任务列表 / 动态路由
✅ 04      权限 + 文件 + 成员 / Excel / 我的任务 / 首页统计
✅ 05      工作流 UI（待办 / 已办 / 发起审批）
○ 06      系统缺页 / 对接 J（候选）
```

| 文档 | 内容 | 状态 |
|------|------|------|
| [01-sprint-h-ui.md](./frontend/01-sprint-h-ui.md) | UI 骨架与系统管理页 | ✅ |
| [02-sprint-project-task.md](./frontend/02-sprint-project-task.md) | 项目列表 + 任务列表 | ✅ |
| [03-sprint-dynamic-routes.md](./frontend/03-sprint-dynamic-routes.md) | getRouters 动态路由 | ✅ |
| [04-sprint-file-perm-deepen.md](./frontend/04-sprint-file-perm-deepen.md) | 权限 + 文件 + 成员/统计等 | ✅ |
| [05-sprint-workflow-ui.md](./frontend/05-sprint-workflow-ui.md) | 待办 / 审批 UI | ✅ 壳 |

## 怎么选下一刀

| 你想… | 去做 |
|------|------|
| 联调审批闭环 | [frontend/05-sprint-workflow-ui.md](./frontend/05-sprint-workflow-ui.md) §3 |
| 补 H4 收尾（项目禁改 / 业务 status 回写） | [backend/08-sprint-h-workflow.md](./backend/08-sprint-h-workflow.md) §5 |
| 系统护栏 / 缺页 | [backend/10-sprint-j-system-guard.md](./backend/10-sprint-j-system-guard.md) 或前端 06 |

## 约定

- **新后端计划** → `plan/backend/`，后端内编号延续
- **新前端计划** → `plan/frontend/`，前端内从 `01-` 自增
- 跨端引用写相对路径
- 不要把计划写到仓库根或其它 docs 目录
