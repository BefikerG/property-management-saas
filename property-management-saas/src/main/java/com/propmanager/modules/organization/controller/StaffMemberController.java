package com.propmanager.modules.organization.controller;

import com.propmanager.modules.organization.dto.StaffMemberRequestDto;
import com.propmanager.modules.organization.dto.StaffMemberResponseDto;
import com.propmanager.modules.organization.service.StaffMemberService;
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
 * REST controller exposing StaffMember lifecycle endpoints.
 *
 * Base path: /api/v1/staff-members
 *
 * All endpoints require a valid JWT — this path is not listed in
 * SecurityConfig's permitAll() rules.
 *
 * Security: @PreAuthorize annotations live on StaffMemberService
 * (the interface), not here. This controller is transport-only.
 *
 * Tenant scoping: the authenticated org's UUID is sourced from
 * TenantContext (populated by JwtAuthenticationFilter) — never
 * from any request parameter.
 */
@RestController
@RequestMapping("/api/v1/staff-members")
@RequiredArgsConstructor
@Tag(
    name        = "Staff Members",
    description = "Staff account management within an authenticated organization."
)
public class StaffMemberController {

    private final StaffMemberService staffMemberService;

    @Operation(
        summary     = "Invite a new staff member",
        description = "Creates a new staff member account within the authenticated " +
                      "organization. Restricted to ADMINISTRATOR role. The tenant " +
                      "boundary is derived from the JWT — never from the request body."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Staff member created successfully."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "409", description = "A staff member with this email already exists in the organization."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping
    public ResponseEntity<StaffMemberResponseDto> create(
        @Valid @RequestBody StaffMemberRequestDto requestDto
    ) {
        StaffMemberResponseDto response = staffMemberService.createStaffMember(requestDto);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
        summary     = "Get staff member by ID",
        description = "Retrieves a single staff member by UUID within the " +
                      "authenticated organization. Returns 404 if the ID does " +
                      "not exist or belongs to a different organization."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Staff member retrieved."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Staff member not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StaffMemberResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffMemberService.findById(id));
    }

    @Operation(
        summary     = "List all staff members",
        description = "Returns all staff members in the authenticated organization."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Staff member list returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<List<StaffMemberResponseDto>> getAll() {
        return ResponseEntity.ok(staffMemberService.findAll());
    }

    @Operation(
        summary     = "Deactivate a staff member",
        description = "Administratively deactivates a staff member account. " +
                      "Restricted to ADMINISTRATOR role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Staff member deactivated."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "404", description = "Staff member not found."),
        @ApiResponse(responseCode = "409", description = "Staff member is already deactivated."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<StaffMemberResponseDto> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(staffMemberService.deactivateStaffMember(id));
    }

    @Operation(
        summary     = "Reactivate a staff member",
        description = "Reactivates a previously deactivated staff member account. " +
                      "Restricted to ADMINISTRATOR role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Staff member reactivated."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "404", description = "Staff member not found."),
        @ApiResponse(responseCode = "409", description = "Staff member is already active."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<StaffMemberResponseDto> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(staffMemberService.reactivateStaffMember(id));
    }

    @Operation(
        summary     = "Change a staff member's role",
        description = "Updates the RBAC role assigned to a staff member. " +
                      "Restricted to ADMINISTRATOR role."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated successfully."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "404", description = "Staff member not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{id}/role")
    public ResponseEntity<StaffMemberResponseDto> changeRole(
        @PathVariable UUID id,
        @Valid @RequestBody StaffMemberRequestDto requestDto
    ) {
        return ResponseEntity.ok(staffMemberService.changeRole(id, requestDto));
    }
}