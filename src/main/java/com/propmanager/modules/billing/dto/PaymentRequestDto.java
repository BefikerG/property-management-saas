package com.propmanager.modules.billing.dto;

import com.propmanager.modules.billing.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inbound payload for logging a payment against an invoice.
 *
 * The invoiceId is supplied as a path variable on the endpoint,
 * not in this payload. The tenantId is sourced from TenantContext.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "Amount must not be null.")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
    @Digits(integer = 13, fraction = 2,
            message = "Amount must have at most 13 integer digits and 2 decimal places.")
    private BigDecimal amount;

    @NotNull(message = "Payment method must not be null.")
    private PaymentMethod paymentMethod;

    @Size(max = 255, message = "Reference must not exceed 255 characters.")
    private String reference;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters.")
    private String notes;
}