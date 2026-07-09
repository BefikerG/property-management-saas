package com.propmanager.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
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

    // Optional admin bootstrap fields
    @Email(message = "Admin email must be a valid email address.")
    @Size(max = 320, message = "Admin email must not exceed 320 characters.")
    @Schema(description = "Optional admin email for bootstrap; if provided, an ADMINISTRATOR staff member is created.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String adminEmail;

    @Size(min = 8, message = "Admin password must be at least 8 characters.")
    @Schema(description = "Optional admin password for bootstrap; required if adminEmail is provided.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String adminPassword;

    @Size(max = 255, message = "Admin full name must not exceed 255 characters.")
    @Schema(description = "Optional admin full name for bootstrap.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String adminFullName;
}