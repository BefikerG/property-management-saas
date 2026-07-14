package com.propmanager.modules.organization.service;

import com.propmanager.modules.organization.dto.StaffMemberRequestDto;
import com.propmanager.modules.organization.dto.StaffMemberResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for StaffMember lifecycle operations.
 *
 * All @PreAuthorize annotations live on this interface — not on the
 * controller or implementation — keeping authorization declarations
 * in the business contract where they are auditable and testable
 * in isolation from the transport layer (skill mandate).
 *
 * Role conventions:
 *   ROLE_ADMINISTRATOR    — full staff management authority within org
 *   ROLE_PROPERTY_MANAGER — operational access, no staff management
 *   ROLE_VIEWER           — read-only across all org data
 */
public interface StaffMemberService {

    /**
     * Invites a new staff member to the authenticated organization.
     * Only administrators may provision new accounts.
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    StaffMemberResponseDto createStaffMember(StaffMemberRequestDto requestDto);

    /**
     * Retrieves a single staff member by ID within the authenticated org.
     * Accessible to all authenticated roles.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    StaffMemberResponseDto findById(UUID id);

    /**
     * Returns all staff members belonging to the authenticated org.
     * Accessible to all authenticated roles.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<StaffMemberResponseDto> findAll(Pageable pageable);

    /**
     * Deactivates a staff member account within the authenticated org.
     * Only administrators may deactivate accounts.
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    StaffMemberResponseDto deactivateStaffMember(UUID id);

    /**
     * Reactivates a deactivated staff member account.
     * Only administrators may reactivate accounts.
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    StaffMemberResponseDto reactivateStaffMember(UUID id);

    /**
     * Changes the role assigned to a staff member.
     * Only administrators may alter role assignments.
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    StaffMemberResponseDto changeRole(UUID id, StaffMemberRequestDto requestDto);
}