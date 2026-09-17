# 前端 Sprint 04：权限 + 文件/导入导出 + 项目任务加深（对齐后端 G / I）

> **编号**：`plan/frontend/04`  
> **状态**：✅ **壳已落地**（待浏览器联调验收）  
> **目标**：把后端 **G（文件/导入导出）** 与 **I（列表工厂/成员/我的任务/统计）** 接到 UI；顺带补按钮权限、收掉 F03 静态菜单。  
> **前置**：前端 01–03 完成；后端 A–G、I1–I5a 主路径可用。  
> **不做**：Flowable 审批页（→ 前端 05，等后端 H3–H5）、企微、完整看板大屏、I5b 定时任务管理页、OSS。

---

## 0. 与后端进度对齐

| 后端 | 状态 | 前端本刀 |
|------|------|----------|
| A–G 系统 / 项目 / 任务 / 文件 | ✅ | 本 Sprint 主吃 **G** |
| **H** Flowable | ▶ H1–H2 有；H3–H5 业务未完 | **不接 UI**（侧栏继续进 `building`） |
| **I** 工厂 / 成员 / 统计 / 逾期 Job | ✅ I1–I5a | 本 Sprint **择要接 UI** |
| **J** 验证码 / 在线用户 / 限流 | ○ 排队 | 后置（前端 06 候选） |

```text
✅ F04-1  v-hasPermi + 系统页挂权限字
✅ F04-2  收尾 F03：去掉静态 system/project 路由与兜底菜单
✅ F04-3  项目列表加深：归档/收藏 + I1 列表 type（我的/收藏/回收站）
✅ F04-4  项目成员：列表 / 邀请 / 移除（I2）— MemberDrawer
✅ F04-5  文件附件：上传 / 列表 / 下载 / 重命名 / 删除（G）— FilePanel
✅ F04-6  任务 Excel：导出 / 导入（G4）
✅ F04-7  任务加深：评论 / 动态 / 子任务 +「我的任务」轻量页（I3）
✅ F04-8  首页 statistics 数字卡片（I4）
```

建议顺序已跑完 1～8。验收对照 §4；下一前端刀见 [05-sprint-workflow-ui.md](./05-sprint-workflow-ui.md)（等后端 H3–H5）。

**协作**：API 壳 + 页面壳由助手写；列表/上传/权限判断等关键逻辑搜 `【学员填写】`。

---

## 1. 后端接口速查

### 项目（E + I1）

| 动作 | 方法 | 路径 |
|------|------|------|
| 列表 | POST | `/project/list`（可加 `type`：我的 / 收藏 / 回收站，以现有 VO 为准） |
| 归档 / 取消 | POST | `/project/archive` · `/project/cancelArchive` |
| 收藏 / 取消 | POST | `/project/collect` · `/project/cancelCollect` |
| 统计 | POST/GET | `/project/statistics` |
| 进行中 / 下拉 | POST | `/project/doing` · `/project/select`（路径以 Controller 为准） |

### 成员（I2）

| 动作 | 方法 | 路径 |
|------|------|------|
| 列表 | POST | `/project/member/list` |
| 邀请 | POST | `/project/member/add` |
| 移除 | POST | `/project/member/remove` |

### 任务（F + G4 + I3）

| 动作 | 方法 | 路径 |
|------|------|------|
| 评论 / 动态 | POST | `/project/task/addComment` · `/project/task/log/list` |
| 子任务 | POST | `/project/task/addChildTask` · `/queryChildTask` |
| 我的任务 | POST | `/project/task/queryMyTaskList`（`type`：1 我执行 / 3 我创建） |
| 状态概况 | POST | `/project/task/situation` |
| 导出 | POST | `/project/task/export` · `/exportAll` |
| 导入 | POST | `/project/task/import`（multipart） |

### 文件（G）

| 动作 | 方法 | 路径 |
|------|------|------|
| 上传 | POST | `/project/file/upload`（`type`=project/task/cover + `ptId`） |
| 列表 / 重命名 / 删除 | POST | `/project/file/list` · `/rename` · `/delete` |
| 下载 | GET | `/project/file/download` |

对照：项目域多为 **POST + JSON**；上传/导入为 **multipart**；导出注意 `responseType: 'blob'`。

---

## 2. 前端落位

```text
taskforge-ui/src/
  directive/permission/       # v-hasPermi
  api/project/
    project.js                # + archive / collect / statistics / type
    member.js                 # 新建
    task.js                   # + comment / log / child / myTask / import / export
    file.js                   # 新建
  views/project/
    list/index.vue            # Tab/type + 归档收藏 + 进成员/附件
    member/index.vue          # 或抽屉嵌在项目行
    task/index.vue            # 评论/动态/子任务/附件/导入导出
    my-task/index.vue         # 可选：我执行/我创建
  views/home/index.vue        # 可选：statistics 卡片
  router/index.js             # F04-2 删静态 children
  layout/index.vue            # 删静态兜底菜单
```

权限字：`getInfo` → `user.permissions`；菜单 `F` 类型 `perms`；`*:*:*` 放行。

---

## 3. 分阶段要点

### F04-1 按钮权限

- 全局指令：无权限移除 DOM；超管 / `*:*:*` 放行。
- 先挂系统管理页增删改（如 `system:user:add`），与库表 `sys_menu.perms` 对齐。

### F04-2 收尾动态路由

对照 [03-sprint-dynamic-routes.md](./03-sprint-dynamic-routes.md)：

- [ ] 常量路由只留 login / home / 404 等
- [ ] layout 不再渲染静态「系统管理/项目管理」
- [ ] 刷新业务 URL 仍靠 `getRouters` + `addRoute`

### F04-3 项目列表加深

- 行操作：归档 / 收藏（二次确认）。
- I1：Tab 或下拉切 `type`（我的 / 收藏 / 回收站）。

### F04-4 成员

- 指定 `projectId`：成员表格 + 邀请（选用户）+ 移除。
- **不做**：复杂项目角色矩阵。

### F04-5 / F04-6 文件与 Excel

- 项目/任务维度附件 CRUD；封面 `cover` 可后置。
- 任务页导出当前项目、导入 xlsx；处理好 blob 与错误 JSON。

### F04-7 / F04-8 任务与统计

- 评论 + 动态时间线 + 子任务最小闭环。
- 「我的任务」页或入口：`queryMyTaskList`。
- 可选：首页四数字（项目数/任务数/今日/逾期）。

---

## 4. 验收标准

- [x] 非超管无权限按钮不可见；有权限可点且 `code===200`
- [x] 侧栏与业务页只依赖 `getRouters`，无静态 system/project 兜底
- [x] 项目可按 type 切列表；可归档/收藏
- [x] 成员可列表/邀请/移除
- [x] 项目/任务可上传附件并下载；可重命名/删除
- [x] 指定项目可导出再导入（或按后端规则提示）
- [x] 任务可评论/看动态/增查子任务；我的任务有数据
- [x] （可选）statistics 卡片数字与接口一致

```bash
mvn -pl taskforge-admin -am -DskipTests compile
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

| 文档 | 何时做 |
|------|--------|
| [05-sprint-workflow-ui.md](./05-sprint-workflow-ui.md) | 后端 H3–H5（start/todo/complete/reject + 禁改）联调通过后 |
| 06 候选 | 系统缺页（岗位/字典/公告/日志）或对接后端 J（验证码/在线用户） |

路线总览：[plan/README.md](../README.md)。
