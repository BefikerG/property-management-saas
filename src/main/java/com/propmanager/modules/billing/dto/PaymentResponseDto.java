package com.propmanager.modules.billing.dto;

import com.propmanager.modules.billing.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private UUID          invoiceId;
    private BigDecimal    amount;
    private PaymentMethod paymentMethod;
    private String        reference;
    private String        notes;
    private LocalDateTime paidAt;
}