package com.propmanager.modules.billing.service;

import com.propmanager.modules.billing.dto.InvoiceResponseDto;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    InvoiceResponseDto findById(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<InvoiceResponseDto> findAll();

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<InvoiceResponseDto> findAllByStatus(InvoiceStatus status);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<InvoiceResponseDto> findAllByLease(UUID leaseId);
}