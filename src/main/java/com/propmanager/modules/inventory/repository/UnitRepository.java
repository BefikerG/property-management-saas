package com.propmanager.modules.inventory.repository;

import com.propmanager.modules.inventory.entity.Unit;
import com.propmanager.modules.inventory.entity.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    @Query("SELECT u FROM Unit u WHERE u.id = :id AND u.tenantId = :tenantId")
    Optional<Unit> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("""
        SELECT u FROM Unit u
        WHERE u.property.id = :propertyId
          AND u.tenantId = :tenantId
        ORDER BY u.unitNumber ASC
        """)
    List<Unit> findAllByPropertyIdAndTenantId(
        @Param("propertyId") UUID propertyId,
        @Param("tenantId")   UUID tenantId
    );

    @Query("""
        SELECT u FROM Unit u
        WHERE u.property.id = :propertyId
          AND u.tenantId = :tenantId
          AND u.status = :status
        ORDER BY u.unitNumber ASC
        """)
    List<Unit> findAllByPropertyIdAndTenantIdAndStatus(
        @Param("propertyId") UUID       propertyId,
        @Param("tenantId")   UUID       tenantId,
        @Param("status")     UnitStatus status
    );

    @Query("""
        SELECT COUNT(u) > 0 FROM Unit u
        WHERE u.unitNumber = :unitNumber
          AND u.property.id = :propertyId
          AND u.tenantId = :tenantId
        """)
    boolean existsByUnitNumberAndPropertyIdAndTenantId(
        @Param("unitNumber")  String unitNumber,
        @Param("propertyId")  UUID   propertyId,
        @Param("tenantId")    UUID   tenantId
    );
}