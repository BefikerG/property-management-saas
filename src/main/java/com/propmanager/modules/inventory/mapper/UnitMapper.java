package com.propmanager.modules.inventory.mapper;

import com.propmanager.modules.inventory.dto.UnitRequestDto;
import com.propmanager.modules.inventory.dto.UnitResponseDto;
import com.propmanager.modules.inventory.entity.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "tenantId",    ignore = true)
    @Mapping(target = "property",    ignore = true)
    @Mapping(target = "structure",   ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Unit toEntity(UnitRequestDto requestDto);

    @Mapping(target = "propertyId",  source = "property.id")
    @Mapping(target = "structureId", source = "structure.id")
    UnitResponseDto toResponseDto(Unit unit);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "tenantId",    ignore = true)
    @Mapping(target = "property",    ignore = true)
    @Mapping(target = "structure",   ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateEntityFromDto(UnitRequestDto requestDto, @MappingTarget Unit unit);
}