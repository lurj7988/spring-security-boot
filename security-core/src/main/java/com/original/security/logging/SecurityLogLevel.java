package com.original.security.logging;

/**
 * 安全日志级别枚举。
 * <p>
 * 定义安全日志中使用的日志级别，并映射到 SLF4J 的标准日志级别。
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public enum SecurityLogLevel {

    /**
     * 调试级别 - 记录详细的内部运行信息，通常仅在开发环境开启。
     */
    DEBUG("DEBUG"),

    /**
     * 信息级别 - 记录关键业务流程的正常执行信息。
     */
    INFO("INFO"),

    /**
     * 警告级别 - 记录可能存在风险但不影响系统继续运行的问题。
     */
    WARN("WARN"),

    /**
     * 错误级别 - 记录系统异常或安全攻击等需要关注的问题。
     */
    ERROR("ERROR");

    private final String level;

    SecurityLogLevel(String level) {
        this.level = level;
    }

    /**
     * 获取日志级别字符串。
     *
     * @return 日志级别字符串
     */
    public String getLevel() {
        return level;
    }

    /**
     * 根据字符串转换。
     *
     * @param level 字符串值
     * @return 匹配的日志级别，默认返回 INFO
     */
    public static SecurityLogLevel fromString(String level) {
        if (level == null || level.isEmpty()) {
            return INFO;
        }
        for (SecurityLogLevel logLevel : values()) {
            if (logLevel.level.equalsIgnoreCase(level)) {
                return logLevel;
            }
        }
        return INFO;
    }
}
