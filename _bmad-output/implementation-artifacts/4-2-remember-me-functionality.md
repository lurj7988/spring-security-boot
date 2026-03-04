# Story 4.2: Remember Me 功能

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 终端用户，
I want 勾选"记住我"后7天内免登录，
so that 我不需要频繁输入密码。

## Acceptance Criteria

1. **Given** 登录表单
   **When** 用户勾选"记住我"并登录成功
   **Then** 生成 Remember Me Token
   **And** Token 有效期为 7 天（可配置）
   **And** Token 持久化到数据库

2. **Given** Remember Me Token 存储
   **When** 查看数据库表
   **Then** 存储在 `persistent_logins` 表
   **And** 包含 `username`、`series`、`token`、`last_used` 字段
   **And** 使用 snake_case 命名

3. **Given** 用户关闭浏览器后重新打开
   **When** Remember Me Token 有效
   **Then** 用户自动登录
   **And** 无需重新输入密码

4. **Given** Remember Me 配置
   **When** 查看 `security.remember-me` 配置
   **Then** 可以配置 Token 有效期（默认 7 天）
   **And** 可以启用/禁用功能
   **And** 配置项有合理默认值

## Tasks / Subtasks

- [x] Task 1: 数据库与实体设计 (AC: 2)
  - [x] Subtask 1.1: 创建 `persistent_logins` 数据库表，确保表字段定义为 `username`, `series`, `token`, `last_used`。
  - [x] Subtask 1.2: 配置 `JdbcTokenRepositoryImpl` 或实现 `PersistentTokenRepository` 进行 Token 的持久化读写。

- [x] Task 2: 实现 Remember Me 配置类 (AC: 4)
  - [x] Subtask 2.1: 在 `security-core` 中创建 `RememberMeProperties`，采用 `@ConfigurationProperties("security.remember-me")`。
  - [x] Subtask 2.2: 配置属性：`enabled` (布尔，控制启用)，`token-validity-seconds` (默认 604800)，以及可选的安全 `key`。

- [x] Task 3: 集成 Spring Security Remember Me 功能 (AC: 1, 3)
  - [x] Subtask 3.1: 在 `SecurityAutoConfiguration` 配置链中增加 `http.rememberMe()` 设定。
  - [x] Subtask 3.2: 注入并配置 `UserDetailsService` 与 `PersistentTokenRepository`。
  - [x] Subtask 3.3: 应用来自 `RememberMeProperties` 中的配置项。

- [x] Task 4: 编写测试并验证覆盖率 (AC: All)
  - [x] Subtask 4.1: 为 `RememberMeProperties` 编写单元测试（包含默认值与边界校验）。
  - [x] Subtask 4.2: 编写集成测试模拟带有记住我 Cookie 的登录及自动恢复会话流程。

## Dev Notes

### Technical Requirements

- **Frameworks:** Spring Boot 2.7.18, Spring Security 5.7.11, Java 1.8
- **Dependency Injection:** 强制使用**构造器依赖注入**，禁止 `@Autowired` 字段注入。
- **Token 存储机制:** 必须将 token 存储于数据库中，防止服务重启导致的 remember-me 登录失效。

### Architecture Compliance

- **Module:** 相关的过滤器、配置和插件应该位于 `security-core`。
- **Database Naming:** 所有的数据库表和列强制遵守 `snake_case` 规范（表名 `persistent_logins`）。

### Previous Story Intelligence

- 从 4.1 (Session 认证) 和 1.5 (JWT 认证) 中学习，我们的核心配置需要有独立的 `*Properties` 类，且在 `SecurityAutoConfiguration` 中通过 `.and()` 进行拼接。
- 我们需要为配置项建立完备的单元测试，并在其中验证输入约束。

### Project Structure Notes

- **配置代码位置:** `security-core/src/main/java/com/original/security/config/RememberMeProperties.java`
- **测试代码位置:** `security-core/src/test/java/com/original/security/config/RememberMePropertiesTest.java`

### Testing Standards

- **测试框架:** JUnit 5 + Mockito + Spring Boot Test
- **覆盖率:** 核心功能必须达到 ≥ 90% 的测试覆盖率。
- **命名约定:** 方法应采取 `test{MethodName}_{Scenario}_{ExpectedResult}` 的格式。

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 4.2]
- [Source: _bmad-output/planning-artifacts/architecture.md#决策 3：配置策略 - 配置清晰化]
- [Source: _bmad-output/implementation-artifacts/4-1-session-authentication-plugin.md]

## Dev Agent Record

### Agent Model Used

Gemini

### Debug Log References

- 添加 `spring-jdbc` 依赖到 `security-core/pom.xml`，以解决 `JdbcTokenRepositoryImpl` 无法找到 `JdbcDaoSupport` 的编译错误。
- 在 `RememberMeAutoConfiguration` 中添加 `@ConditionalOnClass({JdbcTokenRepositoryImpl.class, org.springframework.jdbc.core.support.JdbcDaoSupport.class})` 以支持可选依赖。

### Completion Notes List

- 完成 `RememberMeProperties` 配置类的创建，支持 `enabled`、`token-validity-seconds`、`key` 和 `cookie-name` 的配置。
- 创建 `RememberMeAutoConfiguration` 自动配置类，基于 `DataSource` 注入 `JdbcTokenRepositoryImpl` 作为 `PersistentTokenRepository`。
- 修改 `SecurityAutoConfiguration` 引入 `RememberMeAutoConfiguration`，并在 `securityFilterChain` 中配置 `http.rememberMe()`，注入 `UserDetailsService` 和 `PersistentTokenRepository`。
- 编写了 `RememberMePropertiesTest` 单元测试，验证所有配置属性的默认值与边界条件。
- 编写了 `RememberMeIntegrationTest` 集成测试，验证了 `RememberMe` 的配置加载和基于无效 Token 的请求被拦截。
- `persistent_logins` 表已确认存在于 `schema.sql` 和 `schema-h2.sql` 中。

### Code Review Fixes (Second Pass)
- 在 `AuthenticationControllerTest` 中添加了 Remember Me 相关的测试用例（`testLogin_WithRememberMe_CallsRememberMeServices` 和 `testLogin_WithoutRememberMe_DoesNotCallRememberMeServices`）。
- 修改了 `RememberMeAutoConfiguration` 和 `SecurityAutoConfiguration` 中 key 的默认值处理逻辑，使用 `SecureRandom` 生成 256 位随机密钥替代硬编码默认值。
- 修复了 `RememberMeIntegrationTest` 中的 mock 配置，添加了 `createNewToken()` 和 `updateToken()` 方法的 mock 配置。
- 添加了 `@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)` 注解确保测试隔离。
- 所有 298 个测试通过。

### File List

- `security-core/pom.xml` (Modified)
- `security-core/src/main/java/com/original/security/config/RememberMeProperties.java` (New)
- `security-core/src/main/java/com/original/security/config/RememberMeAutoConfiguration.java` (New, Modified, Third Pass)
- `security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java` (Modified, Third Pass)
- `security-core/src/main/java/com/original/security/controller/AuthenticationController.java` (Modified, Third Pass)
- `security-core/src/main/java/com/original/security/dto/LoginRequest.java` (Modified, Third Pass)
- `security-core/src/test/java/com/original/security/controller/AuthenticationControllerTest.java` (Modified)
- `security-core/src/test/java/com/original/security/config/RememberMePropertiesTest.java` (New)
- `security-core/src/test/java/com/original/security/RememberMeIntegrationTest.java` (New, Modified)

## Code Review Record

### Reviewer
Gemini (Code Review Workflow)

### Review Findings
- **CRITICAL**: `AuthenticationController` login logic bypassed Spring Security's `RememberMeServices`, failing to set the remember-me cookie for REST API logins.
- **CRITICAL**: `LoginRequest` lacked the `rememberMe` boolean parameter.
- **MEDIUM**: `AuthenticationController` logout failed to call `RememberMeServices.logout()`, leaving the remember-me cookie active after logout.
- **MEDIUM**: `RememberMeIntegrationTest` did not contain a valid login/auto-login test.

### Actions Taken
- Updated `LoginRequest` to include `rememberMe`.
- Modified `AuthenticationController` to inject `RememberMeServices` and use an `HttpServletRequestWrapper` during login to supply the `rememberMe` parameter so that `loginSuccess` generates the cookie.
- Fixed `AuthenticationController.logout` to delegate to `RememberMeServices` as a `LogoutHandler` so the cookie is cleared.
- Rewrote `RememberMeIntegrationTest` and updated `RememberMeAutoConfiguration` to expose `RememberMeServices` as a bean properly. Tests pass.

### Reviewer (Second Pass)
Claude Opus 4.6 (Code Review Workflow)

### Review Findings (Second Pass)
- **LOW**: `AuthenticationControllerTest` 中缺少 Remember Me 相关的测试。
  - 位置: `security-core/src/test/java/com/original/security/controller/AuthenticationControllerTest.java`
  - 说明: 测试文件中的 `setUp()` 方法设置了 `rememberMeServicesProvider` 返回 `null`，导致无法测试 Remember Me 功能。
  - 建议: 添加一个测试用例来验证 `login()` 方法在 `rememberMe=true` 时正确调用 `RememberMeServices.loginSuccess()`。
- **LOW**: `RememberMeProperties` 中默认 key 为空可能存在安全风险。
  - 位置: `security-core/src/main/java/com/original/security/config/RememberMeProperties.java:51`
  - 说明: 默认情况下 `key` 字段为 `null`，在 `RememberMeAutoConfiguration.java:76-78` 中，如果 key 为空则使用硬编码的 `"default-remember-me-key"`。
  - 建议: 在 key 为空时，应该生成一个随机密钥或者抛出配置错误。

### Actions Taken (Second Pass)
- 在 `AuthenticationControllerTest` 中添加了两个新的测试用例：
  - `testLogin_WithRememberMe_CallsRememberMeServices`: 验证启用 Remember Me 时正确调用服务
  - `testLogin_WithoutRememberMe_DoesNotCallRememberMeServices`: 验证未启用 Remember Me 时不调用服务
- 修改了 `RememberMeAutoConfiguration` 和 `SecurityAutoConfiguration` 中 key 的默认值处理逻辑：
  - 使用 `SecureRandom` 生成 256 位随机密钥（Base64 编码）
  - 添加警告日志提示生产环境应配置固定密钥
  - 避免了使用硬编码默认值的安全风险
- 修复了 `RememberMeIntegrationTest` 中的 mock 配置：
  - 添加了 `createNewToken()` 方法的 mock 配置
  - 添加了 `updateToken()` 方法的 mock 配置
  - 添加了 `@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)` 注解确保测试隔离
- 所有 298 个测试通过。

### Reviewer (Third Pass)
Claude Opus 4.6 (Code Review Workflow)

### Review Findings (Third Pass)
- **HIGH**: AuthenticationController.login 方法在认证失败后没有清除 SecurityContext。
  - 位置: `security-core/src/main/java/com/original/security/controller/AuthenticationController.java:131-133`
  - 说明: 在认证成功后设置了 SecurityContext，但在认证失败时（catch AuthenticationException），没有清除可能已存在的 SecurityContext。
  - 影响: 攻击者可能利用之前的认证上下文绕过安全检查。
  - 修复: 在 catch AuthenticationException 块中添加 `SecurityContextHolder.clearContext()`。
- **HIGH**: Remember Me Token 的随机密钥在每次应用重启时都会生成新的密钥。
  - 位置: `security-core/src/main/java/com/original/security/config/RememberMeAutoConfiguration.java:78-85` 和 `SecurityAutoConfiguration.java:262-273`
  - 说明: 当 `security.remember-me.key` 未配置时，代码会在每次应用启动时生成一个新的随机密钥，导致应用重启后所有现有的 Remember Me Cookie 失效，多实例部署时 Remember Me 功能不可用。
  - 影响: 用户体验差，多实例部署时功能不可用。
  - 修复: 在 key 为空时抛出 `IllegalStateException`，强制用户配置密钥。
- **LOW**: LoginRequest 的 JavaDoc 没有提到 rememberMe 字段。
  - 位置: `security-core/src/main/java/com/original/security/dto/LoginRequest.java:3-11`
  - 说明: JavaDoc 只提到"包含用户名和密码字段"，没有提到 `rememberMe` 字段。
  - 修复: 更新 JavaDoc 以反映完整的 API。
- **LOW**: Remember Me Cookie 没有设置 SameSite 和 Secure 属性。
  - 位置: `security-core/src/main/java/com/original/security/config/RememberMeAutoConfiguration.java:54-65`
  - 说明: `PersistentTokenBasedRememberMeServices` 配置中只设置了 Cookie 名称、Token 有效期和参数，但没有设置 Cookie 的安全属性（Secure 和 SameSite）。
  - 影响: Cookie 可能通过不安全的 HTTP 连接传输，可能被截获。
  - 修复: 在 `RememberMeAutoConfiguration.rememberMeServices()` 方法的 JavaDoc 中添加安全说明，解释 Spring Security 5.7.11 的限制和推荐配置。

### Actions Taken (Third Pass)
- 修改了 `AuthenticationController` 的 login 方法，在 catch AuthenticationException 块中添加 `SecurityContextHolder.clearContext()`。
- 修改了 `RememberMeAutoConfiguration` 和 `SecurityAutoConfiguration`，删除随机密钥生成逻辑，改为在 key 为空时抛出 `IllegalStateException`，强制用户配置密钥。
- 删除了不再需要的 import 语句（SecureRandom 和 Base64）。
- 更新了 `LoginRequest` 的 JavaDoc，添加了 rememberMe 字段的说明。
- 在 `RememberMeAutoConfiguration.rememberMeServices()` 方法的 JavaDoc 中添加了 Cookie 安全属性说明。
- 所有 298 个测试通过。