# TaskForge 计划索引

计划按端拆开，避免前后端 Sprint 挤在同一层：

```text
plan/
  README.md                 ← 本文件
  backend/                  ← Java / API（对照 pmhub-boot）
  frontend/                 ← taskforge-ui（Vue 3）
```

仿写原则：技术栈是 **Boot 3 + R\<T\>**（前端 Vue3），心智对齐 pmhub，不 1:1 抄类名。细节见 [backend/00-pmhub-reference-map.md](./backend/00-pmhub-reference-map.md)。

## 后端 `backend/`

```text
✅ A–J   系统 → 项目任务文件 → Flowable → 统计逾期 → 护栏
✅ K      定时任务管理端（API 已齐；前端 job 页已有）
○ L      工作流打磨（H4 收尾）
○ M      项目域加深（阶段/工时/动态）
○ N      服务 / 缓存监控
○ O      代码生成
后置     OA 企微 / 动态数据源 / WebSocket
```

总览：[backend/01-backend-roadmap.md](./backend/01-backend-roadmap.md)

| 文档 | 内容 | 状态 |
|------|------|------|
| [00-pmhub-reference-map.md](./backend/00-pmhub-reference-map.md) | pmhub 对照知识图 | 活文档 |
| [01-backend-roadmap.md](./backend/01-backend-roadmap.md) | 后端总路线 | 活文档 |
| [02](./backend/02-sprint-b-monitor-logging.md)～[07](./backend/07-sprint-g-file.md) | B–G | ✅ |
| [08-sprint-h-workflow.md](./backend/08-sprint-h-workflow.md) | H Flowable | ✅ 主路径 |
| [09-sprint-i-job-stats.md](./backend/09-sprint-i-job-stats.md) | I 统计 / 逾期 | ✅ I5a |
| [10-sprint-j-system-guard.md](./backend/10-sprint-j-system-guard.md) | J 护栏 | ✅ |
| [11-sprint-k-quartz-admin.md](./backend/11-sprint-k-quartz-admin.md) | K Job 管理端 | ✅ API（验收勾选可再补） |
| [12-sprint-l-workflow-polish.md](./backend/12-sprint-l-workflow-polish.md) | L 审批打磨 | ○ |
| [13-sprint-m-project-deepen.md](./backend/13-sprint-m-project-deepen.md) | M 项目域加深 | ○ |
| [14-sprint-n-monitor-server.md](./backend/14-sprint-n-monitor-server.md) | N 服务/缓存监控 | ○ |
| [15-sprint-o-generator.md](./backend/15-sprint-o-generator.md) | O 代码生成 | ○ |

## 前端 `frontend/`

```text
✅ 01–06  登录 → 系统 CRUD → 项目任务 → 动态路由 → 文件权限 → 工作流壳 → 验证码/在线用户
✅ K 对齐  views/monitor/job（定时任务 + 调度日志抽屉）已落地
▶ 07      系统缺页：岗位/字典/参数/公告/登录·操作日志  ← 推荐
○ 08      等后端 L/M/N 再开（审批打磨 / 阶段工时 / Server 监控）
```

| 文档 | 内容 | 状态 |
|------|------|------|
| [01](./frontend/01-sprint-h-ui.md)～[03](./frontend/03-sprint-dynamic-routes.md) | 骨架 / 项目任务 / 动态路由 | ✅ |
| [04-sprint-file-perm-deepen.md](./frontend/04-sprint-file-perm-deepen.md) | 权限 + 文件 + 成员/统计 | ✅ |
| [05-sprint-workflow-ui.md](./frontend/05-sprint-workflow-ui.md) | 待办 / 审批 UI | ✅ 壳 |
| [06-sprint-captcha-online.md](./frontend/06-sprint-captcha-online.md) | 验证码 + 在线用户 | ✅ |
| [07-sprint-system-pages.md](./frontend/07-sprint-system-pages.md) | 系统缺页补齐 | ▶ **推荐** |

## 怎么选下一刀

| 你想… | 去做 |
|------|------|
| 前端少点「建设中」 | [frontend/07-sprint-system-pages.md](./frontend/07-sprint-system-pages.md) |
| 继续啃后端审批打磨 | [backend/12-sprint-l-workflow-polish.md](./backend/12-sprint-l-workflow-polish.md) |
| 后端项目域加深 | [backend/13-sprint-m-project-deepen.md](./backend/13-sprint-m-project-deepen.md) |
| 定时任务联调冒烟 | 已有 `views/monitor/job` + `/monitor/job*` |

## 约定

- **新后端计划** → `plan/backend/`，编号延续  
- **新前端计划** → `plan/frontend/`，从 `01-` 自增  
- 跨端引用写相对路径  
- 不要把计划写到仓库根或其它 docs 目录  
