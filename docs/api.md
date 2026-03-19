# API 参考文档

本文档提供 Spring Security Boot 框架提供的所有 API 端点的详细说明。

## 目录

- [通用响应格式](#通用响应格式)
- [错误码说明](#错误码说明)
- [认证端点](#认证端点-apiauth)
- [用户管理端点](#用户管理端点-apiusers)
- [角色管理端点](#角色管理端点-apiroles)
- [会话管理端点](#会话管理端点-apisessions)
- [API 使用示例](#api-使用示例)

---

## 通用响应格式

所有 API 端点统一使用以下响应格式：

### 成功响应

```json
{
  "code": 200,
  "path": "/api/users",
  "message": "success",
  "data": { ... },
  "timestamp": 1679112000000
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|-----|------|------|
| code | int | HTTP 状态码 |
| path | String | 请求路径（默认为空字符串，错误响应中可能包含请求路径） |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| timestamp | long | 时间戳（毫秒） |

### 错误响应

```json
{
  "code": 400,
  "path": "/api/users",
  "message": "请求参数错误",
  "data": null,
  "timestamp": 1679112000000
}
```

### HTTP 状态码说明

| HTTP 状态码 | 说明 |
|-------------|------|
| 200 | OK | 请求成功 |
| 400 | Bad Request | 请求参数错误或验证失败 |
| 401 | Unauthorized | 未认证或认证失败 |
| 403 | Forbidden | 无权限访问 |
| 404 | Not Found | 请求的资源不存在 |
| 500 | Internal Server Error | 服务器内部错误 |

---

## 错误码说明

### 业务错误码

| 错误码 | 说明 |
|-------|------|
| `USER_ALREADY_EXISTS` | 用户名已存在 |
| `EMAIL_ALREADY_EXISTS` | 邮箱已存在 |
| `INVALID_REQUEST` | 无效的请求参数 |
| `USER_NOT_FOUND` | 用户不存在 |
| `USER_DISABLED` | 用户已被禁用 |
| `UNAUTHORIZED` | 用户未认证 |
| `INVALID_OLD_PASSWORD` | 旧密码错误 |

### 通用错误信息

- `用户名或密码错误` - 认证失败时返回
- `请求参数错误` - 参数校验失败时返回
- `服务器内部错误，请稍后重试` - 服务器异常时返回

---

## 认证端点

认证端点提供用户登录、登出和 Token 刷新功能。

### POST /api/auth/login

用户登录接口，验证用户名和密码，成功后返回用户信息和 JWT Token。

#### 请求参数

```json
{
  "username": "admin",
  "password": "Password123!",
  "rememberMe": false
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | String | 是 | 用户名（3-50个字符） |
| password | String | 是 | 密码（8-50个字符，至少包含1个数字、1个字母和1个特殊字符） |
| rememberMe | boolean | 否 | 是否记住我（7天免登录） |

#### 响应示例

**成功响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "enabled": true,
      "roles": ["ADMIN"],
      "createdAt": "2026-03-18T00:00:00Z"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "jwtEnabled": true
  }
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Password123!",
    "rememberMe": false
  }'
```

#### Java 示例

```java
import org.springframework.web.client.RestTemplate;

RestTemplate restTemplate = new RestTemplate();

LoginRequest request = new LoginRequest();
request.setUsername("admin");
request.setPassword("Password123!");
request.setRememberMe(false);

Response<AuthResponse> response = restTemplate.postForObject(
    "http://localhost:8080/api/auth/login",
    request,
    Response.class
);
```

#### 错误响应

**用户名或密码错误：**

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1679112000000
}
```

---

### POST /api/auth/logout

用户登出接口，清除当前用户的安全上下文和 Remember Me Cookie。

#### 请求参数

无需请求体（依赖 HTTP Session 或 JWT Token）

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUz..."
```

#### Java 示例

```java
// 在已认证的请求中
restTemplate.exchange(
    "http://localhost:8080/api/auth/logout",
    HttpMethod.POST,
    new HttpEntity<>(headers),
    String.class
);
```

---

### POST /api/auth/refresh

Token 刷新接口，使用当前有效的 JWT Token 获取新的 Token。

#### 请求参数

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| token | String | 是 | 当前有效的 JWT Token |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user": null,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "jwtEnabled": true
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

#### 错误响应

**Token 为空：**

```json
{
  "code": 400,
  "message": "刷新 Token 不能为空",
  "data": null,
  "timestamp": 1679112000000
}
```

**Token 已过期：**

```json
{
  "code": 401,
  "message": "Token 已过期，请重新登录",
  "data": null,
  "timestamp": 1679112000000
}
```

**无效 Token：**

```json
{
  "code": 401,
  "message": "无效的 Token",
  "data": null,
  "timestamp": 1679112000000
}
```

**JWT 未启用：**

```json
{
  "code": 500,
  "message": "JWT 认证未启用",
  "data": null,
  "timestamp": 1679112000000
}
```

**刷新 Token 失败：**

```json
{
  "code": 500,
  "message": "刷新 Token 失败，请稍后重试",
  "data": null,
  "timestamp": 1679112000000
}
```

---

## 用户管理端点

用户管理端点提供用户 CRUD、密码管理等功能。

### POST /api/users

创建用户接口，创建新的用户账号。

#### 请求参数

```json
{
  "username": "testuser",
  "password": "Password123!",
  "email": "test@example.com"
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | String | 是 | 用户名（3-50个字符） |
| password | String | 是 | 密码（8-50个字符，至少包含1个数字、1个字母和1个特殊字符） |
| email | String | 是 | 邮箱（1-100个字符） |

#### 验证规则

- 用户名不能为空
- 用户名长度 3-50 个字符
- 密码不能为空
- 密码长度 8-50 个字符
- 密码必须包含至少一个数字、一个字母和一个特殊字符（正则：`^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,50}$`）
- 特殊字符包括：@#$%^&+=!
- 邮箱不能为空
- 邮箱格式必须正确（包含 @）
- 邮箱长度 1-100 个字符

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "testuser",
    "email": "test@example.com",
    "enabled": true,
    "roles": [],
    "createdAt": "2026-03-18T00:00:00Z"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!",
    "email": "test@example.com"
  }'
```

#### 错误响应

**用户名已存在：**

```json
{
  "code": 400,
  "message": "[USER_ALREADY_EXISTS] 用户名 testuser 已存在",
  "data": null,
  "timestamp": 1679112000000
}
```

**邮箱已存在：**

```json
{
  "code": 400,
  "message": "[EMAIL_ALREADY_EXISTS] 邮箱 test@example.com 已存在",
  "data": null,
  "timestamp": 1679112000000
}
```

---

### GET /api/users/me

获取当前登录用户的信息。

#### 请求参数

无需路径参数，使用 HTTP Header 中的认证信息。

#### 权限要求

需要用户已认证（`@PreAuthorize("isAuthenticated()")`）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "enabled": true,
    "roles": ["ADMIN"],
    "createdAt": "2026-03-18T00:00:00Z"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 错误响应

**未认证：**

```json
{
  "code": 401,
  "message": "[UNAUTHORIZED] 用户未认证",
  "data": null,
  "timestamp": 1679112000000
}
```

---

### GET /api/users/{userId}

根据用户 ID 获取用户详情。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户 ID |

#### 权限要求

由 Spring Security 默认拦截器控制（通常需要已认证，具体取决于安全配置）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "enabled": true,
    "roles": ["ADMIN"],
    "createdAt": "2026-03-18T00:00:00Z"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 错误响应

**用户不存在：**

```json
{
  "code": 404,
  "message": "[USER_NOT_FOUND] 用户不存在",
  "data": null,
  "timestamp": 1679112000000
}
```

---

### GET /api/users

用户列表查询，支持分页、用户名模糊搜索和状态筛选。

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | int | 否 | 0 | 页码（从 0 开始） |
| size | int | 否 | 10 | 每页大小 |
| username | String | 否 | - | 用户名关键词（模糊查询） |
| enabled | Boolean | 否 | - | 用户启用状态筛选 |

#### 分页参数说明

- `page`：从 0 开始
- `size`：每页条数，最大 100
- 所有参数一起使用可组合过滤条件

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "enabled": true,
        "roles": ["ADMIN"],
        "createdAt": "2026-03-18T00:00:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
# 基础查询
curl -X GET "http://localhost:8080/api/users?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 搜索用户名
curl -X GET "http://localhost:8080/api/users?username=admin&page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 筛选已启用用户
curl -X GET "http://localhost:8080/api/users?enabled=true&page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### POST /api/users/me/password

修改当前用户密码。

#### 请求参数

```json
{
  "oldPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码（8-50个字符，必须包含1个数字、1个字母和1个特殊字符） |

#### 密码策略验证

- 旧密码不能为空
- 新密码不能为空
- 新密码长度 8-50 个字符
- 新密码必须包含至少一个数字、一个字母和一个特殊字符（正则：`^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,50}$`）
- 特殊字符包括：@#$%^&+=!

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/users/me/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "oldPassword": "OldPassword123!",
    "newPassword": "NewPassword456!"
  }'
```

#### 错误响应

**旧密码错误：**

```json
{
  "code": 400,
  "message": "[INVALID_OLD_PASSWORD] 旧密码错误",
  "data": null,
  "timestamp": 1679112000000
}
```

**未认证：**

```json
{
  "code": 401,
  "message": "[UNAUTHORIZED] 用户未认证",
  "data": null,
  "timestamp": 1679112000000
}
```

---

### POST /api/users/{userId}/password/reset

重置指定用户密码（管理员权限）。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户 ID（路径参数） |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "newPassword": "TempPass789",
    "message": "密码已重置"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/users/2/password/reset \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 权限要求

需要管理员权限（`@PreAuthorize("hasRole('ADMIN')")`）。

#### 错误响应

**用户不存在：**

```json
{
  "code": 404,
  "message": "[USER_NOT_FOUND] 用户不存在",
  "data": null,
  "timestamp": 1679112000000
}
```

---

## 角色管理端点

角色管理端点提供角色 CRUD、权限分配等功能。

### POST /api/roles

创建角色。

#### 请求参数

```json
{
  "name": "editor",
  "description": "内容编辑角色"
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| name | String | 是 | 角色名称 |
| description | String | 否 | 角色描述 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "editor",
    "description": "内容编辑角色",
    "permissions": [],
    "createdAt": "2026-03-18T00:00:00Z"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "name": "editor",
    "description": "内容编辑角色"
  }'
```

---

### GET /api/roles/{roleId}

获取角色详情。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| roleId | Long | 是 | 角色 ID |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "editor",
    "description": "内容编辑角色",
    "permissions": [
      {
        "id": 1,
        "name": "article:write",
        "description": "文章写入权限"
      }
    ],
    "createdAt": "2026-03-18T00:00:00Z"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X GET http://localhost:8080/api/roles/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### GET /api/roles

角色列表查询，支持分页。

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | int | 否 | 0 | 页码（从 0 开始） |
| size | int | 否 | 10 | 每页大小 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "editor",
        "description": "内容编辑角色",
        "permissions": [],
        "createdAt": "2026-03-18T00:00:00Z"
      },
      {
        "id": 2,
        "name": "viewer",
        "description": "查看者角色",
        "permissions": [
          {
            "id": 1,
            "name": "article:read",
            "description": "文章读取权限"
          }
        ],
        "createdAt": "2026-03-18T00:00:00Z"
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "size": 10,
    "number": 0
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X GET http://localhost:8080/api/roles?page=0&size=10 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### POST /api/roles/{roleId}/permissions

分配权限给角色。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| roleId | Long | 是 | 角色 ID（路径参数） |

**请求体：**

```json
{
  "permissionIds": [1, 2, 3]
}
```

#### 请求字段说明

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| permissionIds | List&lt;Long&gt; | 是 | 权限 ID 列表 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/roles/1/permissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "permissionIds": [1, 2, 3]
  }'
```

---

### DELETE /api/roles/cache

清除权限和角色缓存。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | String | 否 | 指定用户名的缓存（可选） |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
# 清除所有缓存
curl -X DELETE http://localhost:8080/api/roles/cache \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."

# 清除指定用户缓存
curl -X DELETE "http://localhost:8080/api/roles/cache?username=testuser" \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 会话管理端点

会话管理端点提供会话查询和踢出下线功能。

### GET /api/sessions

查询所有用户的活跃会话（仅限管理员）。

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | int | 否 | 0 | 页码（从 0 开始） |
| size | int | 否 | 10 | 每页大小 |

#### 权限要求

需要管理员权限（`@PreAuthorize("hasRole('ADMIN')")`）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "sessionId": "abc123",
        "username": "user1",
        "loginTime": "2026-03-18T08:00:00Z",
        "lastActiveTime": "2026-03-18T09:30:00Z",
        "ipAddress": "unknown"
      },
      {
        "sessionId": "def456",
        "username": "user2",
        "loginTime": "2026-03-18T08:30:00Z",
        "lastActiveTime": "2026-03-18T09:30:00Z",
        "ipAddress": "unknown"
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "size": 10,
    "number": 0
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X GET "http://localhost:8080/api/sessions?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 注意事项

- 在无状态模式（如纯 JWT 且不保存会话记录）下，可能返回空数据
- SessionRegistry 默认不记录 IP 地址，IP 地址字段将显示为 "unknown"

---

### GET /api/sessions/me

查询当前登录用户的活跃会话。

#### 请求参数

无需路径参数，使用 HTTP Header 中的认证信息。

#### 权限要求

需要用户已认证。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "sessionId": "abc123",
      "username": "admin",
      "loginTime": "2026-03-18T08:00:00Z",
      "lastActiveTime": "2026-03-18T09:30:00Z",
      "ipAddress": "unknown"
    }
  ],
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X GET http://localhost:8080/api/sessions/me \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### POST /api/sessions/{userId}/kick

强制指定用户的所有会话下线（仅限管理员）。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | String | 是 | 用户名（路径参数） |
| reason | String | 否 | 踢出原因（可选） |

#### 权限要求

需要管理员权限（`@PreAuthorize("hasRole('ADMIN')")`）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "user1",
    "kickedCount": 2,
    "message": "User kicked successfully"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/sessions/user1/kick?reason=安全清理 \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 错误响应

**用户 ID 为空：**

```json
{
  "code": 400,
  "message": "userId cannot be empty or blank",
  "data": null,
  "timestamp": 1679112000000
}
```

**SessionRegistry 不可用：**

```json
{
  "code": 500,
  "message": "SessionRegistry not available, cannot kick user",
  "data": null,
  "timestamp": 1679112000000
}
```

---

### POST /api/sessions/{sessionId}/kick

踢出指定会话（仅限管理员）。

#### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| sessionId | String | 是 | 会话 ID |
| reason | String | 否 | 踢出原因（可选） |

#### 权限要求

需要管理员权限（`@PreAuthorize("hasRole('ADMIN')")`）。

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "user1",
    "kickedCount": 1,
    "message": "Session kicked successfully"
  },
  "timestamp": 1679112000000
}
```

#### curl 示例

```bash
curl -X POST http://localhost:8080/api/sessions/abc123/kick \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 错误响应

**会话 ID 为空：**

```json
{
  "code": 400,
  "message": "sessionId cannot be empty or blank",
  "data": null,
  "timestamp": 1679112000000
}
```

**会话不存在或已过期：**

```json
{
  "code": 404,
  "message": "Session not found or already expired",
  "data": null,
  "timestamp": 1679112000000
}
```

---

## API 使用示例

### 完整的认证流程示例

#### 1. 用户登录

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Password123!"
  }'
```

#### 2. 访问受保护资源

```bash
# 使用返回的 Token 访问受保护端点
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Token 刷新

```bash
# 刷新 Token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

#### 4. 用户登出

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 用户管理示例

#### 创建用户

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "username": "newuser",
    "password": "Password123!",
    "email": "newuser@example.com"
  }'
```

#### 修改当前用户密码

```bash
curl -X POST http://localhost:8080/api/users/me/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "oldPassword": "OldPassword123!",
    "newPassword": "NewPassword456!"
  }'
```

#### 重置用户密码（管理员）

```bash
curl -X POST http://localhost:8080/api/users/2/password/reset \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 角色管理示例

#### 创建角色

```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "name": "editor",
    "description": "内容编辑角色"
  }'
```

#### 查询角色列表

```bash
curl -X GET http://localhost:8080/api/roles?page=0&size=10 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 分配权限给角色

```bash
curl -X POST http://localhost:8080/api/roles/1/permissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "permissionIds": [1, 2, 3]
  }'
```

### 会话管理示例

#### 查询所有会话（管理员）

```bash
curl -X GET "http://localhost:8080/api/sessions?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 查询当前用户会话

```bash
curl -X GET http://localhost:8080/api/sessions/me \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 踢出指定用户所有会话

```bash
curl -X POST http://localhost:8080/api/sessions/user1/kick?reason=安全清理 \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 踢出指定会话

```bash
curl -X POST http://localhost:8080/api/sessions/abc123/kick \
  -H "Authorization: Bearer eyJhbGciOiJIzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 下一步阅读

- [配置参考文档](configuration.md) - 详细的配置项说明
- [快速开始文档](quick-start.md) - 30 分钟快速集成指南
- [测试支持工具文档](testing-support.md) - 为安全逻辑添加测试
