package com.propmanager.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Assigns a Correlation ID to every inbound HTTP request.
 *
 * Behaviour:
 *   1. If the client supplies an X-Correlation-ID header, that
 *      value is used unchanged. This allows a frontend or API
 *      gateway to propagate correlation IDs across service
 *      boundaries.
 *   2. If no X-Correlation-ID is present, a new UUID is generated
 *      for this request.
 *   3. The ID is bound to the SLF4J MDC (Mapped Diagnostic Context)
 *      under the key "correlationId". Every log statement that fires
 *      during this request's processing will automatically include
 *      this value if the logging pattern includes %X{correlationId}.
 *   4. The ID is echoed back to the client in the response header
 *      X-Correlation-ID, allowing the client to reference it when
 *      reporting issues.
 *   5. The MDC entry is always cleared in a finally block to prevent
 *      leaking into the next request processed by the same thread.
 *
 * Position: @Order(Ordered.HIGHEST_PRECEDENCE) ensures this filter
 * runs before the rate limiter, the JWT filter, and everything else —
 * so every log line, including authentication failures, carries the
 * correlation ID.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String  CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY               = "correlationId";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest  request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}