# 前端 Sprint 06：验证码登录 + 在线用户（对齐后端 J）

> **编号**：`plan/frontend/06`  
> **状态**：✅ **已联调**（登录验证码 + 在线用户 + F06-5 浏览器验收 2026-09-25）  
> **目标**：对接后端 Sprint J——登录页图形验证码（可开关）、监控「在线用户」列表与强退；顺带确认限流/防重的错误文案能在 UI 上看到。  
> **前置**：前端 01–05 壳可用；后端 [10-sprint-j-system-guard.md](../backend/10-sprint-j-system-guard.md) J1～J4 已验收。  
> **不做**：Server/Cache 监控大盘、WebSocket 在线推送、改后端限流注解拼写、I5b 定时任务页。

---

## 0. 与后端 / 前端进度对齐

| 层 | 现状 |
|----|------|
| 后端 A–I | ✅（含 H 主路径、I1–I5a） |
| 后端 **J** | ✅ J1 验证码 · J2 在线用户 · J3 限流 · J4 防重 |
| 前端 01–05 | ✅ 壳 |
| 前端 **06** | ✅ API + 登录验证码 + 在线用户页（`views/monitor/online`） |

```text
✅ F06-1  api：captchaImage + monitor/online
✅ F06-2  登录页：按开关显示验证码图 / code / uuid，提交带上
✅ F06-3  在线用户页：列表（ip / 用户名筛选）+ 强退
✅ F06-4  菜单：component=`monitor/online/index` 与 views 对齐（无需额外 alias）
✅ F06-5  自测：开关开/关登录；强退后会话消失；防重文案（顺序连点）
```

落地文件：

```text
api/auth.js                 # getCaptchaImage
api/monitor/online.js       # listOnline / forceLogout
stores/user.js              # login(payload) 支持 code/uuid
views/login/index.vue       # 验证码行
views/monitor/online/index.vue
```

建议自测见 §3 F06-5 / §4。

---

## 1. 后端接口速查

### J1 验证码

| 动作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 取验证码 | GET | `/captchaImage` | 无需 Token |

`data` 大致：

| 字段 | 含义 |
|------|------|
| `captchaEnabled` | `true`/`false`（来自 `sys.account.captchaEnabled`） |
| `uuid` | 开启时有；登录要原样带回 |
| `img` | Base64 图（无 `data:image/...` 前缀时前端自己拼） |

登录 `POST /login` body（开启时）：

```json
{ "username": "admin", "password": "...", "code": "abcd", "uuid": "..." }
```

关闭时可不传 `code`/`uuid`（与现网一致）。

### J2 在线用户

| 动作 | 方法 | 路径 | 权限字 |
|------|------|------|--------|
| 列表 | GET | `/monitor/online/list?ipaddr=&userName=` | `monitor:online:list` |
| 强退 | DELETE | `/monitor/online/{tokenId}` | `monitor:online:forceLogout` |

`tokenId` = `LoginUser.token`（Redis key 后缀），**不是**整段 JWT。  
字段以 `SysUserOnline` 为准（如 tokenId、userName、ipaddr、loginTime、browser 等）。

### J3 / J4（本 Sprint 只验收文案）

- 限流：如登录过快 → `R` 失败「访问过于频繁…」
- 防重：如连点项目新增 → 「请勿重复提交…」
- 前端：`request` 拦截器已弹 `message` 即可，**不必**单独做 UI。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/auth.js                 # + getCaptchaImage（或 api/common.js）
  api/monitor/online.js       # listOnline / forceLogout
  views/login/index.vue       # 验证码行 + 刷新图
  stores/user.js              # login() 把 code/uuid 传给 API
  views/monitor/online/index.vue
  utils/permission.js         # COMPONENT_ALIAS：monitor/online/index 等
```

若库里菜单 `component` 已是 `monitor/online/index`，与 `views` 路径对齐即可；否则加别名。

---

## 3. 分阶段要点

### F06-1 API

```js
// GET /captchaImage → R<{ captchaEnabled, uuid?, img? }>
export function getCaptchaImage() {
  return request.get('/captchaImage')
}

// GET /monitor/online/list
export function listOnline(params) {
  return request.get('/monitor/online/list', { params })
}

// DELETE /monitor/online/{tokenId}
export function forceLogout(tokenId) {
  return request.delete(`/monitor/online/${tokenId}`)
}
```

### F06-2 登录页

1. `onMounted` / 打开页 → `getCaptchaImage`
2. `captchaEnabled === true` → 展示图 + 输入框；点图刷新
3. `img` 展示：`src="data:image/png;base64," + img`（若后端已带前缀则勿重复）
4. 提交：`login({ username, password, code, uuid })`；失败后刷新验证码
5. 关闭开关时隐藏验证码区，行为与现在一致

### F06-3 / F06-4 在线用户

- 表格：用户名、IP、登录时间、浏览器（有则显示）、操作「强退」
- 搜索：`ipaddr`、`userName`
- 强退二次确认 → `forceLogout(row.tokenId)` → 刷新列表
- `v-hasPermi`：`['monitor:online:forceLogout']`

### F06-5 自测

| 步骤 | 期望 |
|------|------|
| `sys.account.captchaEnabled=true` 后刷新登录 | 有图；错码登不上；对码可进 |
| 开关 `false` | 无验证码区，可直接登录 |
| 两处登录（或两浏览器） | 在线列表 ≥ 2 条（未做互踢时） |
| 强退另一会话 | 该会话再请求 → 401 / 回登录 |
| 连点「新增项目」两次 | 第二次失败文案含重复提交 |
| 脚本狂打登录（可选） | 限流文案 |

---

## 4. 验收标准

- [x] Network：`/dev-api/captchaImage`、`/login`（含 code/uuid 当开启时）
- [x] 验证码开关开/关 UI 与登录行为正确
- [x] `/monitor/online/list` 有数据；强退后目标会话从列表消失（DELETE 200）
- [x] 侧栏「在线用户」进真实页，不进 `building`
- [x] 防重失败文案「请勿重复提交项目」（顺序二次请求；极短并发双击可能竞态双成功）

```bash
# 后端
mvn -pl taskforge-admin -am -DskipTests compile
# 前端
cd taskforge-ui && npm run dev
```

---

## 5. 再往后（07 候选）

| 候选 | 说明 |
|------|------|
| 系统缺页 | 岗位 / 字典 / 参数 / 公告 / 登录·操作日志（后端 C/B 早已有） |
| 工作流加深 | 05 联调收尾；category/form/model 仍可 building |
| I5b | 定时任务管理页（等后端补齐 `/monitor/job` CRUD） |
| 演示打磨 | 首页统计刷新、空状态文案 |

路线总览：[plan/README.md](../README.md)。
