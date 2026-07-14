package com.propmanager.modules.inventory.service;

import com.propmanager.core.exception.ConflictException;
import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.core.tenant.TenantContext;
import com.propmanager.modules.inventory.dto.*;
import com.propmanager.modules.inventory.entity.*;
import com.propmanager.core.audit.AuditActionType;
import com.propmanager.core.audit.AuditActorResolver;
import com.propmanager.core.audit.AuditDomainEvent;
import com.propmanager.core.audit.AuditEntityType;
import com.propmanager.core.audit.snapshot.PropertyAuditSnapshot;
import com.propmanager.core.audit.snapshot.UnitAuditSnapshot;
import org.springframework.context.ApplicationEventPublisher;
import com.propmanager.modules.inventory.mapper.PropertyMapper;
import com.propmanager.modules.inventory.mapper.PropertyStructureMapper;
import com.propmanager.modules.inventory.mapper.UnitMapper;
import com.propmanager.modules.inventory.repository.PropertyRepository;
import com.propmanager.modules.inventory.repository.PropertyStructureRepository;
import com.propmanager.modules.inventory.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository          propertyRepository;
    private final PropertyStructureRepository structureRepository;
    private final UnitRepository              unitRepository;
    private final PropertyMapper              propertyMapper;
    private final PropertyStructureMapper     structureMapper;
    private final UnitMapper                  unitMapper;
    private final ApplicationEventPublisher   eventPublisher;
    private final AuditActorResolver          auditActorResolver;

    // ── Property CRUD ────────────────────────────────────────────────

    @Override
    @Transactional
    public PropertyResponseDto createProperty(PropertyRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Creating property '{}' for org [{}]", requestDto.getName(), tenantId);

        if (propertyRepository.existsByNameAndTenantId(requestDto.getName(), tenantId)) {
            throw new ConflictException(
                "PROPERTY_NAME_TAKEN",
                "A property named '" + requestDto.getName() + "' already exists in this organization."
            );
        }

        Property property = propertyMapper.toEntity(requestDto);
        property.setTenantId(tenantId);
        Property saved = propertyRepository.save(property);
        propertyRepository.flush();

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.PROPERTY, saved.getId(),
            AuditActionType.CREATE, null, PropertyAuditSnapshot.of(saved)
        ));

        log.info("Property created. ID: [{}], Name: '{}', Org: [{}]",
            saved.getId(), saved.getName(), tenantId);
        return propertyMapper.toResponseDto(
            propertyRepository.findByIdAndTenantId(saved.getId(), tenantId).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> findAllProperties(Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching properties page [{}] for org [{}]",
            pageable.getPageNumber(), tenantId);
        return propertyRepository.findAllByTenantId(tenantId, pageable)
            .map(propertyMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> findAllPropertiesByCity(String city, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching properties in city '{}' for org [{}]", city, tenantId);
        return propertyRepository.findAllByTenantIdAndCity(tenantId, city, pageable)
            .map(propertyMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponseDto findPropertyById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return propertyMapper.toResponseDto(resolveProperty(id, tenantId));
    }

    @Override
    @Transactional
    public PropertyResponseDto updateProperty(UUID id, PropertyRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Updating property ID [{}] for org [{}]", id, tenantId);

        Property property = resolveProperty(id, tenantId);

        if (!property.getName().equals(requestDto.getName()) &&
            propertyRepository.existsByNameAndTenantId(requestDto.getName(), tenantId)) {
            throw new ConflictException(
                "PROPERTY_NAME_TAKEN",
                "A property named '" + requestDto.getName() + "' already exists in this organization."
            );
        }

        PropertyAuditSnapshot before = PropertyAuditSnapshot.of(property);
        propertyMapper.updateEntityFromDto(requestDto, property);
        Property saved = propertyRepository.save(property);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.PROPERTY, saved.getId(),
            AuditActionType.UPDATE, before, PropertyAuditSnapshot.of(saved)
        ));

        log.info("Property updated. ID: [{}]", saved.getId());
        return propertyMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteProperty(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Deleting property ID [{}] for org [{}]", id, tenantId);

        Property property = resolveProperty(id, tenantId);
        PropertyAuditSnapshot before = PropertyAuditSnapshot.of(property);
        propertyRepository.delete(property);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.PROPERTY, id,
            AuditActionType.DELETE, before, null
        ));

        log.info("Property deleted. ID: [{}]", id);
    }

    // ── Structures ───────────────────────────────────────────────────

    @Override
    @Transactional
    public PropertyStructureResponseDto addStructure(UUID propertyId,
                                                     PropertyStructureRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Adding structure '{}' to property [{}] for org [{}]",
            requestDto.getName(), propertyId, tenantId);

        Property property = resolveProperty(propertyId, tenantId);

        PropertyStructure structure = structureMapper.toEntity(requestDto);
        structure.setTenantId(tenantId);
        structure.setProperty(property);

        if (requestDto.getParentId() != null) {
            PropertyStructure parent = structureRepository
                .findByIdAndTenantId(requestDto.getParentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "STRUCTURE_NOT_FOUND",
                    "Parent structure not found with ID: " + requestDto.getParentId()
                ));
            structure.setParent(parent);
        }

        if (requestDto.getOrdinal() != null) {
            structure.setOrdinal(requestDto.getOrdinal());
        }

        PropertyStructure saved = structureRepository.save(structure);
        structureRepository.flush();
        log.info("Structure created. ID: [{}]", saved.getId());
        return structureMapper.toResponseDto(
            structureRepository.findByIdAndTenantId(saved.getId(), tenantId).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyStructureResponseDto> findAllStructures(UUID propertyId, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        resolveProperty(propertyId, tenantId);
        return structureRepository.findAllByPropertyIdAndTenantId(propertyId, tenantId, pageable)
            .map(structureMapper::toResponseDto);
    }

    // ── Units ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UnitResponseDto addUnit(UUID propertyId, UnitRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Adding unit '{}' to property [{}] for org [{}]",
            requestDto.getUnitNumber(), propertyId, tenantId);

        Property property = resolveProperty(propertyId, tenantId);

        if (unitRepository.existsByUnitNumberAndPropertyIdAndTenantId(
            requestDto.getUnitNumber(), propertyId, tenantId)) {
            throw new ConflictException(
                "UNIT_NUMBER_TAKEN",
                "Unit number '" + requestDto.getUnitNumber() +
                "' already exists in this property."
            );
        }

        Unit unit = unitMapper.toEntity(requestDto);
        unit.setTenantId(tenantId);
        unit.setProperty(property);

        if (requestDto.getStructureId() != null) {
            PropertyStructure structure = structureRepository
                .findByIdAndTenantId(requestDto.getStructureId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "STRUCTURE_NOT_FOUND",
                    "Structure not found with ID: " + requestDto.getStructureId()
                ));
            unit.setStructure(structure);
        }

        if (requestDto.getCurrencyCode() != null) {
            unit.setCurrencyCode(requestDto.getCurrencyCode());
        }

        Unit saved = unitRepository.save(unit);
        unitRepository.flush();

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.UNIT, saved.getId(),
            AuditActionType.CREATE, null, UnitAuditSnapshot.of(saved)
        ));

        log.info("Unit created. ID: [{}], Number: '{}', Status: {}",
            saved.getId(), saved.getUnitNumber(), saved.getStatus());
        return unitMapper.toResponseDto(
            unitRepository.findByIdAndTenantId(saved.getId(), tenantId).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitResponseDto> findAllUnits(UUID propertyId, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        resolveProperty(propertyId, tenantId);
        return unitRepository.findAllByPropertyIdAndTenantId(propertyId, tenantId, pageable)
            .map(unitMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponseDto findUnitById(UUID propertyId, UUID unitId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        resolveProperty(propertyId, tenantId);
        Unit unit = unitRepository.findByIdAndTenantId(unitId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "UNIT_NOT_FOUND",
                "No unit found with ID: " + unitId
            ));
        return unitMapper.toResponseDto(unit);
    }

    @Override
    @Transactional
    public UnitResponseDto updateUnitStatus(UUID propertyId, UUID unitId,
                                            UnitStatusUpdateRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Updating status of unit [{}] to [{}] for org [{}]",
            unitId, requestDto.getStatus(), tenantId);

        resolveProperty(propertyId, tenantId);

        Unit unit = unitRepository.findByIdAndTenantId(unitId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "UNIT_NOT_FOUND",
                "No unit found with ID: " + unitId
            ));

        UnitStatus previousStatus = unit.getStatus();
        validateStatusTransition(unit, requestDto.getStatus());

        unit.setStatus(requestDto.getStatus());
        Unit saved = unitRepository.save(unit);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.UNIT, saved.getId(),
            AuditActionType.STATUS_CHANGE,
            java.util.Map.of("status", previousStatus.name()),
            java.util.Map.of("status", saved.getStatus().name())
        ));

        log.info("Unit status updated. ID: [{}], New Status: [{}]",
            saved.getId(), saved.getStatus());
        return unitMapper.toResponseDto(saved);
    }

    // ── Private Helpers ──────────────────────────────────────────────

    private Property resolveProperty(UUID id, UUID tenantId) {
        return propertyRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "PROPERTY_NOT_FOUND",
                "No property found with ID: " + id
            ));
    }

    /**
     * Enforces the unit status state machine.
     *
     * Only manual transitions are validated here:
     *   VACANT      → MAINTENANCE  (allowed)
     *   MAINTENANCE → VACANT       (allowed)
     *   OCCUPIED    → MAINTENANCE  (blocked — must terminate lease first)
     *   Any → OCCUPIED             (blocked — set only by lease activation)
     */
    private void validateStatusTransition(Unit unit, UnitStatus targetStatus) {
        UnitStatus current = unit.getStatus();

        if (targetStatus == UnitStatus.OCCUPIED) {
            throw new ConflictException(
                "INVALID_STATUS_TRANSITION",
                "OCCUPIED status is set automatically by lease activation. " +
                "It cannot be set manually."
            );
        }

        if (current == UnitStatus.OCCUPIED && targetStatus == UnitStatus.MAINTENANCE) {
            throw new ConflictException(
                "INVALID_STATUS_TRANSITION",
                "Unit [" + unit.getId() + "] is currently OCCUPIED. " +
                "Terminate the active lease before placing it under maintenance."
            );
        }

        if (current == targetStatus) {
            throw new ConflictException(
                "UNIT_ALREADY_IN_STATUS",
                "Unit [" + unit.getId() + "] is already " + current + "."
            );
        }
    }
}