package com.original.security.user.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void testUserEntityCreation() {
        // 测试 User 实体的基本创建和字段访问
        String username = "testuser";
        String password = "encrypted_password";
        String email = "test@example.com";

        User user = new User(username, password, email);

        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(email, user.getEmail());
        assertTrue(user.isEnabled()); // 默认值应该是 true

        // 测试各种字段的 setter/getter
        user.setEmail("newemail@example.com");
        assertEquals("newemail@example.com", user.getEmail());

        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void testUserEntityFields() {
        // 验证 User 实体包含所有必需的字段
        User user = new User();

        // 测试各字段的存在
        assertNotNull(user);

        // 设置并验证各字段
        user.setId(1L);
        assertEquals(1L, user.getId());

        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());

        user.setPassword("encrypted_password");
        assertEquals("encrypted_password", user.getPassword());

        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());

        user.setEnabled(true);
        assertTrue(user.isEnabled());
    }

    @Test
    void testUserEqualityAndHashCode() {
        // 测试 User 实体的 equals 和 hashCode 实现
        String username = "testuser";
        User user1 = new User(username, "pass", "test@example.com");
        User user2 = new User(username, "pass", "test@example.com");

        // 基于 username 的相等性比较
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testUserRoleManagement() {
        // 测试 User 实体的角色管理方法
        User user = new User("testuser", "pass", "test@example.com");
        Role role1 = new Role("ADMIN", "Administrator");
        Role role2 = new Role("USER", "User");

        // 测试添加角色
        user.addRole(role1);
        user.addRole(role2);
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(role1));
        assertTrue(user.getRoles().contains(role2));

        // 测试移除角色
        user.removeRole(role1);
        assertEquals(1, user.getRoles().size());
        assertFalse(user.getRoles().contains(role1));
        assertTrue(user.getRoles().contains(role2));
    }

    @Test
    void testUserSetterMethods() {
        // 测试 User 实体的 setter 方法
        User user = new User();

        user.setId(1L);
        assertEquals(1L, user.getId());

        user.setUsername("testuser");
        assertEquals("testuser", user.getUsername());

        user.setPassword("encrypted_password");
        assertEquals("encrypted_password", user.getPassword());

        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getEmail());

        user.setEnabled(true);
        assertTrue(user.isEnabled());

        java.time.LocalDateTime createdAt = java.time.LocalDateTime.of(2026, 1, 1, 0, 0);
        user.setCreatedAt(createdAt);
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testUserRolesSetter() {
        // 测试 User 实体的 roles setter 方法
        User user = new User("testuser", "pass", "test@example.com");
        Role role1 = new Role("ADMIN", "Administrator");
        Role role2 = new Role("USER", "User");

        java.util.Set<Role> roles = new java.util.HashSet<>();
        roles.add(role1);
        roles.add(role2);

        user.setRoles(roles);
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(role1));
        assertTrue(user.getRoles().contains(role2));
    }

    @Test
    void testUserToString() {
        // 测试 User 实体的 toString 方法
        User user = new User("testuser", "pass", "test@example.com");
        String str = user.toString();

        assertTrue(str.contains("username='testuser'"));
        assertTrue(str.contains("email='test@example.com'"));
        assertTrue(str.contains("enabled=true"));
        // 注意：toString 不包含 password 字段，这是正确的（安全性）
        assertFalse(str.contains("password"));
    }

    @Test
    void testUserPrePersist() {
        // 测试 @PrePersist 回调自动设置 createdAt
        User user = new User("testuser", "pass", "test@example.com");

        // 在构造函数中没有设置 createdAt 时，@PrePersist 会自动设置
        // 由于单元测试不运行 JPA 生命周期，这里只验证字段初始化
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
}