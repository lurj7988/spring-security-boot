package com.original.security.controller;

import com.original.security.core.Response;
import com.original.security.dto.PageResult;
import com.original.security.dto.SessionInfo;
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
        
        SessionRegistry sessionRegistry = sessionRegistryProvider.getIfAvailable();
        if (sessionRegistry == null) {
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
        int total = allSessions.size();
        int startIndex = (page - 1) * size;
        List<SessionInfo> pagedList = new ArrayList<>();
        if (startIndex < total) {
            int endIndex = Math.min(startIndex + size, total);
            pagedList = allSessions.subList(startIndex, endIndex);
        }

        PageResult<SessionInfo> result = new PageResult<>(page, size, total, pagedList);
        return Response.successBuilder(result).build();
    }

    /**
     * 查询当前用户的活跃会话。
     *
     * @return 当前用户的会话列表
     */
    @GetMapping("/me")
    public Response<List<SessionInfo>> getMySessions() {
        SessionRegistry sessionRegistry = sessionRegistryProvider.getIfAvailable();
        if (sessionRegistry == null) {
            return Response.successBuilder((List<SessionInfo>) new ArrayList<SessionInfo>()).build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Response.successBuilder((List<SessionInfo>) new ArrayList<SessionInfo>()).build();
        }

        Object principal = authentication.getPrincipal();
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
        
        String username = extractUsername(principal);
        List<SessionInfo> sessionInfos = sessions.stream()
                .map(s -> mapToSessionInfo(s, username))
                .sorted(Comparator.comparing(SessionInfo::getLastActiveTime).reversed())
                .collect(Collectors.toList());

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
        // Spring Security SessionInformation 不直接提供创建时间，
        // 这里为了演示，我们假设 loginTime 即为会话最后一次访问时间或如果实现允许可扩展记录。
        // 由于无法获取准确的 loginTime，这里暂时将 loginTime 和 lastActiveTime 置为相同，或根据需求留空。
        info.setLoginTime(session.getLastRequest()); 
        info.setLastActiveTime(session.getLastRequest());
        // SessionRegistry 默认不记录 IP。如有需要，可通过自定义的 SessionRegistry 实现。
        info.setIpAddress("unknown");
        return info;
    }
}
