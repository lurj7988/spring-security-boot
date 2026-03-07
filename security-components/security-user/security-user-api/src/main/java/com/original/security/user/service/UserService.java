package com.original.security.user.service;

import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;

/**
 * 用户服务接口
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param request 用户创建请求
     * @return 创建的用户信息
     */
    UserDTO createUser(UserCreateRequest request);

    /**
     * 获取当前用户
     *
     * @return 当前用户信息
     */
    UserDTO getCurrentUser();

    /**
     * 根据ID获取用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUser(Long userId);

    /**
     * 获取用户列表（支持分页、用户名模糊查询和状态筛选）
     *
     * @param page 页码
     * @param size 每页大小
     * @param usernameKeyword 用户名关键词（模糊查询，可选）
     * @param enabled 用户启用状态（true/false，可选）
     * @return 分页用户列表
     */
    PageDTO<UserDTO> listUsers(int page, int size, String usernameKeyword, Boolean enabled);
}
