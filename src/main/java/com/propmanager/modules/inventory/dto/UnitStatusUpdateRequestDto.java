package com.propmanager.modules.inventory.dto;

import com.propmanager.modules.inventory.entity.UnitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for the PATCH /units/{id}/status endpoint.
 * Only VACANT and MAINTENANCE are valid manual override targets.
 * OCCUPIED is set exclusively by the lease activation workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitStatusUpdateRequestDto {

    @NotNull(message = "Status must not be null.")
    private UnitStatus status;
}