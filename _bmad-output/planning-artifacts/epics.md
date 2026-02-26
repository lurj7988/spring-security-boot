---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories']
inputDocuments:
  - name: prd.md
    path: _bmad-output/planning-artifacts/prd.md
    type: prd
    loaded: true
  - name: architecture.md
    path: _bmad-output/planning-artifacts/architecture.md
    type: architecture
    loaded: true
workflowType: 'epics'
---

# Spring Security Boot - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Spring Security Boot, decomposing the requirements from the PRD and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

**能力区域 1：认证 (4个)**
- FR1: 开发者可以使用用户名密码进行用户认证
- FR2: 开发者可以使用 JWT Token 进行无状态认证
- FR3: 开发者可以使用 Session 进行有状态认证
- FR4: 系统支持 Remember Me 功能以延长用户会话

**能力区域 2：授权 (5个)**
- FR6: 开发者可以使用注解定义方法级权限要求
- FR7: 开发者可以使用注解定义角色级访问控制
- FR8: 系统支持基于角色的访问控制（RBAC）
- FR9: 系统管理员可以创建和管理角色
- FR10: 系统管理员可以分配权限给角色

**能力区域 3：网络安全 (6个)**
- FR11: 框架自动配置 CORS（跨域资源共享）策略
- FR12: 框架自动启用 CSRF（跨站请求伪造）防护
- FR13: 框架自动配置 XSS（跨站脚本）防护
- FR14: 框架自动配置安全响应头
- FR15: 框架记录认证成功/失败审计事件
- FR16: 框架记录授权失败审计事件

**能力区域 4：配置管理 (4个)**
- FR17: 开发者可以使用注解启用安全配置
- FR18: 框架在启动时验证必填配置
- FR19: 框架提供清晰的配置错误提示和修复建议
- FR20: 框架提供配置默认值以减少必需配置项

**能力区域 5：开发者体验 (5个)**
- FR21: 框架提供配置验证工具
- FR22: 框架提供快速开始文档
- FR23: 框架提供核心概念说明文档
- FR24: 框架提供配置参考文档
- FR25: 框架提供 API 参考文档

**能力区域 6：测试支持 (4个)**
- FR26: 开发者可以在测试中使用 Mock 用户
- FR27: 框架提供认证测试工具类
- FR28: 框架提供安全测试切片
- FR29: 框架提供性能基准测试

**能力区域 7：可观测性 (5个)**
- FR30: 框架暴露认证成功/失败 Metrics
- FR31: 框架暴露认证耗时 Metrics
- FR32: 框架支持分布式追踪（Micrometer Tracing）
- FR33: 框架提供结构化日志
- FR34: 框架提供健康检查端点

**能力区域 8：扩展性 (3个)**
- FR35: 开发者可以实现 AuthenticationProvider 添加新的认证方式
- FR36: 开发者可以实现 UserDetailsService 自定义用户加载
- FR37: 开发者可以实现 PasswordEncoder 自定义密码加密

**能力区域 9：密码安全 (4个)**
- FR38: 框架使用 BCrypt 加密用户密码
- FR39: 框架禁止使用弱加密算法（MD5、SHA1）
- FR40: 框架验证 Token 签名
- FR41: 框架自动生成安全密钥（或警告用户配置）

**能力区域 10：会话管理 (4个)**
- FR42: 用户可以主动登出
- FR43: 系统支持 Token 刷新机制
- FR44: 系统支持查询当前用户会话
- FR45: 系统管理员可以踢出指定用户

**能力区域 11：用户管理 (4个)**
- FR46: 开发者可以使用框架提供的用户创建 API
- FR47: 开发者可以使用框架提供的用户查询 API
- FR48: 开发者可以使用框架提供的密码修改 API
- FR49: 开发者可以使用框架提供的密码重置 API

**能力区域 12：认证端点 (3个)**
- FR50: 框架提供可选的登录端点
- FR51: 框架提供可选的登出端点
- FR52: 框架提供可选的 Token 刷新端点

**能力区域 13：示例与指南 (10个)**
- FR53: 框架提供一键运行示例项目
- FR54: 框架提供故障排查指南
- FR55: 框架提供常见问题解答文档
- FR56: 文档包含完整的代码示例
- FR57: 框架提供插件开发指南
- FR58: 框架提供贡献者指南
- FR59: 开发者可以配置日志级别
- FR60: 开发者可以配置日志格式
- FR61: 框架提供等保 2.0 合规检查清单
- FR62: 框架提供安全配置最佳实践文档

### NonFunctional Requirements

**性能 (4个)**
- NFR-PERF-001: P50 < 100ms, P95 < 200ms, P99 < 500ms
- NFR-PERF-002: 框架启动时间 < 5秒，自动配置 < 3秒
- NFR-PERF-003: 支持 500+ QPS，100+ 并发连接
- NFR-PERF-004: 提供性能基准测试（JMH）

**安全 (6个)**
- NFR-SEC-001: BCrypt 加密（强度≥10），禁止弱加密算法
- NFR-SEC-002: JWT Token HS256+，过期≤60分钟，支持刷新
- NFR-SEC-003: 生产环境强制 HTTPS
- NFR-SEC-004: 安全响应头完整配置
- NFR-SEC-005: 依赖安全扫描，高危漏洞24小时响应
- NFR-SEC-006: 支持等保 2.0 二级认证

**可扩展性 (3个)**
- NFR-SCALE-001: 支持 10x 用户增长，性能退化 < 10%
- NFR-SCALE-002: 支持 MySQL（默认），可扩展其他数据库
- NFR-SCALE-003: 双版本支持（Spring Boot 2.x/3.x）

**集成 (3个)**
- NFR-INT-001: 基于 Spring Security 标准 API
- NFR-INT-002: 兼容 Spring Boot Actuator 和 Spring Cloud Alibaba
- NFR-INT-003: 使用 Spring Data JPA 和 Repository 模式

**可靠性 (3个)**
- NFR-REL-001: 6个月迁移窗口，Breaking Changes 提前6个月通知
- NFR-REL-002: 清晰错误提示 + 修复建议
- NFR-REL-003: 暴露 Metrics（集成 Micrometer）

**可维护性 (2个)**
- NFR-MAINT-001: 构造器依赖注入，核心测试覆盖率≥90%
- NFR-MAINT-002: 提供快速开始、API参考、配置参考文档

### Additional Requirements

**技术栈要求：**
- Spring Boot 2.7.18（目标版本）
- Spring Security 5.8.x
- Java 1.8+ 基线
- javax.* 命名空间（非 Jakarta）

**架构决策要求：**
- 插件化认证系统：AuthenticationPlugin 接口
- ConfigProvider 接口：解耦组件依赖
- API-Impl 分离模式：支持微服务场景
- 构造器依赖注入：100% 强制执行

**模块结构要求：**
- security-core：认证引擎核心
- security-components：默认实现（开箱即用）
- security-plugins：扩展插件
- security-network：网络安全模块

**实施规范要求（15条规则）：**
- 命名规范：数据库 snake_case，API 复数形式
- 结构规范：包结构、配置文件组织
- 格式规范：统一 API 响应格式
- 流程规范：错误处理、日志规范
- 测试规范：测试覆盖率要求

**技术债务：**
- P0: 升级到 Spring Boot 2.7.18
- P0: 核心安全代码添加测试
- P1: 构造器注入改造
- P1: 配置验证机制

**Sprint 0 核心任务：**
- AuthenticationPlugin 接口详细定义
- ConfigProvider 接口详细定义

### FR Coverage Map

FR1: Epic 1 - 用户名密码认证
FR2: Epic 1 - JWT Token认证
FR3: Epic 4 - Session认证
FR4: Epic 4 - Remember Me功能
FR6: Epic 3 - 方法级权限注解
FR7: Epic 3 - 角色级访问控制
FR8: Epic 3 - RBAC支持
FR9: Epic 3 - 角色管理
FR10: Epic 3 - 权限分配
FR11: Epic 2 - CORS自动配置
FR12: Epic 2 - CSRF防护
FR13: Epic 2 - XSS防护
FR14: Epic 2 - 安全响应头
FR15: Epic 6 - 认证审计事件
FR16: Epic 6 - 授权审计事件
FR17: Epic 1 - 注解启用安全配置
FR18: Epic 1 - 启动时验证配置
FR19: Epic 1 - 清晰错误提示
FR20: Epic 1 - 配置默认值
FR21: Epic 7 - 配置验证工具
FR22: Epic 7 - 快速开始文档
FR23: Epic 7 - 核心概念文档
FR24: Epic 7 - 配置参考文档
FR25: Epic 7 - API参考文档
FR26: Epic 6 - Mock用户测试
FR27: Epic 6 - 认证测试工具类
FR28: Epic 6 - 安全测试切片
FR29: Epic 6 - 性能基准测试
FR30: Epic 6 - 认证Metrics
FR31: Epic 6 - 耗时Metrics
FR32: Epic 6 - 分布式追踪
FR33: Epic 6 - 结构化日志
FR34: Epic 6 - 健康检查端点
FR35: Epic 0 - AuthenticationProvider扩展
FR36: Epic 0 - UserDetailsService扩展
FR37: Epic 0 - PasswordEncoder扩展
FR38: Epic 1 - BCrypt加密
FR39: Epic 1 - 禁止弱加密
FR40: Epic 1 - Token签名验证
FR41: Epic 1 - 安全密钥生成
FR42: Epic 4 - 用户登出
FR43: Epic 4 - Token刷新
FR44: Epic 4 - 会话查询
FR45: Epic 4 - 踢人下线
FR46: Epic 5 - 用户创建API
FR47: Epic 5 - 用户查询API
FR48: Epic 5 - 密码修改API
FR49: Epic 5 - 密码重置API
FR50: Epic 2 - 可选登录端点
FR51: Epic 2 - 可选登出端点
FR52: Epic 2 - 可选Token刷新端点
FR53: Epic 7 - 示例项目
FR54: Epic 7 - 故障排查指南
FR55: Epic 7 - 常见问题
FR56: Epic 7 - 代码示例
FR57: Epic 7 - 插件开发指南
FR58: Epic 7 - 贡献者指南
FR59: Epic 7 - 日志级别配置
FR60: Epic 7 - 日志格式配置
FR61: Epic 7 - 等保2.0清单
FR62: Epic 7 - 安全最佳实践

## Epic List

### Epic 0: 项目启动与数据基础

Sprint 0 核心任务：定义接口规范和创建数据库基础

**FRs covered:** 额外需求（AuthenticationPlugin, ConfigProvider, 数据库命名规范）

### Story 0.1: 定义核心接口

As a 框架开发者，
I want 定义清晰的插件接口和配置接口，
So that 框架具有可扩展性和解耦的组件依赖。

**验收标准：**

**Given** 项目依赖 security-core 模块
**When** 开发者查看 AuthenticationPlugin 接口
**Then** 接口包含 getName()、getAuthenticationProvider()、supports() 方法
**And** 接口有清晰的 JavaDoc 文档
**And** 接口位于 `com.original.security.plugin` 包

**Given** 项目依赖 security-core 模块
**When** 开发者查看 ConfigProvider 接口
**Then** 接口包含 getConfig()、getProperties() 方法
**And** 接口支持配置源扩展（数据库、配置文件等）
**And** 接口有清晰的 JavaDoc 文档
**And** 接口位于 `com.original.security.config` 包

**Given** 接口定义完成
**When** 生成接口规范文档
**Then** 文档包含接口方法签名
**And** 文档包含使用示例
**And** 文档输出到 `{output_folder}/planning-artifacts/`

**需求覆盖：** FR35, FR36, FR37, 额外需求（AuthenticationPlugin, ConfigProvider）

---

### Story 0.2: 创建核心数据表

As a 框架开发者，
I want 创建核心用户、角色、权限数据表，
So that 后续功能可以直接使用数据基础。

**验收标准：**

**Given** 数据库 schema.sql 文件
**When** 查看表结构
**Then** 创建以下表：
  - users (id, username, password, email, enabled, created_at)
  - roles (id, name, description, created_at)
  - permissions (id, name, description, created_at)
  - user_roles (user_id, role_id)
  - role_permissions (role_id, permission_id)
  - persistent_logins (username, series, token, last_used) - Remember Me

**Given** 表结构
**When** 检查命名规范
**Then** 所有表名使用 snake_case 复数形式
**And** 所有列名使用 snake_case
**And** 外键格式为 {table}_id

**Given** 表约束
**When** 检查索引和约束
**Then** users.username 唯一索引
**And** roles.name 唯一索引
**And** permissions.name 唯一索引
**And** 外键级联删除配置正确

**Given** 数据库初始化
**When** 应用首次启动
**Then** 自动执行 schema.sql
**And** 创建所有必需的表
**And** 插入默认管理员用户（可选）

**需求覆盖：** 额外需求（数据库命名规范，基础数据结构）

---

### Epic 1: 框架基础与核心认证

开发者可以在30分钟内集成框架，实现用户名密码和JWT认证

**FRs covered:** FR1, FR2, FR17, FR18, FR19, FR20, FR35, FR36, FR37, FR38, FR39, FR40, FR41

**注意：** Story 1.1 已移至 Epic 0，以下为 Epic 1 的其他 Story

### Story 1.1: 实现配置验证与启动检查

As a 开发者集成框架，
I want 框架在启动时验证必填配置并给出清晰错误提示，
So that 框架具有可扩展性和解耦的组件依赖。

**验收标准：**

**Given** 项目依赖 security-core 模块
**When** 开发者查看 AuthenticationPlugin 接口
**Then** 接口包含 getName()、getAuthenticationProvider()、supports() 方法
**And** 接口有清晰的 JavaDoc 文档
**And** 接口位于 `com.original.security.plugin` 包

**Given** 项目依赖 security-core 模块
**When** 开发者查看 ConfigProvider 接口
**Then** 接口包含 getConfig()、getProperties() 方法
**And** 接口支持配置源扩展（数据库、配置文件等）
**And** 接口有清晰的 JavaDoc 文档
**And** 接口位于 `com.original.security.config` 包

**Given** 接口定义完成
**When** 生成接口规范文档
**Then** 文档包含接口方法签名
**And** 文档包含使用示例
**And** 文档输出到 `{output_folder}/planning-artifacts/`

**需求覆盖：** FR35, FR36, FR37, 额外需求（AuthenticationPlugin, ConfigProvider）

---

### Story 1.2: 实现配置验证与启动检查

As a 开发者集成框架，
I want 框架在启动时验证必填配置并给出清晰错误提示，
So that 我能在配置错误时快速定位问题。

**验收标准：**

**Given** 应用启动且 security.config.validation=true（默认）
**When** 数据库连接配置缺失
**Then** 启动失败并显示格式化错误信息
**And** 错误信息包含问题描述
**And** 错误信息包含修复建议（具体配置项示例）
**And** 错误信息包含文档链接

**Given** 应用启动
**When** 所有必填配置正确
**Then** 应用正常启动
**And** 控制台打印配置摘要日志

**Given** 配置项有默认值
**When** 开发者未配置该选项
**Then** 框架使用默认值
**And** 启动时打印使用的默认值

**需求覆盖：** FR18, FR19, FR20, NFR-REL-002

---

### Story 1.3: 实现 @EnableSecurityBoot 注解

As a 开发者集成框架，
I want 使用一个注解启用安全配置，
So that 我不需要手动配置复杂的 SecurityFilterChain。

**验收标准：**

**Given** 项目添加了 @EnableSecurityBoot 注解
**When** 应用启动
**Then** 自动配置 AuthenticationManager
**And** 自动配置 SecurityFilterChain
**And** 自动配置 PasswordEncoder（BCrypt）

**Given** 使用了 @EnableSecurityBoot 注解
**When** 查看注解定义
**Then** 注解位于 `com.original.frame.annotation` 包
**And** 注解导入必要的配置类
**And** 注解有清晰的 JavaDoc 和使用示例

**Given** 注解配置完成
**When** 自定义配置需要覆盖默认行为
**Then** 开发者可以通过配置属性覆盖
**And** 支持外部化配置（application.properties）

**需求覆盖：** FR17, FR38, NFR-INT-001

---

### Story 1.4: 实现用户名密码认证

As a 终端用户，
I want 使用用户名和密码登录，
So that 我可以访问受保护的资源。

**验收标准：**

**Given** 用户数据库存在且用户已注册
**When** 用户提交正确的用户名和密码
**Then** 认证成功
**And** 返回认证成功的响应
**And** 密码使用 BCrypt（强度≥10）验证

**Given** 用户提交错误的密码
**When** 认证失败
**Then** 返回 401 Unauthorized
**And** 错误信息不泄露用户是否存在
**And** 审计事件被记录（FR15）

**Given** 用户账号被禁用
**When** 尝试登录
**Then** 认证失败并返回明确提示
**And** 错误码区分"账号禁用"和"密码错误"

**Given** 认证成功
**When** 检查密码处理
**Then** 密码在日志中已脱敏（不记录明文）
**And** 不使用 MD5 或 SHA1 等弱加密算法
**And** 使用构造器依赖注入

**需求覆盖：** FR1, FR38, FR39, FR40, NFR-SEC-001

---

### Story 1.5: 实现 JWT 认证插件

As a 开发者构建前后端分离应用，
I want 使用 JWT Token 进行无状态认证，
So that 我的 API 可以支持无状态访问。

**验收标准：**

**Given** JwtAuthenticationPlugin 已注册
**When** 用户登录成功
**Then** 生成 JWT Token
**And** Token 使用 HS256 或更强算法签名
**And** Token 包含用户名、角色、过期时间
**And** Token 默认过期时间 ≤ 60 分钟

**Given** 客户端请求携带 JWT Token
**When** JwtAuthenticationFilter 验证 Token
**Then** Token 有效则通过认证
**And** Token 过期则返回 401 和明确错误码
**And** Token 签名无效则拒绝访问

**Given** JWT 配置
**When** 查看 JwtUtils 类
**Then** 提供生成 Token 方法
**And** 提供验证 Token 方法
**And** 提供解析 Token 方法
**And** 使用构造器依赖注入

**Given** JWT 密钥配置
**When** 未配置 security.jwt.secret
**Then** 框架生成安全密钥并警告
**And** 或启动失败并提示配置（更安全的选择）

**需求覆盖：** FR2, FR40, FR41, NFR-SEC-002

---

### Story 1.6: 实现默认认证端点

As a 开发者，
I want 使用框架提供的默认认证端点，
So that 我不需要自己编写登录/登出 API。

**验收标准：**

**Given** security.endpoints.enabled=true（默认）
**When** POST /api/auth/login 携带用户名密码
**Then** 返回 JWT Token（如果使用 JWT 认证）
**And** 返回用户基本信息
**And** 返回统一格式的响应 {code, message, data}

**Given** 用户已登录
**When** POST /api/auth/logout
**Then** 会话被清除
**And** 返回成功响应

**Given** Token 即将过期
**When** POST /api/auth/refresh 携带 refresh token
**Then** 返回新的 access token
**And** refresh token 轮换（可选）

**Given** 不想使用默认端点
**When** 设置 security.endpoints.enabled=false
**Then** 端点不注册
**And** 开发者可以自己实现

**需求覆盖：** FR50, FR51, FR52, FR42, FR43

---

### Epic 2: 网络安全一体化

开发者获得开箱即用的网络安全防护（CORS/CSRF/XSS），无需手动配置

**FRs covered:** FR11, FR12, FR13, FR14, FR50, FR51, FR52

### Story 2.1: 实现 CORS 自动配置

As a 前端开发者调用后端 API，
I want 框架自动配置 CORS 策略，
So that 我不需要手动处理跨域问题。

**验收标准：**

**Given** security.network.cors.enabled=true（默认）
**When** 配置了 security.network.cors.allowed-origins
**Then** CORS 策略自动生效
**And** 允许的域名可以访问 API
**And** 支持通配符（*）用于开发环境

**Given** CORS 已启用但未配置 allowed-origins
**When** 应用启动
**Then** 启动失败并显示格式化错误
**And** 错误信息说明必须配置 allowed-origins
**And** 错误信息提供配置示例

**Given** 前端请求携带自定义头
**When** CORS 配置正确
**Then** 预检请求（OPTIONS）返回正确响应
**And** 响应包含 Access-Control-Allow-Origin 头
**And** 支持配置允许的 HTTP 方法和请求头

**需求覆盖：** FR11, NFR-SEC-004

---

### Story 2.2: 实现 CSRF 防护

As a 开发者构建安全的 Web 应用，
I want 框架自动启用 CSRF 保护，
So that 应用免受跨站请求伪造攻击。

**验收标准：**

**Given** security.network.csrf.enabled=true（默认）
**When** 用户执行状态变更操作（POST/PUT/DELETE）
**Then** 请求必须包含有效的 CSRF Token
**And** Token 无效则返回 403 Forbidden
**And** 审计事件被记录（FR16）

**Given** 使用表单提交
**When** 渲染表单
**Then** 表单包含隐藏的 CSRF Token 字段
**And** Token 通过 CsrfTokenRepository 生成

**Given** 构建 RESTful API（无状态）
**When** 设置 security.network.csrf.enabled=false
**Then** CSRF 保护被禁用
**And** 启动时打印警告日志

**Given** CSRF Token 配置
**When** 查看 security.network.csrf.token-header
**Then** 可以自定义 Token 请求头名称（默认 X-CSRF-TOKEN）
**And** 配置有合理的默认值

**需求覆盖：** FR12, NFR-SEC-004, NFR-SEC-006

---

### Story 2.3: 实现 XSS 防护和安全响应头

As a 开发者，
I want 框架自动配置安全响应头，
So that 应用符合安全最佳实践。

**验收标准：**

**Given** security.network.headers.enabled=true（默认）
**When** 响应返回给客户端
**Then** 包含 X-Frame-Options: DENY
**And** 包含 X-Content-Type-Options: nosniff
**And** 包含 Strict-Transport-Security: max-age=31536000
**And** 包含 X-XSS-Protection: 1; mode=block

**Given** 安全响应头配置
**When** 某个响应头需要自定义
**Then** 可以通过配置覆盖默认值
**And** 配置项命名清晰（security.network.headers.*）

**Given** 启用内容安全策略（CSP）
**When** 配置 security.network.csp.enabled=true
**Then** 响应包含 Content-Security-Policy 头
**And** 默认策略为 default-src 'self'
**And** 可以自定义策略

**Given** XSS 防护机制
**When** 检查输入处理
**Then** 框架提供输入转义工具类
**And** 文档说明如何正确处理用户输入

**需求覆盖：** FR13, FR14, NFR-SEC-004, NFR-SEC-006

---

### Story 2.4: 实现全局安全过滤器

As a 框架开发者，
I want 统一的安全过滤器链管理所有安全功能，
So that 安全功能的执行顺序和优先级一致。

**验收标准：**

**Given** 多个安全过滤器（CORS、CSRF、SecurityHeaders）
**When** 请求进入
**Then** 过滤器按正确顺序执行
**And** CORS 过滤器最先执行
**And** CSRF 和 SecurityHeaders 在认证之前执行

**Given** 过滤器链配置
**When** 查看 SecurityFilterChain 配置
**Then** 使用 Spring Security 标准 FilterChain
**And** 过滤器通过 @Order 注解控制顺序
**And** 配置清晰易读

**Given** 自定义过滤器需要集成
**When** 开发者添加自定义安全过滤器
**Then** 可以通过实现标准接口集成
**And** 可以指定过滤器优先级
**And** 文档提供集成示例

**Given** 过滤器性能
**When** 请求通过过滤器链
**Then** 每个过滤器执行时间 < 10ms
**And** 不阻塞正常请求
**And** 满足 NFR-PERF-001 (P95 < 200ms)

**需求覆盖：** FR11, FR12, FR13, FR14（整合所有网络安全功能）

---

### Epic 3: 授权与权限控制

开发者可以轻松实现基于角色的访问控制（RBAC）

**FRs covered:** FR6, FR7, FR8, FR9, FR10

### Story 3.1: 实现 @PreAuthorize 权限注解支持

As a 开发者，
I want 使用注解定义方法级权限要求，
So that 我可以简洁地控制访问权限。

**验收标准：**

**Given** 方法使用 @PreAuthorize("hasRole('ADMIN')") 注解
**When** ADMIN 角色用户调用该方法
**Then** 方法正常执行
**And** 非 ADMIN 用户调用返回 403 Forbidden

**Given** 方法使用 @PreAuthorize("hasAuthority('user:write')") 注解
**When** 用户拥有 user:write 权限
**Then** 方法正常执行
**And** 审计事件被记录（FR16）

**Given** 权限注解配置
**When** 启用 @EnableGlobalMethodSecurity
**Then** @PreAuthorize 注解生效
**And** 支持 SpEL 表达式
**And** 与 Spring Security 标准注解兼容

**需求覆盖：** FR6, FR7, NFR-INT-001

---

### Story 3.2: 实现角色和权限数据模型

As a 框架开发者，
I want 定义清晰的角色和权限数据模型，
So that RBAC 系统有数据基础。

**验收标准：**

**Given** 数据库表结构
**When** 查看 roles 表
**Then** 包含 id、name、description、created_at 字段
**And** name 字段唯一（UNIQUE）
**And** 使用 snake_case 命名

**Given** 数据库表结构
**When** 查看 permissions 表
**Then** 包含 id、name、description、created_at 字段
**And** name 字段唯一
**And** 支持层级权限（如 user:read, user:write）

**Given** 关联表
**When** 查看用户角色关联表
**Then** 包含 user_id、role_id 外键
**And** 联合主键（user_id, role_id）
**And** 级联删除配置正确

**Given** 关联表
**When** 查看角色权限关联表
**Then** 包含 role_id、permission_id 外键
**And** 联合主键（role_id, permission_id）
**And** 支持角色拥有多个权限

**需求覆盖：** FR8, 额外需求（数据库命名规范）

---

### Story 3.3: 实现权限评估服务

As a 开发者，
I want 框架提供权限检查服务，
So that 我可以在代码中动态检查权限。

**验收标准：**

**Given** PermissionService 服务
**When** 调用 hasPermission(String username, String permission)
**Then** 返回用户是否拥有该权限
**And** 从数据库加载权限数据
**And** 支持权限缓存

**Given** RoleService 服务
**When** 调用 hasRole(String username, String role)
**Then** 返回用户是否拥有该角色
**And** 支持直接角色和继承角色

**Given** 服务配置
**When** 查看服务实现
**Then** 使用构造器依赖注入
**And** 与 AccessDecisionManager 集成
**And** 与 Spring Security 标准接口兼容

**需求覆盖：** FR8, NFR-INT-001, NFR-MAINT-001

---

### Story 3.4: 实现角色管理 API

As a 系统管理员，
I want 通过 API 管理角色和分配权限，
So that 我可以控制系统的访问权限。

**验收标准：**

**Given** 管理员用户
**When** POST /api/roles 创建角色
**Then** 角色创建成功
**And** 返回统一格式响应 {code, message, data}
**And** 角色名称唯一性校验

**Given** 已有角色
**When** POST /api/roles/{roleId}/permissions 分配权限
**Then** 权限分配成功
**And** 可以批量分配多个权限
**And** 操作记录审计日志（FR15）

**Given** 查询角色
**When** GET /api/roles/{roleId}
**Then** 返回角色详情
**And** 包含关联的权限列表
**And** 支持分页查询

**需求覆盖：** FR9, FR10

---

### Story 3.5: 实现动态权限加载

As a 开发者，
I want 框架从数据库动态加载权限，
So that 权限变更无需重启应用。

**验收标准：**

**Given** 用户登录
**When** 加载用户权限
**Then** 从数据库查询用户角色和权限
**And** 权限数据被缓存
**And** 缓存有过期时间

**Given** 管理员修改角色权限
**When** 权限变更提交
**Then** 清除相关缓存
**And** 下次请求加载新权限
**Or** 提供手动刷新接口

**Given** 权限加载性能
**When** 检查权限检查性能
**Then** 权限检查 < 5ms（使用缓存）
**And** 不影响认证响应时间（NFR-PERF-001）

**需求覆盖：** FR8, FR9, FR10, NFR-PERF-001

---

### Epic 4: 会话管理

开发者可以管理用户会话，支持Session模式和会话控制

**FRs covered:** FR3, FR4, FR42, FR43, FR44, FR45

### Story 4.1: 实现 Session 认证插件

As a 开发者构建传统 Web 应用，
I want 使用 Session 进行有状态认证，
So that 我的用户可以通过会话保持登录状态。

**验收标准：**

**Given** SessionAuthenticationPlugin 已注册
**When** 用户登录成功
**Then** 创建用户会话
**And** Session 存储在服务器端
**And** 客户端通过 JSESSIONID Cookie 识别会话

**Given** Session 配置
**When** 查看 Session 配置选项
**Then** 可以配置 Session 过期时间
**And** 可以配置 Session 存储方式（内存/Redis）
**And** 默认过期时间为 30 分钟

**Given** 用户会话存在
**When** 请求携带有效 Session ID
**Then** 用户自动通过认证
**And** 无需重复登录

**Given** Session 过期
**When** 用户请求携带过期 Session
**Then** 返回 401 Unauthorized
**And** 提示重新登录

**需求覆盖：** FR3, NFR-SEC-002

---

### Story 4.2: 实现 Remember Me 功能

As a 终端用户，
I want 勾选"记住我"后7天内免登录，
So that 我不需要频繁输入密码。

**验收标准：**

**Given** 登录表单
**When** 用户勾选"记住我"并登录成功
**Then** 生成 Remember Me Token
**Then** Token 有效期为 7 天（可配置）
**And** Token 持久化到数据库

**Given** Remember Me Token 存储
**When** 查看数据库表
**Then** 存储在 persistent_logins 表
**And** 包含 username、series、token、last_used 字段
**And** 使用 snake_case 命名

**Given** 用户关闭浏览器后重新打开
**When** Remember Me Token 有效
**Then** 用户自动登录
**And** 无需重新输入密码

**Given** Remember Me 配置
**When** 查看 security.remember-me 配置
**Then** 可以配置 Token 有效期（默认 7 天）
**And** 可以启用/禁用功能
**And** 配置项有合理默认值

**需求覆盖：** FR4, NFR-SEC-002

---

### Story 4.3: 实现会话查询 API

As a 管理员，
I want 查询当前活跃的用户会话，
So that 我可以监控系统使用情况。

**验收标准：**

**Given** 管理员用户
**When** GET /api/sessions 查询所有会话
**Then** 返回所有活跃会话列表
**And** 包含用户名、登录时间、最后活跃时间
**And** 支持分页查询

**Given** 普通用户
**When** GET /api/sessions/me 查询自己的会话
**Then** 返回当前用户的所有会话
**And** 不返回其他用户的会话
**And** 支持多设备登录场景

**Given** 会话信息
**When** 查看会话详情
**Then** 包含会话 ID、用户信息、IP 地址、登录时间
**And** 返回统一格式响应

**需求覆盖：** FR44, FR42

---

### Story 4.4: 实现踢人下线功能

As a 管理员，
I want 强制指定用户下线，
So that 我可以处理异常情况或安全事件。

**验收标准：**

**Given** 管理员用户
**When** POST /api/sessions/{userId}/kick 踢出用户
**Then** 指定用户的所有会话被清除
**And** 用户下次请求需要重新登录
**And** 操作记录审计日志（FR15）

**Given** 踢出会话
**When** 被踢用户尝试请求
**Then** 返回 401 Unauthorized
**And** 错误信息提示"账号已在其他设备登录"

**Given** 管理员用户
**When** POST /api/sessions/{sessionId}/kick 踢出指定会话
**Then** 仅指定会话被清除
**And** 用户其他会话保持有效
**And** 支持单端踢出

**Given** 踢出事件
**When** 会话被踢出
**Then** 发布会话过期事件
**And** 事件包含用户 ID、会话 ID、踢出原因
**And** 系统可以监听事件做后续处理

**需求覆盖：** FR45, FR42

---

### Epic 5: 用户管理默认实现

开发者可以直接使用框架提供的用户/角色/权限管理 API

**FRs covered:** FR46, FR47, FR48, FR49

### Story 5.1: 实现用户数据模型和 Repository

As a 框架开发者，
I want 定义用户数据模型和 Repository，
So that 用户管理功能有数据基础。

**验收标准：**

**Given** 用户数据模型
**When** 查看 User 实体
**Then** 包含 id、username、password、email、enabled、created_at 字段
**Then** username 字段唯一（UNIQUE）
**Then** password 字段存储 BCrypt 加密后的值
**And** 使用 snake_case 命名

**Given** UserRepository 接口
**When** 查看接口定义
**Then** 继承 JpaRepository<User, Long>
**And** 包含 findByUsername() 方法
**And** 包含 existsByUsername() 方法
**And** 使用构造器依赖注入

**Given** 数据库表结构
**When** 查看 users 表
**Then** 字段与实体映射正确
**And** 索引配置合理（username、email）
**And** 支持扩展字段

**需求覆盖：** FR46, NFR-INT-003, 额外需求（数据库命名规范）

---

### Story 5.2: 实现用户创建 API

As a 开发者，
I want 使用框架提供的用户创建 API，
So that 我不需要自己编写用户注册功能。

**验收标准：**

**Given** 用户注册请求
**When** POST /api/users 携带用户信息
**Then** 用户创建成功
**And** 密码使用 BCrypt（强度≥10）加密
**And** 返回创建的用户信息（不包含密码）
**And** 返回统一格式响应

**Given** 用户名已存在
**When** 尝试创建同名用户
**Then** 返回 400 Bad Request
**And** 错误码为 USER_ALREADY_EXISTS
**And** 错误信息清晰友好

**Given** 用户创建配置
**When** 新创建用户
**Then** enabled 默认为 true
**And** 可以配置默认角色
**And** 操作记录审计日志（FR15）

**需求覆盖：** FR46, NFR-SEC-001, NFR-MAINT-001

---

### Story 5.3: 实现用户查询 API

As a 开发者，
I want 使用框架提供的用户查询 API，
So that 我可以获取用户信息。

**验收标准：**

**Given** 已有用户
**When** GET /api/users/{id}
**Then** 返回用户详情
**And** 不返回密码字段
**And** 包含角色信息
**And** 返回统一格式响应

**Given** 用户列表
**When** GET /api/users?page=0&size=10
**Then** 返回分页用户列表
**And** 支持按 username 模糊搜索
**And** 支持按 enabled 状态筛选

**Given** 当前登录用户
**When** GET /api/users/me
**Then** 返回当前用户信息
**And** 基于认证上下文获取
**And** 不需要传递用户 ID

**需求覆盖：** FR47

---

### Story 5.4: 实现密码管理 API

As a 用户，
I want 可以修改自己的密码，
So that 我可以定期更新密码保护账号安全。

**验收标准：**

**Given** 登录用户
**When** POST /api/users/me/password 修改密码
**Then** 验证旧密码正确
**Then** 新密码使用 BCrypt 加密
**And** 密码修改成功
**And** 所有会话失效（需要重新登录）

**Given** 旧密码错误
**When** 尝试修改密码
**Then** 返回 400 Bad Request
**And** 错误码为 INVALID_OLD_PASSWORD
**And** 错误信息清晰

**Given** 管理员用户
**When** POST /api/users/{id}/password/reset 重置用户密码
**Then** 密码被重置为随机生成值或指定值
**And** 可以选择是否强制用户下次登录修改密码
**And** 操作记录审计日志（FR15）

**Given** 密码配置
**When** 验证密码强度
**Then** 可以配置最小长度（默认 8 位）
**And** 可以配置复杂度要求
**And** 密码不满足要求时返回 400

**需求覆盖：** FR48, FR49, NFR-SEC-001

---

### Epic 6: 可观测性与测试支持

开发者可以监控安全事件并编写测试

**FRs covered:** FR15, FR16, FR26, FR27, FR28, FR29, FR30, FR31, FR32, FR33, FR34

### Story 6.1: 实现审计事件发布

As a 开发者，
I want 框架发布审计事件，
So that 我可以记录和监控安全相关操作。

**验收标准：**

**Given** 用户登录成功
**When** 认证完成
**Then** 发布 AuthenticationSuccessEvent
**And** 事件包含用户名、认证方式、时间戳
**And** 事件包含 IP 地址（可选）

**Given** 用户登录失败
**When** 认证失败
**Then** 发布 AuthenticationFailureEvent
**And** 事件包含失败原因、用户名（如可用）
**And** 事件用于异常检测

**Given** 用户访问无权限资源
**When** 授权失败
**Then** 发布 AuthorizationFailureEvent
**And** 事件包含用户、资源、所需权限
**And** 审计日志记录

**Given** 事件发布机制
**When** 查看事件配置
**Then** 使用 Spring ApplicationEventPublisher
**And** 应用可以监听事件做后续处理
**And** 不影响认证性能

**需求覆盖：** FR15, FR16, NFR-REL-003

---

### Story 6.2: 实现 Metrics 指标

As a 运维工程师，
I want 监控认证和授权的 Metrics，
So that 我可以了解系统安全状态。

**验收标准：**

**Given** Micrometer 已集成
**When** 查看认证 Metrics
**Then** security.authentication.success（计数器）
**And** security.authentication.failure（计数器）
**And** security.authentication.duration（分布）

**Given** Prometheus 抓取
**When** 访问 /actuator/metrics
**Then** 所有安全 Metrics 可导出
**And** 包含描述信息
**And** 支持标签（tag）如：authentication_type

**Given** 认证性能
**When** 检查 authentication.duration
**Then** P50 < 100ms
**And** P95 < 200ms
**And** P99 < 500ms

**需求覆盖：** FR30, FR31, NFR-PERF-001, NFR-REL-003

---

### Story 6.3: 实现健康检查端点

As a 运维工程师，
I want 通过健康检查端点了解安全组件状态，
So that 我可以快速定位问题。

**验收标准：**

**Given** Spring Boot Actuator 已集成
**When** GET /actuator/health/security
**Then** 返回安全组件健康状态
**And** 包含以下状态：
  - database: UP/DOWN
  - jwtValidator: UP/DOWN
  - cache: UP/DOWN

**Given** 数据库连接断开
**When** 检查健康状态
**Then** security 状态为 DOWN
**And** details 显示具体失败组件
**And** 返回 503 Service Unavailable

**Given** JWT 验证器配置错误
**When** 检查健康状态
**Then** jwtValidator 状态为 DOWN
**And** 错误信息说明配置问题

**需求覆盖：** FR34, NFR-REL-003

---

### Story 6.4: 实现测试支持工具

As a 开发者，
I want 使用测试工具编写安全测试，
So that 我可以验证认证和授权逻辑。

**验收标准：**

**Given** 测试类使用 @WithMockUser(username="admin", roles={"ADMIN"})
**When** 执行测试
**Then** 测试上下文拥有认证用户
**And** 用户拥有指定角色
**And** @PreAuthorize 注解正常工作

**Given** SecurityTest 测试切片
**When** @SecurityTest 注解测试类
**Then** 仅加载安全相关配置
**And** 测试启动更快
**And** 不加载完整的 Web 上下文

**Given** 测试工具类
**When** 使用 AuthenticationTestUtils
**Then** 提供 mockAuthentication() 方法
**And** 提供 withUser() 构建器
**And** 支持 JWT Token mock

**Given** 测试覆盖率
**When** 运行测试
**Then** 核心安全代码覆盖率 ≥ 90%
**And** 认证路径覆盖率 = 100%
**And** 测试通过率 100%

**需求覆盖：** FR26, FR27, FR28, NFR-MAINT-001

---

### Story 6.5: 实现结构化日志

As a 开发者，
I want 框架提供结构化日志，
So that 我可以方便地调试和分析问题。

**验收标准：**

**Given** 日志配置
**When** 用户登录
**Then** 记录结构化日志
**And** 包含字段：event_type、username、success、timestamp
**And** 使用 SLF4J（不用 System.out）

**Given** 敏感数据处理
**When** 记录认证日志
**Then** 密码字段不记录
**And** JWT Token 仅记录前 10 字符
**And** 日志中不含敏感信息

**Given** 日志级别配置
**When** 配置 logging.level.com.original.frame.security
**Then** 支持 DEBUG、INFO、WARN、ERROR
**And** 生产环境默认 INFO
**And** 开发环境可以 DEBUG

**Given** 错误日志
**When** 认证失败
**Then** 记录 ERROR 级别日志
**And** 包含错误堆栈（开发环境）
**And** 不使用 printStackTrace()

**需求覆盖：** FR33, FR59, FR60, NFR-MAINT-001

---

### Story 6.6: 实现分布式追踪

As a 运维工程师，
I want 认证流程包含在分布式追踪中，
So that 我可以追踪跨服务的请求链路。

**验收标准：**

**Given** Micrometer Tracing 已集成
**When** 用户发起登录请求
**Then** 创建认证 Span
**And** Span 名称：authentication/login
**And** 包含用户名标签（脱敏）

**Given** JWT Token 验证
**When** 请求携带 Token
**Then** 创建 JWT 验证 Span
**And** 记录验证耗时
**And** 追踪 ID 传播到下游服务

**Given** 微服务调用
**When** Feign 调用下游服务
**Then** 追踪 ID 自动传播
**And** 下游服务继承上游 TraceContext
**And** 完整链路可追踪

**需求覆盖：** FR32, NFR-INT-002

---

### Epic 7: 开发者体验与文档

开发者可以快速上手并解决常见问题

**FRs covered:** FR21, FR22, FR23, FR24, FR25, FR53, FR54, FR55, FR56, FR57, FR58, FR59, FR60, FR61, FR62

### Story 7.1: 编写快速开始文档

As a 新用户，
I want 有一个 5 分钟快速上手指南，
So that 我可以快速体验框架功能。

**验收标准：**

**Given** 新用户访问文档
**When** 阅读 quick-start.md
**Then** 包含以下内容：
  - 添加 Maven 依赖
  - 配置数据库连接
  - 添加 @EnableSecurityBoot 注解
  - 运行第一个示例

**Given** 快速开始示例
**When** 按照步骤操作
**Then** 30 分钟内完成集成
**And** 不需要深入了解 Spring Security
**And** 示例代码可以运行

**Given** 快速开始文档
**When** 查看文档位置
**Then** 位于 docs/quick-start.md
**And** 在 README.md 首页突出显示
**And** 包含完整的代码示例

**需求覆盖：** FR22, FR56

---

### Story 7.2: 编写配置参考文档

As a 开发者，
I want 有完整的配置参考文档，
So that 我可以找到所有配置项的说明。

**验收标准：**

**Given** 配置参考文档
**When** 查看 configuration.md
**Then** 所有配置项按组分类
**And** 标注必填/可选/高级
**And** 每个配置项包含：
  - 配置键名
  - 默认值
  - 说明
  - 示例

**Given** 配置分组
**When** 查看配置结构
**Then** 分为以下组：
  - security.jdbc.*（数据库配置）
  - security.jwt.*（JWT 配置）
  - security.network.*（网络安全配置）
  - security.endpoints.*（端点配置）

**Given** 配置示例
**When** 查看示例章节
**Then** 提供最小配置示例
**And** 提供完整配置示例
**And** 提供生产环境推荐配置

**需求覆盖：** FR24, FR20

---

### Story 7.3: 编写 API 参考文档

As a 开发者，
Iwant 有完整的 API 参考文档，
So that 我可以正确使用框架提供的 API。

**验收标准：**

**Given** API 参考文档
**When** 查看 api.md
**Then** 包含所有认证端点
**And** 包含所有用户管理端点
**And** 包含所有权限控制端点

**Given** 每个 API 端点
**When** 查看端点文档
**Then** 包含：
  - HTTP 方法和路径
  - 请求参数和请求体
  - 响应格式
  - 错误码说明
  - 示例（curl + Java）

**Given** API 文档格式
**When** 查看文档结构
**Then** 使用 OpenAPI/Markdown 格式
**And** 可以生成 API 文档网站
**And** 支持搜索

**需求覆盖：** FR25, FR56

---

### Story 7.4: 创建快速开始示例项目

As a 新用户，
Iwant 有一个可运行的示例项目，
So that 我可以直接看到框架如何工作。

**验收标准：**

**Given** 示例项目位置
**When** 查看 examples/quick-start
**Then** 项目结构清晰
**And** 包含 README 说明
**And** 可以一键运行（mvn spring-boot:run）

**Given** 示例项目
**When** 运行项目
**Then** 包含登录页面
**And** 包含受保护的 API 端点
**And** 包含权限控制示例

**Given** 前后端分离示例
**When** 查看 examples/jwt-frontend
**Then** Vue3 前端示例
**And** 展示 JWT Token 认证
**And** 展示 CORS 配置

**需求覆盖：** FR53, FR56

---

### Story 7.5: 编写故障排查指南

As a 开发者遇到问题，
Iwant 有一个故障排查指南，
So that 我可以快速解决问题。

**验收标准：**

**Given** 故障排查文档
**When** 查看 troubleshooting.md
**Then** 按错误类型分类
**And** 每个问题包含：
  - 错误现象
  - 可能原因
  - 解决方案
  - 预防措施

**Given** 常见问题
**When** 查看问题列表
**Then** 包含：
  - 连接数据库失败
  - CORS 跨域问题
  - CSRF Token 验证失败
  - JWT Token 过期
  - 权限注解不生效

**Given** 问题解决方案
**When** 查看具体问题
**Then** 提供清晰的修复步骤
**And** 包含代码示例
**And** 包含配置示例

**需求覆盖：** FR54, FR55

---

### Story 7.6: 编写插件开发指南

As a 贡献者，
Iwant 有插件开发指南，
So that 我可以为框架扩展认证方式。

**验收标准：**

**Given** 插件开发文档
**When** 查看 plugin-development.md
**Then** 包含 AuthenticationPlugin 接口说明
**And** 包含完整开发流程
**And** 包含示例代码

**Given** 开发流程
**When** 查看开发步骤
**Then** 步骤清晰：
  1. 创建 Plugin 类
  2. 实现 AuthenticationProvider
  3. 配置注册
  4. 编写测试
  5. 文档更新

**Given** 示例插件
**When** 查看示例
**Then** 包含短信认证示例
**And** 包含第三方登录示例
**And** 代码可以直接参考使用

**需求覆盖：** FR57, FR58

---

### Story 7.7: 编写安全和合规文档

As a 需要通过安全审计的开发者，
Iwant 有安全和合规文档，
So that 我可以确保系统符合安全标准。

**验收标准：**

**Given** 安全文档
**When** 查看 security.md
**Then** 包含等保 2.0 合规清单
**And** 包含安全最佳实践
**And** 包含常见安全问题防范

**Given** 等保 2.0 清单
**When** 查看清单
**Then** 按等保要求分类
**And** 标注框架支持的功能
**And** 说明应用需要做的配置

**Given** 最佳实践
**When** 查看最佳实践
**Then** 包含：
  - 密码策略
  - Token 管理
  - 会话管理
  - 日志和审计
  - HTTPS 配置

**Given** 常见问题
**When** 查看 FAQ
**Then** 回答常见安全问题
**And** 提供配置建议
**And** 引用相关文档

**需求覆盖：** FR61, FR62, NFR-SEC-006

---

## Epic List

### Epic 0: 项目启动与数据基础
Sprint 0 核心任务：定义接口规范和创建数据库基础
**FRs covered:** 额外需求（AuthenticationPlugin, ConfigProvider, 数据库命名规范）

### Epic 1: 框架基础与核心认证
开发者可以在30分钟内集成框架，实现用户名密码和JWT认证
**FRs covered:** FR1, FR2, FR17, FR18, FR19, FR20, FR38, FR39, FR40, FR41

### Epic 2: 网络安全一体化
开发者获得开箱即用的网络安全防护（CORS/CSRF/XSS），无需手动配置
**FRs covered:** FR11, FR12, FR13, FR14, FR50, FR51, FR52

### Epic 3: 授权与权限控制
开发者可以轻松实现基于角色的访问控制（RBAC）
**FRs covered:** FR6, FR7, FR8, FR9, FR10

### Epic 4: 会话管理
开发者可以管理用户会话，支持Session模式和会话控制
**FRs covered:** FR3, FR4, FR42, FR43, FR44, FR45

### Epic 5: 用户管理默认实现
开发者可以直接使用框架提供的用户/角色/权限管理API
**FRs covered:** FR46, FR47, FR48, FR49

### Epic 6: 可观测性与测试支持
开发者可以监控安全事件并编写测试
**FRs covered:** FR15, FR16, FR26, FR27, FR28, FR29, FR30, FR31, FR32, FR33, FR34

### Epic 7: 开发者体验与文档
开发者可以快速上手并解决常见问题
**FRs covered:** FR21, FR22, FR23, FR24, FR25, FR53, FR54, FR55, FR56, FR57, FR58, FR59, FR60, FR61, FR62
