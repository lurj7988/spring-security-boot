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
 * <h3>配置源扩展</h3>
 * <p>
 * 本接口支持多种配置源扩展实现：
 *
 * <ul>
 *   <li><b>数据库配置源</b>：实现 ConfigProvider 接口，从数据库表读取配置
 *       <pre>
 *       public class DatabaseConfigProvider implements ConfigProvider {
 *           private final DataSource dataSource;
 *           // 实现从数据库加载配置的逻辑
 *       }
 *       </pre>
 *   </li>
 *   <li><b>配置文件源</b>：实现 ConfigProvider 接口，从 properties/yaml 文件读取配置
 *       <pre>
 *       public class FileConfigProvider implements ConfigProvider {
 *           private final Properties properties;
 *           // 实现从文件加载配置的逻辑
 *       }
 *       </pre>
 *   </li>
 *   <li><b>环境变量源</b>：实现 ConfigProvider 接口，从系统环境变量读取配置
 *   </li>
 *   <li><b>远程配置源</b>：实现 ConfigProvider 接口，从远程配置中心读取配置
 *   </li>
 * </ul>
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