package com.propmanager.modules.organization.dto;

import com.propmanager.modules.organization.entity.StaffRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for inviting a new staff member to an organization.
 *
 * Validation rules:
 *   - email: mandatory, must be a valid email format, max 320 chars.
 *   - password: mandatory, minimum 8 characters. The plain-text value
 *     is accepted here and hashed by StaffMemberServiceImpl before
 *     persistence — it is never stored or logged in plain text.
 *   - fullName: mandatory, max 255 characters.
 *   - role: mandatory. The client must explicitly assign a role —
 *     no silent defaulting at the DTO layer to prevent accidental
 *     privilege escalation through omission.
 *
 * The tenantId field is intentionally absent from this DTO. The tenant
 * boundary is always sourced from TenantContext (populated by the JWT
 * filter), never from the request body — per TRD §3.1.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffMemberRequestDto {

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Email must be a valid email address.")
    @Size(max = 320, message = "Email must not exceed 320 characters.")
    private String email;

    @NotBlank(message = "Password must not be blank.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    @NotBlank(message = "Full name must not be blank.")
    @Size(max = 255, message = "Full name must not exceed 255 characters.")
    private String fullName;

    @NotNull(message = "Role must not be null.")
    private StaffRole role;
}