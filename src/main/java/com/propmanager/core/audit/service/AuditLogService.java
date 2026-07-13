package com.propmanager.core.audit.service;

import com.propmanager.core.audit.dto.AuditLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

public interface AuditLogService {

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    Page<AuditLogResponseDto> findAll(Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    Page<AuditLogResponseDto> findByEntity(
        String entityType, UUID entityId, Pageable pageable);

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    Page<AuditLogResponseDto> findByActor(UUID actorId, Pageable pageable);
}