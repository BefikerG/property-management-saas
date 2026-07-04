package com.propmanager.modules.lease.controller;

import com.propmanager.modules.lease.dto.LeaseRequestDto;
import com.propmanager.modules.lease.dto.LeaseResponseDto;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.service.LeaseService;
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
@RequestMapping("/api/v1/leases")
@RequiredArgsConstructor
@Tag(name = "Leases",
     description = "Lease lifecycle management — creation, activation, termination, and expiry.")
public class LeaseController {

    private final LeaseService leaseService;

    @Operation(summary = "Create a lease",
        description = "Creates a new DRAFT lease binding a tenant profile to a unit. " +
                      "The lease starts in DRAFT status — call /activate to make it ACTIVE.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lease created in DRAFT status."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Unit or tenant profile not found."),
        @ApiResponse(responseCode = "409", description = "Unit is under maintenance."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping
    public ResponseEntity<LeaseResponseDto> create(
        @Valid @RequestBody LeaseRequestDto requestDto
    ) {
        LeaseResponseDto response = leaseService.createLease(requestDto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get lease by ID",
        description = "Retrieves a single lease within the authenticated organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lease retrieved."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Lease not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LeaseResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(leaseService.findById(id));
    }

    @Operation(summary = "List leases",
        description = "Returns all leases in the organization. " +
                      "Optionally filter by ?status=, ?unitId=, or ?tenantProfileId=")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Leases returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<List<LeaseResponseDto>> getAll(
        @RequestParam(required = false) LeaseStatus status,
        @RequestParam(required = false) UUID        unitId,
        @RequestParam(required = false) UUID        tenantProfileId
    ) {
        if (status != null) {
            return ResponseEntity.ok(leaseService.findAllByStatus(status));
        }
        if (unitId != null) {
            return ResponseEntity.ok(leaseService.findAllByUnit(unitId));
        }
        if (tenantProfileId != null) {
            return ResponseEntity.ok(leaseService.findAllByTenantProfile(tenantProfileId));
        }
        return ResponseEntity.ok(leaseService.findAll());
    }

    @Operation(summary = "Activate a lease",
        description = "Transitions a DRAFT or PENDING lease to ACTIVE status. " +
                      "Atomically sets the unit status to OCCUPIED. " +
                      "Enforces the Single Active Lease Rule — rejects if the unit " +
                      "already has an ACTIVE lease.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lease activated. Unit is now OCCUPIED."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Lease not found."),
        @ApiResponse(responseCode = "409", description = "Lease already active, closed, or unit already occupied."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<LeaseResponseDto> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(leaseService.activateLease(id));
    }

    @Operation(summary = "Terminate a lease",
        description = "Terminates an ACTIVE lease before its natural end_date. " +
                      "Atomically sets the unit status back to VACANT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lease terminated. Unit is now VACANT."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Lease not found."),
        @ApiResponse(responseCode = "409", description = "Lease is not in ACTIVE status."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/{id}/terminate")
    public ResponseEntity<LeaseResponseDto> terminate(@PathVariable UUID id) {
        return ResponseEntity.ok(leaseService.terminateLease(id));
    }

    @Operation(summary = "Expire a lease",
        description = "Marks an ACTIVE lease as EXPIRED (natural end_date reached). " +
                      "Atomically sets the unit status back to VACANT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lease expired. Unit is now VACANT."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Lease not found."),
        @ApiResponse(responseCode = "409", description = "Lease is not in ACTIVE status."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/{id}/expire")
    public ResponseEntity<LeaseResponseDto> expire(@PathVariable UUID id) {
        return ResponseEntity.ok(leaseService.expireLease(id));
    }
}