# Sprint N：服务与缓存监控（自学版）

> **状态**：排队（可与 M 对调，偏运维演示时先做）  
> **估算**：2～3 人日  
> **目标**：对照 pmhub `ServerController` / `CacheController`，提供进程与 Redis 监控只读 API（+ 可选清缓存）。  
> **前置**：Redis 已用（Token / 验证码 / 限流）。  
> **不做**：Prometheus/Grafana、APM、前端大盘像素级抄写（本 Sprint 后端）。

## 文件对照

| 看 pmhub | TaskForge |
|----------|-----------|
| `pmhub-admin/.../monitor/ServerController.java` | **新建** `taskforge-admin/.../controller/monitor/ServerController.java` |
| `pmhub-framework/.../web/domain/Server.java` 及同目录 `Cpu.java`、`Mem.java`、`Jvm.java`、`Sys.java`、`SysFile.java` | **新建** `taskforge-framework/.../web/domain/server/` 下同样几个类（名字可保持） |
| `pmhub-admin/.../monitor/CacheController.java` | **新建** `taskforge-admin/.../controller/monitor/CacheController.java` |
| `pmhub-framework/.../config/DruidConfig.java` | TaskForge 若是 Hikari：**不新建** Druid。只在 `ServerController` 或新建 `taskforge-admin/.../controller/monitor/DataSourceController.java` 读 Hikari 指标 |

**不新建模块。**

---

## 0. 过程

### N1 服务器

1. 父 POM 加入 Oshi（pmhub `Server.java` 用的那套）。Boot 3 下先编译一次，版本不兼容就改用 `ManagementFactory` 只做 JVM 堆和线程，CPU 用 Oshi。
2. 按 pmhub 抄 `Server.copyTo()` 的调用方式：Controller 里 `new Server().copyTo()` 再放进 `R.ok`。
3. `GET /monitor/server`。权限 `monitor:server:list`。
4. 返回里至少有：操作系统名、JVM 已用内存、剩余内存。磁盘列表允许为空但不要 NPE。

### N2 缓存

1. 注入已有的 `RedisTemplate` 或 `StringRedisTemplate`。
2. `GET /monitor/cache`：执行 `info`，取出 used_memory_human、connected_clients、uptime。
3. 再 `keys` 三个前缀的数量：`login_tokens:`、验证码前缀（看 `CacheConstants`）、限流前缀。不要 `keys *`。
4. 权限 `monitor:cache:list`。

### N3 清缓存（可选）

`DELETE /monitor/cache/clear?prefix=login_tokens:` 只允许白名单前缀。默认不做。要做就单独权限 `monitor:cache:remove`。

### N4 连接池

1. 确认项目用的是 Hikari 还是 Druid（看 datasource 依赖）。
2. Hikari：从 `DataSource` 转成 `HikariDataSource`，返回 active、idle、total、等待线程数。
3. 不要为了对齐 pmhub 再引入 Druid。
4. `GET /monitor/server/pool`，和 N1 同一权限即可。

---

## 1. 落位

```text
taskforge-framework 或 system：组装 Server 视图对象
taskforge-admin：
  monitor/ServerController.java
  monitor/CacheController.java
```

依赖：若用 Oshi，进父 POM 管理版本。注意 Boot 3 / 权限 `@PreAuthorize`。

---

## 2. 验收

- [ ] `/monitor/server` 返回非空数值字段  
- [ ] `/monitor/cache` 能看到 Redis 连通与若干 key 模式  
- [ ] 无权限用户 403  

---

## 3. 再往后

后续总表见 [01-backend-roadmap.md](./01-backend-roadmap.md)。下一号是 [O 代码生成](./15-sprint-o-generator.md)，业务上 P 更优先。  
