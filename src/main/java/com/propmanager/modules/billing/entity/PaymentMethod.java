package com.propmanager.modules.billing.entity;

/**
 * Accepted payment methods for MVP manual payment logging.
 *
 * Digital payment gateway integration (Telebirr, etc.) is
 * explicitly out of scope for the MVP release per PRD v2.1
 * §8.1 Disclosed Assumptions.
 */
public enum PaymentMethod {
    CASH,
    CHECK,
    BANK_TRANSFER
}