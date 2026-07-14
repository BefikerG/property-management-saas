package com.propmanager.modules.inventory.service;

import com.propmanager.modules.inventory.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PropertyService {

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PropertyResponseDto createProperty(PropertyRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<PropertyResponseDto> findAllProperties(Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<PropertyResponseDto> findAllPropertiesByCity(String city, Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    PropertyResponseDto findPropertyById(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PropertyResponseDto updateProperty(UUID id, PropertyRequestDto requestDto);

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    void deleteProperty(UUID id);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    PropertyStructureResponseDto addStructure(UUID propertyId, PropertyStructureRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<PropertyStructureResponseDto> findAllStructures(UUID propertyId, Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    UnitResponseDto addUnit(UUID propertyId, UnitRequestDto requestDto);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    Page<UnitResponseDto> findAllUnits(UUID propertyId, Pageable pageable);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER', 'ROLE_VIEWER')")
    UnitResponseDto findUnitById(UUID propertyId, UUID unitId);

    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_PROPERTY_MANAGER')")
    UnitResponseDto updateUnitStatus(UUID propertyId, UUID unitId, UnitStatusUpdateRequestDto requestDto);
}