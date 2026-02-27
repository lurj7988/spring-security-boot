---
story_key: 1-1-config-validation-startup-check
epic: Epic 1: 框架基础与核心认证
status: done
created_date: 2026-02-27
last_updated: 2026-02-27
developers: []
reviewers: []
tags: [core, config, validation, error-handling]
---

# Story 1.1: 实现配置验证与启动检查

As a 开发者集成框架，  
I want 框架在启动时验证必填配置并给出清晰错误提示，  
So that 我能在配置缺失或错误时快速定位问题并解决。

## Acceptance Criteria

**Given** 应用启动且 `security.config.validation=true`（默认）  
**When** 核心安全机制必须的配置（如数据源、或后续的关键配置）缺失或无效  
**Then** 启动失败并抛出带有格式化错误信息的异常 (`ConfigurationException` 或类似)  
**And** 错误信息包含问题描述  
**And** 错误信息包含修复建议（具体配置项示例）  
**And** 错误信息包含文档链接 (如 `https://docs.example.com/config`)

**Given** 应用启动  
**When** 所有必填配置正确  
**Then** 应用正常启动  
**And** 控制台打印配置加载成功的摘要日志

**Given** 配置项有默认值  
**When** 开发者未明确声明配置该选项  
**Then** 框架使用安全的默认值  
**And** 启动时打印使用的默认值 (基于日志级别，建议 INFO/DEBUG)

**Given** 开发者显式关闭验证  
**When** 配置 `security.config.validation=false`  
**Then** 跳过启动配置检查

## Tasks & Subtasks

### Tasks

- [x] 定义配置验证器结构（例如 `SecurityConfigurationValidator`）。
- [x] 监听 Spring Boot 生命周期的事件（如 `ApplicationReadyEvent` 或使用 `EnvironmentPostProcessor` / `@ConfigurationProperties` 校验）。
- [x] 编写格式化错误消息的美化日志输出工具。
- [x] 定义包含具体修复建议和帮助链接的自定义配置异常类。
- [x] 提供单元测试，在提供合法配置与不合法配置的上下文中分别断言框架行为。

## Dev Notes

### Technical Requirements

- 需要用到 Spring Boot 的自动化配置及事件监听机制。
- 需要考虑如何读取用户目前已设定的环境属性（`Environment`）。
- 定义自定义异常 `ConfigurationException` 抛出验证失败。

### Architecture Compliance

- **Component Location:** 属于配置管理，但作为基础应该位于 `security-core/config` 包中（如 `SecurityConfigurationValidator.java`），或者 `security-components/security-config`。根据架构指南：必填配置验证属于**所有模块**的横切关注点。最佳落脚点在 `com.original.security.config` (core) 下。
- **Constructor Injection:** 强制使用**构造器注入**，绝不可使用字段注入（`@Autowired` on fields）。
- **Logging Standards:** 使用 SLF4J (`log.info`, `log.error`)，完全禁止 `System.out.println` 和 `e.printStackTrace()`。
- **Naming Conventions:** API/类使用 `PascalCase/camelCase`，如果涉及任何配置健则使用 `kebab-case`（如 `security.config.validation`）。

### Library and Framework Requirements

- **Spring Boot 2.7.18:** 注意相关 API 和包名。特别是 javax 命名空间约束！
- 测试依然需要 `spring-boot-starter-test`，无需 H2 (除非你在此处就去检查 DataSource 连通性，但这应该由 Spring Boot 或具体的 Plugin 去做。核心框架验证只检查配置键值的存在与合法性)。

### File Structure Requirements

- **Core Code:** `security-core/src/main/java/com/original/security/config/SecurityConfigurationValidator.java`
- **Exceptions:** `security-core/src/main/java/com/original/security/exception/ConfigurationException.java`
- **Tests:** `security-core/src/test/java/com/original/security/config/SecurityConfigurationValidatorTest.java`
- **Properties:** 可能需要 `SecurityProperties.java` 使用 `@ConfigurationProperties(prefix = "security")` 管理配置。

### Testing Requirements

- **Coverage Target:** 验证核心逻辑必须 **90%** 覆盖率。
- **Naming Standard:** `test{MethodName}_{Scenario}_{ExpectedResult}` (例如 `testValidateConfiguration_MissingDatasourceUrl_ThrowsException`)。
- **Testing Approach:** 可以使用 `ApplicationContextRunner` 或者直接通过 JUnit 原生测试验证 `Validator` 处理不同 `Environment` 或 `SecurityProperties` 注入的情况。

### Project Context Reference

- **API Response Format:** `{code, message, data, timestamp, path}` (这里主要影响的是启动失败的 Console 输出，而非 API)。
- 错误信息格式应类似如下，务必清晰：

```text
=== Spring Security Boot 配置错误 ===

错误: 数据库连接未配置

解决方案:
  1. 添加到 application.properties:
     spring.datasource.url=jdbc:mysql://localhost:3306/mydb
     spring.datasource.username=root
     spring.datasource.password=***

  2. 或者禁用验证（不推荐）:
     security.config.validation=false

文档: https://docs.example.com/config
```

## Dev Agent Record

### Implementation Plan

- Created `SecurityProperties` with prefix `security` to manage the root configurations, starting with `security.config.validation` (default: true).
- Created `ConfigurationException` for validation error signaling.
- Implemented `SecurityConfigurationValidator` listening to `ApplicationReadyEvent`. Checks `spring.datasource.url` from `Environment` and logs a formatted error, then throws `ConfigurationException` if missing.
- Implemented `SecurityConfigurationValidatorTest` which achieves over 90% coverage for the validator logic through unit tests simulating validation enabled/disabled and missing/present environments.

### Debug Log

- Tests initially passed seamlessly since the classes logic didn't encompass overly complex DB connection pooling. Only configuration existence is checked on `ApplicationReadyEvent`.

### Completion Notes

- Application correctly validates the `spring.datasource.url` property upon startup when `security.config.validation=true`.
- The configuration structure handles disabling validation gracefully.
- The errors are properly formatted with suggestions.
All tests pass.

### File List

```text
security-core/src/main/java/com/original/security/config/SecurityProperties.java
security-core/src/main/java/com/original/security/config/SecurityConfigurationValidator.java
security-core/src/main/java/com/original/security/exception/ConfigurationException.java
security-core/src/test/java/com/original/security/config/SecurityConfigurationValidatorTest.java
```

### Change Log

- 2026-02-27: Created story file and tasks based on Epic 1 and Architecture guidelines.
- 2026-02-27: Implemented Configuration Validation components and verified logic coverage.
- 2026-02-27: Code review completed - fixed 2 HIGH and 4 MEDIUM issues.

### Code Review Record

**Review Date:** 2026-02-27
**Reviewer:** Claude Code (Adversarial Review)

**Issues Found & Fixed:**

| ID | Severity | Issue | Resolution |
|----|----------|-------|------------|
| HIGH-1 | HIGH | 使用了 `@Autowired` 注解在构造器上 | 移除了 `@Autowired` 注解，Spring 4.3+ 单构造器时可选 |
| HIGH-2 | HIGH | `ConfigurationException` 缺少异常链构造器 | 添加了 `(String, Throwable)` 和 `(Throwable)` 构造器 |
| MEDIUM-1 | MEDIUM | 验证成功时未打印默认值 | 添加了 `logDefaultConfigurationValues()` 方法 |
| MEDIUM-2 | MEDIUM | 文档链接硬编码 | 提取为常量 `DEFAULT_DOC_URL` |
| MEDIUM-3 | MEDIUM | 缺少完整 JavaDoc | 为所有公共类和方法添加了完整 JavaDoc |
| MEDIUM-4 | MEDIUM | 测试未验证日志输出 | 添加了额外的测试用例验证日志行为 |

**LOW Issues (未修复，作为技术债务):**
- LOW-1: 硬编码的 MySQL 示例连接字符串（可接受，作为示例）
- LOW-2: 可考虑使用 `@ConstructorBinding` 模式（Spring Boot 2.2+ 特性，当前实现兼容性更好）

## Status

done
