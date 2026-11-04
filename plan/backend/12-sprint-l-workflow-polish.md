# Sprint L：工作流打磨（H4 收尾 · 自学版）

> **状态**：排队（后端下一刀）  
> **估算**：2～3 人日  
> **目标**：把 Sprint H 留下的审批缺口补齐，使「启流 → 在审禁改 → 通过/驳回回写业务状态」与 pmhub 心智一致。  
> **前置**：H 主路径（`start*Approve` / todo / complete / reject / `approved` 回写）。  
> **不做**：表单设计器、抄送全套、企微待办、多实例会签、前端大改（API 优先）。

## 文件对照

1. pmhub 根目录按你本机的 `pmhub-boot`（其下就是 `pmhub-workflow/`、`pmhub-project/`）。

| 看 pmhub                                                                       | TaskForge                                                                              |
| ----------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| `pmhub-workflow/.../service/impl/WfTaskServiceImpl.java`（complete / reject）   | **改** `taskforge-workflow/.../service/impl/WfTaskServiceImpl.java`                     |
| `pmhub-workflow/.../domain/WfTaskProcess.java`                                | **已有** `taskforge-workflow/.../domain/WfTaskProcess.java`，一般不新建                        |
| `pmhub-project/.../service/impl/ProjectTaskServiceImpl.java`（edit 里 approved） | **改** `taskforge-project/.../service/impl/ProjectTaskServiceImpl.java`                 |
| `pmhub-project/.../service/impl/ProjectServiceImpl.java`（项目 edit）             | **改** `taskforge-project/.../service/impl/ProjectServiceImpl.java`                     |
| `pmhub-admin/.../controller/workflow/WfTaskController.java`                   | **改** `taskforge-admin/.../controller/workflow/WfTaskController.java`（只补校验失败时的返回，接口已有） |

**本 Sprint 不新建模块。** 若要解耦回写，可选新建 `taskforge-project/.../service/IApprovalStatusCallback.java`，由 workflow 调它；不建也行，直接在 `WfTaskServiceImpl` 里更新项目/任务状态。

---

## 0. 过程

先定死回写规则，再写代码，避免通过/驳回各写一套：

| 动作   | `approved`                 | 业务 `status`                           |
| ---- | -------------------------- | ------------------------------------- |
| 任务通过 | `1`                        | 改为进行中 `1`                             |
| 任务驳回 | `2`，清掉 `processInstanceId` | 回到未开始 `0`                             |
| 项目通过 | `1`                        | 改为进行中 `1`                             |
| 项目驳回 | `2`，清实例                    | 保持未开始 `0`                             |
| 在审   | `0`                        | `edit` 里只要 body 带了 status 且和库里不同，直接失败 |

### L1 启流前校验

改 `startTaskApprove` / `startProjectApprove`（在 `WfTaskServiceImpl` 或它们调用的方法里），按这个顺序：

1. 用 id 查任务或项目。查不到 → `R.fail("任务不存在")` / `项目不存在`，不要 insert 关联表。
2. 查 `pmhub_project_task_process`（或现有 `WfTaskProcess`）里该业务 id 是否已有 `approved=0`。有 → `R.fail("审批中，请勿重复提交")`。
3. 已归档的项目、已完成的任务不允许再启流。
4. 上面都过了，才 `runtimeService.startProcessInstance...`，再 insert 关联行，`approved=0`。
5. 用一个不存在的 id 调一次接口，确认关联表没有新行。

### L2 在审禁止改状态

1. 打开 `ProjectTaskServiceImpl.edit`，确认已有「`approved=0` 则拒绝改 status」。没有就补上，文案固定为「审批中不可修改状态」。
2. 在 `ProjectServiceImpl.edit` 里做同样的判断：先按项目 id 查关联表 `approved`，为 `0` 且本次要改 `status` 就抛业务异常。
3. 标题、描述这版允许改。只拦 status。
4. 自测：启流后调项目 edit 改 status，应失败；不改 status 只改名称，应成功。

### L3 通过 / 驳回回写 status

在 `WfTaskServiceImpl.complete` 和 `reject` 末尾，关联表 `approved` 改完之后：

1. 从关联行取出业务 id 和类型（任务还是项目）。
2. complete：按上表把任务或项目的 `status` 写成进行中。
3. reject：任务 status 写成未开始；项目 status 不动（仍是未开始）。`approved=2`，实例 id 清空，这样可以再次启流。
4. 查库核对，不要只看接口 `code=200`。

### L4 可选

`claim` 留到 Sprint P。本 Sprint 不做分类/模型 CRUD。

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

| 已有                             | 本 Sprint 加强点                        |
| ------------------------------ | ----------------------------------- |
| `POST .../startTaskApprove`    | 不存在 / 已在审 → 明确失败码与文案                |
| `POST .../startProjectApprove` | 同上                                  |
| complete / reject              | 成功后查库：`approved` + 业务 `status` 符合约定 |
| （可选）claim                      | `POST /workflow/task/claim`         |

---

## 3. 验收

- 启流非法 id / 重复启流失败且无脏 process 行  
- 项目在审时改状态失败；通过后可改  
- complete 后任务（或项目）status 按约定变化  
- reject 后可再次启流或按约定锁定（行为与文档一致）  
- 两个 Token：办理人可办，无关用户待办为空

---

## 4. 再往后

- [Sprint M](./13-sprint-m-project-deepen.md)  
- 前端 05 联调加深（非本文件范围）

