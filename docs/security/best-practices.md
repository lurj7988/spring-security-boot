# 安全最佳实践

本文档提供 Spring Security Boot 框架的安全配置最佳实践，帮助开发者构建安全可靠的应用系统。

> 📅 **文档版本**: 2026-03-23

---

## 目录

- [密码安全](#密码安全)
- [Token 管理](#token-管理)
- [会话安全](#会话安全)
- [网络安全](#网络安全)
- [日志与审计](#日志与审计)
- [HTTPS 配置](#https-配置)
- [生产环境清单](#生产环境清单)

---

## 密码安全

### 基本原则

1. **永远不要**存储明文密码
2. 使用强加密算法（BCrypt、Argon2、PBKDF2）
3. **禁止**使用 MD5、SHA1 等弱加密算法
4. 配置合理的加密强度

### BCrypt 配置

框架默认使用 BCrypt 加密密码。

> 💡 **注意**: BCrypt 加密强度由框架内部配置决定，应用层通常无需调整。如需自定义，请参考框架源码中的 `BCryptPasswordEncoder` 配置。

### 密码复杂度要求

> 💡 **建议**: 在应用层实现密码复杂度验证

推荐的密码复杂度规则：
- 最小长度：8 位（推荐 12 位）
- 包含大写字母
- 包含小写字母
- 包含数字
- 包含特殊字符（!@#$%^&*）

#### 密码验证器示例

> 💡 **注意**: 以下示例代码需要应用自行实现，框架不提供此验证器。

```java
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*]");

    public static ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null) {
            errors.add("密码不能为空");
            return new ValidationResult(false, errors);
        }

        if (password.length() < MIN_LENGTH) {
            errors.add("密码长度至少 " + MIN_LENGTH + " 位");
        }
        if (!UPPER_CASE.matcher(password).find()) {
            errors.add("密码必须包含大写字母");
        }
        if (!LOWER_CASE.matcher(password).find()) {
            errors.add("密码必须包含小写字母");
        }
        if (!DIGIT.matcher(password).find()) {
            errors.add("密码必须包含数字");
        }
        if (!SPECIAL.matcher(password).find()) {
            errors.add("密码必须包含特殊字符");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

### 常见密码黑名单

> ⚠️ **建议**: 检查用户密码是否在常见密码黑名单中

禁止使用的密码示例：
- `123456`, `password`, `admin`, `root`
- 用户名、邮箱地址
- 常见单词和短语
- 键盘序列（`qwerty`, `asdfgh`）

---

## Token 管理

### JWT 配置

```properties
# JWT 配置
# 注意：密钥必须是 BASE64 编码的字符串
security.jwt.secret=eW91ci0yNTYtYml0LXNlY3JldC1rZXktaGVyZQ==  # BASE64 编码的密钥
security.jwt.expiration=3600          # Access Token 有效期：60分钟
```

> ⚠️ **重要**: JWT 密钥必须是 **BASE64 编码**的字符串。使用明文密钥会导致启动失败。

### 密钥管理

> ⚠️ **重要**: JWT 密钥是系统安全的核心

```properties
# ❌ 错误：使用默认值或简单密钥
security.jwt.secret=secret
security.jwt.secret=123456

# ✅ 正确：使用足够长度的随机密钥
security.jwt.secret=your-very-long-and-secure-secret-key-at-least-256-bits
```

#### 生成安全密钥

```bash
# 使用 OpenSSL 生成 256 位密钥（推荐）
openssl rand -base64 32

# 使用 Java SecureRandom（密码学安全）
# 在应用启动时通过环境变量传入，或在配置中心管理
```

### Token 有效期建议

| Token 类型 | 建议有效期 | 说明 |
|-----------|-----------|------|
| Access Token | 15-60 分钟 | 短期有效，减少泄露风险 |
| Refresh Token | 7-30 天 | 用于刷新 Access Token |

### Token 刷新策略

```java
// 推荐的刷新策略
// 1. Access Token 过期前 5 分钟可刷新
// 2. Refresh Token 使用后轮换（生成新的 Refresh Token）
// 3. Refresh Token 可被撤销
```

### Token 安全传输

- **必须**通过 HTTPS 传输
- **不要**在 URL 中传递 Token
- **建议**使用 `Authorization: Bearer <token>` 请求头

---

## 会话安全

### Session 配置

```properties
# Session 配置
server.servlet.session.timeout=30m     # 会话超时：30分钟
# 并发会话控制需在 SecurityConfiguration 中配置，参考框架文档
```

**配置项有效值范围**:

| 配置项 | 有效值 | 说明 |
|--------|--------|------|
| `server.servlet.session.timeout` | > 0 | 建议生产环境 15-30 分钟 |
| `security.jwt.expiration` | > 0 | 建议生产环境 15-60 分钟（单位：秒） |
| `maximumSessions` | 1 或更大 | 在 SecurityConfiguration 中配置 |

### Remember Me 配置

```properties
# Remember Me 配置
security.remember-me.enabled=true
security.remember-me.key=your-remember-me-key
security.remember-me.token-validity=604800  # 7天
```

### 会话安全最佳实践

1. **会话超时**: 设置合理的超时时间（建议 15-30 分钟）
2. **会话固定保护**: 启用会话固定保护
3. **并发会话控制**: 限制同一用户的并发会话数
4. **安全 Cookie**: 配置 Cookie 安全属性

```properties
# Cookie 安全配置
server.servlet.session.cookie.http-only=true   # 防止 XSS 窃取
server.servlet.session.cookie.secure=true      # 仅 HTTPS 传输
server.servlet.session.cookie.same-site=strict # 防止 CSRF
```

---

## 网络安全

### 认证方式选择指南

> 💡 **重要**: 根据应用架构选择合适的认证方式

| 认证方式 | 适用场景 | CSRF 建议 |
|----------|----------|-----------|
| **JWT（无状态）** | 前后端分离、API 服务、微服务 | **可禁用** CSRF（无 Cookie Session） |
| **Session（有状态）** | 传统 Web 应用、服务端渲染 | **必须启用** CSRF |
| **混合模式** | 同时支持多种客户端 | 根据端点类型分别配置 |

```java
// JWT 模式（无状态）：可禁用 CSRF
http.csrf().disable()
    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

// Session 模式（有状态）：必须启用 CSRF
http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .and()
    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
```

### CORS 配置

```properties
# CORS 配置
security.network.cors.enabled=true
# ⚠️ 生产环境必须配置具体的域名，不要使用 *
security.network.cors.allowed-origins=https://your-domain.com,https://admin.your-domain.com
security.network.cors.allowed-methods=GET,POST,PUT,DELETE
security.network.cors.allowed-headers=Authorization,Content-Type,X-Requested-With
```

> 💡 **注意**: CORS 配置项以框架实际支持为准，具体参考 `configuration.md`。

> ⚠️ **警告**: 生产环境**禁止**使用 `allowed-origins=*`

### CSRF 配置

```properties
# CSRF 配置
security.network.csrf.enabled=true
security.network.csrf.token-header=X-CSRF-TOKEN
```

#### 前端 CSRF Token 处理

```javascript
// 从 Cookie 或响应头获取 CSRF Token
const csrfToken = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN='))
    ?.split('=')[1];

// 在请求头中携带
fetch('/api/resource', {
    method: 'POST',
    headers: {
        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
});
```

### XSS 防护

框架自动配置以下 XSS 防护头：

```
X-XSS-Protection: 1; mode=block
X-Content-Type-Options: nosniff
Content-Security-Policy: default-src 'self'
```

```properties
# 安全响应头配置
security.network.headers.enabled=true
# CSP 配置示例（根据实际需求调整）
# security.network.headers.content-security-policy=default-src 'self'
```

> ⚠️ **安全警告**: 避免在 CSP 中使用 `'unsafe-inline'`，这会禁用对内联脚本的保护。建议使用 nonce 或 hash 方式代替。

### 安全响应头

框架自动配置的响应头：

| 响应头 | 值 | 说明 |
|--------|-----|------|
| X-Frame-Options | DENY | 防止点击劫持 |
| X-Content-Type-Options | nosniff | 防止 MIME 类型嗅探 |
| X-XSS-Protection | 1; mode=block | XSS 过滤 |
| Strict-Transport-Security | max-age=31536000 | 强制 HTTPS |

---

## 日志与审计

### 日志配置

```properties
# 日志级别
logging.level.com.original.security=INFO
logging.level.org.springframework.security=INFO

# 日志格式（结构化）
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### 敏感数据脱敏

> ⚠️ **重要**: 日志中**禁止**记录以下信息
- 用户密码
- JWT Token 完整内容
- 身份证号、银行卡号
- 其他 PII（个人身份信息）

```java
// 正确的日志记录方式
log.info("用户登录成功: username={}", username);
log.info("Token 生成: token={}", maskToken(token));

// ❌ 错误的日志记录方式
log.info("用户登录: password={}", password);  // 禁止！
log.info("Token: {}", fullToken);              // 禁止！
```

### 审计事件

> 💡 **注意**: 以下示例代码需要应用自行实现 `AuditLog` 实体和存储逻辑，框架只发布事件。

```java
@Component
@Slf4j
public class SecurityAuditListener {

    // 应用需要自行注入审计日志存储服务
    // private final AuditLogService auditLogService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (event == null || event.getAuthentication() == null) {
            return;
        }

        String username = event.getAuthentication().getName();
        log.info("审计日志: 用户 {} 于 {} 登录成功", username, LocalDateTime.now());

        // 应用需要将日志存储到持久化存储
        // AuditLog auditLog = AuditLog.builder()
        //     .eventType("AUTHENTICATION_SUCCESS")
        //     .username(username)
        //     .timestamp(LocalDateTime.now())
        //     .build();
        // auditLogService.save(auditLog);
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureEvent event) {
        if (event == null || event.getAuthentication() == null) {
            return;
        }

        String username = event.getAuthentication().getPrincipal() instanceof String
            ? (String) event.getAuthentication().getPrincipal()
            : "unknown";

        String reason = event.getException() != null
            ? event.getException().getMessage()
            : "unknown";

        log.warn("审计日志: 用户 {} 登录失败，原因: {}", username, reason);
    }
}
```

---

## HTTPS 配置

### 开发环境

```properties
# 开发环境可使用自签名证书
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
```

### 生产环境

```properties
# 生产环境 HTTPS 配置
server.ssl.enabled=true
server.ssl.protocol=TLSv1.2
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3

# Cookie 安全配置
server.servlet.session.cookie.secure=true
```

> 💡 **注意**: 如需强制 HTTPS 重定向，请在 `SecurityConfiguration` 中配置：
> ```java
> http.requiresChannel().anyRequest().requiresSecure();
> ```

### 证书配置

推荐使用受信任的 CA 证书：
- [Let's Encrypt](https://letsencrypt.org/)（免费）
- 阿里云、腾讯云等云服务商证书

---

## 生产环境清单

### 启动前必须检查

- [ ] **JWT 密钥**已配置（不使用默认值）
- [ ] **数据库密码**已修改
- [ ] **CORS 域名**已限制（不使用 `*`）
- [ ] **HTTPS**已启用
- [ ] **敏感配置**已加密

### 安全配置清单

```properties
# ===== 生产环境安全配置 =====

# JWT 安全（密钥必须是 BASE64 编码）
security.jwt.secret=${JWT_SECRET}  # 从环境变量读取
security.jwt.expiration=3600

# HTTPS
server.ssl.enabled=true

# Session 安全
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# CORS（限制具体域名）
security.network.cors.enabled=true
security.network.cors.allowed-origins=https://your-domain.com

# CSRF
security.network.csrf.enabled=true

# 安全响应头
security.network.headers.enabled=true

# 日志
logging.level.com.original.security=WARN
```

> 💡 **注意**:
> - JWT 密钥必须是 **BASE64 编码**
> - 并发会话控制需要在 `SecurityConfiguration` 中通过代码配置
> - 具体配置项以框架实际支持为准，参考 `configuration.md`

### 定期安全检查

- [ ] 每月进行**依赖漏洞扫描**
- [ ] 每季度审查**密码策略**
- [ ] 每季度审查**Token 有效期**
- [ ] 每半年审查**访问权限**
- [ ] 每年进行**安全渗透测试**

---

## 参考资源

- [等保 2.0 合规清单](compliance.md) - 等保合规要求
- [安全配置检查清单](checklist.md) - 启动前/部署前检查
- [安全 FAQ](faq.md) - 常见问题解答
- [配置参考文档](../configuration.md) - 完整配置项说明

---

**文档最后更新：** 2026-03-23
