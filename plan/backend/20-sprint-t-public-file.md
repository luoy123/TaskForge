# Sprint T：公共文件 / 分享链接

> **状态**：排队  
> **估算**：3～5 人日  
> **目标**：对照 `pmhub_public_file`、`pmhub_public_file_link`、`pmhub_public_file_cache`，做公共网盘：上传、目录、分享链接、访问计数。  
> **前置**：G 的本地上传工具。  
> **不做**：OSS、在线预览转码、外链鉴权中心。

## 文件对照

公开索引里没有 PublicFile 类。本地 pmhub 搜 `PublicFile`；没有就按表建。

表：`pmhub_public_file`、`pmhub_public_file_link`、`pmhub_public_file_cache`（`sql/pmhub_20240305.sql`）。上传工具看已有 `taskforge-common/.../utils/file/FileUploadUtils.java`，以及 pmhub 的 `UploadTemplateFileExecutor` 只作「怎么落盘」参考，不要塞进项目文件工厂。

| TaskForge 新建 | 说明 |
|----------------|------|
| `taskforge-project/.../domain/PublicFile.java` | 文件/文件夹 |
| `taskforge-project/.../domain/PublicFileLink.java` | 分享链接 |
| `taskforge-project/.../domain/PublicFileCache.java` | 访问计数，可后做 |
| 对应 Mapper + `PublicFileServiceImpl` | |
| `taskforge-admin/.../controller/project/PublicFileController.java` | `/public/file`、`/public/link` |

**不新建模块。**

---

## 0. 过程

磁盘目录用 `TaskForgeConfig` 下的 `public/`，不要和项目附件目录混用。

### T1 文件

1. 实体对齐 `pmhub_public_file`。先确认有没有 `parent_id`、是文件还是文件夹的标记。
2. `mkdir`：在 parent 下插入文件夹行，不写磁盘文件；磁盘上仍可按 id 建目录，二选一，注释写清。
3. `upload`：`FileUploadUtils.upload` 到 public 目录，插入文件行，记下相对路径。
4. `list`：按 parentId 列出当前用户的直接子项。
5. `rename`：只改显示名。
6. `delete`：文件夹下还有子项就拒绝。文件则删行并删磁盘文件。
7. `download`：登录用户，且创建人是自己（超管除外），把文件写入 response。

### T2 分享

1. 实体对齐 `pmhub_public_file_link`。
2. `create`：生成随机 token，`expireTime` 默认 7 天，可被 body 覆盖。
3. `disable`：把状态改成停用。
4. `GET /public/link/open?token=` 加入 Security 白名单。
5. 打开时按顺序拒绝：token 不存在、已停用、`expireTime` 早于现在、对应文件已删。通过则下载，不要求登录。

### T3 计数

打开成功后，按表结构给 `pmhub_public_file_cache` insert 或把计数字段 +1。表结构对不上就跳过，不要为了计数改业务表。

### T4 权限核对

用用户 A 的文件让用户 B 调 download，应 失败。同一文件的分享链接，未登录能下。

---

## 1. API

| 前缀 | 能力 |
|------|------|
| `/public/file` | list / upload / mkdir / rename / delete / download |
| `/public/link` | create / list / disable |
| `/public/link/open` | 凭 token 下载（匿名） |

---

## 2. 验收

- [ ] 上传后列表可见、可下载  
- [ ] 分享链接过期后不可下  
- [ ] 停用后立即不可下  
- [ ] 删除文件后链接失效  
