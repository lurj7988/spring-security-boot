package com.original.security.filter;

import com.original.security.config.SecurityMetricsProperties;
import com.original.security.observability.SecurityMetrics;
import com.original.security.plugin.SecurityFilterPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证耗时 Metrics 记录过滤器。
 * <p>
 * 记录认证请求的处理时间。仅对配置的认证路径生效。
 * 通过 {@code security.metrics.authentication-duration-enabled} 属性控制是否启用。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Component
@ConditionalOnBean(SecurityMetrics.class)
@ConditionalOnProperty(prefix = "security.metrics", name = "authentication-duration-enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationMetricsFilter extends OncePerRequestFilter implements SecurityFilterPlugin {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationMetricsFilter.class);

    private static final String START_TIME_ATTR = "authentication.startTime";

    private final SecurityMetrics securityMetrics;
    private final SecurityMetricsProperties properties;
    private final List<String> authPaths;

    public AuthenticationMetricsFilter(SecurityMetrics securityMetrics, SecurityMetricsProperties properties) {
        this.securityMetrics = securityMetrics;
        this.properties = properties;
        this.authPaths = properties.getAuthPaths();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 检查是否为认证请求
        if (isAuthenticationRequest(request)) {
            long startTime = System.nanoTime();
            request.setAttribute(START_TIME_ATTR, startTime);

            try {
                filterChain.doFilter(request, response);
            } finally {
                // 记录认证耗时
                recordAuthenticationDuration(request, response, startTime);
            }
        } else {
            // 非认证请求，不记录时间，直接通过
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public String getName() {
        return "AuthenticationMetricsFilter";
    }

    @Override
    public Filter getFilter() {
        return this;
    }

    @Override
    public Position getPosition() {
        return Position.BEFORE;
    }

    @Override
    public boolean isEnabled() {
        return properties.isAuthenticationDurationEnabled();
    }

    /**
     * 检查请求是否为认证请求。
     *
     * @param request HTTP 请求
     * @return 如果是认证请求返回 true
     */
    private boolean isAuthenticationRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        for (String path : authPaths) {
            if (uri.equals(path) || uri.startsWith(path + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 记录认证耗时。
     */
    private void recordAuthenticationDuration(HttpServletRequest request, HttpServletResponse response, long startTime) {
        long durationNanos = System.nanoTime() - startTime;
        String authMethod = getAuthMethod(request);
        int status = response.getStatus();

        securityMetrics.recordAuthenticationDuration(authMethod, durationNanos);
        log.debug("Authentication duration recorded: {}ms for {} (status: {})",
                TimeUnit.NANOSECONDS.toMillis(durationNanos), authMethod, status);
    }

    /**
     * 获取认证方法。
     */
    private String getAuthMethod(HttpServletRequest request) {
        Object authMethodAttr = request.getAttribute("authMethod");
        if (authMethodAttr != null) {
            return authMethodAttr.toString();
        }
        return "unknown";
    }
}

