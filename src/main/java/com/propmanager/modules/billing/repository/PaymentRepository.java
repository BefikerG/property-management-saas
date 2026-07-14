package com.propmanager.modules.billing.repository;

import com.propmanager.modules.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Payment> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("""
        SELECT p FROM Payment p
        WHERE p.invoice.id = :invoiceId
          AND p.tenantId = :tenantId
        ORDER BY p.paidAt DESC
        """)
    Page<Payment> findAllByInvoiceIdAndTenantId(
        @Param("invoiceId") UUID invoiceId,
        @Param("tenantId")  UUID tenantId,
        Pageable                 pageable
    );

    /**
     * Sums all payment amounts for a given invoice.
     * Used by PaymentServiceImpl to recalculate amount_paid
     * after a new payment is logged.
     * Returns ZERO if no payments exist yet.
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.invoice.id = :invoiceId
        """)
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
}