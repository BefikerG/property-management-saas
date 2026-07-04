-- ============================================================
-- Changeset V005: Create units table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Individual rentable space — the fundamental object
--           against which leases are executed. Carries the unit
--           status state machine and NUMERIC(15,2) baseline price
--           per TRD §4.1 financial precision mandate.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.units
(
    id            UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID           NOT NULL,
    property_id   UUID           NOT NULL,
    structure_id  UUID,
    unit_number   VARCHAR(50)    NOT NULL,
    status        VARCHAR(30)    NOT NULL DEFAULT 'VACANT',
    specification TEXT,
    baseline_price NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    currency_code VARCHAR(3)     NOT NULL DEFAULT 'ETB',
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_units
        PRIMARY KEY (id),

    CONSTRAINT fk_units_organization
        FOREIGN KEY (tenant_id)
        REFERENCES public.organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_units_property
        FOREIGN KEY (property_id)
        REFERENCES public.properties (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_units_structure
        FOREIGN KEY (structure_id)
        REFERENCES public.property_structures (id)
        ON DELETE SET NULL,

    CONSTRAINT uq_units_tenant_property_number
        UNIQUE (tenant_id, property_id, unit_number),

    CONSTRAINT chk_units_status
        CHECK (status IN ('VACANT', 'OCCUPIED', 'MAINTENANCE')),

    CONSTRAINT chk_units_baseline_price
        CHECK (baseline_price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_units_tenant_property
    ON public.units (tenant_id, property_id);

CREATE INDEX IF NOT EXISTS idx_units_tenant_status
    ON public.units (tenant_id, status);

COMMENT ON TABLE  public.units                  IS 'Individual rentable space — the object against which leases are executed.';
COMMENT ON COLUMN public.units.structure_id     IS 'FK to property_structures — nullable. Units may attach directly to a property.';
COMMENT ON COLUMN public.units.status           IS 'State machine: VACANT → OCCUPIED → MAINTENANCE → VACANT.';
COMMENT ON COLUMN public.units.baseline_price   IS 'Rental price in NUMERIC(15,2) per TRD §4.1 financial precision mandate. Never FLOAT.';
COMMENT ON COLUMN public.units.currency_code    IS 'ISO 4217 currency code. Defaults to ETB (Ethiopian Birr).';