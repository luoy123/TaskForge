# Sprint O：代码生成 generator（自学版）

> **状态**：排队（学习向；非业务 MVP）  
> **目标**：对照 pmhub-generator / RuoYi，从数据表生成基础 Java（domain/mapper/service/controller）与可选 Vue 片段。  
> **前置**：system 模块、代码风格稳定。  
> **不做**：一次性完美支持所有表类型、Flowable 表、覆盖手写业务代码。

---

## 0. 范围

```text
O1  新建 taskforge-generator 模块 + Velocity/Freemarker 模板
O2  读库表 / 列元数据；预览生成结果
O3  下载 zip 或写入指定包路径（二选一，计划写死）
O4  生成物：Entity、Mapper、XML、Service、Controller、R\<T\> 风格
```

---

## 1. 注意

- 包名 `com.zhq.taskforge...`，响应 `R` 不是 `AjaxResult`。  
- 权限字、Swagger 注解按现有 admin 习惯。  
- 生成后不自动覆盖 `taskforge-project` 已有复杂业务。

---

## 2. 验收

- [ ] 选一张简单表（如 `sys_notice`）能预览/下载代码  
- [ ] 生成代码可编译（或文档标明需手改的点）  

---

## 3. 明确更后置

- **OA / 企微**：无企业微信时可永久跳过或只做消息模板 Mock。  
- 动态数据源、WebSocket：一般不做。  
