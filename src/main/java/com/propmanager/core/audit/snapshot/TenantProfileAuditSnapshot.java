package com.propmanager.core.audit.snapshot;

import com.propmanager.modules.lease.entity.TenantProfile;
import java.util.UUID;

public record TenantProfileAuditSnapshot(
    UUID id,
    String fullName,
    String email,
    String phone,
    String identificationReference,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static TenantProfileAuditSnapshot of(TenantProfile entity) {
        if (entity == null) return null;
        return new TenantProfileAuditSnapshot(
            entity.getId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getIdentificationReference(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
