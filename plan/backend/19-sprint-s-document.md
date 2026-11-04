# Sprint S：文档类型与文档数据

> **状态**：排队  
> **估算**：3～4 人日  
> **目标**：对照库表 `pmhub_document_type`、`pmhub_document_data`，做文档分类与文档记录 CRUD（项目/任务可挂文档）。  
> **前置**：项目、文件模块可用。  
> **不做**：在线协同编辑、OnlyOffice。

## 文件对照

pmhub 公开文件树里没有搜到 Document 类（仓库后半不在索引里）。开工时在本地 pmhub 搜 `DocumentType`、`DocumentData`；对不上就按下面两张表自己建类。

表：`sql/pmhub_20240305.sql` 里的 `pmhub_document_type`、`pmhub_document_data`。

| TaskForge 新建 | 说明 |
|----------------|------|
| `taskforge-project/.../domain/DocumentType.java` | 文档类型 |
| `taskforge-project/.../domain/DocumentData.java` | 文档记录 |
| `.../mapper/DocumentTypeMapper.java`、`DocumentDataMapper.java` | MyBatis-Plus |
| `.../service/impl/DocumentTypeServiceImpl.java`、`DocumentDataServiceImpl.java` | CRUD |
| `taskforge-admin/.../controller/project/DocumentTypeController.java` | `/document/type` |
| `taskforge-admin/.../controller/project/DocumentDataController.java` | `/document/data` |

**不新建模块**，放在 `taskforge-project`。附件继续用已有 `ProjectFile`。

---

## 0. 过程

1. 打开 `pmhub_document_type`、`pmhub_document_data` 的建表语句，实体字段跟列走。有 `parent_id` 就做树，没有就做平铺列表，不要发明层级。
2. 类型 add：名称在同一父节点下不重复。
3. 类型 delete：`document_data` 里还有该 typeId 就拒绝。有子类型也拒绝，先删子节点。
4. 文档 add：必填标题、typeId、projectId。projectId 必须是已存在项目。正文大字段按表的类型存（text 或 varchar）。
5. 文档 list：`POST /document/data/list`，可按 projectId、typeId、标题筛选，分页。
6. 软删：表上有删除标记就更新标记；列表默认不查出已删。没有标记就物理删除。
7. 可选附件：add 的 body 增加 `fileId`。保存前确认 `pmhub_project_file` 这条记录的 project 与文档的 projectId 一致。详情接口把文件名、下载 id 一并返回，下载仍走已有 `/project/file/download`。

---

## 1. API

| 前缀 | 能力 |
|------|------|
| `/document/type` | list / add / edit / delete |
| `/document/data` | list / detail / add / edit / delete |

---

## 2. 验收

- [ ] 类型可维护，删除被引用类型时拒绝  
- [ ] 文档能挂到项目，列表可按项目过滤  
- [ ] 详情能打开关联附件（若做了 S4）  
