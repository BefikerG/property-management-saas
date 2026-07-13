package com.propmanager.core.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {

    private Long          id;
    private UUID          tenantId;
    private UUID          actorId;
    private String        entityType;
    private UUID          entityId;
    private String        actionType;
    private String        oldValue;
    private String        newValue;
    private LocalDateTime occurredAt;
}