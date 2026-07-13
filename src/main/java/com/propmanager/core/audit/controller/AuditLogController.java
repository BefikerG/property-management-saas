package com.propmanager.core.audit.controller;

import com.propmanager.core.audit.AuditEntityType;
import com.propmanager.core.audit.dto.AuditLogResponseDto;
import com.propmanager.core.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Read-only access to the organization's immutable audit trail.
 *
 * All write operations across the platform are automatically captured
 * by AsyncAuditLedgerListener — this controller only exposes the
 * read path for compliance and operational forensics.
 *
 * Restricted to ADMINISTRATOR role — per @PreAuthorize on
 * AuditLogService interface.
 */
@RestController
@RequestMapping("/api/v1/audit-log")
@RequiredArgsConstructor
@Tag(
    name = "Audit Log",
    description = "Read-only access to the organization's immutable audit trail. " +
                  "Restricted to ADMINISTRATOR role."
)
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(
        summary = "Browse audit trail",
        description = "Returns paginated audit records for the authenticated organization. " +
                      "Optionally filter by entityType + entityId, or by actorId. " +
                      "Records are ordered newest-first."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Audit records returned."),
        @ApiResponse(responseCode = "401", description = "Unauthorized."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR only."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @GetMapping
    public ResponseEntity<Page<AuditLogResponseDto>> getAuditLog(
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) UUID   entityId,
        @RequestParam(required = false) UUID   actorId,
        @RequestParam(defaultValue = "0")   int page,
        @RequestParam(defaultValue = "50")  int size
    ) {
        PageRequest pageable = PageRequest.of(
            page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        if (entityType != null && entityId != null) {
            return ResponseEntity.ok(
                auditLogService.findByEntity(entityType, entityId, pageable));
        }
        if (actorId != null) {
            return ResponseEntity.ok(
                auditLogService.findByActor(actorId, pageable));
        }
        return ResponseEntity.ok(auditLogService.findAll(pageable));
    }
}