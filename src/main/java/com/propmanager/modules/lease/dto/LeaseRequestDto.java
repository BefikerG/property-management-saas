package com.propmanager.modules.lease.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Inbound payload for creating a new Lease.
 *
 * Business rules enforced at DTO layer:
 *   - unitId and tenantProfileId are mandatory references.
 *   - monthlyRent must be greater than zero.
 *   - billingDay must be between 1 and 28 (avoids month-end ambiguity).
 *   - startDate and endDate are mandatory; date ordering (end > start)
 *     is enforced at the service layer since cross-field validation
 *     cannot be expressed cleanly with a single Bean Validation annotation.
 *
 * The initial status is always DRAFT — the client cannot specify
 * an arbitrary starting status. Status transitions happen through
 * dedicated endpoints (activate, terminate, expire).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseRequestDto {

    @NotNull(message = "Unit ID must not be null.")
    private UUID unitId;

    @NotNull(message = "Tenant profile ID must not be null.")
    private UUID tenantProfileId;

    @NotNull(message = "Start date must not be null.")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null.")
    private LocalDate endDate;

    @NotNull(message = "Monthly rent must not be null.")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than zero.")
    @Digits(integer = 13, fraction = 2,
            message = "Monthly rent must have at most 13 integer digits and 2 decimal places.")
    private BigDecimal monthlyRent;

    @Min(value = 1, message = "Billing day must be at least 1.")
    @Max(value = 28, message = "Billing day must not exceed 28.")
    private Short billingDay;

    @DecimalMin(value = "0.00", message = "Security deposit must be zero or greater.")
    @Digits(integer = 13, fraction = 2,
            message = "Security deposit must have at most 13 integer digits and 2 decimal places.")
    private BigDecimal securityDeposit;

    @Size(max = 2000, message = "Escalation terms must not exceed 2000 characters.")
    private String escalationTerms;
}