package com.propmanager.modules.organization.repository;

import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.entity.StaffStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the StaffMember aggregate.
 *
 * Tenant scoping:
 *   Every query that operates on tenant-scoped data includes tenant_id
 *   as an explicit parameter. The TenantFilterAspect additionally applies
 *   the Hibernate "tenantFilter" at the Session level before any of these
 *   methods execute, providing defense-in-depth isolation (TRD §3.2).
 *
 * The findByEmail method is the sole exception — it intentionally queries
 * across tenant boundaries. It is used exclusively by
 * StaffMemberDetailsService.loadUserByUsername() which must find a staff
 * member by email alone during JWT authentication, before a tenant context
 * has been established. This method is NOT called from any business service.
 *
 * No @Repository annotation: extending JpaRepository registers this
 * interface as a Spring bean automatically.
 */
public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {

    /**
     * Finds a staff member by email across ALL organizations.
     * Used ONLY by StaffMemberDetailsService during JWT authentication.
     * Must never be called from any business service method.
     *
     * @param email the login email address
     * @return the matching StaffMember, or empty if none exists
     */
    @Query("SELECT s FROM StaffMember s WHERE s.email = :email")
    Optional<StaffMember> findByEmail(@Param("email") String email);

    /**
     * Finds a staff member by ID within a specific organization.
     * The standard tenant-scoped lookup — always use this in business
     * services rather than the unscoped findById(UUID).
     *
     * @param id       the staff member's UUID
     * @param tenantId the authenticated organization's UUID
     * @return the matching StaffMember, or empty if not found or
     *         if it belongs to a different organization
     */
    @Query("SELECT s FROM StaffMember s WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<StaffMember> findByIdAndTenantId(
        @Param("id")       UUID id,
        @Param("tenantId") UUID tenantId
    );

    /**
     * Returns all staff members belonging to a specific organization,
     * ordered by creation date descending.
     *
     * @param tenantId the authenticated organization's UUID
     * @return list of staff members in the organization
     */
    @Query("SELECT s FROM StaffMember s WHERE s.tenantId = :tenantId ORDER BY s.createdAt DESC")
    Page<StaffMember> findAllByTenantId(
        @Param("tenantId") UUID tenantId,
        Pageable pageable
    );

    /**
     * Checks whether a staff member with the given email already exists
     * within a specific organization. Used for conflict detection before
     * inserting a new record.
     *
     * @param email    the email address to check
     * @param tenantId the organization to scope the check to
     * @return true if a staff member with this email exists in the org
     */
    @Query("SELECT COUNT(s) > 0 FROM StaffMember s WHERE s.email = :email AND s.tenantId = :tenantId")
    boolean existsByEmailAndTenantId(
        @Param("email")    String email,
        @Param("tenantId") UUID   tenantId
    );

    /**
     * Returns all staff members with a given status within an organization.
     *
     * @param tenantId the authenticated organization's UUID
     * @param status   the status to filter by
     * @return list of matching staff members
     */
    @Query("SELECT s FROM StaffMember s WHERE s.tenantId = :tenantId AND s.status = :status ORDER BY s.createdAt DESC")
    List<StaffMember> findAllByTenantIdAndStatus(
        @Param("tenantId") UUID        tenantId,
        @Param("status")   StaffStatus status
    );
}