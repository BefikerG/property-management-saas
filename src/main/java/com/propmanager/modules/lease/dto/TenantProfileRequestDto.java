package com.propmanager.modules.lease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for creating or updating a TenantProfile.
 *
 * tenant_id is intentionally absent — always sourced from
 * TenantContext (JWT), never from the request body (TRD §3.1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProfileRequestDto {

    @NotBlank(message = "Full name must not be blank.")
    @Size(max = 255, message = "Full name must not exceed 255 characters.")
    private String fullName;

    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Email must be a valid email address.")
    @Size(max = 320, message = "Email must not exceed 320 characters.")
    private String email;

    @Size(max = 30, message = "Phone must not exceed 30 characters.")
    private String phone;

    @Size(max = 255, message = "Identification reference must not exceed 255 characters.")
    private String identificationReference;
}