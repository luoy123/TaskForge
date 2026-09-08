# Sprint D：数据权限 DataScope（自学版）

> **目标**：按角色 `data_scope`（1–5）过滤列表数据，并带上 `@DataScope.permission` 角色权限匹配；自己写代码，对照 pmhub。  
> **前置**：Sprint A–C 完成；部门树、`sys_role.data_scope`、`sys_role_dept` 表已存在。  
> **不做**：项目模块、验证码、在线用户。

---

## 0. 进度与本 Sprint 范围


| 阶段                                  | 状态                        |
| ----------------------------------- | ------------------------- |
| Sprint A 用户 CRUD                    | 完成                        |
| Sprint B 登录日志 + `@Log` + monitor    | 完成                        |
| Sprint C getInfo / Profile / Notice | 完成                        |
| **Sprint D**                        | **本文件：D1 → D2 → D3 → D4** |
| 再往后                                 | `taskforge-project`       |


```text
D1 @DataScope(+permission) + Aspect + PermissionContextHolder + 登录补齐 roles/deptId/角色perms
D2 用户列表 selectUserPage 接入过滤
D3 部门列表 selectDeptList 接入过滤
D4 角色配置 dataScope + sys_role_dept（自定义范围可测）
```

对照习惯：左边 pmhub，右边 TaskForge；返回体继续用 `R<T>`。

---

## 0.1 为何不能 1:1 抄 pmhub


| pmhub                                            | TaskForge 现状                                                                                              |
| ------------------------------------------------ | --------------------------------------------------------------------------------------------------------- |
| 列表多在 **Mapper XML**，`${params.dataScope}`        | 用户列表是 `**LambdaQueryWrapper` + `selectPage`**（`SysUserServiceImpl.selectUserPage`）                        |
| 切面往 `**BaseEntity.params`** 塞 `AND (...)`        | `SysUser` **不继承** BaseEntity；登录时 **未 set deptId**，也 **未加载角色列表**（`UserDetailsServiceImpl` 只装了 permissions） |
| `@DataScope` 挂在 **Service**，依赖 `user.getRoles()` | `SysUser` 无 `roles` 字段                                                                                    |


**本 Sprint 选定实现（固定方案）**：保留 pmhub 的「注解 + Aspect 拼条件」心智模型；注入方式改为 **ThreadLocal 存 SQL 片段**，在带 `@DataScope` 的 Service 方法里对 Wrapper 执行 `qw.apply(...)`。不强制先把用户列表整段改成 XML。

```text
Controller list
  → Service.@DataScope 方法
      → DataScopeAspect（@Before）：按角色拼条件 → 放入 DataScopeContext（ThreadLocal）
      → 业务 qw 构建完后：qw.apply(DataScopeContext.get())
      → @After / finally：clear ThreadLocal
  → MySQL
```

---

## 1. `data_scope` 含义（与库一致）

先查库确认：

```sql
SELECT role_id, role_name, data_scope FROM sys_role;
-- 确认存在表
SELECT TABLE_NAME FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_role_dept';
```


| 值   | 含义     | 过滤思路（用户单表，别名可空）                                                                            |
| --- | ------ | ------------------------------------------------------------------------------------------ |
| 1   | 全部     | 不加条件；`userId == 1` 直接跳过                                                                    |
| 2   | 自定义    | `dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = ?)`                         |
| 3   | 本部门    | `dept_id = {loginUser.deptId}`                                                             |
| 4   | 本部门及以下 | `dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = ? OR find_in_set(?, ancestors))` |
| 5   | 仅本人    | `user_id = {loginUser.userId}`（查询没有 user 列时结果应为空）                                          |


多角色：**OR** 合并各角色条件（对齐 pmhub）。  
若声明了 `permission`：只让「拥有该菜单权限字符」的角色参与拼条件（一人多角色时很关键）。

---

## 2. D1 — 基础设施（注解 + permission + 切面 + 登录补齐）

### 参考

- `E:/Code/02-Projects/pmhub-boot/pmhub-common/.../annotation/DataScope.java`（含 `permission`）
- `E:/Code/02-Projects/pmhub-boot/pmhub-framework/.../aspectj/DataScopeAspect.java`（角色过滤逻辑）
- `E:/Code/02-Projects/pmhub-boot/pmhub-framework/.../web/service/PermissionService.java`（`hasPermi` 里 `PermissionContextHolder.setContext`）
- TaskForge 已有：`[PermissionContextHolder](../taskforge-framework/src/main/java/com/zhq/taskforge/framework/security/context/PermissionContextHolder.java)`（目前几乎没被写入）

### 为什么要 `permission`

用户可能有多个角色，例如：

- 角色 A：`data_scope=1`（全部），但只有 `system:notice:list`
- 角色 B：`data_scope=5`（仅本人），有 `system:user:list`

查用户列表时若把两个角色的 dataScope **都 OR 进去**，会被角色 A「全部」放大。  
正确做法：只取「带有当前接口所需权限」的角色来拼数据范围——这就是 `permission`。

```text
permission 来源（对齐 pmhub）：
1. @DataScope(permission = "system:user:list") 显式指定
2. 若为空：PermissionContextHolder.getContext()
   ← 由 @PreAuthorize("@ss.hasPermi('...')") / hasPermi 在校验时 setContext
```

切面里对每个角色：

```text
if (permission 非空 && role.permissions 非空
    && role.permissions 不包含 permission 中任一权限字符)
    → continue（该角色不参与本次数据范围）
```

### 模块清单

```text
taskforge-common:
  annotation/DataScope.java                 # deptAlias / userAlias / permission
  core/domain/entity/SysUser.java           # + List<SysRole> roles
  core/domain/entity/SysRole.java           # + Set<String> permissions（exist=false）

taskforge-framework:
  datascope/DataScopeContext.java           # ThreadLocal SQL get/set/clear
  aspectj/DataScopeAspect.java
  web/service/PermissionService.java        # hasPermi 时 setContext
  web/service/UserDetailsServiceImpl.java   # deptId + roles + 每角色 menu perms
```

### 你要做

1. `**@DataScope**`：`deptAlias()`、`userAlias()`、`**permission()**`（默认 `""`）。
2. `**DataScopeContext**`：`set` / `get` / `clear`（ThreadLocal）。
3. `**SysRole**`：`@TableField(exist = false) private Set<String> permissions;`
  （该角色拥有的菜单权限字符集合，不是用户并集。）
4. `**SysUser**`：`@TableField(exist = false) private List<SysRole> roles;`
5. **登录补齐**（`UserDetailsServiceImpl`）：
  - `loginUser.setDeptId(userByName.getDeptId())`
  - 查角色列表（含 `dataScope`）→ `user.setRoles(...)`
  - **给每个角色填 `permissions`**：按 `roleId` 查菜单 perms（需 Mapper：按角色查 `sys_menu.perms`，可对照 pmhub / 现有 `SysMenuMapper` 扩展）
6. `**PermissionService.hasPermi**`：在真正校验前
  `PermissionContextHolder.setContext(permi);`  
   （`hasPermiOr` / `hasPermiAnd` 按 pmhub 同样写入当前权限上下文，逗号拼接或多个依次 set——以 pmhub 为准抄。）
7. `**DataScopeAspect**`（`@Before` + `@After`/`finally`）：
  - 先 `DataScopeContext.clear`
  - `LoginUser`；`userId == 1` → 跳过
  - `permission = @DataScope.permission()`；若空则用 `PermissionContextHolder.getContext()`
  - 遍历 `user.getRoles()`：先做 **permission 匹配**，再按 `dataScope` 拼条件
  - OR 合并后 `DataScopeContext.set(...)`
  - 方法结束 **必须 clear** ThreadLocal

### 拼条件注意

- 优先 **MyBatis-Plus 占位**：Service 里 `qw.apply("dept_id = {0}", deptId)`；或切面产出安全片段。
- 字面量 SQL 的数值只能来自 `LoginUser` 的 `Long`，**禁止**拼请求参数。
- `apply` 片段是条件体，注意不要和 MP 自动 AND 冲突导致语法错。

### 验收 D1

- 重新登录后：`deptId`、`user.roles[].dataScope`、`**user.roles[].permissions`** 都有值。
- 走带 `@PreAuthorize("@ss.hasPermi('system:user:list')")` 的接口时，请求内 `PermissionContextHolder.getContext()` 能读到该权限串。
- `mvn -pl taskforge-admin -am -DskipTests compile` 通过。

---

## 3. D2 — 接到用户列表

### 现状

`[SysUserServiceImpl.selectUserPage](../taskforge-system/src/main/java/com/zhq/taskforge/system/service/impl/SysUserServiceImpl.java)`：Lambda 条件 + `selectPage`，**无**数据权限。

### 你要改

1. 方法上加（建议显式带 permission，更稳）：

```java
@DataScope(deptAlias = "", userAlias = "", permission = "system:user:list")
```

单表 `sys_user`，别名可空；SQL 用 `dept_id` / `user_id`。  
不写 `permission` 时，依赖 Controller 上 `@PreAuthorize` → `PermissionService` 写入的 `PermissionContextHolder`。

1. 业务 `qw` 建完后：

```java
String scope = DataScopeContext.get();
if (StringUtils.hasText(scope)) {
    qw.apply(scope);
}
```

1. 准备测试角色/用户：把某非 admin 角色设为 `data_scope = 3` 或 `5`，与 admin 对比 `/system/user/list` 行数。
2. **permission 专项**：一人两角色（一个有 `system:user:list` 且 scope=5，另一个无该权限但 scope=1）→ 列表应按「仅本人」，不能被另一个角色放大成全部。

### 验收 D2


| 账号 / 场景                        | 期望                           |
| ------------------------------ | ---------------------------- |
| admin（userId=1 或 data_scope=1） | 看到全量（或未过滤）                   |
| 仅本人（5）                         | 列表只有自己                       |
| 本部门（3）                         | 只有同 `dept_id` 的用户            |
| 多角色 + permission               | 只按「拥有 list 权限」的角色算 dataScope |


---

## 4. D3 — 部门列表

### 现状

`[SysDeptServiceImpl.selectDeptList](../taskforge-system/src/main/java/com/zhq/taskforge/system/service/impl/SysDeptServiceImpl.java)`

### 你要改

- 同样挂 `@DataScope`（可带 `permission = "system:dept:list"`），按部门维度过滤（`dept_id` / `ancestors`）。
- **AOP 自调用坑**：若 `selectDeptTreeList` 内部 `this.selectDeptList(...)`，切面**不生效**。对照 pmhub：用 `SpringUtils.getAopProxy(this).selectDeptList(...)`。

### 验收 D3

受限角色看到的部门树明显变少/变矮。

---

## 5.**AOP 自调用坑**：若 `selectDeptTreeList` 内部 `this.selectDeptList(...)`，切面**不生效**。对照 pmhub：用 `SpringUtils.getAopProxy(this).selectDeptList(...)`。

### 验收 D3

### 现状

- 表 `sys_role_dept` 已有。
- 角色 CRUD 可能已有，但往往还缺「改 dataScope + 保存自定义部门」接口。

### 参考

`pmhub-admin/.../SysRoleController.java` 中与 dataScope / 部门树相关的接口。

### 你要做

1. 更新角色的 `dataScope` 字段。
2. 当 `data_scope = 2` 时：先清再写 `sys_role_dept`（`roleId` + `deptId` 列表）。
3. 需要则补 `SysRoleDept` 实体 / Mapper（`BaseMapper` 即可）。

### 验收 D4

角色设为自定义并勾选若干部门 → 该角色用户调 `/system/user/list` 只能看到这些部门下的人。

---

## 6. 推荐动手顺序


| 天    | 任务      | 完成标志                                                                                       |
| ---- | ------- | ------------------------------------------------------------------------------------------ |
| Day1 | D1      | LoginUser 带 `deptId` + `roles` + 每角色 `permissions`；`hasPermi` 写入 Context；切面含 permission 过滤 |
| Day2 | D2      | 非 admin 列表被过滤；多角色 permission 场景不被放大                                                        |
| Day3 | D3 + D4 | 部门树过滤 + 自定义范围可演示                                                                           |


每完成一块：

```powershell
mvn -pl taskforge-admin -am -DskipTests compile
```

用两个 Token 在 Knife4j 对比 `/system/user/list`。

---

## 7. 常见坑


| 坑                             | 处理                                                                           |
| ----------------------------- | ---------------------------------------------------------------------------- |
| ThreadLocal 不 clear           | `@After` / `finally` 必 `clear`                                               |
| 超管也被滤                         | `userId == 1` bypass                                                         |
| 范围 3/4 全空                     | 登录必须 `setDeptId`                                                             |
| 切面不跑                          | 避免 `this.xxx` 自调用；用 AOP 代理                                                   |
| SQL 注入                        | 禁止拼请求参数；只用登录态 Long / MP `{0}` 占位                                             |
| `data_scope=2` 列表空            | 未配 `sys_role_dept` 时为空是预期，需 D4                                               |
| 多角色被「全部」放大                    | 角色要带 `permissions`；切面按 `permission` 跳过无关角色                                   |
| `PermissionContextHolder` 一直空 | `PermissionService.hasPermi` 必须 `setContext`；或 `@DataScope` 显式写 `permission` |
| 角色 `permissions` 为空导致全跳过      | 登录时按角色装菜单权限；空集合时与 pmhub 行为对齐（有 permission 要求时该角色不参与）                         |


---

## 8. 本 Sprint 明确不做

- 把所有列表改回 XML `${params.dataScope}`
- 头像上传、验证码、在线用户
- `taskforge-project` / 任务看板

D1–D4 过关后，下一站再开 **项目管理模块**。

---

## 9. 卡住时怎么问

贴：**文件路径 + 方法片段 + 两个账号下列表差异（或 SQL 日志）**。  
例如「角色 data_scope=5 但仍看到全部用户」比整文件粘贴更好定位。