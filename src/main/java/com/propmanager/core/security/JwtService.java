package com.propmanager.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Central JWT utility service responsible for:
 *
 *   1. Generating signed access and refresh tokens carrying the
 *      authenticated user's identity and org_id claim.
 *   2. Extracting typed claims (subject, org_id, expiry) from a
 *      token string without throwing on expected validation failures.
 *   3. Validating a token's signature, expiry, and structural integrity.
 *
 * Token structure (payload claims):
 *   sub      — the staff member's email address (Spring Security username)
 *   org_id   — the authenticated organization's UUID (tenant boundary).
 *              This value is what TenantContext and TenantFilterAspect
 *              use to scope every subsequent database query.
 *   iat      — issued-at timestamp (Unix epoch seconds)
 *   exp      — expiry timestamp (Unix epoch seconds)
 *
 * Algorithm: HMAC-SHA512 (HS512) with a minimum 512-bit secret key.
 *
 * Why HMAC-SHA256 over RSA:
 *   For a single-application monolith where the same service both issues
 *   and validates tokens, a symmetric HMAC algorithm is simpler to operate
 *   and equally secure. RSA asymmetric signing is only necessary when
 *   tokens need to be validated by an external party that should not hold
 *   the signing key — which is not the case in this architecture.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    /**
     * Custom claim key for the tenant organization identifier.
     * This constant is shared between token generation and extraction
     * to prevent typo-driven claim name mismatches.
     */
    public static final String CLAIM_ORG_ID = "org_id";

    private final JwtProperties jwtProperties;

    // ── Token Generation ──────────────────────────────────────────────

    /**
     * Generates a signed access token for the given user and organization.
     *
     * @param userDetails the authenticated Spring Security principal
     *                    (email as username)
     * @param orgId       the UUID of the organization this staff member
     *                    belongs to — embedded as the org_id claim
     * @return a compact, signed JWT string
     */
    public String generateAccessToken(UserDetails userDetails, UUID orgId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(CLAIM_ORG_ID, orgId.toString());
        return buildToken(extraClaims, userDetails, jwtProperties.getExpirationMs());
    }

    /**
     * Generates a signed refresh token for the given user.
     * Refresh tokens carry only the subject claim (email) — they do not
     * carry the org_id, since their sole purpose is to obtain a new
     * access token and they never directly authorize data access.
     *
     * @param userDetails the authenticated Spring Security principal
     * @return a compact, signed refresh JWT string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshExpirationMs());
    }

    private String buildToken(
        Map<String, Object> extraClaims,
        UserDetails         userDetails,
        long                expirationMs
    ) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationMs))
            .signWith(getSigningKey())
            .compact();
    }

    // ── Claim Extraction ──────────────────────────────────────────────

    /**
     * Extracts the subject claim (staff member email) from a token.
     *
     * @param token a compact JWT string
     * @return the subject string embedded in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the org_id claim from a token and converts it to a UUID.
     *
     * @param token a compact JWT string
     * @return the organization UUID embedded in the token's org_id claim,
     *         or {@code null} if the claim is absent (e.g. a refresh token)
     */
    public UUID extractOrgId(String token) {
        String orgIdString = extractClaim(token,
            claims -> claims.get(CLAIM_ORG_ID, String.class));

        if (orgIdString == null) {
            return null;
        }

        try {
            return UUID.fromString(orgIdString);
        } catch (IllegalArgumentException ex) {
            log.warn("JWT contained malformed org_id claim: '{}'", orgIdString);
            return null;
        }
    }

    /**
     * Extracts the expiry date from a token.
     *
     * @param token a compact JWT string
     * @return the expiry Date embedded in the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor accepting a resolver function.
     *
     * @param token          a compact JWT string
     * @param claimsResolver a function mapping Claims to the desired value
     * @param <T>            the type of the extracted claim value
     * @return the resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ── Validation ────────────────────────────────────────────────────

    /**
     * Validates the token's signature, expiry, and subject against the
     * given Spring Security UserDetails.
     *
     * @param token       a compact JWT string
     * @param userDetails the UserDetails loaded for the token's subject
     * @return {@code true} if the token is valid and matches the principal;
     *         {@code false} otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Performs structural validation of the token — signature, format,
     * and expiry — without requiring a UserDetails lookup.
     * Used by JwtAuthenticationFilter for the initial token parse.
     *
     * @param token a compact JWT string
     * @return {@code true} if the token parses and validates successfully
     */
    public boolean isTokenStructurallyValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT has expired: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT is malformed: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT algorithm is unsupported: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("JWT signature validation failed: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty or null: {}", ex.getMessage());
        }
        return false;
    }

    // ── Internal Helpers ──────────────────────────────────────────────

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Derives the HMAC-SHA512 SecretKey from the configured hex-encoded
     * secret string. Called on every token parse/generation to avoid
     * holding a mutable key reference as a field.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret()
            .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}