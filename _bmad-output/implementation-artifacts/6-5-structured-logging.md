# Story 6.5: 实现结构化日志

Status: done

## Story

As a 开发者，
I want 框架提供结构化日志，
So that 我可以方便地调试和分析问题。

## Acceptance Criteria

**AC1: 结构化日志记录**
**Given** 日志配置已启用
**When** 用户登录
**Then** 记录结构化日志
**And** 包含字段：event_type、username、success、timestamp
**And** 使用 SLF4J（不用 System.out）

**AC2: 敏感数据脱敏**
**Given** 敏感数据处理机制
**When** 记录认证日志
**Then** 密码字段不记录
**And** JWT Token 仅记录前 10 字符
**And** 日志中不含敏感信息

**AC3: 日志级别配置**
**Given** 日志级别配置
**When** 配置 logging.level.com.original.security
**Then** 支持 DEBUG、INFO、WARN、ERROR
**And** 生产环境默认 INFO
**And** 开发环境可以 DEBUG

**AC4: 错误日志记录**
**Given** 错误日志机制
**When** 认证失败
**Then** 记录 ERROR 级别日志
**And** 包含错误堆栈（开发环境）
**And** 不使用 printStackTrace()

## Tasks / Subtasks

- [x] 创建结构化日志基础设施 (AC: #1)
  - [x] 创建 SecurityLogEvent 类（结构化日志事件）
  - [x] 创建 SecurityLogField 枚举（日志字段定义）
  - [x] 创建 SecurityLogLevel 枚举（日志级别）
- [x] 创建日志脱敏工具类 (AC: #2)
  - [x] 创建 SensitiveDataMasker 类
  - [x] 实现密码脱敏
  - [x] 实现 JWT Token 脱敏
  - [x] 实现通用敏感字段脱敏
- [x] 创建安全日志记录器 (AC: #1, #3, #4)
  - [x] 创建 SecurityLogger 接口
  - [x] 创建 DefaultSecurityLogger 实现
  - [x] 支持 MDC 上下文传播
  - [x] 支持不同日志级别
- [x] 集成到认证流程 (AC: #1, #4)
  - [x] 在认证成功时记录结构化日志
  - [x] 在认证失败时记录错误日志
  - [x] 在授权失败时记录审计日志
- [x] 创建日志配置 (AC: #3)
  - [x] 创建 SecurityLoggingProperties 配置类
  - [x] 创建 SecurityLoggingAutoConfiguration 自动配置
  - [x] 支持 spring.factories 自动配置注册
- [x] 编写测试覆盖 (AC: #1, #2, #3, #4)
  - [x] 测试 SecurityLogEvent
  - [x] 测试 SensitiveDataMasker
  - [x] 测试 DefaultSecurityLogger
  - [x] 测试 SecurityLoggingProperties

## Dev Notes

### Epic Context

**Epic 6: 可观测性与测试支持**

- **Epic 目标**: 开发者可以监控安全事件并编写测试
- **相关 Stories**: 6-1 审计事件发布, 6-2 Metrics 指标, 6-3 健康检查端点, 6-4 测试支持工具
- **业务价值**: 提供结构化日志，方便调试、分析和监控安全事件
- **技术关联**: 依赖 SLF4J/Logback，与 Spring Boot Logging 集成

**需求覆盖**: FR33 (结构化日志), FR59 (日志级别配置), FR60 (日志格式配置), NFR-MAINT-001 (核心测试覆盖率≥90%)

### 技术要求与约束

**架构模式**:

- 基于 SLF4J + Logback 日志框架
- 使用 MDC (Mapped Diagnostic Context) 传播上下文
- 支持结构化 JSON 格式日志（可选）
- 日志记录器应该是线程安全的

**日志框架版本**:

- SLF4J API（Spring Boot 默认）
- Logback（Spring Boot 默认）
- Jackson（用于 JSON 序列化，可选）

**源树组件要触达**:

- security-core/src/main/java/com/original/security/logging/SecurityLogEvent.java
- security-core/src/main/java/com/original/security/logging/SecurityLogField.java
- security-core/src/main/java/com/original/security/logging/SecurityLogLevel.java
- security-core/src/main/java/com/original/security/logging/SecurityLogger.java
- security-core/src/main/java/com/original/security/logging/DefaultSecurityLogger.java
- security-core/src/main/java/com/original/security/logging/SensitiveDataMasker.java
- security-core/src/main/java/com/original/security/logging/config/SecurityLoggingProperties.java

### 测试标准摘要

**测试命名规范** (来自 project-context.md):

```
test{MethodName}_{Scenario}_{ExpectedResult}

示例：
testLogAuthentication_Success_LogsStructuredEvent
testMaskPassword_ValidPassword_ReturnsMasked
testLogAuthentication_InvalidPassword_LogsError
```

**测试组织**:

- 测试类放在对应模块的 src/test/java 目录下
- 测试类命名：{ClassName}Test.java
- 使用 logback-test.xml 配置测试日志
- 使用 ListAppender 捕获日志进行验证

**测试优先级**:
**P0 - 立即**: SecurityLogger 功能测试、SensitiveDataMasker 功能测试
**P1 - 重要**: MDC 上下文测试、日志级别测试
**P2 - 常规**: 集成测试、性能测试

**覆盖率目标**: 核心安全组件 90%+，日志工具代码 80%+

### Previous Story Intelligence (Story 6-4)

**从 6-4 测试支持工具学习的经验**:

1. **测试模式**:
   - 使用嵌套类组织不同场景测试
   - 使用 @MockBean 模拟外部依赖
   - 每个测试方法验证一个具体行为

2. **代码质量检查清单**:
   - [x] 使用构造器依赖注入
   - [x] 方法长度 < 50 行
   - [x] 异常被正确处理（使用日志框架）
   - [x] 没有魔法值
   - [x] 公共 API 有 JavaDoc
   - [x] 测试覆盖率 ≥ 80%

3. **日志规范**:
   - 使用 SLF4J（不用 System.out）
   - 不使用 printStackTrace()
   - 敏感数据脱敏
   - 日志级别正确使用

4. **常见问题避免**:
   - 并发问题需要使用正确的锁机制
   - 异步处理要谨慎使用
   - 日志级别要统一

### Project Structure Notes

**统一项目结构对齐**:

- 日志相关类放在 logging 包下
- 配置类放在 logging/config 子包
- 日志字段使用枚举定义
- 遵循 Spring Boot Logging 自动配置模式

**检测到的冲突或差异**:

- 需要确保与现有日志配置兼容
- 不覆盖用户自定义的 logback 配置
- 支持 Spring Boot 环境区分

### References

- [Source: epics.md#Story 6.5] 完整验收标准
- [Source: architecture.md#日志规范] 日志规范要求
- [Source: project-context.md#日志规则] 日志框架和命名规范
- [Source: Story 6-4#Code Review] 测试模式和经验

## Dev Agent Guardrails

### Technical Requirements

**必须实现的核心功能**:

1. 创建 SecurityLogEvent 结构化日志事件类
2. 创建 SensitiveDataMasker 敏感数据脱敏工具
3. 创建 SecurityLogger 接口和默认实现
4. 创建 SecurityLoggingProperties 配置类
5. 集成到认证/授权流程中

**技术约束**:

- 必须使用 SLF4J，禁止使用 System.out.println()
- 必须使用构造器依赖注入，禁止 @Autowired 字段注入
- 日志记录器必须是线程安全的
- 敏感数据必须脱敏后才能记录

**性能要求**:

- 日志记录不应影响主流程性能
- 结构化日志序列化应 < 5ms
- 脱敏操作应 < 1ms

### Architecture Compliance

**模块架构**:

- 日志组件位于 security-core 模块的 logging 子包
- 不需要创建单独的模块（日志是核心功能的一部分）
- 遵循 Spring Boot Logging 自动配置模式

**依赖管理**:

- 父 POM 管理依赖版本
- SLF4J 和 Logback 由 Spring Boot 默认提供
- Jackson 可选（用于 JSON 格式日志）

**日志格式模式**:

- 结构化日志使用 JSON 格式
- 包含标准字段：event_type、timestamp、level、message
- 支持自定义字段扩展

### Library & Framework Requirements

**核心库版本**:

- Spring Boot 2.7.18（必须精确）
- SLF4J API（Spring Boot 默认版本）
- Logback（Spring Boot 默认版本）
- Jackson（Spring Boot 默认版本）

**API 兼容性**:

- 必须兼容 SLF4J API
- 必须兼容 Logback 配置
- 支持 Spring Boot Logging 配置

### File Structure Requirements

**目录结构**:

```
security-core/src/main/java/com/original/security/logging/
├── SecurityLogEvent.java              # 结构化日志事件
├── SecurityLogField.java              # 日志字段枚举
├── SecurityLogLevel.java              # 日志级别枚举
├── SecurityLogger.java                # 日志记录器接口
├── DefaultSecurityLogger.java         # 默认日志记录器实现
├── SensitiveDataMasker.java           # 敏感数据脱敏工具
├── SecurityLoggingListener.java       # 安全日志事件监听器
├── config/
│   ├── SecurityLoggingProperties.java # 日志配置属性
│   ├── SecurityLoggingAutoConfiguration.java  # 自动配置
│   ├── AsyncLoggingConfiguration.java # 异步日志线程池配置
│   └── package-info.java
└── package-info.java
```

**测试文件结构**:

```
security-core/src/test/java/com/original/security/logging/
├── SecurityLogEventTest.java
├── SensitiveDataMaskerTest.java
├── DefaultSecurityLoggerTest.java
├── SecurityLoggingPropertiesTest.java
└── config/
    └── SecurityLoggingAutoConfigurationTest.java
```

**命名规范**:

- 日志事件类：SecurityLogEvent（PascalCase）
- 脱敏工具类：SensitiveDataMasker（PascalCase）
- 配置属性类：SecurityLoggingProperties（PascalCase）
- 包名：com.original.security.logging（全小写）

### Testing Requirements

**测试框架配置**:

- 使用 JUnit 5 (@Test)
- 使用 Mockito 模拟依赖
- 使用 Logback ListAppender 捕获日志

**测试场景覆盖**:

1. SecurityLogEvent 正确创建和序列化
2. SensitiveDataMasker 正确脱敏各种敏感数据
3. SecurityLogger 支持不同日志级别
4. MDC 上下文正确传播
5. 认证成功/失败时正确记录日志
6. 敏感数据不在日志中出现

**测试数据准备**:

- 测试用户：admin/ADMIN 角色
- 测试密码：password123（验证脱敏）
- 测试 JWT Token：固定测试 Token

**代码质量检查清单**:

- [x] 使用构造器依赖注入
- [x] 方法长度 < 50 行
- [x] 异常被正确处理（使用日志框架）
- [x] 没有魔法值
- [x] 公共 API 有 JavaDoc
- [x] 测试覆盖率 ≥ 80%

## Story Completion Status

### 任务完成状态

**已完成的任务**:

- [x] 确定目标故事（Story 6.5: 结构化日志）
- [x] 加载和分析核心文档
- [x] 实现所有日志组件
- [x] 编写单元测试和集成测试
- [x] 编写日志文档和配置示例

### 最终文档确认

**Story 文件**: `6-5-structured-logging.md`
**Story ID**: 6.5
**Story Key**: 6-5-structured-logging
**状态**: ready-for-dev
**Epic**: Epic 6 - 可观测性与测试支持
**验收标准数量**: 4个
**任务数量**: 6个（含子任务）

## Dev Agent Record

### Agent Model Used

{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

- 2026-03-12: Story implementation completed
- All 113 tests passing (BUILD SUCCESS)
- Implemented: SecurityLogEvent, SecurityLogField, SecurityLogLevel, SensitiveDataMasker, SecurityLogger, DefaultSecurityLogger, SecurityLoggingListener, SecurityLoggingProperties, SecurityLoggingAutoConfiguration

### File List

**Source Files Created:**
- `security-core/src/main/java/com/original/security/logging/SecurityLogLevel.java`
- `security-core/src/main/java/com/original/security/logging/SecurityLogField.java`
- `security-core/src/main/java/com/original/security/logging/SecurityLogEvent.java`
- `security-core/src/main/java/com/original/security/logging/SensitiveDataMasker.java`
- `security-core/src/main/java/com/original/security/logging/SecurityLogger.java`
- `security-core/src/main/java/com/original/security/logging/DefaultSecurityLogger.java`
- `security-core/src/main/java/com/original/security/logging/SecurityLoggingListener.java`
- `security-core/src/main/java/com/original/security/logging/config/SecurityLoggingProperties.java`
- `security-core/src/main/java/com/original/security/logging/config/SecurityLoggingAutoConfiguration.java`
- `security-core/src/main/java/com/original/security/logging/config/AsyncLoggingConfiguration.java`
- `security-core/src/main/java/com/original/security/logging/config/package-info.java`
- `security-core/src/main/java/com/original/security/logging/package-info.java`
- `security-core/src/main/resources/META-INF/spring.factories`

**Test Files Created:**
- `security-core/src/test/java/com/original/security/logging/SecurityLogLevelTest.java`
- `security-core/src/test/java/com/original/security/logging/SecurityLogFieldTest.java`
- `security-core/src/test/java/com/original/security/logging/SecurityLogEventTest.java`
- `security-core/src/test/java/com/original/security/logging/SensitiveDataMaskerTest.java`
- `security-core/src/test/java/com/original/security/logging/DefaultSecurityLoggerTest.java`
- `security-core/src/test/java/com/original/security/logging/SecurityLoggingListenerTest.java`
- `security-core/src/test/java/com/original/security/logging/config/SecurityLoggingPropertiesTest.java`
- `security-core/src/test/java/com/original/security/logging/config/SecurityLoggingAutoConfigurationTest.java`

## Change Log

| Date | Status | Notes |
|------|--------|-------|
| 2026-03-12 | review | Implementation completed, all 113 tests passing |
| 2026-03-12 | review | Code review fixes: added SecurityLoggingAutoConfigurationTest, implemented includeStackTrace config, implemented maskingMode support, added @EnableAsync configuration |
| 2026-03-12 | review | Code review fixes #2: Fixed thread safety with AtomicReference, unified event type prefix, added bounded thread pool config, added null check for throwable.getMessage(), added TODO for unused config properties, created SecurityLoggingListenerTest |
| 2026-03-12 | done | Code review fixes #3 (Final): H1-Fixed NPE risk in DefaultSecurityLogger.error with null guard; M1-Clarified unimplemented properties; M2-Enhanced masking to handle non-String sensitive data; L1-Cleaned up redundant checks in SensitiveDataMasker; L2-Fixed JavaDoc encoding issues. |
