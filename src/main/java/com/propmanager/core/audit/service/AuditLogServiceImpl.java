package com.propmanager.core.audit.service;

import com.propmanager.core.audit.dto.AuditLogResponseDto;
import com.propmanager.core.audit.mapper.AuditLogMapper;
import com.propmanager.core.audit.repository.AuditLogRepository;
import com.propmanager.core.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper     auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> findAll(Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching audit log for org [{}], page [{}]",
            tenantId, pageable.getPageNumber());
        return auditLogRepository.findAllByTenantId(tenantId, pageable)
            .map(auditLogMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> findByEntity(
        String entityType, UUID entityId, Pageable pageable
    ) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return auditLogRepository.findByEntity(
            tenantId, entityType, entityId, pageable)
            .map(auditLogMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> findByActor(
        UUID actorId, Pageable pageable
    ) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return auditLogRepository.findByActor(tenantId, actorId, pageable)
            .map(auditLogMapper::toResponseDto);
    }
}