package com.original.security.user.repository;

import com.original.security.user.entity.User;
import com.original.security.user.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testUserRepositoryBasicOperations() {
        // 测试 UserRepository 的基本 CRUD 操作
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        // 保存用户
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        // 通过 ID 查找
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByUsername() {
        // 测试 findByUsername 方法
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testExistsByUsername() {
        // 测试 existsByUsername 方法
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testFindByEmail() {
        // 测试 findByEmail 方法
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void testFindByRoles_Name() {
        // 测试根据角色名称查找用户
        Role adminRole = new Role("ADMIN", "Administrator");
        Role userRole = new Role("USER", "User");
        
        entityManager.persist(adminRole);
        entityManager.persist(userRole);

        User adminUser = new User();
        adminUser.setUsername("admin_test_roles");
        adminUser.setPassword("pass");
        adminUser.setEmail("admin_test_roles@example.com");
        adminUser.setEnabled(true);
        adminUser.addRole(adminRole);
        
        User normalUser = new User();
        normalUser.setUsername("user_test_roles");
        normalUser.setPassword("pass");
        normalUser.setEmail("user_test_roles@example.com");
        normalUser.setEnabled(true);
        normalUser.addRole(userRole);
        
        User multiRoleUser = new User();
        multiRoleUser.setUsername("super_test_roles");
        multiRoleUser.setPassword("pass");
        multiRoleUser.setEmail("super_test_roles@example.com");
        multiRoleUser.setEnabled(true);
        multiRoleUser.addRole(adminRole);
        multiRoleUser.addRole(userRole);

        entityManager.persist(adminUser);
        entityManager.persist(normalUser);
        entityManager.persist(multiRoleUser);
        entityManager.flush();

        java.util.List<User> adminUsers = userRepository.findByRoles_Name("ADMIN");
        assertEquals(2, adminUsers.size());
        assertTrue(adminUsers.stream().anyMatch(u -> u.getUsername().equals("admin_test_roles")));
        assertTrue(adminUsers.stream().anyMatch(u -> u.getUsername().equals("super_test_roles")));

        java.util.List<User> normalUsers = userRepository.findByRoles_Name("USER");
        assertEquals(2, normalUsers.size());
        assertTrue(normalUsers.stream().anyMatch(u -> u.getUsername().equals("user_test_roles")));
        assertTrue(normalUsers.stream().anyMatch(u -> u.getUsername().equals("super_test_roles")));
        
        java.util.List<User> noUsers = userRepository.findByRoles_Name("GUEST");
        assertEquals(0, noUsers.size());
    }

    @Test
    void testDeleteById() {
        // 测试删除用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testUpdateUser() {
        // 测试更新用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        savedUser.setEmail("newemail@example.com");
        savedUser.setEnabled(false);

        User updatedUser = userRepository.save(savedUser);

        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertFalse(updatedUser.isEnabled());
    }

    @Test
    void testFindAll() {
        // 测试查找所有用户
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setEmail("user2@example.com");

        userRepository.save(user1);
        userRepository.save(user2);

        java.util.List<User> users = userRepository.findAll();
        assertTrue(users.size() >= 2);
    }

    @Test
    void testCount() {
        // 测试统计用户数量
        long initialCount = userRepository.count();

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encrypted_password");
        user.setEmail("test@example.com");
        user.setEnabled(true);

        userRepository.save(user);

        long finalCount = userRepository.count();
        assertEquals(initialCount + 1, finalCount);
    }

    // ==================== 新增测试：模糊查询和筛选功能 ====================

    /**
     * 测试模糊查询和启用状态筛选 - 无筛选条件
     */
    @Test
    void testFindByUsernameContainingAndEnabled_NoFilters_ReturnsAllUsers() {
        // Given
        createTestUser("user1", "user1@example.com", true);
        createTestUser("user2", "user2@example.com", true);
        createTestUser("user3", "user3@example.com", false);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findByUsernameContainingAndEnabled(null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
    }

    /**
     * 测试模糊查询 - 按用户名关键词
     */
    @Test
    void testFindByUsernameContainingAndEnabled_UsernameFilter_ReturnsMatchingUsers() {
        // Given
        createTestUser("alice_test", "alice@test.com", true);
        createTestUser("bob_test", "bob@test.com", true);
        createTestUser("charlie_other", "charlie@test.com", true);

        Pageable pageable = PageRequest.of(0, 10);

        // When - 搜索包含 "test" 的用户名
        Page<User> result = userRepository.findByUsernameContainingAndEnabled("test", null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(u -> u.getUsername().contains("test")));
    }

    /**
     * 测试状态筛选 - 按启用状态
     */
    @Test
    void testFindByUsernameContainingAndEnabled_EnabledFilter_ReturnsMatchingUsers() {
        // Given
        createTestUser("enabled_user", "enabled@test.com", true);
        createTestUser("disabled_user", "disabled@test.com", false);
        createTestUser("another_enabled", "another@test.com", true);

        Pageable pageable = PageRequest.of(0, 10);

        // When - 搜索启用的用户
        Page<User> result = userRepository.findByUsernameContainingAndEnabled(null, true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(User::isEnabled));
    }

    /**
     * 测试状态筛选 - 按禁用状态
     */
    @Test
    void testFindByUsernameContainingAndEnabled_DisabledFilter_ReturnsMatchingUsers() {
        // Given
        createTestUser("enabled_user", "enabled@test.com", true);
        createTestUser("disabled_user1", "disabled1@test.com", false);
        createTestUser("disabled_user2", "disabled2@test.com", false);

        Pageable pageable = PageRequest.of(0, 10);

        // When - 搜索禁用的用户
        Page<User> result = userRepository.findByUsernameContainingAndEnabled(null, false, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().noneMatch(User::isEnabled));
    }

    /**
     * 测试组合筛选 - 用户名和状态同时筛选
     */
    @Test
    void testFindByUsernameContainingAndEnabled_CombinedFilters_ReturnsMatchingUsers() {
        // Given
        createTestUser("alice_enabled", "alice1@test.com", true);
        createTestUser("alice_disabled", "alice2@test.com", false);
        createTestUser("bob_enabled", "bob@test.com", true);

        Pageable pageable = PageRequest.of(0, 10);

        // When - 搜索包含 "alice" 且启用的用户
        Page<User> result = userRepository.findByUsernameContainingAndEnabled("alice", true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("alice_enabled", result.getContent().get(0).getUsername());
        assertTrue(result.getContent().get(0).isEnabled());
    }

    /**
     * 测试分页功能
     */
    @Test
    void testFindByUsernameContainingAndEnabled_Pagination_ReturnsPagedResults() {
        // Given - 创建 5 个用户
        for (int i = 1; i <= 5; i++) {
            createTestUser("user" + i, "user" + i + "@example.com", true);
        }

        Pageable pageable = PageRequest.of(0, 2); // 第一页，每页2条

        // When
        Page<User> result = userRepository.findByUsernameContainingAndEnabled(null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 5条数据，每页2条 = 3页
        assertEquals(2, result.getContent().size()); // 当前页2条
        assertEquals(0, result.getNumber()); // 第一页
    }

    /**
     * 辅助方法：创建测试用户
     */
    private void createTestUser(String username, String email, boolean enabled) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded_password");
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}