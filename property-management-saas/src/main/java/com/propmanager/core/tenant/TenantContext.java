package com.propmanager.core.tenant;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Request-scoped ThreadLocal container for the authenticated tenant's
 * organization identifier.
 *
 * Lifecycle (TRD §3.1):
 *   1. The JwtAuthenticationFilter (core/security/ — Step 3) extracts the
 *      org_id claim from the validated Bearer token and calls
 *      TenantContext.setCurrentTenantId(orgId) before the request reaches
 *      any controller or service.
 *   2. The TenantFilterAspect reads TenantContext.getCurrentTenantId() and
 *      binds it to the Hibernate session filter before any repository query
 *      executes.
 *   3. The JwtAuthenticationFilter calls TenantContext.clear() in a
 *      finally block after the request completes, preventing the tenant
 *      identity from leaking into a subsequent request reusing the same
 *      Tomcat worker thread from the pool.
 *
 * Why ThreadLocal rather than passing org_id as a method parameter:
 *   The ThreadLocal approach makes the tenant boundary transparently
 *   available to any code on the request-handling thread — including the
 *   AOP interceptor that operates outside any explicit call chain — without
 *   forcing every service and repository method signature to carry an
 *   additional orgId parameter. It also ensures that the Organization
 *   entity itself (which has no tenant_id and never reads TenantContext)
 *   is never accidentally filtered.
 *
 * Thread safety:
 *   ThreadLocal is inherently per-thread. In Spring Boot's embedded Tomcat
 *   model, each request is handled by exactly one worker thread for its
 *   full duration. The mandatory clear() call on request completion ensures
 *   thread-pool reuse never carries stale tenant state between requests.
 */
@Slf4j
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    // Utility class — no instantiation permitted.
    private TenantContext() {}

    /**
     * Binds the given organization UUID to the current request thread.
     * Called exclusively by JwtAuthenticationFilter immediately after
     * successful token validation.
     *
     * @param tenantId the authenticated organization's UUID extracted
     *                 from the JWT org_id claim. Must not be null.
     */
    public static void setCurrentTenantId(UUID tenantId) {
        log.debug("TenantContext: binding tenant ID [{}] to thread [{}]",
            tenantId, Thread.currentThread().getName());
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Returns the tenant UUID bound to the current request thread.
     *
     * @return the authenticated organization's UUID, or {@code null} if
     *         called outside a request context (e.g. from a scheduled job
     *         or a batch step — contexts that intentionally have no tenant
     *         boundary).
     */
    public static UUID getCurrentTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Removes the tenant binding from the current thread.
     * MUST be called in a finally block at the end of every request,
     * regardless of success or failure, to prevent tenant identity from
     * leaking into the next request handled by the same pooled thread.
     *
     * Called exclusively by JwtAuthenticationFilter.
     */
    public static void clear() {
        log.debug("TenantContext: clearing tenant binding from thread [{}]",
            Thread.currentThread().getName());
        CURRENT_TENANT.remove();
    }
}