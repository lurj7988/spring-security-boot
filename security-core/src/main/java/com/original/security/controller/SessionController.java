package com.original.security.controller;

import com.original.security.core.Response;
import com.original.security.dto.PageResult;
import com.original.security.dto.SessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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
 * 提供对活跃会话的查询功能。
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

    private final ObjectProvider<SessionRegistry> sessionRegistryProvider;

    public SessionController(ObjectProvider<SessionRegistry> sessionRegistryProvider) {
        this.sessionRegistryProvider = sessionRegistryProvider;
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
