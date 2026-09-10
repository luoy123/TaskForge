# Sprint E：项目管理（第一刀 · 自学版）

> **目标**：新建 `taskforge-project` 模块，打通「项目」生命周期：创建（自动阶段+成员+日志）、列表、详情、编辑、删除、归档/退出/收藏；自己写代码，对照 pmhub。  
> **前置**：Sprint A–D 完成（登录 / RBAC / `@Log` / DataScope）。  
> **库表**：本地库已有 `pmhub_project*` 表，**直接复用表结构**，不必另起一套 DDL（实体可继续映射这些表名，或后续再迁 `tf_` 前缀）。  
> **不做**：任务 CRUD、文件上传工厂、Flowable 审批、企微 OA、验证码、在线用户。

---

## 0. 进度与本 Sprint 范围

| 阶段 | 状态 |
|------|------|
| Sprint A–C 系统主线 | 完成 |
| Sprint D DataScope | 完成 |
| **Sprint E** | **本文件：E1 → E2 → E3 → E4** |
| 再往后 | [任务模块 Sprint F](./06-sprint-f-task.md) → [文件/导入导出 G](./07-sprint-g-file.md) |

```text
E1  Maven 模块骨架 + 枚举/实体/Mapper 落位  ✅
E2  创建项目 saveProject（阶段 + 创建者成员 + 日志）+ 基础详情  ✅
E3  列表（分页；可先不做工厂，再升级 QueryProjectFactory）+ 编辑 + 删除  ✅
E4  归档 / 取消归档 / 退出 / 收藏  ✅
```

对照习惯：左边 pmhub，右边 TaskForge；返回体继续用 `R<T>`；分页继续用 MyBatis-Plus `Page`（不必硬抄 PageHelper）。

---

## 0.1 模块与依赖（相对 pmhub 的简化）

```text
pmhub-project 依赖：common + oa + workflow
TaskForge 本 Sprint：
  taskforge-project  → 只依赖 taskforge-common
  taskforge-admin    → 再依赖 taskforge-project（挂 Controller）
```

`oa` / `workflow` 本 Sprint **不要**引进来。成员昵称等需要查用户时：在 project Mapper XML 里 join `sys_user`，或注入 `ISysUserService`（注意：若 project → system，则 admin 依赖图仍成立；**更干净**是 Mapper join，避免 project 依赖 system）。

推荐依赖方向：

```text
common ← project
common ← system ← framework ← admin
                    ↑
                 project（admin 聚合）
```

---

## 1. 领域心智（先读再写）

```text
Project
  ├── ProjectStage     创建时按枚举插齐，回写当前 stageId
  ├── ProjectMember    pt_id + type=project，创建者 creator=1
  ├── ProjectLog       动态日志（create / inviteMember / edit …）
  └── ProjectCollection 收藏（userId + projectId）
```

**状态（项目）**：未开始(0) → 进行中(1) → 已归档(2)；旁路逾期(3)/暂停(4)。  
**公私**：`type` 0 公开 / 1 私有（详情要对私有做成员校验）。  
**软删**：`deleted` 字段（对照表结构，别和 `sys_*` 的 `del_flag` 搞混）。

### 创建项目副作用（必须写进 E2）

对照 `ProjectServiceImpl.saveProject`：

1. `projectCode = "P" + Seq`  
2. insert 项目  
3. 按 `ProjectStageEnum` 插入全部阶段  
4. 查 STAGE_0，回写 `projectStageId`  
5. insert 创建者成员（`creator=1`）  
6. 写 create + inviteMember 日志  

---

## 2. E1 — 模块骨架 + 领域模型

### 参考

- `/home/zxb/projects/pmhub-boot/pmhub-boot/pmhub-project/pom.xml`
- `.../project/domain/Project.java` 等实体
- `.../common/enums/ProjectStageEnum.java` / `ProjectStatusEnum.java`
- `sql` 里已有表：你库中的 `pmhub_project*`（DBX / Navicat 看字段即可）

### 你要做

1. **父 POM** 增加 `<module>taskforge-project</module>`，`dependencyManagement` 声明 `taskforge-project`。  
2. 新建模块目录：

```text
taskforge-project/
  pom.xml
  src/main/java/com/zhq/taskforge/project/
    domain/          # Project, ProjectStage, ProjectMember, ProjectLog, ProjectCollection
    domain/vo/       # ProjectReqVO, ProjectResVO, ProjectVO …（按需，别一次造太多）
    mapper/
    service/ + impl/
  src/main/resources/mapper/project/   # 需要 XML 的复杂列表再写
```

3. **枚举**放到 `taskforge-common/.../enums/`（与 pmhub 一致，方便多模块共用）：  
   - `ProjectStageEnum`（5 个阶段名照抄 pmhub）  
   - `ProjectStatusEnum`（含 PROJECT 类型名给 member.type 用）  
4. **实体**用 MyBatis-Plus：`@TableName("pmhub_project")` 等，主键策略对齐表（pmhub 多用 UUID 字符串）。  
5. `admin` 的 `pom.xml` 依赖 `taskforge-project`；启动后能扫到 Mapper（`@MapperScan` 覆盖 `com.zhq.taskforge.**.mapper`）。

### 验收 E1

```bash
mvn -pl taskforge-admin -am -DskipTests compile
```

实体/枚举齐全，应用能启动（哪怕还没有业务接口）。

---

## 3. E2 — 创建 + 详情

### 参考

- `pmhub-project/.../ProjectServiceImpl.saveProject` / `detail`
- `pmhub-admin/.../ProjectController` → `POST /project/add`、`POST /project/detail`

### 你要做

| 接口（建议路径） | 权限字符（可先对齐 pmhub） | 行为 |
|------------------|---------------------------|------|
| `POST /project/add` | `project:manage:add` | `saveProject` 全套副作用 |
| `POST /project/detail` 或 `GET /project/{id}` | `project:manage:detail` | 详情；私有项目非成员拒绝 |

权限：若菜单里还没有这些 `perms`，可暂时 `@PreAuthorize("isAuthenticated()")`，但计划里**记下要补菜单**，别长期裸奔。

日志：可先 `ProjectLogService` 简单 insert；operateType 字符串与 pmhub 对齐（`create` / `inviteMember`）。

### 验收 E2

1. 创建项目成功。  
2. DB：`pmhub_project` 1 行；`pmhub_project_stage` = 枚举数量；`pmhub_project_member` 创建者一条；`pmhub_project_log` 至少 2 条。  
3. 详情能带出阶段列表 / 是否收藏（收藏字段可先 false）。

---

## 4. E3 — 列表 + 编辑 + 删除

### 参考

- `ProjectServiceImpl.list` + `QueryProjectFactory`（我的项目 / 收藏 / 回收站）  
- `editProject` / `deleteProject`

### 本 Sprint 列表策略（固定）

**第一版（必做）**：只做「我参与/我创建的项目」分页列表（join `pmhub_project_member`），用 MP `Page` + Wrapper/XML 均可。  

**第二版（鼓励）**：再抽 `QueryProjectFactory` + 3 个 Executor，对照 pmhub 的 type 分流。工厂可以放在 E3 后半或 E4 后，**不要**卡死在工厂设计上写不出列表。

### 业务规则（删除/编辑）

| 操作 | 规则（对齐 pmhub） |
|------|-------------------|
| 删除 | 软删；若已有任务则禁止（本 Sprint 还没任务表写入，可先留校验空实现 / 查 `pmhub_project_task` count） |
| 编辑 | 更新字段 + 写日志；注意状态流转别乱跳 |
| 列表 | 填充 statusName、是否收藏、负责人昵称 |

### 验收 E3

- 分页列表只看到与自己相关的项目。  
- 编辑后详情字段变化且有日志。  
- 删除后列表不可见（软删），DB `deleted=1`。

---

## 5. E4 — 归档 / 退出 / 收藏

### 参考

- `ProjectServiceImpl.archived` / `cancelArchived` / `quit`  
- `ProjectCollectionController` 收藏/取消  
- Controller：`POST /project/archive`、`/cancelArchive`、`/quit`；收藏相关路径对照 pmhub

### 规则摘要

| 操作 | 要点 |
|------|------|
| 归档 | 通常要求已发布等（对照源码条件，不要猜） |
| 取消归档 | 状态回到进行中一类 |
| 退出 | 创建者不能退出 |
| 收藏 | `userId + projectId` 唯一；列表带 `collected` |

### 验收 E4

用两个账号（创建者 / 普通成员）在 Knife4j 走通：归档、退出失败/成功、收藏后列表标记变化。

---

## 6. 推荐动手顺序


| 天 | 任务 | 完成标志 |
|----|------|----------|
| Day1 | E1 | 模块可编译启动；实体映射已有表 |
| Day2 | E2 | 创建项目副作用齐全；详情可读 |
| Day3 | E3 | 列表+编辑+删除可用 |
| Day4 | E4 | 归档/退出/收藏可演示 |


每完成一块：

```bash
mvn -pl taskforge-admin -am -DskipTests compile
```

用 Token 调 Knife4j：`/project/add` → `/project/detail` → `/project/list`。

---

## 7. 常见坑


| 坑 | 处理 |
|----|------|
| project 依赖了 workflow/oa | 本 Sprint 禁止；编译会拖进一堆无关东西 |
| 主键 UUID vs Long | 跟表现有类型走；别和 `sys_user.user_id` 混用类型时忘转换 |
| `deleted` vs `del_flag` | 项目表用自己的软删字段；`@TableLogic` 值对齐库 |
| 列表把自己项目滤没了 | member 表 `pt_id/type/userId` 条件写错 |
| 自调用 AOP | 若以后列表方法互相调且要切面，用 `SpringUtils.getAopProxy` |
| 权限字符 403 | 菜单未配 `project:manage:*`；开发期可短暂放宽，收尾补菜单 SQL |
| 一次写完全部 VO/工厂 | 先跑通主路径，再抽象 |

---

## 8. 本 Sprint 明确不做

- `ProjectTask` 全套（下个 Sprint）  
- 文件上传 `UploadFileFactory`  
- Flowable / `startProjectApprove`  
- 企微通知、逾期 Job  
- 验证码、在线用户、服务监控  

E1–E4 过关后，下一站开 **Sprint F：任务 CRUD（子任务 / 评论 / 状态）**。

---

## 9. 卡住时怎么问

贴：**接口路径 + 请求体 + 表里相关行（project/stage/member/log）+ 期望 vs 实际**。  
例如「创建后 stage 只有 1 条」比整文件粘贴更好定位。

---

## 10. 关键源码锚点

| 主题 | 路径 |
|------|------|
| 创建项目 | `pmhub-project/.../ProjectServiceImpl.saveProject` |
| 列表工厂 | `pmhub-project/.../service/project/QueryProjectFactory` |
| 归档/退出 | 同文件 `archived` / `quit` |
| Controller | `pmhub-admin/.../controller/project/ProjectController.java` |
| 阶段枚举 | `pmhub-common/.../enums/ProjectStageEnum.java` |
| 表结构 | 库表 `pmhub_project*` 或 `sql/pmhub_20240305.sql` |
| 总对照 | [00-pmhub-reference-map.md](./00-pmhub-reference-map.md) |
