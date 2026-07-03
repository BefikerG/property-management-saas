package com.propmanager.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Inbound payload for creating a new Organization (root tenant registration).
 *
 * Validation rules:
 *   - name is mandatory and may not be blank.
 *   - name is capped at 255 characters to match the database column length.
 *
 * The status field is intentionally absent from the request payload: every
 * newly registered organization starts as ACTIVE by definition. Status
 * transitions (e.g. SUSPENDED) are performed through a dedicated management
 * endpoint, not through the creation payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequestDto {

    @NotBlank(message = "Organization name must not be blank.")
    @Size(max = 255, message = "Organization name must not exceed 255 characters.")
    private String name;
}