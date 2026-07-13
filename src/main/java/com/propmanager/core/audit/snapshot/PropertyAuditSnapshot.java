package com.propmanager.core.audit.snapshot;

import com.propmanager.modules.inventory.entity.Property;
import java.util.UUID;

public record PropertyAuditSnapshot(
    UUID id,
    String name,
    String address,
    String locationCity,
    String description,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static PropertyAuditSnapshot of(Property entity) {
        if (entity == null) return null;
        return new PropertyAuditSnapshot(
            entity.getId(),
            entity.getName(),
            entity.getAddress(),
            entity.getLocationCity(),
            entity.getDescription(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
