# TaskForge

企业级项目管理系统，从 Spring Boot 3 单体架构起步，最终演进为微服务架构。

## 技术栈

| 层 | 选型 |
|------|------|
| 框架 | Spring Boot 3.3, Java 17 |
| ORM | MyBatis-Plus 3.5.7 |
| 安全 | Spring Security + JWT (Hutool HS256, 7天过期) |
| 缓存 | Redis (Spring Data Redis) |
| 数据库 | MySQL 8 |
| 文档 | Knife4j (Swagger) |

## 快速启动

```bash
# 1. 创建数据库
mysql> CREATE DATABASE laigeoffer-pmhub;

# 2. 修改数据库密码（application-dev.yml）
spring.datasource.password=你的密码

# 3. 启动
mvn spring-boot:run

# 4. 访问 API 文档
http://localhost:1234/doc.html
```

## 登录测试

```
POST /auth/login
{"userName": "admin", "password": "123456"}
```

## 已完成模块

- [x] 用户登录 / JWT 鉴权
- [x] 用户管理
- [x] 角色管理
- [x] 菜单权限（路由/按钮级）
- [x] 部门管理（树形结构）
- [x] 岗位管理
- [x] 字典管理
- [x] 参数设置

## 开发

```bash
mvn clean compile    # 编译检查
mvn test            # 运行测试
```

## 项目结构

```
com.zhq.taskforge
├── auth/        登录认证
├── common/      公共工具类、常量
├── config/      安全配置、全局异常处理
├── security/    JWT 过滤器、权限服务
└── model/system/  系统模块（entity/mapper/service/controller）
```

## 路线图

参见 [ROADMAP.md](ROADMAP.md) — 共 6 阶段，当前已完成阶段一和阶段二。
