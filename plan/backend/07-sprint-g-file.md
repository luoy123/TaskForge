# Sprint G：文件上传 + 任务导入导出（自学版）

> **目标**：落地本地文件上传（工厂分流）、项目/任务附件 CRUD、任务 Excel 导入导出；对照 pmhub，继续用 `R<T>`。  
> **前置**：Sprint E–F 完成（项目/任务主路径、`ProjectLog` 多态可用）。  
> **库表**：复用 `pmhub_project_file`；磁盘目录用本地 path（不接 OSS / MinIO）。  
> **不做**：Flowable、企微提醒、批量 zip 下载可后置、物料 materials、云存储。

---

## 0. 进度与本 Sprint 范围


| 阶段           | 状态                               |
| ------------ | -------------------------------- |
| Sprint A–F   | 完成                               |
| **Sprint G** | **G1–G4 已实现；2026-09-07 联调通过**（template / zip 按计划不做） |
| 再往后          | **前端 Sprint 01**（见 [`../frontend/01-sprint-h-ui.md`](../frontend/01-sprint-h-ui.md)）；工作流 / 逾期 Job 可后置 |


```text
G1  上传基础设施 + ProjectFile 实体/Mapper + 配置
G2  UploadFileFactory（project / task / cover；template 可选）
G3  文件列表 / 重命名 / 软删 / 单文件下载
G4  任务 Excel 导出 + 导入
```

对照习惯：左边 pmhub，右边 TaskForge。  
`easyexcel` 已在父 POM / `taskforge-common` 声明，可直接用。

---

## 0.1 模块与落位

仍落在 `taskforge-project` + admin Controller，**不新建模块**：

```text
taskforge-common/
  config/TaskForgeConfig.java          # profile / projectPath / 允许后缀（对照 PmhubConfig）
  utils/file/FileUploadUtils.java      # 落盘、扩展名、路径名
  utils/poi/ExcelUtil.java             # 或薄封装 EasyExcel（若尚无则本 Sprint 补齐）
  enums/FileTypeEnum.java              # 可选；上传分流更关键的是 UploadTypeEnum

taskforge-project/
  domain/ProjectFile.java
  domain/vo/file/                      # FileVO / ProjectFileReqVO / ProjectFileIdsVO
  domain/vo/task/TaskExcelVO.java      # 导入列
  domain/vo/task/TaskExportVO.java     # 导出列
  mapper/ProjectFileMapper.java + xml
  service/IProjectFileService.java
  service/impl/ProjectFileServiceImpl.java
  service/file/
    UploadAbstractExecutor.java
    UploadFileFactory.java
    UploadTypeEnum.java
    UploadProjectFileExecutor.java
    UploadTaskFileExecutor.java
    UploadCoverFileExecutor.java
    UploadTemplateFileExecutor.java    # G2 可后置

taskforge-admin/
  web/controller/project/ProjectFileController.java   # /project/file/*
  # 在 ProjectTaskController 上挂 import / export
```

依赖方向不变：`project → common`。

---

## 1. 领域心智（先读再写）

```text
MultipartFile
    └── UploadFileFactory(type)
            ├── uploadProjectFileExecutor   type=project，pt_id=项目id
            ├── uploadTaskFileExecutor      type=task，pt_id=任务id，project_id=归属项目
            ├── uploadCoverFileExecutor     写回 Project.cover
            └── uploadTemplateFileExecutor  type=template（可后置）

ProjectFile（pmhub_project_file）
  type + pt_id 多态；file_url / path_name 落盘路径；deleted 软删
```

**上传 type 字符串**（对照 `UploadTypeEnum`，以 pmhub 源码为准）：


| type     | Bean                         | 行为摘要                                   |
| -------- | ---------------------------- | -------------------------------------- |
| project  | `uploadProjectFileExecutor`  | insert 文件行 + 交付物日志 `uploadProjectFile` |
| task     | `uploadTaskFileExecutor`     | insert + 日志；需带上 `projectId`            |
| cover    | `uploadCoverFileExecutor`    | 更新项目封面字段                               |
| template | `uploadTemplateFileExecutor` | 任务模板文件（G2 可选）                          |


**表字段要点**（`pmhub_project_file`）：`id` UUID、`type`、`pt_id`、`project_id`、`file_name`、`file_size`(KB)、`extension`、`file_url`、`path_name`、`user_id`、`deleted`。

### 工厂心智（G2 必保留）

```text
UploadTypeEnum: type → beanName
Spring 注入 Map<String, UploadAbstractExecutor>
Factory.execute(type, loginUser, file, id) → executor.upload(...)
```

不要把四种上传全塞进一个 if-else Service；工厂是本 Sprint 要练的设计点。

---

## 2. G1 — 配置 + 实体 + 工具骨架

### 参考

- `pmhub-common/.../config/PmhubConfig.java`（`profile` / 路径拼接）
- `pmhub-common/.../utils/file/FileUploadUtils.java`、`MimeTypeUtils`
- `pmhub-project/.../domain/ProjectFile.java`
- 库表 `pmhub_project_file`

### 你要做

1. `application-dev.yml` 增加上传根目录，例如：

```yaml
taskforge:
  profile: /home/zxb/data/taskforge/uploadPath   # 按本机改路径，目录需可写
```

1. `TaskForgeConfig` 读取 `profile`，提供 `getProfile()` / `getProjectPath()` 等（可简化成「根目录 + 子目录 project/task/cover」）。
2. `FileUploadUtils`：校验后缀、生成唯一文件名、写到磁盘、返回可访问或相对 `file_url` / `path_name`。
3. `ProjectFile` 实体 + `BaseMapper`；注意软删与 `@TableLogic`（删文件记录时学 E3，必要时 `softDelete` XML）。
4. `ProjectFileController` / `IProjectFileService` 空壳可编译。

### 验收 G1

```bash
mvn -pl taskforge-admin -am -DskipTests compile
```

配置的目录存在且进程用户可写。

---

## 3. G2 — UploadFileFactory（核心）

### 参考

- `pmhub-project/.../service/file/UploadFileFactory.java`
- `UploadAbstractExecutor` + 各 `Upload*Executor`
- `UploadTypeEnum`
- `ProjectFileController.upload` → `POST /project/file/upload`

### 你要做


| 接口                          | 参数                                | 行为                               |
| --------------------------- | --------------------------------- | -------------------------------- |
| `POST /project/file/upload` | `file` + `id` + `type`（form-data） | `uploadFileFactory.execute(...)` |


**本切片必做 Executor**：`project`、`task`、`cover`。  
`template` 可放到 G2 末尾或 G3。

每个 Executor 共性：

1. `FileUploadUtils` 落盘
2. insert `ProjectFile`（size 用 KB，`type`/`ptId`/`projectId`/`extension`/`pathName`）
3. 写 `ProjectLog`（`logType=交付物`，operateType 对齐 pmhub：`uploadProjectFile` / `uploadTaskFile` 等）
4. cover：额外 `update` 项目 `cover` 字段
5. 返回 `FileVO`（fileId / fileName / fileUrl）

权限：菜单有 `project:file:upload`；开发期可先 `isAuthenticated()`，收尾对齐。

### 验收 G2

1. 上传项目文件 → 磁盘有文件 + `pmhub_project_file` 1 行 + 日志。
2. 上传任务文件 → `type=task`，`pt_id=任务id`。
3. 上传封面 → 项目 `cover` 更新。
4. 非法 `type` / 空文件 → 明确错误，不 NPE。

---

## 4. G3 — 列表 / 重命名 / 删除 / 下载

### 参考

- `ProjectFileServiceImpl.queryFileList` / `rename` / `deleteFileList`
- `ProjectFileController`：`/list`、`/rename`、`/delete`、下载接口
- Mapper XML：`queryProjectFileList`（按 projectId + type 等）

### 你要做


| 接口（建议）                                 | 权限字符（对齐菜单）                   | 行为                   |
| -------------------------------------- | ---------------------------- | -------------------- |
| `POST /project/file/list`              | `project:file:queryFileList` | 分页或列表；按项目/任务筛选       |
| `POST /project/file/rename`            | `project:file:rename`        | 改 `file_name`（可只改库名） |
| `POST` 或 `DELETE /project/file/delete` | `project:file:delete`        | 软删记录；磁盘文件可先保留或同步删    |
| `GET /project/file/download`           | 登录即可或专用 perms                | 按 fileId 流式下载        |


批量 zip 下载（pmhub 有）**本 Sprint 可不做**，记 TODO。

### 验收 G3

- 列表能看到 G2 上传的文件。  
- 重命名后列表名变。  
- 删除后列表不可见，`deleted=1`。  
- 下载能打开原文件。

---

## 5. G4 — 任务导入 / 导出

### 参考

- `ProjectTaskController`：`/task/export`、`/task/exportAll`、`/task/import`
- `ProjectTaskServiceImpl.export` / `exportAll` / `importTask`
- `TaskExcelVO` / `TaskExportVO` + `ExcelUtil`（pmhub）或 EasyExcel

### 你要做


| 接口                             | 行为                                     |
| ------------------------------ | -------------------------------------- |
| `POST /project/task/export`    | 按 taskIds 导出 Excel                     |
| `POST /project/task/exportAll` | 导出当前用户相关任务（对齐 pmhub SQL 条件，可简化为「我参与的」） |
| `POST /project/task/import`    | multipart Excel → 解析 → 批量建任务           |


**导入规则（对照 pmhub，写清再实现）**：

1. 行不能为空列表。
2. 按 Excel 里的 **用户名** 找 `sys_user`；找不到则跳过该行。
3. 按 **项目编码 projectCode** 找项目；找不到则跳过。
4. 执行人须已是该 **项目成员**；否则跳过。
5. 阶段取该项目 stageCode 最小的一条。
6. insert 任务 + 成员/日志（可复用 F2 的 insertMember / saveLog，operateType=`importTask`）。

导出列：任务名、项目、执行人、优先级、时间、状态等——以 `TaskExportVO` 为准精简一版即可。

若 TaskForge 尚无 `ExcelUtil`：本切片用 **EasyExcel 直接读写** 即可，不必完整抄若依注解版。

### 验收 G4

1. 导出能用 Excel 打开，行数据正确。
2. 合法模板导入 → 新任务入库 + member/log。
3. 错误行（无用户/无项目/非成员）被跳过且不拖垮整批（可返回成功数/跳过数，可选）。
4. 空文件 / 空表 → 业务异常提示。

---

## 6. 推荐动手顺序


| 天    | 任务  | 完成标志              |
| ---- | --- | ----------------- |
| Day1 | G1  | 配置 + 实体工具编译过；目录可写 |
| Day2 | G2  | 三种 type 上传成功落库落盘  |
| Day3 | G3  | 列表/改名/软删/下载可演示    |
| Day4 | G4  | 导入导出闭环            |


```bash
mvn -pl taskforge-admin -am -DskipTests compile
```

联调建议用 Knife4j：先 `project/file/upload`（form-data），再 list/download，最后 task import/export。

---

## 7. 常见坑


| 坑                           | 处理                                                         |
| --------------------------- | ---------------------------------------------------------- |
| 上传 403 / 413                | Security 放行需登录；`spring.servlet.multipart.max-file-size` 调大 |
| Windows/Linux 路径            | 只用配置里的 `profile`，别写死盘符                                     |
| `type` 与 Executor bean 名对不上 | 对照 `UploadTypeEnum`，Factory Map key 用 Spring bean 名        |
| 任务文件忘写 `project_id`         | task Executor 里从任务表查出再 set                                 |
| 软删文件记录仍显示                   | 列表 SQL 带 `deleted=0`；删用 softDelete                         |
| 导入静默跳过太多                    | 日志或返回「成功 x / 跳过 y」便于验收                                     |
| 一次抄 zip 打包 + template + OSS | 先 G2 三种上传 + G4 导入导出                                        |


---

## 8. 本 Sprint 明确不做

- MinIO / 云 OSS  
- 批量 zip 打包下载（可记 TODO）  
- Flowable / 审批附件联动  
- 物料 `materials` 类型  
- 首页统计、燃尽图深化

G1–G4 过关后，下一站可开 **Sprint H：Flowable 审批联动**（`startTaskApprove` / `pmhub_project_task_process`），或先插系统向能力（验证码 / 在线用户 ）。

---

## 9. 卡住时怎么问

贴：**接口 + type/id + 磁盘路径是否生成 + `pmhub_project_file` 行 + 期望 vs 实际**。  
导入问题再加：**Excel 一行样例 + 是否项目成员**。

---

## 10. 关键源码锚点


| 主题            | 路径                                                                    |
| ------------- | --------------------------------------------------------------------- |
| 上传工厂          | `pmhub-project/.../service/file/UploadFileFactory.java`               |
| 各 Executor    | 同目录 `Upload*Executor.java`、`UploadTypeEnum.java`                      |
| 文件 Controller | `pmhub-admin/.../ProjectFileController.java`                          |
| 文件实体/Service  | `.../domain/ProjectFile.java`、`ProjectFileServiceImpl`                |
| 导入导出          | `ProjectTaskController` + `ProjectTaskServiceImpl.importTask/export`* |
| 路径工具          | `pmhub-common/.../utils/file/FileUploadUtils.java`、`PmhubConfig`      |
| 表结构           | 库表 `pmhub_project_file`                                               |
| 上一切片          | [06-sprint-f-task.md](./06-sprint-f-task.md)                          |
| 总对照           | [00-pmhub-reference-map.md](./00-pmhub-reference-map.md)              |


