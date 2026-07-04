package com.propmanager.modules.lease.service;

import com.propmanager.core.exception.ConflictException;
import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.core.tenant.TenantContext;
import com.propmanager.modules.lease.dto.TenantProfileRequestDto;
import com.propmanager.modules.lease.dto.TenantProfileResponseDto;
import com.propmanager.modules.lease.entity.TenantProfile;
import com.propmanager.modules.lease.mapper.TenantProfileMapper;
import com.propmanager.modules.lease.repository.TenantProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantProfileServiceImpl implements TenantProfileService {

    private final TenantProfileRepository tenantProfileRepository;
    private final TenantProfileMapper     tenantProfileMapper;

    @Override
    @Transactional
    public TenantProfileResponseDto createTenantProfile(TenantProfileRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Creating tenant profile for email [{}] in org [{}]",
            requestDto.getEmail(), tenantId);

        if (tenantProfileRepository.existsByEmailAndTenantId(requestDto.getEmail(), tenantId)) {
            throw new ConflictException(
                "TENANT_PROFILE_EMAIL_TAKEN",
                "A tenant profile with email '" + requestDto.getEmail() +
                "' already exists in this organization."
            );
        }

        TenantProfile profile = tenantProfileMapper.toEntity(requestDto);
        profile.setTenantId(tenantId);
        TenantProfile saved = tenantProfileRepository.save(profile);

        log.info("Tenant profile created. ID: [{}], Email: [{}], Org: [{}]",
            saved.getId(), saved.getEmail(), tenantId);
        return tenantProfileMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantProfileResponseDto findById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return tenantProfileMapper.toResponseDto(resolveProfile(id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantProfileResponseDto> findAll() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Fetching all tenant profiles for org [{}]", tenantId);
        return tenantProfileRepository.findAllByTenantId(tenantId)
            .stream()
            .map(tenantProfileMapper::toResponseDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantProfileResponseDto> search(String query) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.debug("Searching tenant profiles with query '{}' in org [{}]", query, tenantId);
        return tenantProfileRepository.searchByTenantId(tenantId, query)
            .stream()
            .map(tenantProfileMapper::toResponseDto)
            .toList();
    }

    @Override
    @Transactional
    public TenantProfileResponseDto updateTenantProfile(UUID id, TenantProfileRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Updating tenant profile ID [{}] in org [{}]", id, tenantId);

        TenantProfile profile = resolveProfile(id, tenantId);

        if (!profile.getEmail().equals(requestDto.getEmail()) &&
            tenantProfileRepository.existsByEmailAndTenantId(requestDto.getEmail(), tenantId)) {
            throw new ConflictException(
                "TENANT_PROFILE_EMAIL_TAKEN",
                "A tenant profile with email '" + requestDto.getEmail() +
                "' already exists in this organization."
            );
        }

        tenantProfileMapper.updateEntityFromDto(requestDto, profile);
        TenantProfile saved = tenantProfileRepository.save(profile);

        log.info("Tenant profile updated. ID: [{}]", saved.getId());
        return tenantProfileMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteTenantProfile(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Deleting tenant profile ID [{}] in org [{}]", id, tenantId);
        TenantProfile profile = resolveProfile(id, tenantId);
        tenantProfileRepository.delete(profile);
        log.info("Tenant profile deleted. ID: [{}]", id);
    }

    private TenantProfile resolveProfile(UUID id, UUID tenantId) {
        return tenantProfileRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "TENANT_PROFILE_NOT_FOUND",
                "No tenant profile found with ID: " + id
            ));
    }
}