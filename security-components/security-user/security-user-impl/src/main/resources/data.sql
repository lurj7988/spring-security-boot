-- data.sql
-- Created for Spring Security Boot Epic 0: 项目启动与数据基础

-- Insert default admin user (password is Bcrypt hashed 'admin123')
INSERT INTO users (id, username, password, email, enabled) 
VALUES (1, 'admin', '$2a$10$XbOW.k08jXm.nI7M2aI9sOY9uV6P8iH/hBvqCjR//S.F3b/5U10q.', 'admin@example.com', true);

-- Insert default roles
INSERT INTO roles (id, name, description) VALUES (1, 'ROLE_ADMIN', '系统管理员');
INSERT INTO roles (id, name, description) VALUES (2, 'ROLE_USER', '普通用户');

-- Insert default permissions
INSERT INTO permissions (id, name, description) VALUES (1, 'user:read', '读取用户数据');
INSERT INTO permissions (id, name, description) VALUES (2, 'user:write', '修改用户数据');
INSERT INTO permissions (id, name, description) VALUES (3, 'role:read', '读取角色数据');
INSERT INTO permissions (id, name, description) VALUES (4, 'role:write', '修改角色数据');
INSERT INTO permissions (id, name, description) VALUES (5, 'permission:read', '读取权限数据');
INSERT INTO permissions (id, name, description) VALUES (6, 'permission:write', '修改权限数据');

-- Assign role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);

-- Assign permissions to roles
-- Admin gets all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 4);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 5);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 6);

-- User gets read permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 1);
