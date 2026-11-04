# Sprint V：WebSocket 在线推送

> **状态**：排队  
> **估算**：2～3 人日  
> **目标**：对照 pmhub `WebSocketServer`，登录后可收站内实时消息（待办、公告、U 的站内信）。  
> **前置**：U1 站内消息更好接；没有 U 时也可先推「在线测试消息」。  
> **不做**：集群广播（多实例要 Redis pub/sub，本 Sprint 单机）。

## 文件对照

| 看 pmhub | TaskForge 新建 |
|----------|----------------|
| `pmhub-framework/.../websocket/WebSocketConfig.java` | `taskforge-framework/.../websocket/WebSocketConfig.java` |
| `pmhub-framework/.../websocket/WebSocketServer.java` | `taskforge-framework/.../websocket/WebSocketServer.java` |
| `pmhub-framework/.../websocket/WebSocketUsers.java` | `taskforge-framework/.../websocket/WebSocketUsers.java` |
| 同目录 `SemaphoreUtils.java`（有则看） | 连接数限制需要再新建；单机可先不建 |

**不新建模块。** 推送入口 **改** U 里的 `MessageSender` 实现，或新建 `taskforge-oa/.../service/WebSocketMessageSender.java`。安全配置 **改** `taskforge-framework` 里放行 WebSocket 路径的那个 Security 配置类（现有 JWT 过滤器旁边）。

---

## 0. 过程

1. 抄 pmhub `WebSocketConfig` 注册端点 `/websocket/message`。在 Security 里把这个路径改为握手时鉴权，而不是完全匿名。
2. 握手：从 query `token` 取出 JWT，用现有 Token 服务换成 userId。失败则拒绝连接。
3. `WebSocketUsers` 用 `ConcurrentHashMap<userId, Session>`。同一用户后连上的覆盖旧 Session，旧的关掉。
4. `send(userId, json)`：没有 Session 就 return，不抛异常。
5. U 的 `DbMessageSender.send` 在 insert 成功后调用上面的 send。JSON 含 title、content、bizType、bizId。U 还没做时，先做 `POST /oa/message/pushTest` 给自己推一条，仅管理员。
6. `GET /oa/message/onlineCount` 返回 map 的 size。这是连接数，不是 J2 的 Redis 在线用户，接口注释写清楚。

---

## 1. 验收

- [ ] 带 Token 连接成功，无 Token 拒绝  
- [ ] 给指定用户发一条，仅该连接收到  
- [ ] 断开后发送不抛到全局 500（记日志即可）  
