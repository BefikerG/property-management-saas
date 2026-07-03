package com.propmanager.modules.inventory.entity;

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
 * JPA entity mapping to public.properties.
 *
 * Represents the top-level physical real estate asset belonging to an
 * Organization — a building, shopping plaza, or residential complex.
 *
 * Extends TenantEntity which provides:
 *   - tenant_id column (FK → organizations.id)
 *   - @FilterDef / @Filter Hibernate annotations for automatic
 *     tenant scoping via TenantFilterAspect
 *
 * Design decisions:
 *   - @SuperBuilder: required because TenantEntity uses @SuperBuilder.
 *     Regular @Builder does not propagate superclass fields.
 *   - UNIQUE (tenant_id, name): a property name must be unique within
 *     an organization but the same name may exist across organizations.
 *   - structures and units are mapped as @OneToMany for convenience
 *     in detail queries but are lazily loaded — never eagerly fetched
 *     to avoid N+1 problems on list endpoints.
 */
@Entity
@Table(
    name   = "properties",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(
            name        = "uq_properties_tenant_name",
            columnNames = {"tenant_id", "name"}
        )
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Property extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "location_city", nullable = false, length = 100)
    private String locationCity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<PropertyStructure> structures = new ArrayList<>();

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Unit> units = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}