# 前端 Sprint 16：代码生成（对齐后端 O）

> **编号**：`plan/frontend/16`  
> **状态**：✅ 已完成（2026-11-13 联调通过）  
> **目标**：从数据库选表导入，改生成配置，预览源码并下载 zip。  
> **不做**：写入本机工程目录、给 Flowable 表或 `gen_table` 自身生成代码。

---

## 0. 范围

```text
F16-1  未导入表列表，勾选导入
F16-2  已导入表列表；编辑类名、包名、列的 Java 类型
F16-3  预览：按文件名切换源码（只读）
F16-4  下载 zip
```

建议：**1 → 2 → 3 → 4**。用 `sys_notice` 走通一遍即可。

**协作**：双列表与预览抽屉壳由助手写；导入、保存、下载 blob 搜 `【学员填写】`。

---

## 1. 后端接口速查

| 动作 | 方法 | 路径 |
|------|------|------|
| 库里还没导入的表 | GET | `/tool/gen/db/list` |
| 导入 | POST | `/tool/gen/importTable`（表名数组） |
| 已导入列表 / 详情 | GET | `/tool/gen/list` · `/tool/gen/{tableId}` |
| 改配置 | PUT | `/tool/gen`（类名、包名、列类型，以 VO 为准） |
| 预览 | GET | `/tool/gen/preview/{tableId}` → map：文件名 → 源码字符串 |
| 下载 | GET | `/tool/gen/download/{tableId}` → zip |

zip 内应有 Entity、Mapper、xml、Service、Controller，且 Controller 用 `R`，不是 `TableDataInfo`。前端不校验编译，能下到文件即可。

---

## 2. 前端落位

```text
taskforge-ui/src/
  api/tool/gen.js
  views/tool/gen/index.vue       # 已导入表
  # 导入弹窗、编辑弹窗、预览抽屉都放在同一页
  utils/download.js              # 复用
```

菜单在「系统工具」。component 多为 `tool/gen/index`。

---

## 3. 分阶段要点

### F16-1 / F16-2

- 工具栏「导入」打开对话框，表格多选表名。
- 编辑：基本信息 + 字段表（Java 类型下拉或输入）。保存后列表类名更新。

### F16-3 / F16-4

- 预览：左侧文件名，右侧 `<pre>` 或只读文本域。不要提供「复制到项目」按钮。
- 下载文件名：`{tableName}.zip` 或后端 `Content-Disposition`。
- blob 错误：若返回的是 JSON（失败），要读出来弹 message，不要把 JSON 存成 zip。

---

## 4. 验收标准

- [x] `sys_notice` 能导入、改包名、预览到 Controller 源码
- [x] 下载的 zip 能打开，里面有上述几类文件
- [x] 页面上没有「生成到磁盘」
- [x] 菜单不进 `building`

```bash
cd taskforge-ui && npm run dev
```

---

## 5. 再往后

生成出来的页面不要自动注册进 `taskforge-ui` 路由。
