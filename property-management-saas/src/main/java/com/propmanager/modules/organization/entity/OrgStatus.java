package com.propmanager.modules.organization.entity;

/**
 * Lifecycle states for an Organization (root tenant boundary).
 *
 * Stored as a VARCHAR string in PostgreSQL via @Enumerated(EnumType.STRING).
 * The CHECK constraint on organizations.status enforces that only these
 * two values ever reach the database.
 */
public enum OrgStatus {

    /**
     * The organization is operational. Staff members may log in, manage
     * properties, execute leases, and trigger billing cycles.
     */
    ACTIVE,

    /**
     * The organization has been administratively suspended. All JWT tokens
     * belonging to its staff members are effectively invalidated at the
     * service layer — no data operations are permitted while this status
     * is in effect.
     */
    SUSPENDED
}