package com.propmanager.core.audit.snapshot;

import com.propmanager.modules.organization.entity.Organization;
import java.util.UUID;

public record OrganizationAuditSnapshot(
    UUID id,
    String name,
    String status,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static OrganizationAuditSnapshot of(Organization entity) {
        if (entity == null) return null;
        return new OrganizationAuditSnapshot(
            entity.getId(),
            entity.getName(),
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
