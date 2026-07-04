package com.propmanager.modules.lease.mapper;

import com.propmanager.modules.lease.dto.LeaseRequestDto;
import com.propmanager.modules.lease.dto.LeaseResponseDto;
import com.propmanager.modules.lease.entity.Lease;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaseMapper {

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "tenantId",        ignore = true)
    @Mapping(target = "unit",            ignore = true)
    @Mapping(target = "tenantProfile",   ignore = true)
    @Mapping(target = "status",          ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    Lease toEntity(LeaseRequestDto requestDto);

    @Mapping(target = "unitId",          source = "unit.id")
    @Mapping(target = "tenantProfileId", source = "tenantProfile.id")
    LeaseResponseDto toResponseDto(Lease lease);
}