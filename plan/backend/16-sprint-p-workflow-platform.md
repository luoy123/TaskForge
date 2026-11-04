# Sprint P：流程平台（分类 / 模型 / 部署 / 表单 / 办理动作）

> **状态**：排队  
> **估算**：8～12 人日  
> **目标**：把 pmhub-workflow 从「内置一条 BPMN + 通过/驳回」补成可配置流程平台。  
> **前置**：Sprint H 主路径；建议 L（状态回写）先做完。  
> **对照**：`WfCategoryController` / `WfModelController` / `WfDeployController` / `WfFormController` / `WfInstanceController` / `WfTaskController`。  
> **不做**：企微卡片、物料报废专用监听（放 R）、可视化设计器前端像素（本 Sprint 只出 API + bpmn XML）。

## 文件对照

**不新建模块**，都进现有 `taskforge-workflow` + `taskforge-admin`。下面 pmhub 有的类，TaskForge 目前都没有（除已有的 Process/Task）。

| 看 pmhub | TaskForge |
|----------|-----------|
| `pmhub-admin/.../workflow/WfCategoryController.java` | **新建** `taskforge-admin/.../controller/workflow/WfCategoryController.java` |
| `pmhub-workflow/.../domain/WfCategory.java`、`bo/WfCategoryBo.java`、`vo/WfCategoryVo.java`、`mapper/WfCategoryMapper.java`、`service/impl/WfCategoryServiceImpl.java` | **新建** 同名一套，包名改 `com.zhq.taskforge.workflow` |
| `WfFormController` + `domain/WfForm.java` + `WfFormServiceImpl` | **新建** 同名 |
| `WfModelController` + `domain/WfModelDeploy.java` + `WfModelServiceImpl` + `mapper/WfModelDeployMapper.java` | **新建** 同名 |
| `WfDeployController` + `domain/WfDeployForm.java` + `WfDeployServiceImpl` + `WfDeployFormServiceImpl` | **新建** 同名 |
| `WfInstanceController` + `WfInstanceServiceImpl` | **新建** 同名 |
| `pmhub-workflow/.../domain/WfCopy.java` + `WfCopyServiceImpl` | **新建** 同名；接口加在 **已有** `WfTaskController` |
| `pmhub-workflow/.../service/impl/WfTaskServiceImpl.java`（claim / 转办 / 委派） | **改** `taskforge-workflow/.../service/impl/WfTaskServiceImpl.java` |
| `pmhub-workflow/.../service/impl/WfProcessServiceImpl.java` | **改** 已有同名类（我的申请、取消） |

Flowable 自带表不用你建。业务表用库里的 `pmhub_wf_category`、`pmhub_wf_form`、`pmhub_wf_deploy_form`、`pmhub_wf_copy`、`pmhub_wf_cancel`、`pmhub_wf_model_deploy`。

---

## 0. 过程

先做 P1–P4，用接口部署出一条能办的流程，再做 P5–P7。办理人继续用 `sys_user.userId` 的字符串，不要开 Flowable 自带用户表。

### P1 分类

1. 看 `pmhub_wf_category` 的列，建 `WfCategory` 实体（分类名、编码、排序）。
2. Service：分页 list、add、edit、delete。删除前如果已有模型引用该分类，拒绝。
3. `WfCategoryController`：`/workflow/category`，返回 `R`。
4. 插入一条「任务审批」，后面的模型用这个 categoryId。

### P2 表单

1. 建 `WfForm`，内容字段存表单 JSON 字符串（前端以后再画，这版 Postman 塞一段 JSON 即可）。
2. CRUD：`/workflow/form`。
3. `WfDeployForm` 先把实体和 Mapper 建好。绑定放在 P3 部署成功之后：部署返回的 deployId + formId insert 一行。

### P3 模型与部署

1. 读 pmhub `WfModelServiceImpl` 的 save、deploy 两个方法，看它怎么把 XML 交给 `repositoryService`。
2. `POST /workflow/model/save`：body 含 name、categoryId、bpmnXml。写入 Flowable 模型，并在 `pmhub_wf_model_deploy` 留你自己的记录（按表字段）。
3. `POST /workflow/model/deploy`：`repositoryService.createDeployment().addBytes(...).deploy()`。成功后写 `pmhub_wf_deploy_form`。
4. `GET /workflow/model/export`：把 bpmnXml 以文件流返回。导入是反过来接收 xml 再走 save。
5. 用现有 `task-approve.bpmn20.xml` 当第一份 xml，部署后 `GET /workflow/process/definition/{key}` 能看到新版本。

### P4 部署管理

1. `GET /workflow/deploy/list`：查 Flowable 部署 + 你的 deploy_form。
2. 挂起：`repositoryService.suspendProcessDefinitionById`。挂起后再 `start`，应失败。
3. 激活：`activateProcessDefinitionById`，start 恢复成功。
4. 删除部署：有运行中实例则拒绝。

### P5 实例

1. `GET /workflow/instance/my`：`historyService` 查 `startedBy = 当前用户`。
2. 详情：实例 id、当前节点名、开始结束时间。
3. `POST /workflow/instance/cancel`：发起人才能取消。`runtimeService.deleteProcessInstance`，并 insert `pmhub_wf_cancel`。业务侧 `approved` 按驳回处理（调用 L 里同一段回写）。

### P6 办理动作

都加在已有 `WfTaskController` / `WfTaskServiceImpl`：

1. `claim`：`taskService.claim(taskId, userId)`。已被别人签了再 claim，返回失败。
2. `transfer`：`taskService.setAssignee(taskId, 目标用户)`。原用户待办列表不再出现，目标用户出现。
3. `delegate`：`taskService.delegateTask`。这版只要求接口能调通，委派回归规则照抄 pmhub 注释，不要自己发明。
4. `copy`：body 带被抄送 userId 列表。insert `pmhub_wf_copy`。`GET /workflow/task/copyList` 只查抄送给当前用户的。

### P7 结束回调

1. 在 bpmn 的结束事件上挂一个 Flowable `ExecutionListener`，或在 `complete` 发现流程已结束时调用。
2. 结束时走 L3 的同一套 status 回写，不要再写一份 if/else。
3. 从「保存模型 → 部署 → start → complete」走一遍，业务 status 必须变成进行中。

---

## 1. API 前缀（建议）

| 前缀 | 能力 |
|------|------|
| `/workflow/category` | 分类 CRUD |
| `/workflow/form` | 表单 CRUD |
| `/workflow/model` | 列表 / 保存 / 部署 / 导出 |
| `/workflow/deploy` | 列表 / 激活 / 挂起 |
| `/workflow/instance` | 我发起的 / 详情 / 取消 |
| `/workflow/task` | 在 complete/reject 上加 claim、transfer、delegate、copy |

返回继续 `R<T>`。办理人用 `sys_user` id，不开 Flowable IDM。

---

## 2. 验收

- [ ] 能新建分类、保存一份 bpmn、部署后待办可办  
- [ ] 挂起后不能再启该定义；激活后恢复  
- [ ] 转办后原办理人待办消失、目标用户出现  
- [ ] 抄送写入 `pmhub_wf_copy`，被抄送人可查列表  
- [ ] 发起人能看到「我的申请」，取消后实例结束  

编译：`mvn -pl taskforge-admin -am -DskipTests compile`

---

## 3. 排期切法

可拆两刀验收：先 P1–P4（能配流程），再 P5–P7（实例与办理动作）。
