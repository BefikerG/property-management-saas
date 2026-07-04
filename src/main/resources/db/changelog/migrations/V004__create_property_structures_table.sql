-- ============================================================
-- Changeset V004: Create property_structures table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Optional intermediate structural layer within a
--           property (Block, Floor, Wing). Self-referential
--           via nullable parent_id to support nested hierarchies.
--           Units attach to a structure or directly to a property.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.property_structures
(
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID        NOT NULL,
    property_id   UUID        NOT NULL,
    parent_id     UUID,
    node_type     VARCHAR(50) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    ordinal       SMALLINT    NOT NULL DEFAULT 0,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_property_structures
        PRIMARY KEY (id),

    CONSTRAINT fk_property_structures_organization
        FOREIGN KEY (tenant_id)
        REFERENCES public.organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_property_structures_property
        FOREIGN KEY (property_id)
        REFERENCES public.properties (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_property_structures_parent
        FOREIGN KEY (parent_id)
        REFERENCES public.property_structures (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_property_structures_node_type
        CHECK (node_type IN ('BLOCK', 'FLOOR', 'WING', 'ZONE'))
);

CREATE INDEX IF NOT EXISTS idx_property_structures_tenant_property
    ON public.property_structures (tenant_id, property_id);

CREATE INDEX IF NOT EXISTS idx_property_structures_parent
    ON public.property_structures (parent_id);

COMMENT ON TABLE  public.property_structures             IS 'Optional intermediate structural layer (Block, Floor, Wing) within a property.';
COMMENT ON COLUMN public.property_structures.parent_id  IS 'Self-referential FK — NULL means this structure is a direct child of the property.';
COMMENT ON COLUMN public.property_structures.node_type  IS 'Structural classification: BLOCK, FLOOR, WING, or ZONE.';
COMMENT ON COLUMN public.property_structures.ordinal    IS 'Display ordering within a property or parent structure.';