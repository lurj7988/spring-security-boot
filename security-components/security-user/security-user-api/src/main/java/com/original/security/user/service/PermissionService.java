package com.original.security.user.service;

/**
 * 权限服务接口
 *
 * <p>提供用户权限的动态检查能力。实现类可能包含缓存机制，因此权限数据可能
 * 不是实时的。当用户权限发生变更时，调用方应主动调用 {@link #clearCache(String)}
 * 或 {@link #clearAllCache()} 使缓存失效，以保证数据一致性。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 判断用户是否拥有指定权限
     *
     * @param username   用户名
     * @param permission 权限名称
     * @return true 如果用户拥有该权限，false 如果没有
     */
    boolean hasPermission(String username, String permission);

    /**
     * 清除指定用户的权限缓存
     *
     * <p>当用户的角色或权限发生变更时，应调用此方法使该用户的缓存失效。
     *
     * @param username 用户名，为 null 时不执行任何操作
     */
    void clearCache(String username);

    /**
     * 清除所有用户的权限缓存
     *
     * <p>进行批量权限变更操作后，可调用此方法清除全量缓存。
     */
    void clearAllCache();
}
