package com.propmanager.modules.lease.entity;

import com.propmanager.core.tenant.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapping to public.tenant_profiles.
 *
 * Represents a renter's centralized record within an Organization's
 * directory (PRD v2.1 §3.3.1). A TenantProfile persists across
 * multiple leases over time — the same individual accumulates a full
 * rental history under one profile.
 *
 * Extends TenantEntity which provides:
 *   - tenant_id column (FK → organizations.id)
 *   - Hibernate @FilterDef / @Filter for automatic tenant scoping
 *
 * The leases collection is lazily loaded — it is never eagerly
 * fetched to prevent N+1 problems on the tenant directory list endpoint.
 * The detail endpoint explicitly loads lease history via a dedicated
 * repository query when required.
 */
@Entity
@Table(
    name   = "tenant_profiles",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(
            name        = "uq_tenant_profiles_tenant_email",
            columnNames = {"tenant_id", "email"}
        )
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProfile extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "identification_reference", length = 255)
    private String identificationReference;

    @OneToMany(mappedBy = "tenantProfile", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Lease> leases = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}