package com.propmanager.core.auth.controller;

import com.propmanager.core.auth.dto.AuthResponseDto;
import com.propmanager.core.auth.dto.LoginRequestDto;
import com.propmanager.core.auth.dto.RefreshTokenRequestDto;
import com.propmanager.core.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints.
 *
 * Base path: /api/v1/auth
 *
 * Both endpoints are explicitly permitted in SecurityConfig without
 * a JWT requirement — they are the entry point for obtaining tokens.
 *
 * No @PreAuthorize annotations here or on AuthService — authentication
 * endpoints are public by definition. Authorization belongs on the
 * business services that require an authenticated context to operate.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name        = "Authentication",
    description = "Token issuance and renewal for authenticated platform access."
)
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary     = "Login",
        description = "Authenticates a staff member by email and password. " +
                      "On success, returns a short-lived access token (24h) and a " +
                      "long-lived refresh token (7d), along with the authenticated " +
                      "staff member's organization context and assigned role. " +
                      "This endpoint is publicly accessible — no JWT is required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful. Access and refresh tokens returned."),
        @ApiResponse(responseCode = "400", description = "Validation failure — email or password is blank or malformed."),
        @ApiResponse(responseCode = "401", description = "Invalid credentials — email not found or password incorrect."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
        @Valid @RequestBody LoginRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    @Operation(
        summary     = "Refresh access token",
        description = "Accepts a valid refresh token and issues a new short-lived " +
                      "access token without requiring the user to re-enter credentials. " +
                      "The refresh token itself is not rotated — the same refresh token " +
                      "is returned alongside the new access token. " +
                      "This endpoint is publicly accessible — no JWT is required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token issued successfully."),
        @ApiResponse(responseCode = "400", description = "Validation failure — refresh token is blank."),
        @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or could not be validated."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
        @Valid @RequestBody RefreshTokenRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.refresh(requestDto));
    }
}