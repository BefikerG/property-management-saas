package com.propmanager.modules.inventory.repository;

import com.propmanager.modules.inventory.entity.PropertyStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyStructureRepository extends JpaRepository<PropertyStructure, UUID> {

    @Query("SELECT s FROM PropertyStructure s WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<PropertyStructure> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("""
        SELECT s FROM PropertyStructure s
        WHERE s.property.id = :propertyId
          AND s.tenantId = :tenantId
        ORDER BY s.ordinal ASC, s.name ASC
        """)
    List<PropertyStructure> findAllByPropertyIdAndTenantId(
        @Param("propertyId") UUID propertyId,
        @Param("tenantId")   UUID tenantId
    );
}
