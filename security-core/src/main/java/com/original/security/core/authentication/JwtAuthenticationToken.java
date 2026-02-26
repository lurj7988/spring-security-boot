package com.original.security.core.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * JWT 认证令牌
 * <p>
 * 用于 JWT 认证的 AuthenticationToken 实现
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final UserDetails userDetails;

    public JwtAuthenticationToken(String token, UserDetails userDetails) {
        super(userDetails.getAuthorities());
        this.token = token;
        this.userDetails = userDetails;
        setAuthenticated(true);
    }

    public JwtAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.userDetails = null;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    public String getToken() {
        return token;
    }

    public boolean isValid() {
        return isAuthenticated() && userDetails != null;
    }
}