# Story 3.3: permission-evaluation-service

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a 开发者,
I want 框架提供权限检查服务,
so that 我可以在代码中动态检查权限。

## Acceptance Criteria

1. **Given** `PermissionService` 服务
   **When** 调用 `hasPermission(String username, String permission)`
   **Then** 返回用户是否拥有该权限
   **And** 从数据库加载权限数据 (AC: 1.1)
   **And** 支持权限缓存 (AC: 1.2)

2. **Given** `RoleService` 服务
   **When** 调用 `hasRole(String username, String role)`
   **Then** 返回用户是否拥有该角色
   **And** 支持直接角色和继承角色 (AC: 2.1)

3. **Given** 服务配置
   **When** 查看服务实现 (AC: 3.1)
   **Then** 使用构造器依赖注入
   **And** 与 `AccessDecisionManager` 集成 (AC: 3.2)
   **And** 与 Spring Security 标准接口兼容 (AC: 3.3)

## Tasks / Subtasks

- [x] Task 1: 定义核心服务接口 (AC: 1, 2)
  - [x] Subtask 1.1: 在 `security-user-api` 中定义 `PermissionService` 和 `RoleService` 接口
  - [x] Subtask 1.2: 按照 API-Impl 分离模式组织包结构
- [x] Task 2: 实现权限和角色服务 (AC: 1, 2)
  - [x] Subtask 2.1: 实现 `PermissionServiceImpl`，注入 `UserRepository`（权限通过 JPA 延迟加载自动遍历）
  - [x] Subtask 2.2: 实现 `RoleServiceImpl`，注入 `UserRepository`（角色通过 JPA 延迟加载自动遍历）
  - [x] Subtask 2.3: 实现高效的权限加载逻辑 (从 User -> Roles -> Permissions)
- [x] Task 3: 添加缓存支持 (AC: 1.2)
  - [x] Subtask 3.1: 使用有界 LRU (`LinkedHashMap`, MAX_CACHE_SIZE=1000) 防止内存无限增长
  - [x] Subtask 3.2: 在接口中暴露 `clearCache(username)` 和 `clearAllCache()` 缓存失效方法
- [x] Task 4: Spring Security 集成 (AC: 3.2, 3.3)
  - [x] Subtask 4.1: 实现 `SecurityPermissionEvaluator` (实现 Spring Security 的 `PermissionEvaluator` 接口)
  - [x] Subtask 4.2: 将 `PermissionService` 集成到 `SecurityPermissionEvaluator` 中
- [x] Task 5: 编写单元与集成测试 (AC: 1, 2, 3)
  - [x] Subtask 5.1: 测试不同用户的权限评估结果
  - [x] Subtask 5.2: 测试角色继承逻辑
  - [x] Subtask 5.3: 验证构造器注入和异常处理

## Dev Notes

### Previous Story Intelligence

- **Story 3.2**: 已完成 `User`, `Role`, `Permission` 实体及对应的 `Repository`。
- **Database Schema**:
  - `user_roles` (user_id, role_id)
  - `role_permissions` (role_id, permission_id)
- **Learnings**: 实体类使用了 `snake_case` 映射，并且建立了双向关联。

### Technical Requirements

- **Spring Boot 2.7.18 / Spring Security 5.7.11**
- **Java 1.8**
- **Constructor Injection**: 必须 100% 强制执行。
- **Response Pattern**: 统一使用 `Response.successBuilder(data).build()`。

### Architecture Compliance

- **Module**: `security-components/security-user`
- **Interfaces**: 定义在 `security-user-api` 模块下的 `com.original.security.user.service` 包。
- **Implementation**: 定义在 `security-user-impl` 模块下的 `com.original.security.user.service.impl` 包。
- **Naming**: 遵循 PascalCase 类名和 camelCase 方法名。

### Project Structure Notes

- 需要在 `security-user-api` 和 `security-user-impl` 相应位置创建类。
- 请注意 `PermissionEvaluator` 是 Spring Security 提供的接口，用于 SpEL `hasPermission` 表达式。
- **重要**: 测试必须用 `-am` 参数运行以包含 API 模块编译: `mvn test -pl security-components/security-user/security-user-impl -am`

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 3.3]
- [Source: _bmad-output/planning-artifacts/architecture.md#授权架构]
- [Source: _bmad-output/project-context.md#关键实现规则]

## Dev Agent Record

### Agent Model Used

Gemini 2.5 Pro

### Debug Log References

- Fixed circular test dependency issue by splitting `application.properties` and applying `@ActiveProfiles("schema-init")` exclusively to `SchemaInitializationTest`.
- Mapped mocked testing generically to avoid ClassCastException in Mockito implementations.
- Included `spring-boot-starter-security` in `security-user-impl` for `PermissionEvaluator` reference.

### Completion Notes List

- Implemented `PermissionService` and `RoleService` in `security-user-api`.
- Implemented robust `RoleServiceImpl` and `PermissionServiceImpl` using `ConcurrentHashMap` for simple in-memory caching as specified.
- Support optional injection of Spring's `RoleHierarchy` in `RoleServiceImpl` to correctly resolve inherited roles mapping.
- Designed `SecurityPermissionEvaluator` mapping Spring Security generic evaluation pattern seamlessly into the core services.
- Created standalone configuration for schema tests and separated test data files using `empty-data.sql` to avoid sequence conflicts with repository validation testing (`RbacRepositoryTest`). All tests successfully validated.

### Code Review Fixes (v1) - 2026-03-03

- **[HIGH-2]** Added `clearCache(username)` and `clearAllCache()` to both `PermissionService` and `RoleService` interfaces, allowing callers to explicitly invalidate caches when permissions change.
- **[MEDIUM-1]** Replaced `@Autowired(required=false)` with `@Nullable` from `org.springframework.lang` in `RoleServiceImpl` constructor for optional `RoleHierarchy` injection.
- **[MEDIUM-2]** Removed unused `RoleService` dependency from `SecurityPermissionEvaluator`; constructor now only accepts `PermissionService`.
- **[MEDIUM-3]** Replaced unbounded `ConcurrentHashMap` with a bounded LRU cache (`Collections.synchronizedMap(LinkedHashMap)`, `MAX_CACHE_SIZE=1000`) in both `PermissionServiceImpl` and `RoleServiceImpl` to prevent unbounded memory growth.
- **[LOW-3]** Removed unused `PermissionRepository` injection from `PermissionServiceImpl`; permissions are loaded via `User → Roles → Permissions` JPA traversal.
- **[MEDIUM-4]** Replaced hollow `testUniqueConstraintsExist` test in `SchemaInitializationTest` with three real constraint tests using `DataIntegrityViolationException` assertions for `username`, `role name`, and `permission name` uniqueness.
- **[v2-MEDIUM]** 移除 `RoleServiceImpl` 中未使用的 `RoleRepository` 依赖；角色通过 `user.getRoles()` JPA 延迟加载，无需直接查 `RoleRepository`。
- **[v2-LOW]** 修复条件缓存：`PermissionServiceImpl` 和 `RoleServiceImpl` 均将 `computeIfAbsent` 改为显式 get/put，仅在用户存在且已启用时缓存结果，避免账户启用后权限查询依然返回 false 的隐蔽正确性问题。
- **[v2-REFACTOR]** 提取 `RoleServiceImpl.matchesRole()` 辅助方法，消除缓存命中与未命中路径之间的重复逻辑。
- **[v3-MEDIUM]** 修复并发竞态条件：将缓存填充改为双重检查锁定（DCL）模式——DB 查询在锁外执行（避免锁竞争），put-if-absent 在 `synchronized(cache)` 块内双重检查完成（保证原子性）。两个实现类均已修复。
- **[v3-LOW]** 移除 `RoleServiceImpl` 中未使用的 `import java.util.HashSet`。
- **[v3-LOW]** 在 `RoleServiceImpl` JavaDoc 中补充说明：`RoleHierarchy` 继承解析实时执行、不受角色缓存影响。
- 总测试数：**34**（从 33 增加），全部通过。

### File List

- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/service/PermissionService.java`
- `security-components/security-user/security-user-api/src/main/java/com/original/security/user/service/RoleService.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/service/impl/PermissionServiceImpl.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/service/impl/RoleServiceImpl.java`
- `security-components/security-user/security-user-impl/src/main/java/com/original/security/user/evaluator/SecurityPermissionEvaluator.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/service/impl/PermissionServiceImplTest.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/service/impl/RoleServiceImplTest.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/evaluator/SecurityPermissionEvaluatorTest.java`
- `security-components/security-user/security-user-impl/src/test/java/com/original/security/user/SchemaInitializationTest.java`
- `security-components/security-user/security-user-impl/src/test/resources/application.properties`
- `security-components/security-user/security-user-impl/src/test/resources/application-schema-init.properties`
- `security-components/security-user/security-user-impl/src/test/resources/empty-data.sql`
- `security-components/security-user/security-user-impl/pom.xml`
