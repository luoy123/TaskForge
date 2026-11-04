# 前端 Sprint 08：审批状态打磨（对齐后端 L）

> **编号**：`plan/frontend/08`  
> **状态**：代码已完成，待联调验收（列表出参已补 `approved`；后端 [L](../backend/12-sprint-l-workflow-polish.md) L1～L3 已写）  
> **目标**：在已有「发起审批 / 待办」上，让用户看懂审批中、通过、驳回后的业务状态，并在改状态被拒时看到明确文案。  
> **前置**：前端 [05](./05-sprint-workflow-ui.md) 壳可用；后端 L1～L3（启流校验、在审禁改、通过/驳回回写 status）。  
> **不做**：流程设计器、抄送、转办、多级会签（→ [10](./10-sprint-workflow-platform.md)）。

---

## 0. 与后端约定对齐

后端 L 定死的规则（前端只展示，不自己改状态码）：

| 动作 | `approved` | 业务 `status` |
|------|------------|----------------|
| 任务通过 | `1` | 进行中 `1` |
| 任务驳回 | `2`，清实例 | 未开始 `0` |
| 项目通过 | `1` | 进行中 `1` |
| 项目驳回 | `2`，清实例 | 保持未开始 `0` |
| 在审 | `0` | `edit` 若改 status 则失败，文案「审批中不可修改状态」 |
| 重复启流 | — | 「审批中，请勿重复提交」 |

```text
F08-1  项目/任务行或编辑框展示审批态（审批中 / 已通过 / 已驳回）
F08-2  审批中禁用「状态」控件；提交被拒时弹出后端 message
F08-3  待办同意/驳回成功后，回到列表能看到 status 已变
F08-4  重复点「发起审批」失败有文案，不白屏
```

建议顺序：**1 → 2 → 3 → 4**。

**协作**：状态标签与禁用壳由助手写；列表字段映射、错误提示是否重复弹窗搜 `【学员填写】`（`request` 拦截器已会 `ElMessage.error`）。

---

## 1. 接口（不新开，沿用 05）

| 动作 | 方法 | 路径 |
|------|------|------|
| 启任务/项目审批 | POST | `/workflow/task/startTaskApprove` · `/startProjectApprove` |
| 同意 / 驳回 | POST | `/workflow/task/complete` · `/reject` |
| 改任务/项目 | POST | 现有 `/project/task/edit` · `/project/edit` |

列表出参若已有 `status` / `statusName` / `approved`，直接绑；没有 `approved` 就只靠失败文案和 status 变化验收，不要前端猜。

---

## 2. 前端落位

```text
taskforge-ui/src/
  views/project/list/index.vue       # 行上审批态 + 发起按钮禁用条件
  views/project/task/index.vue
  components/project/TaskDetailDrawer.vue   # 状态控件
  components/workflow/ApproveStartDialog.vue
  views/workflow/todo/index.vue      # 办结后提示「回列表看状态」
```

不新菜单。

---

## 3. 分阶段要点

### F08-1 展示

- `approved === '0'`（或数字 0）显示「审批中」标签。
- 通过/驳回用现有 `statusName`，不要再维护一套中文映射，除非接口没给 name。

### F08-2 禁改

- 审批中：状态下拉 `disabled`。标题等其它字段按后端：MVP 若整单 edit 带了不同 status 会被拒，前端提交前去掉未改的 status，或直接不传 status。
- 失败文案交给拦截器；页面 `catch` 里不要再弹一次同样的错。

### F08-3 / F08-4 闭环

- 同意后刷新任务列表，状态为进行中；驳回后为未开始。
- 项目驳回后状态仍是未开始。
- 审批中再点发起：Message 含「请勿重复提交」或后端原文。

---

## 4. 验收标准

- [ ] 发起后列表出现「审批中」，编辑里不能改状态
- [ ] 强行提交改状态，提示「审批中不可修改状态」
- [ ] 同意后任务/项目为进行中，且可以再改状态
- [ ] 驳回后任务回到未开始；项目保持未开始
- [ ] 重复发起失败且有文案

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

流程配置页见 [10-sprint-workflow-platform.md](./10-sprint-workflow-platform.md)（后端 P）。
