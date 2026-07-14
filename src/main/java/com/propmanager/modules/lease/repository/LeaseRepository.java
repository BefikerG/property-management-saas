package com.propmanager.modules.lease.repository;

import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    @Query("SELECT l FROM Lease l WHERE l.id = :id AND l.tenantId = :tenantId")
    Optional<Lease> findByIdAndTenantId(
            @Param("id") UUID id,
            @Param("tenantId") UUID tenantId);

    @Query("""
            SELECT l FROM Lease l
            WHERE l.tenantId = :tenantId
            ORDER BY l.createdAt DESC
            """)
    Page<Lease> findAllByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("""
            SELECT l FROM Lease l
            WHERE l.tenantId = :tenantId
              AND l.status = :status
            ORDER BY l.createdAt DESC
            """)
    Page<Lease> findAllByTenantIdAndStatus(
            @Param("tenantId") UUID tenantId,
            @Param("status") LeaseStatus status,
            Pageable pageable);

    @Query("""
            SELECT l FROM Lease l
            WHERE l.unit.id = :unitId
              AND l.tenantId = :tenantId
            ORDER BY l.createdAt DESC
            """)
    Page<Lease> findAllByUnitIdAndTenantId(
            @Param("unitId") UUID unitId,
            @Param("tenantId") UUID tenantId,
            Pageable pageable);

    @Query("""
            SELECT l FROM Lease l
            WHERE l.tenantProfile.id = :tenantProfileId
              AND l.tenantId = :tenantId
            ORDER BY l.createdAt DESC
            """)
    Page<Lease> findAllByTenantProfileIdAndTenantId(
            @Param("tenantProfileId") UUID tenantProfileId,
            @Param("tenantId") UUID tenantId,
            Pageable pageable);

    /**
     * ⚠️ CROSS-TENANT QUERY — FOR BILLING ENGINE USE ONLY.
     * Returns ALL leases with the given status across ALL organizations.
     * Must NEVER be called from any tenant-scoped business service.
     * Calling this from a business service is a critical data isolation violation.
     *
     * Authorized callers: BillingJobConfig.activeLeaseReader() ONLY.
     */
    @Query("SELECT l FROM Lease l WHERE l.status = :status")
    List<Lease> findAllByStatus(@Param("status") LeaseStatus status);

    /**
     * Checks whether a unit already has an ACTIVE lease.
     * Used by the service layer to produce a clean 409 response
     * BEFORE the database partial unique index fires — giving a
     * structured error message rather than a raw constraint violation.
     * The database index remains the ultimate safety net under concurrency.
     */
    @Query("""
            SELECT COUNT(l) > 0 FROM Lease l
            WHERE l.unit.id = :unitId
              AND l.tenantId = :tenantId
              AND l.status = 'ACTIVE'
            """)
    boolean existsActiveLeasForUnit(
            @Param("unitId") UUID unitId,
            @Param("tenantId") UUID tenantId);

    /**
     * Finds all ACTIVE leases whose end_date is strictly before the
     * given date. Used exclusively by LeaseExpiryJob.
     *
     * ⚠️  CROSS-TENANT QUERY — FOR LEASE EXPIRY JOB USE ONLY.
     * Returns leases across ALL organizations.
     * Must NEVER be called from any tenant-scoped business service.
     */
    @Query("""
        SELECT l FROM Lease l
        WHERE l.status = 'ACTIVE'
          AND l.endDate < :today
        """)
    List<Lease> findAllActiveWithEndDateBefore(@Param("today") LocalDate today);
}