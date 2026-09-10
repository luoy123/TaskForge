# pmhub-boot 对照知识图（给后续 Sprint 计划用）

> 来源：`/home/zxb/projects/pmhub-boot/pmhub-boot`（单体版）+ 其 `article/` 文档。  
> TaskForge 技术栈：**Spring Boot 3.3 / Java 17 / MyBatis-Plus / R\<T\>**，不是 1:1 抄 pmhub（Boot 2.5 / Java 8 / AjaxResult）。  
> 更新时机：每开新 Sprint 前扫一眼「进度对照」；学到新对照点再补本节。

---

## 1. 模块地图

```
pmhub-common          注解/实体/工具/常量（最底层）
pmhub-framework       Security / JWT / AOP / Redis / Druid
pmhub-system          用户/角色/菜单/部门/字典/配置/日志 Service+Mapper
pmhub-project         项目+任务（依赖 oa + workflow）
pmhub-workflow        Flowable 封装
pmhub-oa              企微消息 / SSO（可后置）
pmhub-quartz          Quartz
pmhub-generator       代码生成（可后置）
pmhub-admin           启动入口 + 全部 Controller（端口 6880）
```

TaskForge 当前：`common / framework / system / project / admin`。  
再往后可按需加 `workflow`。

---

## 2. 认证与权限心智模型

```
POST /login
  → AuthenticationManager → UserDetailsServiceImpl
  → TokenService：JWT(UUID) + Redis login_tokens:{uuid}
  → JwtFilter 每请求：JWT→UUID→Redis→SecurityContext
  → @PreAuthorize("@ss.hasPermi('x:y:z')")
       → PermissionService.hasPermi → PermissionContextHolder.setContext
  → @DataScope → Aspect 按角色 data_scope 拼过滤条件
```

| 点 | pmhub | TaskForge 注意 |
|----|-------|----------------|
| 响应体 | AjaxResult / TableDataInfo | 继续用 `R<T>` |
| DataScope 注入 | `BaseEntity.params.dataScope` + XML `${}` | ThreadLocal `DataScopeContext` + `qw.apply` |
| 超管 | `user.isAdmin()` / userId=1 | 对齐 userId=1 bypass |
| 多角色 permission | 每角色 `role.permissions` | 登录必须按 roleId 装填 |

---

## 3. 系统管理（sys_*）— TaskForge 已覆盖大半

| 能力 | pmhub 入口 | TaskForge 状态 |
|------|-----------|----------------|
| 登录 / Token | SysLoginController + TokenService | ✅ |
| getInfo / getRouters | SysLoginController / SysMenu | ✅（Sprint C） |
| 用户 CRUD | SysUserController | ✅（Sprint A） |
| 角色 CRUD + 菜单 | SysRoleController | ✅ |
| 角色 dataScope + role_dept | `PUT /system/role/dataScope` → authDataScope | ✅（Sprint D） |
| 菜单 / 部门 / 岗位 / 字典 / 配置 / 公告 | 对应 Controller | ✅ |
| 登录日志 / 操作日志 | monitor + `@Log` | ✅（Sprint B） |
| DataScope | DataScopeAspect | ✅（Sprint D） |
| 验证码 | CaptchaController + Kaptcha | 后置 |
| 在线用户 | SysUserOnlineController | 后置 |
| 服务/缓存监控 | ServerController / CacheController | 后置 |
| 岗位-用户关联深度能力 | sys_user_post | 按需 |
| OAuth2 / SSO | OAuth2ServiceImpl | 后置（oa） |

---

## 4. 项目管理（pmhub_project_*）— 下一大站

### 4.1 表（前缀 `pmhub_`，仿写可缩成 `pm_` / `tf_`）

| 表 | 作用 |
|----|------|
| pmhub_project | 项目主表（UUID 主键） |
| pmhub_project_stage | 阶段（创建项目自动生成） |
| pmhub_project_task | 任务（task_pid 子任务） |
| pmhub_project_member | 成员（pt_id + type 多态） |
| pmhub_project_file | 文件（多态） |
| pmhub_project_log | 动态/交付物/评论（多态） |
| pmhub_project_collection | 收藏 |
| pmhub_project_task_process | 任务↔Flowable |
| pmhub_project_task_notify | 通知 |
| pmhub_project_task_work_time | 工时 |

### 4.2 状态

```
项目：未开始(0) → 进行中(1) → 已归档(2) ；旁路 逾期(3)/暂停(4)
任务：待认领(4) → 未开始(0) → 进行中(1) → 已完成(2) ；旁路 逾期(3)
审批：approved=0 需审批不可手改状态；≠0 仅创建人可改
```

### 4.3 创建项目副作用（必须写进计划）

`ProjectServiceImpl.saveProject`：
1. 生成 projectCode（`P` + Seq）
2. insert 项目
3. 按 `ProjectStageEnum` 插全部阶段，回写当前 stageId
4. insert 创建者成员（creator=1）
5. 写 create + inviteMember 日志

### 4.4 API 面（Controller 在 admin）

**项目** `/project/*`：add / edit / list / detail / delete / archive / cancelArchive / quit / statistics / doing / select / queryAllProject / taskList / startProjectApprove …

**任务** `/project/task/*`：add / addChildTask / edit / list / detail / delete / queryMyTaskList / queryChildTask / situation / burnDownChart / addComment / log/list / import&export / startTaskApprove …

**附属**：member / stage / file / log / collection Controllers。

### 4.5 设计模式（仿写要保留心智，不必全抄类名）

| 模式 | 用途 |
|------|------|
| QueryProjectFactory + Executor | 我的项目 / 收藏 / 回收站 |
| UploadFileFactory + Executor | 封面 / 项目文件 / 任务文件 / 模板 |
| QueryTaskLogFactory | 全部 / 动态 / 交付物 / 评论 |
| FieldUtils + @ForUpdate | 任务编辑字段变更日志 |

### 4.6 多态约定

`ProjectMember` / `ProjectFile` / `ProjectLog`：`pt_id` + `type`（project | task | …）同一表服务两级。

---

## 5. 工作流（pmhub-workflow）

| Service | 职责 |
|---------|------|
| WfModelServiceImpl | 模型 CRUD / 部署 / 导入导出 |
| WfDeployServiceImpl | 部署激活暂停 |
| WfProcessServiceImpl | 定义查询 / 启动实例 |
| WfTaskServiceImpl | **核心**：complete / claim / 驳回转办委派抄送 |
| WfInstance / Category / Copy / Form / Listener | 实例、分类、抄送、表单、全局监听 |

与项目联动：`pmhub_project_task_process` + `startTaskApprove` / `startProjectApprove`。  
Flowable：自动建表、异步 Job 默认关、IDM 关。

自定义表：`pmhub_wf_category / form / deploy_form / copy / cancel / model_deploy`。

---

## 6. Framework AOP / 横切（对照清单）

| 能力 | pmhub 类 | TaskForge |
|------|----------|-----------|
| 操作日志 | @Log + LogAspect + AsyncFactory | ✅ |
| 数据权限 | @DataScope + DataScopeAspect | ⏳ D（注解+Context 已有，缺 Aspect 接线） |
| 限流 | @RateLimiter + RateLimiterAspect | 后置 |
| 防重提交 | @RepeatSubmit + Interceptor | 后置 |
| 动态数据源 | @DataSource + DataSourceAspect | 通常不做 |
| WebSocket | WebSocketServer | 后置 |

---

## 7. OA / Quartz / Generator（计划优先级低）

- **oa**：企微消息模板、回调、任务逾期推送；无企微可砍或 mock。
- **quartz**：sys_job + 任务逾期 Job（TaskOverdue*Job 在 project 模块）。
- **generator**：Velocity CRUD；非 MVP。

---

## 8. 推荐 TaskForge 路线（出计划时按此切 Sprint）

```
✅ Sprint A  用户 CRUD
✅ Sprint B  登录/操作日志 + monitor
✅ Sprint C  getInfo / Profile / Notice
✅ Sprint D  DataScope
✅ Sprint E  项目模块第一刀 → [05-sprint-e-project.md](./05-sprint-e-project.md)
✅ Sprint F  任务 CRUD + 子任务 + 评论 → [06-sprint-f-task.md](./06-sprint-f-task.md)
✅ Sprint G  文件上传工厂 + 导入导出 → [07-sprint-g-file.md](./07-sprint-g-file.md)
→ 前端 Sprint 01：[01-sprint-h-ui.md](../frontend/01-sprint-h-ui.md)
── 系统收尾（可选，随时可插）──
   验证码 / 在线用户 / 服务监控（按需拆）
── 再往后 ──
   H  （可选）Flowable 审批联动
   I  （可选）逾期 Job / 看板统计加深
```

出计划时固定习惯：
1. 左边 pmhub 路径，右边 TaskForge 路径  
2. 写清「不做」  
3. 验收用接口对比 / 两个 Token  
4. 编译：`mvn -pl taskforge-admin -am -DskipTests compile`

---

## 9. 关键源码锚点（深挖时直达）

| 主题 | 路径 |
|------|------|
| DataScope | `pmhub-framework/.../DataScopeAspect.java` |
| 登录权限装填 | `pmhub-framework/.../SysPermissionService.java` |
| 角色数据权限 | `SysRoleController.dataScope` + `SysRoleServiceImpl.authDataScope` |
| 创建项目 | `pmhub-project/.../ProjectServiceImpl.saveProject` |
| 任务编辑/审批 | `ProjectTaskServiceImpl.edit` |
| 任务审批动作 | `WfTaskServiceImpl.complete` / `claim` |
| SQL 全量 | `sql/pmhub_20240305.sql` |
| 自带分析文 | `article/01`~`05`、`AGENTS.md` |

---

## 10. TaskForge ↔ pmhub 差异备忘（写计划必查）

| 差异 | 影响 |
|------|------|
| Boot 3 / Security 6 vs Boot 2 / WebSecurityConfigurerAdapter | 配置 API 不同，心智可抄 |
| LambdaQueryWrapper 列表 vs Mapper XML | DataScope、复杂 join 要改写法 |
| 无 BaseEntity.params | 用 ThreadLocal / 显式 apply |
| 响应 R\<T\> | Controller 层转换，别混 AjaxResult |
| 项目主键 UUID 字符串 | 仿写可改为雪花/Long，计划里要显式决策 |
| 企微/RocketMQ 默认关 | MVP 可跳过 |
