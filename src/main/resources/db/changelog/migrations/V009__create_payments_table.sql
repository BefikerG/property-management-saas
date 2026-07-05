-- ============================================================
-- Changeset V009: Create payments table
-- Schema  : public
-- Author  : Befiker Gezahegn Hailemichael
-- Purpose : Manual payment receipts logged by property managers
--           against open invoices. MVP supports Cash, Check,
--           and Bank Transfer. No digital gateway integration.
--
-- All monetary columns use NUMERIC(15,2) per TRD §4.1.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.payments
(
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    tenant_id      UUID          NOT NULL,
    invoice_id     UUID          NOT NULL,
    amount         NUMERIC(15,2) NOT NULL,
    payment_method VARCHAR(30)   NOT NULL,
    reference      VARCHAR(255),
    notes          TEXT,
    paid_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments
        PRIMARY KEY (id),

    CONSTRAINT fk_payments_organization
        FOREIGN KEY (tenant_id)
        REFERENCES public.organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES public.invoices (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_payments_method
        CHECK (payment_method IN ('CASH', 'CHECK', 'BANK_TRANSFER')),

    CONSTRAINT chk_payments_amount_positive
        CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_payments_tenant_id
    ON public.payments (tenant_id);

CREATE INDEX IF NOT EXISTS idx_payments_invoice_id
    ON public.payments (tenant_id, invoice_id);

COMMENT ON TABLE  public.payments                 IS 'Manual payment receipts logged against invoices by property managers.';
COMMENT ON COLUMN public.payments.amount          IS 'NUMERIC(15,2) — never FLOAT or DOUBLE. TRD §4.1.';
COMMENT ON COLUMN public.payments.payment_method  IS 'MVP payment methods: CASH, CHECK, BANK_TRANSFER.';
COMMENT ON COLUMN public.payments.reference       IS 'Optional cheque number, bank transaction reference, etc.';