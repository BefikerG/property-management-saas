package com.propmanager.core.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Abstract mapped superclass inherited by every Tenant-Scoped JPA entity
 * in the platform (TRD §2.2 — Tenant-Scoped isolation boundary).
 *
 * What this class provides:
 *
 *   1. tenant_id column — maps to the org_id foreign key present on every
 *      tenant-scoped table in the public schema. Declared updatable = false
 *      because a record's organizational ownership must never change after
 *      creation; moving data between organizations is not a supported
 *      operation in this platform.
 *
 *   2. @FilterDef — declares the Hibernate named filter "tenantFilter" and
 *      its single parameter "tenantId" of type UUID. This declaration must
 *      live on the superclass so it is registered with Hibernate once and
 *      inherited by every subclass entity automatically.
 *
 *   3. @Filter — applies the declared filter to every subclass entity with
 *      the SQL condition tenant_id = :tenantId. When the filter is enabled
 *      on a Hibernate Session (which TenantFilterAspect does before every
 *      repository call), this condition is automatically appended to every
 *      SELECT, UPDATE, and DELETE statement targeting that entity's table.
 *
 * What this class intentionally does NOT provide:
 *   - id, createdAt, updatedAt — each concrete entity declares its own
 *     primary key and timestamp fields because their column names and
 *     generation strategies may differ (e.g. UUID vs BIGSERIAL on audit_log).
 *
 * Which entities inherit this class (TRD §2.2 data dictionary):
 *   - Property         (public.properties)
 *   - PropertyStructure (public.property_structures)
 *   - Unit             (public.units)
 *   - Lease            (public.leases)
 *   - Invoice          (public.invoices)
 *   - SystemAuditLog   (public.system_audit_logs)
 *
 * Which entity does NOT inherit this class:
 *   - Organization     (public.organizations) — it IS the tenant boundary.
 *     Applying TenantEntity to Organization would be architecturally incorrect
 *     and would cause the filter to block every organization lookup.
 *
 * Lombok note:
 *   @SuperBuilder is used instead of @Builder because @Builder on a subclass
 *   that extends a @MappedSuperclass does not propagate the superclass fields
 *   into the generated builder. @SuperBuilder + @SuperBuilder on each concrete
 *   subclass produces a correct, complete builder chain.
 */
@MappedSuperclass
@FilterDef(
    name   = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
@Filter(
    name      = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class TenantEntity {

    /**
     * Foreign key to public.organizations(id).
     *
     * Nullable = false: every tenant-scoped record must belong to exactly
     * one organization. A record without a tenant_id is architecturally
     * invalid — it would be invisible to all tenants and unreachable by any
     * API endpoint.
     *
     * Updatable = false: organizational ownership is permanent. Once a
     * Property, Unit, or Lease is created under an organization, it cannot
     * be reassigned to a different organization.
     *
     * Insertable = true (default): the application layer is responsible for
     * setting this field from TenantContext at entity creation time. It is
     * never derived from the request body.
     */
    @Column(
        name       = "tenant_id",
        nullable   = false,
        updatable  = false
    )
    private UUID tenantId;
}