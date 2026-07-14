package com.propmanager.modules.inventory.controller;

import com.propmanager.modules.inventory.dto.*;
import com.propmanager.modules.inventory.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Physical asset portfolio management — properties, structures, and units.")
public class PropertyController {

    private final PropertyService propertyService;

    // ── Properties ───────────────────────────────────────────────────

    @Operation(summary = "Create a property",
        description = "Registers a new physical property asset under the authenticated organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Property created successfully."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "409", description = "A property with this name already exists in the organization."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping
    public ResponseEntity<PropertyResponseDto> createProperty(
        @Valid @RequestBody PropertyRequestDto requestDto
    ) {
        PropertyResponseDto response = propertyService.createProperty(requestDto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List all properties",
        description = "Returns all properties belonging to the authenticated organization. Optionally filter by city.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Property list returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<Page<PropertyResponseDto>> getAllProperties(
        @RequestParam(required = false) String city,
        @RequestParam(defaultValue = "0")  int    page,
        @RequestParam(defaultValue = "20") int    size
    ) {
        int safeSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(
            page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(
                propertyService.findAllPropertiesByCity(city, pageable));
        }
        return ResponseEntity.ok(propertyService.findAllProperties(pageable));
    }

    @Operation(summary = "Get property by ID",
        description = "Retrieves a single property by UUID within the authenticated organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Property retrieved."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> getPropertyById(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.findPropertyById(id));
    }

    @Operation(summary = "Replace a property",
        description = "Replaces the entire property resource with the provided data.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Property updated."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property not found."),
        @ApiResponse(responseCode = "409", description = "Property name already taken."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> updateProperty(
        @PathVariable UUID id,
        @Valid @RequestBody PropertyRequestDto requestDto
    ) {
        return ResponseEntity.ok(propertyService.updateProperty(id, requestDto));
    }

    @Operation(summary = "Delete a property",
        description = "Permanently deletes a property. Restricted to ADMINISTRATOR role.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Property deleted."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "404", description = "Property not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable UUID id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    // ── Structures ───────────────────────────────────────────────────

    @Operation(summary = "Add a structural segment",
        description = "Adds a Block, Floor, Wing, or Zone to an existing property.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Structure created."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property or parent structure not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/{propertyId}/structures")
    public ResponseEntity<PropertyStructureResponseDto> addStructure(
        @PathVariable UUID propertyId,
        @Valid @RequestBody PropertyStructureRequestDto requestDto
    ) {
        PropertyStructureResponseDto response =
            propertyService.addStructure(propertyId, requestDto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List structural segments",
        description = "Returns all structural segments (Blocks, Floors, Wings) for a property.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Structures returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{propertyId}/structures")
    public ResponseEntity<Page<PropertyStructureResponseDto>> getAllStructures(
        @PathVariable UUID propertyId,
        @RequestParam(defaultValue = "0")  int    page,
        @RequestParam(defaultValue = "20") int    size
    ) {
        int safeSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(
            page, safeSize, Sort.by(Sort.Direction.ASC, "ordinal"));
        return ResponseEntity.ok(propertyService.findAllStructures(propertyId, pageable));
    }

    // ── Units ────────────────────────────────────────────────────────

    @Operation(summary = "Add a unit",
        description = "Adds a rentable unit to a property. Optionally assigns it to a structural segment.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Unit created."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property or structure not found."),
        @ApiResponse(responseCode = "409", description = "Unit number already exists in this property."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/{propertyId}/units")
    public ResponseEntity<UnitResponseDto> addUnit(
        @PathVariable UUID propertyId,
        @Valid @RequestBody UnitRequestDto requestDto
    ) {
        UnitResponseDto response = propertyService.addUnit(propertyId, requestDto);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List units for a property",
        description = "Returns all units for a property. This is the primary data source for the unit matrix dashboard.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Units returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{propertyId}/units")
    public ResponseEntity<Page<UnitResponseDto>> getAllUnits(
        @PathVariable UUID propertyId,
        @RequestParam(defaultValue = "0")  int    page,
        @RequestParam(defaultValue = "20") int    size
    ) {
        int safeSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(
            page, safeSize, Sort.by(Sort.Direction.ASC, "unitNumber"));
        return ResponseEntity.ok(propertyService.findAllUnits(propertyId, pageable));
    }

    @Operation(summary = "Get unit by ID",
        description = "Retrieves a single unit by UUID within a property.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unit retrieved."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property or unit not found."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping("/{propertyId}/units/{unitId}")
    public ResponseEntity<UnitResponseDto> getUnitById(
        @PathVariable UUID propertyId,
        @PathVariable UUID unitId
    ) {
        return ResponseEntity.ok(propertyService.findUnitById(propertyId, unitId));
    }

    @Operation(summary = "Update unit status",
        description = "Manually transitions a unit to VACANT or MAINTENANCE. " +
                      "OCCUPIED is set exclusively by lease activation and cannot be set here.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unit status updated."),
        @ApiResponse(responseCode = "400", description = "Validation failure."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — insufficient role."),
        @ApiResponse(responseCode = "404", description = "Property or unit not found."),
        @ApiResponse(responseCode = "409", description = "Invalid status transition."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PatchMapping("/{propertyId}/units/{unitId}/status")
    public ResponseEntity<UnitResponseDto> updateUnitStatus(
        @PathVariable UUID propertyId,
        @PathVariable UUID unitId,
        @Valid @RequestBody UnitStatusUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
            propertyService.updateUnitStatus(propertyId, unitId, requestDto)
        );
    }
}