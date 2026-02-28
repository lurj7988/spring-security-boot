package com.original.security.plugin.jwt;

import com.original.security.core.authentication.AuthenticationProvider;
import com.original.security.plugin.AuthenticationPlugin;
import org.springframework.stereotype.Component;

/**
 * JWT Authentication Plugin.
 *
 * @author bmad
 * @since 0.1.0
 */
@Component
public class JwtAuthenticationPlugin implements AuthenticationPlugin {

    public static final String PLUGIN_NAME = "jwt";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        // JWT uses a OncePerRequestFilter for authentication, so no standard Provider is needed here.
        return null;
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return false;
    }
}
