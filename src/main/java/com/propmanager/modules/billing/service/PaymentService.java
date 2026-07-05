package com.propmanager.modules.billing.service;

import com.propmanager.modules.billing.dto.PaymentRequestDto;
import com.propmanager.modules.billing.dto.PaymentResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    /**
     * Logs a manual payment against an open invoice.
     * Automatically recalculates invoice.amount_paid and
     * transitions invoice.status after the payment is saved.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PaymentResponseDto logPayment(UUID invoiceId, PaymentRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<PaymentResponseDto> findAllByInvoice(UUID invoiceId);
}