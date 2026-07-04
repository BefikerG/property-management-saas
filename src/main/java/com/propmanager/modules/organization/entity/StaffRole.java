package com.propmanager.modules.organization.entity;

/**
 * RBAC role assigned to a StaffMember within their Organization.
 *
 * Roles are tenant-scoped — they grant authority only within the
 * boundary of the staff member's own organization, never across
 * organizations (TRD §6 — Access Control Model).
 *
 * Stored as VARCHAR via @Enumerated(EnumType.STRING).
 * The CHECK constraint on staff_members.role enforces that only
 * these three values ever reach the database.
 *
 * Spring Security role conventions:
 *   When loaded into a UserDetails GrantedAuthority, each value is
 *   prefixed with "ROLE_" by StaffMemberDetailsService so that Spring
 *   Security's @PreAuthorize("hasRole('ADMINISTRATOR')") expressions
 *   resolve correctly against the "ROLE_ADMINISTRATOR" authority string.
 */
public enum StaffRole {

    /**
     * Full authority within the organization: staff provisioning, role
     * assignment, organization settings, and all create/read/update/delete
     * operations across every module.
     */
    ADMINISTRATOR,

    /**
     * Operational role: create, read, and update operations across
     * Properties, Units, Tenant Profiles, and Leases. Cannot manage
     * staff accounts or modify organization-level settings.
     */
    PROPERTY_MANAGER,

    /**
     * Read-only access to all organization data. Cannot perform any
     * create, update, or delete operation. Intended for ownership-level
     * oversight without operational risk.
     */
    VIEWER
}