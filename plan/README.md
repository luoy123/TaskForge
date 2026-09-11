# TaskForge 计划索引

计划按端拆开，避免前后端 Sprint 挤在同一层：

```text
plan/
  README.md                 ← 本文件
  backend/                  ← Java / API（对照 pmhub-boot）
  frontend/                 ← taskforge-ui（Vue 3）
```

## 后端 `backend/`

编号在后端目录内自增（与历史 Sprint 字母 B–G 文件名一致）。

| 文档 | 内容 |
|------|------|
| [00-pmhub-reference-map.md](./backend/00-pmhub-reference-map.md) | pmhub 对照知识图 |
| [02-sprint-b-monitor-logging.md](./backend/02-sprint-b-monitor-logging.md) | Sprint B 监控日志 |
| [03-sprint-c-system-polish.md](./backend/03-sprint-c-system-polish.md) | Sprint C 系统打磨 |
| [04-sprint-d-datascope.md](./backend/04-sprint-d-datascope.md) | Sprint D 数据权限 |
| [05-sprint-e-project.md](./backend/05-sprint-e-project.md) | Sprint E 项目 |
| [06-sprint-f-task.md](./backend/06-sprint-f-task.md) | Sprint F 任务 |
| [07-sprint-g-file.md](./backend/07-sprint-g-file.md) | Sprint G 文件 / 导入导出 |

## 前端 `frontend/`

**单独编号**：`01-`、`02-`…，不与后端 `02–07` 抢号。

| 文档 | 内容 |
|------|------|
| [01-sprint-h-ui.md](./frontend/01-sprint-h-ui.md) | 前端 Sprint 01：UI 骨架与系统管理页（已完成） |
| [02-sprint-project-task.md](./frontend/02-sprint-project-task.md) | 前端 Sprint 02：项目列表 + 任务列表（已完成） |
| [03-sprint-dynamic-routes.md](./frontend/03-sprint-dynamic-routes.md) | 前端 Sprint 03：getRouters 动态路由 + 侧栏（已完成 · 可选收尾静态菜单） |

## 约定

- **新后端计划** → `plan/backend/`，后端内编号延续
- **新前端计划** → `plan/frontend/`，前端内从 `01-` 自增（`02-`、`03-`…）
- 跨端引用写相对路径，例如：`../frontend/01-sprint-h-ui.md`
- 不要把计划写到仓库根或其它 docs 目录
