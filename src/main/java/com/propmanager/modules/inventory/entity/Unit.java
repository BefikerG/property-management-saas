package com.propmanager.modules.inventory.entity;

import com.propmanager.core.tenant.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping to public.units.
 *
 * Represents the individual rentable space — the fundamental object
 * against which leases are executed and billing is calculated.
 *
 * Financial precision (TRD §4.1):
 *   baseline_price is mapped as BigDecimal with
 *   columnDefinition = "NUMERIC(15,2)" — never Double or Float.
 *   This directly enforces the Currency & Mathematical Precision Rule.
 *
 * Status state machine (TRD §2.2):
 *   VACANT → OCCUPIED     (triggered by lease activation — Phase 2)
 *   OCCUPIED → VACANT     (triggered by lease termination — Phase 2)
 *   VACANT → MAINTENANCE  (manual override via PATCH /status)
 *   MAINTENANCE → VACANT  (manual override via PATCH /status)
 *   OCCUPIED → MAINTENANCE is INVALID — enforced at service layer.
 *
 * structure_id is nullable: units may be attached directly to a
 * Property without an intermediate PropertyStructure.
 */
@Entity
@Table(
    name   = "units",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(
            name        = "uq_units_tenant_property_number",
            columnNames = {"tenant_id", "property_id", "unit_number"}
        )
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Unit extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    /**
     * Nullable — units may attach directly to a property.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    private PropertyStructure structure;

    @Column(name = "unit_number", nullable = false, length = 50)
    private String unitNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private UnitStatus status = UnitStatus.VACANT;

    @Column(name = "specification", columnDefinition = "TEXT")
    private String specification;

    /**
     * Rental price in ETB. NUMERIC(15,2) — never FLOAT or DOUBLE.
     * TRD §4.1 — Currency & Mathematical Precision Rule.
     */
    @Column(name = "baseline_price", nullable = false,
            columnDefinition = "NUMERIC(15,2)", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal baselinePrice = BigDecimal.ZERO;

    @Column(name = "currency_code", nullable = false, length = 3)
    @Builder.Default
    private String currencyCode = "ETB";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}