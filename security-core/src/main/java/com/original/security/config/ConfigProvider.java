package com.original.security.config;

import java.util.Map;
import java.util.Optional;

import org.springframework.lang.Nullable;

/**
 * 配置提供者接口
 * <p>
 * ConfigProvider 提供统一的配置访问抽象，
 * 支持从多种配置源（数据库、配置文件等）加载配置
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public interface ConfigProvider {

    /**
     * 获取指定键的配置值
     *
     * @param key 配置键
     * @param <T> 配置值类型
     * @return 包含配置值的 Optional，如果不存在则为空
     */
    <T> Optional<T> getConfig(String key);

    /**
     * 获取指定键的配置值，如果不存在则返回默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @param <T> 配置值类型
     * @return 配置值或默认值
     */
    <T> T getConfig(String key, T defaultValue);

    /**
     * 获取指定前缀的所有配置属性
     *
     * @param prefix 配置前缀
     * @return 包含配置属性的 Map，键为配置键，值为配置值
     */
    Map<String, Object> getProperties(String prefix);

    /**
     * 获取所有配置属性
     *
     * @return 包含所有配置属性的 Map
     */
    Map<String, Object> getAllProperties();

    /**
     * 检查是否存在指定的配置键
     *
     * @param key 配置键
     * @return true 表示存在该配置键
     */
    boolean hasConfig(String key);

    /**
     * 检查配置值是否满足指定条件
     *
     * @param key 配置键
     * @param condition 条件检查函数
     * @param <T> 配置值类型
     * @return true 表示配置值存在且满足条件
     */
    <T> boolean checkConfig(String key, java.util.function.Predicate<T> condition);

    /**
     * 获取配置值的原始字符串形式
     *
     * @param key 配置键
     * @return 配置值的字符串形式，如果不存在则为空字符串
     */
    String getString(String key);

    /**
     * 获取配置值并转换为指定类型
     *
     * @param key 配置键
     * @param type 要转换的类型
     * @return 转换后的配置值，如果转换失败或不存在则为 null
     */
    @Nullable
    <T> T getConfigAs(String key, Class<T> type);

    /**
     * 获取配置值的原始字符串形式，如果不存在则为 null
     *
     * @param key 配置键
     * @return 配置值的字符串形式，如果不存在则为 null
     */
    @Nullable
    String getStringNullable(String key);

    /**
     * 刷新配置缓存
     * 当配置源发生变化时，调用此方法重新加载配置
     */
    void refresh();

    /**
     * 获取配置源的信息
     *
     * @return 配置源描述信息
     */
    String getSourceInfo();
}