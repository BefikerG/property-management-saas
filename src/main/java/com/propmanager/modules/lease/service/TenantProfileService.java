package com.propmanager.modules.lease.service;

import com.propmanager.modules.lease.dto.TenantProfileRequestDto;
import com.propmanager.modules.lease.dto.TenantProfileResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TenantProfileService {

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    TenantProfileResponseDto createTenantProfile(TenantProfileRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    TenantProfileResponseDto findById(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<TenantProfileResponseDto> findAll(Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<TenantProfileResponseDto> search(String query, Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    TenantProfileResponseDto updateTenantProfile(UUID id, TenantProfileRequestDto requestDto);

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    void deleteTenantProfile(UUID id);
}