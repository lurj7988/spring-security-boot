package com.original.security.config.impl;

import com.original.security.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认配置提供者实现
 * <p>
 * 基于 Map 的配置提供者，支持内存中的配置管理
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class DefaultConfigProvider implements ConfigProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigProvider.class);

    private final Map<String, Object> configMap;
    private final String sourceInfo;

    /**
     * 使用默认的空配置创建配置提供者
     */
    public DefaultConfigProvider() {
        this(new ConcurrentHashMap<>(), "DefaultConfigProvider");
    }

    /**
     * 使用指定的配置映射创建配置提供者
     *
     * @param configMap 配置映射
     */
    public DefaultConfigProvider(Map<String, Object> configMap) {
        this(configMap, "DefaultConfigProvider");
    }

    /**
     * 使用指定的配置映射和源信息创建配置提供者
     *
     * @param configMap 配置映射
     * @param sourceInfo 源信息
     */
    public DefaultConfigProvider(Map<String, Object> configMap, String sourceInfo) {
        if (configMap == null) {
            throw new IllegalArgumentException("Config map cannot be null");
        }
        if (sourceInfo == null) {
            throw new IllegalArgumentException("Source info cannot be null");
        }

        this.configMap = new ConcurrentHashMap<>(configMap);
        this.sourceInfo = sourceInfo;
    }

    @Override
    public <T> Optional<T> getConfig(String key) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }

        Object value = configMap.get(key);
        return value != null ? Optional.ofNullable(castSafely(value)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> T castSafely(Object value) {
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> T getConfig(String key, T defaultValue) {
        Optional<T> optional = getConfig(key);
        return optional.orElse(defaultValue);
    }

    @Override
    public Map<String, Object> getProperties(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.unmodifiableMap(new ConcurrentHashMap<>(configMap));
        }

        Map<String, Object> result = new ConcurrentHashMap<>();
        configMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .forEach(entry -> {
                    String keyWithoutPrefix = entry.getKey().substring(prefix.length());
                    result.put(keyWithoutPrefix, entry.getValue());
                });

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, Object> getAllProperties() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(configMap));
    }

    @Override
    public boolean hasConfig(String key) {
        return key != null && !key.trim().isEmpty() && configMap.containsKey(key);
    }

    @Override
    public <T> boolean checkConfig(String key, java.util.function.Predicate<T> condition) {
        if (key == null || condition == null) {
            return false;
        }

        Optional<T> configOpt = getConfig(key);
        if (!configOpt.isPresent()) {
            return false;
        }

        try {
            T value = configOpt.get();
            return condition.test(value);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public String getString(String key) {
        Object value = getConfig(key).orElse("");
        return value != null ? value.toString() : "";
    }

    @Override
    @Nullable
    public <T> T getConfigAs(String key, Class<T> type) {
        if (key == null || type == null) {
            return null;
        }

        try {
            Object value = configMap.get(key);
            if (value != null) {
                // 检查是否是基本类型的包装类
                if (type.isPrimitive()) {
                    Class<?> wrapperType = getWrapperClass(type);
                    if (wrapperType != null && wrapperType.isInstance(value)) {
                        return type.cast(castPrimitiveValue(value, type));
                    }
                } else if (type.isInstance(value)) {
                    return type.cast(value);
                } else if (value instanceof String) {
                    // 尝试从字符串转换
                    return convertFromString((String) value, type);
                }
            }
            return null;
        } catch (ClassCastException | InstantiationException | IllegalAccessException e) {
            logger.warn("Failed to cast config value for key '{}': {}", key, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertFromString(String value, Class<T> type) {
        if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class || type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Double.class || type == double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Float.class || type == float.class) {
            return (T) Float.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == String.class) {
            return (T) value;
        }
        return null;
    }

    private Class<?> getWrapperClass(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T castPrimitiveValue(Object value, Class<T> targetType)
            throws InstantiationException, IllegalAccessException {
        if (targetType == int.class) return (T) Integer.valueOf(((Number) value).intValue());
        if (targetType == long.class) return (T) Long.valueOf(((Number) value).longValue());
        if (targetType == double.class) return (T) Double.valueOf(((Number) value).doubleValue());
        if (targetType == float.class) return (T) Float.valueOf(((Number) value).floatValue());
        if (targetType == boolean.class) return (T) Boolean.valueOf((Boolean) value);
        if (targetType == char.class) return (T) Character.valueOf((Character) value);
        if (targetType == byte.class) return (T) Byte.valueOf(((Number) value).byteValue());
        if (targetType == short.class) return (T) Short.valueOf(((Number) value).shortValue());
        return null;
    }

    @Override
    public void refresh() {
        logger.debug("Refreshing configuration from source: {}", sourceInfo);
        // 在 DefaultConfigProvider 中，refresh 方法不需要执行任何操作
        // 因为配置在内存中管理，外部需要手动更新配置映射
        logger.debug("Configuration refresh completed. Current configuration count: {}", configMap.size());
    }

    @Override
    public String getSourceInfo() {
        return sourceInfo;
    }

    /**
     * 添加配置项
     *
     * @param key 配置键
     * @param value 配置值
     */
    @Override
    @Nullable
    public String getStringNullable(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        Object value = configMap.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 添加配置项
     *
     * @param key 配置键
     * @param value 配置值
     */
    public void addConfig(String key, Object value) {
        if (key != null && !key.trim().isEmpty()) {
            configMap.put(key, value);
        }
    }

    /**
     * 移除配置项
     *
     * @param key 配置键
     */
    public void removeConfig(String key) {
        if (key != null) {
            configMap.remove(key);
        }
    }

    /**
     * 清空所有配置
     */
    public void clear() {
        configMap.clear();
    }

    /**
     * 获取配置项数量
     *
     * @return 配置项数量
     */
    public int size() {
        return configMap.size();
    }
}