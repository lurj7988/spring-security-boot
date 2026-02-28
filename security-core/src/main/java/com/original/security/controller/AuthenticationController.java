package com.original.security.controller;

import com.original.security.core.Response;
import com.original.security.dto.AuthResponse;
import com.original.security.dto.LoginRequest;
import com.original.security.dto.RefreshRequest;
import com.original.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 默认认证控制器。
 * <p>
 * 提供基于 REST API 的认证端点，包括登录、登出和令牌刷新。
 * 通过 {@code security.endpoints.enabled} 配置项控制是否启用。
 * </p>
 * <p>
 * 注意：当前 refresh 端点使用 JWT access token 进行令牌刷新。
 * 完整的 refresh token 轮换功能（独立的长期有效 refresh token）将在后续版本中实现。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(prefix = "security.endpoints", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationController {

    private static final String ERROR_MSG_AUTH_FAILED = "用户名或密码错误";
    private static final String ERROR_MSG_INTERNAL_ERROR = "登录时发生未知错误，请稍后重试";
    private static final String ERROR_MSG_REFRESH_TOKEN_EMPTY = "刷新 Token 不能为空";
    private static final String ERROR_MSG_JWT_NOT_ENABLED = "JWT 认证未启用";
    private static final String ERROR_MSG_TOKEN_EXPIRED = "Token 已过期，请重新登录";
    private static final String ERROR_MSG_INVALID_TOKEN = "无效的 Token";
    private static final String ERROR_MSG_REFRESH_FAILED = "刷新 Token 失败，请稍后重试";

    private final AuthenticationManager authenticationManager;
    private final ObjectProvider<JwtUtils> jwtUtilsProvider;

    /**
     * 构造认证控制器。
     *
     * @param authenticationManager 认证管理器
     * @param jwtUtilsProvider JWT 工具类提供者（可选）
     */
    public AuthenticationController(AuthenticationManager authenticationManager, ObjectProvider<JwtUtils> jwtUtilsProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtUtilsProvider = jwtUtilsProvider;
    }

    /**
     * 用户登录接口。
     * <p>
     * 验证用户名和密码，成功后返回用户信息和 JWT Token（如果 JWT 认证已启用）。
     * </p>
     *
     * @param loginRequest 登录请求，包含用户名和密码
     * @return 认证响应，包含用户信息和 Token（如果可用）
     */
    @PostMapping("/login")
    public Response<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // 在无状态 JWT 模式下，SecurityContext 设置的作用有限，
            // 但保留以兼容可能的 session 模式扩展
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Object user = authentication.getPrincipal();
            String token = null;
            boolean jwtEnabled = false;

            JwtUtils jwtUtils = jwtUtilsProvider.getIfAvailable();
            if (jwtUtils != null) {
                jwtEnabled = true;
                Collection<String> authorities = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());
                token = jwtUtils.generateToken(authentication.getName(), authorities);
            }

            AuthResponse authResponse = new AuthResponse(user, token, jwtEnabled);
            return Response.successBuilder(authResponse).build();
        } catch (AuthenticationException e) {
            // 不暴露具体异常信息，仅返回通用错误消息
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_AUTH_FAILED).build();
        } catch (Exception e) {
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_INTERNAL_ERROR).build();
        }
    }

    /**
     * 用户登出接口。
     * <p>
     * 清除当前请求的安全上下文。
     * </p>
     *
     * @param request HTTP 请求对象
     * @return 登出响应
     */
    @PostMapping("/logout")
    public Response<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return Response.<Void>successBuilder(null).build();
    }

    /**
     * Token 刷新接口。
     * <p>
     * 使用当前有效的 JWT Token 颁发新的 Token。
     * 注意：此实现使用 access token 进行刷新。完整的 refresh token 轮换功能将在后续版本中实现。
     * </p>
     *
     * @param refreshRequest 刷新请求，包含当前的 JWT Token
     * @return 新的认证响应，包含新的 Token
     */
    @PostMapping("/refresh")
    public Response<AuthResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        String oldToken = refreshRequest.getToken();
        if (oldToken == null || oldToken.trim().isEmpty()) {
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_REFRESH_TOKEN_EMPTY).build();
        }

        JwtUtils jwtUtils = jwtUtilsProvider.getIfAvailable();
        if (jwtUtils == null) {
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_JWT_NOT_ENABLED).build();
        }

        try {
            // 校验并解析旧 token
            Claims claims = jwtUtils.parseToken(oldToken);
            String username = claims.getSubject();
            String authoritiesStr = claims.get("authorities", String.class);
            Collection<String> authorities = authoritiesStr != null && !authoritiesStr.isEmpty()
                    ? Arrays.asList(authoritiesStr.split(","))
                    : java.util.Collections.emptyList();

            // 生成新 token
            String newToken = jwtUtils.generateToken(username, authorities);

            AuthResponse authResponse = new AuthResponse(null, newToken, true);
            return Response.successBuilder(authResponse).build();

        } catch (ExpiredJwtException e) {
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_TOKEN_EXPIRED).build();
        } catch (JwtException | IllegalArgumentException e) {
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_INVALID_TOKEN).build();
        } catch (Exception e) {
            return Response.<AuthResponse>errorBuilder().msg(ERROR_MSG_REFRESH_FAILED).build();
        }
    }
}
