package com.propmanager.modules.billing.service;

import com.propmanager.modules.billing.dto.InvoiceResponseDto;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    InvoiceResponseDto findById(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<InvoiceResponseDto> findAll(Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<InvoiceResponseDto> findAllByStatus(InvoiceStatus status, Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<InvoiceResponseDto> findAllByLease(UUID leaseId, Pageable pageable);
}