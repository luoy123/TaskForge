# Sprint N：服务与缓存监控（自学版）

> **状态**：排队（可与 M 对调，偏运维演示时先做）  
> **目标**：对照 pmhub `ServerController` / `CacheController`，提供进程与 Redis 监控只读 API（+ 可选清缓存）。  
> **前置**：Redis 已用（Token / 验证码 / 限流）。  
> **不做**：Prometheus/Grafana、APM、前端大盘像素级抄写（本 Sprint 后端）。

---

## 0. 范围

```text
N1  GET /monitor/server  — CPU / JVM / 堆 / 磁盘 / 系统信息（Oshi 或 JDK MXBean）
N2  GET /monitor/cache   — Redis info 摘要、自定义 key 统计（login_tokens、captcha 等）
N3  （可选）DELETE 按名前缀清理缓存；慎做，需权限字
```

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

[Sprint O 代码生成](./15-sprint-o-generator.md)；OA 后置。  
