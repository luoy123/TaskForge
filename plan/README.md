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
▶ H      Flowable 审批联动          ← 当前焦点
○ I      查询工厂 / 成员 / 统计 / 逾期 Job
○ J      验证码 / 在线用户 / 限流 / 防重
后置     OA / generator
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
| [08-sprint-h-workflow.md](./backend/08-sprint-h-workflow.md) | Sprint H Flowable | ▶ **当前** |
| [09-sprint-i-job-stats.md](./backend/09-sprint-i-job-stats.md) | Sprint I 统计 / 逾期 Job | ○ |
| [10-sprint-j-system-guard.md](./backend/10-sprint-j-system-guard.md) | Sprint J 系统护栏 | ○ |

## 前端 `frontend/`

单独编号，不与后端抢号。已完成 01–03；04 为前端侧候选，**不挡后端 H**。

| 文档 | 内容 | 状态 |
|------|------|------|
| [01-sprint-h-ui.md](./frontend/01-sprint-h-ui.md) | UI 骨架与系统管理页 | ✅ |
| [02-sprint-project-task.md](./frontend/02-sprint-project-task.md) | 项目列表 + 任务列表 | ✅ |
| [03-sprint-dynamic-routes.md](./frontend/03-sprint-dynamic-routes.md) | getRouters 动态路由 | ✅ |
| [04-sprint-file-perm-deepen.md](./frontend/04-sprint-file-perm-deepen.md) | 按钮权限 + 文件 UI 等 | ○ 候选 |

## 约定

- **新后端计划** → `plan/backend/`，后端内编号延续
- **新前端计划** → `plan/frontend/`，前端内从 `01-` 自增
- 跨端引用写相对路径
- 不要把计划写到仓库根或其它 docs 目录
