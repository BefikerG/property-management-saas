package com.propmanager.modules.inventory.service;

import com.propmanager.modules.inventory.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

public interface PropertyService {

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PropertyResponseDto createProperty(PropertyRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<PropertyResponseDto> findAllProperties();

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<PropertyResponseDto> findAllPropertiesByCity(String city);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    PropertyResponseDto findPropertyById(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PropertyResponseDto updateProperty(UUID id, PropertyRequestDto requestDto);

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    void deleteProperty(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PropertyStructureResponseDto addStructure(UUID propertyId, PropertyStructureRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<PropertyStructureResponseDto> findAllStructures(UUID propertyId);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    UnitResponseDto addUnit(UUID propertyId, UnitRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    List<UnitResponseDto> findAllUnits(UUID propertyId);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    UnitResponseDto findUnitById(UUID propertyId, UUID unitId);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    UnitResponseDto updateUnitStatus(UUID propertyId, UUID unitId, UnitStatusUpdateRequestDto requestDto);
}