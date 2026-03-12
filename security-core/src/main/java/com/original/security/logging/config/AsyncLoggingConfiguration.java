package com.original.security.logging.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步日志配置。
 * <p>
 * 启用 Spring @Async 支持，用于 SecurityLoggingListener 的异步事件处理。
 * 配置有界线程池，确保日志记录不会阻塞主业务流程，同时防止资源耗尽。
 * <p>
 * 注意：本配置不实现 AsyncConfigurer，避免覆盖消费应用的全局异步配置。
 * SecurityLoggingListener 通过 @Async("securityLoggingTaskExecutor") 指定使用此线程池。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncLoggingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AsyncLoggingConfiguration.class);

    /**
     * 核心线程数。
     */
    private static final int CORE_POOL_SIZE = 2;

    /**
     * 最大线程数。
     */
    private static final int MAX_POOL_SIZE = 10;

    /**
     * 队列容量。
     */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * 线程名前缀。
     */
    private static final String THREAD_NAME_PREFIX = "SecurityLog-";

    /**
     * 线程空闲时间（秒）。
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 配置异步日志线程池。
     * <p>
     * SecurityLoggingListener 使用 @Async("securityLoggingTaskExecutor") 引用此执行器。
     *
     * @return 线程池任务执行器
     */
    @Bean(name = "securityLoggingTaskExecutor")
    public Executor securityLoggingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
