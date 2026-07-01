package com.propmanager.core.auth.service;

import com.propmanager.core.auth.dto.AuthResponseDto;
import com.propmanager.core.auth.dto.LoginRequestDto;
import com.propmanager.core.auth.dto.RefreshTokenRequestDto;
import com.propmanager.core.exception.BaseException;
import com.propmanager.core.security.JwtService;
import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService.
 *
 * Login flow:
 *   1. AuthenticationManager.authenticate() validates the email/password
 *      pair against the BCrypt hash in the database via
 *      StaffMemberDetailsService. Throws BadCredentialsException on failure.
 *   2. The authenticated email is used to load the full StaffMember entity
 *      (needed for the org_id and role claims in the JWT).
 *   3. JwtService generates an access token (carrying org_id) and a
 *      refresh token (carrying subject only).
 *   4. An AuthResponseDto is returned carrying both tokens and the
 *      staff member's identity context for the client.
 *
 * Refresh flow:
 *   1. JwtService validates the structural integrity of the refresh token.
 *   2. The subject (email) is extracted and used to load UserDetails
 *      and the full StaffMember entity.
 *   3. The token is validated against the loaded UserDetails.
 *   4. A new access token is generated. The refresh token is unchanged.
 *
 * Why StaffMemberRepository is injected directly here:
 *   After Spring Security's AuthenticationManager validates credentials,
 *   we need the StaffMember JPA entity (not just UserDetails) to read
 *   the tenantId and role for the JWT org_id claim and the response body.
 *   UserDetailsService only returns a UserDetails object — it does not
 *   expose the underlying StaffMember fields.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager  authenticationManager;
    private final JwtService             jwtService;
    private final StaffMemberRepository  staffMemberRepository;
    private final UserDetailsService     userDetailsService;

    // ── Login ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto requestDto) {
        log.info("Login attempt for email: [{}]", requestDto.getEmail());

        // Step 1 — Delegate credential validation to Spring Security.
        // This triggers StaffMemberDetailsService.loadUserByUsername()
        // internally and validates the BCrypt password hash.
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    requestDto.getEmail(),
                    requestDto.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            log.warn("Login failed for email: [{}] — bad credentials.", requestDto.getEmail());
            throw new AuthenticationFailedException(
                "INVALID_CREDENTIALS",
                "The email address or password you entered is incorrect."
            );
        }

        // Step 2 — Load the full StaffMember entity for org_id and role.
        StaffMember staffMember = staffMemberRepository
            .findByEmail(requestDto.getEmail())
            .orElseThrow(() -> new AuthenticationFailedException(
                "STAFF_MEMBER_NOT_FOUND",
                "Authenticated staff member could not be located."
            ));

        // Step 3 — Load UserDetails for token generation.
        UserDetails userDetails = userDetailsService
            .loadUserByUsername(requestDto.getEmail());

        // Step 4 — Generate both tokens.
        String accessToken  = jwtService.generateAccessToken(userDetails, staffMember.getTenantId());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Login successful. Email: [{}], Org: [{}], Role: [{}]",
            staffMember.getEmail(),
            staffMember.getTenantId(),
            staffMember.getRole());

        return AuthResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .organizationId(staffMember.getTenantId())
            .email(staffMember.getEmail())
            .role(staffMember.getRole().name())
            .build();
    }

    // ── Refresh ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto refresh(RefreshTokenRequestDto requestDto) {
        log.debug("Token refresh requested.");

        // Step 1 — Validate structural integrity of the refresh token.
        if (!jwtService.isTokenStructurallyValid(requestDto.getRefreshToken())) {
            throw new AuthenticationFailedException(
                "INVALID_REFRESH_TOKEN",
                "The refresh token is invalid or has expired."
            );
        }

        // Step 2 — Extract subject and load UserDetails + StaffMember.
        String email = jwtService.extractUsername(requestDto.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Step 3 — Validate token against loaded UserDetails.
        if (!jwtService.isTokenValid(requestDto.getRefreshToken(), userDetails)) {
            throw new AuthenticationFailedException(
                "REFRESH_TOKEN_VALIDATION_FAILED",
                "The refresh token could not be validated."
            );
        }

        // Step 4 — Load full StaffMember for org_id claim on new access token.
        StaffMember staffMember = staffMemberRepository
            .findByEmail(email)
            .orElseThrow(() -> new AuthenticationFailedException(
                "STAFF_MEMBER_NOT_FOUND",
                "Staff member associated with refresh token could not be located."
            ));

        // Step 5 — Issue new access token. Refresh token is unchanged.
        String newAccessToken = jwtService.generateAccessToken(
            userDetails,
            staffMember.getTenantId()
        );

        log.info("Token refreshed successfully for email: [{}], Org: [{}]",
            email, staffMember.getTenantId());

        return AuthResponseDto.builder()
            .accessToken(newAccessToken)
            .refreshToken(requestDto.getRefreshToken())
            .organizationId(staffMember.getTenantId())
            .email(staffMember.getEmail())
            .role(staffMember.getRole().name())
            .build();
    }

    // ── Inner exception type ──────────────────────────────────────────

    /**
     * Thrown when login credentials are invalid or a refresh token
     * fails validation. Maps to HTTP 401 Unauthorized via the
     * GlobalExceptionHandler extension below.
     */
    public static class AuthenticationFailedException extends BaseException {
        public AuthenticationFailedException(String errorCode, String message) {
            super(errorCode, message);
        }
    }
}