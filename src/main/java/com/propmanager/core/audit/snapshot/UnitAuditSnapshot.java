package com.propmanager.core.audit.snapshot;

import com.propmanager.modules.inventory.entity.Unit;
import java.util.UUID;

public record UnitAuditSnapshot(
    UUID id,
    UUID propertyId,
    String unitNumber,
    String specification,
    String status,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static UnitAuditSnapshot of(Unit entity) {
        if (entity == null) return null;
        return new UnitAuditSnapshot(
            entity.getId(),
            entity.getProperty() != null ? entity.getProperty().getId() : null,
            entity.getUnitNumber(),
            entity.getSpecification(),
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
