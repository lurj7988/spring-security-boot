package com.original.security.user.repository;

import com.original.security.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限 Repository
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据名称查询权限
     *
     * @param name 权限名称
     * @return 权限信息
     */
    Optional<Permission> findByName(String name);

    /**
     * 根据名称查询权限列表（用于唯一约束验证）
     *
     * @param name 权限名称
     * @return 权限列表
     */
    List<Permission> findAllByName(String name);
}
