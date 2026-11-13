# 前端 Sprint 15：服务与缓存监控（对齐后端 N）

> **编号**：`plan/frontend/15`  
> **状态**：✅ 已完成（2026-11-13，联调通过；另加连接池卡片、全局 404 兜底页）  
> **目标**：在「系统监控」下增加服务器与 Redis 只读页，方便看进程和缓存是否活着。  
> **前置**：在线用户、定时任务页已有，本 Sprint 不改它们。  
> **不做**：Prometheus、曲线图、清缓存按钮（除非后端做了 N3 且你明确要）。

---

## 0. 范围

```text
F15-1  api：server / cache
F15-2  服务器页：系统名、JVM 已用/剩余、磁盘列表
F15-3  缓存页：内存、连接数、运行时长、若干 key 前缀数量
F15-4  菜单映射，不进 building
```

**协作**：卡片壳由助手写；把接口字段填进卡片搜 `【学员填写】`。字段名以 `R.data` 实际结构为准（Oshi 的 cpu/mem/jvm/sys/sysFiles）。

---

## 1. 后端接口速查

| 动作 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 服务器 | GET | `/monitor/server` | `monitor:server:list` |
| 缓存 | GET | `/monitor/cache` | `monitor:cache:list` |

缓存至少包含：`used_memory_human`、`connected_clients`、`uptime`，以及 `login_tokens:`、验证码前缀、限流前缀的数量。不要在前端发 `keys *`。

可选 N3：`DELETE /monitor/cache/clear?prefix=` 只允许白名单。默认**不做按钮**。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/monitor/server.js
  api/monitor/cache.js
  views/monitor/server/index.vue
  views/monitor/cache/index.vue
```

`el-descriptions` 或几张 `el-card` 即可，磁盘用表格。右上角「刷新」。

---

## 3. 分阶段要点

- 数字为空时显示「—」，磁盘空数组显示空表，不要报错。
- 无权限：按钮不出现或接口 403，页面不要白屏（拦截器已有 Message）。
- 缓存页文案写明：这是 Redis 概况，不是「在线用户」列表。

---

## 4. 验收标准

- [x] `/monitor/server` 能看到系统名和 JVM 内存
- [x] `/monitor/cache` 能看到内存、连接数和至少一类 key 数量
- [x] 侧栏点开是真实页
- [x] 无权限用户看不到数据或收到 403（`test` 用户侧栏无监控菜单；直接输入地址显示 404 兜底页，不白屏）

实际落地补充：
- 服务器页多一张「数据库连接池（Hikari）」卡片，调后端 N4 `GET /monitor/server/pool`。
- `router/index.js` 在 RootLayout 下加了不带 name 的 `:pathMatch(.*)*` → `views/error/404.vue`。不能带 name，否则 `beforeEach` 里 `next({ ...to })` 会停在 404。
- 菜单 114「缓存列表」（`monitor/cache/list`）本 Sprint 未做，仍落到建设中页。

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

代码生成在 [16](./16-sprint-generator.md)，不要塞进监控菜单。
