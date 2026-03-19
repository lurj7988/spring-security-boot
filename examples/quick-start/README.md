# Spring Security Boot 快速开始示例

这是一个完整的 Spring Security Boot 框架示例项目，展示了如何快速集成和使用框架的核心功能。

## 前置条件

- Java 1.8+
- Maven 3.x

## 快速开始

### 1. 运行项目

```bash
mvn spring-boot:run
```

或先打包再运行：

```bash
mvn clean package
java -jar target/quick-start-0.1.0-SNAPSHOT.jar
```

### 2. 访问端点

项目启动后，访问以下端点：

| 功能 | 端点 | 说明 |
|-----|------|-----|
| 公开 API | `GET /api/hello` | 不需要认证 |
| 登录 | `POST /api/auth/login` | 获取 JWT Token |
| 受保护 API | `GET /api/users/me` | 需要认证 Token |
| 管理员 API | `GET /api/admin/users` | 需要 ADMIN 角色 |

## 示例说明

### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com"
    }
  }
}
```

### 2. 使用 Token 访问受保护接口

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer {token}"
```

### 3. 访问管理员接口（需要 ADMIN 角色）

```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer {token}"
```

## 示例代码说明

### 目录结构

```
quick-start/
├── src/main/
│   ├── java/com/example/quickstart/
│   │   ├── QuickStartApplication.java    # 启动类
│   │   └── controller/
│   │       ├── HelloController.java       # 公开端点
│   │       ├── UserController.java        # 用户接口
│   │       └── AdminController.java      # 管理员接口
│   └── resources/
│       ├── application.properties         # 配置文件
│       └── data.sql                       # 初始化数据
```

### 关键代码

**启用 Security Boot：**

```java
@EnableSecurityBoot
@SpringBootApplication
public class QuickStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickStartApplication.class, args);
    }
}
```

**受保护的端点：**

```java
@GetMapping("/users/me")
public Response<UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    // 自动获取当前用户
}
```

**权限控制：**

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/users")
public Response<List<UserInfo>> getAllUsers() {
    // 只有 ADMIN 角色可以访问
}
```

## 测试账户

| 用户名 | 密码 | 角色 |
|-------|------|-----|
| admin | password123 | ADMIN, USER |
| user | password123 | USER |
| test | password123 | USER |

## 更多文档

- [快速开始文档](../../docs/quick-start.md)
- [配置参考](../../docs/configuration.md)
- [API 参考](../../docs/api.md)
