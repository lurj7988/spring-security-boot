package com.original.security.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * ConfigProvider 单元测试
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
public class ConfigProviderTest {

    private ConfigProvider configProvider;

    @Before
    public void setUp() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("app.name", "Spring Security Boot");
        configMap.put("app.version", "1.0.0");
        configMap.put("security.jwt.enabled", true);
        configMap.put("security.jwt.expiration", 3600);

        configProvider = new DefaultConfigProvider(configMap);
    }

    @Test
    public void testGetConfig() {
        String appName = configProvider.getConfig("app.name").orElse(null);
        assertEquals("Spring Security Boot", appName);

        String nonExistentKey = configProvider.getConfig("non.existent").orElse(null);
        assertNull(nonExistentKey);
    }

    @Test
    public void testGetConfigWithDefault() {
        String version = configProvider.getConfig("app.version", "1.0.0");
        assertEquals("1.0.0", version);

        String nonExistentKey = configProvider.getConfig("non.existent", "default-value");
        assertEquals("default-value", nonExistentKey);
    }

    @Test
    public void testGetProperties() {
        Map<String, Object> appProperties = configProvider.getProperties("app.");
        assertEquals(2, appProperties.size());
        assertEquals("Spring Security Boot", appProperties.get("app.name"));
        assertEquals("1.0.0", appProperties.get("app.version"));
    }

    @Test
    public void testGetAllProperties() {
        Map<String, Object> allProperties = configProvider.getAllProperties();
        assertEquals(4, allProperties.size());
        assertTrue(allProperties.containsKey("app.name"));
        assertTrue(allProperties.containsKey("app.version"));
        assertTrue(allProperties.containsKey("security.jwt.enabled"));
        assertTrue(allProperties.containsKey("security.jwt.expiration"));
    }

    @Test
    public void testHasConfig() {
        assertTrue(configProvider.hasConfig("app.name"));
        assertFalse(configProvider.hasConfig("non.existent"));
    }

    @Test
    public void testCheckConfig() {
        assertTrue(configProvider.checkConfig("app.name", name -> "Spring Security Boot".equals(name)));
        assertFalse(configProvider.checkConfig("app.name", name -> "Other App".equals(name)));
        assertFalse(configProvider.checkConfig("non.existent", value -> true));
    }

    @Test
    public void testGetString() {
        assertEquals("Spring Security Boot", configProvider.getString("app.name"));
        assertEquals("", configProvider.getString("non.existent"));
    }

    @Test
    public void testGetConfigAs() {
        Boolean enabled = configProvider.getConfigAs("security.jwt.enabled", Boolean.class);
        assertTrue(enabled);

        Integer expiration = configProvider.getConfigAs("security.jwt.expiration", Integer.class);
        assertEquals(3600, expiration.intValue());
    }

    @Test
    public void testSourceInfo() {
        assertNotNull(configProvider.getSourceInfo());
        assertTrue(configProvider.getSourceInfo().contains("DefaultConfigProvider"));
    }

    @Test
    public void testDefaultConstructor() {
        ConfigProvider defaultConfig = new DefaultConfigProvider();
        assertNotNull(defaultConfig);
        assertEquals(0, defaultConfig.getAllProperties().size());
    }
}