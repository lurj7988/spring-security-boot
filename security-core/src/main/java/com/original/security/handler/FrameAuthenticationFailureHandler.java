package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.original.security.event.AuditEventPublisher;
import com.original.security.event.AuthenticationFailureEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义认证失败处理器
 * <p>
 * 统一处理 Spring Security 抛出的 AuthenticationException 并通过框架标准的 Response 返回。
 * 同时发布 {@link AuthenticationFailureEvent} 审计事件用于安全监控。
 * </p>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class FrameAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(FrameAuthenticationFailureHandler.class);
    
    private final ObjectMapper objectMapper;
    private final AuditEventPublisher auditEventPublisher;

    public FrameAuthenticationFailureHandler(ObjectMapper objectMapper, AuditEventPublisher auditEventPublisher) {
        this.objectMapper = objectMapper;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.warn("用户认证失败: {}", exception.getMessage());

        // 发布认证失败审计事件
        publishAuditFailure(request, exception);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        String message = "认证失败";
        // 区分"账号禁用"和"密码错误"，但不要区分用户名不存在
        if (exception instanceof DisabledException) {
            message = "账号已被禁用";
        } else if (exception instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        }

        Response<Object> errorResponse = Response.withBuilder(HttpStatus.UNAUTHORIZED.value())
                .msg(message)
                .build();
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private void publishAuditFailure(HttpServletRequest request, AuthenticationException exception) {
        try {
            String username = request.getParameter("username");
            if (username == null) {
                username = "unknown";
            }

            Map<String, Object> details = new HashMap<>();
            details.put("ipAddress", request.getRemoteAddr());
            details.put("userAgent", request.getHeader("User-Agent"));
            details.put("exceptionType", exception.getClass().getName());

            AuthenticationFailureEvent event = new AuthenticationFailureEvent(
                this,
                username,
                exception.getMessage(),
                details
            );
            auditEventPublisher.publish(event);
        } catch (Exception e) {
            // 事件发布失败不应影响正常错误响应
            log.warn("Failed to publish authentication failure audit event: {}", e.getMessage());
        }
    }
}
