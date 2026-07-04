package com.propmanager.modules.inventory.mapper;

import com.propmanager.modules.inventory.dto.PropertyStructureRequestDto;
import com.propmanager.modules.inventory.dto.PropertyStructureResponseDto;
import com.propmanager.modules.inventory.entity.PropertyStructure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyStructureMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "tenantId",  ignore = true)
    @Mapping(target = "property",  ignore = true)
    @Mapping(target = "parent",    ignore = true)
    @Mapping(target = "children",  ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PropertyStructure toEntity(PropertyStructureRequestDto requestDto);

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "parentId",   source = "parent.id")
    PropertyStructureResponseDto toResponseDto(PropertyStructure propertyStructure);
}