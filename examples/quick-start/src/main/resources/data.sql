-- Insert sample users (password is "password123" encrypted with BCrypt)
INSERT INTO sys_user (username, password, email, phone, status, created_at, updated_at)
VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '13800138000', 1, NOW(), NOW()),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user@example.com', '13800138001', 1, NOW(), NOW()),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'test@example.com', '13800138002', 1, NOW(), NOW());

-- Insert sample roles
INSERT INTO sys_role (role_name, role_code, description, created_at, updated_at)
VALUES
('超级管理员', 'ROLE_ADMIN', '拥有所有权限', NOW(), NOW()),
('普通用户', 'ROLE_USER', '普通用户权限', NOW(), NOW());

-- Insert user-role mappings
INSERT INTO sys_user_role (user_id, role_id)
VALUES
((SELECT id FROM sys_user WHERE username = 'admin'), (SELECT id FROM sys_role WHERE role_code = 'ROLE_ADMIN')),
((SELECT id FROM sys_user WHERE username = 'user'), (SELECT id FROM sys_role WHERE role_code = 'ROLE_USER')),
((SELECT id FROM sys_user WHERE username = 'test'), (SELECT id FROM sys_role WHERE role_code = 'ROLE_USER'));
