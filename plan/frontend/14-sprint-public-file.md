# 前端 Sprint 14：公共网盘（对齐后端 T）

> **编号**：`plan/frontend/14`  
> **状态**：排队（等后端 [T](../backend/20-sprint-t-public-file.md)）  
> **目标**：登录后管理自己的公共文件（文件夹、上传、下载），并能生成到期即失效的分享链接；未登录凭链接下载。  
> **前置**：前端已有 blob 下载写法（任务导出 / 附件下载）。  
> **不做**：OSS、在线预览转码、外链账号体系。

---

## 0. 范围

```text
F14-1  网盘：按父目录列出、新建文件夹、上传、重命名、删除、下载
F14-2  分享：创建（默认 7 天，可改到期时间）、我的链接、停用
F14-3  公开页：/s?token= 或 /share，不带登录态下载
F14-4  别人的文件不能下载；停用、过期、文件已删都要有提示
```

建议：**1 → 2 → 3 → 4**。

**协作**：页面壳由助手写；上传 FormData、公开下载不带 Token 搜 `【学员填写】`。

---

## 1. 后端接口速查

| 前缀 | 能力 |
|------|------|
| `/public/file` | list（parentId）/ upload / mkdir / rename / delete / download |
| `/public/link` | create / list / disable |
| `/public/link/open?token=` | 匿名下载。拒绝：token 不存在、已停用、已过期、文件已删 |

删除文件夹：下面还有子项则拒绝。用户 B 下载用户 A 的文件应失败；同一文件的分享链接未登录可以下。

访问计数（T3）前端不做展示，除非列表 VO 里已经带了次数。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/public/file.js
  api/public/link.js         # open 不要走会强制跳登录的业务码处理时要单独看 401
  views/public/file/index.vue
  views/public/share/index.vue   # meta.public = true，常量路由，不进 layout 侧栏也行
  router/index.js            # 公开路由
```

面包屑：根 → 当前文件夹，点上级改 `parentId` 再 list。

---

## 3. 分阶段要点

### F14-1

- 表格：名称、类型（文件/文件夹）、大小、时间。
- 文件夹行点击进入；文件行下载。
- 上传用 `el-upload`，字段名跟后端 `MultipartFile` 参数名一致（写前看接口）。
- 删除二次确认。

### F14-2

- 创建后把完整打开地址复制到剪贴板（前端拼 `origin + /share?token=`）。
- 停用后该链接立即不可用。

### F14-3

- 公开页只有一个「下载」按钮。
- `request` 默认 401 会踢去登录：公开下载要单独 axios，或给该请求加标记跳过 401 跳转，失败时在页面上显示「链接无效」。

---

## 4. 验收标准

- [ ] 上传后列表可见、登录状态可下载
- [ ] 非空文件夹删不掉；文件删除后磁盘与列表都没了
- [ ] 分享链接未登录能下
- [ ] 过期、停用、文件删除后打开有明确失败，不下载到错误页 HTML
- [ ] 用户 B 不能直接下载用户 A 的文件

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

不把网盘并进项目附件面板。
