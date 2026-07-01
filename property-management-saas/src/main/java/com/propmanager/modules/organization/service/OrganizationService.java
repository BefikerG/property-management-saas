package com.propmanager.modules.organization.service;

import com.propmanager.modules.organization.dto.OrganizationRequestDto;
import com.propmanager.modules.organization.dto.OrganizationResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Organization lifecycle operations.
 *
 * Security annotations live on this interface, not on the implementation,
 * keeping authorization declarations in the business contract layer where
 * they are immediately visible to any engineer reading the API surface.
 *
 * Role conventions (to be expanded when the full RBAC model is wired
 * in core/security/):
 *   ROLE_PLATFORM_ADMIN — platform-level operator managing tenant firms.
 *   ROLE_ORG_ADMIN      — administrator of a specific organization.
 */
public interface OrganizationService {

    /**
     * Registers a new property management firm as a root tenant organization.
     * The first staff member (Administrator) for this organization is
     * provisioned in a subsequent step once the StaffMember entity is built.
     */
    @PreAuthorize("permitAll()")
    OrganizationResponseDto registerOrganization(OrganizationRequestDto requestDto);

    /**
     * Retrieves a single organization by its UUID.
     * Returns 404 Not Found if the ID does not match any registered firm.
     */
    @PreAuthorize("hasAnyRole('ROLE_PLATFORM_ADMIN', 'ROLE_ORG_ADMIN')")
    OrganizationResponseDto findById(UUID id);

    /**
     * Returns all registered organizations on the platform.
     * Restricted to platform-level administrators only.
     */
    @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
    List<OrganizationResponseDto> findAll();

    /**
     * Suspends an active organization. All associated staff member JWT
     * tokens become operationally invalid once this status is set.
     */
    @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
    OrganizationResponseDto suspendOrganization(UUID id);

    /**
     * Reactivates a previously suspended organization.
     */
    @PreAuthorize("hasRole('ROLE_PLATFORM_ADMIN')")
    OrganizationResponseDto reactivateOrganization(UUID id);
}

