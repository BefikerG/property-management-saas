package com.propmanager.modules.lease.entity;

import com.propmanager.core.tenant.TenantEntity;
import com.propmanager.modules.inventory.entity.Unit;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping to public.leases.
 *
 * The most architecturally critical entity in the platform — it binds
 * a TenantProfile to a Unit for a defined time period, enforces the
 * Single Active Lease Rule via a database partial unique index, and
 * triggers atomic unit status transitions on activation and termination.
 *
 * Critical constraints (TRD §9.1):
 *   - idx_unique_active_lease_per_unit: UNIQUE(tenant_id, unit_id)
 *     WHERE status = 'ACTIVE'. Enforced at the database engine level.
 *     Attempting to activate a second lease against an OCCUPIED unit
 *     raises a PostgreSQL unique constraint violation, surfaced as
 *     HTTP 409 Conflict.
 *
 * Atomic unit status transitions (TRD §9.6 — C-06):
 *   - Lease → ACTIVE:     unit.status → OCCUPIED  (same @Transactional)
 *   - Lease → TERMINATED: unit.status → VACANT    (same @Transactional)
 *   - Lease → EXPIRED:    unit.status → VACANT    (same @Transactional)
 *
 * Financial precision (TRD §4.1):
 *   - monthly_rent: NUMERIC(15,2) mapped as BigDecimal
 *   - security_deposit: NUMERIC(15,2) mapped as BigDecimal
 *   - Never Double or Float for any monetary field.
 */
@Entity
@Table(
    name   = "leases",
    schema = "public"
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lease extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_profile_id", nullable = false)
    private TenantProfile tenantProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private LeaseStatus status = LeaseStatus.DRAFT;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Monthly rental amount. NUMERIC(15,2) — never Float or Double.
     * Locked at signing. TRD §4.1 financial precision mandate.
     */
    @Column(name = "monthly_rent", nullable = false,
            columnDefinition = "NUMERIC(15,2)", precision = 15, scale = 2)
    private BigDecimal monthlyRent;

    /**
     * Day of month (1–28) on which invoices are generated.
     * Consumed by the Spring Batch billing engine (Phase 4).
     */
    @Column(name = "billing_day", nullable = false)
    @Builder.Default
    private Short billingDay = 1;

    /**
     * Security deposit amount. NUMERIC(15,2). Optional, defaults to zero.
     */
    @Column(name = "security_deposit",
            columnDefinition = "NUMERIC(15,2)", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal securityDeposit = BigDecimal.ZERO;

    @Column(name = "escalation_terms", columnDefinition = "TEXT")
    private String escalationTerms;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}