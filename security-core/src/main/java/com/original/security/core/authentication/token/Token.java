package com.original.security.core.authentication.token;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Token 接口
 * <p>
 * 定义了 Token 的基本功能和属性
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface Token {

    /**
     * 获取 Token 值
     *
     * @return Token 字符串
     */
    String getTokenValue();

    /**
     * 获取 Token 类型
     *
     * @return Token 类型
     */
    String getTokenType();

    /**
     * 获取签发时间
     *
     * @return 签发时间
     */
    LocalDateTime getIssuedAt();

    /**
     * 获取过期时间
     *
     * @return 过期时间
     */
    LocalDateTime getExpiresAt();

    /**
     * 检查 Token 是否过期
     *
     * @return true 表示已过期，false 表示未过期
     */
    boolean isExpired();

    /**
     * 获取 Token 签发者
     *
     * @return 签发者标识
     */
    String getIssuer();

    /**
     * 获取 Token 主题
     *
     * @return 主题
     */
    String getSubject();

    /**
     * 获取受众信息
     *
     * @return 受众列表
     */
    String[] getAudience();

    /**
     * 获取自定义声明
     *
     * @return 自定义声明映射
     */
    Map<String, Object> getClaims();

    /**
     * 检查是否具有指定的声明
     *
     * @param claimName 声明名称
     * @return true 表示具有该声明
     */
    boolean hasClaim(String claimName);

    /**
     * 获取指定声明值
     *
     * @param claimName 声明名称
     * @return 声明值，如果不存在则返回 null
     */
    Object getClaim(String claimName);
}