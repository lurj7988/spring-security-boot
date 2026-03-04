package com.original.security.user.repository;

import com.original.security.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户 Repository
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true 如果存在，false 如果不存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据角色名称查找用户 (用于缓存精确失效)
     *
     * @param roleName 角色名称
     * @return 关联该角色的用户列表
     */
    java.util.List<User> findByRoles_Name(String roleName);
}
