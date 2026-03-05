# Story 5.1: 实现用户数据模型和 Repository

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 框架开发者，
I want 验证和完善现有的用户数据模型和 Repository，
so that 用户管理功能有健壮的数据基础.

## Acceptance Criteria

1. **Given** 用户数据模型
   **When** 查看 User 实体
   **Then** 包含 id、username、password、email、enabled、created_at 字段
   **Then** username 字段唯一（UNIQUE）
   **Then** password 字段存储 BCrypt 加密后的值
   **And** 使用 snake_case 命名
   **And** 符合 JPA 规范

2. **Given** UserRepository 接口
   **When** 查看接口定义
   **Then** 继承 JpaRepository<User, Long>
   **And** 包含 findByUsername() 方法
   **And** 包含 existsByUsername() 方法
   **And** 支持 findByEmail() 方法
   **And** 使用构造器依赖注入

3. **Given** 数据库表结构
   **When** 查看 users 表
   **Then** 字段与实体映射正确
   **And** 索引配置合理（username、email）
   **And** 支持扩展字段

## Tasks / Subtasks

- [x] Task 1: 验证现有的 User 实体类 (AC: 1)
  - [x] Subtask 1.1: 确认包含 id、username、password、email、enabled、created_at 字段
  - [x] Subtask 1.2: 验证 username 字段唯一约束配置
  - [x] Subtask 1.3: 确认 password 字段适合存储 BCrypt 加密后的值
  - [x] Subtask 1.4: 验证使用 snake_case 数据库字段映射
  - [x] Subtask 1.5: 检查是否需要添加其他字段以满足用户管理需求

- [x] Task 2: 验证现有的 UserRepository 接口 (AC: 2)
  - [x] Subtask 2.1: 确认继承 JpaRepository<User, Long>
  - [x] Subtask 2.2: 验证 findByUsername() 方法存在且正常工作
  - [x] Subtask 2.3: 验证 existsByUsername() 方法存在且正常工作
  - [x] Subtask 2.4: 验证 findByEmail() 方法存在且正常工作
  - [x] Subtask 2.5: 检查是否需要添加其他查询方法以满足用户管理需求

- [x] Task 3: 验证数据库表结构和映射 (AC: 3)
  - [x] Subtask 3.1: 确认数据库表字段与实体映射正确
  - [x] Subtask 3.2: 验证合理的索引配置（username、email）
  - [x] Subtask 3.3: 确认支持扩展字段的能力
  - [x] Subtask 3.4: 检查与其他实体（Role, Permission）的关系映射

## Dev Notes

### 相关架构模式和约束
- **Spring Data JPA**: 使用 Spring Data JPA 作为数据访问层
- **数据库命名规范**: 所有表名使用 snake_case 复数形式，所有列名使用 snake_case
- **构造器依赖注入**: 所有 Bean 均通过构造器注入，无字段注入
- **安全要求**: 密码必须使用 BCrypt（强度≥10）加密
- **数据完整性**: username 字段唯一约束，支持扩展字段

### 源码树需触及的组件
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/entity/User.java`（现有用户实体，需验证和可能扩展）
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/repository/UserRepository.java`（现有用户仓库接口，需验证和可能扩展）
- 数据库迁移脚本（如需要）

### 测试标准摘要
- 单元测试：测试 User 实体的字段映射和约束
- 集成测试：测试 UserRepository 的基本 CRUD 操作和自定义方法
- 验证测试：确保 findByUsername、existsByUsername 和 findByEmail 方法按预期工作

### 项目结构对齐说明
- 实体类放在 `entity` 包中 (已在正确位置)
- Repository 接口放在 `repository` 包中 (已在正确位置)
- 构造器依赖注入：所有依赖项通过构造器注入
- SLF4J 日志：使用 `log.info()`/`log.warn()`/`log.error()`，无 System.out

### 检测到的冲突或差异（附带理由）
- User 实体和 UserRepository 已存在，所以不是创建新的，而是验证和完善现有实现
- 现有实现已包含与 Role 的多对多关系，满足未来权限管理需求

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 5.1]
- [Source: _bmad-output/planning-artifacts/architecture.md#项目结构与边界]
- [Source: docs/project-context.md#关键实现规则]
- [Source: docs/CLAUDE.md#Build Commands]

## Dev Agent Record

### Agent Model Used

claude-opus-4-6

### Debug Log References

### Completion Notes List

✅ **Task 1.1**: User 实体包含以下字段：id (Long)、username (String)、password (String)、email (String)、enabled (boolean)、createdAt (LocalDateTime)

✅ **Task 1.2**: username 字段配置了唯一约束 `@Column(name = "username", unique = true, nullable = false, length = 50)`

✅ **Task 1.3**: password 字段长度为100字符，足够存储 BCrypt 加密后的哈希值 `@Column(name = "password", nullable = false, length = 100)`

✅ **Task 1.4**: 使用 snake_case 数据库字段映射，例如 `@Column(name = "username")`, `@Column(name = "created_at")` 等

✅ **Subtask 1.5**: 检查用户管理需求后，现有字段已足够满足 Story 5.2-5.4 的基本需求，暂无需添加额外字段。如后续需要，可考虑添加 lastLoginAt、failedLoginAttempts 等字段。

✅ **Task 2.1**: UserRepository 接口正确继承了 `JpaRepository<User, Long>` 接口

✅ **Task 2.2**: 存在 findByUsername 方法：`Optional<User> findByUsername(String username)`，可用于根据用户名查询用户

✅ **Task 2.3**: 存在 existsByUsername 方法：`boolean existsByUsername(String username)`，可用于检查用户名是否存在

✅ **Task 2.4**: 存在 findByEmail 方法：`Optional<User> findByEmail(String email)`，可支持基于邮箱的查询

✅ **Subtask 2.5**: 检查用户管理需求后，发现现有方法已满足基本需求。后续用户创建、查询、密码管理等功能中如需更多查询方法，可根据需要添加。

✅ **Task 3.1**: 数据库表字段与实体映射正确。schema.sql 中的 users 表定义与 User 实体类匹配：id-BIGINT, username-VARCHAR(50), password-VARCHAR(100), email-VARCHAR(100), enabled-BOOLEAN, created_at-TIMESTAMP

✅ **Task 3.2**: 索引配置合理，schema.sql 中已包含 email 索引 `CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);` 和 username 的唯一约束

✅ **Task 3.3**: 数据库设计支持扩展字段的能力，使用了可扩展的 VARCHAR 长度和预留字段空间

✅ **Task 3.4**: 检查了与 Role、Permission 实体的关系映射，User 与 Role 之间建立了多对多关系，通过 user_roles 中间表实现，并有适当的外键约束和级联删除配置

### 代码审查修复记录

**已修复的问题：**

🔧 **CRITICAL - 文档路径错误修复**
- 修正了 File List 中 UserEntityTest.java 的包路径拼写错误：`com/origina/security/user/entity` → `com/original/security/user/entity`

🔧 **MEDIUM - 数据库完整性增强**
- 在 schema.sql 中添加了 email 字段的唯一约束：`CONSTRAINT uk_users_email UNIQUE (email)`
- 确保用户邮箱的唯一性，提升数据完整性

🔧 **MEDIUM - 测试覆盖率完善**
- UserEntityTest.java 新增测试方法：
  - `testUserRoleManagement()` - 测试 addRole() 和 removeRole() 方法
  - `testUserSetterMethods()` - 测试所有 setter 方法
  - `testUserRolesSetter()` - 测试 setRoles() 方法
  - `testUserToString()` - 测试 toString() 方法
  - `testUserPrePersist()` - 测试 @PrePersist 回调
- UserRepositoryTest.java 新增测试方法：
  - `testDeleteById()` - 测试删除用户功能
  - `testUpdateUser()` - 测试更新用户功能
  - `testFindAll()` - 测试查询所有用户功能
  - `testCount()` - 测试统计用户数量功能
- findByRoles_Name() 测试暂跳过，需要在集成测试环境中完整实现

### File List

- security-components/security-user/security-user-impl/src/main/java/com/original/security/user/entity/User.java
- security-components/security-user/security-user-impl/src/main/java/com/original/security/user/repository/UserRepository.java
- security-components/security-user/security-user-impl/src/test/java/com/original/security/user/entity/UserEntityTest.java
- security-components/security-user/security-user-impl/src/test/java/com/original/security/user/repository/UserRepositoryTest.java