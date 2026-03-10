package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.GrantedAuthority;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import com.original.security.util.JwtUtils;

import com.original.security.event.AuditEventPublisher;
import com.original.security.event.AuthenticationSuccessEvent;

/**
 * 自定义认证成功处理器
 * <p>
 * 遵循框架统一的 API 响应格式返回认证成功的用户数据。
 * 同时发布 {@link AuthenticationSuccessEvent} 审计事件用于安全监控。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class FrameAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(FrameAuthenticationSuccessHandler.class);
    
    private final ObjectMapper objectMapper;
    private final ObjectProvider<JwtUtils> jwtUtilsProvider;
    private final AuditEventPublisher auditEventPublisher;

    public FrameAuthenticationSuccessHandler(ObjectMapper objectMapper, 
                                           ObjectProvider<JwtUtils> jwtUtilsProvider,
                                           AuditEventPublisher auditEventPublisher) {
        this.objectMapper = objectMapper;
        this.jwtUtilsProvider = jwtUtilsProvider;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Log authentication success without exposing sensitive username information
        log.info("用户认证成功");
        
        // 发布认证成功审计事件
        publishAuditSuccess(request, authentication);
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        Map<String, Object> data = new HashMap<>();
        data.put("user", authentication.getPrincipal());
        
        JwtUtils jwtUtils = jwtUtilsProvider.getIfAvailable();
        if (jwtUtils != null) {
            Collection<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            String token = jwtUtils.generateToken(authentication.getName(), authorities);
            data.put("token", token);
        }

        // 使用框架标准的 Response 构建器
        Response<Object> successResponse = Response.successBuilder((Object)data).build();
        objectMapper.writeValue(response.getWriter(), successResponse);
    }

    private void publishAuditSuccess(HttpServletRequest request, Authentication authentication) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("ipAddress", request.getRemoteAddr());
            details.put("userAgent", request.getHeader("User-Agent"));

            String authMethod = "unknown";
            if (request.getAttribute("authMethod") != null) {
                authMethod = request.getAttribute("authMethod").toString();
            }

            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                this,
                authentication.getName(),
                authMethod,
                details
            );
            auditEventPublisher.publish(event);
        } catch (Exception e) {
            // 事件发布失败不应影响正常认证流程
            log.warn("Failed to publish authentication success audit event: {}", e.getMessage());
        }
    }
}
