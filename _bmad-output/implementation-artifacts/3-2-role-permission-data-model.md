# Story 3.2: 实现角色和权限数据模型

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 框架开发者,
I want 定义清晰的角色和权限数据模型,
so that RBAC 系统有数据基础。

## Acceptance Criteria

1. **Given** 数据库表结构
   **When** 查看 `roles` 表
   **Then** 包含 `id`、`name`、`description`、`created_at` 字段
   **And** `name` 字段唯一（UNIQUE）
   **And** 使用 `snake_case` 命名

2. **Given** 数据库表结构
   **When** 查看 `permissions` 表
   **Then** 包含 `id`、`name`、`description`、`created_at` 字段
   **And** `name` 字段唯一
   **And** 支持层级权限（如 `user:read`, `user:write`）

3. **Given** 关联表
   **When** 查看用户角色关联表 `user_roles`
   **Then** 包含 `user_id`、`role_id` 外键
   **And** 联合主键（`user_id`, `role_id`）
   **And** 级联删除配置正确

4. **Given** 关联表
   **When** 查看角色权限关联表 `role_permissions`
   **Then** 包含 `role_id`、`permission_id` 外键
   **And** 联合主键（`role_id`, `permission_id`）
   **And** 支持角色拥有多个权限

## Tasks / Subtasks

- [x] Task 1: 创建 JPA 实体类 (AC: 1, 2)
  - [x] Subtask 1.1: 创建 `Role` 实体类，映射到 `roles` 表
  - [x] Subtask 1.2: 创建 `Permission` 实体类，映射到 `permissions` 表
  - [x] Subtask 1.3: 配置唯一的 `name` 约束和 `snake_case` 列名
- [x] Task 2: 配置实体间关联关系 (AC: 3, 4)
  - [x] Subtask 2.1: 在 `User` 实体中添加 `@ManyToMany` 关联到 `Role`
  - [x] Subtask 2.2: 在 `Role` 实体中添加 `@ManyToMany` 关联到 `Permission`
  - [x] Subtask 2.3: 使用 `@JoinTable` 定义中间表 `user_roles` 和 `role_permissions`
- [x] Task 3: 创建 Spring Data JPA Repositories (AC: 1, 2)
  - [x] Subtask 3.1: 创建 `RoleRepository`
  - [x] Subtask 3.2: 创建 `PermissionRepository`
- [x] Task 4: 编写集成测试 (AC: 1, 2, 3, 4)
  - [x] Subtask 4.1: 测试角色和权限的 CRUD 操作
  - [x] Subtask 4.2: 测试关联关系的级联和查询性能
  - [x] Subtask 4.3: 验证数据库 Schema 符合命名规范

## Dev Notes

### Previous Story Intelligence

- Epic 0 已完成了核心接口定义和基础表结构的 SQL 初始化。
- `security-core` 中定义了 `SecurityUser` 模型，它期望 roles 和 permissions 以 `List<String>` 形式存在。
- `SchemaInitializationTest` 已经验证了 `users`, `roles`, `permissions` 表的存在。

### Technical Requirements

- **Spring Boot 2.7.18 / Spring Security 5.7.11**
- **Java Persistence API (JPA)**: 使用 Hibernate 作为实现。
- **强制约束**: 必须使用构造器依赖注入。
- **命名规范**: 数据库表名复数 `snake_case`，列名 `snake_case`。

### Architecture Compliance

- 实体类位置: `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/entity/`
- Repository 位置: `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/repository/`
- 使用 `Response.successBuilder(data).build()` 统一响应。

### Project Structure Notes

- 需要在 `security-components/security-user/security-user-impl/` 下创建对应的目录和类。
- 注意 `User` 实体可能已经在之前的 Story 中部分实现，需要完善关联关系。

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 3.2]
- [Source: _bmad-output/planning-artifacts/architecture.md#命名规范]
- [Source: security-core/src/main/java/com/original/security/core/authentication/user/SecurityUser.java]

## Dev Agent Record

### Agent Model Used

Antigravity (GLM-4)

### Debug Log References

- mvn test -Dtest=RbacRepositoryTest: PASSED (Initial failure due to Optional.orElseThrow Java 8 incompatibility fixed)

### Completion Notes List

- Created JPA entities `User`, `Role`, `Permission` with requested table mappings and constraints.
- Established MANY-TO-MANY relationships between User/Role and Role/Permission.
- Implemented `UserRepository`, `RoleRepository`, and `PermissionRepository`.
- Added `RbacRepositoryTest` covering CRUD, relationships, and cascade behaviors.
- Verified compliance with project coding standards (Constructor Injection, PascalCase, snake_case).

### File List

- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/entity/User.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/entity/Role.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/entity/Permission.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/repository/UserRepository.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/repository/RoleRepository.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/repository/PermissionRepository.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/repository/RbacRepositoryTest.java`

## Change Log

- 2026-03-02: Initial implementation of RBAC data model and repositories. addressed Java 8 compatibility in tests.
- 2026-03-02: Code review fixes (v1):
  - Fixed Java 8 compatibility in tests (orElseThrow lambda syntax)
  - Added bidirectional mapping between Permission and Role entities
  - Added existsByUsername() method to UserRepository
  - Added full-argument constructors for User, Role, and Permission entities
- 2026-03-02: Code review fixes (v2):
  - Fixed test dependency injection: Repository dependencies now use constructor injection via @TestConstructor(ALL)
  - Fixed exception messages to English with context (e.g., "Role not found with id: 1")
  - Added all story files and implementation files to git staging area
- 2026-03-02: Code review fixes (v3):
  - Added testEmptyCollectionInitialization() test to verify empty collections are properly initialized
  - Added testUniqueConstraintViolation() test to verify unique constraint behavior
  - Added testEqualsAndHashCode() test to verify equals/hashCode contract
  - Added testPermissionNameFormatValidation() test to validate hierarchical permission names
  - Added findAllByName() method to PermissionRepository for constraint testing
