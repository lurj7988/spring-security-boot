package com.original.security.controller;

import com.original.security.core.Response;
import com.original.security.dto.KickResult;
import com.original.security.dto.PageResult;
import com.original.security.dto.SessionInfo;
import com.original.security.event.SessionKickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话管理控制器。
 * <p>
 * 提供对活跃会话的查询和踢出功能。
 * 依赖于 {@link SessionRegistry}，在无状态模式（如纯 JWT 且不保存会话记录）下，可能返回空数据。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/sessions")
@ConditionalOnProperty(prefix = "security.endpoints", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SessionController {

    private static final Logger log = LoggerFactory.getLogger(SessionController.class);
    private static final String WARN_SESSION_REGISTRY_UNAVAILABLE = "SessionRegistry not available, returning empty session list";
    private static final String WARN_NO_AUTHENTICATION = "No authentication found, returning empty session list";
    private static final int MAX_PAGE_SIZE = 1000;
    private static final int DEFAULT_PAGE_SIZE = 10;

    // 踢出原因常量
    private static final String KICK_REASON_ADMIN = "admin_kick";
    private static final String KICK_REASON_ADMIN_SESSION = "admin_kick_session";

    private final ObjectProvider<SessionRegistry> sessionRegistryProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造会话管理控制器。
     *
     * @param sessionRegistryProvider SessionRegistry 提供者
     * @param eventPublisher 事件发布器
     */
    public SessionController(ObjectProvider<SessionRegistry> sessionRegistryProvider,
                         ApplicationEventPublisher eventPublisher) {
        this.sessionRegistryProvider = sessionRegistryProvider;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 查询所有用户的活跃会话（仅限管理员）。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页大小
     * @return 分页的会话列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Response<PageResult<SessionInfo>> getAllSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        long startTime = System.currentTimeMillis();

        // Validate pagination inputs and log corrections
        int originalPage = page;
        int originalSize = size;
        if (page < 1) {
            page = 1;
            log.warn("Invalid page parameter {} corrected to 1", originalPage);
        }
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
            log.warn("Invalid size parameter {} corrected to {}", originalSize, DEFAULT_PAGE_SIZE);
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
            log.warn("Size parameter {} exceeds maximum, corrected to {}", originalSize, MAX_PAGE_SIZE);
        }

        SessionRegistry sessionRegistry = sessionRegistryProvider.getIfAvailable();
        if (sessionRegistry == null) {
            log.warn(WARN_SESSION_REGISTRY_UNAVAILABLE);
            return Response.successBuilder(new PageResult<SessionInfo>(page, size, 0, new ArrayList<>())).build();
        }

        List<SessionInfo> allSessions = new ArrayList<>();

        // 获取所有 principal（通常是 UserDetails 实例或 username 字符串）
        List<Object> principals = sessionRegistry.getAllPrincipals();
        for (Object principal : principals) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            String username = extractUsername(principal);
            for (SessionInformation session : sessions) {
                allSessions.add(mapToSessionInfo(session, username));
            }
        }

        // 排序：按最后活跃时间降序
        allSessions.sort(Comparator.comparing(SessionInfo::getLastActiveTime).reversed());

        // 内存分页
        // 注意：对于大量会话场景，建议实现数据库级分页或使用缓存优化
        int total = allSessions.size();
        int startIndex = (page - 1) * size;
        List<SessionInfo> pagedList = new ArrayList<>();
        if (startIndex < total) {
            int endIndex = Math.min(startIndex + size, total);
            pagedList = allSessions.subList(startIndex, endIndex);
        }

        PageResult<SessionInfo> result = new PageResult<>(page, size, total, pagedList);
        long duration = System.currentTimeMillis() - startTime;
        log.info("Query all sessions completed: total={}, page={}, size={}, returned={}, duration={}ms",
                total, page, size, pagedList.size(), duration);

        return Response.successBuilder(result).build();
    }

    /**
     * 查询当前用户的活跃会话。
     *
     * @return 当前用户的会话列表
     */
    @GetMapping("/me")
    public Response<List<SessionInfo>> getMySessions() {
        long startTime = System.currentTimeMillis();

        SessionRegistry sessionRegistry = sessionRegistryProvider.getIfAvailable();
        if (sessionRegistry == null) {
            log.warn(WARN_SESSION_REGISTRY_UNAVAILABLE);
            return Response.successBuilder((List<SessionInfo>) new ArrayList<SessionInfo>()).build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn(WARN_NO_AUTHENTICATION);
            return Response.successBuilder((List<SessionInfo>) new ArrayList<SessionInfo>()).build();
        }

        Object principal = authentication.getPrincipal();
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

        String username = extractUsername(principal);
        List<SessionInfo> sessionInfos = sessions.stream()
                .map(s -> mapToSessionInfo(s, username))
                .sorted(Comparator.comparing(SessionInfo::getLastActiveTime).reversed())
                .collect(Collectors.toList());

        long duration = System.currentTimeMillis() - startTime;
        log.info("Query my sessions completed: username={}, sessions={}, duration={}ms",
                username, sessionInfos.size(), duration);

        return Response.successBuilder(sessionInfos).build();
    }

    /**
     * 强制指定用户的所有会话下线（仅限管理员）。
     *
     * @param userId 目标用户 ID
     * @param reason 踢出原因（可选，默认为 "admin_kick"）
     * @return 踢出结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/kick")
    public Response<KickResult> kickUser(
            @PathVariable String userId,
            @RequestParam(value = "reason", required = false) String reason) {
        long startTime = System.currentTimeMillis();

        // 参数校验：userId 不能为空或空白
        if (!StringUtils.hasText(userId)) {
            log.warn("Invalid userId parameter: empty or blank");
            return Response.<KickResult>withBuilder(400)
                    .msg("userId cannot be empty or blank")
                    .build();
        }

        SessionRegistry sessionRegistry = sessionRegistryProvider.getIfAvailable();
        if (sessionRegistry == null) {
            log.warn(WARN_SESSION_REGISTRY_UNAVAILABLE);
            return Response.<KickResult>withBuilder(500)
                    .msg("SessionRegistry not available, cannot kick user")
                    .build();
        }

        String operator = getCurrentUsername();
        String kickReason = StringUtils.hasText(reason) ? reason : KICK_REASON_ADMIN;
        log.info("Kicking user: userId={}, operator={}, reason={}", userId, operator, kickReason);

        List<SessionInformation> sessionsToKick = new ArrayList<>();
        List<Object> principals = sessionRegistry.getAllPrincipals();

        // 查找目标用户的所有会话
        for (Object principal : principals) {
            String username = extractUsername(principal);
            if (userId.equals(username)) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                // 过滤掉已过期的会话
                for (SessionInformation session : sessions) {
                    if (!session.isExpired()) {
                        sessionsToKick.add(session);
                    }
                }
            }
        }

        // 执行踢出操作
        int kickedCount = 0;
        for (SessionInformation session : sessionsToKick) {
            sessionRegistry.removeSessionInformation(session.getSessionId());
            log.debug("Removed session: sessionId={}, userId={}", session.getSessionId(), userId);
            kickedCount++;

            // 发布踢出事件
            eventPublisher.publishEvent(new SessionKickEvent(
                    this,
                    userId,
                    session.getSessionId(),
                    operator,
                    kickReason
            ));
        }

        KickResult result = new KickResult(userId, kickedCount,
                kickedCount > 0 ? "User kicked successfully" : "No active sessions found for user");

        long duration = System.currentTimeMillis() - startTime;
        log.info("Kick user completed: userId={}, kicked={}, duration={}ms",
                userId, kickedCount, duration);

        return Response.successBuilder(result).build();
    }

    /**
     * 踢出指定会话（仅限管理员）。
     *
     * @param sessionId 目标会话 ID
     * @param reason 踢出原因（可选，默认为 "admin_kick_session"）
     * @return 踢出结果
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{sessionId}/kick")
    public Response<KickResult> kickSession(
            @PathVariable String sessionId,
            @RequestParam(value = "reason", required = false) String reason) {
        long startTime = System.currentTimeMillis();

        // 参数校验：sessionId 不能为空或空白
        if (!StringUtils.hasText(sessionId)) {
            log.warn("Invalid sessionId parameter: empty or blank");
            return Response.<KickResult>withBuilder(400)
                    .msg("sessionId cannot be empty or blank")
                    .build();
        }

        SessionRegistry sessionRegistry = sessionRegistryProvider.getIfAvailable();
        if (sessionRegistry == null) {
            log.warn(WARN_SESSION_REGISTRY_UNAVAILABLE);
            return Response.<KickResult>withBuilder(500)
                    .msg("SessionRegistry not available, cannot kick session")
                    .build();
        }

        String operator = getCurrentUsername();
        String kickReason = StringUtils.hasText(reason) ? reason : KICK_REASON_ADMIN_SESSION;
        log.info("Kicking session: sessionId={}, operator={}, reason={}", sessionId, operator, kickReason);

        // 查找指定会话
        SessionInformation targetSession = null;
        String userId = null;
        List<Object> principals = sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {
            userId = extractUsername(principal);
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            for (SessionInformation session : sessions) {
                if (sessionId.equals(session.getSessionId()) && !session.isExpired()) {
                    targetSession = session;
                    break;
                }
            }
            if (targetSession != null) {
                break;
            }
        }

        if (targetSession == null) {
            log.warn("Session not found or already expired: sessionId={}", sessionId);
            return Response.<KickResult>withBuilder(404)
                    .msg("Session not found or already expired")
                    .build();
        }

        // 执行踢出操作
        sessionRegistry.removeSessionInformation(sessionId);
        log.debug("Removed session: sessionId={}", sessionId);

        // 发布踢出事件
        eventPublisher.publishEvent(new SessionKickEvent(
                this,
                userId,
                sessionId,
                operator,
                kickReason
        ));

        KickResult result = new KickResult(userId, 1, "Session kicked successfully");

        long duration = System.currentTimeMillis() - startTime;
        log.info("Kick session completed: sessionId={}, userId={}, duration={}ms",
                sessionId, userId, duration);

        return Response.successBuilder(result).build();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return extractUsername(authentication.getPrincipal());
        }
        return "unknown";
    }

    private String extractUsername(Object principal) {
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    private SessionInfo mapToSessionInfo(SessionInformation session, String username) {
        SessionInfo info = new SessionInfo();
        info.setSessionId(session.getSessionId());
        info.setUsername(username);

        // 注意：Spring Security 标准的 SessionInformation 不直接提供会话创建时间（loginTime）
        // 由于无法获取准确的登录时间，这里将 loginTime 和 lastActiveTime 都设置为最后请求时间
        // 这是 Spring Security 的限制，如需区分登录时间和最后活跃时间，需要：
        // 1. 扩展 SessionRegistry 实现记录创建时间
        // 2. 或在会话创建时在应用层记录额外元数据
        info.setLoginTime(session.getLastRequest());
        info.setLastActiveTime(session.getLastRequest());

        // 注意：SessionRegistry 默认不记录 IP 地址
        // 如需获取真实 IP 地址，需要：
        // 1. 扩展 SessionRegistry 实现在会话创建时记录请求 IP
        // 2. 或维护一个独立的 sessionId -> IP 地址映射表
        info.setIpAddress("unknown");

        return info;
    }
}
