# 前端 Sprint 05：工作流待办 / 审批 UI（对齐后端 H）

> **编号**：`plan/frontend/05`  
> **状态**：✅ **壳已落地**（待浏览器联调：发起 → 待办 → 同意/驳回）  
> **目标**：在 `taskforge-ui` 落地「发起审批 → 我的待办 → 同意/驳回」，并在项目/任务编辑侧体现「审批中不可手改」。  
> **前置**：后端 [08-sprint-h-workflow.md](../backend/08-sprint-h-workflow.md) 主路径已可用。  
> **不做**：表单设计器、企微待办、流程图可视化编辑、完整抄 pmhub 流程中心。

---

## 0. 进度

```text
✅ F05-1  api/workflow/* + COMPONENT_ALIAS（todo / finished）
✅ F05-2  待办列表 + 同意/驳回对话框；已办列表；部署内置 BPMN 按钮
✅ F05-3  项目/任务行「发起审批」→ ApproveStartDialog（选 approver）
✅ F05-4  任务编辑可改 status；提示审批中改状态会被后端拒绝
○ F05-5  自测联调（浏览器）
```

仍进 `building` 的菜单（本 Sprint 不做）：`workflow/category|form|model|deploy`、`workflow/work/create|own|claim|copy` 等。

---

## 1. 后端 API

| 动作 | 路径 |
|------|------|
| 待办 / 已办 | `GET /workflow/process/todoList` · `finishedList` |
| 部署内置 | `POST /workflow/process/deployBuiltin` |
| 同意 / 驳回 | `POST /workflow/task/complete` · `/reject` |
| 启任务/项目审批 | `POST /workflow/task/startTaskApprove` · `/startProjectApprove` |

Body：`{ taskId, approver }` / `{ projectId, approver }`；办理 `{ taskId, comment? }`。  
`approver` 为**用户 id 字符串**，须与登录人一致才会出现在其待办。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/workflow/process.js · task.js
  components/workflow/ApproveStartDialog.vue
  views/workflow/todo/index.vue
  views/workflow/finished/index.vue
  utils/permission.js          # workflow/work/todo|finished 别名
  views/project/list|task      # 发起审批
```

---

## 3. 验收标准

- [ ] Network 有 todo / complete（或 reject），`code===200`
- [ ] 发起任务审批后，办理人待办可见
- [ ] 同意后 `approved` 放开，可再改状态
- [ ] 审批中直接改状态失败，UI 有明确提示
- [ ] 驳回路径可走通

联调提示：先点待办页「部署内置流程」（若未部署）；发起时选自己的 userId（admin 一般为 `1`）。

---

## 4. 明确后置

- 流程定义在线部署 UI、多级会签 / 抄送  
- 其余 workflow/* 菜单页  
- 对接后端 J 的验证码登录页改动
