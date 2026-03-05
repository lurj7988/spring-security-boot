package com.original.security.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 会话踢出检测过滤器。
 * <p>
 * 该过滤器检测当前会话是否被管理员踢出，如果是则设置 request attribute，
 * 以便 SessionExpiredHandler 能够返回特定的错误消息。
 * </p>
 *
 * @author Naulu
 * @since 0.1.0
 */
public class SessionKickDetectionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionKickDetectionFilter.class);

    private final SessionRegistry sessionRegistry;

    /**
     * 构造会话踢出检测过滤器。
     *
     * @param sessionRegistry Session 注册表
     */
    public SessionKickDetectionFilter(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String sessionId = session.getId();

            // 检查会话是否还在 SessionRegistry 中
            SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionId);

            if (sessionInfo == null) {
                // 会话不在注册表中，说明可能被踢出了
                // 检查是否有 kicked 标记
                Boolean wasKicked = (Boolean) session.getAttribute("sessionWasKicked");

                if (Boolean.TRUE.equals(wasKicked)) {
                    String kickReason = (String) session.getAttribute("kickReason");
                    request.setAttribute("sessionWasKicked", true);
                    request.setAttribute("kickReason", kickReason != null ? kickReason : "admin_kick");
                    log.debug("Detected kicked session: sessionId={}, reason={}", sessionId, kickReason);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
