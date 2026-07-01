package com.propmanager.core.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed binding for all JWT configuration values.
 *
 * Values are read from application-{profile}.yml under the prefix
 * app.security.jwt at application startup. No @Value annotations are
 * used anywhere in the JWT infrastructure — all configuration access
 * goes through this class.
 *
 * Fields:
 *   secret           — the HMAC-SHA256 signing key, expressed as a
 *                      hex-encoded string. Must be at least 256 bits
 *                      (64 hex characters) to meet HS256 requirements.
 *   expirationMs     — access token lifetime in milliseconds.
 *   refreshExpirationMs — refresh token lifetime in milliseconds.
 *
 * Registered as a @Component so it can be constructor-injected into
 * JwtService without requiring @EnableConfigurationProperties on a
 * separate configuration class.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * Hex-encoded HMAC-SHA256 signing key.
     * Minimum length: 64 hex characters (256 bits).
     */
    private String secret;

    /**
     * Access token validity period in milliseconds.
     * Default dev value: 86400000 (24 hours).
     */
    private long expirationMs;

    /**
     * Refresh token validity period in milliseconds.
     * Default dev value: 604800000 (7 days).
     */
    private long refreshExpirationMs;
}