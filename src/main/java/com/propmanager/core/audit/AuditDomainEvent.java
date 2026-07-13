package com.propmanager.core.audit;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Immutable domain event published by any service method that
 * performs a state-changing operation.
 *
 * Published via Spring's ApplicationEventPublisher within the
 * originating business transaction. The AsyncAuditLedgerListener
 * is bound to AFTER_COMMIT — this event carrier is only acted
 * upon once the originating transaction commits successfully.
 *
 * If the originating transaction rolls back, this event is
 * discarded and no audit record is written — guaranteeing that
 * the audit ledger only ever describes actions that actually
 * persisted to the database.
 *
 * Fields:
 *   tenantId   — organization boundary of the affected record
 *   actorId    — UUID of the StaffMember who performed the action
 *   entityType — canonical domain entity name (e.g. "LEASE")
 *   entityId   — UUID of the affected record
 *   actionType — canonical action verb (e.g. "ACTIVATE")
 *   oldState   — serializable snapshot before the action (null on CREATE)
 *   newState   — serializable snapshot after the action (null on DELETE)
 */
@Getter
public class AuditDomainEvent extends ApplicationEvent {

    private final UUID   tenantId;
    private final UUID   actorId;
    private final String entityType;
    private final UUID   entityId;
    private final String actionType;
    private final Object oldState;
    private final Object newState;

    public AuditDomainEvent(
        Object source,
        UUID   tenantId,
        UUID   actorId,
        String entityType,
        UUID   entityId,
        String actionType,
        Object oldState,
        Object newState
    ) {
        super(source);
        this.tenantId   = tenantId;
        this.actorId    = actorId;
        this.entityType = entityType;
        this.entityId   = entityId;
        this.actionType = actionType;
        this.oldState   = oldState;
        this.newState   = newState;
    }
}