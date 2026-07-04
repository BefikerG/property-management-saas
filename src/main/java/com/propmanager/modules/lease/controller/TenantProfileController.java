package com.propmanager.modules.lease.controller;

import com.propmanager.modules.lease.dto.TenantProfileRequestDto;
import com.propmanager.modules.lease.dto.TenantProfileResponseDto;
import com.propmanager.modules.lease.service.TenantProfileService;
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

@RestController
@RequestMapping("/api/v1/tenant-profiles")
@RequiredArgsConstructor
@Tag(name = "Tenant Profiles",
     description = "Centralized renter directory management within an authenticated organization.")
public class TenantProfileController {

    private final TenantProfileService tenantProfileService;

    @Operation(summary = "Register a tenant profile",
        description = "Creates a new renter record in the organization's centralized directory.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tenant profile created."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "409", description = "A tenant profile with this email already exists."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping
    public ResponseEntity<TenantProfileResponseDto> create(
        @Valid @RequestBody TenantProfileRequestDto requestDto
    ) {
        TenantProfileResponseDto response = tenantProfileService.createTenantProfile(requestDto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get tenant profile by ID",
        description = "Retrieves a single tenant profile within the authenticated organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant profile retrieved."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Tenant profile not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TenantProfileResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantProfileService.findById(id));
    }

    @Operation(summary = "List tenant profiles",
        description = "Returns all tenant profiles in the organization. " +
                      "Optionally filter with a ?query= search parameter across name and email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant profiles returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<List<TenantProfileResponseDto>> getAll(
        @RequestParam(required = false) String query
    ) {
        if (query != null && !query.isBlank()) {
            return ResponseEntity.ok(tenantProfileService.search(query));
        }
        return ResponseEntity.ok(tenantProfileService.findAll());
    }

    @Operation(summary = "Update a tenant profile",
        description = "Updates contact information for an existing tenant profile.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant profile updated."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Tenant profile not found."),
        @ApiResponse(responseCode = "409", description = "Email already in use."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TenantProfileResponseDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody TenantProfileRequestDto requestDto
    ) {
        return ResponseEntity.ok(tenantProfileService.updateTenantProfile(id, requestDto));
    }

    @Operation(summary = "Delete a tenant profile",
        description = "Permanently deletes a tenant profile. " +
                      "Restricted to ADMINISTRATOR role.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tenant profile deleted."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "404", description = "Tenant profile not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenantProfileService.deleteTenantProfile(id);
        return ResponseEntity.noContent().build();
    }
}