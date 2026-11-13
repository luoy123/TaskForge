# Sprint O：代码生成 generator（自学版）

> **状态**：✅ 已完成（2026-11-13 验收；学习向，非业务 MVP）  
> **估算**：4～6 人日  
> **目标**：对照 pmhub-generator / RuoYi，从数据表生成基础 Java（domain/mapper/service/controller）与可选 Vue 片段。  
> **前置**：system 模块、代码风格稳定。  
> **不做**：一次性完美支持所有表类型、Flowable 表、覆盖手写业务代码。

## 文件对照

pmhub 整模块在 `pmhub-generator/`。TaskForge **新建模块** `taskforge-generator/`，并在父 `pom.xml` 的 `<modules>`、`taskforge-admin/pom.xml` 里加上依赖。

| 看 pmhub | TaskForge 新建 |
|----------|----------------|
| `pmhub-generator/.../controller/GenController.java` | `taskforge-admin/.../web/controller/tool/GenController.java`（跟 TaskForge 现有习惯：quartz 等模块的 Controller 都在 admin） |
| `.../config/GenConfig.java` + `resources/generator.yml` | `taskforge-generator/.../config/GenConfig.java`（非 static 的 `@ConfigurationProperties`），配置写进 admin 的 `application.yml` |
| `.../util/VelocityInitializer.java` | `taskforge-generator/.../config/VelocityConfig.java`（注册一个 `VelocityEngine` Bean） |
| `.../util/VelocityUtils.java`、`GenUtils.java` | 同名工具类（O2、O3 再写） |
| `.../domain/GenTable.java`、`GenTableColumn.java` | 同名 domain |
| `.../mapper/GenTableMapper.java`、`GenTableColumnMapper.java` | 同名 Mapper |
| `.../resources/mapper/generator/GenTableMapper.xml`、`GenTableColumnMapper.xml` | 同路径 XML |
| `.../service/impl/GenTableServiceImpl.java`、`GenTableColumnServiceImpl.java` | 同名 Service |
| `pmhub-generator/src/main/resources/vm/java/*.java.vm`、`vm/xml/mapper.xml.vm` | **新建** `taskforge-generator/src/main/resources/vm/`，O3 再改成 `R<T>`、MP 风格和 `com.zhq.taskforge` |

> pmhub 有两套模板：`vm/` 给在线生成（`GenController` → `VelocityUtils`）用，是本 Sprint 要抄的；`templates/` 是 `GeneratorTest` 里 MyBatis-Plus `AutoGenerator` 直接写磁盘用的，违反「只 zip 下载」，**不抄**。

表用库里已有的 `gen_table`、`gen_table_column`，不新建表。

---

## 0. 过程

下载方式定死为 **zip 下载**，不写回源码目录，避免覆盖手写业务。

### O1 模块

1. 新建 `taskforge-generator`，依赖 `taskforge-common`。父 POM 加 module 和 dependencyManagement，admin 依赖 generator，`@MapperScan` 加 `com.zhq.taskforge.generator.mapper`。
2. Velocity：父 POM 管 `velocity-engine-core` 2.3 版本。不引 `commons-collections`、`mybatis-plus-generator`、mysql 驱动。
3. `VelocityConfig`：注册单例 `VelocityEngine` Bean，从 classpath 读模板、UTF-8。
4. `GenConfig`：`gen.author / packageName / autoRemovePre / tablePrefix`，写在 admin 的 `application.yml`。
5. 把 pmhub `vm/java/{domain,mapper,service,serviceImpl,controller}.java.vm` 和 `vm/xml/mapper.xml.vm` 原样复制过来，**先不改内容**，O3 能预览后再边看效果边改。
6. 验收：`mvn clean install -DskipTests` 通过，admin 能启动；写个小测试能用 `VelocityEngine` 渲染出一段字符串。

### O2 导入表

1. 实体映射 `gen_table`、`gen_table_column`。
2. `GET /tool/gen/db/list`：查 `information_schema.TABLES`，排除已经在 `gen_table` 里的表。
3. `POST /tool/gen/importTable`：body 为表名数组。读 `information_schema.COLUMNS` 写入两张 gen 表。Java 字段用下划线转驼峰。
4. `GET /tool/gen/list`、`GET /tool/gen/{tableId}`：预览配置。`PUT` 能改类名、包名、列的 Java 类型。

拆成三小步：

- **O2-1 实体 + Mapper**：`GenTable`、`GenTableColumn`、`GenConstants`；两个 Mapper 继承 `BaseMapper`，只手写 3 条 `information_schema` SQL（`selectDbTableList` 分页、`selectDbTableListByNames`、`selectDbTableColumnsByName`）。pmhub XML 里的 insert / update / delete / 按 id 联表查询都用 MP 代替。
- **O2-2 `GenUtils` + 导入**：`initTable`、`initColumnField`、`convertClassName` 接收 `GenConfig` 参数；`GenTableServiceImpl.importGenTable` 加事务。
- **O2-3 Controller**：`/tool/gen` 下 `db/list`、`importTable`、`list`、`{tableId}`、`PUT`、`DELETE`，权限字 `tool:gen:*`。

已核对过的库情况（2026-11-13）：

- 两张 gen 表**没有 `deleted` 列**，而全局配置了 `logic-delete-field: deleted`。实体**不能继承 `BaseEntity`**，否则 MP 会给查询自动拼 `deleted = 0`，报 Unknown column。
- `gen_table_column.table_id` 是 `varchar(64)`（若依原版如此），Java 端仍用 `Long`，MySQL 隐式转换没问题。
- `NOT IN (select table_name from gen_table)` 在本库实测没有排序规则冲突。排除条件里额外加 `ACT\_%`、`FLW\_%`（对应 O4）。

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

- [x] 选一张简单表（如 `sys_notice`）能预览/下载代码（2026-11-13 浏览器联调：导入 → 编辑 → 预览 → 下载 zip → 删除）  
- [x] 生成代码可编译：`GenCodeTest` 把 `sys_notice` 生成到 `target/gen-out`，用 admin 的 classpath 跑 javac 通过，无过时警告  

**已知限制**：只做 crud 单表；Controller 固定生成到 `{basePackage}.web.controller.{moduleName}`，不生成前端页面和菜单 SQL，权限字符串要自己在菜单里配。

---

## 3. 明确更后置

- **OA / 企微**：无企业微信时可永久跳过或只做消息模板 Mock。  
- 动态数据源、WebSocket：一般不做。  
