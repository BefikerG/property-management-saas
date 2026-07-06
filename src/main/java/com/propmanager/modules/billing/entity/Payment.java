package com.propmanager.modules.billing.entity;

import com.propmanager.core.tenant.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping to public.payments.
 *
 * Represents a single manual payment receipt logged by a property
 * manager against an open invoice. Multiple payments may be logged
 * against a single invoice (partial payment support).
 *
 * After each payment is saved, PaymentServiceImpl recalculates
 * the invoice's amount_paid and transitions its status automatically:
 *   UNPAID → PARTIALLY_PAID → PAID
 *
 * Financial precision (TRD §4.1):
 *   amount is NUMERIC(15,2) mapped as BigDecimal.
 */
@Entity
@Table(
    name   = "payments",
    schema = "public"
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /**
     * Payment amount in ETB. NUMERIC(15,2) — never Float or Double.
     */
    @Column(name = "amount", nullable = false,
            columnDefinition = "NUMERIC(15,2)", precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /**
     * Optional external reference — cheque number, bank transaction ID, etc.
     */
    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;
}