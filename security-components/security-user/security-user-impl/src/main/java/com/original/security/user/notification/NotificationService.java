package com.original.security.user.notification;

import com.original.security.user.entity.User;

/**
 * 通知服务接口
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface NotificationService {

    /**
     * 发送密码重置通知
     *
     * @param user 被重置密码的用户
     * @param newPassword 生成的新密码
     */
    void sendPasswordResetNotification(User user, String newPassword);

    /**
     * 发送密码更改通知
     *
     * @param user 修改密码的用户
     */
    void sendPasswordChangedNotification(User user);
}