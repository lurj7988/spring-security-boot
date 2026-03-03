package com.original.security.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步处理配置
 *
 * <p>启用 Spring 异步事件处理支持（{@code @Async}），使 {@code @EventListener} 与
 * {@code @Async} 组合可以在独立线程中执行，避免阻塞业务主事务线程。
 *
 * <p>主要用途：审计事件监听器（FR15）异步处理权限分配审计日志。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot 默认使用 SimpleAsyncTaskExecutor 执行 @Async 任务。
    // 如需自定义线程池（如线程数、队列大小等），可在此注入 TaskExecutor Bean。
}
