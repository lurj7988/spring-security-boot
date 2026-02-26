package com.original.security.core.authentication.token;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 简单的 Token 实现
 * <p>
 * 用于演示和测试的简单 Token 实现
 *
 * @author Original Security Team
 * @since 1.0.0
 */
public class SimpleToken implements Token {

    private final String tokenValue;
    private final String tokenType;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private final String issuer;
    private final String subject;
    private final String[] audience;
    private final Map<String, Object> claims;

    public SimpleToken(String tokenValue, String tokenType, LocalDateTime issuedAt,
                      LocalDateTime expiresAt, String issuer, String subject,
                      String[] audience, Map<String, Object> claims) {
        this.tokenValue = tokenValue;
        this.tokenType = tokenType;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.issuer = issuer;
        this.subject = subject;
        this.audience = audience;
        this.claims = claims;
    }

    @Override
    public String getTokenValue() {
        return tokenValue;
    }

    @Override
    public String getTokenType() {
        return tokenType;
    }

    @Override
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    @Override
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String[] getAudience() {
        return audience;
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }

    @Override
    public boolean hasClaim(String claimName) {
        return claims.containsKey(claimName);
    }

    @Override
    public Object getClaim(String claimName) {
        return claims.get(claimName);
    }
}