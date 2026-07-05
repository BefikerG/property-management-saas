package com.propmanager.modules.billing.dto;

import com.propmanager.modules.billing.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private UUID          leaseId;
    private String        billingPeriod;
    private BigDecimal    amountDue;
    private BigDecimal    amountPaid;
    private BigDecimal    balance;
    private InvoiceStatus status;
    private LocalDate     dueDate;
    private LocalDateTime issuedAt;
}