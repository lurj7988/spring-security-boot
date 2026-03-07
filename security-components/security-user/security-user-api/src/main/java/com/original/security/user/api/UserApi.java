package com.original.security.user.api;

import com.original.security.core.Response;
import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理 API 接口
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@FeignClient(name = "security-user-api", url = "${security.user.api.url:}")
@RequestMapping("/api/users")
public interface UserApi {

    /**
     * 创建用户
     *
     * @param request 用户创建请求
     * @return 创建的用户信息
     */
    @PostMapping
    Response<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request);

    /**
     * 获取当前用户信息
     * 需要用户已认证
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    Response<UserDTO> getCurrentUser();

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    Response<UserDTO> getUser(@PathVariable("userId") Long userId);

    /**
     * 获取用户列表（支持分页、用户名模糊查询和状态筛选）
     *
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名关键词（模糊查询，可选）
     * @param enabled 用户启用状态（true/false，可选）
     * @return 分页用户列表
     */
    @GetMapping
    Response<PageDTO<UserDTO>> listUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        @RequestParam(value = "username", required = false) String username,
                                        @RequestParam(value = "enabled", required = false) Boolean enabled);
}
