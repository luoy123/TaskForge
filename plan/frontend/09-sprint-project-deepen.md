# 前端 Sprint 09：阶段 / 工时 / 动态（对齐后端 M）

> **编号**：`plan/frontend/09`  
> **状态**：排队（等后端 [M](../backend/13-sprint-m-project-deepen.md)）  
> **目标**：在项目里维护阶段、在任务上登记工时，并把任务动态按类型分开看。  
> **前置**：前端 04 的任务抽屉、评论、项目列表可用；后端阶段 / 工时 / `log/list` 的 `logType` 已通。  
> **不做**：甘特图、企微提醒、提醒记录管理页（M5 可选，本 Sprint 默认不做）。

---

## 0. 范围

```text
F09-1  api：stage / workTime；log 请求带上 logType
F09-2  项目侧：阶段列表、新增、改名、删除、设为当前
F09-3  任务抽屉：登记工时、列表、合计小时
F09-4  动态：全部 / 动态 / 交付物 / 评论 四个筛选
F09-5  编辑任务名称或截止时间后，动态里出现「A → B」类文案
```

建议顺序：**1 → 2 → 4 → 3 → 5**。

**协作**：API 与抽屉壳由助手写；列表、提交、删除搜 `【学员填写】`。

---

## 1. 后端接口速查

| 动作 | 方法 | 路径 | Body 要点 |
|------|------|------|-----------|
| 阶段列表 | 以 Controller 为准 | `/project/stage/list` | `projectId` |
| 新增 / 改名 / 删除 | POST 等 | `/project/stage/add` · `edit` · `delete` | 名称；删除失败文案「阶段下仍有任务」 |
| 设当前 | POST | `/project/stage/setCurrent`（路径以落地为准） | `projectId` + 阶段 id |
| 登记工时 | POST | `/project/task/workTime/add` | `taskId`、`hours`（小时，一位小数；`<=0` 拒绝） |
| 工时列表 | POST | `/project/task/workTime/list` | `taskId`；含合计 |
| 删除工时 | POST/DELETE | `/project/task/workTime/delete` | 只能删自己的，超管除外 |
| 动态 | POST | `/project/task/log/list` | 已有；增加 `logType`：空 / `dynamic` / `deliverable` / `comment` |

`userId` 不要从前端传，后端用当前登录人。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/project/stage.js
  api/project/task.js          # + workTime、log 增加 logType
  components/project/
    StageDrawer.vue            # 从项目行打开
    TaskDetailDrawer.vue       # 工时区 + 动态筛选
  views/project/list/index.vue # 「阶段」按钮
```

---

## 3. 分阶段要点

### F09-2 阶段

- 表格：名称、顺序。操作：改名、删除、设为当前（当前行打标）。
- 删除二次确认；失败展示后端原文。
- 设为当前后，新建任务的阶段应是这一条（可在任务列表看阶段列，没有列就在详情里看）。

### F09-3 工时

- 输入小时数 + 备注（后端有字段再显示）。
- 表格：登记人、小时、时间；底部「合计 x 小时」。
- 非本人记录不显示删除（或点了被拒）。

### F09-4 / F09-5 动态

- `el-radio-group` 四个值对应 `logType`。
- 评论提交后切到「评论」能看到刚写的那条。
- 改任务名称再打开动态，能看到变更说明；没改字段不应多一条空日志。

---

## 4. 验收标准

- [ ] 阶段增改、设当前、删除空阶段成功；有任务的阶段删不掉
- [ ] 设当前后新建任务落在该阶段
- [ ] 工时登记后列表与合计正确；0 或负数被拒
- [ ] 只能删自己的工时
- [ ] 日志四种筛选结果不同；编辑任务产生变更说明

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

项目级日志（不只单任务）若后端 Q8 做了，可在 [11](./11-sprint-system-task-gap.md) 加一个项目动态抽屉，本 Sprint 不扩。
