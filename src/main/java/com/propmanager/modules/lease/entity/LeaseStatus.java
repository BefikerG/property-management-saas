package com.propmanager.modules.lease.entity;

/**
 * Lifecycle states for a Lease (TRD §2.2 — leases table).
 *
 * Valid progression:
 *   DRAFT     → PENDING    (lease prepared, awaiting activation)
 *   PENDING   → ACTIVE     (lease activated — unit transitions to OCCUPIED)
 *   DRAFT     → ACTIVE     (direct activation without pending stage)
 *   ACTIVE    → TERMINATED (early termination — unit transitions to VACANT)
 *   ACTIVE    → EXPIRED    (natural end_date reached — unit transitions to VACANT)
 *
 * The transition ACTIVE → TERMINATED and ACTIVE → EXPIRED are the two
 * events that release a unit back to VACANT status, making it available
 * for new tenant placement.
 *
 * DRAFT and PENDING leases do not affect unit status — a unit may have
 * a DRAFT or PENDING lease while remaining VACANT, allowing future-dated
 * lease preparation without blocking the unit for immediate assignment.
 *
 * Stored as VARCHAR via @Enumerated(EnumType.STRING).
 * The CHECK constraint on leases.status enforces only these five values.
 */
public enum LeaseStatus {

    /**
     * Lease has been prepared but not yet formally agreed upon.
     * Unit status is unaffected — unit remains VACANT.
     */
    DRAFT,

    /**
     * Lease has been agreed upon but the start_date has not yet arrived.
     * Unit status is unaffected — unit remains VACANT.
     */
    PENDING,

    /**
     * Lease is currently in force. The partial unique index
     * idx_unique_active_lease_per_unit enforces that only one ACTIVE
     * lease may exist per unit at any moment.
     * Unit status is OCCUPIED.
     */
    ACTIVE,

    /**
     * Lease was ended before its agreed end_date.
     * Unit status transitions atomically to VACANT on termination.
     */
    TERMINATED,

    /**
     * Lease ran its full natural course to end_date.
     * Unit status transitions atomically to VACANT on expiry.
     */
    EXPIRED
}