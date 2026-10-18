# Sprint L：工作流打磨（H4 收尾 · 自学版）

> **状态**：排队（建议 K 之后，或与 K 并行由你选）  
> **目标**：把 Sprint H 留下的审批缺口补齐，使「启流 → 在审禁改 → 通过/驳回回写业务状态」与 pmhub 心智一致。  
> **前置**：H 主路径（`start*Approve` / todo / complete / reject / `approved` 回写）。  
> **不做**：表单设计器、抄送全套、企微待办、多实例会签、前端大改（API 优先）。

---

## 0. H 文档已标缺口（对照 [08](./08-sprint-h-workflow.md) §0）

```text
L1  启流前校验：任务/项目存在、状态允许、未在审
L2  项目 edit：审批中（approved=0）禁止改状态（对齐任务 edit）
L3  complete / reject 回写业务 status（不只改关联表 approved）
L4  （可选）claim 签收；驳回策略文档化并单测式 curl
L5  （可选）分类 / 模型最小 CRUD；或继续只用内置 bpmn
```

| 项 | 建议行为（写进实现前先定死） |
|----|------------------------------|
| 任务审批通过 | `approved=1`；任务 status → 进行中或保持原约定（对照 pmhub，选一种写清） |
| 任务审批驳回 | `approved=2` 或清空关联；status 回退到未开始/待认领（选一种） |
| 项目审批通过 | 项目可进入「进行中」；驳回保持未开始 |
| 在审时 | 禁止改 status；允许改标题/描述可另议（建议 MVP：整单 edit 中与 status 相关字段全拒） |

---

## 1. 落位提示

```text
taskforge-workflow/
  WfTaskServiceImpl          # complete/reject 后回调业务
  WfProcessServiceImpl       # start 前校验可放这里或 project

taskforge-project/
  ProjectServiceImpl#edit    # L2
  ProjectTaskServiceImpl#edit# 已有任务禁改，核对是否覆盖所有入口
```

避免环依赖：回写可用
- workflow 内直接 Mapper 更新项目/任务表（简单但耦合），或  
- admin/application 层事件、`IApprovalCallback` 接口由 project 实现。

---

## 2. API

多数仍走现有：

| 已有 | 本 Sprint 加强点 |
|------|------------------|
| `POST .../startTaskApprove` | 不存在 / 已在审 → 明确失败码与文案 |
| `POST .../startProjectApprove` | 同上 |
| complete / reject | 成功后查库：`approved` + 业务 `status` 符合约定 |
| （可选）claim | `POST /workflow/task/claim` |

---

## 3. 验收

- [ ] 启流非法 id / 重复启流失败且无脏 process 行  
- [ ] 项目在审时改状态失败；通过后可改  
- [ ] complete 后任务（或项目）status 按约定变化  
- [ ] reject 后可再次启流或按约定锁定（行为与文档一致）  
- [ ] 两个 Token：办理人可办，无关用户待办为空  

---

## 4. 再往后

- [Sprint M](./13-sprint-m-project-deepen.md)  
- 前端 05 联调加深（非本文件范围）  
