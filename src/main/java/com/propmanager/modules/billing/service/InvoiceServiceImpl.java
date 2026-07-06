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
    public List<InvoiceResponseDto> findAll() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching all invoices for org [{}]", tenantId);
        return invoiceRepository.findAllByTenantId(tenantId)
            .stream().map(invoiceMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> findAllByStatus(InvoiceStatus status) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return invoiceRepository.findAllByTenantIdAndStatus(tenantId, status)
            .stream().map(invoiceMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> findAllByLease(UUID leaseId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return invoiceRepository.findAllByLeaseIdAndTenantId(leaseId, tenantId)
            .stream().map(invoiceMapper::toResponseDto).toList();
    }
}