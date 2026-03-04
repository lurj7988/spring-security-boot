package com.original.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.session.SessionInformationExpiredEvent;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionExpiredHandler 单元测试。
 * <p>
 * 使用 Spring Mock 对象而非 Mockito 来测试 final 类。
 * </p>
 *
 * @author Original Security Team
 * @since 1.0.0
 */
class SessionExpiredHandlerTest {

    private SessionExpiredHandler handler;
    private ObjectMapper objectMapper;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        handler = new SessionExpiredHandler(objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        request.setRequestURI("/api/protected/resource");
    }

    private SessionInformationExpiredEvent createEvent() {
        // SessionInformation 需要 non-null 的 lastRequest 参数
        SessionInformation sessionInformation = new SessionInformation(
                "testUser", "testSessionId123", new Date());
        return new SessionInformationExpiredEvent(sessionInformation, request, response);
    }

    @Test
    @DisplayName("onExpiredSessionDetected_SetsCorrectResponseStatus")
    void testOnExpiredSessionDetected_SetsCorrectResponseStatus() throws Exception {
        // Given
        SessionInformationExpiredEvent event = createEvent();

        // When
        handler.onExpiredSessionDetected(event);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    @DisplayName("onExpiredSessionDetected_SetsCorrectContentType")
    void testOnExpiredSessionDetected_SetsCorrectContentType() throws Exception {
        // Given
        SessionInformationExpiredEvent event = createEvent();

        // When
        handler.onExpiredSessionDetected(event);

        // Then
        assertTrue(response.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE),
                "Content-Type should start with application/json");
        assertTrue(response.getContentType().contains("charset=UTF-8"),
                "Content-Type should contain charset=UTF-8");
    }

    @Test
    @DisplayName("onExpiredSessionDetected_ContainsErrorMessage")
    void testOnExpiredSessionDetected_ContainsErrorMessage() throws Exception {
        // Given
        SessionInformationExpiredEvent event = createEvent();

        // When
        handler.onExpiredSessionDetected(event);

        // Then
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("会话已过期，请重新登录"));
    }

    @Test
    @DisplayName("onExpiredSessionDetected_ContainsCorrectStatusCode")
    void testOnExpiredSessionDetected_ContainsCorrectStatusCode() throws Exception {
        // Given
        SessionInformationExpiredEvent event = createEvent();

        // When
        handler.onExpiredSessionDetected(event);

        // Then
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains(String.valueOf(HttpStatus.UNAUTHORIZED.value())));
    }

    @Test
    @DisplayName("onExpiredSessionDetected_ContainsRequestUri")
    void testOnExpiredSessionDetected_ContainsRequestUri() throws Exception {
        // Given
        SessionInformationExpiredEvent event = createEvent();

        // When
        handler.onExpiredSessionDetected(event);

        // Then
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("/api/protected/resource"));
    }

    @Test
    @DisplayName("onExpiredSessionDetected_ReturnsValidJson")
    void testOnExpiredSessionDetected_ReturnsValidJson() throws Exception {
        // Given
        SessionInformationExpiredEvent event = createEvent();

        // When
        handler.onExpiredSessionDetected(event);

        // Then
        String responseBody = response.getContentAsString();
        // 验证返回的是有效 JSON
        assertDoesNotThrow(() -> objectMapper.readTree(responseBody));
    }
}
