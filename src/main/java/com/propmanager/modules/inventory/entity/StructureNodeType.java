package com.propmanager.modules.inventory.entity;

/**
 * Classification of an intermediate structural layer within a Property.
 *
 * Stored as VARCHAR via @Enumerated(EnumType.STRING).
 * The CHECK constraint on property_structures.node_type enforces
 * that only these values reach the database.
 */
public enum StructureNodeType {

    /** A named block within a property — e.g. "Block A", "Block B". */
    BLOCK,

    /** A floor level within a building — e.g. "Ground Floor", "2nd Floor". */
    FLOOR,

    /** A wing or section of a building — e.g. "East Wing", "North Wing". */
    WING,

    /** A named zone within a property — e.g. "Retail Zone", "Office Zone". */
    ZONE
}