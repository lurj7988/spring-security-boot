package com.original.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FrameAuthenticationFailureHandler}.
 * Tests cover authentication failure response formatting for different exception types.
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class FrameAuthenticationFailureHandlerTest {

    private FrameAuthenticationFailureHandler failureHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        failureHandler = new FrameAuthenticationFailureHandler(objectMapper);
    }

    @Test
    public void testOnAuthenticationFailure_BadCredentials_ReturnsUnauthorizedResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(401, jsonNode.get("code").asInt());
        assertTrue(jsonNode.get("msg").asText().contains("用户名或密码错误"));
    }

    @Test
    public void testOnAuthenticationFailure_DisabledAccount_ReturnsDisabledMessage() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        DisabledException exception = new DisabledException("User is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(401, jsonNode.get("code").asInt());
        assertTrue(jsonNode.get("msg").asText().contains("账号已被禁用"));
    }
}
