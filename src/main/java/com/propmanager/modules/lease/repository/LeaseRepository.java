package com.propmanager.modules.lease.repository;

import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    @Query("SELECT l FROM Lease l WHERE l.id = :id AND l.tenantId = :tenantId")
    Optional<Lease> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("""
        SELECT l FROM Lease l
        WHERE l.tenantId = :tenantId
        ORDER BY l.createdAt DESC
        """)
    List<Lease> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Query("""
        SELECT l FROM Lease l
        WHERE l.tenantId = :tenantId
          AND l.status = :status
        ORDER BY l.createdAt DESC
        """)
    List<Lease> findAllByTenantIdAndStatus(
        @Param("tenantId") UUID        tenantId,
        @Param("status")   LeaseStatus status
    );

    @Query("""
        SELECT l FROM Lease l
        WHERE l.unit.id = :unitId
          AND l.tenantId = :tenantId
        ORDER BY l.createdAt DESC
        """)
    List<Lease> findAllByUnitIdAndTenantId(
        @Param("unitId")   UUID unitId,
        @Param("tenantId") UUID tenantId
    );

    @Query("""
        SELECT l FROM Lease l
        WHERE l.tenantProfile.id = :tenantProfileId
          AND l.tenantId = :tenantId
        ORDER BY l.createdAt DESC
        """)
    List<Lease> findAllByTenantProfileIdAndTenantId(
        @Param("tenantProfileId") UUID tenantProfileId,
        @Param("tenantId")        UUID tenantId
    );

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
        @Param("unitId")   UUID unitId,
        @Param("tenantId") UUID tenantId
    );
}