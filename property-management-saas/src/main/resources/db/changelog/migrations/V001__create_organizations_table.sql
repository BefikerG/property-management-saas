-- ============================================================
-- Changeset V001: Create organizations table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Root tenant boundary — every other tenant-scoped
--           table's tenant_id resolves to this table's id.
--           No tenant_id on this table by design (TRD §2.1).
-- ============================================================

CREATE TABLE IF NOT EXISTS public.organizations
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_organizations PRIMARY KEY (id),
    CONSTRAINT uq_organizations_name UNIQUE (name),
    CONSTRAINT chk_organizations_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

COMMENT ON TABLE  public.organizations            IS 'Root tenant identity boundary for the multi-tenant SaaS platform.';
COMMENT ON COLUMN public.organizations.id         IS 'Immutable UUID — embedded in JWT org_id claim.';
COMMENT ON COLUMN public.organizations.name       IS 'Legal or trading name of the property management firm.';
COMMENT ON COLUMN public.organizations.status     IS 'Lifecycle state: ACTIVE or SUSPENDED.';
COMMENT ON COLUMN public.organizations.created_at IS 'Registration timestamp, set once on insert.';
COMMENT ON COLUMN public.organizations.updated_at IS 'Updated by application layer on every modification.';

