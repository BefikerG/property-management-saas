package com.propmanager.core.security;

import com.propmanager.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Stateless JWT authentication filter that executes exactly once per
 * request (guaranteed by OncePerRequestFilter).
 *
 * Execution sequence (TRD §3.1 — Token Context Harvesting):
 *
 *   1. Extract the Bearer token from the Authorization header.
 *      If absent or malformed, skip authentication and continue the
 *      filter chain — Spring Security will handle the 401 response
 *      for any protected endpoint downstream.
 *
 *   2. Validate the token's structure and signature via JwtService.
 *      If invalid (expired, malformed, wrong signature), skip and
 *      continue — same handling as absent token.
 *
 *   3. Extract the subject (email) and org_id claim from the token.
 *
 *   4. Load UserDetails for the subject email via UserDetailsService.
 *
 *   5. Validate the token against the loaded UserDetails.
 *
 *   6. Populate the Spring Security SecurityContextHolder with an
 *      authenticated UsernamePasswordAuthenticationToken — this is
 *      what makes @PreAuthorize role checks work downstream.
 *
 *   7. *** Call TenantContext.setCurrentTenantId(orgId) ***
 *      This is the critical handoff between the security layer and the
 *      tenant isolation layer. The org_id extracted from the JWT claim
 *      is bound to the current request thread, where TenantFilterAspect
 *      will read it before every repository call.
 *
 *   8. Continue the filter chain — the request proceeds to its controller.
 *
 *   9. In a finally block after the chain completes:
 *      TenantContext.clear() removes the org_id binding from the thread,
 *      preventing it from leaking into the next request reusing this
 *      Tomcat worker thread from the pool.
 *
 * Why UserDetailsService is injected here:
 *   We need to verify the JWT subject (email) corresponds to a real,
 *   active staff member in the database before trusting the token's
 *   claims. A valid signature alone is insufficient — the staff member
 *   account may have been deactivated or suspended since the token was
 *   issued.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";

    private final JwtService         jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest  request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        final String token = extractBearerToken(request);

        // No token present — skip authentication, continue chain.
        // Spring Security will enforce 401 on protected endpoints.
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token is present but structurally invalid — skip authentication.
        if (!jwtService.isTokenStructurallyValid(token)) {
            log.debug("JwtAuthenticationFilter: invalid token on request to [{}]",
                request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        final String username = jwtService.extractUsername(token);
        final UUID   orgId    = jwtService.extractOrgId(token);

        // Only authenticate if no authentication is already present in the
        // SecurityContext (prevents double-processing on forward dispatches).
        if (username != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {

                // ── Step 6: Populate SecurityContextHolder ────────────
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // ── Step 7: Bind tenant context ───────────────────────
                if (orgId != null) {
                    TenantContext.setCurrentTenantId(orgId);
                    log.debug("JwtAuthenticationFilter: authenticated [{}] " +
                              "for org [{}] on [{}]",
                        username, orgId, request.getRequestURI());
                } else {
                    // Token valid but no org_id claim — this occurs for
                    // platform-level admin tokens that are not scoped to
                    // any single organization. TenantFilterAspect handles
                    // the null case by skipping filter activation.
                    log.debug("JwtAuthenticationFilter: authenticated [{}] " +
                              "with no org_id claim (platform-level token).",
                        username);
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // ── Step 9: Always clear tenant context after request ─────
            // Critical: prevents thread-pool reuse from leaking one
            // request's org_id into an unrelated subsequent request.
            TenantContext.clear();
        }
    }

    /**
     * Extracts the raw JWT string from the Authorization header.
     *
     * @param request the incoming HTTP request
     * @return the token string without the "Bearer " prefix,
     *         or {@code null} if the header is absent or malformed
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}