# TaskForge 计划索引

**当前做到哪 → [PROGRESS.md](./PROGRESS.md)**（新对话从这里接续）

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
✅ A–L   系统 → 项目任务文件 → Flowable 主路径 → 统计逾期 → 护栏 → 定时任务 → 审批打磨
○ M→W    见总排期，做完即覆盖 pmhub-boot 业务功能面
```

总览（含人日与覆盖表）：[backend/01-backend-roadmap.md](./backend/01-backend-roadmap.md)

| 文档 | 内容 | 状态 |
|------|------|------|
| [00-pmhub-reference-map.md](./backend/00-pmhub-reference-map.md) | pmhub 对照知识图 | 活文档 |
| [01-backend-roadmap.md](./backend/01-backend-roadmap.md) | 后端总路线 | 活文档 |
| [02](./backend/02-sprint-b-monitor-logging.md)～[07](./backend/07-sprint-g-file.md) | B–G | ✅ |
| [08-sprint-h-workflow.md](./backend/08-sprint-h-workflow.md) | H Flowable | ✅ 主路径 |
| [09-sprint-i-job-stats.md](./backend/09-sprint-i-job-stats.md) | I 统计 / 逾期 | ✅ I5a |
| [10-sprint-j-system-guard.md](./backend/10-sprint-j-system-guard.md) | J 护栏 | ✅ |
| [11-sprint-k-quartz-admin.md](./backend/11-sprint-k-quartz-admin.md) | K Job 管理端 | ✅ API（验收勾选可再补） |
| [12-sprint-l-workflow-polish.md](./backend/12-sprint-l-workflow-polish.md) | L 审批打磨 | ✅ |
| [13-sprint-m-project-deepen.md](./backend/13-sprint-m-project-deepen.md) | M 项目域加深 | ✅ |
| [14-sprint-n-monitor-server.md](./backend/14-sprint-n-monitor-server.md) | N 服务/缓存监控 | ○ 下一刀 |
| [15-sprint-o-generator.md](./backend/15-sprint-o-generator.md) | O 代码生成 | ○ |
| [16-sprint-p-workflow-platform.md](./backend/16-sprint-p-workflow-platform.md) | P 流程平台 | ○ |
| [17-sprint-q-system-task-gap.md](./backend/17-sprint-q-system-task-gap.md) | Q 系统/任务边角 | ○ |
| [18-sprint-r-materials.md](./backend/18-sprint-r-materials.md) | R 物料 | ○ |
| [19-sprint-s-document.md](./backend/19-sprint-s-document.md) | S 文档 | ○ |
| [20-sprint-t-public-file.md](./backend/20-sprint-t-public-file.md) | T 公共文件 | ○ |
| [21-sprint-u-oa-message.md](./backend/21-sprint-u-oa-message.md) | U 消息/企微/OAuth2 | ○ |
| [22-sprint-v-websocket.md](./backend/22-sprint-v-websocket.md) | V WebSocket | ○ |
| [23-sprint-w-async-mq.md](./backend/23-sprint-w-async-mq.md) | W 异步投递 | ○ 可选 |

## 前端 `frontend/`

编号不跟后端字母抢。01–06 与定时任务页已落地；07 起对齐「还缺的页」和后端 L–W。

```text
✅ 01–06   骨架 / 项目任务 / 动态路由 / 文件权限 / 审批壳 / 验证码与在线用户
✅ 定时任务  views/monitor/job（对齐后端 K，无单独编号）
✅ 07       岗位/字典/参数/公告/日志 + 角色菜单树（F07-7）
○ 08–19    等对应后端 Sprint 再写 UI（见下表；08 待联调）
```

| 文档 | 对齐后端 | 状态 |
|------|----------|------|
| [01](./frontend/01-sprint-h-ui.md)～[06](./frontend/06-sprint-captcha-online.md) | A–J | ✅ |
| [07-sprint-system-pages.md](./frontend/07-sprint-system-pages.md) | B/C 缺页 | ✅（含 F07-7 角色菜单树） |
| [08-sprint-workflow-polish.md](./frontend/08-sprint-workflow-polish.md) | L | ▶ 待联调 |
| [09-sprint-project-deepen.md](./frontend/09-sprint-project-deepen.md) | M | ✅ |
| [10-sprint-workflow-platform.md](./frontend/10-sprint-workflow-platform.md) | P | ○ |
| [11-sprint-system-task-gap.md](./frontend/11-sprint-system-task-gap.md) | Q | ○ |
| [12-sprint-materials.md](./frontend/12-sprint-materials.md) | R | ○ |
| [13-sprint-document.md](./frontend/13-sprint-document.md) | S | ○ |
| [14-sprint-public-file.md](./frontend/14-sprint-public-file.md) | T | ○ |
| [15-sprint-monitor.md](./frontend/15-sprint-monitor.md) | N | ○ |
| [16-sprint-generator.md](./frontend/16-sprint-generator.md) | O | ○ |
| [17-sprint-message.md](./frontend/17-sprint-message.md) | U | ○ |
| [18-sprint-websocket.md](./frontend/18-sprint-websocket.md) | V | ○ |
| [19-sprint-async-admin.md](./frontend/19-sprint-async-admin.md) | W 可选 | ○ |

## 怎么选下一刀

| 你想… | 去做 |
|------|------|
| 前端少点「建设中」 | [07](./frontend/07-sprint-system-pages.md) 已完成；角色菜单树见 F07-7 |
| 审批联调 / 前端状态展示 | [08](./frontend/08-sprint-workflow-polish.md) + 后端 [L](./backend/12-sprint-l-workflow-polish.md) |
| 后端按总排期继续 | [N](./backend/14-sprint-n-monitor-server.md) 起；M 与前端 09 均已完成 |
| 定时任务冒烟 | 已有 `views/monitor/job` |

## 约定

- **新后端计划** → `plan/backend/`，编号延续  
- **新前端计划** → `plan/frontend/`，从 `01-` 自增  
- 跨端引用写相对路径  
- 不要把计划写到仓库根或其它 docs 目录  
