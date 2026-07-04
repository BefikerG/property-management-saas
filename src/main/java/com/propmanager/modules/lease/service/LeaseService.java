package com.propmanager.modules.lease.service;

import com.propmanager.modules.lease.dto.LeaseRequestDto;
import com.propmanager.modules.lease.dto.LeaseResponseDto;
import com.propmanager.modules.lease.entity.LeaseStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface LeaseService {

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    LeaseResponseDto createLease(LeaseRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    LeaseResponseDto findById(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<LeaseResponseDto> findAll();

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<LeaseResponseDto> findAllByStatus(LeaseStatus status);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<LeaseResponseDto> findAllByUnit(UUID unitId);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<LeaseResponseDto> findAllByTenantProfile(UUID tenantProfileId);

    /**
     * Activates a DRAFT or PENDING lease.
     * Atomically transitions unit status → OCCUPIED within the same transaction.
     * Enforces the Single Active Lease Rule — rejects if the unit already
     * has an ACTIVE lease.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    LeaseResponseDto activateLease(UUID id);

    /**
     * Terminates an ACTIVE lease before its natural end_date.
     * Atomically transitions unit status → VACANT within the same transaction.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    LeaseResponseDto terminateLease(UUID id);

    /**
     * Marks an ACTIVE lease as EXPIRED (natural end_date reached).
     * Atomically transitions unit status → VACANT within the same transaction.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    LeaseResponseDto expireLease(UUID id);
}