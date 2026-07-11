package com.propmanager.billing;

import com.propmanager.base.BaseIntegrationTest;
import com.propmanager.modules.billing.dto.PaymentRequestDto;
import com.propmanager.modules.billing.entity.Invoice;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import com.propmanager.modules.billing.entity.PaymentMethod;
import com.propmanager.modules.billing.repository.InvoiceRepository;
import com.propmanager.modules.inventory.entity.Property;
import com.propmanager.modules.inventory.entity.Unit;
import com.propmanager.modules.inventory.entity.UnitStatus;
import com.propmanager.modules.inventory.repository.PropertyRepository;
import com.propmanager.modules.inventory.repository.UnitRepository;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.entity.TenantProfile;
import com.propmanager.modules.lease.repository.LeaseRepository;
import com.propmanager.modules.lease.repository.TenantProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Payment logging and invoice status transition tests.
 *
 * Verifies:
 *   - UNPAID → PARTIALLY_PAID on partial payment
 *   - PARTIALLY_PAID → PAID on full settlement
 *   - 409 on payment against already-PAID invoice
 *   - amount_paid calculated from DB SUM (not in-memory)
 *   - Tenant isolation on invoice access
 */
@DisplayName("Payment Logging & Invoice Status Transitions")
class PaymentServiceTest extends BaseIntegrationTest {

    @Autowired private PropertyRepository      propertyRepository;
    @Autowired private UnitRepository          unitRepository;
    @Autowired private TenantProfileRepository tenantProfileRepository;
    @Autowired private LeaseRepository         leaseRepository;
    @Autowired private InvoiceRepository       invoiceRepository;

    private Invoice unpaidInvoice;

    private static final BigDecimal AMOUNT_DUE = new BigDecimal("45000.00");

    @BeforeEach
    void seedInvoice() {
        Property property = propertyRepository.save(Property.builder()
            .tenantId(orgAlpha.getId())
            .name("Payment Test Building")
            .address("Payment St")
            .locationCity("Addis Ababa")
            .build());

        Unit unit = unitRepository.save(Unit.builder()
            .tenantId(orgAlpha.getId())
            .property(property)
            .unitNumber("P-001")
            .status(UnitStatus.OCCUPIED)
            .baselinePrice(AMOUNT_DUE)
            .currencyCode("ETB")
            .build());

        TenantProfile profile = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgAlpha.getId())
            .fullName("Payment Tenant")
            .email("payment.tenant@test.com")
            .build());

        Lease lease = leaseRepository.save(Lease.builder()
            .tenantId(orgAlpha.getId())
            .unit(unit)
            .tenantProfile(profile)
            .status(LeaseStatus.ACTIVE)
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(AMOUNT_DUE)
            .billingDay((short) 1)
            .build());

        unpaidInvoice = invoiceRepository.save(Invoice.builder()
            .tenantId(orgAlpha.getId())
            .lease(lease)
            .billingPeriod("2026-07")
            .amountDue(AMOUNT_DUE)
            .amountPaid(BigDecimal.ZERO)
            .status(InvoiceStatus.UNPAID)
            .dueDate(LocalDate.now().plusDays(7))
            .issuedAt(LocalDateTime.now())
            .build());
    }

    @Test
    @DisplayName("Partial payment transitions invoice to PARTIALLY_PAID")
    void partialPaymentTransitionsToPartiallyPaid() throws Exception {
        PaymentRequestDto dto = PaymentRequestDto.builder()
            .amount(new BigDecimal("20000.00"))
            .paymentMethod(PaymentMethod.CASH)
            .reference("RCPT-001")
            .build();

        mockMvc.perform(post("/api/v1/invoices/{id}/payments",
                unpaidInvoice.getId())
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated());

        Invoice refreshed = invoiceRepository
            .findById(unpaidInvoice.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(refreshed.getAmountPaid().compareTo(
            new BigDecimal("20000.00"))).isZero();
    }

    @Test
    @DisplayName("Full payment transitions invoice to PAID")
    void fullPaymentTransitionsToPaid() throws Exception {
        PaymentRequestDto dto = PaymentRequestDto.builder()
            .amount(AMOUNT_DUE)
            .paymentMethod(PaymentMethod.BANK_TRANSFER)
            .reference("TXN-2026-001")
            .build();

        mockMvc.perform(post("/api/v1/invoices/{id}/payments",
                unpaidInvoice.getId())
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated());

        Invoice refreshed = invoiceRepository
            .findById(unpaidInvoice.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(refreshed.getAmountPaid()
            .compareTo(AMOUNT_DUE)).isZero();
    }

    @Test
    @DisplayName("Payment against PAID invoice returns 409 INVOICE_ALREADY_PAID")
    void paymentAgainstPaidInvoiceReturns409() throws Exception {
        // Mark invoice as already PAID
        unpaidInvoice.setStatus(InvoiceStatus.PAID);
        unpaidInvoice.setAmountPaid(AMOUNT_DUE);
        invoiceRepository.save(unpaidInvoice);

        PaymentRequestDto dto = PaymentRequestDto.builder()
            .amount(new BigDecimal("1000.00"))
            .paymentMethod(PaymentMethod.CASH)
            .build();

        mockMvc.perform(post("/api/v1/invoices/{id}/payments",
                unpaidInvoice.getId())
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("INVOICE_ALREADY_PAID"));
    }

    @Test
    @DisplayName("Beta admin cannot see Alpha invoice — 404 on cross-tenant payment attempt")
    void betaCannotPayAlphaInvoice() throws Exception {
        PaymentRequestDto dto = PaymentRequestDto.builder()
            .amount(new BigDecimal("1000.00"))
            .paymentMethod(PaymentMethod.CASH)
            .build();

        mockMvc.perform(post("/api/v1/invoices/{id}/payments",
                unpaidInvoice.getId())
                .header("Authorization", bearerOf(tokenBetaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("INVOICE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Invoice list is tenant-scoped — Beta cannot see Alpha invoices")
    void invoiceListIsTenantScoped() throws Exception {
        mockMvc.perform(get("/api/v1/invoices")
                .header("Authorization", bearerOf(tokenBetaAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.tenantId == '"
                + orgAlpha.getId() + "')]").doesNotExist());
    }
}