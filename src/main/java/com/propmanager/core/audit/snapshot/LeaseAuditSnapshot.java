package com.propmanager.core.audit.snapshot;

import com.propmanager.modules.lease.entity.Lease;
import java.util.UUID;

public record LeaseAuditSnapshot(
    UUID id,
    UUID unitId,
    UUID tenantProfileId,
    java.time.LocalDate startDate,
    java.time.LocalDate endDate,
    java.math.BigDecimal monthlyRent,
    String escalationTerms,
    String status,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
    public static LeaseAuditSnapshot of(Lease entity) {
        if (entity == null) return null;
        return new LeaseAuditSnapshot(
            entity.getId(),
            entity.getUnit() != null ? entity.getUnit().getId() : null,
            entity.getTenantProfile() != null ? entity.getTenantProfile().getId() : null,
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getMonthlyRent(),
            entity.getEscalationTerms(),
            entity.getStatus() != null ? entity.getStatus().name() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
