package com.propmanager.core.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * AOP interceptor that binds the Hibernate "tenantFilter" to the active
 * Hibernate Session before any Spring Data JPA repository method executes
 * (TRD §3.2 — Automated Repository Scoping).
 *
 * How it works:
 *   The @Before advice fires on every method invocation matching the
 *   pointcut expression:
 *
 *       execution(* com.propmanager.modules..repository..*(..))
 *
 *   This targets every method on every interface in any repository
 *   sub-package under com.propmanager.modules — covering every current
 *   and future business-domain repository in the platform without
 *   requiring any per-repository configuration.
 *
 *   Before the matched method executes, the advice:
 *     1. Unwraps the underlying Hibernate Session from the injected
 *        EntityManager.
 *     2. Reads the current tenant UUID from TenantContext.
 *     3. If a tenant is present (normal authenticated request path),
 *        enables the "tenantFilter" on the Session and sets its tenantId
 *        parameter to the authenticated organization's UUID.
 *     4. If no tenant is present (batch job, scheduled task, or any
 *        execution context that legitimately operates without a tenant
 *        boundary), the filter is not enabled and the query runs
 *        unrestricted — which is correct and intentional for cross-org
 *        batch processing.
 *
 * Why AOP rather than a Repository base class override:
 *   The Hibernate filter mechanism requires access to the underlying
 *   Session object, which is not directly available inside a Spring Data
 *   JPA repository interface. An AOP @Before advice with a PersistenceContext-
 *   injected EntityManager is the standard, idiomatic approach for applying
 *   Hibernate named filters uniformly across all repositories without
 *   modifying each repository individually.
 *
 * Why this is safer than WHERE-clause conventions:
 *   A developer writing a new repository method cannot accidentally omit
 *   the tenant filter — the filter is applied by this aspect regardless
 *   of what the repository method's own query says. The Hibernate filter
 *   appends its condition at the SQL generation level, below any JPQL
 *   or Criteria API logic the developer writes.
 *
 * Important: OrganizationRepository is in com.propmanager.modules.organization
 * and IS covered by this pointcut. However, Organization does NOT extend
 * TenantEntity and therefore does NOT have the "tenantFilter" filter
 * definition registered with Hibernate. Calling session.enableFilter() on
 * an entity that has no @FilterDef for that filter name is a no-op in
 * Hibernate — it silently ignores the enable call. Organization queries
 * therefore run without any tenant filter applied, which is architecturally
 * correct: organizations are System-Wide, not Tenant-Scoped.
 */
@Slf4j
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Fires before every repository method under com.propmanager.modules.
     * Enables the Hibernate "tenantFilter" on the current Session when
     * a tenant context is present on the executing thread.
     */
    @Before("execution(* com.propmanager.modules..repository..*(..))")
    public void activateTenantFilter() {
        UUID currentTenantId = TenantContext.getCurrentTenantId();

        if (currentTenantId == null) {
            // No tenant context — batch job, scheduled task, or unauthenticated
            // bootstrap call. Allow the query to run unrestricted.
            log.debug("TenantFilterAspect: no tenant context present — " +
                      "skipping filter activation (batch/system context).");
            return;
        }

        Session session = entityManager.unwrap(Session.class);
        Filter filter   = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", currentTenantId);

        log.debug("TenantFilterAspect: enabled tenantFilter for tenant [{}] " +
                  "on thread [{}]", currentTenantId, Thread.currentThread().getName());
    }
}