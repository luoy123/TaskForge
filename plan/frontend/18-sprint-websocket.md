# 前端 Sprint 18：WebSocket 推送（对齐后端 V）

> **编号**：`plan/frontend/18`  
> **状态**：排队（等后端 [V](../backend/22-sprint-v-websocket.md)；有 [17](./17-sprint-message.md) 时体验完整）  
> **目标**：登录后建立 WebSocket，收到站内消息时弹出通知并更新未读数。  
> **不做**：多实例集群、自己把消息存 localStorage 当收件箱（收件箱以 U 的接口为准）。

---

## 0. 范围

```text
F18-1  layout 在已登录时连接，退出时关闭
F18-2  收到 JSON 后 ElNotification，并刷新 17 的未读列表
F18-3  断线有限次数重连；无 token 不连
F18-4  若无 U：管理员「推测试消息」能在本页收到
```

**协作**：连接工具函数由助手写；onmessage 里怎么更新铃铛搜 `【学员填写】`。

---

## 1. 后端约定

| 点 | 说明 |
|----|------|
| 地址 | `/websocket/message?token=<JWT>`（开发环境走 Vite 代理时要让 ws 也能转到 1234，见下） |
| 鉴权 | 无 token 或 token 无效，握手失败 |
| 推送体 | JSON：`title`、`content`、`bizType`、`bizId` |
| 同用户 | 新连接踢掉旧连接 |
| 在线数 | `GET /oa/message/onlineCount` 是 **WebSocket 连接数**，不是 Redis 在线用户。可选在缓存/监控页加一行，必须写清这句话 |

Vite：在 `vite.config.js` 的 proxy 里为 websocket 升级放行（`ws: true`），否则本地收不到。

没有 U 时：`POST /oa/message/pushTest` 仅管理员，推给自己。

---

## 2. 前端落位

```text
taskforge-ui/src/
  utils/websocket.js         # connect / close / 重连
  layout/index.vue           # onMounted 连接，onLogout 关闭
  vite.config.js             # proxy ws
```

URL 用 `location.host` + 当前 token：`ws://` 或页面是 https 时 `wss://`。

---

## 3. 分阶段要点

- 重连：例如最多 5 次，间隔递增；组件卸载必须 `close`，避免开发时热更新堆连接。
- 通知点击：有 `bizId` 时走 17 的同一套跳转。
- 推送到达时若 17 的列表已打开，重新 `getMy()`。
- 后端没人在线时发送不会 500，前端不用处理这个失败。

---

## 4. 验收标准

- [ ] 带 token 能连上；清掉 token 再连被拒绝
- [ ] 给当前用户发一条，只有这个浏览器弹出（另一用户不弹）
- [ ] 退出登录后连接关闭，不再收
- [ ] 断网再恢复，有限次重连，不无限刷控制台
- [ ] （有 U 时）指派任务后不用手动刷新也能看到通知

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

异步入队后，通知可能晚几秒才到，仍应能弹出。失败重放页在 [19](./19-sprint-async-admin.md)，普通用户不可见。
