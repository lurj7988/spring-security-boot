package com.original.security.dto;

import java.util.Date;

/**
 * 会话信息数据传输对象。
 * <p>
 * 用于封装用户的会话详情，包含会话 ID、用户名、登录时间、最后活跃时间以及 IP 地址等。
 * </p>
 *
 * @author Naulu
 * @since 0.1.0
 */
public class SessionInfo {

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录时间（或创建时间）
     */
    private Date loginTime;

    /**
     * 最后活跃时间
     */
    private Date lastActiveTime;

    /**
     * IP 地址（如果有）
     */
    private String ipAddress;

    public SessionInfo() {
    }

    public SessionInfo(String sessionId, String username, Date loginTime, Date lastActiveTime, String ipAddress) {
        this.sessionId = sessionId;
        this.username = username;
        this.loginTime = loginTime;
        this.lastActiveTime = lastActiveTime;
        this.ipAddress = ipAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
