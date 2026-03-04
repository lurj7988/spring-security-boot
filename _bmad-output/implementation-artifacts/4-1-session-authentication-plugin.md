# Story 4.1: Session 认证插件

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 开发者构建传统 Web 应用，
I want 使用 Session 进行有状态认证，
So that 我的用户可以通过会话保持登录状态。

## Acceptance Criteria

1. **Given** SessionAuthenticationPlugin 已注册
   **When** 用户登录成功
   **Then** 创建用户会话
   **And** Session 存储在服务器端
   **And** 客户端通过 JSESSIONID Cookie 识别会话

2. **Given** Session 配置
   **When** 查看 Session 配置选项
   **Then** 可以配置 Session 过期时间
   **And** 可以配置 Session 存储方式（内存/Redis）
   **And** 默认过期时间为 30 分钟

3. **Given** 用户会话存在
   **When** 请求携带有效 Session ID
   **Then** 用户自动通过认证
   **And** 无需重复登录

4. **Given** Session 过期
   **When** 用户请求携带过期 Session
   **Then** 返回 401 Unauthorized
   **And** 提示重新登录

## Tasks / Subtasks

- [x] Task 1: 定义 SessionAuthenticationPlugin 接口实现 (AC: 1)
  - [x] Subtask 1.1: 在 `security-core/plugin/session/` 创建 `SessionAuthenticationPlugin` 类，实现 `AuthenticationPlugin` 接口
  - [x] Subtask 1.2: 实现 `getName()` 返回 "session-authentication"
  - [x] Subtask 1.3: 实现 `getAuthenticationProvider()` 返回 Session 认证提供者
  - [x] Subtask 1.4: 实现 `supports(Class<?> authenticationType)` 支持 `UsernamePasswordAuthenticationToken`

- [x] Task 2: 实现 Session 认证配置 (AC: 2)
  - [x] Subtask 2.1: 创建独立的 `SessionProperties` 配置类管理 `session.*` 配置项（timeout, store-type, cookie-name, fixation-protection）
  - [x] Subtask 2.2: 创建 `SessionProperties` 配置类使用 `@ConfigurationProperties`
  - [x] Subtask 2.3: 配置默认过期时间为 30 分钟（1800秒）
  - [x] Subtask 2.4: 支持配置存储方式（memory/redis），默认为内存

- [x] Task 3: 配置 Spring Security Session 管理 (AC: 1, 3)
  - [x] Subtask 3.1: 在 `SecurityAutoConfiguration` 中配置 `SessionCreationPolicy.IF_REQUIRED`
  - [x] Subtask 3.2: 配置 `sessionCreationPolicy = SessionCreationPolicy.IF_REQUIRED`
  - [x] Subtask 3.3: Session 固定攻击防护由 Spring Security 默认启用
  - [x] Subtask 3.4: 并发 Session 控制通过 `SessionRegistry` 支持

- [x] Task 4: 实现 Session 过期处理 (AC: 4)
  - [x] Subtask 4.1: 创建 `InvalidSessionStrategy` 处理无效 Session
  - [x] Subtask 4.2: 确保 Session 过期时返回 401 状态码
  - [x] Subtask 4.3: 添加 `SessionInformationExpiredStrategy` 处理并发登录踢出场景

- [x] Task 5: 编写单元测试和集成测试 (AC: All)
  - [x] Subtask 5.1: 编写 `SessionAuthenticationPluginTest` 测试插件注册和基本功能
  - [x] Subtask 5.2: 编写 `SessionPropertiesTest` 测试配置加载
  - [x] Subtask 5.3: 编写 `SessionExpiredHandlerTest` 测试 Session 过期处理
  - [x] Subtask 5.4: 所有测试通过（34 个测试用例）

## Dev Notes

### Technical Requirements

- **Frameworks:** Spring Boot 2.7.18, Spring Security 5.7.11, Java 1.8
- **Dependency Injection:** 必须使用**构造器依赖注入**，禁止字段注入（@Autowired on fields）
- **Session 管理:** 使用 Spring Security 内置的 `SessionManagementConfigurer`
- **Cookie:** 使用标准 JSESSIONID Cookie（Servlet 容器默认）

### Architecture Compliance

- **Module:** `security-core` (插件定义和核心配置)
- **Package:** `com.original.security.plugin.session`
- **Interface:** 必须实现 `AuthenticationPlugin` 接口（定义于 `com.original.security.plugin`）
- **Configuration:** 通过 `SecurityProperties` 管理配置，使用 `@ConfigurationProperties` 注解

### 核心架构参考

```java
// AuthenticationPlugin 接口定义（已存在于 security-core）
public interface AuthenticationPlugin {
    String getName();
    AuthenticationProvider getAuthenticationProvider();
    boolean supports(Class<?> authenticationType);
}
```

### Session 配置模式

```java
// SecurityConfiguration 中的 Session 配置示例
@Override
protected void configure(HttpSecurity http) throws Exception {
    http.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        .sessionFixation().migrateSession()
        .maximumSessions(1)
        .expiredSessionStrategy(sessionExpiredStrategy());
}
```

### 配置属性参考

```properties
# Session 配置项（需要添加到 application.properties）
# 必须显式启用 Session 功能，否则默认使用 STATELESS 模式
security.session.enabled=true

# Session 超时时间（秒），默认 30 分钟
security.session.timeout=1800

# 单用户最大 Session 数，默认 1
security.session.max-sessions=1

# 存储方式：memory/redis，默认 memory
security.session.store-type=memory

# Cookie 名称，默认 JSESSIONID
security.session.cookie-name=JSESSIONID

# 是否启用 Session 固定攻击防护，默认 true
security.session.fixation-protection=true
```

### Previous Story Intelligence

- **Story 3.5 (动态权限加载):** 使用 Caffeine 缓存优化性能，配置通过 `SecurityProperties` 外部化
- **Story 1.5 (JWT 认证插件):** 已建立 `AuthenticationPlugin` 实现模式，Session 插件应遵循相同模式
- **Story 1.3 (@EnableSecurityBoot 注解):** `SecurityConfiguration` 已存在，需要添加 Session 配置

### Git Intelligence Summary

- 最近提交显示严格的代码规范：构造器注入、JavaDoc、单元测试
- `SecurityProperties` 已在 `security-core` 中存在，需要扩展添加 Session 配置
- 认证插件模式已建立（参考 `JwtAuthenticationPlugin`）

### Project Structure Notes

- **Plugin 位置:** `security-core/src/main/java/com/original/security/plugin/session/`
- **Configuration 位置:** `security-core/src/main/java/com/original/security/config/`
- **Test 位置:** `security-core/src/test/java/com/original/security/plugin/session/`

### Testing Standards

- **测试框架:** JUnit 5 + Mockito + Spring Boot Test
- **命名规范:** `test{MethodName}_{Scenario}_{ExpectedResult}`
- **覆盖率要求:** 核心代码 ≥ 80%
- **测试类型:**
  - 单元测试：Plugin 类、Properties 类
  - 集成测试：完整的 Session 创建/验证/过期流程

### Security Considerations

- **Session 固定攻击防护:** 必须启用 `sessionFixation().migrateSession()`
- **Cookie 安全:** 生产环境建议启用 `Cookie.setHttpOnly(true)` 和 `Cookie.setSecure(true)`
- **Session 过期:** 必须有明确的过期处理，返回 401 而非 302 重定向（API 场景）

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 4.1]
- [Source: _bmad-output/planning-artifacts/architecture.md#认证架构]
- [Source: _bmad-output/project-context.md#技术栈与版本]
- [Source: _bmad-output/implementation-artifacts/3-5-dynamic-permission-loading.md]

## Dev Agent Record

### Agent Model Used

Claude Opus 4.6 (claude-opus-4-6)

### Debug Log References

- 编译错误修复：`SessionSecurityAutoConfiguration` 删除（冗余文件）
- 编译错误修复：`SecurityAutoConfiguration` 添加缺失的 `.and()` 方法调用
- 编译错误修复：`SessionExpiredHandler.onExpired()` 方法名修正为 `onExpiredSessionDetected()`
- 测试修复：Java 1.8 不支持 `var` 关键字，替换为显式类型声明
- 测试修复：`SessionInformationExpiredEvent` 是 final 类，改用 Spring Mock 对象
- 测试修复：`SessionInformation` 构造函数需要 non-null 的 `lastRequest` 参数
- 测试修复：`ContentType` 断言修正为检查包含关系

### Completion Notes List

1. **SessionAuthenticationPlugin**: 实现了 `AuthenticationPlugin` 接口，提供 Session 认证插件功能
2. **SessionProperties**: 创建了独立的配置属性类，支持 timeout、maxSessions、storeType、fixationProtection 配置
3. **SessionProperties**: 独立配置类管理所有 Session 配置（未修改 SecurityProperties）
4. **SessionAutoConfiguration**: 配置 SessionRegistry、HttpSessionEventPublisher、SessionInformationExpiredStrategy、InvalidSessionStrategy
5. **SessionExpiredHandler**: 实现 `SessionInformationExpiredStrategy` 接口，返回 401 JSON 响应
6. **SecurityAutoConfiguration**: 修改 Session 策略从 `STATELESS` 改为 `IF_REQUIRED`
7. **单元测试和集成测试**: 34 个测试用例全部通过，新增 SessionAuthenticationIntegrationTest 验证完整认证流程。

**AI Review Fixes (Code Review - 2nd Round)**:
- 修复并发 Session 控制配置：添加 `maxSessionsPreventsLogin(false)` 允许新登录踢出旧 Session
- 增强 SessionProperties 验证：为 `setMaxSessions` 添加验证，确保值大于 0 或等于 -1
- 增强 SessionProperties 验证：为 `setStoreType` 添加验证，只允许 "memory" 或 "redis"
- 新增验证测试：为 `setMaxSessions` 和 `setStoreType` 添加 4 个新的测试用例
- 更新文档：说明 `cookieName` 配置为未来功能预留接口

**AI Review Fixes (Code Review - 3rd Round)**:
- 修复无效配置问题：将 `enabled` 字段添加到 `SessionProperties`，使得 `security.session.enabled=true` 可以被正确绑定
- 新增 enabled 字段测试：添加 `testIsEnabled_DefaultValue_ReturnsTrue` 和 `testSetEnabled_UpdatesValue`
- 更新文档：在配置示例中添加 `security.session.enabled=true` 说明

### File List

**新增文件:**

- `security-core/src/main/java/com/original/security/plugin/session/SessionAuthenticationPlugin.java`
- `security-core/src/main/java/com/original/security/config/SessionProperties.java`
- `security-core/src/main/java/com/original/security/config/SessionAutoConfiguration.java`
- `security-core/src/main/java/com/original/security/handler/SessionExpiredHandler.java`
- `security-core/src/main/java/com/original/security/handler/InvalidSessionHandler.java`
- `security-core/src/test/java/com/original/security/plugin/session/SessionAuthenticationPluginTest.java`
- `security-core/src/test/java/com/original/security/config/SessionPropertiesTest.java`
- `security-core/src/test/java/com/original/security/handler/SessionExpiredHandlerTest.java`
- `security-core/src/test/java/com/original/security/SessionAuthenticationIntegrationTest.java`

**修改文件:**

- `security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java` - 修改 SessionCreationPolicy 为 IF_REQUIRED，添加 Session 管理配置，修复并发 Session 控制
- `security-core/src/main/java/com/original/security/config/SessionProperties.java` - 添加输入验证，更新文档
