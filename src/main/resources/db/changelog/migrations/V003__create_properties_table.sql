-- ============================================================
-- Changeset V003: Create properties table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Top-level physical real estate asset belonging to
--           an Organization. Every property is tenant-scoped
--           via tenant_id → organizations(id).
-- ============================================================

CREATE TABLE IF NOT EXISTS public.properties
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,
    name          VARCHAR(255) NOT NULL,
    address       TEXT         NOT NULL,
    location_city VARCHAR(100) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_properties
        PRIMARY KEY (id),

    CONSTRAINT fk_properties_organization
        FOREIGN KEY (tenant_id)
        REFERENCES public.organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_properties_tenant_name
        UNIQUE (tenant_id, name)
);

CREATE INDEX IF NOT EXISTS idx_properties_tenant_id
    ON public.properties (tenant_id);

CREATE INDEX IF NOT EXISTS idx_properties_tenant_city
    ON public.properties (tenant_id, location_city);

COMMENT ON TABLE  public.properties               IS 'Top-level physical real estate asset scoped to an organization.';
COMMENT ON COLUMN public.properties.id            IS 'Immutable UUID primary key.';
COMMENT ON COLUMN public.properties.tenant_id     IS 'FK to organizations(id) — tenant isolation discriminator.';
COMMENT ON COLUMN public.properties.name          IS 'Display name of the property. Unique per organization.';
COMMENT ON COLUMN public.properties.address       IS 'Full street address of the property.';
COMMENT ON COLUMN public.properties.location_city IS 'City where the property is located. Indexed for city-filtered portfolio queries.';
COMMENT ON COLUMN public.properties.description   IS 'Optional free-text description of physical characteristics.';