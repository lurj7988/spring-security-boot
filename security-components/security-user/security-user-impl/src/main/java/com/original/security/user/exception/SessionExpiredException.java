package com.original.security.user.exception;

/**
 * 会话失效异常
 *
 * <p>此异常用于表示用户会话已过期或无效。
 * 可在以下场景使用：
 * <ul>
 *   <li>JWT Token 过期时</li>
 *   <li>Session 被强制失效时（如密码修改后）</li>
 *   <li>并发登录控制时</li>
 * </ul>
 *
 * TODO: 在实现会话管理功能时集成此异常
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SessionExpiredException extends RuntimeException {

    public SessionExpiredException(String message) {
        super(message);
    }

    public SessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}