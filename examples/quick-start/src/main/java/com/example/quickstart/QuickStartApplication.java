package com.example.quickstart;

import com.original.security.annotation.EnableSecurityBoot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Security Boot 快速开始示例启动类。
 * <p>
 * 通过 {@link EnableSecurityBoot} 注解启用框架自动配置，
 * 提供开箱即用的认证和授权功能。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
@EnableSecurityBoot
@SpringBootApplication
public class QuickStartApplication {

    /**
     * 应用程序入口点。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(QuickStartApplication.class, args);
    }
}
