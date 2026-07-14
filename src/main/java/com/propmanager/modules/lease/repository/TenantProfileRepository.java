package com.propmanager.modules.lease.repository;

import com.propmanager.modules.lease.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantProfileRepository extends JpaRepository<TenantProfile, UUID> {

    @Query("SELECT t FROM TenantProfile t WHERE t.id = :id AND t.tenantId = :tenantId")
    Optional<TenantProfile> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("SELECT t FROM TenantProfile t WHERE t.tenantId = :tenantId ORDER BY t.fullName ASC")
    Page<TenantProfile> findAllByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("""
        SELECT t FROM TenantProfile t
        WHERE t.tenantId = :tenantId
          AND (LOWER(t.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(t.email) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY t.fullName ASC
        """)
    Page<TenantProfile> searchByTenantId(
        @Param("tenantId") UUID   tenantId,
        @Param("query")    String query,
        Pageable                  pageable
    );

    @Query("SELECT COUNT(t) > 0 FROM TenantProfile t WHERE t.email = :email AND t.tenantId = :tenantId")
    boolean existsByEmailAndTenantId(
        @Param("email")    String email,
        @Param("tenantId") UUID   tenantId
    );
}