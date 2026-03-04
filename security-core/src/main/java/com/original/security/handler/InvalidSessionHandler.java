package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 无效 Session 处理器。
 * <p>
 * 当请求携带无效的 Session ID 时，返回 401 Unauthorized 响应，
 * 并返回 JSON 格式的错误信息提示用户重新登录。
 * </p>
 *
 * @author Original Security Team
 * @since 1.0.0
 * @see SessionExpiredHandler
 */
public class InvalidSessionHandler implements InvalidSessionStrategy {

    private static final Logger log = LoggerFactory.getLogger(InvalidSessionHandler.class);

    private final ObjectMapper objectMapper;

    /**
     * 构造无效 Session 处理器。
     *
     * @param objectMapper JSON 序列化器
     */
    public InvalidSessionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 处理无效 Session 请求。
     * <p>
     * 当请求携带无效的 Session ID 时，此方法被调用。
     * 返回 401 Unauthorized 响应，并在响应体中包含错误信息。
     * </p>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException 如果写入响应失败
     */
    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUri = request.getRequestURI();
        log.warn("Invalid session detected for request: {}", requestUri);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Response<Object> errorResponse = Response.<Object>withBuilder(HttpStatus.UNAUTHORIZED.value())
                .msg("无效会话，请重新登录")
                .location(requestUri)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
