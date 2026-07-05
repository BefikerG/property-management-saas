package com.propmanager.modules.billing.entity;

import com.propmanager.core.tenant.TenantEntity;
import com.propmanager.modules.lease.entity.Lease;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapping to public.invoices.
 *
 * Financial statement generated automatically by the Spring Batch
 * billing engine against an active lease for a specific calendar month.
 *
 * Idempotency guarantee (TRD §9.2):
 *   The composite unique index idx_uq_invoice_per_lease_period on
 *   (lease_id, billing_period) ensures that running the billing job
 *   multiple times for the same month produces exactly one invoice
 *   per active lease — never duplicates.
 *
 * Financial precision (TRD §4.1):
 *   amount_due and amount_paid are NUMERIC(15,2) mapped as BigDecimal.
 *   amount_due is a snapshot of the lease's monthly_rent at the moment
 *   of invoice generation — it does not change if the lease is later
 *   amended.
 *
 * Status transitions are automatic:
 *   PaymentServiceImpl recalculates and updates status after every
 *   payment. No manual status override is permitted via the API.
 */
@Entity
@Table(
    name   = "invoices",
    schema = "public"
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_id", nullable = false)
    private Lease lease;

    /**
     * Billing month in YYYY-MM format.
     * Part of the composite unique index that guarantees idempotency.
     */
    @Column(name = "billing_period", nullable = false, length = 7)
    private String billingPeriod;

    /**
     * Snapshot of the lease's monthly_rent at generation time.
     * NUMERIC(15,2) — never Float or Double. TRD §4.1.
     */
    @Column(name = "amount_due", nullable = false,
            columnDefinition = "NUMERIC(15,2)", precision = 15, scale = 2)
    private BigDecimal amountDue;

    /**
     * Running total of payments received against this invoice.
     * Updated by PaymentServiceImpl on every payment log.
     * NUMERIC(15,2) — never Float or Double. TRD §4.1.
     */
    @Column(name = "amount_paid", nullable = false,
            columnDefinition = "NUMERIC(15,2)", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}