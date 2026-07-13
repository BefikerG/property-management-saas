package com.propmanager.core.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propmanager.core.audit.entity.AuditLog;
import com.propmanager.core.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Asynchronous audit ledger writer.
 *
 * Execution model (TRD §5 — Asynchronous Audit Ledger Architecture):
 *
 *   1. A service method performs a write operation within its own
 *      @Transactional boundary.
 *   2. Before committing, the service publishes an AuditDomainEvent
 *      via ApplicationEventPublisher.
 *   3. The business transaction commits. The HTTP response thread
 *      is now free to return to the client.
 *   4. AFTER the commit is confirmed, Spring invokes this listener.
 *      The @Async annotation dispatches it to the dedicated
 *      auditTaskExecutor thread pool — not the HTTP request thread.
 *   5. This method serializes the event payload to JSON and persists
 *      an AuditLog record in its own, independent transaction.
 *   6. If this async write fails (transient DB issue), the failure
 *      is logged as an error for operational monitoring. The original
 *      business transaction is NOT affected — it already committed.
 *
 * Why AFTER_COMMIT (not AFTER_COMPLETION or synchronous):
 *   A transaction that publishes an event but subsequently rolls back
 *   must never result in an audit record describing a state change
 *   that, from the database's point of view, never happened.
 *   AFTER_COMMIT guarantees audit records only exist for durably
 *   persisted state changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncAuditLedgerListener {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper       objectMapper;

    @Async(AuditAsyncConfig.AUDIT_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuditEvent(AuditDomainEvent event) {
        log.debug("AuditLedger: writing [{}] on [{}] entity [{}] by actor [{}]",
            event.getActionType(), event.getEntityType(),
            event.getEntityId(), event.getActorId());
        try {
            AuditLog record = AuditLog.builder()
                .tenantId(event.getTenantId())
                .actorId(event.getActorId())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .actionType(event.getActionType())
                .oldValue(serialize(event.getOldState()))
                .newValue(serialize(event.getNewState()))
                .occurredAt(LocalDateTime.now())
                .build();

            auditLogRepository.save(record);

            log.debug("AuditLedger: record written. id=[{}], entity=[{}:{}]",
                record.getId(), event.getEntityType(), event.getEntityId());

        } catch (Exception ex) {
            // Audit failure must NEVER propagate back to the caller.
            // The business transaction already committed — this is
            // a best-effort write. Log for operational monitoring.
            log.error("AuditLedger: FAILED to write audit record. " +
                "entity=[{}:{}], action=[{}], error={}",
                event.getEntityType(), event.getEntityId(),
                event.getActionType(), ex.getMessage(), ex);
        }
    }

    private String serialize(Object state) {
        if (state == null) return null;
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException ex) {
            log.warn("AuditLedger: could not serialize state object [{}]: {}",
                state.getClass().getSimpleName(), ex.getMessage());
            return "{\"error\": \"serialization_failed\"}";
        }
    }
}