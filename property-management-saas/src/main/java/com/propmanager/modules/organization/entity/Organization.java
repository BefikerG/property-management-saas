package com.propmanager.modules.organization.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping to public.organizations.
 *
 * This is the root tenant boundary of the entire multi-tenant architecture.
 * Every other tenant-scoped entity (Property, Unit, Lease, Invoice, etc.)
 * carries a tenant_id foreign key that ultimately resolves to this table's
 * primary key.
 *
 * Design decisions (TRD §2.1 & §2.2):
 *   - UUID primary key: org IDs are embedded in JWT tokens exposed to
 *     client applications. Sequential BIGSERIAL IDs would expose a tenant
 *     enumeration attack vector. UUID eliminates this surface.
 *   - No tenant_id column: this entity IS the tenant boundary.
 *     Adding a self-referential tenant_id would be architecturally incorrect.
 *   - UNIQUE constraint on name: enforced both here via @Column(unique=true)
 *     and at the database layer via uq_organizations_name.
 */
@Entity
@Table(
    name = "organizations",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_organizations_name", columnNames = "name")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private OrgStatus status = OrgStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

