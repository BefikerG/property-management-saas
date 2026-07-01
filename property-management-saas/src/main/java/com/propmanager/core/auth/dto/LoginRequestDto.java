package com.propmanager.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for the POST /api/v1/auth/login endpoint.
 *
 * Only email and password are accepted — the tenant organization is
 * determined from the authenticated StaffMember's own record, not
 * from any client-supplied field. This prevents a client from
 * claiming membership in an arbitrary organization by simply
 * including a foreign org_id in their login payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Email must be a valid email address.")
    private String email;

    @NotBlank(message = "Password must not be blank.")
    private String password;
}