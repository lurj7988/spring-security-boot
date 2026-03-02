package com.original.security.user.repository;

import com.original.security.user.entity.Permission;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RBAC 实体和 Repository 集成测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class RbacRepositoryTest {

    /**
     * TestEntityManager - Spring Boot 测试框架特有的，必须使用 @Autowired 字段注入
     * 注意：这是 Spring Boot 测试框架的限制，其他依赖应使用构造器注入
     */
    @Autowired
    private TestEntityManager entityManager;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 构造器注入依赖 - 符合项目依赖注入规范
     */
    public RbacRepositoryTest(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Test
    public void testPermissionCrud() {
        Permission permission = new Permission("user:read", "Read access for users");
        Permission saved = permissionRepository.save(permission);

        assertNotNull(saved.getId());
        assertEquals("user:read", saved.getName());

        Optional<Permission> found = permissionRepository.findByName("user:read");
        assertTrue(found.isPresent());
        assertEquals("Read access for users", found.get().getDescription());
    }

    @Test
    public void testRoleCrudAndPermissions() {
        Permission read = permissionRepository.save(new Permission("user:read", "Read users"));
        Permission write = permissionRepository.save(new Permission("user:write", "Write users"));

        Role admin = new Role("ADMIN", "Administrator role");
        admin.addPermission(read);
        admin.addPermission(write);

        Role savedRole = roleRepository.save(admin);
        assertNotNull(savedRole.getId());
        assertEquals(2, savedRole.getPermissions().size());

        entityManager.flush();
        entityManager.clear();

        Role foundRole = roleRepository.findById(savedRole.getId()).orElseThrow(() -> new java.util.NoSuchElementException("Role not found with id: " + savedRole.getId()));
        assertEquals(2, foundRole.getPermissions().size());
        assertTrue(foundRole.getPermissions().contains(read));
        assertTrue(foundRole.getPermissions().contains(write));
    }

    @Test
    public void testUserRoles() {
        Role admin = roleRepository.save(new Role("ADMIN", "Admin role"));
        Role userRole = roleRepository.save(new Role("USER", "User role"));

        User user = new User("tester", "password", "test@example.com");
        user.addRole(admin);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow(() -> new java.util.NoSuchElementException("User not found with id: " + savedUser.getId()));
        assertEquals(2, foundUser.getRoles().size());
        assertTrue(foundUser.getRoles().contains(admin));
        assertTrue(foundUser.getRoles().contains(userRole));
    }

    @Test
    public void testCascadeDelete() {
        Permission read = permissionRepository.save(new Permission("user:read", "Read users"));
        Role admin = new Role("ADMIN", "Admin role");
        admin.addPermission(read);
        Role savedRole = roleRepository.save(admin);

        User user = new User("tester", "password", "test@example.com");
        user.addRole(savedRole);
        User savedUser = userRepository.save(user);

        // Delete user
        userRepository.delete(savedUser);
        entityManager.flush();

        // Verify role and permission still exist
        assertTrue(roleRepository.findById(savedRole.getId()).isPresent());
        assertTrue(permissionRepository.findById(read.getId()).isPresent());

        // Delete role
        roleRepository.delete(savedRole);
        entityManager.flush();

        // Verify permission still exists
        assertTrue(permissionRepository.findById(read.getId()).isPresent());
    }

    @Test
    public void testEmptyCollectionInitialization() {
        // Verify empty collections are properly initialized
        User user = new User();
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());

        Role role = new Role();
        assertNotNull(role.getPermissions());
        assertTrue(role.getPermissions().isEmpty());

        Permission permission = new Permission();
        assertNotNull(permission.getRoles());
        assertTrue(permission.getRoles().isEmpty());
    }

    @Test
    @Transactional
    public void testUniqueConstraintViolation() {
        // Create first permission
        permissionRepository.save(new Permission("unique:perm", "First"));
        entityManager.flush();
        entityManager.clear();

        // Attempt to create duplicate - database should reject it
        Permission duplicate = new Permission("unique:perm", "Duplicate");
        assertThrows(DataIntegrityViolationException.class, () -> {
            permissionRepository.save(duplicate);
            entityManager.flush();
        });

        entityManager.clear();

        // Verify only one exists with that name
        assertEquals(1, permissionRepository.findAllByName("unique:perm").size());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Test Role equals/hashCode
        Role role1 = new Role("ADMIN", "Admin");
        Role role2 = new Role("ADMIN", "Admin");
        Role role3 = new Role("USER", "User");

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1, role3);

        // Test Permission equals/hashCode
        Permission perm1 = new Permission("user:read", "Read");
        Permission perm2 = new Permission("user:read", "Read");
        Permission perm3 = new Permission("user:write", "Write");

        assertEquals(perm1, perm2);
        assertEquals(perm1.hashCode(), perm2.hashCode());
        assertNotEquals(perm1, perm3);

        // Test User equals/hashCode
        User user1 = new User("testuser", "pass", "test@test.com");
        User user2 = new User("testuser", "other", "other@test.com");
        User user3 = new User("otheruser", "pass", "test@test.com");

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
    }

    @Test
    public void testPermissionNameFormatValidation() {
        // Test hierarchical permission names (e.g., user:read, user:write)
        String[] validFormats = {
            "user:read",
            "user:write",
            "user:delete",
            "admin:all",
            "system:config",
            "api:v1:user:read"
        };

        for (String format : validFormats) {
            Permission perm = permissionRepository.save(new Permission(format, "Test permission"));
            assertNotNull(perm.getId());
            assertEquals(format, perm.getName());
        }
    }
}
