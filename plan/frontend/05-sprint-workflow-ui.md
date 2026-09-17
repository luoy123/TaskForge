# 前端 Sprint 05：工作流待办 / 审批 UI（对齐后端 H）

> **编号**：`plan/frontend/05`  
> **状态**：排队（**等后端 Sprint H 的 H3–H5 业务跑通**再开）  
> **目标**：在 `taskforge-ui` 落地「发起审批 → 我的待办 → 同意/驳回」，并在项目/任务编辑侧体现「审批中不可手改」。  
> **前置**：后端 [08-sprint-h-workflow.md](../backend/08-sprint-h-workflow.md) 验收勾完；前端 03 动态路由可用（流程菜单不再进 `building`，或本 Sprint 配 `COMPONENT_ALIAS`）。  
> **不做**：表单设计器、企微待办、流程图可视化编辑、完整抄 pmhub 流程中心。

---

## 0. 为何现在不急着写

| 后端 H | 现状（写计划时） |
|--------|------------------|
| H1 模块骨架 | ✅ |
| H2 BPMN 部署 | ✅ |
| H3 start / todo / complete / reject | ⬜ Service TODO |
| H4 项目/任务联动 + 审批中禁改 | ⬜ |
| H5 Controller 入口 | ✅ 已挂，依赖 H3/H4 |

前端若现在接，只能打到空实现或半成品。**优先把后端 H 填完，再开本 Sprint。**  
同期前端可继续做 [04](./04-sprint-file-perm-deepen.md)（G/I）。

---

## 1. 范围（H 通过后）

```text
F05-1  api/workflow/* + 视图映射（菜单 component → views）
F05-2  我的待办列表（todo）+ 办理抽屉（complete / reject + 意见）
F05-3  项目/任务行：发起审批（startTaskApprove / startProjectApprove）
F05-4  编辑态：审批中（approved=0）禁用改状态，展示文案
F05-5  自测：发起 → 待办可见 → 通过/驳回 → 业务状态回写
```

### 后端 API（以 H 落地为准）

| 动作 | 建议路径 |
|------|----------|
| 待办列表 | 以 `WfProcessController` / todo 接口为准（H 文档） |
| 同意 / 驳回 | POST `/workflow/task/complete` · `/reject` |
| 启任务审批 | POST `/workflow/task/startTaskApprove` |
| 启项目审批 | POST `/workflow/task/startProjectApprove` |

Body 字段对照 `WfTaskBo` / H 计划（`taskId`、`approver`、`comment` 等）。

---

## 2. 前端落位（建议）

```text
taskforge-ui/src/
  api/workflow/
    task.js
    process.js
  views/workflow/
    todo/index.vue          # 我的待办
    # 已办 / 发起记录可后置
  views/project/
    list/index.vue          # 「发起审批」按钮
    task/index.vue          # 同上 + 审批中禁用
  utils/permission.js       # COMPONENT_ALIAS：pmhub 流程 path → 本仓库 views
```

侧栏：后端 `getRouters` 里「流程管理」在映射好后应打开真实页，而不是 `building.vue`。

---

## 3. 验收标准

- [ ] Network 有 todo / complete（或 reject），`code===200`
- [ ] 发起任务审批后，办理人待办可见
- [ ] 同意后任务/项目状态按后端约定更新，可再编辑
- [ ] 审批中直接改状态失败，UI 有明确提示（接口错误或前置禁用）
- [ ] 驳回路径可走通（策略以后端为准）

---

## 4. 明确后置

- 流程定义在线部署 UI  
- 多级会签 / 抄送  
- 对接后端 J 的验证码登录页改动（与审批无关）
