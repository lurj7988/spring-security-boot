package com.original.security.tracing;

import com.original.security.tracing.config.SecurityTracingProperties;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.original.security.tracing.TracingConstants.*;

/**
 * 认证追踪过滤器。
 * <p>
 * 为每个安全请求创建追踪 Span，记录认证信息。
 * 自动提取请求信息并添加到追踪标签中。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class AuthenticationTracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationTracingFilter.class);

    private final SecurityTracer securityTracer;
    private final Tracer tracer;
    private final SecurityTracingProperties properties;

    /**
     * 构造函数。
     *
     * @param securityTracer 安全追踪器
     * @param tracer         Micrometer Tracer 实例（用于 SpanInScope）
     * @param properties     追踪配置属性
     */
    public AuthenticationTracingFilter(SecurityTracer securityTracer, Tracer tracer, SecurityTracingProperties properties) {
        this.securityTracer = securityTracer;
        this.tracer = tracer;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!securityTracer.isAvailable()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 创建请求追踪 Span
        String spanName = buildSpanName(request);
        Map<String, String> tags = buildRequestTags(request);

        io.micrometer.tracing.Span span = securityTracer.startSpan(spanName, tags);

        // 设置 SpanInScope，确保下游组件能获取正确的追踪上下文
        Tracer.SpanInScope ws = (span != null && tracer != null) ? tracer.withSpan(span) : null;
        try {
            // 添加请求开始事件
            securityTracer.addEvent(EVENT_AUTH_START);

            // 执行过滤器链
            filterChain.doFilter(request, response);

            // 记录认证成功
            recordAuthenticationSuccess(request);

        } catch (Exception e) {
            // 记录认证失败
            recordAuthenticationFailure(e);
            throw e;
        } finally {
            // 添加请求完成事件
            securityTracer.addEvent(EVENT_AUTH_COMPLETE);

            // 关闭 SpanInScope
            if (ws != null) {
                ws.close();
            }
            // 结束 Span
            if (span != null) {
                span.end();
            }
        }
    }

    /**
     * 构建 Span 名称。
     * <p>
     * 对于登录路径返回固定的认证 Span 名称，其他路径返回 "METHOD PATH" 格式。
     * 路径中不包含查询参数以避免敏感信息泄露。
     *
     * @param request HTTP 请求
     * @return Span 名称
     */
    private String buildSpanName(HttpServletRequest request) {
        if (isLoginPath(request.getRequestURI())) {
            return SPAN_AUTHENTICATION;
        }
        String method = request.getMethod();
        String path = getRequestPath(request);
        // 移除查询参数，避免敏感信息进入 Span 名称
        String pathWithoutQuery = removeQueryParameters(path);
        return String.format("%s %s", method, pathWithoutQuery);
    }

    /**
     * 移除 URL 中的查询参数。
     *
     * @param path 可能包含查询参数的路径
     * @return 不含查询参数的路径
     */
    private String removeQueryParameters(String path) {
        if (path == null) {
            return "/";
        }
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            return path.substring(0, queryIndex);
        }
        return path;
    }

    /**
     * 检查请求路径是否为登录路径。
     *
     * @param requestUri 请求 URI
     * @return 如果是登录路径返回 true
     */
    private boolean isLoginPath(String requestUri) {
        if (properties.getLoginPaths() != null) {
            for (String loginPath : properties.getLoginPaths()) {
                if (requestUri.endsWith(loginPath)) {
                    return true;
                }
            }
        }
        return requestUri.endsWith("/login");
    }

    /**
     * 构建请求标签。
     *
     * @param request HTTP 请求
     * @return 标签 Map
     */
    private Map<String, String> buildRequestTags(HttpServletRequest request) {
        Map<String, String> tags = new HashMap<>();

        tags.put(TAG_HTTP_METHOD, request.getMethod());
        tags.put(TAG_REQUEST_PATH, getRequestPath(request));

        // 添加认证类型（优先级：Bearer > Basic > Session）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tags.put(TAG_AUTH_TYPE, AUTH_TYPE_JWT);
        } else if (authHeader != null && authHeader.startsWith("Basic ")) {
            tags.put(TAG_AUTH_TYPE, AUTH_TYPE_USERNAME_PASSWORD);
        } else if (request.getSession(false) != null) {
            // 仅当没有 Authorization header 时才标记为 Session 认证
            tags.put(TAG_AUTH_TYPE, AUTH_TYPE_SESSION);
        }

        return tags;
    }

    /**
     * 记录认证成功。
     *
     * @param request HTTP 请求
     */
    private void recordAuthenticationSuccess(HttpServletRequest request) {
        if (!isLoginPath(request.getRequestURI())) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            securityTracer.addTag(TAG_AUTH_RESULT, VALUE_SUCCESS);

            String username = authentication.getName();
            if (username != null && !"anonymousUser".equals(username)) {
                securityTracer.addTag(TAG_USERNAME, securityTracer.maskUsername(username));
            }

            log.debug("Authentication success recorded for user: {}", securityTracer.maskUsername(username));
        }
    }

    /**
     * 记录认证失败。
     *
     * @param e 异常
     */
    private void recordAuthenticationFailure(Exception e) {
        securityTracer.addTag(TAG_AUTH_RESULT, VALUE_FAILURE);
        securityTracer.addTag(TAG_ERROR_TYPE, e.getClass().getSimpleName());
        securityTracer.recordError(e);

        log.debug("Authentication failure recorded: {}", e.getMessage());
    }

    /**
     * 获取请求路径。
     *
     * @param request HTTP 请求
     * @return 请求路径
     */
    private String getRequestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (properties.getIgnoredPaths() != null) {
            for (String ignoredPath : properties.getIgnoredPaths()) {
                if (path.startsWith(ignoredPath)) {
                    return true;
                }
            }
        }
        return false;
    }
}
