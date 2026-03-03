package com.original.security.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("schema-init")
public class SchemaInitializationTest {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SchemaInitializationTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    public void testSchemaInitialized() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        assertEquals(1, userCount, "Should have 1 default user");

        Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
        assertEquals(2, roleCount, "Should have 2 default roles");

        Integer permissionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM permissions", Integer.class);
        assertEquals(6, permissionCount, "Should have 6 default permissions");
    }

    @Test
    public void testUsernameUniqueConstraintEnforced() {
        // 验证 users.username 唯一约束真正生效（功能性验证，而非查询 INFORMATION_SCHEMA）
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update(
                        "INSERT INTO users (username, password, email, enabled) VALUES (?, ?, ?, ?)",
                        "admin", "encoded_password", "duplicate@test.com", true
                ),
                "Inserting duplicate username should throw DataIntegrityViolationException"
        );
    }

    @Test
    public void testRoleNameUniqueConstraintEnforced() {
        // 验证 roles.name 唯一约束真正生效
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update(
                        "INSERT INTO roles (name, description) VALUES (?, ?)",
                        "ADMIN", "Duplicate Admin Role"
                ),
                "Inserting duplicate role name should throw DataIntegrityViolationException"
        );
    }

    @Test
    public void testPermissionNameUniqueConstraintEnforced() {
        // 验证 permissions.name 唯一约束真正生效
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update(
                        "INSERT INTO permissions (name, description) VALUES (?, ?)",
                        "user:read", "Duplicate Permission"
                ),
                "Inserting duplicate permission name should throw DataIntegrityViolationException"
        );
    }
}
