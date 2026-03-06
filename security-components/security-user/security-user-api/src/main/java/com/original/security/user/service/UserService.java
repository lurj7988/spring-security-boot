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
     * 获取用户列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页用户列表
     */
    PageDTO<UserDTO> listUsers(int page, int size);
}
