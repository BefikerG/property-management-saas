package com.propmanager.modules.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response returned by the manual billing trigger endpoint.
 * Summarizes the result of a billing job execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRunResponseDto {

    private String        billingPeriod;
    private int           invoicesGenerated;
    private int           invoicesSkipped;
    private LocalDateTime executedAt;
    private String        status;
    private String        message;
}