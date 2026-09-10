# Sprint B：登录日志 + 操作日志 + Monitor 查询（自学版）

> **目标**：自己写代码，对照 pmhub-boot。本文件只讲「写什么、怎么写、对哪份参考」。
> **前置**：Sprint A 用户 CRUD 已完成。
> **不做**：项目模块、DataScope、个人中心、验证码（下一批再开）。

---

## 0. 你现在有什么 / 缺什么

### 已有（不要重写）

| 组件 | 路径 |
|------|------|
| `@Log` 注解 | `taskforge-common/.../annotation/Log.java` |
| `BusinessType` | `taskforge-common/.../enums/BusinessType.java` |
| `LogAspect` | `taskforge-framework/.../aspectj/LogAspect.java` |
| `AsyncFactory.recordLogininfor` | `taskforge-framework/.../manager/factory/AsyncFactory.java` |
| `AsyncFactory.recordOper` | 同上 |
| `AsyncManager` | `taskforge-framework/.../manager/AsyncManager.java` |
| `ISysOperLogService` + Impl | `taskforge-system/...`（insert/list/delete/clean） |
| `ISysLogininforService` + Impl | 同上 |
| Mapper XML | `taskforge-admin/src/main/resources/mapper/system/SysOperLogMapper.xml` 等 |
| 常量 | `Constants.LOGIN_SUCCESS` / `LOGIN_FAIL` |

### 缺口（本 Sprint 要补）

1. 登录时**没有调用** `recordLogininfor`
2. 业务写接口**几乎没挂** `@Log`
3. **没有** `/monitor/operlog`、`/monitor/logininfor` 的 Controller

---

## 1. 整体心智模型

```text
登录请求
  → SysLoginService.login
  → 成功/失败时 AsyncManager 丢一个 TimerTask
  → AsyncFactory 里组装 SysLogininfor → insert DB

业务写接口（增删改）
  → 方法上 @Log(title=..., businessType=...)
  → LogAspect 环绕通知
  → AsyncFactory.recordOper → insert sys_oper_log

后台查看
  → GET /monitor/operlog/list
  → GET /monitor/logininfor/list
```

对照习惯：左边 pmhub，右边 TaskForge；Controller 风格抄自己的 `SysRoleController` / `SysUserController`（`R<T>`，不是 pmhub 的 `AjaxResult`）。

---

## 2. B1 — 登录日志写入（先做）

### 改哪个文件

`taskforge-framework/.../web/service/SysLoginService.java`

### 参考文件

`E:/Code/02-Projects/pmhub-boot/pmhub-framework/.../web/service/SysLoginService.java`  
搜 `recordLogininfor`。

### 实现思路

在现有 `login` 方法里：

1. **认证成功后、返回 token 前**：异步记成功日志  
2. **`catch (BadCredentialsException)`**：先记失败日志，再 `throw`  
3. **其它 `catch (Exception)`**：也可记失败（`e.getMessage()`），再抛

伪代码（自己写成真代码）：

```java
// 成功路径末尾
AsyncManager.me().execute(
    AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, "登录成功")
);

// 密码错误
AsyncManager.me().execute(
    AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "用户密码错误")
);
throw new ServiceException("用户密码错误");
```

### 需要的 import

- `AsyncManager`
- `AsyncFactory`
- `Constants`（`LOGIN_SUCCESS` / `LOGIN_FAIL` 已在 common）

### 注意

- `recordLogininfor` 内部会读 `ServletUtils.getRequest()`，必须在**还在请求线程里**创建 TimerTask（`execute` 时已经把 UA/IP 采好了），不要放到奇怪的异步里再调 `recordLogininfor`。
- username 用 `loginBody.getUsername()`，失败时也有用户名可记。

### 验收 B1

1. 故意输错密码 → 查表 `sys_logininfor`，有一条失败（status 对应 FAIL）
2. 登录成功 → 有一条成功记录

---

## 3. B2 — 业务接口挂 `@Log`

### 做什么

在**写操作**方法上加注解，列表查询可以不加。

示例（用户模块）：

```java
@Log(title = "用户管理", businessType = BusinessType.INSERT)
@PostMapping
public R<Void> add(...) { ... }

@Log(title = "用户管理", businessType = BusinessType.UPDATE)
@PutMapping
public R<SysUser> update(...) { ... }

@Log(title = "用户管理", businessType = BusinessType.DELETE)
@DeleteMapping("/{userIds}")
public R<Void> remove(...) { ... }

@Log(title = "用户管理", businessType = BusinessType.UPDATE)
@PutMapping("/changeStatus")
...

@Log(title = "用户管理", businessType = BusinessType.UPDATE)
@PutMapping("/resetPwd")
...
```

### 建议挂载范围（本 Sprint）

| Controller | 挂哪些 |
|------------|--------|
| `SysUserController` | add / update / remove / changeStatus / resetPwd |
| `SysRoleController` | add / update / remove / changeStatus |
| 有余力再挂 | Menu / Dept / Post / Dict / Config 的写接口 |

### 参考

- 注解定义：`taskforge-common/.../annotation/Log.java`
- `BusinessType`：INSERT / UPDATE / DELETE / OTHER / CLEAN 等
- pmhub：任意 `SysUserController` 上的 `@Log`

### 原理（理解即可）

`LogAspect` 已存在：拦截带 `@Log` 的方法 → 组装 `SysOperLog` → `AsyncFactory.recordOper` → DB。  
你**只需加注解**，不用改 Aspect。

### 验收 B2

新增或修改一个用户 → 查 `sys_oper_log` 有对应 title / businessType 记录。

---

## 4. B3 — Monitor 查询 API（核心交付）

### 新建包

`taskforge-admin/src/main/java/com/zhq/taskforge/web/controller/monitor/`

### 4.1 权限常量

改 `PermissionConstants.java`，增加例如：

```text
monitor:operlog:list
monitor:operlog:query
monitor:operlog:remove

monitor:logininfor:list
monitor:logininfor:remove
```

（命名与 pmhub 一致，方便以后对前端菜单。）

若库里菜单权限没有这些串，非 admin 会 403；本地用 **userId=1 的 admin** 测即可（你们 PermissionService 对 admin 放行）。

### 4.2 `SysOperlogController`

路径：`@RequestMapping("/monitor/operlog")`

| 方法 | HTTP | 说明 |
|------|------|------|
| list | GET `/list` | 调 `selectOperLogList`，可先 `R.ok(list)`；有过滤条件用查询对象 `SysOperLog` 接参 |
| getInfo | GET `/{operId}` | `selectOperLogById`（可选） |
| remove | DELETE `/{operIds}` | `deleteOperLogByIds` |
| clean | DELETE `/clean` | `cleanOperLog`，建议加 `@Log(..., CLEAN)` |

参考：  
`pmhub-boot/.../controller/monitor/SysOperlogController.java`

风格对齐自己的模块：

```text
@PreAuthorize("hasAnyAuthority('" + PermissionConstants.OPERLOG_LIST + "')")
public R<List<SysOperLog>> list(SysOperLog operLog) {
    return R.ok(operLogService.selectOperLogList(operLog));
}
```

第一版**可以不做** Excel 导出。

### 4.3 `SysLogininforController`

路径：`@RequestMapping("/monitor/logininfor")`

| 方法 | HTTP | 说明 |
|------|------|------|
| list | GET `/list` | `selectLogininforList` |
| remove | DELETE `/{infoIds}` | `deleteLogininforByIds` |
| clean | DELETE `/clean` | `cleanLogininfor` |

参考：  
`pmhub-boot/.../controller/monitor/SysLogininforController.java`

### 4.4 Service 若不够用

接口里已有 list/delete/clean。若 `selectOperLogList` 过滤条件太少，可再加 wrapper 条件，**先能查出全部再优化过滤**。

分页：Service 当前是 `List`。两种做法选一种：

- A. 先返回全量 `R<List<...>>`（学习阶段够用）
- B. 自己在 Service 加 `Page`（学用户模块那套）

推荐先 A，跑通再改 B。

### 验收 B3

Knife4j：

1. `GET /monitor/logininfor/list` 能看到 B1 产生的登录记录  
2. `GET /monitor/operlog/list` 能看到 B2 产生的操作记录  
3. delete / clean 能清数据（小心别清生产；本地库随意）

---

## 5. 推荐动手顺序（每天一块）

| 顺序 | 任务 | 完成标志 |
|------|------|----------|
| Day1 | B1 登录日志 | 表里有成功/失败行 |
| Day2 | B2 用户 Controller 挂 `@Log` | oper_log 有用户操作 |
| Day3 | B3 两个 Monitor Controller + 权限常量 | Knife4j 列表可查 |
| 有余力 | 其它模块也挂 `@Log` | — |

每完成一块：`mvn -pl taskforge-admin -am -DskipTests compile`，再重启测接口。

---

## 6. 常见坑

| 坑 | 处理 |
|----|------|
| 加了 `@Log` 表没数据 | 看 Aspect 是否生效、异步线程是否报错；确认写方法真的执行成功 |
| 登录日志 NPE | `ServletUtils.getRequest()` 为空；确保在 Web 请求线程里调用 `recordLogininfor` |
| monitor 403 | admin 测；或权限串与菜单不一致 |
| `DELETE /clean` 和 `/{ids}` 冲突 | 把 `/clean` 写在更具体的映射上，或注意路径顺序（Spring 一般能区分字面量 `clean`） |
| 和 pmhub 返回体不一致 | 用你们的 `R<T>`，不要照抄 `AjaxResult` / `TableDataInfo` |

---

## 7. 本 Sprint 明确不做

- `getInfo` / 个人中心 / 头像  
- `SysNotice`  
- `@DataScope`  
- 在线用户 / 服务监控 / 缓存监控  
- `taskforge-project`  

B1–B3 验收通过后再开下一批。

---

## 8. 卡住时怎么问

贴：**改的文件路径 + 方法片段 + 报错/现象**（例如「登录成功了但 logininfor 没行」）。  
不要整文件无说明粘贴。
