---
story_key: 1-3-enable-security-boot-annotation
epic: Epic 1: 框架基础与核心认证
status: dev-complete
created_date: 2026-02-27
last_updated: 2026-02-27
developers: []
reviewers: []
tags: [core, annotation, auto-config, security-filter]
---

# Story 1.3: 实现 @EnableSecurityBoot 注解

As a 开发者集成框架，  
I want 使用一个简单的注解开启全局安全配置，  
So that 我不需要在每个项目中重复拷贝并手动注册繁琐的 `SecurityFilterChain`、`AuthenticationManager` 和密码加密组件。

## Acceptance Criteria

**Given** 任何依赖了 `security-core` 的 Spring Boot 项目在启动类上配置了 `@EnableSecurityBoot` (或 `@EnableFrameAuthorizationServer` 视最终技术决策定) 注解  
**When** Spring 容器启动并初始化安全机制  
**Then** 系统自动配置 `AuthenticationManager`  
**And** 自动装配一条具备极简基础能力的 `SecurityFilterChain` 配置  
**And** 自动注册 `PasswordEncoder` (必须采用 `BCryptPasswordEncoder` 且符合强度规范)

**Given** 开发者查阅核心代码  
**When** 查看该开启注解的定义源码  
**Then** 注解应位于约定的 annotation 包中（`com.original.frame.annotation` 或 `com.original.security.annotation`）  
**And** 该注解必须通过 `@Import` 等机制引入核心的安全支撑配置类  
**And** 注解具备清晰的 JavaDoc 描述及代码使用示例集

**Given** 该注解的生效期间  
**When** 开发者在具体的 `application.properties` 中调整 `security.*` 相关属性，或在上下文中提供了自定义的相同类型的 Bean (如覆盖默认的 `PasswordEncoder`)  
**Then** 组件应遵守 Spring Boot `ConditionalOnMissingBean` 及 `@ConditionalOnProperty` 的规约，不与开发者的自定义控制权冲突，从而允许精细化替换。

## Tasks & Subtasks

### Tasks

- [x] 在 `security-core` 中定义 `@EnableSecurityBoot` 注解。
- [x] 编写核心配置类（如 `SecurityAutoConfiguration` 或在现有配置类的基础上），通过 `@Import` 注入。
- [x] 在配置类中实例化基于 BCrypt 的 `PasswordEncoder`。
- [x] 在配置类中注册 `AuthenticationManager`。
- [x] 在配置类中构建基础层面的 `SecurityFilterChain`（为后续的网络安全、JWT 预留口子，当前提供最基础的无状态或默认安全拦截策略）。
- [x] 补全所有新增公共配置的完整 JavaDoc。
- [x] 补齐相应的单元测试，验证 Bean 的装配以及条件触发是否健康。

## Dev Notes

### Technical Requirements

- 核心目标是降低使用者的接入成本。该注解需要将所有分散的底层安全装配整合进组件扫描生命周期或被显式导入。
- 可以使用 `@Import({SecurityAutoConfiguration.class, SecurityConfigurationValidator.class})` 来确保配置验证等前置条件一起加载。
- 保证 `PasswordEncoder` 的配置采用 `BCrypt` 加密算法并禁止使用 md5 或 sha1 等弱算法 (参阅 NFR-SEC-001)。

### Architecture Compliance

- **Component Location:**
  - 注解应位于 `security-core/src/main/java/com/original/security/annotation/` 或者是 `com.original.frame.annotation/` (请参照实际已建包名)。
  - 核心配置工厂位于 `security-core/.../config/` 包中。
- **Constructor Injection:** 任何对该配置类的组件注入（如注入配置属性），要求强制具备单例构造器注入，禁止 `@Autowired` 字段。
- **Conditional Configuration:** 提供 `@ConditionalOnMissingBean(PasswordEncoder.class)` 以便用户可以选用更复杂的密码配置策略。
- 从 `project-context.md` 中的命名规范：框架中可能之前期望的是 `@EnableFrameAuthorizationServer` 组合注解。在这个 Story 中，如果架构组确认使用 `@EnableSecurityBoot`，你可以将其作为实现的基础，或者建立 `@EnableFrameAuthorizationServer` 然后将本故事作为其底层能力的一部分。优先实现本 Story 定义的 `@EnableSecurityBoot` 并提供别名或关联。

### Library and Framework Requirements

- 在 Spring Boot 2.7.18 且 Spring Security 5.7+ 下，由于 `WebSecurityConfigurerAdapter` 已废弃，必须使用注册 `SecurityFilterChain` Bean 的方式来声明过滤链。
- `AuthenticationManager` 的注册：推荐通过在配置类中获取 `AuthenticationConfiguration` 并返回其 `getAuthenticationManager()`。

### File Structure Requirements

- `security-core/.../annotation/EnableSecurityBoot.java`
- `security-core/.../config/SecurityAutoConfiguration.java`
- `security-core/src/test/java/com/original/security/config/SecurityAutoConfigurationTest.java`

### Testing Requirements

- **Coverage:** 配置类及注解机制的验证，通过原生的 Spring Boot 切片测试 `@SpringBootTest(classes = ...)` 加载检查对应的 Bean 是否真的被放进去了上下文中。覆盖目标 **≥90%**。
- 测试 `Conditional` 的短路效果（如我们主动放了一个 `MockPasswordEncoder` 在容器里，框架自带的就不该生效）。

### Previous Story Intelligence

- 在上一个任务 (`1-1-config-validation-startup-check`) 中学到的经验显示：
  - Spring 4.3+ 后单构造器情况下无需标注 `@Autowired` 注解，直接声明字段可实现干净注入；在配置类中也应该如此运用。
  - 对于组件启动和加载日志，务必使用 SLF4J 打印例如 `log.info("Security auto configuration initialized")`，不要过于沉默。
  - 在前一个 Story 中发现的类似 “未打印默认值”、“缺失 JavaDoc” 的审查问题，本次实现一定要一上来就填补：**类的头部 JavaDoc 必须详尽**。

### Git Intelligence Summary

- 最近的 Git 记录 (`456cdcc`，`a47f43b`) 表明了我们不仅完成了数据表的建构，同时也补足了配置验证器相关的启动阻断。
- 后续所有的 PR 和 Commit 信息均遵循 Conventional Commits (`feat(core): ...`)。

### Project Context Reference

- 代码不要产生魔法值 (Magic values)，使用具体的常量类进行管理。
- 避免抛出或记录未处理的堆栈，所有的异常最好是由受检途径或者利用后续在全局异常里拦截。
- `response` 系列请谨记 `Response.successBuilder()` 或者原生的 Spring MVC 控制形式（考虑到现在只是搭骨架过滤器链，可能暂不涉及直接的业务 JSON 输出，但如果有异常，务必用 JSON 构建）。

## Dev Agent Record

### Implementation Plan

1. Create `EnableSecurityBoot` annotation
2. Create `SecurityAutoConfiguration` class with the beans.
3. Replace raw `@Import(SecurityAutoConfiguration.class)` with `DeferredImportSelector` to fix Bean overriding order.
4. Add `javax.servlet-api` dependency to fix test execution classpath.
5. Author short-circuit config tests.

### Debug Log

- `IllegalStateException`: Failed to load ApplicationContext during SureFire due to missing `javax.servlet.Filter` class in classpath. Fixed by declaring `javax.servlet-api` dependency.
- `ConfigurationException`: Test context failed because validation was triggered and database string was empty. Fixed by defining properties in `@SpringBootTest`.
- `NoUniqueBeanDefinitionException`: Found 2 `PasswordEncoder` classes and wasn't able to use `@ConditionalOnMissingBean` during manual config. Fixed by relying on `DeferredImportSelector`.

### Completion Notes

- Created `@EnableSecurityBoot` annotation
- Created `SecurityAutoConfiguration` configuring password encoder, authentication manager and stateless filter chain.
- Created `SecurityDeferredImportSelector`
- Tested and verified short-circuited override logic.
- Implemented full Javadoc on the components.

### File List

```text
- security-core/src/main/java/com/original/security/annotation/EnableSecurityBoot.java
- security-core/src/main/java/com/original/security/config/SecurityAutoConfiguration.java
- security-core/src/main/java/com/original/security/config/SecurityDeferredImportSelector.java
- security-core/src/test/java/com/original/security/config/SecurityAutoConfigurationTest.java
- security-core/src/test/java/com/original/security/config/SecurityAutoConfigurationConditionalTest.java
- security-core/pom.xml
```

### Senior Developer Review (AI)

- [x] Automatically fixed missing test validity for interception configuration check (added MockMvc).
- [x] Automatically fixed missing test validity for `AuthenticationManager` and `SecurityFilterChain` conditional overrides.
- [x] Simplified the unit test context setup, opting for `security.config.validation=false` instead of mocking a database connection.
- [x] Automatically fixed missing standard JavaDoc blocks for `SecurityDeferredImportSelector`.

**Review Status**: APPROVED (Issues Automatically Fixed)

### Change Log

- 2026-02-27: Created story file and tasks based on Epic 1 and Architecture guidelines by BMad Method.
- 2026-02-27: Dev completed implementation.
- 2026-02-28: Performed code review and resolved 4 AI findings.

## Status

done
