package com.original.security.config;

import com.original.security.config.impl.DefaultConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigProvider 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
class ConfigProviderTest {

    private DefaultConfigProvider configProvider;

    @BeforeEach
    void setUp() {
        configProvider = new DefaultConfigProvider();
    }

    @Test
    void testGetConfig() {
        configProvider.addConfig("test.key", "test.value");

        Optional<String> value = configProvider.getConfig("test.key");
        assertTrue(value.isPresent());
        assertEquals("test.value", value.get());
    }

    @Test
    void testGetConfigWithDefault() {
        configProvider.addConfig("test.key", "test.value");

        String value = configProvider.getConfig("test.key", "default");
        assertEquals("test.value", value);
    }

    @Test
    void testGetConfigNonExistent() {
        Optional<String> value = configProvider.getConfig("non.existent");
        assertFalse(value.isPresent());
    }

    @Test
    void testGetConfigWithDefaultForNonExistent() {
        String value = configProvider.getConfig("non.existent", "default");
        assertEquals("default", value);
    }

    @Test
    void testGetProperties() {
        configProvider.addConfig("app.name", "Spring Security Boot");
        configProvider.addConfig("app.version", "1.0.0");
        configProvider.addConfig("db.url", "jdbc:mysql://localhost:3306/mydb");

        Map<String, Object> appProps = configProvider.getProperties("app.");
        assertEquals(2, appProps.size());
        assertEquals("Spring Security Boot", appProps.get("name"));
        assertEquals("1.0.0", appProps.get("version"));
    }

    @Test
    void testGetAllProperties() {
        configProvider.addConfig("key1", "value1");
        configProvider.addConfig("key2", "value2");

        Map<String, Object> allProps = configProvider.getAllProperties();
        assertEquals(2, allProps.size());
        assertEquals("value1", allProps.get("key1"));
        assertEquals("value2", allProps.get("key2"));
    }

    @Test
    void testHasConfig() {
        assertFalse(configProvider.hasConfig("non.existent"));

        configProvider.addConfig("existing.key", "value");
        assertTrue(configProvider.hasConfig("existing.key"));
    }

    @Test
    void testCheckConfig() {
        configProvider.addConfig("env", "development");

        boolean isDev = configProvider.checkConfig("env", env -> "development".equals(env));
        assertTrue(isDev);

        boolean isProd = configProvider.checkConfig("env", env -> "production".equals(env));
        assertFalse(isProd);
    }

    @Test
    void testGetString() {
        configProvider.addConfig("string.key", "hello world");

        String value = configProvider.getString("string.key");
        assertEquals("hello world", value);
    }

    @Test
    void testGetStringNullable() {
        configProvider.addConfig("string.key", "not null");

        String value = configProvider.getStringNullable("string.key");
        assertEquals("not null", value);

        String nullValue = configProvider.getStringNullable("non.existent");
        assertNull(nullValue);
    }

    @Test
    void testGetConfigAs() {
        configProvider.addConfig("number.key", "123");

        Integer value = configProvider.getConfigAs("number.key", Integer.class);
        assertNotNull(value);
        assertEquals(123, value.intValue());
    }

    @Test
    void testRefresh() {
        configProvider.addConfig("initial.key", "initial");

        // Modify the underlying config map directly for testing
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("new.key", "new value");

        // Create a new provider with the test map
        DefaultConfigProvider newProvider = new DefaultConfigProvider(testMap, "test");

        newProvider.refresh(); // Should not throw any exceptions
        assertEquals("new value", newProvider.getString("new.key"));
    }

    @Test
    void testSourceInfo() {
        DefaultConfigProvider provider = new DefaultConfigProvider();
        assertNotNull(provider.getSourceInfo());
        assertTrue(provider.getSourceInfo().contains("DefaultConfigProvider"));
    }

    @Test
    void testConstructorWithCustomMap() {
        Map<String, Object> initialMap = new HashMap<>();
        initialMap.put("custom.key", "custom.value");

        DefaultConfigProvider provider = new DefaultConfigProvider(initialMap, "custom source");
        assertEquals("custom.value", provider.getString("custom.key"));
        assertEquals("custom source", provider.getSourceInfo());
    }

    @Test
    void testAddConfigNullKey() {
        assertDoesNotThrow(() -> {
            configProvider.addConfig(null, "value");
        });
    }

    @Test
    void testAddConfigEmptyKey() {
        assertDoesNotThrow(() -> {
            configProvider.addConfig("", "value");
        });
    }

    @Test
    void testAddConfigValidKey() {
        assertDoesNotThrow(() -> {
            configProvider.addConfig("valid.key", "value");
        });

        String value = configProvider.getString("valid.key");
        assertEquals("value", value);
    }
}