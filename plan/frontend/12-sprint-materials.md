# 前端 Sprint 12：物料（对齐后端 R）

> **编号**：`plan/frontend/12`  
> **状态**：排队（等后端 [R](../backend/18-sprint-r-materials.md)；报废审批依赖 [P](../backend/16-sprint-p-workflow-platform.md) 与前端 [10](./10-sprint-workflow-platform.md)）  
> **目标**：物料主数据可维护、可导入导出；配好流程后能发起报废，在已有待办里办结，并看到库存变化。  
> **不做**：对接 WMS、企微出入库通知。

---

## 0. 范围

```text
F12-1  物料列表 / 新增 / 编辑 / 删除 / 导入 / 导出     （可在 P 之前做）
F12-2  审批配置：物料 + processDefinitionKey
F12-3  发起报废：数量、原因；审批中不能改库存
F12-4  待办办结后回到物料页看库存（通过减少，驳回不变）
```

建议：**1 先做**；2～4 等流程部署可用。

**协作**：API 与表格壳由助手写；导入导出、发起审批搜 `【学员填写】`。

---

## 1. 后端接口速查

| 动作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 列表 | POST | `/materials/list` | 名称、编码模糊；分页 |
| 增改删 | POST 等 | `/materials/add` · `edit` · `delete` | 编码唯一；删除方式以后端为准 |
| 导出 / 导入 | — | `/materials/export` · `/import` | 编码重复则更新名称和规格 |
| 审批配置 | POST | `/materials/approvalSet` | 同一物料再次保存覆盖；key 不存在则拒绝 |
| 发起报废 | POST | `/materials/scrap/start` | 物料 id、数量、原因；无配置「未配置审批流程」；有未结束单则拒绝重复 |

待办仍用 `/workflow/process/todoList` 与 complete/reject，不新做办理页。库存字段名以表为准，前端列名跟列表 VO。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/materials/materials.js
  views/materials/
    list/index.vue           # 主数据 + 发起报废 + 配流程对话框
  utils/permission.js        # 菜单 component 别名（若不是 materials/list/index）
```

`v-hasPermi` 与后端权限字对齐（落地后从 `sys_menu` 抄）。

---

## 3. 分阶段要点

### F12-1

- 搜索、表格、对话框，套路同项目列表。
- 导入 xlsx、导出 blob，错误 JSON 要能提示。

### F12-2 / F12-3

- 「配置流程」对话框：流程 key 输入框（或已部署定义下拉，有接口再用下拉）。
- 「报废」：数量、原因。审批中该行库存输入禁用；若仍提交，展示后端拒绝文案。
- 无配置时按钮可点，但必须把「未配置审批流程」显示出来。

### F12-4

- 用自己的用户当审批人走通待办（与 05 相同：approver 要是自己的 userId 才能在待办看到，若报废接口的办理人规则不同，以 R 文档为准）。
- 通过后刷新列表，数量减少且不为负；驳回数量不变。

---

## 4. 验收标准

- [ ] 物料增删改查、导入一条成功
- [ ] 未配置时发起失败并有提示
- [ ] 配置后发起，待办可见
- [ ] 通过后库存按约定减少；驳回不改库存
- [ ] 审批中改库存失败

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

出入库提醒若要站内信，接 [17](./17-sprint-message.md)，本 Sprint 不发消息。
