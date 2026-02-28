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

/**
 * 自定义认证失败处理器
 * <p>
 * 统一处理 Spring Security 抛出的 AuthenticationException 并通过框架标准的 Response 返回。
 * </p>
 *
 * @author Original Security Team
 */
@Component
public class FrameAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(FrameAuthenticationFailureHandler.class);
    
    private final ObjectMapper objectMapper;

    public FrameAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.warn("用户认证失败: {}", exception.getMessage());

        // TODO: Publish audit event when audit module is available (FR15)
        // auditEventPublisher.publish(AuthenticationAuditEvent.failed(username, exception));

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
}
