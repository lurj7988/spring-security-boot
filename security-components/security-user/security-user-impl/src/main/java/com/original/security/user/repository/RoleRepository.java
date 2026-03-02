package com.original.security.user.repository;

import com.original.security.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色 Repository
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据名称查询角色
     *
     * @param name 角色名称
     * @return 角色信息
     */
    Optional<Role> findByName(String name);
}
