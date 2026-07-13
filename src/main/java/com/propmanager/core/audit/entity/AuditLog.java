package com.propmanager.core.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping to public.system_audit_logs.
 *
 * This entity intentionally does NOT extend TenantEntity.
 *
 * Reason: TenantEntity applies the Hibernate tenantFilter
 * which requires TenantContext to be populated. The audit
 * listener writes records on a separate async thread after
 * the HTTP request has completed — TenantContext has already
 * been cleared by JwtAuthenticationFilter's finally block.
 * The tenant_id is carried explicitly on the AuditDomainEvent
 * and set directly on this entity instead.
 *
 * Primary key is BIGSERIAL (Long) not UUID — see V011 for
 * the full rationale on append-only, chronologically-ordered
 * tables.
 */
@Entity
@Table(name = "system_audit_logs", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "actor_id", nullable = false, updatable = false)
    private UUID actorId;

    @Column(name = "entity_type", nullable = false, length = 100,
            updatable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false, updatable = false)
    private UUID entityId;

    @Column(name = "action_type", nullable = false, length = 50,
            updatable = false)
    private String actionType;

    /**
     * JSONB column — stored as String in Java.
     * Jackson serializes the old/new state objects to JSON strings
     * before persistence. PostgreSQL stores them as native JSONB.
     */
    @Column(name = "old_value", columnDefinition = "jsonb",
            updatable = false)
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "jsonb",
            updatable = false)
    private String newValue;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}