# Sprint Q：系统边角 + 任务协作缺口

> **状态**：排队  
> **估算**：4～6 人日  
> **目标**：补齐 pmhub 系统管理与项目任务里「有接口、TaskForge 还没有」的边角，不新开大模块。  
> **前置**：A–G、I。  
> **不做**：代码生成（O）、服务监控（N）、企微指派提醒（U）。

## 文件对照

| 看 pmhub | TaskForge |
|----------|-----------|
| `pmhub-admin/.../system/SysUserController.java`（import/export、authRole） | **改** `taskforge-admin/.../controller/system/SysUserController.java`；导入导出逻辑 **改** `taskforge-system/.../service/impl/SysUserServiceImpl.java` |
| `pmhub-admin/.../system/SysRegisterController.java`、`pmhub-framework/.../web/service/SysRegisterService.java` | **新建** `taskforge-admin/.../controller/system/SysRegisterController.java`、`taskforge-framework/.../web/service/SysRegisterService.java` |
| `pmhub-admin/.../system/SysProfileController.java`（头像） | **改** 已有 `SysProfileController.java` |
| `pmhub-admin/.../common/CommonController.java` | 下载若和文件模块重复就只 **改** `ProjectFileController`；缺通用下载再 **新建** `taskforge-admin/.../controller/common/CommonController.java` |
| `pmhub-project/.../service/file/UploadTemplateFileExecutor.java` | **新建** `taskforge-project/.../service/file/UploadTemplateFileExecutor.java`，并 **改** `UploadFileFactory.java`、`UploadTypeEnum.java` 注册 template |
| `pmhub-admin/.../project/ProjectLogController.java`、`ProjectLogServiceImpl.java` | **新建** `ProjectLogController.java`；`ProjectLog` / `ProjectLogMapper` **已有** |
| 任务类型：库表 `pmhub_task_type`。类名在本地 pmhub-project 搜 `TaskType` | **新建** `taskforge-project/.../domain/TaskType.java`、Mapper、Service、`taskforge-admin/.../controller/project/TaskTypeController.java` |
| 认领：看 `ProjectTaskServiceImpl` 里待认领状态流转 | **改** `ProjectTaskServiceImpl.java`、`ProjectTaskController.java`，加 `claim` |
| zip：看 `ProjectFileController` / `ProjectFileServiceImpl` 的批量下载 | **改** 已有 `ProjectFileController.java`、`ProjectFileServiceImpl.java` |

---

## 0. 过程

系统边角（Q1–Q4）和项目边角（Q5–Q9）可以分开做。每做完一块用 curl 或现有前端点一次。

### Q1 用户导入导出、角色分配用户

1. 打开 pmhub `SysUserController` 的 `export`、`importData`、`allocatedList`、`unallocatedList`、`authUser/selectAll`。
2. 导出：`GET /system/user/export`，条件与用户列表相同，用已有 EasyExcel 写成 `UserExcelVO` 流。
3. 导入：`POST /system/user/import`，multipart。用户名已存在则更新昵称和手机，不存在则插入，默认密码用 `sys_config` 里已有的初始密码键；没有这个键就先在参数表加一条再读。
4. 已分配：`GET /system/user/authRole/allocatedList?roleId=`，join `sys_user_role`。
5. 未分配：同表 `not exists`。
6. 保存：`PUT /system/user/authRole`，body 为 roleId + userIds。先删该角色下这些用户的旧关系再插入（或按 pmhub 的增量语义，选一种写在接口注释里）。

### Q2 头像

1. `POST /system/user/profile/avatar`，`MultipartFile`。
2. 用 `FileUploadUtils` 落到头像目录（`TaskForgeConfig` 里加一个 avatar 路径，没有就加）。
3. 更新 `sys_user.avatar`，返回新 URL。`getInfo` 里确认带出这个字段。

### Q3 注册

1. 读配置键 `sys.account.registerUser`，没有就插入，默认 `false`。
2. `POST /register` 放行匿名（改 Security 白名单）。
3. 开关不是 `true` 时直接 `R.fail("未开放注册")`。
4. 开放时：校验用户名唯一、密码长度，插入用户并给一个默认角色（角色 id 写死在配置或代码常量，注释说明）。

### Q4 通用下载

1. 看现有 `GET /project/file/download` 是否已按文件 id 下本地盘。
2. 若只差「按相对路径下 profile 目录」，再补 `GET /common/download?path=`，且 path 必须落在 profile 根目录内，拒绝 `..`。

### Q5 任务类型

1. 看 `pmhub_task_type` 列，建实体。
2. CRUD：`/project/taskType`。删除时若有任务引用则拒绝。
3. `ProjectTask` 增加类型字段（表上已有就映射）。`add` 时类型 id 可空。

### Q6 模板上传

1. 复制 `UploadTaskFileExecutor` 为 `UploadTemplateFileExecutor`。
2. `type=template`，`pt_id` 用项目 id 或约定的模板业务 id（跟 pmhub 该方法的入参一致）。
3. 在 `UploadTypeEnum`、`UploadFileFactory` 的 map 里注册。不注册的话工厂会走到默认分支。
4. 调 `POST /project/file/upload`，type=template，库里 `type` 列是 template。

### Q7 zip

1. `POST /project/file/zip`，body 为 fileId 列表，必须属于同一个 projectId。
2. 服务端把这些本地文件打成 zip，写到 response，`Content-Disposition` 带文件名。
3. 某个 id 的磁盘文件不存在：跳过并在响应头或日志里说明，不要让整个接口 500。若一个都没有，返回 `R.fail`。

### Q8 项目日志

1. `POST /project/log/list`，body：`projectId`、分页、可选 `logType`。
2. 查 `pmhub_project_log` 中 `project_id` 匹配的行（含任务日志）。任务那边的 `/project/task/log/list` 保持只查 `type=task` 且 `pt_id=任务 id`。

### Q9 任务认领（不是流程 claim）

1. `POST /project/task/claim`，body：`taskId`。
2. 任务 status 不是待认领 `4` 就拒绝。
3. 给当前用户 insert `pmhub_project_member`，`type=task`，`creator=0`。已是成员则不要插第二条。
4. status 改为未开始 `0`。
5. 写一条日志 `operateType=claimTask`。

---

## 1. 验收

- [ ] 用户 Excel 往返：导出再导入能新增或更新（规则写进接口说明）  
- [ ] 角色分配用户：已选/未选两列表，保存后 `sys_user_role` 正确  
- [ ] 头像更新后 getInfo 能拿到地址  
- [ ] 注册开关关闭时接口拒绝  
- [ ] 任务类型可维护，新建任务能挂上  
- [ ] 模板上传落 `pmhub_project_file` 且 type=template  
- [ ] 项目动态列表与任务动态列表都能分页  

---

## 2. 排期切法

Q1–Q4 系统（约 2～3 天），Q5–Q9 项目（约 2～3 天），可分开排。
