package com.original.security.user.service;

import com.original.security.user.api.dto.request.PermissionAssignRequest;
import com.original.security.user.api.dto.request.RoleCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.RoleDTO;

/**
 * 角色服务接口
 *
 * <p>提供用户角色的动态检查能力。实现类可能包含缓存机制，因此角色数据可能
 * 不是实时的。当用户角色发生变更时，调用方应主动调用 {@link #clearCache(String)}
 * 或 {@link #clearAllCache()} 使缓存失效，以保证数据一致性。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface RoleService {

    /**
     * 判断用户是否拥有指定角色
     *
     * <p>支持直接角色和通过 {@code RoleHierarchy} 配置的继承角色。
     *
     * @param username 用户名
     * @param role     角色名称
     * @return true 如果用户拥有该角色，false 如果没有
     */
    boolean hasRole(String username, String role);

    /**
     * 清除指定用户的角色缓存
     *
     * <p>当用户的角色发生变更时，应调用此方法使该用户的缓存失效。
     *
     * @param username 用户名，为 null 时不执行任何操作
     */
    void clearCache(String username);

    /**
     * 清除所有用户的角色缓存
     *
     * <p>进行批量角色变更操作后，可调用此方法清除全量缓存。
     */
    void clearAllCache();

    /**
     * 创建角色
     *
     * @param request 角色创建请求
     * @return 创建后的角色信息
     */
    RoleDTO createRole(RoleCreateRequest request);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param request 权限分配请求
     */
    void assignPermissions(Long roleId, PermissionAssignRequest request);

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleDTO getRole(Long roleId);

    /**
     * 分页查询角色列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 角色分页数据
     */
    PageDTO<RoleDTO> listRoles(int page, int size);
}
