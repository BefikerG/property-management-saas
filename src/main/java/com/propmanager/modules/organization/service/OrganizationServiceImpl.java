package com.propmanager.modules.organization.service;

import com.propmanager.core.exception.ConflictException;
import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.modules.organization.dto.OrganizationRequestDto;
import com.propmanager.modules.organization.dto.OrganizationResponseDto;
import com.propmanager.modules.organization.entity.OrgStatus;
import com.propmanager.modules.organization.entity.Organization;
import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.entity.StaffRole;
import com.propmanager.modules.organization.entity.StaffStatus;
import com.propmanager.modules.organization.mapper.OrganizationMapper;
import com.propmanager.modules.organization.repository.OrganizationRepository;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of OrganizationService.
 *
 * Transactional rules (skill mandate):
 *   @Transactional(readOnly = true) on all read methods.
 *   @Transactional                  on all write methods.
 *   Never placed on the controller layer.
 *
 * Conflict detection: name uniqueness is checked at the service layer
 * first (findByName), producing a structured 409 ConflictException
 * before the database unique constraint is reached. Both layers protect
 * correctness — the service check gives a clean API response, the
 * database constraint is the ultimate safety net under race conditions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper     organizationMapper;
    private final StaffMemberRepository  staffMemberRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    @Transactional
    public OrganizationResponseDto registerOrganization(OrganizationRequestDto requestDto) {
        log.info("Registering new organization with name: '{}'", requestDto.getName());

        organizationRepository.findByName(requestDto.getName()).ifPresent(existing -> {
            throw new ConflictException(
                "ORGANIZATION_NAME_TAKEN",
                "An organization with the name '" + requestDto.getName() + "' is already registered."
            );
        });

        Organization organization = organizationMapper.toEntity(requestDto);
        Organization saved = organizationRepository.save(organization);
        organizationRepository.flush();

        log.info("Organization registered successfully. ID: {}, Name: '{}'",
            saved.getId(), saved.getName());

        // Admin bootstrap: if admin fields are provided, create the first ADMINISTRATOR
        // atomically within this transaction.
        //
        // Why we do NOT delegate to StaffMemberService.createStaffMember() here:
        //   createStaffMember() reads TenantContext.getCurrentTenantId(), which is null
        //   during this unauthenticated registration request (no JWT token, no filter).
        //   Instead, we build the StaffMember entity directly and supply the tenantId
        //   from the newly persisted organization's ID — this is the correct bootstrap path.
        if (requestDto.getAdminEmail() != null && !requestDto.getAdminEmail().isBlank()
                && requestDto.getAdminPassword() != null && !requestDto.getAdminPassword().isBlank()) {

            String fullName = (requestDto.getAdminFullName() != null
                    && !requestDto.getAdminFullName().isBlank())
                    ? requestDto.getAdminFullName()
                    : requestDto.getAdminEmail(); // sensible fallback if fullName is omitted

            StaffMember admin = new StaffMember();
            admin.setTenantId(saved.getId());
            admin.setEmail(requestDto.getAdminEmail().trim());
            admin.setPasswordHash(passwordEncoder.encode(requestDto.getAdminPassword()));
            admin.setFullName(fullName);
            admin.setRole(StaffRole.ADMINISTRATOR);
            admin.setStatus(StaffStatus.ACTIVE);
            staffMemberRepository.save(admin);

            log.info("Bootstrap admin created for org [{}]: email=[{}]",
                    saved.getId(), requestDto.getAdminEmail());
        }

        return organizationMapper.toResponseDto(
            organizationRepository.findById(saved.getId()).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponseDto findById(UUID id) {
        log.debug("Fetching organization by ID: {}", id);

        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "ORGANIZATION_NOT_FOUND",
                "No organization found with ID: " + id
            ));

        return organizationMapper.toResponseDto(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponseDto> findAll() {
        log.debug("Fetching all registered organizations.");

        return organizationRepository.findAll()
            .stream()
            .map(organizationMapper::toResponseDto)
            .toList();
    }

    @Override
    @Transactional
    public OrganizationResponseDto suspendOrganization(UUID id) {
        log.info("Suspending organization with ID: {}", id);

        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "ORGANIZATION_NOT_FOUND",
                "No organization found with ID: " + id
            ));

        if (organization.getStatus() == OrgStatus.SUSPENDED) {
            throw new ConflictException(
                "ORGANIZATION_ALREADY_SUSPENDED",
                "Organization with ID " + id + " is already suspended."
            );
        }

        organization.setStatus(OrgStatus.SUSPENDED);
        Organization saved = organizationRepository.save(organization);

        log.info("Organization suspended. ID: {}", saved.getId());
        return organizationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public OrganizationResponseDto reactivateOrganization(UUID id) {
        log.info("Reactivating organization with ID: {}", id);

        Organization organization = organizationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "ORGANIZATION_NOT_FOUND",
                "No organization found with ID: " + id
            ));

        if (organization.getStatus() == OrgStatus.ACTIVE) {
            throw new ConflictException(
                "ORGANIZATION_ALREADY_ACTIVE",
                "Organization with ID " + id + " is already active."
            );
        }

        organization.setStatus(OrgStatus.ACTIVE);
        Organization saved = organizationRepository.save(organization);

        log.info("Organization reactivated. ID: {}", saved.getId());
        return organizationMapper.toResponseDto(saved);
    }
}