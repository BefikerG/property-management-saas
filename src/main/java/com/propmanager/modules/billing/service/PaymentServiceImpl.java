package com.propmanager.modules.billing.service;

import com.propmanager.core.exception.ConflictException;
import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.core.tenant.TenantContext;
import com.propmanager.modules.billing.dto.PaymentRequestDto;
import com.propmanager.modules.billing.dto.PaymentResponseDto;
import com.propmanager.modules.billing.entity.Invoice;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import com.propmanager.modules.billing.entity.Payment;
import com.propmanager.modules.billing.mapper.PaymentMapper;
import com.propmanager.modules.billing.repository.InvoiceRepository;
import com.propmanager.modules.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of PaymentService.
 *
 * Payment logging flow:
 *   1. Resolve the invoice (tenant-scoped, 404 on mismatch)
 *   2. Reject if invoice is already PAID (409)
 *   3. Persist the Payment entity
 *   4. Recalculate invoice.amount_paid by summing ALL payments
 *      via PaymentRepository.sumAmountByInvoiceId()
 *   5. Transition invoice status:
 *        amount_paid == 0           → UNPAID
 *        0 < amount_paid < amount_due → PARTIALLY_PAID
 *        amount_paid >= amount_due  → PAID
 *   6. Save the updated invoice
 *
 * Financial arithmetic (TRD §4.1):
 *   All calculations use BigDecimal with RoundingMode.HALF_EVEN
 *   (Banker's Rounding). No double or float arithmetic anywhere.
 *   The sum is fetched from the database via COALESCE(SUM(...), 0)
 *   to ensure correctness under concurrent payment logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository  paymentRepository;
    private final InvoiceRepository  invoiceRepository;
    private final PaymentMapper      paymentMapper;

    @Override
    @Transactional
    public PaymentResponseDto logPayment(UUID invoiceId, PaymentRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Logging payment of [{}] ETB against invoice [{}] for org [{}]",
            requestDto.getAmount(), invoiceId, tenantId);

        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "INVOICE_NOT_FOUND",
                "No invoice found with ID: " + invoiceId
            ));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ConflictException(
                "INVOICE_ALREADY_PAID",
                "Invoice [" + invoiceId + "] has already been fully paid."
            );
        }

        // ── Step 3: Persist payment ───────────────────────────────────
        Payment payment = paymentMapper.toEntity(requestDto);
        payment.setTenantId(tenantId);
        payment.setInvoice(invoice);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // ── Step 4: Recalculate amount_paid from database sum ─────────
        // Fetch the authoritative sum from the database rather than
        // adding in memory — prevents drift under concurrent payments.
        BigDecimal totalPaid = paymentRepository
            .sumAmountByInvoiceId(invoiceId)
            .setScale(2, RoundingMode.HALF_EVEN);

        // ── Step 5: Transition invoice status ─────────────────────────
        invoice.setAmountPaid(totalPaid);
        invoice.setStatus(calculateStatus(totalPaid, invoice.getAmountDue()));
        invoiceRepository.save(invoice);

        log.info("Payment logged. Invoice [{}] new status: [{}], " +
                 "amount_paid: [{}], amount_due: [{}]",
            invoiceId, invoice.getStatus(), totalPaid, invoice.getAmountDue());

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> findAllByInvoice(UUID invoiceId) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "INVOICE_NOT_FOUND",
                "No invoice found with ID: " + invoiceId
            ));

        return paymentRepository.findAllByInvoiceIdAndTenantId(invoiceId, tenantId)
            .stream().map(paymentMapper::toResponseDto).toList();
    }

    // ── Private Helpers ──────────────────────────────────────────────

    /**
     * Calculates the correct InvoiceStatus based on BigDecimal comparison.
     * Uses compareTo() — never equals() on BigDecimal (scale-sensitive).
     * RoundingMode.HALF_EVEN (Banker's Rounding) per TRD §4.1.
     */
    private InvoiceStatus calculateStatus(BigDecimal totalPaid, BigDecimal amountDue) {
        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            return InvoiceStatus.UNPAID;
        }
        if (totalPaid.compareTo(amountDue) >= 0) {
            return InvoiceStatus.PAID;
        }
        return InvoiceStatus.PARTIALLY_PAID;
    }
}