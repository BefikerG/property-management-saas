package com.propmanager.core.audit;

/**
 * Canonical action type constants used as the actionType field
 * in AuditDomainEvent. Maps to the CHECK constraint defined in
 * V011__create_system_audit_logs_table.sql.
 *
 * Every value here must match a value in the database CHECK constraint.
 */
public final class AuditActionType {

    private AuditActionType() {}

    public static final String CREATE          = "CREATE";
    public static final String UPDATE          = "UPDATE";
    public static final String DELETE          = "DELETE";
    public static final String ACTIVATE        = "ACTIVATE";
    public static final String TERMINATE       = "TERMINATE";
    public static final String EXPIRE          = "EXPIRE";
    public static final String PAYMENT_LOGGED  = "PAYMENT_LOGGED";
    public static final String STATUS_CHANGE   = "STATUS_CHANGE";
    public static final String ROLE_CHANGE     = "ROLE_CHANGE";
    public static final String DEACTIVATE      = "DEACTIVATE";
    public static final String REACTIVATE      = "REACTIVATE";
}