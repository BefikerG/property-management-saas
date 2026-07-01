package com.propmanager.core.auth.service;

import com.propmanager.core.auth.dto.AuthResponseDto;
import com.propmanager.core.auth.dto.LoginRequestDto;
import com.propmanager.core.auth.dto.RefreshTokenRequestDto;

/**
 * Service contract for authentication operations.
 *
 * Authentication endpoints are public — no @PreAuthorize annotations
 * are needed here because SecurityConfig explicitly permits all
 * requests to /api/v1/auth/** without a JWT.
 *
 * No @PreAuthorize on either method — by definition, a user calling
 * login has no token yet, and a user calling refresh holds an expired
 * access token. Spring Security's permitAll() rule in SecurityConfig
 * covers both endpoints at the filter chain level.
 */
public interface AuthService {

    /**
     * Authenticates a staff member by email and password.
     * On success, returns a fresh access token and refresh token
     * pair along with the authenticated staff member's identity context.
     *
     * @param requestDto the login credentials
     * @return a populated AuthResponseDto containing both tokens
     *         and the staff member's org context
     */
    AuthResponseDto login(LoginRequestDto requestDto);

    /**
     * Validates a refresh token and issues a new access token.
     * The refresh token itself is not rotated on this call —
     * the same refresh token remains valid until its own expiry.
     *
     * @param requestDto the refresh token submitted by the client
     * @return a populated AuthResponseDto with a fresh access token
     *         and the same refresh token
     */
    AuthResponseDto refresh(RefreshTokenRequestDto requestDto);
}