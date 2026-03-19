# Story 7.4: 创建快速开始示例项目

Status: done  # Code review passed with fixes applied - 2026-03-19

Story Key: 7-4-quick-start-example-project
Epic: 7 - 开发者体验与文档

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 新用户，
I want 有一个可运行的示例项目，
so that 我可以直接看到框架如何工作。

## Acceptance Criteria

**AC1: 后端示例项目结构完整**
**Given** 后端示例项目位置
**When** 查看 `examples/quick-start`
**Then** 项目结构清晰
**And** 包含 README 说明
**And** 可以一键运行（`mvn spring-boot:run`）

**AC2: 后端示例功能完整**
**Given** 后端示例项目
**When** 运行项目
**Then** 包含登录 API（`/api/auth/login`）
**And** 包含受保护的 API 端点
**And** 包含权限控制示例（`@PreAuthorize`）
**And** 包含用户管理示例

**AC3: 前端示例项目结构完整**
**Given** 前后端分离示例
**When** 查看 `examples/jwt-frontend`
**Then** 使用 Vue3 + Vite
**And** 包含 README 说明
**And** 可以一键运行（`npm install && npm run dev`）

**AC4: 前端示例功能完整**
**Given** Vue3 前端示例
**When** 运行项目
**Then** 展示 JWT Token 认证流程
**And** 展示登录/登出功能
**And** 展示用户信息获取
**And** 展示 Token 刷新
**And** 展示 CORS 配置

**AC5: 文档入口明确**
**Given** 示例项目
**When** 查看主 README
**Then** 有示例项目链接
**And** `examples/README.md` 提供示例导航

## Tasks / Subtasks

- [x] 创建后端示例项目结构 (AC: #1)
  - [x] 创建 `examples/quick-start/` 目录
  - [x] 创建 `pom.xml`（依赖 security-core）
  - [x] 创建 `src/main/resources/application.properties`（配置示例）
  - [x] 创建 `src/main/resources/data.sql`（初始化数据）
  - [x] 创建 `README.md`（运行说明）

- [x] 实现后端示例代码 (AC: #2)
  - [x] 创建启动类 `QuickStartApplication.java`（@EnableSecurityBoot）
  - [x] 创建示例控制器 `HelloController.java`（公开端点）
  - [x] 创建受保护控制器 `UserController.java`（需要认证）
  - [x] 创建权限控制示例 `AdminController.java`（@PreAuthorize）
  - [x] 创建示例实体和 Repository

- [x] 创建前端示例项目结构 (AC: #3)
  - [x] 创建 `examples/jwt-frontend/` 目录
  - [x] 创建 `package.json`（Vue3 + Axios + Vite）
  - [x] 创建 `vite.config.js`（代理配置）
  - [x] 创建 `README.md`（运行说明）

- [x] 实现前端示例代码 (AC: #4)
  - [x] 创建登录页面 `src/views/Login.vue`
  - [x] 创建首页 `src/views/Home.vue`（用户信息展示）
  - [x] 创建管理页面 `src/views/Admin.vue`（权限控制示例）
  - [x] 创建 API 服务 `src/services/auth.js`
  - [x] 创建路由配置 `src/router/index.js`
  - [x] 创建 Token 存储和刷新逻辑

- [x] 建立文档入口 (AC: #5)
  - [x] 创建 `examples/README.md`（示例导航）
  - [x] 更新主 `README.md` 添加示例链接

## Dev Notes

### Epic Context

**Epic 7: 开发者体验与文档**

- **Epic 目标**: 为开发者提供完整的文档体系，包括快速开始、配置参考、API 参考和示例项目
- **相关 Stories**:
  - 7-1 快速开始文档 (已完成)
  - 7-2 配置参考文档 (已完成)
  - 7-3 API 参考文档 (已完成)
  - 7-4 快速开始示例项目 (当前)
  - 7-5 故障排查指南
- **业务价值**: 让开发者可以直接运行完整示例，快速理解框架用法
- **技术关联**: 示例代码基于 security-core 模块的真实 API

**需求覆盖**: FR53, FR56

### Developer Context

1. **框架 API 位置**:
   - `security-core/src/main/java/com/original/security/annotation/EnableSecurityBoot.java`
   - `security-core/src/main/java/com/original/security/controller/AuthenticationController.java`
   - `security-core/src/main/java/com/original/security/controller/SessionController.java`
   - `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/controller/UserController.java`
   - `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/controller/RoleController.java`

2. **已有文档参考**:
   - `docs/quick-start.md` - 快速开始文档
   - `docs/configuration.md` - 配置参考
   - `docs/api.md` - API 参考

3. **项目技术栈**:
   - 后端: Java 1.8, Spring Boot 2.7.18, Spring Security 5.7.11
   - 前端: Vue3, Vite, Axios
   - 数据库: MySQL

### 示例项目目标结构

**后端示例** (`examples/quick-start/`):
```
examples/quick-start/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/com/example/quickstart/
        │   ├── QuickStartApplication.java
        │   ├── controller/
        │   │   ├── HelloController.java
        │   │   ├── UserController.java
        │   │   └── AdminController.java
        │   └── entity/
        │       └── SampleEntity.java
        └── resources/
            ├── application.properties
            └── data.sql
```

**前端示例** (`examples/jwt-frontend/`):
```
examples/jwt-frontend/
├── package.json
├── vite.config.js
├── README.md
├── index.html
└── src/
    ├── main.js
    ├── App.vue
    ├── router/
    │   └── index.js
    ├── views/
    │   ├── Login.vue
    │   ├── Home.vue
    │   └── Admin.vue
    ├── services/
    │   └── auth.js
    └── stores/
        └── auth.js
```

### Technical Requirements & Constraints

**后端示例约束**:

| 约束项 | 要求 | 原因 |
|-------|------|------|
| Java 版本 | 1.8 | 与框架基线一致 |
| Spring Boot | 2.7.18 | 与框架基线一致 |
| 依赖方式 | 直接依赖 security-core | 简化配置 |
| 配置格式 | application.properties | 与文档一致 |
| 响应格式 | 使用 Response<T> | 与框架统一 |

**前端示例约束**:

| 约束项 | 要求 | 原因 |
|-------|------|------|
| Vue 版本 | 3.x | 最新稳定版 |
| 构建工具 | Vite | 开发体验好 |
| HTTP 客户端 | Axios | 主流选择 |
| 状态管理 | Pinia 或 reactive | Vue3 推荐 |
| Token 存储 | localStorage | 简单演示 |

**功能演示要求**:

1. **登录流程**: 用户名密码登录 → 获取 JWT Token → 存储 Token
2. **Token 使用**: 请求头携带 `Authorization: Bearer {token}`
3. **Token 刷新**: Token 过期前调用 `/api/auth/refresh`
4. **权限控制**: 展示 `@PreAuthorize("hasRole('ADMIN')")` 效果
5. **用户管理**: 展示用户 CRUD 基本操作

### Architecture-Relevant Findings

- 示例项目应展示框架的核心能力，不应包含过多额外功能
- 后端示例应独立可运行，不依赖其他示例
- 前端示例假设后端在 `http://localhost:8080` 运行
- 示例代码应与 `docs/quick-start.md` 中的步骤对应
- 示例应使用框架默认配置，减少用户配置负担

### Source Tree Components To Touch

**必须创建的文件**:

- `examples/README.md`（示例导航）
- `examples/quick-start/pom.xml`
- `examples/quick-start/README.md`
- `examples/quick-start/src/main/java/com/example/quickstart/QuickStartApplication.java`
- `examples/quick-start/src/main/java/com/example/quickstart/controller/HelloController.java`
- `examples/quick-start/src/main/java/com/example/quickstart/controller/UserController.java`
- `examples/quick-start/src/main/java/com/example/quickstart/controller/AdminController.java`
- `examples/quick-start/src/main/resources/application.properties`
- `examples/quick-start/src/main/resources/data.sql`
- `examples/jwt-frontend/package.json`
- `examples/jwt-frontend/README.md`
- `examples/jwt-frontend/vite.config.js`
- `examples/jwt-frontend/index.html`
- `examples/jwt-frontend/src/main.js`
- `examples/jwt-frontend/src/App.vue`
- `examples/jwt-frontend/src/router/index.js`
- `examples/jwt-frontend/src/views/Login.vue`
- `examples/jwt-frontend/src/views/Home.vue`
- `examples/jwt-frontend/src/views/Admin.vue`
- `examples/jwt-frontend/src/services/auth.js`
- `examples/jwt-frontend/src/stores/auth.js`

**需要修改的文件**:

- `README.md`（添加示例链接）

**需要参考的现有文件**:

- `security-core/src/main/java/com/original/security/annotation/EnableSecurityBoot.java`
- `security-core/src/main/java/com/original/security/controller/AuthenticationController.java`
- `docs/quick-start.md`
- `docs/api.md`
- `docs/configuration.md`

### Testing Standards Summary

- 后端示例应能成功启动（`mvn spring-boot:run`）
- 前端示例应能成功启动（`npm run dev`）
- 示例登录流程应能完整演示
- 示例 API 调用应返回正确响应
- README 中的命令应能正确执行

### Project Structure Notes

- 示例项目放在 `examples/` 目录下
- 每个示例有独立的 README.md
- 示例使用简化的配置，方便理解
- 示例代码应有适当注释

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story-74-创建快速开始示例项目]
- [Source: _bmad-output/planning-artifacts/architecture.md#examples目录]
- [Source: docs/quick-start.md]
- [Source: docs/api.md]

## Dev Agent Guardrails

### Technical Requirements

- 后端示例必须使用 `@EnableSecurityBoot` 注解
- 后端示例必须依赖 `security-core` 模块
- 前端示例必须使用 Vue3
- 前端示例必须展示完整的 JWT 认证流程
- 示例代码必须与现有 API 端点匹配（参考 `docs/api.md`）
- 示例配置必须与 `docs/configuration.md` 中的配置项匹配

### Architecture Compliance

- 示例项目结构应遵循 Spring Boot 标准结构
- 示例代码应使用构造器依赖注入
- 示例代码应使用 SLF4J 日志
- 示例代码应使用 `Response<T>` 响应格式
- 示例代码不应使用 `System.out.println()` 或 `printStackTrace()`

### Library & Framework Requirements

**后端依赖**:
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>

<dependencies>
    <dependency>
        <groupId>com.original.frame</groupId>
        <artifactId>security-core</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**前端依赖**:
```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "axios": "^1.6.0",
    "pinia": "^2.1.0"
  },
  "devDependencies": {
    "vite": "^5.0.0",
    "@vitejs/plugin-vue": "^5.0.0"
  }
}
```

### File Structure Requirements

**本故事目标结构**:

```text
examples/
├── README.md
├── quick-start/
│   ├── pom.xml
│   ├── README.md
│   └── src/main/
│       ├── java/com/example/quickstart/
│       │   ├── QuickStartApplication.java
│       │   └── controller/
│       │       ├── HelloController.java
│       │       ├── UserController.java
│       │       └── AdminController.java
│       └── resources/
│           ├── application.properties
│           └── data.sql
└── jwt-frontend/
    ├── package.json
    ├── vite.config.js
    ├── README.md
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/index.js
        ├── views/
        │   ├── Login.vue
        │   ├── Home.vue
        │   └── Admin.vue
        ├── services/auth.js
        └── stores/auth.js
```

### Testing Requirements

- 验证后端示例能成功启动
- 验证前端示例能成功启动
- 验证登录流程完整可演示
- 验证 API 调用返回正确响应
- 验证 README 命令可执行

### Previous Story Intelligence

**来自 7-1 快速开始文档的经验**:

- 文档示例必须基于真实 API
- 配置项必须与源码中的验证逻辑一致
- 示例代码应简洁易懂

**来自 7-2 配置参考文档的经验**:

- 配置示例应使用真实的配置键名
- 应提供最小配置和完整配置两种示例

**来自 7-3 API 参考文档的经验**:

- API 端点路径必须与 Controller 注解一致
- 请求/响应格式必须与 DTO 定义一致

**重要模式**:

- 示例代码应有适当注释
- 示例应使用框架默认值，减少配置
- 示例 README 应提供清晰的运行步骤

### Git Intelligence Summary

- 最近的提交主要是文档更新
- 仓库已实现完整的认证和用户管理 API
- API 端点位于 `/api/auth/*`、`/api/users/*`、`/api/roles/*`、`/api/sessions/*`

### Latest Tech Information

**Vue3 + Vite 最佳实践**:
- 使用 Composition API
- 使用 `<script setup>` 语法
- 使用 Pinia 进行状态管理
- 使用 Axios 拦截器处理 Token

**Spring Boot 2.7.18 示例最佳实践**:
- 使用 `@SpringBootApplication` + `@EnableSecurityBoot`
- 使用 `application.properties` 配置
- 使用构造器依赖注入

### Project Context Reference

- 必须遵守 `_bmad-output/project-context.md` 中的技术栈和响应格式
- 示例代码应展示框架核心能力
- 示例应与 `docs/quick-start.md` 的内容对应

## Dev Agent Record

### Agent Model Used

glm-4.7

### Debug Log References

### Implementation Plan

1. **步骤 1**: 创建后端示例项目结构
   - 创建完整的 Maven 项目结构
   - 配置 pom.xml 依赖 security-core 模块
   - 设置 application.properties 配置数据库和应用信息
   - 创建 data.sql 初始化测试数据
   - 编写详细的 README.md 说明文档

2. **步骤 2**: 实现后端示例代码
   - 创建 QuickStartApplication 启动类，使用 @EnableSecurityBoot 注解
   - 实现公开 HelloController，提供健康检查和问候端点
   - 实现 UserController，展示认证后的用户信息获取
   - 实现 AdminController，展示 @PreAuthorize 权限控制
   - 创建 UserInfo DTO 实体类
   - 使用模拟数据替代未实现的数据库层

3. **步骤 3**: 创建前端示例项目结构
   - 使用 Vue3 + Vite 创建前端项目结构
   - 配置 package.json 添加 Vue Router、Pinia、Axios 依赖
   - 配置 vite.config.js 设置代理转发到后端
   - 创建 index.html 添加基础样式
   - 编写前端 README.md 说明

4. **步骤 4**: 实现前端示例代码
   - 创建 main.js 配置路由和导航守卫
   - 实现 App.vue 根组件
   - 创建 auth store 管理 Token 和用户状态
   - 实现 Login.vue 登录页面
   - 实现 Home.vue 用户信息页面
   - 实现 Admin.vue 管理员页面（带权限验证）
   - 创建 auth API 服务封装 HTTP 请求

5. **步骤 5**: 建立文档入口
   - 创建 examples/README.md 作为示例导航
   - 更新主 README.md 添加示例项目链接

### Completion Notes List

- ✅ 实现了完整的后端示例项目，包含公开端点、认证端点和权限控制端点
- ✅ 实现了完整的前端示例项目，展示完整的 JWT 认证流程
- ✅ 使用模拟数据避免依赖未完成的数据库模块
- ✅ 配置了跨域代理，前后端可以独立开发
- ✅ 提供了详细的运行说明和测试账户
- ✅ 添加了完整的文档导航和链接

### File List

**创建的新文件**:

- `examples/README.md` - 示例项目导航文档
- `examples/quick-start/pom.xml` - 后端项目 Maven 配置
- `examples/quick-start/src/main/java/com/example/quickstart/QuickStartApplication.java` - 后端启动类
- `examples/quick-start/src/main/java/com/example/quickstart/controller/HelloController.java` - 公开端点控制器
- `examples/quick-start/src/main/java/com/example/quickstart/controller/UserController.java` - 用户控制器
- `examples/quick-start/src/main/java/com/example/quickstart/controller/AdminController.java` - 管理员控制器
- `examples/quick-start/src/main/java/com/example/quickstart/entity/UserInfo.java` - 用户信息实体
- `examples/quick-start/src/main/resources/application.properties` - 后端配置文件
- `examples/quick-start/src/main/resources/data.sql` - 数据库初始化脚本
- `examples/quick-start/README.md` - 后端说明文档
- `examples/jwt-frontend/package.json` - 前端项目配置
- `examples/jwt-frontend/vite.config.js` - Vite 配置文件
- `examples/jwt-frontend/index.html` - 前端入口 HTML
- `examples/jwt-frontend/src/main.js` - 前端应用入口
- `examples/jwt-frontend/src/App.vue` - 根组件
- `examples/jwt-frontend/src/views/Login.vue` - 登录页面
- `examples/jwt-frontend/src/views/Home.vue` - 用户首页
- `examples/jwt-frontend/src/views/Admin.vue` - 管理页面
- `examples/jwt-frontend/src/services/auth.js` - API 服务
- `examples/jwt-frontend/src/stores/auth.js` - Pinia 状态管理
- `examples/jwt-frontend/src/router/index.js` - 路由配置
- `examples/jwt-frontend/README.md` - 前端说明文档

**修改的文件**:

- `README.md` - 添加示例项目链接

## Senior Developer Review (AI)

**审查日期**: 2026-03-19
**审查结果**: Changes Requested
**审查模型**: glm-5 (opus)

### 审查摘要

| 分类 | 数量 |
|-----|------|
| Intent Gap | 2 |
| Bad Spec | 2 |
| Patch | 8 |
| Defer | 4 |
| **总计** | **16** |

### Action Items

- [x] **[HIGH]** P-001: 移除 Login.vue 中的无效代码块
- [x] **[HIGH]** P-002: 修复 Home.vue 重复声明 user 变量
- [x] **[HIGH]** P-003: 修复 UserInfo.java 使用完全限定名，添加 import 语句
- [x] **[MEDIUM]** P-004: 为 UserInfo.java 和 AdminController.AdminStats 添加 JavaDoc 字段注释
- [x] **[MEDIUM]** P-005: 修复 Admin.vue hasAdminRole 变量未使用问题
- [x] **[MEDIUM]** P-006: 更新前端 README 项目结构描述
- [x] **[MEDIUM]** P-007: 为 UserController 添加空值检查
- [x] **[MEDIUM]** P-008: 为 AdminController 添加空值检查和常量定义
- [x] **[HIGH]** BS-001: 更新 application.properties 使用环境变量配置
- [x] **[HIGH]** BS-002: 移除 Login.vue 中的明文密码显示

### Review Follow-ups (AI)

> 所有 Patch 类别问题已修复，所有 Bad Spec 问题已修复。

### Change Log

- 2026-03-19: 代码审查完成，修复 10 个问题
