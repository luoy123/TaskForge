# 前端 Sprint 07：系统缺页补齐（岗位 / 字典 / 参数 / 公告 / 日志）

> **编号**：`plan/frontend/07`  
> **状态**：**联调通过**（空 `status=` 列表为空已修；日志路由为 `/system/log/*`）；**F07-7 角色菜单树已补**  


> **目标**：把侧栏里仍进 `building` 的系统管理 / 监控日志页接到已有后端 API，减少「建设中」占位。  
> **前置**：前端 01–06 完成；后端 B/C（日志、岗位、字典、参数、公告）主路径可用。  
> **不做**：服务/缓存监控大盘（等后端 N）、代码生成 UI（等 O）、工作流分类/表单设计器、改后端接口契约；**数据权限弹窗**（部门树）仍可后置。

---

## 0. 与后端 / 前端进度对齐

| 层 | 现状 |
|----|------|
| 后端 A–J | ✅ |
| 后端 **K** Job 管理端 | ✅ API 已齐；前端 `views/monitor/job` 已有壳 |
| 后端 L–O | ○ 排队（审批打磨 / 项目加深 / Server 监控 / generator） |
| 前端 01–06 | ✅（含验证码、在线用户、定时任务页） |
| **缺口** | 岗位 / 字典 / 参数 / 公告 / 登录日志 / 操作日志 仍多进 `building` |

```text
F07-1  api/system：post · dict · config · notice
F07-2  api/monitor：logininfor · operlog
F07-3  岗位 / 参数 / 公告 CRUD 页（套路同用户页）
F07-4  字典：类型列表 + 数据项（可抽屉/二级页）
F07-5  登录日志 / 操作日志：查询 + 删除/清空
F07-6  自测：侧栏点开各菜单不再进 building；超管 CRUD / 清日志 OK
F07-7  角色编辑：菜单权限树（后端已有，原角色页未接）
```

建议顺序：**1 → 2 → 3 → 5 → 4 → 6**（字典字段稍多，可后做）；**F07-7** 可随时补（发现角色页无法分配菜单时）。

**协作**：API + 表格/对话框壳由助手写；`getList` / `submitForm` / `handleDelete`（及日志清空）搜 `【学员填写】`。

---

## 1. 后端接口速查（以 Controller 为准）

| 模块 | 前缀 | 典型能力 |
|------|------|----------|
| 岗位 | `/system/post` | list / 详情 / 增改删 |
| 字典 | `/system/dict` | 类型与数据（对照现有 `SysDictController` 方法名） |
| 参数 | `/system/config` | list / 增改删；`sys.account.captchaEnabled` 可在此改 |
| 公告 | `/system/notice` | list / 增改删 |
| 登录日志 | `/monitor/logininfor` | list（分页）/ 删 / clean |
| 操作日志 | `/monitor/operlog` | list（分页）/ 删 / clean |

对照习惯：系统管理多为 **GET query + POST/PUT/DELETE**；日志 list 带 `pageNum`/`pageSize`，返回 `R<Page>` 用 `records`/`total`。  
写前先用 Swagger 或浏览器 Network 看一眼真实字段，避免猜 VO。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/system/
    post.js · dict.js · config.js · notice.js
  api/monitor/
    logininfor.js · operlog.js
  views/system/
    post/index.vue
    dict/index.vue          # 类型表 + 数据抽屉/子表
    config/index.vue
    notice/index.vue
  views/monitor/
    logininfor/index.vue
    operlog/index.vue
  # job / online 已有，本 Sprint 不动
```

菜单 `component` 一般已是 `system/post/index`、`monitor/operlog/index` 等，与 `views` 对齐即可；对不上再补 `COMPONENT_ALIAS`。

---

## 3. 分阶段要点

### F07-1 / F07-2 API

照抄 `api/system/user.js` / `api/monitor/online.js` 风格：`listXxx`、`addXxx`、`updateXxx`、`delXxx`；日志另加 `cleanXxx`。

### F07-3 岗位 / 参数 / 公告

- 表格 + 搜索 + 对话框增改删；挂 `v-hasPermi`（与库表 `sys_menu.perms` 对齐）。
- 参数页注意：改 `sys.account.captchaEnabled` 后登录页行为会变（可顺带回归 06）。

### F07-4 字典

- MVP：左侧/上表「字典类型」，点开抽屉维护「字典数据」。
- **不做**：前端缓存刷新全站字典（可后置）。

### F07-5 日志

- 登录日志：用户名 / IP / 状态 / 时间筛选；删选中；清空。
- 操作日志：模块 / 操作人 / 状态 / 时间；详情可弹窗看 request 摘要（有字段再展示）。
- 清空二次确认。

### F07-6 自测

| 步骤 | 期望 |
|------|------|
| 侧栏点岗位/字典/参数/公告 | 非 building，有数据或空表 |
| 新增一条公告再删 | `code===200` |
| 改 captcha 参数开关 | 登录页验证码显隐符合 06 |
| 登录日志有记录；清空后变少 | 与后端一致 |
| 无权限角色 | 按钮消失或 403 |

### F07-7 角色菜单权限（✅ 已落地）

**背景**：角色编辑弹窗原先只有名称/权限字符/排序/状态/备注，未接菜单树。后端 `PUT /system/role` 的 `menuIds` 与 `GET /system/menu/roleMenuTreeselect/{roleId}` 已可用；不传 `menuIds` 时 `updateRole` 会先清空再按空列表插入，等于**冲掉该角色全部菜单**。

**落位**：

```text
api/system/menu.js          # + treeselect · roleMenuTreeselect
views/system/role/index.vue # 编辑弹窗加 el-tree；提交带 menuIds + menuCheckStrictly
```

**要点**：

- 编辑：先调 `roleMenuTreeselect`，打开弹窗后 `setCheckedKeys(checkedKeys)`。
- 提交：`menuIds = checkedKeys ∪ halfCheckedKeys`（父子联动时父节点常为半选）。
- 顶层 `parent_id=0` 的按钮（如 `project:manage:approve`）会出现在树最外层，可勾。
- 保存后提示：用户需**重新登录**刷新 Redis 里的权限缓存。

**自测**：admin 编辑「测试管理员」勾上「项目审批 / 任务审批 / 任务办理 / 任务驳回」等 → 保存 → py 重登 → 项目列表出现「发起审批」。

---

## 4. 验收标准

- [x] 上述 6 个菜单路径均映射到真实页（`system/*/index`、`monitor/*/index` 与菜单 component 一致）
- [x] 岗位 / 参数 / 公告至少完成列表 + 新增 + 删除联调（接口冒烟：list 全绿；公告增删 OK）
- [x] 字典类型可维护，且能维护其下数据项（抽屉）
- [x] 登录日志、操作日志可查询；支持删除与清空
- [x] 权限字挂接，超管可操作（`v-hasPermi` 对齐 `sys_menu.perms`）
- [x] 角色编辑可勾选菜单树并写回 `sys_role_menu`（F07-7）

```bash
mvn -pl taskforge-admin -am -DskipTests compile
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

08–19 已按后端 L–W 拆开，索引见 [plan/README.md](../README.md)。下一刀在本 Sprint 做完后，等对应后端再开（先 08 对齐 L）。

路线总览：[plan/README.md](../README.md)。
