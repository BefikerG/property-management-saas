package com.propmanager.core.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Outbound payload returned by a successful login or token refresh.
 *
 * Fields:
 *   accessToken      — short-lived JWT (24h). Sent as a Bearer token
 *                      in the Authorization header on every subsequent
 *                      authenticated request.
 *   refreshToken     — long-lived JWT (7d). Used exclusively to obtain
 *                      a new accessToken via POST /api/v1/auth/refresh.
 *                      It carries no org_id claim and cannot authorize
 *                      any data access directly.
 *   tokenType        — always "Bearer". Included so clients can
 *                      construct the Authorization header value without
 *                      hardcoding the scheme.
 *   organizationId   — the authenticated staff member's org UUID.
 *                      Returned as a convenience so the client can
 *                      identify which organization's context is active
 *                      without decoding the JWT payload.
 *   email            — the authenticated staff member's email.
 *   role             — the authenticated staff member's assigned role.
 *                      Allows the client to render the correct UI
 *                      without a separate /me endpoint call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private UUID   organizationId;
    private String email;
    private String role;
}