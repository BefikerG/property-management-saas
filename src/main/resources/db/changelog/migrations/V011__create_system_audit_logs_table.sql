-- ============================================================
-- Changeset V011: Create system_audit_logs table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Append-only immutable audit ledger capturing every
--           state-changing operation across the platform.
--           Written asynchronously via @TransactionalEventListener
--           (AFTER_COMMIT) — never blocks the request thread.
--
-- Key design decisions (TRD §2.2):
--   - BIGSERIAL PK: monotonically increasing, guarantees
--     chronological ordering without a secondary sort column.
--     UUID would produce a larger index with no ordering benefit
--     on an append-only, high-volume table.
--   - JSONB payload: variable-shape before/after state per entity
--     type. Supports future GIN indexing for field-level queries.
--   - No UPDATE or DELETE ever issued on this table.
--     INSERT-only enforced at the application layer.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.system_audit_logs
(
    id          BIGSERIAL    NOT NULL,
    tenant_id   UUID         NOT NULL,
    actor_id    UUID         NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID         NOT NULL,
    action_type VARCHAR(50)  NOT NULL,
    old_value   JSONB,
    new_value   JSONB,
    occurred_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_system_audit_logs
        PRIMARY KEY (id),

    CONSTRAINT chk_audit_action_type
        CHECK (action_type IN (
            'CREATE', 'UPDATE', 'DELETE',
            'ACTIVATE', 'TERMINATE', 'EXPIRE',
            'PAYMENT_LOGGED', 'STATUS_CHANGE',
            'ROLE_CHANGE', 'DEACTIVATE', 'REACTIVATE'
        ))
);

CREATE INDEX IF NOT EXISTS idx_audit_tenant_entity
    ON public.system_audit_logs (tenant_id, entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_tenant_actor
    ON public.system_audit_logs (tenant_id, actor_id);

CREATE INDEX IF NOT EXISTS idx_audit_tenant_occurred
    ON public.system_audit_logs (tenant_id, occurred_at DESC);

COMMENT ON TABLE  public.system_audit_logs            IS 'Append-only immutable audit ledger. Never UPDATE or DELETE.';
COMMENT ON COLUMN public.system_audit_logs.id         IS 'BIGSERIAL — monotonic ordering without secondary sort column.';
COMMENT ON COLUMN public.system_audit_logs.tenant_id  IS 'Organization boundary — every audit record is tenant-scoped.';
COMMENT ON COLUMN public.system_audit_logs.actor_id   IS 'UUID of the StaffMember who performed the action.';
COMMENT ON COLUMN public.system_audit_logs.entity_type IS 'Domain entity class: ORGANIZATION, PROPERTY, UNIT, LEASE, etc.';
COMMENT ON COLUMN public.system_audit_logs.entity_id  IS 'UUID of the affected record.';
COMMENT ON COLUMN public.system_audit_logs.action_type IS 'Canonical action performed on the entity.';
COMMENT ON COLUMN public.system_audit_logs.old_value  IS 'JSONB snapshot of entity state before the action. Null on CREATE.';
COMMENT ON COLUMN public.system_audit_logs.new_value  IS 'JSONB snapshot of entity state after the action. Null on DELETE.';