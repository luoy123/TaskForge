# Sprint C：getInfo / 个人中心 / 通知公告（自学版）

> **目标**：补齐前端登录后必备的系统接口；自己写代码，对照 pmhub。  
> **前置**：Sprint A（用户 CRUD）、Sprint B（登录/操作日志 + monitor）已完成。  
> **不做**：`@DataScope`、项目模块、验证码、在线用户。

---

## 0. 进度与本 Sprint 范围


| 阶段                               | 状态                              |
| -------------------------------- | ------------------------------- |
| Sprint A 用户 CRUD                 | 完成                              |
| Sprint B 登录日志 + `@Log` + monitor | 完成                              |
| **Sprint C**                     | **本文件：C1 → C2 → C3**            |
| 再往后                              | DataScope 或 `taskforge-project` |


```text
C1 GET /getInfo          → 给前端 user + roles + permissions
C2 /system/user/profile  → 改自己的资料 / 密码
C3 /system/notice        → 通知公告 CRUD
```

对照习惯：左边 pmhub，右边 TaskForge；返回体继续用你们的 `R<T>`（不要照抄 `AjaxResult`）。

---

## 1. C1 — `GET /getInfo`

### 现状

- `[SysLoginController](../taskforge-admin/src/main/java/com/zhq/taskforge/web/controller/system/SysLoginController.java)` 只有 `/getRouters`
- `/system/user/me` 信息太少，且权限绑了 `USER_LIST`，不适合「任意登录用户取自己信息」

### 参考

`E:/Code/02-Projects/pmhub-boot/pmhub-admin/.../SysLoginController.java` → 方法 `getInfo`：

```text
user = SecurityUtils.getLoginUser().getUser()
roles = 角色权限标识集合
permissions = 菜单权限标识集合
返回 { user, roles, permissions }
```

### 你要改/加

**文件**：`SysLoginController.java`（与 `/getRouters` 同级，路径 `GET /getInfo`）

**思路伪代码：**

```java
@GetMapping("/getInfo")
@PreAuthorize("isAuthenticated()")  // 登录即可，不要 USER_LIST
public R<Map<String, Object>> getInfo() {
    LoginUser loginUser = SecurityUtils.getLoginUser();
    SysUser user = loginUser.getUser();
    user.setPassword(null); // 别把密码哈希返回前端

    Set<String> permissions = loginUser.getPermissions(); // 登录时已装进 LoginUser

    // roles：若 LoginUser / SysUser 上已有角色信息就取；
    // 没有就查 sys_user_role + sys_role，取出 roleKey 集合
    Set<String> roles = ...;

    Map<String, Object> data = new HashMap<>();
    data.put("user", user);
    data.put("roles", roles);
    data.put("permissions", permissions);
    return R.ok(data);
}
```

### 注意

1. **密码清空**：`user.setPassword(null)`
2. **permissions**：优先用 `LoginUser` 里现成的；admin（userId=1）一般是全部权限
3. **roles**：pmhub 用 `permissionService.getRolePermission(user)`；你们若没有同类方法，可：
  - 查该用户角色列表，返回 `roleKey`（如 `admin`、`common`）
  - 或先返回空 Set / 简单实现，保证 permissions 先通
4. 建议顺手把 `/getRouters` 的权限也改成 `isAuthenticated()`（登录用户都要拉路由）

### 验收 C1

1. 登录拿 Token
2. `GET /getInfo`，Header：`Authorization: Bearer <token>`
3. 响应里有 `user`、`permissions`（至少 permissions 非空，admin 更明显）

---

## 2. C2 — 个人中心 `SysProfileController`

### 参考

`pmhub-boot/.../controller/system/SysProfileController.java`

### 新建

`taskforge-admin/.../web/controller/system/SysProfileController.java`  
`@RequestMapping("/system/user/profile")`


| 方法  | 路径           | 说明                        |
| --- | ------------ | ------------------------- |
| GET | `/`          | 当前用户资料                    |
| PUT | `/`          | 更新昵称、手机、邮箱、性别等（**不能改别人**） |
| PUT | `/updatePwd` | 旧密码 + 新密码                 |


### 实现要点

**查资料：**

```text
LoginUser → userId → selectUserById（或 getById）
清空 password 再返回
```

**改资料：**

```text
只允许改 SecurityUtils.getUserId() 对应的用户
可复用 checkPhoneUnique（带 userId 排除自己）
不要接收并更新 password / userName（用户名一般不允许自己乱改）
```

**改密码：**

```text
body: oldPassword, newPassword
用 SecurityUtils.matchesPassword(old, dbPassword) 校验旧密码
新密码 SecurityUtils.encryptPassword 后 updateById
可选：改密成功后让 Token 失效（清 Redis）——第一版可先不做
```

写操作建议加 `@Log(title = "个人信息", businessType = UPDATE)`。

### 验收 C2

- GET profile 能看到自己  
- PUT 改昵称成功  
- updatePwd：旧密码错失败；对了能用新密码登录

---

## 3. C3 — 通知公告 `SysNotice`

### 先查表

用 MySQL MCP 或客户端：

```sql
SHOW TABLES LIKE 'sys_notice';
DESC sys_notice;
```

- **有表**：按表字段写 Entity  
- **无表**：对照 pmhub 的 `sys_notice` 建表（或看 pmhub SQL 脚本）

### 参考

- Entity：`pmhub-system/.../domain/SysNotice.java`  
- Controller：`pmhub-admin/.../SysNoticeController.java`

### 模块清单（按你们惯例）

```text
taskforge-system:
  domain/SysNotice.java
  mapper/SysNoticeMapper.java        # 可 extends BaseMapper
  service/ISysNoticeService.java
  service/impl/SysNoticeServiceImpl.java

taskforge-admin:
  web/controller/system/SysNoticeController.java
  （若用自定义 XML）resources/mapper/system/SysNoticeMapper.xml
```

### 接口建议

`@RequestMapping("/system/notice")`


| 方法     | 路径             | 权限建议                                   |
| ------ | -------------- | -------------------------------------- |
| GET    | `/list`        | `system:notice:list`                   |
| GET    | `/{noticeId}`  | `system:notice:query`                  |
| POST   | `/`            | `system:notice:add` + `@Log INSERT`    |
| PUT    | `/`            | `system:notice:edit` + `@Log UPDATE`   |
| DELETE | `/{noticeIds}` | `system:notice:remove` + `@Log DELETE` |


list 用 MyBatis-Plus `Page`（与用户、日志模块一致）。  
在 `PermissionConstants` 增加 `NOTICE_*` 常量。

### 验收 C3

Knife4j：分页列表、新增、修改、删除；`sys_oper_log` 有对应操作记录。

---

## 4. 推荐动手顺序


| 天    | 任务              | 完成标志                  |
| ---- | --------------- | --------------------- |
| Day1 | C1 getInfo      | Token 能取到 permissions |
| Day2 | C2 profile + 改密 | 资料/密码自助可改             |
| Day3 | C3 Notice       | CRUD + `@Log` 通       |


每完成一块：`mvn -pl taskforge-admin -am -DskipTests compile`，重启测接口。

---

## 5. 常见坑


| 坑                                | 处理                              |
| -------------------------------- | ------------------------------- |
| getInfo 把 password 返回了           | `setPassword(null)`             |
| getInfo / getRouters 要 USER_LIST | 改成 `isAuthenticated()`          |
| profile 能改别人 userId              | 强制用 `SecurityUtils.getUserId()` |
| 改密没校验旧密码                         | 必须 `matchesPassword`            |
| Notice 403                       | admin 测；或补菜单权限串                 |
| 表不存在                             | 先建 `sys_notice` 再写代码            |


---

## 6. 本 Sprint 明确不做

- `@DataScope`  
- 头像上传（profile 可后补）  
- 验证码、在线用户、服务监控  
- `taskforge-project`

C1–C3 过关后，再选：**DataScope** 或 **项目管理模块**。

---

## 7. 卡住时怎么问

贴：**文件路径 + 方法片段 + 报错/响应**。  
例如「getInfo 里 permissions 是空的」比整文件粘贴更好定位。