package com.propmanager.core.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for the POST /api/v1/auth/refresh endpoint.
 *
 * The client submits the refresh token it received at login.
 * The service validates it, loads the staff member, and issues
 * a new access token without requiring the user to re-enter
 * their credentials.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token must not be blank.")
    private String refreshToken;
}