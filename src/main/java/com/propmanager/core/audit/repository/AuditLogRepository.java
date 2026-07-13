package com.propmanager.core.audit.repository;

import com.propmanager.core.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repository for the audit ledger.
 *
 * Note: AuditLog does NOT extend TenantEntity, so the
 * TenantFilterAspect does NOT apply here. All queries
 * include explicit tenant_id parameters — the tenant
 * boundary is enforced manually, not by the Hibernate filter.
 *
 * All queries use Pageable — audit_log is unbounded and
 * must never be queried without pagination.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.tenantId = :tenantId
        ORDER BY a.occurredAt DESC
        """)
    Page<AuditLog> findAllByTenantId(
        @Param("tenantId") UUID     tenantId,
        Pageable                    pageable
    );

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.tenantId = :tenantId
          AND a.entityType = :entityType
          AND a.entityId = :entityId
        ORDER BY a.occurredAt DESC
        """)
    Page<AuditLog> findByEntity(
        @Param("tenantId")   UUID     tenantId,
        @Param("entityType") String   entityType,
        @Param("entityId")   UUID     entityId,
        Pageable                      pageable
    );

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.tenantId = :tenantId
          AND a.actorId = :actorId
        ORDER BY a.occurredAt DESC
        """)
    Page<AuditLog> findByActor(
        @Param("tenantId") UUID     tenantId,
        @Param("actorId")  UUID     actorId,
        Pageable                    pageable
    );
}