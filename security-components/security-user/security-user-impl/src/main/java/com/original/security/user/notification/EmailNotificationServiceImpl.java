package com.original.security.user.notification;

import com.original.security.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 邮件通知服务实现
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Service
public class EmailNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    @Override
    public void sendPasswordResetNotification(User user, String newPassword) {
        // 在实际应用中，这里应该集成邮件服务发送通知
        log.info("发送密码重置通知给用户: username={}, email={}", user.getUsername(), user.getEmail());
        // 注意：出于安全考虑，不在日志中记录密码信息

        // TODO: 实际的邮件发送逻辑应该在这里实现
        // 可能需要集成Spring Mail或其他邮件服务

        log.info("密码重置通知已发送给用户: username={}", user.getUsername());
    }

    @Override
    public void sendPasswordChangedNotification(User user) {
        // 在实际应用中，这里应该集成邮件服务发送通知
        log.info("发送密码更改通知给用户: username={}, email={}", user.getUsername(), user.getEmail());
        log.info("您的密码已于 {} 更改", java.time.LocalDateTime.now());

        // TODO: 实际的邮件发送逻辑应该在这里实现
        // 可能需要集成Spring Mail或其他邮件服务

        log.info("密码更改通知已发送给用户: username={}", user.getUsername());
    }
}