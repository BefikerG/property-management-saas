-- ============================================================
-- Changeset V002: Create staff_members table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Represents individual user accounts belonging to
--           an Organization. This is the first Tenant-Scoped
--           table in the schema — every row carries a tenant_id
--           FK referencing public.organizations(id).
--
-- Key constraints:
--   - UNIQUE (tenant_id, email): the same email may exist
--     across different organizations but not twice within one.
--   - ON DELETE RESTRICT on tenant_id: deleting an organization
--     is blocked while staff members still exist under it.
--   - CHECK on role: enforces the exact enum values permitted.
--   - CHECK on status: enforces the exact enum values permitted.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.staff_members
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    email         VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'PROPERTY_MANAGER',
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_staff_members
        PRIMARY KEY (id),

    CONSTRAINT fk_staff_members_organization
        FOREIGN KEY (tenant_id)
        REFERENCES public.organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_staff_members_tenant_email
        UNIQUE (tenant_id, email),

    CONSTRAINT chk_staff_members_role
        CHECK (role IN ('ADMINISTRATOR', 'PROPERTY_MANAGER', 'VIEWER')),

    CONSTRAINT chk_staff_members_status
        CHECK (status IN ('ACTIVE', 'DEACTIVATED'))
);

CREATE INDEX IF NOT EXISTS idx_staff_members_tenant_id
    ON public.staff_members (tenant_id);

CREATE INDEX IF NOT EXISTS idx_staff_members_email
    ON public.staff_members (email);

COMMENT ON TABLE  public.staff_members               IS 'Tenant-scoped user accounts belonging to a property management organization.';
COMMENT ON COLUMN public.staff_members.id            IS 'Immutable UUID primary key.';
COMMENT ON COLUMN public.staff_members.tenant_id     IS 'FK to organizations(id) — tenant isolation discriminator.';
COMMENT ON COLUMN public.staff_members.email         IS 'Login identifier. Unique per organization, not globally.';
COMMENT ON COLUMN public.staff_members.password_hash IS 'BCrypt-hashed credential. Never stored or logged in plain text.';
COMMENT ON COLUMN public.staff_members.full_name     IS 'Display name of the staff member.';
COMMENT ON COLUMN public.staff_members.role          IS 'RBAC role: ADMINISTRATOR, PROPERTY_MANAGER, or VIEWER.';
COMMENT ON COLUMN public.staff_members.status        IS 'Lifecycle state: ACTIVE or DEACTIVATED.';