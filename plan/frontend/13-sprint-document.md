# 前端 Sprint 13：文档（对齐后端 S）

> **编号**：`plan/frontend/13`  
> **状态**：排队（等后端 [S](../backend/19-sprint-s-document.md)）  
> **目标**：维护文档类型，并把文档记录挂到项目上；有附件字段时能下载已有项目文件。  
> **前置**：项目列表、文件下载可用。  
> **不做**：在线协同编辑、OnlyOffice、富文本协同。

---

## 0. 范围

```text
F13-1  文档类型：列表（表有 parent_id 则树，没有则平铺）、增改删
F13-2  文档：按项目 / 类型 / 标题筛选，增改删，详情
F13-3  可选：选择本项目已有附件，详情里给下载
```

建议：**1 → 2 → 3**。

**协作**：API 与页壳由助手写；树或表格的 getList / 提交 / 删除搜 `【学员填写】`。类型是树还是表，开工前看接口返回有没有 `children` 或 `parentId`，不要先写死树。

---

## 1. 后端接口速查

| 前缀 | 能力 |
|------|------|
| `/document/type` | list / add / edit / delete |
| `/document/data` | list / detail / add / edit / delete |

约定（后端计划）：

- 同一父节点下类型名不重复。
- 类型下仍有文档，或仍有子类型，删除失败。
- 文档必填标题、typeId、projectId；projectId 必须存在。
- list：`POST /document/data/list`，分页，可按 projectId、typeId、标题筛。
- 软删则列表默认不显示已删。
- 可选 `fileId`：必须属于同一项目；下载仍走 `/project/file/download`。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/document/type.js
  api/document/data.js
  views/document/
    type/index.vue
    data/index.vue          # 或从项目行「文档」抽屉进入并带上 projectId
```

菜单 component 与 views 对齐；不对就 `COMPONENT_ALIAS`。

---

## 3. 分阶段要点

### F13-1

- 平铺：普通表格。树：`el-table` 树或 `el-tree`，与部门页同一套路（`buildTree` 仅在接口返回扁平列表时用）。
- 删除失败把后端文案原样提示。

### F13-2

- 筛选：项目（下拉用已有 `/project/select` 或列表）、类型、标题。
- 新增：标题、类型、项目、正文（textarea，长度跟表）。
- 不在这里做附件上传新通道。

### F13-3

- 详情抽屉列出文件名 + 「下载」。
- 选文件时只列当前 `projectId` 的附件（复用 file list）。

---

## 4. 验收标准

- [ ] 类型可增改；被引用或有子节点时删不掉
- [ ] 文档能挂到项目，按项目筛选正确
- [ ] 详情能下载关联附件（做了 F13-3 时）
- [ ] 侧栏进入真实页，不进 `building`

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

公共网盘是另一套目录，见 [14](./14-sprint-public-file.md)，不要和项目附件、文档附件混在一个页面里。
