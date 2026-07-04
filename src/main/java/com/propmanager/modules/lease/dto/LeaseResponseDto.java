package com.propmanager.modules.lease.dto;

import com.propmanager.modules.lease.entity.LeaseStatus;
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
public class LeaseResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private UUID          unitId;
    private UUID          tenantProfileId;
    private LeaseStatus   status;
    private LocalDate     startDate;
    private LocalDate     endDate;
    private BigDecimal    monthlyRent;
    private Short         billingDay;
    private BigDecimal    securityDeposit;
    private String        escalationTerms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}