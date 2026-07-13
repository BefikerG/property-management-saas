package com.propmanager.core.audit.mapper;

import com.propmanager.core.audit.dto.AuditLogResponseDto;
import com.propmanager.core.audit.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    AuditLogResponseDto toResponseDto(AuditLog auditLog);
}