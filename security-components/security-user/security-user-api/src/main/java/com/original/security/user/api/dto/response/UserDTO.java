package com.original.security.user.api.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户响应 DTO
 * 不包含密码字段
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 用户角色列表
     */
    private Set<String> roles;
}
