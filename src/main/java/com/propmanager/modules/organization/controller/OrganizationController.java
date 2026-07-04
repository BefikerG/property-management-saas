package com.propmanager.modules.organization.controller;

import com.propmanager.modules.organization.dto.OrganizationRequestDto;
import com.propmanager.modules.organization.dto.OrganizationResponseDto;
import com.propmanager.modules.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing Organization lifecycle endpoints.
 *
 * Base path: /api/v1/organizations
 *
 * Security note: @PreAuthorize is applied at the service interface level,
 * NOT here. This controller is intentionally free of security annotations —
 * the authorization layer belongs in the business contract, not in the
 * transport layer.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(
    name = "Organizations",
    description = "Root tenant registration and lifecycle management for property management firms."
)
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(
        summary = "Register a new organization",
        description = "Registers a new property management firm as a root tenant on the platform. " +
                      "The organization is created with ACTIVE status. This endpoint is publicly " +
                      "accessible — authentication is not required to register a new firm."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Organization registered successfully."),
        @ApiResponse(responseCode = "400", description = "Validation failure — name is blank or exceeds 255 characters."),
        @ApiResponse(responseCode = "409", description = "An organization with this name already exists."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponseDto> register(
        @Valid @RequestBody OrganizationRequestDto requestDto
    ) {
        OrganizationResponseDto response = organizationService.registerOrganization(requestDto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
        summary = "Get organization by ID",
        description = "Retrieves a single organization by its UUID. " +
                      "Accessible to platform administrators and organization administrators."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organization retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "No organization found with the supplied ID."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.findById(id));
    }

    @Operation(
        summary = "List all organizations",
        description = "Returns every registered organization on the platform. " +
                      "Restricted to platform-level administrators only."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organization list returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — platform admin role required."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<List<OrganizationResponseDto>> getAll() {
        return ResponseEntity.ok(organizationService.findAll());
    }

    @Operation(
        summary = "Suspend an organization",
        description = "Administratively suspends an active organization. " +
                      "Staff members belonging to this organization will be unable to " +
                      "perform any data operations while this status is in effect."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organization suspended successfully."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — platform admin role required."),
        @ApiResponse(responseCode = "404", description = "No organization found with the supplied ID."),
        @ApiResponse(responseCode = "409", description = "Organization is already suspended."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<OrganizationResponseDto> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.suspendOrganization(id));
    }

    @Operation(
        summary = "Reactivate an organization",
        description = "Reactivates a previously suspended organization, " +
                      "restoring full operational access for its staff members."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organization reactivated successfully."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — platform admin role required."),
        @ApiResponse(responseCode = "404", description = "No organization found with the supplied ID."),
        @ApiResponse(responseCode = "409", description = "Organization is already active."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<OrganizationResponseDto> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.reactivateOrganization(id));
    }
}