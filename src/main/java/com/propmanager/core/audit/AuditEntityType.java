package com.propmanager.core.audit;

/**
 * Canonical entity type constants used as the entityType field
 * in AuditDomainEvent. Using constants rather than free-text strings
 * prevents typos and keeps the audit ledger queryable and consistent.
 */
public final class AuditEntityType {

    private AuditEntityType() {}

    public static final String ORGANIZATION    = "ORGANIZATION";
    public static final String STAFF_MEMBER    = "STAFF_MEMBER";
    public static final String PROPERTY        = "PROPERTY";
    public static final String UNIT            = "UNIT";
    public static final String TENANT_PROFILE  = "TENANT_PROFILE";
    public static final String LEASE           = "LEASE";
    public static final String INVOICE         = "INVOICE";
    public static final String PAYMENT         = "PAYMENT";
}