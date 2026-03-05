package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Session 过期处理器。
 * <p>
 * 当 Session 过期或并发登录被踢出时，返回 401 Unauthorized 响应，
 * 并返回 JSON 格式的错误信息提示用户重新登录。
 * 对于被管理员踢出的场景，返回特定错误消息"账号已在其他设备登录"。
 * </p>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SessionExpiredHandler implements SessionInformationExpiredStrategy {

    private static final Logger log = LoggerFactory.getLogger(SessionExpiredHandler.class);

    private final ObjectMapper objectMapper;
    private final SessionRegistry sessionRegistry;

    /**
     * 构造 Session 过期处理器。
     *
     * @param objectMapper JSON 序列化器
     * @param sessionRegistry Session 注册表（用于检测会话是否被踢出）
     */
    public SessionExpiredHandler(ObjectMapper objectMapper, SessionRegistry sessionRegistry) {
        this.objectMapper = objectMapper;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * 处理 Session 过期事件。
     * <p>
     * 当用户的 Session 过期或因并发登录被踢出时，此方法被调用。
     * 返回 401 Unauthorized 响应，并在响应体中包含错误信息。
     * 对于被管理员踢出的场景，返回特定消息"账号已在其他设备登录"。
     * </p>
     *
     * @param event              Session 信息过期事件
     * @throws IOException 如果写入响应失败
     */
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();

        String requestUri = request.getRequestURI();
        SessionInformation sessionInfo = event.getSessionInformation();

        // 检查会话是否被踢出：如果 sessionId 不在 SessionRegistry 中，说明被踢出了
        String sessionId = sessionInfo.getSessionId();
        SessionInformation registryInfo = (sessionRegistry != null) ? sessionRegistry.getSessionInformation(sessionId) : null;

        String message;
        if (registryInfo == null) {
            // 会话不在注册表中，很可能是被管理员踢出了
            message = "账号已在其他设备登录";
            log.warn("Session kicked by admin for user: {}, Session ID: {}",
                    sessionInfo.getPrincipal(), sessionId);
        } else {
            // 会话自然过期
            message = "会话已过期，请重新登录";
            log.warn("Session expired for user: {}, Session ID: {}",
                    sessionInfo.getPrincipal(), sessionId);
        }

        log.warn("{}: URI={}", message, requestUri);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Response<Object> errorResponse = Response.<Object>withBuilder(HttpStatus.UNAUTHORIZED.value())
                .msg(message)
                .location(requestUri)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
