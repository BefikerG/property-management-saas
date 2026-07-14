package com.propmanager.core.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process rate limiter protecting the authentication endpoints
 * against brute-force credential attacks.
 *
 * Applied exclusively to:
 *   POST /api/v1/auth/login
 *   POST /api/v1/auth/refresh
 *
 * Strategy:
 *   One token bucket per client IP address.
 *   Capacity: 10 requests per minute.
 *   Refill: 10 tokens every 60 seconds (fixed window).
 *
 * When a bucket is exhausted:
 *   HTTP 429 Too Many Requests with Retry-After header.
 *   The request is rejected before reaching the authentication
 *   logic — credentials are never evaluated.
 *
 * Limitation:
 *   This implementation uses an in-process ConcurrentHashMap.
 *   In a multi-instance deployment, each instance has its own
 *   bucket map — the effective limit per IP across N instances
 *   is N × 10 requests per minute. A Redis-backed implementation
 *   (Bucket4j + Spring Data Redis) is the correct solution for
 *   horizontally scaled deployments and is a Phase 2D item.
 *
 * The bucket map grows indefinitely in the current implementation.
 * A production deployment should use a Caffeine cache with TTL
 * eviction to bound memory usage.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int    CAPACITY        = 10;
    private static final int    REFILL_TOKENS   = 10;
    private static final long   REFILL_SECONDS  = 60L;
    private static final String RETRY_AFTER_HDR = "Retry-After";
    private static final String RATE_LIMIT_HDR  = "X-RateLimit-Remaining";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest  request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        Bucket bucket   = buckets.computeIfAbsent(clientIp, this::newBucket);

        long remaining = bucket.getAvailableTokens();

        if (bucket.tryConsume(1)) {
            response.setHeader(RATE_LIMIT_HDR, String.valueOf(remaining - 1));
            filterChain.doFilter(request, response);
        } else {
            log.warn("RateLimiter: IP [{}] exceeded auth rate limit on [{}]",
                clientIp, request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(RETRY_AFTER_HDR, String.valueOf(REFILL_SECONDS));
            response.getWriter().write("""
                {
                  "errorCode": "RATE_LIMIT_EXCEEDED",
                  "message": "Too many authentication attempts. \
Please wait 60 seconds before trying again.",
                  "timestamp": "%s",
                  "validationErrors": null
                }
                """.formatted(java.time.LocalDateTime.now()));
        }
    }

    private Bucket newBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(
            CAPACITY,
            Refill.greedy(REFILL_TOKENS, Duration.ofSeconds(REFILL_SECONDS))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resolves the real client IP from the X-Forwarded-For header
     * when the application is behind a reverse proxy or load balancer.
     * Falls back to getRemoteAddr() for direct connections.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}