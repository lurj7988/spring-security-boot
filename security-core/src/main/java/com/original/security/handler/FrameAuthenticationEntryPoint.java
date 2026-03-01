package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义的身份验证入口点。
 * <p>
 * 当匿名用户尝试访问受保护资源，或者由于身份验证失败而抛出 {@link AuthenticationException} 时，
 * 框架将调用此处理器的 {@link #commence} 方法。
 * 根据架构要求，使用全局统一错误响应对象 {@link Response} 以 JSON 格式返回 401 Unauthorized。
 * </p>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class FrameAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(FrameAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    /**
     * 构建身份验证入口点。
     *
     * @param objectMapper 用于 JSON 序列化的构建器依赖对象
     */
    public FrameAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        log.warn("认证失败 (未登录或凭证无效): URI={}, Reason={}", requestUri, authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Response<Object> errorResponse = Response.withBuilder(HttpStatus.UNAUTHORIZED.value())
                .msg("未登录或提供无效的认证信息")
                .location(requestUri)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
