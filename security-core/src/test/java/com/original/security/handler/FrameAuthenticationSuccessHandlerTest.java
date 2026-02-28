package com.original.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FrameAuthenticationSuccessHandler}.
 * Tests cover successful authentication response formatting and JSON output.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class FrameAuthenticationSuccessHandlerTest {

    private FrameAuthenticationSuccessHandler successHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        successHandler = new FrameAuthenticationSuccessHandler(objectMapper);
    }

    @Test
    public void testOnAuthenticationSuccess_ValidAuthentication_ReturnsSuccessResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pass", null);

        successHandler.onAuthenticationSuccess(request, response, auth);

        assertEquals(200, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(200, jsonNode.get("code").asInt());
        assertTrue(jsonNode.has("body"));
        assertEquals("admin", jsonNode.get("body").asText());
    }
}
