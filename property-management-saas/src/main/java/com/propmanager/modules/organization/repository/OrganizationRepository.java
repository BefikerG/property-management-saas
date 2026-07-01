package com.propmanager.modules.organization.repository;

import com.propmanager.modules.organization.entity.OrgStatus;
import com.propmanager.modules.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Organization aggregate.
 *
 * Note on tenant scoping: Organization is the ONLY entity in this system
 * that does NOT carry a tenant_id filter. It IS the tenant boundary.
 * The Hibernate @Filter and TenantFilterAspect introduced in the next step
 * (core/tenant/) will NOT be applied to this repository — organizations are
 * System-Wide, not Tenant-Scoped, per the TRD §2.2 data dictionary.
 *
 * All JPQL queries use named parameters exclusively — never string
 * concatenation (TRD §3 / skill mandate).
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    /**
     * Finds an organization by its exact name.
     * Used during registration to enforce the UNIQUE name constraint at the
     * service layer before hitting the database constraint, producing a
     * cleaner 409 Conflict response rather than a raw constraint violation.
     */
    @Query("SELECT o FROM Organization o WHERE o.name = :name")
    Optional<Organization> findByName(@Param("name") String name);

    /**
     * Returns all organizations currently in a given lifecycle status.
     * Primarily used by platform-level administrative operations.
     */
    @Query("SELECT o FROM Organization o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Organization> findAllByStatus(@Param("status") OrgStatus status);
}