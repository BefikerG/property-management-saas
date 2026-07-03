package com.propmanager.modules.inventory.mapper;

import com.propmanager.modules.inventory.dto.PropertyRequestDto;
import com.propmanager.modules.inventory.dto.PropertyResponseDto;
import com.propmanager.modules.inventory.entity.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "structures",ignore = true)
    @Mapping(target = "units",     ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Property toEntity(PropertyRequestDto requestDto);

    PropertyResponseDto toResponseDto(Property property);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "structures",ignore = true)
    @Mapping(target = "units",     ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(PropertyRequestDto requestDto, @MappingTarget Property property);
}