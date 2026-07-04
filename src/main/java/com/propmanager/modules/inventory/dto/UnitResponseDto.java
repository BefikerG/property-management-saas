package com.propmanager.modules.inventory.dto;

import com.propmanager.modules.inventory.entity.UnitStatus;
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
public class UnitResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private UUID          propertyId;
    private UUID          structureId;
    private String        unitNumber;
    private UnitStatus    status;
    private String        specification;
    private BigDecimal    baselinePrice;
    private String        currencyCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}