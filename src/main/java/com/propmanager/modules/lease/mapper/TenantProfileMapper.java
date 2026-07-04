package com.propmanager.modules.lease.mapper;

import com.propmanager.modules.lease.dto.TenantProfileRequestDto;
import com.propmanager.modules.lease.dto.TenantProfileResponseDto;
import com.propmanager.modules.lease.entity.TenantProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantProfileMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "leases",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TenantProfile toEntity(TenantProfileRequestDto requestDto);

    TenantProfileResponseDto toResponseDto(TenantProfile tenantProfile);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "leases",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TenantProfileRequestDto requestDto,
                             @MappingTarget TenantProfile tenantProfile);
}