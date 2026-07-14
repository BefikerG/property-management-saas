package com.propmanager.modules.organization.service;

import com.propmanager.core.exception.ConflictException;
import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.core.tenant.TenantContext;
import com.propmanager.modules.organization.dto.StaffMemberRequestDto;
import com.propmanager.modules.organization.dto.StaffMemberResponseDto;
import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.entity.StaffStatus;
import com.propmanager.modules.organization.mapper.StaffMemberMapper;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.propmanager.core.audit.AuditActionType;
import com.propmanager.core.audit.AuditActorResolver;
import com.propmanager.core.audit.AuditDomainEvent;
import com.propmanager.core.audit.AuditEntityType;
import com.propmanager.core.audit.snapshot.StaffMemberAuditSnapshot;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of StaffMemberService.
 *
 * Tenant context sourcing:
 *   Every write operation reads the current tenant boundary from
 *   TenantContext.getCurrentTenantId(). This value was set by
 *   JwtAuthenticationFilter from the JWT's org_id claim and is
 *   available for the full duration of the request thread.
 *   It is NEVER sourced from the request body or path parameters.
 *
 * Password handling:
 *   Plain-text passwords from StaffMemberRequestDto are hashed with
 *   BCrypt via the injected PasswordEncoder before the entity is
 *   persisted. The plain-text value is discarded immediately after
 *   hashing and never stored or logged anywhere.
 *
 * Transactional discipline (skill mandate):
 *   @Transactional(readOnly = true) on all read methods.
 *   @Transactional                  on all write methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffMemberServiceImpl implements StaffMemberService {

    private final StaffMemberRepository     staffMemberRepository;
    private final StaffMemberMapper         staffMemberMapper;
    private final PasswordEncoder           passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver        auditActorResolver;

    @Override
    @Transactional
    public StaffMemberResponseDto createStaffMember(StaffMemberRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        log.info("Creating staff member with email [{}] for org [{}]",
            requestDto.getEmail(), tenantId);

        if (staffMemberRepository.existsByEmailAndTenantId(requestDto.getEmail(), tenantId)) {
            throw new ConflictException(
                "STAFF_EMAIL_TAKEN",
                "A staff member with email '" + requestDto.getEmail() +
                "' already exists in this organization."
            );
        }

        StaffMember staffMember = staffMemberMapper.toEntity(requestDto);
        staffMember.setTenantId(tenantId);
        staffMember.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        staffMember.setRole(requestDto.getRole());

        StaffMember saved = staffMemberRepository.save(staffMember);
        staffMemberRepository.flush();

        eventPublisher.publishEvent(new AuditDomainEvent(
            this,
            tenantId,
            auditActorResolver.resolveActorId(),
            AuditEntityType.STAFF_MEMBER,
            saved.getId(),
            AuditActionType.CREATE,
            null,
            StaffMemberAuditSnapshot.of(saved)
        ));

        log.info("Staff member created. ID: [{}], Email: [{}], Role: [{}], Org: [{}]",
            saved.getId(), saved.getEmail(), saved.getRole(), tenantId);

        return staffMemberMapper.toResponseDto(
            staffMemberRepository.findByIdAndTenantId(saved.getId(), tenantId).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public StaffMemberResponseDto findById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        log.debug("Fetching staff member ID [{}] for org [{}]", id, tenantId);

        StaffMember staffMember = staffMemberRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "STAFF_MEMBER_NOT_FOUND",
                "No staff member found with ID: " + id
            ));

        return staffMemberMapper.toResponseDto(staffMember);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffMemberResponseDto> findAll(Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        log.debug("Fetching all staff members for org [{}]", tenantId);

        return staffMemberRepository.findAllByTenantId(tenantId, pageable)
            .map(staffMemberMapper::toResponseDto);
    }

    @Override
    @Transactional
    public StaffMemberResponseDto deactivateStaffMember(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        log.info("Deactivating staff member ID [{}] in org [{}]", id, tenantId);

        StaffMember staffMember = staffMemberRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "STAFF_MEMBER_NOT_FOUND",
                "No staff member found with ID: " + id
            ));

        if (staffMember.getStatus() == StaffStatus.DEACTIVATED) {
            throw new ConflictException(
                "STAFF_ALREADY_DEACTIVATED",
                "Staff member with ID " + id + " is already deactivated."
            );
        }

        StaffMemberAuditSnapshot before = StaffMemberAuditSnapshot.of(staffMember);
        staffMember.setStatus(StaffStatus.DEACTIVATED);
        StaffMember saved = staffMemberRepository.save(staffMember);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this,
            tenantId,
            auditActorResolver.resolveActorId(),
            AuditEntityType.STAFF_MEMBER,
            saved.getId(),
            AuditActionType.DEACTIVATE,
            before,
            StaffMemberAuditSnapshot.of(saved)
        ));

        log.info("Staff member deactivated. ID: [{}]", saved.getId());
        return staffMemberMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public StaffMemberResponseDto reactivateStaffMember(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        log.info("Reactivating staff member ID [{}] in org [{}]", id, tenantId);

        StaffMember staffMember = staffMemberRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "STAFF_MEMBER_NOT_FOUND",
                "No staff member found with ID: " + id
            ));

        if (staffMember.getStatus() == StaffStatus.ACTIVE) {
            throw new ConflictException(
                "STAFF_ALREADY_ACTIVE",
                "Staff member with ID " + id + " is already active."
            );
        }

        StaffMemberAuditSnapshot before = StaffMemberAuditSnapshot.of(staffMember);
        staffMember.setStatus(StaffStatus.ACTIVE);
        StaffMember saved = staffMemberRepository.save(staffMember);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this,
            tenantId,
            auditActorResolver.resolveActorId(),
            AuditEntityType.STAFF_MEMBER,
            saved.getId(),
            AuditActionType.REACTIVATE,
            before,
            StaffMemberAuditSnapshot.of(saved)
        ));

        log.info("Staff member reactivated. ID: [{}]", saved.getId());
        return staffMemberMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public StaffMemberResponseDto changeRole(UUID id, StaffMemberRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();

        log.info("Changing role for staff member ID [{}] to [{}] in org [{}]",
            id, requestDto.getRole(), tenantId);

        StaffMember staffMember = staffMemberRepository
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "STAFF_MEMBER_NOT_FOUND",
                "No staff member found with ID: " + id
            ));

        StaffMemberAuditSnapshot before = StaffMemberAuditSnapshot.of(staffMember);
        staffMember.setRole(requestDto.getRole());
        StaffMember saved = staffMemberRepository.save(staffMember);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this,
            tenantId,
            auditActorResolver.resolveActorId(),
            AuditEntityType.STAFF_MEMBER,
            saved.getId(),
            AuditActionType.ROLE_CHANGE,
            before,
            StaffMemberAuditSnapshot.of(saved)
        ));

        log.info("Role changed. ID: [{}], New Role: [{}]", saved.getId(), saved.getRole());
        return staffMemberMapper.toResponseDto(saved);
    }
}