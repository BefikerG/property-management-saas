package com.propmanager.modules.organization.entity;

/**
 * Lifecycle status of a StaffMember account.
 *
 * Stored as VARCHAR via @Enumerated(EnumType.STRING).
 * The CHECK constraint on staff_members.status enforces that only
 * these two values ever reach the database.
 */
public enum StaffStatus {

    /**
     * The account is operational. The staff member may authenticate,
     * receive JWT tokens, and perform actions permitted by their role.
     */
    ACTIVE,

    /**
     * The account has been administratively deactivated. Login attempts
     * for this email are rejected at the UserDetailsService layer.
     * Existing JWT tokens become operationally invalid because
     * StaffMemberDetailsService checks status on every request.
     */
    DEACTIVATED
}