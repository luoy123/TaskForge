# Sprint J：系统护栏（验证码 / 在线用户 / 限流 / 防重提交）

> **状态**：排队（可与 I 并行插队；任一小块可单独拆）  
> **目标**：对照 pmhub-framework / monitor，补齐登录与写接口护栏，方便后续压测与演示。  
> **前置**：登录 / Token / Redis 已可用（Sprint B 前后）。  
> **不做**：动态数据源、WebSocket 通知中心、完整服务器监控大盘（可只做在线用户）。

---

## 0. 范围（可拆着做）

```text
J1  验证码：CaptchaController + Redis 存答 + 登录校验开关
J2  在线用户：扫 Redis login_tokens → 列表 / 强退
J3  @RateLimiter + Aspect（按 IP / 用户 / 全局限流）
J4  @RepeatSubmit + 拦截器（短时重复提交）
```

对照：[00-pmhub-reference-map.md](./00-pmhub-reference-map.md) §3、§6。

---

## 1. J1 — 验证码


| 点   | 说明                                          |
| --- | ------------------------------------------- |
| 生成  | 图形验证码（Kaptcha 或简单算术），uuid → Redis           |
| 登录  | `LoginBody` 带 code + uuid；开关可用 `sys_config` |
| 响应  | 仍 `R<T>`，不要混 AjaxResult                     |


### 验收

- `/captchaImage`（路径自定）返回图 + uuid  
- 错误验证码登录失败；正确可登录  
- 开关关闭时可不传验证码

---

## 2. J2 — 在线用户


| API       | 能力                                          |
| --------- | ------------------------------------------- |
| GET 列表    | 从 Redis `login_tokens:*` 组装 ip / 浏览器 / 登录时间 |
| DELETE 强退 | 删对应 token                                   |


### 验收

- 两处登录可见两条（若未做单端互踢）  
- 强退后该 Token 再调 `/getInfo` 401

---

## 3. J3 / J4 — 限流与防重


| 注解              | 行为                   |
| --------------- | -------------------- |
| `@RateLimiter`  | 超限抛异常 → 全局异常成 `R` 失败 |
| `@RepeatSubmit` | 同用户同 URL 短窗口重复 → 拒绝  |


先挂在登录、用户新增、项目新增等写接口上试点。

### 验收

- 压测或脚本触发限流返回明确文案  
- 连点提交第二次被拦

---

## 4. 明确后置

- Server / Cache 监控页后端  
- OA 企微  
- generator 代码生成

---

## 5. 验收总清单

- J1～J4 按需完成；每完成一块更新本文件勾选  
- `mvn -pl taskforge-admin -am -DskipTests compile` 通过

