# 前端 Sprint 10：流程平台页（对齐后端 P）

> **编号**：`plan/frontend/10`  
> **状态**：排队（等后端 [P](../backend/16-sprint-p-workflow-platform.md)；建议 [08](./08-sprint-workflow-polish.md) 先过）  
> **目标**：把侧栏「流程管理」里仍进 `building` 的菜单换成可配置流程：分类、表单 JSON、模型部署、部署管理、我的申请，以及待办上的转办/委派/抄送。  
> **前置**：前端 05 待办/已办；后端 P1–P7。  
> **不做**：拖拽表单设计器、BPMN 画布（用 XML 文本）。

---

## 0. 范围

```text
F10-1  api/workflow：category / form / model / deploy / instance，task 增加 claim·transfer·delegate·copy
F10-2  分类 CRUD
F10-3  表单：JSON 文本存取
F10-4  模型：粘贴或上传 XML、部署、导出
F10-5  部署列表：挂起 / 激活 / 删除
F10-6  我的申请 + 取消；待办增加转办、委派、抄送列表
F10-7  别名：菜单 component 对不上 views 时写入 COMPONENT_ALIAS
```

建议先 **2→3→4→5**（能配出一条流程），再 **6**。

**协作**：页面壳由助手写；提交、部署、挂起搜 `【学员填写】`。

---

## 1. 后端接口速查

| 前缀 | 能力 |
|------|------|
| `/workflow/category` | 分类分页 list / add / edit / delete（被模型引用则删失败） |
| `/workflow/form` | 表单 CRUD，内容是 JSON 字符串 |
| `/workflow/model` | save（name、categoryId、bpmnXml）/ deploy / export |
| `/workflow/deploy` | list / 挂起 / 激活 / 删除（有运行实例则拒绝） |
| `/workflow/instance` | `my` 我发起的 / 详情 / cancel（仅发起人） |
| `/workflow/task` | 已有 complete、reject；加 claim、transfer、delegate、copy、copyList |

办理人继续用用户 id 字符串。返回 `R<T>`。路径以 Controller 落地为准，写 API 前对一下 Swagger。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/workflow/
    category.js · form.js · model.js · deploy.js · instance.js
    task.js                 # 在 05 上追加
  views/workflow/
    category/index.vue
    form/index.vue          # textarea 编辑 JSON
    model/index.vue
    deploy/index.vue
    own/index.vue           # 我的申请
    copy/index.vue          # 抄送我的（也可并进待办 Tab）
  views/workflow/todo/index.vue   # 转办 / 委派按钮
  utils/permission.js       # COMPONENT_ALIAS
```

库里常见 component：`workflow/category/index`、`workflow/form/index`、`workflow/model/index`、`workflow/deploy/index`、`workflow/work/own`、`workflow/work/copy`。对不上就别名到上表路径。

---

## 3. 分阶段要点

### F10-2～4 配置

- 分类：名称、编码、排序。
- 表单：名称 + 大文本 JSON，不做可视化。
- 模型：名称、分类、XML（可用现有 `task-approve.bpmn20.xml` 内容粘贴）。部署成功后提示 deployId。导出用 blob 下载。

### F10-5 部署

- 表格：流程名、版本、挂起状态。挂起后从业务侧再发起应失败；激活后恢复。
- 删除二次确认。

### F10-6 办理

- 我的申请：开始时间、当前节点、取消（仅进行中）。取消后实例结束，业务侧按驳回（与 L 同一套，前端只刷新状态）。
- 转办：选目标用户 id（可复用用户下拉）。原办理人待办消失。
- 抄送：多选用户；「抄送我的」列表只看自己。
- 委派：能调通即可，回归规则以后端注释为准，前端不发明第二套。

---

## 4. 验收标准

- [ ] 新建分类、保存 XML、部署后，待办能办结，业务 status 变为进行中
- [ ] 挂起后不能再启；激活后恢复
- [ ] 转办后原人待办消失、目标人出现
- [ ] 抄送列表被抄送人可见
- [ ] 发起人能看到我的申请，取消后实例结束
- [ ] 上述菜单不再进 `building`

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

物料报废走待办，页面在 [12](./12-sprint-materials.md)，不在本 Sprint 做。
