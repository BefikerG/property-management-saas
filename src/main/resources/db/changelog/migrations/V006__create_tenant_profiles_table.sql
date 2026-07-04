-- ============================================================
-- Changeset V006: Create tenant_profiles table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Centralized renter directory scoped per organization.
--           A TenantProfile persists across multiple leases over
--           time — the same individual accumulates a full rental
--           history under a single profile record.
--
-- Key constraints:
--   - UNIQUE (tenant_id, email): the same email may exist across
--     different organizations but not twice within one.
--   - ON DELETE RESTRICT on tenant_id: a tenant profile cannot
--     be deleted while active leases reference it.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.tenant_profiles
(
    id                       UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id                UUID         NOT NULL,
    full_name                VARCHAR(255) NOT NULL,
    email                    VARCHAR(320) NOT NULL,
    phone                    VARCHAR(30),
    identification_reference VARCHAR(255),
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_tenant_profiles
        PRIMARY KEY (id),

    CONSTRAINT fk_tenant_profiles_organization
        FOREIGN KEY (tenant_id)
        REFERENCES public.organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_tenant_profiles_tenant_email
        UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_tenant_profiles_tenant_id
    ON public.tenant_profiles (tenant_id);

CREATE INDEX IF NOT EXISTS idx_tenant_profiles_tenant_email
    ON public.tenant_profiles (tenant_id, email);

COMMENT ON TABLE  public.tenant_profiles                          IS 'Centralized renter directory. Scoped per organization. Persists across multiple leases.';
COMMENT ON COLUMN public.tenant_profiles.id                      IS 'Immutable UUID primary key.';
COMMENT ON COLUMN public.tenant_profiles.tenant_id               IS 'FK to organizations(id) — tenant isolation discriminator.';
COMMENT ON COLUMN public.tenant_profiles.full_name               IS 'Full legal name of the renter.';
COMMENT ON COLUMN public.tenant_profiles.email                   IS 'Contact email. Unique per organization, not globally.';
COMMENT ON COLUMN public.tenant_profiles.phone                   IS 'Optional contact phone number.';
COMMENT ON COLUMN public.tenant_profiles.identification_reference IS 'Optional reference to supporting ID or legal documentation.';