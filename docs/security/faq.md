# 安全常见问题 (FAQ)

本文档收集了 Spring Security Boot 框架使用过程中的常见安全问题及解答。

> 📅 **文档版本**: 2026-03-23

---

## 目录

- [密码与认证](#密码与认证)
- [Token 管理](#token-管理)
- [会话安全](#会话安全)
- [网络安全](#网络安全)
- [审计与日志](#审计与日志)
- [生产部署](#生产部署)
- [漏洞响应](#漏洞响应)

---

## 密码与认证

### Q1: 如何配置密码复杂度要求？

**A**: 框架使用 BCrypt 加密密码，但密码复杂度验证需要在应用层自行实现。

> 💡 **注意**: 以下示例代码需要应用自行实现，框架不提供此验证器。

```java
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    public static void validate(String password) {
        if (password == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("密码长度至少 " + MIN_LENGTH + " 位");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("密码必须包含大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("密码必须包含小写字母");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("密码必须包含数字");
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            throw new IllegalArgumentException("密码必须包含特殊字符");
        }
    }
}
```

**推荐配置**:
- 最小长度: 8-12 位
- 包含大小写字母
- 包含数字
- 包含特殊字符

> 📖 参考: [best-practices.md](best-practices.md#密码安全)

---

### Q2: 如何实现登录失败锁定功能？

**A**: 可以通过实现 `AuthenticationFailureListener` 来记录登录失败次数，并在达到阈值时锁定账号。

> 💡 **注意**: 以下示例代码需要应用自行实现 `AccountLockService`，框架不提供此功能。建议使用缓存库（如 Caffeine）实现带过期机制的锁定存储。

```java
@Component
public class LoginFailureHandler {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    // 应用需要自行实现 AccountLockService，建议使用缓存库（如 Caffeine）
    // private final AccountLockService accountLockService;

    public void recordFailure(String username) {
        // 示例：使用缓存记录失败次数
        // int count = accountLockService.incrementFailureCount(username);
        // if (count >= MAX_FAILURES) {
        //     accountLockService.lock(username, LOCK_DURATION_MINUTES);
        //     log.warn("账号 {} 因连续登录失败已被锁定 {} 分钟", username, LOCK_DURATION_MINUTES);
        // }
    }

    public boolean isLocked(String username) {
        // 检查账号是否被锁定
        // return accountLockService.isLocked(username);
        return false;
    }
}
```

**配置要点**:
- 最大失败次数: 建议 5 次
- 锁定时长: 建议 15-30 分钟
- **必须**使用带过期机制的存储（如 Redis、Caffeine Cache），避免内存泄漏
- 记录审计日志

---

### Q3: 框架支持哪些认证方式？

**A**: 框架支持以下认证方式：

| 认证方式 | 适用场景 | 配置说明 |
|----------|----------|----------|
| 用户名密码 | 传统 Web 应用 | 默认支持 |
| JWT Token | 前后端分离、API | 推荐 |
| Session | 传统 Web 应用 | 支持有状态 |
| Remember Me | 延长登录状态 | 7天免登录 |
| HTTP Basic | 简单认证场景 | 不推荐生产使用 |

> 📖 参考: [configuration.md](../configuration.md#jwt-认证配置)

---

## Token 管理

### Q4: JWT Token 过期时间应该设置多长？

**A**: 根据安全性和用户体验平衡考虑：

| Token 类型 | 建议有效期 | 原因 |
|-----------|-----------|------|
| Access Token | 15-60 分钟 | 短期有效，减少泄露风险 |
| Refresh Token | 7-30 天 | 用于刷新 Access Token（需应用自行实现） |

```properties
# 推荐配置
security.jwt.expiration=3600          # Access Token: 60分钟
# 注意：Refresh Token 机制需要应用自行实现
```

> 💡 **注意**: 框架当前只支持 Access Token 过期配置，Refresh Token 机制需要应用层自行实现。

**计算方法**:
- 高安全场景（金融）: 15 分钟
- 一般业务场景: 30-60 分钟
- 低风险场景: 可适当延长

---

### Q5: JWT 密钥应该如何管理？

**A**: JWT 密钥管理是安全的核心：

**✅ 正确做法**:
```properties
# 1. 使用 BASE64 编码的密钥（必须）
security.jwt.secret=eW91ci12ZXJ5LWxvbmctYW5kLXNlY3VyZS1zZWNyZXQta2V5

# 2. 从环境变量读取（推荐）
security.jwt.secret=${JWT_SECRET}
```

**❌ 错误做法**:
```properties
# 不要使用明文密钥（会导致启动失败）
security.jwt.secret=your-plain-text-secret

# 不要使用简单密钥
security.jwt.secret=secret
security.jwt.secret=123456
security.jwt.secret=admin
```

**密钥生成方法**:
```bash
# 使用 OpenSSL 生成 BASE64 编码的密钥（推荐）
openssl rand -base64 32

# 生成的密钥直接配置到 application.properties 或通过环境变量传入
```

> ⚠️ **重要**:
> - 密钥必须是 **BASE64 编码**的字符串，使用明文会导致启动失败
> - 密钥泄露后需要立即更换，并使所有 Token 失效

---

### Q6: Token 泄露后如何处理？

**A**: Token 泄露后的应急处理流程：

1. **立即更换密钥**
```bash
# 生成新密钥
openssl rand -base64 32
# 更新配置并重启服务
```

2. **使所有 Token 失效**
```java
// 清除所有会话
sessionRegistry.getAllSessions().forEach(SessionInformation::expireNow);
```

3. **通知用户重新登录**

4. **排查泄露原因**
   - 检查日志是否有异常访问
   - 检查是否存在 XSS 漏洞
   - 检查 HTTPS 是否正确配置

---

## 会话安全

### Q7: 如何配置会话超时？

**A**: 在 `application.properties` 中配置：

```properties
# Session 超时配置
server.servlet.session.timeout=30m  # 30分钟

# Cookie 配置
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
```

**超时时间建议**:
- 高安全场景: 15-30 分钟
- 一般业务场景: 30-60 分钟
- Remember Me: 7 天

---

### Q8: 如何限制同一用户的并发登录数？

**A**: 框架支持并发会话控制，需要在 `SecurityConfiguration` 中配置：

```java
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
            .maximumSessions(1)  // 每个用户只能有1个会话
            .maxSessionsPreventsLogin(false);  // true: 禁止新登录; false: 踢出旧会话
    }
}
```

当用户在另一设备登录时：
- `false`（默认）: 踢出旧会话，新会话生效
- `true`: 禁止新登录，旧会话保持

---

## 网络安全

### Q9: CORS 配置允许所有域名会有什么风险？

**A**: 使用 `allowed-origins=*` 存在以下风险：

1. **CSRF 攻击风险增加**
2. **敏感数据泄露**
3. **无法验证请求来源**

**✅ 正确配置**:
```properties
# 生产环境必须指定具体域名
security.network.cors.allowed-origins=https://your-domain.com,https://admin.your-domain.com
```

**❌ 错误配置**:
```properties
# 生产环境禁止使用
security.network.cors.allowed-origins=*
```

> 📖 参考: [best-practices.md](best-practices.md#cors-配置)

---

### Q10: 为什么 CSRF Token 验证失败？

**A**: CSRF Token 验证失败的常见原因：

1. **Token 未携带**
   - 前端未在请求头中添加 Token
   - Cookie 中的 Token 丢失

2. **Token 过期**
   - Session 过期导致 Token 失效

3. **Token 不匹配**
   - Cookie 中的 Token 与请求头不一致

**解决方案**:

```javascript
// 前端正确处理 CSRF Token
// 1. 从 Cookie 获取 Token
const csrfToken = getCookie('XSRF-TOKEN');

// 2. 在请求头中携带
fetch('/api/resource', {
    method: 'POST',
    headers: {
        'X-CSRF-TOKEN': csrfToken,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
});
```

**后端配置**:
```properties
security.network.csrf.enabled=true
security.network.csrf.token-header=X-CSRF-TOKEN
```

---

### Q11: 如何防止 XSS 攻击？

**A**: 框架已自动配置 XSS 防护，但应用层也需要注意：

**框架自动防护**:
- `X-XSS-Protection: 1; mode=block`
- `X-Content-Type-Options: nosniff`
- `Content-Security-Policy`（可配置）

**应用层注意事项**:

1. **输入验证**
> ⚠️ **警告**: 以下仅是简化示例，**不要**在生产环境中直接使用。建议使用成熟的库如 [OWASP ESAPI](https://owasp.org/www-project-enterprise-security-api/) 或 [AntiSamy](https://owasp.org/www-project-antisamy/) 进行输入净化。

```java
// 简化示例 - 生产环境请使用 OWASP ESAPI
public String sanitizeInput(String input) {
    if (input == null) return null;
    return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;");
}
```

2. **输出编码**
```html
<!-- 使用 Thymeleaf 自动编码 -->
<span th:text="${userInput}"></span>
```

3. **CSP 配置**
```properties
security.network.headers.content-security-policy=default-src 'self'; script-src 'self'
```

---

## 审计与日志

### Q12: 审计日志需要保存多长时间？

**A**: 根据等保 2.0 要求：

- **等保二级**: 审计日志保存 **≥ 6 个月**
- **等保三级**: 审计日志保存 **≥ 6 个月**

```java
// 审计日志存储示例
@EventListener
public void onAuditEvent(AuditEvent event) {
    AuditLog log = AuditLog.builder()
        .eventType(event.getType())
        .username(event.getUsername())
        .timestamp(LocalDateTime.now())
        .details(event.getDetails())
        .build();

    auditLogRepository.save(log);
}
```

**日志内容建议**:
- 事件类型
- 操作用户
- 时间戳
- 来源 IP
- 操作结果
- 详细信息

---

### Q13: 日志中可以记录密码吗？

**A**: **绝对禁止**在日志中记录密码！

**❌ 禁止记录的敏感信息**:
- 用户密码（明文或加密后）
- JWT Token 完整内容
- 身份证号、银行卡号
- API 密钥

**✅ 正确的日志记录**:
```java
// 用户登录
log.info("用户登录: username={}, result={}", username, "success");

// Token 生成
log.info("Token 生成: username={}, token={}", username, maskToken(token));

private String maskToken(String token) {
    if (token == null || token.length() < 20) return "***";
    return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
}
```

---

## 生产部署

### Q14: 生产环境必须配置哪些安全项？

**A**: 生产环境安全配置清单：

```properties
# ===== 生产环境安全配置 =====

# 1. JWT 密钥（必须，且必须是 BASE64 编码）
security.jwt.secret=${JWT_SECRET}

# 2. HTTPS（必须）
server.ssl.enabled=true

# 3. Session 安全
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true

# 4. CORS（必须限制域名）
security.network.cors.allowed-origins=https://your-domain.com

# 5. CSRF
security.network.csrf.enabled=true

# 6. 安全响应头
security.network.headers.enabled=true
```

> 📖 完整清单: [checklist.md](checklist.md)
> 💡 **注意**: 具体配置项以框架实际支持为准，参考 `configuration.md`。

---

### Q15: 如何进行依赖漏洞扫描？

**A**: 使用 OWASP Dependency-Check 或 Maven 插件：

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.2.1</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

**运行扫描**:
```bash
mvn dependency-check:check
```

**扫描频率建议**:
- 每次发布前: 必须
- CI/CD 流水线: 自动
- 定期扫描: 每月

---

## 漏洞响应

### Q16: 发现安全漏洞如何报告？

**A**: 如果发现框架安全漏洞，请通过以下方式报告：

1. **不要**公开披露漏洞
2. 发送邮件到: security@your-project.com
3. 包含以下信息:
   - 漏洞描述
   - 复现步骤
   - 影响范围
   - 可能的修复方案

**响应时间**:
| 漏洞等级 | 响应时间 | 修复时间 |
|---------|---------|---------|
| 🔴 高危 | 24 小时内 | 48 小时内 |
| 🟡 中危 | 72 小时内 | 1 周内 |
| 🟢 低危 | 1 周内 | 2 周内 |

---

### Q17: 如何处理依赖漏洞告警？

**A**: 处理依赖漏洞的流程：

1. **评估风险**
   - 查看漏洞 CVE 等级
   - 确认是否影响当前使用

2. **确认影响范围**
   - 检查依赖的使用方式
   - 确认是否存在利用路径

3. **采取行动**
   - 高危: 立即升级或替换
   - 中危: 尽快升级
   - 低危: 计划升级

4. **验证修复**
   - 升级后进行测试
   - 重新运行漏洞扫描

---

## 参考资源

- [等保 2.0 合规清单](compliance.md) - 等保合规要求
- [安全最佳实践](best-practices.md) - 详细配置指南
- [安全配置检查清单](checklist.md) - 启动前/部署前检查
- [故障排查指南](../troubleshooting.md) - 常见问题解决
- [配置参考文档](../configuration.md) - 完整配置项说明

---

**文档最后更新：** 2026-03-23
