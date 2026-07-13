package com.propmanager.core.audit.snapshot;

import com.propmanager.modules.organization.entity.StaffMember;
import java.util.UUID;

public record StaffMemberAuditSnapshot(
    UUID id,
    String email,
    String fullName,
    String role,
    String status,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static StaffMemberAuditSnapshot of(StaffMember entity) {
        if (entity == null) return null;
        return new StaffMemberAuditSnapshot(
            entity.getId(),
            entity.getEmail(),
            entity.getFullName(),
            entity.getRole() != null ? entity.getRole().name() : null,
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
