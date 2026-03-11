package com.original.security.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DatabaseHealthIndicator 单元测试。
 *
 * @author bmad
 * @since 0.1.0
 */
class DatabaseHealthIndicatorTest {

    private DataSource dataSource;
    private Connection connection;
    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        healthIndicator = new DatabaseHealthIndicator(dataSource);
    }

    @Test
    @DisplayName("health_数据库连接正常_返回UP状态")
    void health_ConnectionValid_ReturnsUpStatus() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("database"));
    }

    @Test
    @DisplayName("health_数据库连接失败_返回DOWN状态")
    void health_ConnectionFailed_ReturnsDownStatus() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_连接验证失败_返回DOWN状态")
    void health_ConnectionInvalid_ReturnsDownStatus() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(false);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_检查执行时间_小于50毫秒")
    void health_CheckExecutionTime_LessThan50Ms() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        // When
        long startTime = System.currentTimeMillis();
        healthIndicator.health();
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(executionTime < 50, "Database health check should complete within 50ms, took: " + executionTime + "ms");
    }

    @Test
    @DisplayName("health_数据源为null_返回DOWN状态")
    void health_DataSourceNull_ReturnsDownStatus() {
        // Given
        DatabaseHealthIndicator indicatorWithNullDataSource = new DatabaseHealthIndicator(null);

        // When
        Health health = indicatorWithNullDataSource.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }

    @Test
    @DisplayName("health_连接后正确关闭")
    void health_ConnectionProperlyClosed() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        // When
        healthIndicator.health();

        // Then
        verify(connection).close();
    }
}
