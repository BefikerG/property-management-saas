package com.propmanager.modules.billing.service;

import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.core.tenant.TenantContext;
import com.propmanager.modules.billing.dto.InvoiceResponseDto;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import com.propmanager.modules.billing.mapper.InvoiceMapper;
import com.propmanager.modules.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper     invoiceMapper;

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto findById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching invoice [{}] for org [{}]", id, tenantId);
        return invoiceMapper.toResponseDto(
            invoiceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "INVOICE_NOT_FOUND",
                    "No invoice found with ID: " + id
                ))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> findAll(Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching all invoices for org [{}]", tenantId);
        return invoiceRepository.findAllByTenantId(tenantId, pageable)
            .map(invoiceMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> findAllByStatus(InvoiceStatus status, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return invoiceRepository.findAllByTenantIdAndStatus(tenantId, status, pageable)
            .map(invoiceMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> findAllByLease(UUID leaseId, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return invoiceRepository.findAllByLeaseIdAndTenantId(leaseId, tenantId, pageable)
            .map(invoiceMapper::toResponseDto);
    }
}