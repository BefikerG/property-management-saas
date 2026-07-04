package com.propmanager.modules.inventory.entity;

import com.propmanager.core.tenant.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapping to public.property_structures.
 *
 * Represents an optional intermediate structural layer within a Property
 * (Block, Floor, Wing, or Zone). This layer exists because many commercial
 * properties have meaningful internal subdivisions that affect how units
 * are numbered, accessed, and marketed.
 *
 * Self-referential relationship:
 *   A PropertyStructure may have a parent PropertyStructure, enabling
 *   arbitrarily nested hierarchies — e.g. "Block A" → "Ground Floor" → units.
 *   parent is nullable: a top-level structure has no parent (it is a direct
 *   child of the Property).
 *
 * Cascade rules (TRD §2.2 — property_structures):
 *   - ON DELETE CASCADE from property_id: removing a property removes
 *     all its structural segments automatically.
 *   - ON DELETE CASCADE from parent_id: removing a parent structure
 *     removes all child structures beneath it.
 *   - ON DELETE SET NULL from unit.structure_id: removing a structure
 *     leaves its units in place, attached directly to the property.
 */
@Entity
@Table(
    name   = "property_structures",
    schema = "public"
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyStructure extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    /**
     * Self-referential parent. Null for top-level structures.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PropertyStructure parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<PropertyStructure> children = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 50)
    private StructureNodeType nodeType;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "ordinal", nullable = false)
    @Builder.Default
    private Short ordinal = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}