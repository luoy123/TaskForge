# Sprint O：代码生成 generator（自学版）

> **状态**：排队（学习向；非业务 MVP）  
> **估算**：4～6 人日  
> **目标**：对照 pmhub-generator / RuoYi，从数据表生成基础 Java（domain/mapper/service/controller）与可选 Vue 片段。  
> **前置**：system 模块、代码风格稳定。  
> **不做**：一次性完美支持所有表类型、Flowable 表、覆盖手写业务代码。

## 文件对照

pmhub 整模块在 `pmhub-generator/`。TaskForge **新建模块** `taskforge-generator/`，并在父 `pom.xml` 的 `<modules>`、`taskforge-admin/pom.xml` 里加上依赖。

| 看 pmhub | TaskForge 新建 |
|----------|----------------|
| `pmhub-generator/.../controller/GenController.java` | `taskforge-generator/.../controller/GenController.java`（或放到 admin，二选一；建议跟 pmhub 放 generator 模块） |
| `.../domain/GenTable.java`、`GenTableColumn.java` | 同名 domain |
| `.../mapper/GenTableMapper.java`、`GenTableColumnMapper.java` | 同名 Mapper |
| `.../resources/mapper/generator/GenTableMapper.xml`、`GenTableColumnMapper.xml` | 同路径 XML |
| `.../service/impl/GenTableServiceImpl.java`、`GenTableColumnServiceImpl.java` | 同名 Service |
| `pmhub-generator/src/main/resources/templates/*.java.vm`（controller、entity、mapper、service 等） | **新建** `taskforge-generator/src/main/resources/templates/`，模板改成 `R<T>` 和 `com.zhq.taskforge` |

表用库里已有的 `gen_table`、`gen_table_column`，不新建表。

---

## 0. 过程

下载方式定死为 **zip 下载**，不写回源码目录，避免覆盖手写业务。

### O1 模块

1. 新建 `taskforge-generator`，依赖 `taskforge-common`。admin 依赖 generator。
2. 引入与 pmhub 相同的模板引擎（多半是 Velocity）。父 POM 管版本。
3. 从 pmhub `resources/templates` 复制 vm，全局替换：`AjaxResult` → `R`，包名改为 `${packageName}`，作者等信息走生成参数。

### O2 导入表

1. 实体映射 `gen_table`、`gen_table_column`。
2. `GET /tool/gen/db/list`：查 `information_schema.TABLES`，排除已经在 `gen_table` 里的表。
3. `POST /tool/gen/importTable`：body 为表名数组。读 `information_schema.COLUMNS` 写入两张 gen 表。Java 字段用下划线转驼峰。
4. `GET /tool/gen/list`、`GET /tool/gen/{tableId}`：预览配置。`PUT` 能改类名、包名、列的 Java 类型。

### O3 预览和下载

1. `GET /tool/gen/preview/{tableId}`：返回 map，key 是文件名，value 是渲染后的源码字符串。
2. `GET /tool/gen/download/{tableId}`：同样渲染，打成 zip。zip 内路径 `main/java/...`、`main/resources/mapper/...`。
3. 用 `sys_notice` 走一遍导入 → 预览 → 下载。zip 里应有 Entity、Mapper、Mapper.xml、Service、ServiceImpl、Controller。
4. Controller 模板里的列表方法返回 `R<Page<...>>`，不要出现 `TableDataInfo`。

### O4 不做的事

不要提供「生成到本地项目路径」的按钮。不要给 `ACT_*`、`gen_table` 自己生成代码。

---

## 1. 注意

- 包名 `com.zhq.taskforge...`，响应 `R` 不是 `AjaxResult`。  
- 权限字、Swagger 注解按现有 admin 习惯。  
- 生成后不自动覆盖 `taskforge-project` 已有复杂业务。

---

## 2. 验收

- [ ] 选一张简单表（如 `sys_notice`）能预览/下载代码  
- [ ] 生成代码可编译（或文档标明需手改的点）  

---

## 3. 明确更后置

- **OA / 企微**：无企业微信时可永久跳过或只做消息模板 Mock。  
- 动态数据源、WebSocket：一般不做。  
