package com.example.quickstart.entity;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息 DTO。
 * <p>
 * 用于向客户端返回用户的基本信息和角色信息。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
public class UserInfo {

    /** 用户ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 邮箱地址。 */
    private String email;

    /** 手机号码。 */
    private String phone;

    /** 用户状态（active/inactive）。 */
    private String status;

    /** 用户角色列表。 */
    private List<String> roles;

    /**
     * 默认构造函数。
     */
    public UserInfo() {
    }

    /**
     * 全参构造函数。
     *
     * @param id 用户ID
     * @param username 用户名
     * @param email 邮箱
     * @param phone 手机号
     * @param status 状态
     * @param roles 角色列表
     */
    public UserInfo(Long id, String username, String email, String phone, String status, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.roles = roles;
    }

    /**
     * 从 UserDetails 构建用户信息。
     *
     * @param userDetails 用户详情对象
     * @param id 用户ID
     * @param email 邮箱
     * @param phone 手机号
     * @param status 状态
     * @return 用户信息对象
     * @throws IllegalArgumentException 如果 userDetails 为 null
     */
    public static UserInfo fromUserDetails(
            UserDetails userDetails,
            Long id, String email, String phone, String status) {
        if (userDetails == null) {
            throw new IllegalArgumentException("userDetails cannot be null");
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

        return new UserInfo(id, userDetails.getUsername(), email, phone, status, roles);
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}