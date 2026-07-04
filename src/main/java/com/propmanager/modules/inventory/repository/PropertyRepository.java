package com.propmanager.modules.inventory.repository;

import com.propmanager.modules.inventory.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    @Query("SELECT p FROM Property p WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Property> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("SELECT p FROM Property p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    List<Property> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT p FROM Property p WHERE p.tenantId = :tenantId AND p.locationCity = :city ORDER BY p.name ASC")
    List<Property> findAllByTenantIdAndCity(
        @Param("tenantId") UUID   tenantId,
        @Param("city")     String city
    );

    @Query("SELECT COUNT(p) > 0 FROM Property p WHERE p.name = :name AND p.tenantId = :tenantId")
    boolean existsByNameAndTenantId(
        @Param("name")     String name,
        @Param("tenantId") UUID   tenantId
    );
}