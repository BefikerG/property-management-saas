package com.propmanager.modules.billing.repository;

import com.propmanager.modules.billing.entity.Invoice;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.tenantId = :tenantId")
    Optional<Invoice> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId ORDER BY i.issuedAt DESC")
    List<Invoice> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.tenantId = :tenantId
          AND i.status = :status
        ORDER BY i.issuedAt DESC
        """)
    List<Invoice> findAllByTenantIdAndStatus(
        @Param("tenantId") UUID          tenantId,
        @Param("status")   InvoiceStatus status
    );

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.lease.id = :leaseId
          AND i.tenantId = :tenantId
        ORDER BY i.billingPeriod DESC
        """)
    List<Invoice> findAllByLeaseIdAndTenantId(
        @Param("leaseId")  UUID leaseId,
        @Param("tenantId") UUID tenantId
    );

    /**
     * Used by the billing engine to check if an invoice already exists
     * before attempting insertion. Works alongside the database unique
     * index as a clean pre-check for structured error messages.
     */
    @Query("""
        SELECT COUNT(i) > 0 FROM Invoice i
        WHERE i.lease.id = :leaseId
          AND i.billingPeriod = :billingPeriod
        """)
    boolean existsByLeaseIdAndBillingPeriod(
        @Param("leaseId")       UUID   leaseId,
        @Param("billingPeriod") String billingPeriod
    );
}