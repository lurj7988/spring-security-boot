package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import com.original.security.event.AuthorizationFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一处理 Spring Security 抛出的 AccessDeniedException (包括 CsrfException)
 * 并通过框架标准的 Response 返回。
 * <p>
 * 同时发布 {@link AuthorizationFailureEvent} 审计事件，用于安全监控和审计日志。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class FrameAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(FrameAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public FrameAccessDeniedHandler(ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        String errorMessage = accessDeniedException.getMessage();

        log.warn("访问被拒绝: URI={}, Reason={}", requestUri, errorMessage);

        // 发布授权失败审计事件
        publishAuthorizationFailureEvent(requestUri, errorMessage);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        String message = "拒绝访问";
        if (accessDeniedException instanceof CsrfException) {
            message = "无效的 CSRF Token";
        }

        Response<Object> errorResponse = Response.withBuilder(HttpStatus.FORBIDDEN.value())
                .msg(message)
                .location(requestUri)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * 发布授权失败审计事件。
     *
     * @param resource 被访问的资源路径
     * @param denialReason 拒绝原因
     */
    private void publishAuthorizationFailureEvent(String resource, String denialReason) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requiredAuthority = extractRequiredAuthority(denialReason);

            AuthorizationFailureEvent event = new AuthorizationFailureEvent(
                    this,
                    authentication,
                    resource,
                    requiredAuthority,
                    denialReason
            );

            eventPublisher.publishEvent(event);
            log.debug("Published authorization failure event: {}", event);
        } catch (Exception e) {
            // 事件发布失败不应影响正常响应
            log.error("Failed to publish authorization failure event", e);
        }
    }

    /**
     * 从异常消息中提取需要的权限信息。
     *
     * @param denialReason 拒绝原因消息
     * @return 需要的权限，如果无法提取则返回 "unknown"
     */
    private String extractRequiredAuthority(String denialReason) {
        if (denialReason == null || denialReason.isEmpty()) {
            return "unknown";
        }

        // 尝试从常见的 Spring Security 异常信息中提取具体的权限或角色信息。
        // 例如：对于 "@PreAuthorize("hasRole('ADMIN')")"，如果没有处理可能会显示 "Access is denied"

        // 查找特定的访问拒绝消息，可以从 SecurityContext 中进一步推断。不过，
        // 简单提取通常需要拦截具体的 Expression 类型，这里我们从常见的 denialReason 中解析具体的缺失内容。
        if (denialReason.contains("Access is denied") || denialReason.contains("拒绝访问")) {
            // Spring Security 通常会在抛出 AccessDeniedException 时带上详细原因，如果可能的话。
            // 简单的回退：
            return "expression-based";
        }

        return "unknown";
    }
}
