package com.original.security.user.evaluator;

import com.original.security.user.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityPermissionEvaluatorTest {

    @Mock
    private PermissionService permissionService;

    @Mock
    private Authentication authentication;

    private SecurityPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SecurityPermissionEvaluator(permissionService);
    }

    @Test
    void hasPermission_DomainObject_ShouldReturnTrue_WhenServiceReturnsTrue() {
        UserDetails userDetails = new User("admin", "password", Collections.emptyList());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(permissionService.hasPermission("admin", "user:read")).thenReturn(true);

        assertTrue(evaluator.hasPermission(authentication, new Object(), "user:read"));
    }

    @Test
    void hasPermission_DomainObject_ShouldReturnFalse_WhenNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);
        assertFalse(evaluator.hasPermission(authentication, new Object(), "user:read"));
    }

    @Test
    void hasPermission_DomainObject_ShouldHandleStringPrincipal() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("admin");
        when(permissionService.hasPermission("admin", "user:read")).thenReturn(true);

        assertTrue(evaluator.hasPermission(authentication, new Object(), "user:read"));
    }

    @Test
    void hasPermission_TargetId_ShouldReturnTrue_WhenServiceReturnsTrue() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("admin");
        when(permissionService.hasPermission("admin", "user:read")).thenReturn(true);

        assertTrue(evaluator.hasPermission(authentication, 1L, "User", "user:read"));
    }

    @Test
    void hasPermission_TargetId_ShouldReturnFalse_WhenPermissionIsNull() {
        when(authentication.isAuthenticated()).thenReturn(true);
        assertFalse(evaluator.hasPermission(authentication, 1L, "User", null));
    }

    @Test
    void hasPermission_DomainObject_ShouldReturnFalse_WhenAuthenticationIsNull() {
        assertFalse(evaluator.hasPermission(null, new Object(), "user:read"));
    }
}
