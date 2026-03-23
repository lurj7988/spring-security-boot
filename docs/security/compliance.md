# 等保 2.0 合规清单

本文档提供 Spring Security Boot 框架针对**网络安全等级保护 2.0（等保 2.0）二级**要求的合规清单，帮助开发者快速了解框架支持的安全能力和需要自行配置的项目。

> 📅 **文档版本**: 2026-03-23
> 🎯 **适用标准**: GB/T 22239-2019《信息安全技术 网络安全等级保护基本要求》

---

## 概述

### 重要声明

> ⚠️ **注意**: Spring Security Boot 是一个**安全框架**，不是安全产品。框架提供安全能力（加密、认证、授权等），但**不保证**使用框架的应用自动通过等保认证。应用需要正确配置和使用这些能力才能满足合规要求。

### 如何使用本清单

- ✅ **框架已实现** - 框架提供的能力，开箱即用或简单配置即可
- 🔧 **应用需配置** - 框架提供支持，但需要应用进行具体配置
- 📋 **应用需实现** - 需要应用自行实现业务逻辑

---

## 一、安全物理环境

> 📋 本节要求主要涉及机房物理安全，属于基础设施层面，**框架不提供相关能力**。

| 要求项 | 框架支持 | 说明 |
|--------|----------|------|
| 机房安全 | ❌ 不适用 | 基础设施层面 |
| 通信线路安全 | ❌ 不适用 | 基础设施层面 |

---

## 二、安全通信网络

### 2.1 网络架构

| 要求项 | 框架支持 | 说明 |
|--------|----------|------|
| 网络分区 | 📋 应用需实现 | 部署架构层面 |
| 通信传输加密 | 🔧 应用需配置 | 配置 HTTPS（见 [best-practices.md](best-practices.md#https-配置)）|

### 2.2 通信传输

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **数据传输完整性** | 🔧 应用需配置 | |
| 使用 HTTPS | 🔧 应用需配置 | `server.ssl.enabled=true` |
| 强制 SSL | 🔧 应用需配置 | 在 SecurityConfiguration 中配置 |
| **数据传输保密性** | 🔧 应用需配置 | |
| TLS 1.2+ | 🔧 应用需配置 | `server.ssl.protocol=TLSv1.2` |

```properties
# 推荐配置：启用 HTTPS
server.ssl.enabled=true
server.ssl.protocol=TLSv1.2
```

> 💡 **注意**: 如需强制 HTTPS 重定向，请在 `SecurityConfiguration` 中配置 `.requiresChannel().anyRequest().requiresSecure()`。

---

## 三、安全区域边界

### 3.1 边界防护

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **边界访问控制** | 📋 应用需实现 | 网络设备层面 |
| **CORS 配置** | ✅ 框架已实现 | 框架自动配置 CORS 策略 |
| **CSRF 防护** | ✅ 框架已实现 | 框架自动启用 CSRF 保护 |

```properties
# CORS 配置（框架支持，应用需配置允许的域名）
security.network.cors.enabled=true
security.network.cors.allowed-origins=https://your-domain.com

# CSRF 配置（默认启用）
security.network.csrf.enabled=true
```

### 3.2 访问控制

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **XSS 防护** | ✅ 框架已实现 | 框架自动配置 XSS 防护头 |
| **安全响应头** | ✅ 框架已实现 | 框架自动配置安全响应头 |

框架自动配置的安全响应头：
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000`

```properties
# 安全响应头配置（默认启用）
security.network.headers.enabled=true
```

### 3.3 入侵防范

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **请求频率限制** | 🔧 应用需配置 | 可通过配置实现 |
| **登录失败锁定** | 🔧 应用需配置 | 实现登录失败次数限制 |

---

## 四、安全计算环境

### 4.1 身份鉴别 ⭐

> 这是等保 2.0 的核心要求之一

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **身份标识唯一性** | ✅ 框架已实现 | 用户名唯一约束 |
| **身份鉴别信息复杂度** | 🔧 应用需配置 | 配置密码复杂度规则 |
| **身份鉴别信息防窃听** | 🔧 应用需配置 | 使用 HTTPS 传输 |
| **登录失败处理** | 🔧 应用需配置 | 配置登录失败锁定策略 |
| **鉴别信息有效期** | ✅ 框架已实现 | JWT Token 过期机制 |

#### 框架支持的认证方式

| 认证方式 | 支持状态 | 配置说明 |
|----------|----------|----------|
| 用户名密码 | ✅ 已实现 | 默认支持 |
| JWT Token | ✅ 已实现 | 前后端分离推荐 |
| Session | ✅ 已实现 | 传统 Web 应用 |
| Remember Me | ✅ 已实现 | 7天免登录 |
| HTTP Basic | ✅ 已实现 | 简单认证场景 |

#### 密码安全配置

```properties
# BCrypt 加密由框架自动处理
# 密码复杂度规则需要在应用层实现
```

**建议的密码复杂度规则**:
- 最小长度：8位（推荐 12位）
- 包含大小写字母
- 包含数字
- 包含特殊字符

### 4.2 访问控制 ⭐

> 这是等保 2.0 的核心要求之一

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **访问控制策略** | 🔧 应用需配置 | 定义具体权限规则 |
| **访问控制粒度** | ✅ 框架已实现 | 支持方法级权限控制 |
| **主体客体标记** | 🔧 应用需配置 | 定义角色和权限标签 |

#### 框架支持的授权机制

| 授权机制 | 支持状态 | 配置说明 |
|----------|----------|----------|
| @PreAuthorize | ✅ 已实现 | 方法级权限控制 |
| @Secured | ✅ 已实现 | 角色控制 |
| RBAC | ✅ 已实现 | 基于角色的访问控制 |
| 权限注解 | ✅ 已实现 | SpEL 表达式 |

#### 权限控制示例

```java
// 角色控制
@PreAuthorize("hasRole('ADMIN')")
public void adminOperation() { }

// 权限控制
@PreAuthorize("hasAuthority('user:write')")
public void updateUser() { }

// 组合条件
@PreAuthorize("hasRole('ADMIN') or hasAuthority('user:manage')")
public void manageUsers() { }
```

### 4.3 安全审计 ⭐

> 这是等保 2.0 的核心要求之一

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **审计记录** | ✅ 框架已实现 | 框架发布审计事件 |
| **审计日志存储** | 📋 应用需实现 | 应用需存储审计日志 |
| **审计日志保护** | 📋 应用需实现 | 应用需保护日志完整性 |
| **审计日志分析** | 📋 应用需实现 | 应用需进行日志分析 |

#### 框架提供的审计事件

| 事件类型 | 事件名称 | 说明 |
|----------|----------|------|
| 认证成功 | AuthenticationSuccessEvent | 用户登录成功 |
| 认证失败 | AuthenticationFailureEvent | 用户登录失败 |
| 授权失败 | AuthorizationFailureEvent | 访问无权限资源 |
| 会话创建 | SessionCreatedEvent | 用户会话创建 |
| 会话销毁 | SessionDestroyedEvent | 用户会话销毁 |

#### 审计事件监听配置

```java
@Component
public class SecurityAuditListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditListener.class);

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (event == null || event.getAuthentication() == null) {
            return;
        }
        String username = event.getAuthentication().getName();
        String timestamp = LocalDateTime.now().toString();
        log.info("审计日志: 用户 {} 于 {} 登录成功", username, timestamp);
        // 应用需将日志存储到持久化存储
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

> ⚠️ **重要**: 等保要求审计日志保存时间 **≥ 6 个月**

### 4.4 数据完整性

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **数据传输完整性** | 🔧 应用需配置 | 使用 HTTPS |
| **JWT 签名验证** | ✅ 框架已实现 | 框架自动验证 JWT 签名 |
| **防重放攻击** | 🔧 应用需配置 | 配置 Token 有效期 |

```properties
# JWT Token 配置
# 注意：密钥必须是 BASE64 编码
security.jwt.secret=eW91ci0yNTYtYml0LXNlY3JldC1rZXktaGVyZQ==
security.jwt.expiration=3600  # 60分钟
```

> ⚠️ **重要**: JWT 密钥必须是 **BASE64 编码**的字符串。

### 4.5 数据保密性

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **密码加密存储** | ✅ 框架已实现 | BCrypt 加密 |
| **敏感数据脱敏** | ✅ 框架已实现 | 日志脱敏 API |
| **数据传输加密** | 🔧 应用需配置 | 使用 HTTPS |

#### 密码安全

框架使用 **BCrypt** 算法加密密码：

```java
// 密码加密（框架自动处理）
String encodedPassword = passwordEncoder.encode(rawPassword);

// 密码验证（框架自动处理）
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

> ⚠️ **禁止**使用 MD5、SHA1 等弱加密算法存储密码

#### 敏感数据脱敏

```java
// 日志脱敏示例
log.info("用户登录: token={}", maskToken(token));
log.info("用户信息: password={}", "***");

private String maskToken(String token) {
    if (token == null || token.length() < 20) {
        return "***";
    }
    return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
}
```

### 4.6 数据备份恢复

| 要求项 | 框架支持 | 说明 |
|--------|----------|------|
| 数据备份 | 📋 应用需实现 | 数据库层面 |
| 数据恢复 | 📋 应用需实现 | 数据库层面 |

---

## 五、安全管理中心

### 5.1 系统管理

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **系统状态监控** | ✅ 框架已实现 | Actuator 健康检查 |
| **资源监控** | ✅ 框架已实现 | Micrometer Metrics |

```properties
# Actuator 配置
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
```

### 5.2 安全管理

| 要求项 | 框架支持 | 配置说明 |
|--------|----------|----------|
| **安全事件告警** | 🔧 应用需配置 | 配置告警规则 |
| **安全策略管理** | 🔧 应用需配置 | 定义安全策略 |

---

## 合规检查清单

### 框架已实现的功能

- [x] CORS 自动配置
- [x] CSRF 防护
- [x] XSS 防护
- [x] 安全响应头
- [x] BCrypt 密码加密
- [x] JWT Token 签名验证
- [x] Token 过期机制
- [x] 审计事件发布
- [x] 结构化日志
- [x] 健康检查端点
- [x] Metrics 指标
- [x] @PreAuthorize 权限控制
- [x] RBAC 模型支持

### 应用需要配置的项目

- [ ] 配置 HTTPS（生产环境必须）
- [ ] 配置 JWT 密钥（不使用默认值）
- [ ] 配置 CORS 允许的域名
- [ ] 实现密码复杂度验证
- [ ] 实现登录失败锁定策略
- [ ] 实现审计日志存储（≥6个月）
- [ ] 配置安全事件告警
- [ ] 定期进行依赖漏洞扫描
- [ ] 定期进行安全审计

---

## 参考资源

- [安全最佳实践](best-practices.md) - 详细的配置指南
- [安全配置检查清单](checklist.md) - 启动前/部署前检查
- [安全 FAQ](faq.md) - 常见问题解答
- [配置参考文档](../configuration.md) - 完整配置项说明

---

**文档最后更新：** 2026-03-23
