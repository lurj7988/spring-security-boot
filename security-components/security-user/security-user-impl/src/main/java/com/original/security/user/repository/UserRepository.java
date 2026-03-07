package com.original.security.user.repository;

import com.original.security.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return true 如果存在，false 如果不存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据角色名称查找用户 (用于缓存精确失效)
     *
     * @param roleName 角色名称
     * @return 关联该角色的用户列表
     */
    java.util.List<User> findByRoles_Name(String roleName);

    /**
     * 根据用户名模糊查询和启用状态筛选用户列表
     * <p>
     * <strong>性能注意：</strong>此方法使用 LIKE '%keyword%' 进行模糊查询，
     * 由于前导通配符的存在，数据库无法使用索引，在大数据量时性能会下降。
     * 对于生产环境的大量用户数据，建议使用全文搜索（如 MySQL FULLTEXT、
     * Elasticsearch 等）替代此查询。
     *
     * @param usernameKeyword 用户名关键词，支持模糊匹配，null或空字符串表示不过滤
     * @param enabled 状态过滤条件，null表示不过滤
     * @param pageable 分页参数
     * @return 分页的用户列表
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:usernameKeyword IS NULL OR u.username LIKE %:usernameKeyword%) " +
           "AND (:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findByUsernameContainingAndEnabled(
            @Param("usernameKeyword") String usernameKeyword,
            @Param("enabled") Boolean enabled,
            Pageable pageable);
}
