package com.propmanager.modules.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitRequestDto {

    @NotBlank(message = "Unit number must not be blank.")
    @Size(max = 50, message = "Unit number must not exceed 50 characters.")
    private String unitNumber;

    @Size(max = 2000, message = "Specification must not exceed 2000 characters.")
    private String specification;

    /**
     * BigDecimal on the DTO — enforces exact decimal input.
     * Must be non-negative per database CHECK constraint.
     */
    @NotNull(message = "Baseline price must not be null.")
    @DecimalMin(value = "0.00", message = "Baseline price must be zero or greater.")
    @Digits(integer = 13, fraction = 2,
            message = "Baseline price must have at most 13 integer digits and 2 decimal places.")
    private BigDecimal baselinePrice;

    @Size(max = 3, message = "Currency code must not exceed 3 characters.")
    private String currencyCode;

    /**
     * Optional structure UUID. Null means unit attaches directly to property.
     */
    private UUID structureId;
}