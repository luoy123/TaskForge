# 前端 Sprint 01：骨架 + 登录联调 + 系统管理页（自学版）

> **编号**：`plan/frontend/` 内自增（本篇为 `01`）；内容对应原路线图「Sprint H」。  
> **目标**：在仓库根目录落地 `taskforge-ui`（Vue 3 + Vite + JavaScript + Element Plus），打通登录 / Token / getInfo，再逐步接系统管理与项目任务页面。  
> **前置**：后端 Sprint A–G 主路径可用（登录、菜单路由、项目/任务/文件）。  
> **不做**：Flowable 工作流 UI、完整动态路由生成器抄 pmhub、OSS、移动端。  
> **协作**：脚手架、页面壳、提示注释由助手写；**带 Token / 存 Token / 路由鉴权等关键几行由你填写**（代码里搜 `【学员填写】`）。

---

## 0. 进度与本 Sprint 范围

| 阶段 | 状态 |
|------|------|
| 后端 Sprint A–G | 主路径完成 |
| **前端 Sprint 01** | **已完成**（登录 + 系统管理 CRUD） |
| 再往后 | 前端 `02-`：动态路由 / 项目任务页等 |

```text
F01-0  仓库布局：根目录 taskforge-ui/（不进父 pom <modules>）
F01-1  工程骨架：Vite + Router + Pinia + Element Plus + axios 代理
F01-2  登录联调：/login → Token → /getInfo → 首页
F01-3  布局壳 + 静态菜单（系统管理入口）
F01-4  系统管理页：用户 / 角色 / 菜单 / 部门（CRUD 联调）
F01-5  （可选）项目列表 + 任务列表最小闭环
```

对照习惯：左边 pmhub-ui（Vue2 + Element UI + Vue CLI），右边 TaskForge（Vue3 + Element Plus + Vite + JS）。

---

## 0.1 仓库落位（已定）

```text
TaskForge/
  taskforge-admin/     # Java 启动入口 :1234
  taskforge-common/
  ...
  taskforge-ui/        # 前端 :5173  ← 本 Sprint
  plan/
    README.md
    backend/           # 后端 Sprint（本文件所在侧）
    frontend/          # 前端 Sprint（taskforge-ui）
  pom.xml              # 只管 Java；不要把 ui 加进 <modules>
```

| 做法 | 评价 |
|------|------|
| 根目录 `taskforge-ui/` | **首选**：同仓联调，对照 pmhub |
| 另开远程仓库 | 学习项目易散 |
| 塞进 `static/` | 不适合现代 SPA |

`.gitignore` 已覆盖 `node_modules/`、`dist/`。

---

## 1. 技术栈心智

```text
浏览器 :5173
  └── Vite dev server
        ├── 静态 Vue SPA
        └── proxy /dev-api → http://localhost:1234
              └── Spring Boot (taskforge-admin)
                    ├── POST /login
                    ├── GET  /getInfo
                    ├── GET  /getRouters
                    └── /system/** /project/**
```

- 响应体对齐后端 `R<T>`：`{ code, message, data }`，成功 `code === 200`。
- Token：`localStorage['Admin-Token']`，请求头 `Authorization: Bearer <token>`（对齐 JwtFilter）。
- 状态：Pinia `user` store；路由守卫拦未登录。

---

## 2. 已落地（F01-0～F01-1）

当前骨架目录（均为 **JavaScript**）：

```text
taskforge-ui/
  .env.development      # VITE_APP_BASE_API=/dev-api
  .env.production
  vite.config.js        # @ 别名 + /dev-api 代理 + Element 按需
  src/
    api/auth.js
    layout/index.vue
    router/index.js
    stores/user.js
    utils/request.js    # axios + R 拦截
    views/login/
    views/home/
    styles/
```

---

## 3. F01-2：登录联调（当前焦点 · 自学步骤）

> **目标**：不写新业务页，先证明「浏览器 → Vite 代理 → Java → Token → 首页」整条链通。  
> **账号**：常见为 `admin` / 你库里的密码（若依种子库多为 `admin123`；以你本机能登录为准）。

### 进度勾选

- [x] **步骤 1**：后端 + 前端都起来，能打开登录页
- [x] **步骤 2**：用 curl/接口确认 `POST /login` 返回 `data.token`（本机密码：`admin` / `123456`）
- [x] **步骤 3**：页面登录成功 → localStorage 有 `Admin-Token` → 进 `/home`
- [x] **步骤 4**：Layout 拉到 `getInfo`，顶栏/首页有用户信息（顶栏显示 `admin`）
- [x] **步骤 5**：清 Token 后再访问 `/home` → 被踢回 `/login?redirect=/home`
- [x] **步骤 6**（可选）：看 Network 确认请求是 `/dev-api/...` 且业务 `code===200`（代理与直连均已验证）

> **F01-2 联调通过**（2026-09-09，Playwright + curl）。学员填写 ①②③ 正确。

### 步骤 1 — 起两个进程

```bash
# 终端 A：后端（端口 1234）
# IDE 运行 TaskForgeApplication，或：
# mvn -pl taskforge-admin -am spring-boot:run

# 终端 B：前端
cd taskforge-ui
npm install          # 若已装过可跳过
npm run dev          # http://localhost:5173
```

浏览器打开：`http://localhost:5173/login`  
**过关**：能看到登录卡片（用户名默认 `admin`）。

### 步骤 2 — 先撇开前端，确认后端登录 OK

```bash
curl -s -X POST http://localhost:1234/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"你的密码"}'
```

期望大致：

```json
{ "code": 200, "message": "...", "data": { "token": "...." } }
```

**过关**：有 `data.token`。若这里失败，先修后端/账号，别怪前端。

### 步骤 3 — 页面登录（你来补 3 处关键代码）

调用链：

```text
views/login/index.vue  onSubmit          ← 壳子已写好
  → stores/user.js     login()           ← 【学员填写 ②】存 token
    → api/auth.js      POST /login       ← 已写好
      → utils/request.js 请求拦截         ← 【学员填写 ①】带 Authorization
router/index.js        beforeEach        ← 【学员填写 ③】无 token 踢登录
```

在仓库里搜索：`【学员填写】`，按注释里的提示把空缺补上（每处大约 2～3 行）。

补完后：填密码 → 点登录。

**过关**：

1. 跳到 `/home`（或带 redirect 的目标页）
2. DevTools → Application → Local Storage → 有 `Admin-Token`
3. Network 里有 `login`，URL 形如 `/dev-api/login`，响应 `code: 200`
4. 再进其它接口时 Request Headers 里有 `Authorization: Bearer ...`（填写 ① 才有）

### 步骤 4 — getInfo

进 layout 后会 `fetchUserInfo()` → `GET /getInfo`。

**过关**：顶栏不再是「未拉取用户信息」；首页角色/权限数有值（超管至少有角色或权限之一）。

### 步骤 5 — 路由守卫

1. DevTools → Application → 删掉 `Admin-Token`
2. 地址栏进 `http://localhost:5173/home`

**过关**：被重定向到 `/login?redirect=...`。

### 步骤 6 — 自检表（联调通过再勾）

| 检查项 | 期望 |
|--------|------|
| 请求前缀 | `/dev-api`（不是直接打 1234，除非你改了配置） |
| 登录后请求头 | 后续接口带 `Authorization: Bearer ...` |
| 业务码 | `R.code === 200`（不是只看 HTTP 200） |
| 401 | 会清 Token 并回登录（`request.js`） |

**F01-2 全部勾完 → 再开 F01-3 静态菜单。**

---

## 4. F01-3：布局与静态菜单（当前焦点）

> **目标**：侧栏能点进用户/角色/菜单/部门占位页（先不接 CRUD）。  
> **协作**：占位页 + 侧栏「系统管理」分组壳由助手写；**子路由 + 菜单项**由你填（搜 `【学员填写 A/B】`）。

### 进度勾选

- [x] **A**：在 `router/index.js` 的 `children` 里补 4 条系统路由
- [x] **B**：在 `layout/index.vue` 的「系统管理」下补 4 个 `el-menu-item`
- [x] **自测**：登录后点菜单，中间区域标题变成对应用户/角色/菜单/部门；地址栏 path 正确

### 助手已准备

```text
src/views/system/role|menu|dept/index.vue  # 仍为占位
src/views/system/user/index.vue            # F01-4 已换成 CRUD 壳
layout / router：系统管理静态菜单已接好
```

| 前端 path | 组件 |
|-----------|------|
| `/system/user` | `@/views/system/user/index.vue` |
| `/system/role` | `@/views/system/role/index.vue` |
| `/system/menu` | `@/views/system/menu/index.vue` |
| `/system/dept` | `@/views/system/dept/index.vue` |

**要点**：`el-menu` 的 `index` 必须和路由最终 path 一致。

**F01-3 完成 → 进入 F01-4。**

---

## 5. F01-4：用户管理 CRUD（当前焦点）

> **目标**：用户列表 + 新增/编辑/删除跑通（角色分配、部门树可后置）。  
> **协作**：`api/system/user.js`、表格/对话框壳由助手写；**getList / submitForm / handleDelete** 由你填（搜 `【学员填写 C/D/E】`）。

### 后端接口（对照）

| 动作 | 方法 | 路径 |
|------|------|------|
| 列表 | GET | `/system/user/list?pageNum&pageSize&userName` → `R<Page>`，用 `data.records` / `data.total` |
| 新增 | POST | `/system/user` body: SysUser |
| 编辑 | PUT | `/system/user` |
| 删除 | DELETE | `/system/user/{userIds}` |

状态：`0` 正常，`1` 停用。

### 进度勾选

- [x] **C**：实现 `getList`（listUser → tableData / total）
- [x] **D**：实现 `submitForm`（有 userId 则 update，否则 add）
- [x] **E**：实现 `handleDelete`
- [x] **自测**：列表/搜索/新增/编辑/删除均已验证（2026-09-09）

**F01-4 用户页已完成。**

---

## 5.1 角色 / 部门 / 菜单 CRUD（当前焦点）

> **协作**：`api/system/*.js`、表格/对话框壳已写好；各页 **getList / submitForm / handleDelete** 由你填（搜 `【学员填写 R/D/M】`）。

| 页面 | API | 列表特点 | 你要填的标记 |
|------|-----|----------|--------------|
| 角色 | `api/system/role.js` | 分页，同用户 | R-C / R-D / R-E |
| 部门 | `api/system/dept.js` | **树表**，后端已带 `children` | D-C / D-D / D-E |
| 菜单 | `api/system/menu.js` | 扁平列表 → `buildTree(..., 'menuId', 'parentId')` | M-C / M-D / M-E |

### 进度勾选

- [x] **R-C/D/E**：`views/system/role/index.vue`
- [x] **D-C/D/E**：`views/system/dept/index.vue`
- [x] **M-C/D/E**：`views/system/menu/index.vue`
- [x] **自测**：三页均能列表 + 新增 + 编辑 + 删除（勿删超管角色 id=1）；部门新增曾因后端状态判断反了失败，已改为 `DEPT_DISABLE` 后复测通过（2026-09-10）

### 参考写法（与用户页同套路）

**角色 R-C**（分页）：

```js
async function getList() {
  loading.value = true
  try {
    const { data: res } = await listRole(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}
```

**部门 D-C**（树，无 total）：

```js
async function getList() {
  loading.value = true
  try {
    const { data: res } = await listDept(query)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}
```

**菜单 M-C**（扁平转树）：

```js
async function getList() {
  loading.value = true
  try {
    const { data: res } = await listMenu(query)
    tableData.value = buildTree(res.data || [], 'menuId', 'parentId')
  } finally {
    loading.value = false
  }
}
```

**D/E 提交与删除**：照抄用户页 `submitForm` / `handleDelete`，换对应 API 即可；角色删除跳过 `roleId === 1`。

---

## 6. F01-5（可选）：项目 / 任务最小页

- 项目列表 + 新建/编辑对话框
- 进入项目后任务列表（或独立任务页）
- 文件上传可后置，对照后端 Sprint G 接口

---

## 7. 日常命令（记住这几条就够）

```bash
cd taskforge-ui
npm run dev      # 开发
npm run build    # 产出 dist/（勿提交）
npm run preview  # 预览构建
```

父工程继续：

```bash
# 仓库根
mvn -pl taskforge-admin -am spring-boot:run
# 或 IDE 跑 TaskForgeApplication
```

---

## 8. 验收标准

- [x] `taskforge-ui` 在根目录，且 **未** 出现在父 `pom.xml` `<modules>`
- [x] `npm run dev` 可打开登录页；代理到 `1234` 成功
- [x] 登录 → Token → getInfo → 首页展示用户信息
- [x] 未登录访问业务路由被重定向
- [x] （F01-4）至少一个系统管理页 CRUD 联调通过
- [x] `node_modules/`、`dist/` 被 git 忽略

---

## 9. 学习顺序建议

1. 先读 `vite.config.ts` 代理、`utils/request.ts`、`stores/user.ts`——理解「请求怎么到后端」。
2. 再改登录页文案 / 校验，确认你能改动 UI。
3. 照着 `api/auth.js` 抄一份 `api/system/user.js`，做用户列表页。
4. 有余力再研究 `getRouters` → 动态路由（对照 pmhub `permission.js` 心智即可，API 用 Vue Router 4）。
