# 安全配置检查清单

本文档提供 Spring Security Boot 框架的安全配置检查清单，用于确保系统在启动前、生产部署时和定期审计时满足安全要求。

> 📅 **文档版本**: 2026-03-23

---

## 使用说明

- ✅ 表示已完成或确认
- ❌ 表示未完成或存在问题
- 🔧 表示需要配置

---

## 一、启动前检查清单

### 1.1 密钥与密码

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 🔲 JWT 密钥已配置（不使用默认值） | ⬜ | 密钥长度 ≥ 256 位 |
| 🔲 JWT 密钥从环境变量读取 | ⬜ | `security.jwt.secret=${JWT_SECRET}` |
| 🔲 数据库密码已修改 | ⬜ | 不使用默认密码 |
| 🔲 Remember Me 密钥已配置 | ⬜ | 自定义密钥 |

### 1.2 认证配置

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 🔲 密码复杂度验证已实现 | ⬜ | 应用层实现 |
| 🔲 登录失败锁定已配置 | ⬜ | 防暴力破解（需应用实现） |

### 1.3 网络安全

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 🔲 CORS 域名已限制 | ⬜ | 不使用 `*` |
| 🔲 CSRF 保护已启用 | ⬜ | 默认启用 |
| 🔲 安全响应头已启用 | ⬜ | 默认启用 |

### 1.4 日志与监控

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 🔲 敏感数据不在日志中记录 | ⬜ | 密码、Token 等 |
| 🔲 审计事件监听已配置 | ⬜ | 登录/登出/授权失败 |
| 🔲 健康检查端点已配置 | ⬜ | Actuator |

---

## 二、生产部署检查清单

### 2.1 HTTPS 配置 ⭐

| 检查项 | 状态 | 配置 |
|--------|------|------|
| 🔲 HTTPS 已启用 | ⬜ | `server.ssl.enabled=true` |
| 🔲 TLS 版本 ≥ 1.2 | ⬜ | `server.ssl.protocol=TLSv1.2` |
| 🔲 HSTS 已启用 | ⬜ | 框架自动配置 `Strict-Transport-Security` 头 |
| 🔲 Cookie Secure 属性 | ⬜ | `server.servlet.session.cookie.secure=true` |

```properties
# 生产环境 HTTPS 配置
server.ssl.enabled=true
server.ssl.protocol=TLSv1.2
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
server.servlet.session.cookie.secure=true
```

> 💡 **注意**: 如需强制 HTTPS 重定向，请在 `SecurityConfiguration` 中配置 `.requiresChannel().anyRequest().requiresSecure()`。

### 2.2 JWT 安全 ⭐

| 检查项 | 状态 | 配置 |
|--------|------|------|
| 🔲 密钥是 BASE64 编码 | ⬜ | 使用 `openssl rand -base64 32` 生成 |
| 🔲 密钥长度 ≥ 256 位 | ⬜ | 使用强密钥 |
| 🔲 Access Token 有效期 15-60 分钟 | ⬜ | `security.jwt.expiration=3600` |

> ⚠️ **重要**: JWT 密钥必须是 **BASE64 编码**的字符串，使用明文会导致启动失败。

### 2.3 会话安全

| 检查项 | 状态 | 配置 |
|--------|------|------|
| 🔲 会话超时 ≤ 30 分钟 | ⬜ | `server.servlet.session.timeout=30m` |
| 🔲 Cookie HttpOnly 已启用 | ⬜ | `server.servlet.session.cookie.http-only=true` |
| 🔲 Cookie SameSite 已配置 | ⬜ | `server.servlet.session.cookie.same-site=strict` |
| 🔲 并发会话已限制 | ⬜ | 在 SecurityConfiguration 中配置 `.maximumSessions(1)` |

### 2.4 CORS 配置 ⭐

| 检查项 | 状态 | 配置 |
|--------|------|------|
| 🔲 允许的域名已明确指定 | ⬜ | 不使用 `*` |
| 🔲 允许的方法已限制 | ⬜ | 仅必要方法 |
| 🔲 允许的请求头已限制 | ⬜ | 仅必要请求头 |

```properties
# 生产环境 CORS 配置
security.network.cors.enabled=true
security.network.cors.allowed-origins=https://your-domain.com
security.network.cors.allowed-methods=GET,POST,PUT,DELETE
security.network.cors.allowed-headers=Authorization,Content-Type,X-Requested-With
```

> 💡 **注意**: CORS 配置项以框架实际支持为准，具体配置参考 `configuration.md`。

### 2.5 CSRF 配置

| 检查项 | 状态 | 配置 |
|--------|------|------|
| 🔲 CSRF 保护已启用 | ⬜ | `security.network.csrf.enabled=true` |
| 🔲 Token 请求头已配置 | ⬜ | `X-CSRF-TOKEN` |
| 🔲 前端 Token 处理已实现 | ⬜ | 读取并发送 Token |

### 2.6 安全响应头

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 🔲 X-Frame-Options: DENY | ⬜ | 防止点击劫持 |
| 🔲 X-Content-Type-Options: nosniff | ⬜ | 防止 MIME 嗅探 |
| 🔲 X-XSS-Protection: 1; mode=block | ⬜ | XSS 过滤 |
| 🔲 Strict-Transport-Security | ⬜ | 强制 HTTPS |
| 🔲 Content-Security-Policy | ⬜ | 内容安全策略 |

### 2.7 审计与日志

| 检查项 | 状态 | 配置 |
|--------|------|------|
| 🔲 审计事件已记录 | ⬜ | 登录/登出/授权失败 |
| 🔲 审计日志存储已实现 | ⬜ | 数据库或日志文件 |
| 🔲 日志保留期限 ≥ 6 个月 | ⬜ | 等保要求 |
| 🔲 敏感数据已脱敏 | ⬜ | 密码、Token 不记录 |

---

## 三、定期审计检查清单

### 3.1 每月检查

| 检查项 | 上次检查 | 下次检查 | 状态 |
|--------|----------|----------|------|
| 🔲 依赖漏洞扫描 | | | ⬜ |
| 🔲 密码策略审查 | | | ⬜ |
| 🔲 访问日志审查 | | | ⬜ |
| 🔲 异常登录检测 | | | ⬜ |

### 3.2 每季度检查

| 检查项 | 上次检查 | 下次检查 | 状态 |
|--------|----------|----------|------|
| 🔲 Token 有效期审查 | | | ⬜ |
| 🔲 CORS 配置审查 | | | ⬜ |
| 🔲 权限配置审查 | | | ⬜ |
| 🔲 密钥轮换计划 | | | ⬜ |

### 3.3 每年检查

| 检查项 | 上次检查 | 下次检查 | 状态 |
|--------|----------|----------|------|
| 🔲 安全渗透测试 | | | ⬜ |
| 🔲 等保合规评估 | | | ⬜ |
| 🔲 密钥全面轮换 | | | ⬜ |
| 🔲 安全培训 | | | ⬜ |

---

## 四、应急响应检查清单

### 4.1 安全事件发生时

| 检查项 | 状态 | 备注 |
|--------|------|------|
| 🔲 事件已记录 | ⬜ | 时间、影响范围 |
| 🔲 相关日志已保存 | ⬜ | 防止丢失 |
| 🔲 受影响账号已锁定 | ⬜ | 防止进一步损失 |
| 🔲 JWT 密钥已更换 | ⬜ | 使所有 Token 失效 |
| 🔲 通知相关用户 | ⬜ | 重新登录 |

### 4.2 漏洞修复后

| 检查项 | 状态 | 备注 |
|--------|------|------|
| 🔲 漏洞已验证修复 | ⬜ | 重新测试 |
| 🔲 相关测试已通过 | ⬜ | 确保无回归 |
| 🔲 文档已更新 | ⬜ | 记录修复过程 |
| 🔲 用户已通知 | ⬜ | 如有需要 |

---

## 五、快速检查脚本

### 5.1 配置检查

```bash
#!/bin/bash
# security-check.sh

echo "=== Spring Security Boot 安全配置检查 ==="

# 检查 JWT 密钥
if grep -q "security.jwt.secret=.*" application.properties; then
    echo "✅ JWT 密钥已配置"
else
    echo "❌ JWT 密钥未配置"
fi

# 检查 HTTPS
if grep -q "server.ssl.enabled=true" application.properties; then
    echo "✅ HTTPS 已启用"
else
    echo "⚠️ HTTPS 未启用（生产环境必须）"
fi

# 检查 CORS
if grep -q "security.network.cors.allowed-origins=\*" application.properties; then
    echo "❌ CORS 允许所有域名（生产环境禁止）"
else
    echo "✅ CORS 域名已限制"
fi

echo "=== 检查完成 ==="
```

### 5.2 依赖漏洞扫描

```bash
# 使用 OWASP Dependency-Check
mvn dependency-check:check

# 或使用 Maven Enforcer
mvn enforcer:enforce
```

---

## 六、检查清单导出

### 完整配置示例

```properties
# ===== 生产环境安全配置 =====

# --- JWT 配置 ---
# 注意：密钥必须是 BASE64 编码
security.jwt.secret=${JWT_SECRET}
security.jwt.expiration=3600

# --- HTTPS 配置 ---
server.ssl.enabled=true
server.ssl.protocol=TLSv1.2
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3

# --- Session 配置 ---
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# --- CORS 配置 ---
security.network.cors.enabled=true
security.network.cors.allowed-origins=https://your-domain.com
security.network.cors.allowed-methods=GET,POST,PUT,DELETE
security.network.cors.allowed-headers=Authorization,Content-Type,X-Requested-With

# --- CSRF 配置 ---
security.network.csrf.enabled=true
security.network.csrf.token-header=X-CSRF-TOKEN

# --- 安全响应头 ---
security.network.headers.enabled=true

# --- 日志配置 ---
logging.level.com.original.security=INFO
logging.level.org.springframework.security=WARN
```

> 💡 **注意**: 以上配置项以框架实际支持为准。并发会话控制等需要在 `SecurityConfiguration` 中通过代码配置。

---

## 参考资源

- [等保 2.0 合规清单](compliance.md) - 等保合规要求
- [安全最佳实践](best-practices.md) - 详细配置指南
- [安全 FAQ](faq.md) - 常见问题解答
- [配置参考文档](../configuration.md) - 完整配置项说明

---

**文档最后更新：** 2026-03-23
