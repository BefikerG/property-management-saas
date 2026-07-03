package com.propmanager.modules.inventory.entity;

/**
 * Operational status of a Unit — the state machine governing
 * whether a unit may be leased, is currently tenanted, or is
 * temporarily unavailable.
 *
 * Valid transitions (TRD §2.2 — units table):
 *   VACANT      → OCCUPIED    (lease activation)
 *   OCCUPIED    → VACANT      (lease termination or expiry)
 *   VACANT      → MAINTENANCE (manual override by ADMINISTRATOR or PROPERTY_MANAGER)
 *   MAINTENANCE → VACANT      (manual override — repairs complete)
 *
 * Invalid transition (enforced at service layer):
 *   OCCUPIED → MAINTENANCE   rejected — a unit with an active lease
 *                             cannot be placed into maintenance until
 *                             the lease is terminated first.
 */
public enum UnitStatus {

    /**
     * The unit is unoccupied and available for lease assignment.
     * This is the default state for all newly created units.
     */
    VACANT,

    /**
     * The unit has an active lease. No new lease may be created
     * against this unit until the current lease is terminated or expires.
     * Set automatically by the lease activation workflow (Phase 2).
     */
    OCCUPIED,

    /**
     * The unit is temporarily unavailable due to repairs or maintenance.
     * Lease creation is blocked while this status is active.
     * Transition to this state is a manual override — no lease event triggers it.
     */
    MAINTENANCE
}