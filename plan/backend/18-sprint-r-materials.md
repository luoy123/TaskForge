# Sprint R：物料与物料审批

> **状态**：排队  
> **估算**：5～7 人日  
> **目标**：对照 pmhub 物料域，打通物料主数据、审批配置、报废流程与出入库类提醒记录。  
> **前置**：P 的流程部署可用更顺；没有 P 时也可先做主数据 CRUD，审批后挂。  
> **库表**：`pmhub_materials`、`pmhub_materials_approval_set`。  
> **不做**：对接真实 WMS；企微出入库卡片实发（U 再接）。

## 文件对照

GitHub 文件树后半被截断，物料主数据类在你本机 pmhub 里按名搜 `Materials`。已确认的审批类如下。

| 看 pmhub | TaskForge |
|----------|-----------|
| 本地 `pmhub-project` 里搜物料实体 / `MaterialsController`（表 `pmhub_materials`） | **新建模块** `taskforge-materials/`：`domain/Materials.java`、Mapper、`MaterialsServiceImpl`、`taskforge-admin/.../controller/materials/MaterialsController.java` |
| `pmhub-workflow/.../domain/MaterialsApprovalSet.java` | **新建** `taskforge-workflow/.../domain/MaterialsApprovalSet.java` |
| `.../domain/dto/MaterialsApprovalSetDTO.java`、`vo/MaterialsApprovalSetVO.java` | **新建** 同名 DTO/VO |
| `.../mapper/MaterialsApprovalSetMapper.java` | **新建** 同名 Mapper |
| `.../domain/WfMaterialsScrappedProcess.java`、`mapper/WfMaterialsScrappedProcessMapper.java` | **新建** 同名（报废流程关联） |
| `pmhub-workflow/.../listener/strategy/SupplierApprovalListenerExecutor.java` | **新建** `taskforge-workflow/.../listener/` 下一个结束监听，通过后回写物料；不必抄供应商策略的全部类 |

父 `pom.xml`、`taskforge-admin/pom.xml` 增加 `taskforge-materials`。表不新建，用 `pmhub_materials`、`pmhub_materials_approval_set`。

---

## 0. 过程

先只做 R1，能增删改查再做审批。开工第一件事：打开 `sql/pmhub_20240305.sql` 里 `pmhub_materials`，把库存字段名记下来。本计划约定：**通过报废审批时把库存数量减 body 里的数量；驳回不动库存。** 若表上没有数量列，就改成只改状态字段，并在代码注释写明。

### R1 主数据

1. 父 POM 增加 module `taskforge-materials`，admin 依赖它。包名 `com.zhq.taskforge.materials`。
2. 实体字段对齐表。编码唯一。
3. `POST /materials/list` 分页，可按名称、编码模糊查。
4. add / edit / delete。删除不做物理清空库存流水，按表上的删除标记来；表没有删除标记就物理删，并在注释说明。
5. 导出、导入走 EasyExcel，列与表的业务字段一致。导入时编码重复则更新名称和规格。

### R2 审批配置

1. 实体 `MaterialsApprovalSet` 对齐 `pmhub_materials_approval_set`。
2. `POST /materials/approvalSet`：物料 id（或物料类别，以表为准）+ Flowable 的 `processDefinitionKey`。
3. 同一物料只保留一条配置，再次保存覆盖。
4. key 在 Flowable 里不存在就拒绝保存。

### R3 发起

1. `POST /materials/scrap/start`，body：物料 id、数量、原因。
2. 没有 R2 的配置 → `R.fail("未配置审批流程")`。
3. 有未结束的报废关联（`WfMaterialsScrappedProcess`）→ 拒绝重复发起。
4. `runtimeService.start`，把物料 id、数量放进流程变量。
5. insert 报废关联行，状态=审批中。此期间 `edit` 物料的库存字段直接拒绝。

### R4 回写

1. 在流程结束监听里读变量。通过：库存减去数量，关联状态=通过。库存不足则把流程记为异常并回滚库存变更（不要减成负数）。
2. 驳回：关联状态=驳回，库存不变。
3. 待办仍走已有 `/workflow/process/todoList` 与 complete。

### R5 日志

每次发起、通过、驳回 insert 一条你自己的操作说明（可复用 `pmhub_project_log` 以外的简单表，或写在报废关联的备注字段）。不要在这里发企微。

---

## 1. 模块

```text
taskforge-project 或新建 taskforge-materials（物料独立时新建更干净）
taskforge-admin：/materials/* 、/materials/approvalSet/*
```

建议：**新建 `taskforge-materials`**，admin 聚合，避免 project 继续膨胀。流程启动仍调 workflow。

---

## 2. 验收

- [ ] 物料增删改查、导入一条成功  
- [ ] 配置审批流后，发起审批能在待办看到  
- [ ] 通过后状态/库存按约定变化；驳回不改库存  
- [ ] 无配置时发起审批失败并提示  

---

## 3. 排期切法

R1 主数据可先排；R2–R4 依赖 P，放在 P 之后。
