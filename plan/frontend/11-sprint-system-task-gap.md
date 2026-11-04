# 前端 Sprint 11：系统与任务边角（对齐后端 Q）

> **编号**：`plan/frontend/11`  
> **状态**：排队（等后端 [Q](../backend/17-sprint-q-system-task-gap.md)）  
> **目标**：补用户导入导出与角色分配、头像、注册、任务类型、任务认领、附件打包下载。  
> **前置**：[07](./07-sprint-system-pages.md) 可先做完（岗位/字典/日志不依赖 Q）。  
> **不做**：代码生成、服务监控、企微卡片。通用路径下载若后端未做 Q4，前端不单开入口。  
> **已旁路完成（不依赖 Q）**：角色「菜单权限」树见 [07 F07-7](./07-sprint-system-pages.md)（与本文件 F11-1 的「角色分配用户」不同）。

---

## 0. 范围

```text
F11-1  用户页：导出、导入、分配角色（已分配 / 未分配）；角色「勾菜单」已在 07-F07-7，此处只做「角色↔用户」
F11-2  顶栏或个人区：上传头像，getInfo 后能显示
F11-3  登录页：注册开关打开才显示注册；关闭不显示
F11-4  任务类型 CRUD；新建任务可选类型
F11-5  待认领任务「认领」（业务认领，不是流程 claim）
F11-6  附件多选打 zip 下载
```

建议：**1 → 2 → 3** 系统，再 **4 → 5 → 6** 项目。

**协作**：API 与对话框壳由助手写；导入 FormData、导出 blob、认领搜 `【学员填写】`。

---

## 1. 后端接口速查

| 动作 | 方法 | 路径 |
|------|------|------|
| 用户导出 | GET | `/system/user/export`（条件同列表，blob） |
| 用户导入 | POST | `/system/user/import` multipart；用户名已存在则更新昵称和手机 |
| 已分配 / 未分配 | GET | `/system/user/authRole/allocatedList` · `unallocatedList?roleId=` |
| 保存分配 | PUT | `/system/user/authRole`（roleId + userIds，语义以接口注释为准） |
| 头像 | POST | `/system/user/profile/avatar` |
| 注册 | POST | `/register`；开关 `sys.account.registerUser` 非 true 则「未开放注册」 |
| 任务类型 | — | `/project/taskType` CRUD；有任务引用则不能删 |
| 认领 | POST | `/project/task/claim` body `{ taskId }`；仅 status=待认领 `4`，成功后变为未开始 `0` |
| zip | POST | `/project/file/zip` body 为同一项目的 fileId 列表，响应为文件流 |

模板上传是 `POST /project/file/upload` 且 `type=template`，可在现有 FilePanel 加一个类型选项，不单开页。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/system/user.js         # + export / import / authRole
  api/system/profile.js      # avatar
  api/auth.js                # register
  api/project/taskType.js
  api/project/task.js        # claim
  api/project/file.js        # zip
  views/system/user/index.vue
  views/login/index.vue      # 注册入口 + 注册对话框或子页
  views/project/task-type/index.vue
  views/project/task/index.vue
  components/project/FilePanel.vue
  layout/index.vue           # 头像
```

导出沿用 `utils/download.js`（任务 Excel 那套）：`responseType: 'blob'`，错误 JSON 要能解析出 message。

---

## 3. 分阶段要点

### F11-1 用户

- 「导出」带当前搜索条件。
- 「导入」选 xlsx，成功提示后端返回的条数说明。
- 角色页或用户页：选角色 → 左右或两个表格（已分配/未分配）→ 保存。

### F11-2 / F11-3

- 头像：`el-upload` 限制图片；成功后刷新 `fetchUserInfo`，顶栏用 `user.avatar`。
- 注册：进入登录页可读配置（若没有匿名读配置接口，就始终显示注册按钮，失败时展示「未开放注册」）。优先：有公开查询再用开关控制显隐。

### F11-4～6

- 任务类型表格 + 对话框；新建任务下拉可空。
- 仅 `status===4` 显示「认领」。
- zip：勾选多个附件再下载；一个文件都没有时提示，不要下出空包还当成功。

---

## 4. 验收标准

- [ ] 导出再导入：新用户能进去，已有用户名只更新昵称/手机
- [ ] 角色分配保存后两边列表变化正确
- [ ] 头像更新后刷新页面仍在
- [ ] 注册关闭时不能注册成功，且有文案
- [ ] 任务类型可维护；被引用时删除失败
- [ ] 待认领任务认领后状态变为未开始
- [ ] 多文件 zip 能打开

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

项目动态总列表若要单独页，可加在项目行「动态」，接口 `POST /project/log/list`。本 Sprint 有余力再做，不挡验收。
