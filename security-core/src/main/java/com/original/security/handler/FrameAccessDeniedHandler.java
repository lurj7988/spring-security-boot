package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
public class FrameAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(FrameAccessDeniedHandler.class);
    
    private final ObjectMapper objectMapper;

    public FrameAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("访问被拒绝: {}", accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        String message = "拒绝访问";
        if (accessDeniedException instanceof CsrfException) {
            message = "无效的 CSRF Token";
        }

        Response<Object> errorResponse = Response.withBuilder(HttpStatus.FORBIDDEN.value())
                .msg(message)
                .location(request.getRequestURI())
                .build();
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
