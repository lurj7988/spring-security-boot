package com.original.security.controller;

import com.original.security.core.Response;
import com.original.security.dto.PageResult;
import com.original.security.dto.SessionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionControllerTest {

    private SessionController sessionController;
    private SessionRegistry sessionRegistry;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        sessionRegistry = mock(SessionRegistry.class);
        //noinspection unchecked
        ObjectProvider<SessionRegistry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sessionRegistry);

        sessionController = new SessionController(provider);
    }

    @Test
    void testGetAllSessions_Pagination() {
        // Mock data
        UserDetails user1 = User.withUsername("admin").password("").authorities("ADMIN").build();
        UserDetails user2 = User.withUsername("user").password("").authorities("USER").build();

        Date now = new Date();
        SessionInformation session1 = new SessionInformation(user1, "session-1", new Date(now.getTime() - 1000));
        SessionInformation session2 = new SessionInformation(user2, "session-2", new Date(now.getTime() - 5000));
        SessionInformation session3 = new SessionInformation(user1, "session-3", new Date(now.getTime() - 2000));

        when(sessionRegistry.getAllPrincipals()).thenReturn(Arrays.asList(user1, user2));
        when(sessionRegistry.getAllSessions(eq(user1), anyBoolean())).thenReturn(Arrays.asList(session1, session3));
        when(sessionRegistry.getAllSessions(eq(user2), anyBoolean())).thenReturn(Collections.singletonList(session2));

        // Test page 1, size 2
        Response<PageResult<SessionInfo>> response = sessionController.getAllSessions(1, 2);
        
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(1, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(3, result.getTotal());
        assertEquals(2, result.getList().size());

        // verify sorting (descending by last active time)
        assertEquals("session-1", result.getList().get(0).getSessionId());
        assertEquals("session-3", result.getList().get(1).getSessionId());

        // Test page 2, size 2
        Response<PageResult<SessionInfo>> responsePage2 = sessionController.getAllSessions(2, 2);
        PageResult<SessionInfo> resultPage2 = responsePage2.getBody();
        assertEquals(1, resultPage2.getList().size());
        assertEquals("session-2", resultPage2.getList().get(0).getSessionId());
    }

    @Test
    void testGetMySessions() {
        UserDetails user = User.withUsername("me").password("").authorities("USER").build();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        Date now = new Date();
        SessionInformation session1 = new SessionInformation(user, "my-session-1", now);
        when(sessionRegistry.getAllSessions(eq(user), anyBoolean())).thenReturn(Collections.singletonList(session1));

        Response<List<SessionInfo>> response = sessionController.getMySessions();
        assertEquals(200, response.getCode());
        List<SessionInfo> result = response.getBody();
        assertEquals(1, result.size());
        assertEquals("my-session-1", result.get(0).getSessionId());
        assertEquals("me", result.get(0).getUsername());

        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAllSessions_WithInvalidPage_Zero_CorrectedToOne() {
        UserDetails user = User.withUsername("test").password("").authorities("USER").build();
        when(sessionRegistry.getAllPrincipals()).thenReturn(Collections.singletonList(user));
        when(sessionRegistry.getAllSessions(eq(user), anyBoolean())).thenReturn(new ArrayList<>());

        Response<PageResult<SessionInfo>> response = sessionController.getAllSessions(0, 10);
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(1, result.getPage()); // Corrected to 1
        assertEquals(10, result.getSize());
    }

    @Test
    void testGetAllSessions_WithInvalidPage_Negative_CorrectedToOne() {
        UserDetails user = User.withUsername("test").password("").authorities("USER").build();
        when(sessionRegistry.getAllPrincipals()).thenReturn(Collections.singletonList(user));
        when(sessionRegistry.getAllSessions(eq(user), anyBoolean())).thenReturn(new ArrayList<>());

        Response<PageResult<SessionInfo>> response = sessionController.getAllSessions(-1, 10);
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(1, result.getPage()); // Corrected to 1
        assertEquals(10, result.getSize());
    }

    @Test
    void testGetAllSessions_WithInvalidSize_Zero_CorrectedToTen() {
        UserDetails user = User.withUsername("test").password("").authorities("USER").build();
        when(sessionRegistry.getAllPrincipals()).thenReturn(Collections.singletonList(user));
        when(sessionRegistry.getAllSessions(eq(user), anyBoolean())).thenReturn(new ArrayList<>());

        Response<PageResult<SessionInfo>> response = sessionController.getAllSessions(1, 0);
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize()); // Corrected to 10
    }

    @Test
    void testGetAllSessions_WithInvalidSize_ExceedsMaximum_CorrectedToThousand() {
        UserDetails user = User.withUsername("test").password("").authorities("USER").build();
        when(sessionRegistry.getAllPrincipals()).thenReturn(Collections.singletonList(user));
        when(sessionRegistry.getAllSessions(eq(user), anyBoolean())).thenReturn(new ArrayList<>());

        Response<PageResult<SessionInfo>> response = sessionController.getAllSessions(1, 2000);
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(1, result.getPage());
        assertEquals(1000, result.getSize()); // Corrected to 1000
    }

    @Test
    void testGetAllSessions_WithPageBeyondData_ReturnsEmptyList() {
        UserDetails user = User.withUsername("test").password("").authorities("USER").build();
        when(sessionRegistry.getAllPrincipals()).thenReturn(Collections.singletonList(user));
        when(sessionRegistry.getAllSessions(eq(user), anyBoolean())).thenReturn(new ArrayList<>());

        Response<PageResult<SessionInfo>> response = sessionController.getAllSessions(999, 10);
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(999, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void testGetAllSessions_WithoutSessionRegistry_ReturnsEmptyResult() {
        //noinspection unchecked
        ObjectProvider<SessionRegistry> emptyProvider = mock(ObjectProvider.class);
        when(emptyProvider.getIfAvailable()).thenReturn(null);

        SessionController controllerWithoutRegistry = new SessionController(emptyProvider);

        Response<PageResult<SessionInfo>> response = controllerWithoutRegistry.getAllSessions(1, 10);
        assertEquals(200, response.getCode());
        PageResult<SessionInfo> result = response.getBody();
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void testGetMySessions_WithoutSessionRegistry_ReturnsEmptyList() {
        //noinspection unchecked
        ObjectProvider<SessionRegistry> emptyProvider = mock(ObjectProvider.class);
        when(emptyProvider.getIfAvailable()).thenReturn(null);

        SessionController controllerWithoutRegistry = new SessionController(emptyProvider);

        Response<List<SessionInfo>> response = controllerWithoutRegistry.getMySessions();
        assertEquals(200, response.getCode());
        List<SessionInfo> result = response.getBody();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMySessions_WithoutAuthentication_ReturnsEmptyList() {
        SecurityContextHolder.clearContext();

        Response<List<SessionInfo>> response = sessionController.getMySessions();
        assertEquals(200, response.getCode());
        List<SessionInfo> result = response.getBody();
        assertTrue(result.isEmpty());
    }
}
