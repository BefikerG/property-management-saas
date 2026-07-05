package com.propmanager.modules.billing.entity;

/**
 * Lifecycle status of an Invoice.
 *
 * Transitions:
 *   UNPAID → PARTIALLY_PAID  (payment logged, amount_paid < amount_due)
 *   UNPAID → PAID            (full payment logged in one transaction)
 *   PARTIALLY_PAID → PAID    (remaining balance fully settled)
 *
 * Transitions are calculated and applied automatically by
 * PaymentServiceImpl after every payment is logged. No manual
 * status override is permitted through the API.
 */
public enum InvoiceStatus {

    /**
     * No payment has been received against this invoice.
     * amount_paid = 0.00
     */
    UNPAID,

    /**
     * A partial payment has been received.
     * amount_paid > 0 but amount_paid < amount_due.
     */
    PARTIALLY_PAID,

    /**
     * The invoice has been fully settled.
     * amount_paid >= amount_due.
     */
    PAID
}