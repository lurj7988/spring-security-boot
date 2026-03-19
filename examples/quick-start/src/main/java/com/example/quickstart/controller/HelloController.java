package com.example.quickstart.controller;

import com.original.security.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开端点控制器示例。
 * <p>
 * 该控制器中的所有端点都不需要认证即可访问，
 * 用于展示框架的公开 API 能力。
 * </p>
 *
 * @author bmad
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    /**
     * 健康检查端点，无需认证。
     *
     * @return 健康状态响应
     */
    @GetMapping("/health")
    public Response<Map<String, String>> health() {
        log.info("Health check requested");
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Spring Security Boot Quick Start is running");
        return Response.successBuilder(status).build();
    }

    /**
     * 公开问候端点，无需认证。
     *
     * @return 问候信息
     */
    @GetMapping("/hello")
    public Response<Map<String, String>> hello() {
        log.info("Hello endpoint requested");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Spring Security Boot!");
        response.put("note", "This is a public endpoint that does not require authentication");
        return Response.successBuilder(response).build();
    }
}
