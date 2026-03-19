# Spring Security Boot 配置参考文档

本文档提供了 Spring Security Boot 框架所有配置项的详细说明，帮助开发者快速理解和配置框架的各项功能。

## 文档导航

- [配置组总览](#配置组总览)
- [数据库配置](#数据库配置)
- [配置验证](#配置验证)
- [缓存配置](#缓存配置)
- [JWT 认证配置](#jwt-认证配置)
- [网络安全配置](#网络安全配置)
  - [CORS 配置](#cors-配置)
  - [CSRF 配置](#csrf-配置)
  - [安全响应头配置](#安全响应头配置)
  - [内容安全策略配置](#内容安全策略配置)
- [会话管理配置](#会话管理配置)
- [记住我功能配置](#记住我功能配置)
- [监控与指标配置](#监控与指标配置)
  - [Metrics 配置](#metrics-配置)
  - [健康检查配置](#健康检查配置)
  - [日志配置](#日志配置)
  - [追踪配置](#追踪配置)
- [配置示例](#配置示例)
  - [最小配置示例](#最小配置示例)
  - [完整配置示例](#完整配置示例)
  - [生产环境推荐配置](#生产环境推荐配置)

---

## 配置组总览

### 配置键前缀总览表

| 配置组 | 配置键前缀 | 说明 | 优先级 |
|-------|-----------|------|--------|
| 数据库配置 | `spring.datasource.*` | 数据源连接配置 | P0 |
| 配置验证 | `security.config.*` | 配置验证开关 | P0 |
| 缓存配置 | `security.cache.*` | 缓存行为配置 | P1 |
| JWT 认证配置 | `security.jwt.*` | JWT Token 生成与验证 | P0 |
| CORS 配置 | `security.network.cors.*` | 跨域资源共享配置 | P0 |
| CSRF 配置 | `security.network.csrf.*` | 跨站请求伪造防护 | P0 |
| 安全响应头配置 | `security.network.headers.*` | HTTP 安全响应头 | P0 |
| 内容安全策略配置 | `security.network.csp.*` | 内容安全策略 | P1 |
| 会话管理配置 | `security.session.*` | 会话管理配置 | P1 |
| 记住我功能配置 | `security.remember-me.*` | 记住我功能配置 | P1 |
| Metrics 配置 | `security.metrics.*` | 安全监控指标 | P2 |
| 健康检查配置 | `security.health.*` | 安全组件健康检查 | P2 |
| 日志配置 | `security.logging.*` | 安全日志配置 | P2 |
| 追踪配置 | `security.tracing.*` | 分布式追踪配置 | P2 |

### 配置级别说明

- **Required (必填)**: 启动时必须配置，否则应用启动失败
- **Optional (可选)**: 有默认值，可根据需要覆盖
- **Advanced (高级)**: 仅在生产环境或特殊场景需要配置

---

## 数据库配置

配置组：`spring.datasource.*`
必填级别：**Required**
优先级：**P0**

框架使用标准的 Spring Boot 数据源配置。数据库连接必须在启动时配置，否则会触发配置验证错误。

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 示例 |
|-------|------|--------|------|------|
| `spring.datasource.url` | String | - | 数据库连接 URL | `jdbc:mysql://localhost:3306/security_demo?useSSL=false&serverTimezone=UTC` |
| `spring.datasource.username` | String | - | 数据库用户名 | `root` |
| `spring.datasource.password` | String | - | 数据库密码 | `your_secure_password` |
| `spring.datasource.driver-class-name` | String | - | JDBC 驱动类名（可选） | `com.mysql.cj.jdbc.Driver` |

### 配置示例

```properties
# MySQL 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/security_demo?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_secure_password_here

# 可选：明确指定驱动类名
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## 配置验证

配置组：`security.config.*`
必填级别：**Optional**
优先级：**P0**

框架在启动时会验证关键配置，确保安全功能的正确性。可以通过此配置开关启用或禁用验证。

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.config.validation` | Boolean | `true` | 是否启用配置验证 | Optional |

### 说明

- **启用验证**（默认）：框架会在启动时检查数据库配置、CORS 配置等关键项
- **禁用验证**：不进行配置检查，适用于快速开发和测试环境（不推荐生产环境使用）

### 配置示例

```properties
# 启用配置验证（默认）
security.config.validation=true

# 禁用配置验证（不推荐生产环境使用）
security.config.validation=false
```

### 验证失败提示

如果配置验证失败，框架会提供详细的错误信息和解决方案：

1. **数据库连接未配置**：
   ```
   错误: 数据库连接未配置
   解决方案:
     1. 添加到 application.properties:
        spring.datasource.url=jdbc:mysql://localhost:3306/mydb
        spring.datasource.username=root
        spring.datasource.password=***
   ```

2. **CORS 已启用，但未配置 allowed-origins**：
   ```
   错误: CORS 已启用，但未配置 allowed-origins
   解决方案:
     1. 添加到 application.properties:
        security.network.cors.allowed-origins=http://localhost:8080,https://example.com
   ```

---

## 缓存配置

配置组：`security.cache.*`
必填级别：**Optional**
优先级：**P1**

框架内部使用的缓存行为配置，用于优化认证和授权相关的性能。

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.cache.maximum-size` | Integer | `1000` | 缓存最大条目数 | Optional |
| `security.cache.ttl-minutes` | Long | `30` | 缓存条目存活时间（分钟） | Optional |

### 配置示例

```properties
# 缓存配置
security.cache.maximum-size=2000
security.cache.ttl-minutes=45
```

---

## JWT 认证配置

配置组：`security.jwt.*`
必填级别：**Optional**
优先级：**P0**

用于配置 JWT（JSON Web Token）的生成和验证。

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.jwt.secret` | String | - | JWT 签名密钥，必须足够安全 | Optional |
| `security.jwt.expiration` | Long | `3600` | Token 过期时间（秒），默认 1 小时 | Optional |

### 说明

- **`security.jwt.secret`**: 生产环境必须配置强密钥，建议至少 32 位随机字符串
- **`security.jwt.expiration`**: Token 有效期，单位为秒

### 配置示例

```properties
# JWT 配置
security.jwt.secret=your-very-long-and-secret-key-here-at-least-32-characters
security.jwt.expiration=7200  # 2 小时
```

---

## 网络安全配置

### CORS 配置

配置组：`security.network.cors.*`
必填级别：**Required**（当启用时）
优先级：**P0**

Cross-Origin Resource Sharing（跨域资源共享）配置。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.network.cors.enabled` | Boolean | `true` | 是否启用 CORS | Optional |
| `security.network.cors.allowed-origins` | List<String> | - | 允许的来源列表 | Required（当启用时） |
| `security.network.cors.allowed-methods` | List<String> | `["GET", "POST", "PUT", "DELETE", "OPTIONS"]` | 允许的 HTTP 方法 | Optional |
| `security.network.cors.allowed-headers` | List<String> | `["*"]` | 允许的请求头 | Optional |
| `security.network.cors.max-age` | Long | `1800` | 预检请求缓存时间（秒） | Optional |
| `security.network.cors.exposed-headers` | List<String> | - | 暴露给客户端的响应头 | Optional |

#### 说明

- **`allowed-origins`**: 必须配置，否则启动失败
  - 开发环境可以使用 `*` 允许所有来源（不推荐生产环境使用）
  - 生产环境应配置具体的域名列表
- **`allowed-methods`**: 默认包含所有常用 HTTP 方法
- **`allowed-headers`**: `["*"]` 表示允许所有请求头

#### 配置示例

```properties
# CORS 配置
security.network.cors.enabled=true
security.network.cors.allowed-origins=http://localhost:8080,https://yourdomain.com
security.network.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
security.network.cors.allowed-headers=*,Content-Type,X-Requested-With
security.network.cors.max-age=3600
```

### CSRF 配置

配置组：`security.network.csrf.*`
必填级别：**Optional**
优先级：**P0**

Cross-Site Request Forgery（跨站请求伪造）防护配置。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.network.csrf.enabled` | Boolean | `true` | 是否启用 CSRF 防护 | Optional |
| `security.network.csrf.token-header` | String | `X-CSRF-TOKEN` | CSRF Token 请求头名称 | Optional |

#### 配置示例

```properties
# CSRF 配置
security.network.csrf.enabled=true
security.network.csrf.token-header=X-CSRF-TOKEN

# 对于 REST API，通常禁用 CSRF
security.network.csrf.enabled=false
```

### 安全响应头配置

配置组：`security.network.headers.*`
必填级别：**Optional**
优先级：**P0**

HTTP 安全响应头配置，提供额外的安全保护。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.network.headers.enabled` | Boolean | `true` | 是否启用安全响应头 | Optional |
| `security.network.headers.frame-options` | String | `DENY` | X-Frame-Options 值（DENY/SAMEORIGIN） | Optional |
| `security.network.headers.content-type-options` | Boolean | `true` | 是否启用 X-Content-Type-Options | Optional |
| `security.network.headers.xss-protection` | Boolean | `true` | 是否启用 X-XSS-Protection | Optional |
| `security.network.headers.hsts-max-age` | Integer | `31536000` | HSTS max-age 值（秒），默认约 365 天 | Optional |
| `security.network.headers.hsts-include-sub-domains` | Boolean | `true` | HSTS 是否包含子域名 | Optional |
| `security.network.headers.hsts-preload` | Boolean | `false` | 是否启用 HSTS preload | Optional |

#### 说明

- **`frame-options`**:
  - `DENY`: 完全禁止页面被嵌入
  - `SAMEORIGIN`: 仅允许同源页面嵌入
- **`hsts-max-age`**: 仅在 HTTPS 环境下有效，设置为 0 禁用 HSTS

#### 配置示例

```properties
# 安全响应头配置
security.network.headers.enabled=true
security.network.headers.frame-options=SAMEORIGIN
security.network.headers.content-type-options=true
security.network.headers.xss-protection=true
security.network.headers.hsts-max-age=31536000
security.network.headers.hsts-include-sub-domains=true
```

### 内容安全策略配置

配置组：`security.network.csp.*`
必填级别：**Optional**
优先级：**P1**

Content Security Policy（内容安全策略）配置，防止跨站脚本攻击。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.network.csp.enabled` | Boolean | `false` | 是否启用 CSP | Optional |
| `security.network.csp.policy` | String | `default-src 'self'` | CSP 策略内容 | Optional |

#### 配置示例

```properties
# CSP 配置
security.network.csp.enabled=true
security.network.csp.policy=default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self'

# 严格策略
security.network.csp.policy=default-src 'self'; script-src 'none'; style-src 'none'; img-src 'self'
```

---

## 会话管理配置

配置组：`security.session.*`
必填级别：**Optional**
优先级：**P1**

Session 认证和会话管理配置。

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.session.enabled` | Boolean | `true` | 是否启用 Session 功能 | Optional |
| `security.session.timeout` | Integer | `1800` | Session 超时时间（秒），默认 30 分钟 | Optional |
| `security.session.max-sessions` | Integer | `1` | 单用户最大并发 Session 数，-1 表示无限制 | Optional |
| `security.session.store-type` | String | `memory` | Session 存储方式：memory 或 redis | Optional |
| `security.session.cookie-name` | String | `JSESSIONID` | Session Cookie 名称 | Optional |
| `security.session.fixation-protection` | Boolean | `true` | 是否启用 Session 固定攻击防护 | Optional |

### 配置示例

```properties
# Session 配置
security.session.enabled=true
security.session.timeout=1800
security.session.max-sessions=1
security.session.store-type=memory
security.session.cookie-name=JSESSIONID
security.session.fixation-protection=true

# Redis 存储（需要额外依赖）
security.session.store-type=redis
```

---

## 记住我功能配置

配置组：`security.remember-me.*`
必填级别：**Optional**
优先级：**P1**

Remember Me 功能配置，允许用户在关闭浏览器后仍保持登录状态。

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.remember-me.enabled` | Boolean | `true` | 是否启用 Remember Me 功能 | Optional |
| `security.remember-me.token-validity-seconds` | Integer | `604800` | Token 有效期（秒），默认 7 天 | Optional |
| `security.remember-me.key` | String | - | Remember Me 安全密钥 | Optional |
| `security.remember-me.cookie-name` | String | `remember-me` | Cookie 名称 | Optional |

### 配置示例

```properties
# Remember Me 配置
security.remember-me.enabled=true
security.remember-me.token-validity-seconds=604800
security.remember-me.key=your-remember-me-secret-key
security.remember-me.cookie-name=remember-me
```

---

## 监控与指标配置

### Metrics 配置

配置组：`security.metrics.*`
必填级别：**Optional**
优先级：**P2**

安全监控指标配置，用于收集认证相关的性能数据。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.metrics.enabled` | Boolean | `true` | 是否启用安全 Metrics | Optional |
| `security.metrics.authentication-success-enabled` | Boolean | `true` | 是否记录认证成功事件 | Optional |
| `security.metrics.authentication-failure-enabled` | Boolean | `true` | 是否记录认证失败事件 | Optional |
| `security.metrics.authentication-duration-enabled` | Boolean | `true` | 是否记录认证耗时 | Optional |
| `security.metrics.duration-percentiles` | List<Double> | `[0.5, 0.95, 0.99]` | 认证耗时百分位数配置 | Optional |
| `security.metrics.auth-paths` | List<String> | `["/api/auth/login", "/login"]` | 认证路径列表 | Optional |

#### 配置示例

```properties
# Metrics 配置
security.metrics.enabled=true
security.metrics.authentication-success-enabled=true
security.metrics.authentication-failure-enabled=true
security.metrics.authentication-duration-enabled=true
security.metrics.duration-percentiles=0.5,0.9,0.95,0.99
security.metrics.auth-paths=/api/auth/login,/api/v1/auth,/login
```

### 健康检查配置

配置组：`security.health.*`
必填级别：**Optional**
优先级：**P2**

安全组件健康检查配置。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.health.enabled` | Boolean | `true` | 是否启用健康检查 | Optional |
| `security.health.check-timeout-ms` | Integer | `5000` | 健康检查超时时间（毫秒） | Optional |

#### 配置示例

```properties
# 健康检查配置
security.health.enabled=true
security.health.check-timeout-ms=5000
```

### 日志配置

配置组：`security.logging.*`
必填级别：**Optional**
优先级：**P2**

安全日志配置，支持结构化日志输出。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.logging.enabled` | Boolean | `true` | 是否启用安全日志 | Optional |
| `security.logging.json-output` | Boolean | `true` | 是否使用 JSON 格式输出 | Optional |
| `security.logging.include-stack-trace` | Boolean | `true` | 是否包含堆栈跟踪（ERROR 级别） | Optional |
| `security.logging.include-client-ip` | Boolean | `true` | 是否包含客户端 IP | Optional |
| `security.logging.include-user-agent` | Boolean | `false` | 是否包含 User-Agent | Optional |
| `security.logging.include-request-id` | Boolean | `true` | 是否包含请求 ID | Optional |
| `security.logging.include-session-id` | Boolean | `false` | 是否包含会话 ID | Optional |
| `security.logging.default-level` | String | `INFO` | 默认日志级别 | Optional |
| `security.logging.masking-mode` | String | `PARTIAL` | 敏感数据脱敏模式 | Optional |

#### 脱敏模式说明

- `FULL`: 完全隐藏敏感字段
- `PARTIAL`: 部分显示（如 JWT Token 显示前 10 字符）
- `NONE`: 不脱敏（仅用于调试）

#### 配置示例

```properties
# 日志配置
security.logging.enabled=true
security.logging.json-output=true
security.logging.include-stack-trace=true
security.logging.include-client-ip=true
security.logging.include-user-agent=false
security.logging.include-request-id=true
security.logging.include-session-id=false
security.logging.default-level=INFO
security.logging.masking-mode=PARTIAL
```

### 追踪配置

配置组：`security.tracing.*`
必填级别：**Optional**
优先级：**P2**

分布式追踪配置，用于追踪请求链路。

#### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 级别 |
|-------|------|--------|------|------|
| `security.tracing.enabled` | Boolean | `true` | 是否启用分布式追踪 | Optional |
| `security.tracing.username-mask-length` | Integer | `3` | 用户名脱敏长度，保留前 N 个字符 | Optional |
| `security.tracing.token-mask-length` | Integer | `8` | Token 脱敏长度，保留前 N 个字符 | Optional |
| `security.tracing.record-auth-failure-details` | Boolean | `false` | 是否记录认证失败详细信息 | Optional |
| `security.tracing.propagate-to-feign` | Boolean | `true` | 是否传播追踪上下文到 Feign 调用 | Optional |
| `security.tracing.record-request-path` | Boolean | `true` | 是否记录请求路径 | Optional |
| `security.tracing.record-client-ip` | Boolean | `false` | 是否记录客户端 IP | Optional |
| `security.tracing.sampling-rate` | Float | `1.0` | 采样率（0.0-1.0） | Optional |
| `security.tracing.ignored-paths` | List<String> | `["/actuator/health", "/actuator/prometheus", "/static/", "/favicon.ico"]` | 不需要追踪的路径列表 | Optional |
| `security.tracing.login-paths` | List<String> | `["/login", "/api/login", "/auth/login", "/api/auth/login"]` | 登录路径列表 | Optional |

#### 配置示例

```properties
# 追踪配置
security.tracing.enabled=true
security.tracing.username-mask-length=3
security.tracing.token-mask-length=8
security.tracing.record-auth-failure-details=false
security.tracing.propagate-to-feign=true
security.tracing.record-request-path=true
security.tracing.record-client-ip=false
security.tracing.sampling-rate=1.0
security.tracing.ignored-paths=/actuator/health,/static/
security.tracing.login-paths=/login,/api/auth/login
```

---

## 配置示例

### 最小配置示例

适用于快速开发和验证框架功能：

```properties
# 数据库配置（必填）
spring.datasource.url=jdbc:mysql://localhost:3306/security_demo
spring.datasource.username=root
spring.datasource.password=your_password

# CORS 配置（必填，当启用时）
security.network.cors.enabled=true
security.network.cors.allowed-origins=http://localhost:8080

# 其他配置使用默认值
```

### 完整配置示例

包含所有主要功能的生产级配置：

```properties
# =================
# 数据库配置
# =================
spring.datasource.url=jdbc:mysql://prod-db.example.com:3306/security_db?sslMode=REQUIRED
spring.datasource.username=security_user
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# =================
# 基础安全配置
# =================
# 配置验证（启用）
security.config.validation=true

# 缓存配置
security.cache.maximum-size=2000
security.cache.ttl-minutes=45

# =================
# JWT 认证配置
# =================
security.jwt.secret=${JWT_SECRET:-your-secret-key-here}
security.jwt.expiration=3600

# =================
# 网络安全配置
# =================
# CORS 配置
security.network.cors.enabled=true
security.network.cors.allowed-origins=https://frontend.example.com,https://admin.example.com
security.network.cors.allowed-methods=GET,POST,PUT,DELETE
security.network.cors.allowed-headers=*,Content-Type,X-Requested-With,Authorization
security.network.cors.max-age=3600

# CSRF 配置（REST API 通常禁用）
security.network.csrf.enabled=false

# 安全响应头
security.network.headers.enabled=true
security.network.headers.frame-options=SAMEORIGIN
security.network.headers.content-type-options=true
security.network.headers.xss-protection=true
security.network.headers.hsts-max-age=31536000
security.network.headers.hsts-include-sub-domains=true

# 内容安全策略
security.network.csp.enabled=true
security.network.csp.policy=default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self'

# =================
# 会话管理配置
# =================
security.session.enabled=true
security.session.timeout=1800
security.session.max-sessions=1
security.session.store-type=redis  # 需要 spring-session-data-redis
security.session.cookie-name=JSESSIONID
security.session.fixation-protection=true

# =================
# 记住我功能配置
# =================
security.remember-me.enabled=true
security.remember-me.token-validity-seconds=604800
security.remember-me.key=${REMEMBER_ME_KEY}
security.remember-me.cookie-name=remember-me

# =================
# 监控与指标配置
# =================
# Metrics
security.metrics.enabled=true
security.metrics.authentication-success-enabled=true
security.metrics.authentication-failure-enabled=true
security.metrics.duration-percentiles=0.5,0.95,0.99
security.metrics.auth-paths=/api/auth,/auth,/login

# 健康检查
security.health.enabled=true
security.health.check-timeout-ms=5000

# 日志
security.logging.enabled=true
security.logging.json-output=true
security.logging.include-client-ip=true
security.logging.masking-mode=PARTIAL

# 追踪
security.tracing.enabled=true
security.tracing.sampling-rate=0.1
security.tracing.username-mask-length=3
security.tracing.record-client-ip=false
```

### 生产环境推荐配置

针对高安全性要求的生产环境优化配置：

```properties
# =================
# 基础配置
# =================
# 配置验证严格模式
security.config.validation=true

# 缓存配置
security.cache.maximum-size=2000
security.cache.ttl-minutes=45

# 数据库配置（使用环境变量）
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# =================
# JWT 配置（高安全性）
# =================
security.jwt.secret=${JWT_SECRET_KEY}  # 必须使用强密钥
security.jwt.expiration=3600  # 1小时，定期轮换

# =================
# 严格的网络安全配置
# =================
# 严格 CORS 配置（仅允许必要域名）
security.network.cors.enabled=true
security.network.cors.allowed-origins=https://your-domain.com
security.network.cors.allowed-methods=GET,POST
security.network.cors.allowed-headers=Authorization,Content-Type

# CSRF 启用（非 REST API）
security.network.csrf.enabled=true

# 严格的安全响应头
security.network.headers.enabled=true
security.network.headers.frame-options=SAMEORIGIN
security.network.headers.content-type-options=true
security.network.headers.xss-protection=true
security.network.headers.hsts-max-age=31536000
security.network.headers.hsts-include-sub-domains=true
security.network.headers.hsts-preload=false

# 严格的内容安全策略
security.network.csp.enabled=true
security.network.csp.policy=default-src 'self'; script-src 'none'; style-src 'self'; img-src 'self'

# =================
# 会话配置
# =================
security.session.enabled=true
security.session.timeout=900  # 15分钟
security.session.max-sessions=1
security.session.store-type=redis
security.session.fixation-protection=true

# =================
# 监控与日志
# =================
# 详细的安全监控
security.metrics.enabled=true
security.metrics.authentication-success-enabled=true
security.metrics.authentication-failure-enabled=true
security.metrics.authentication-duration-enabled=true

# 结构化日志
security.logging.enabled=true
security.logging.json-output=true
security.logging.include-stack-trace=true
security.logging.include-client-ip=false  # 出于隐私考虑
security.logging.include-user-agent=true
security.logging.masking-mode=FULL

# 分布式追踪（10% 采样）
security.tracing.enabled=true
security.tracing.sampling-rate=0.1
security.tracing.username-mask-length=3
security.tracing.token-mask-length=8
security.tracing.record-client-ip=false
```

---

## 下一步阅读

配置完成后，建议继续阅读以下文档：

- [API 参考文档](api.md)：了解所有默认端点与返回格式
- [快速开始文档](quick-start.md)：了解最小集成路径和快速验证方法
- [测试支持工具文档](testing-support.md)：为安全逻辑添加测试支持

如有配置相关问题，请参考[故障排查指南](troubleshooting-guide.md)或提交 Issue 到项目仓库。