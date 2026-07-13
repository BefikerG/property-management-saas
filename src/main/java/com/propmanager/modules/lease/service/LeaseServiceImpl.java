package com.propmanager.modules.lease.service;

import com.propmanager.core.exception.ConflictException;
import com.propmanager.core.exception.ResourceNotFoundException;
import com.propmanager.core.tenant.TenantContext;
import com.propmanager.modules.inventory.entity.Unit;
import com.propmanager.modules.inventory.entity.UnitStatus;
import com.propmanager.modules.inventory.repository.UnitRepository;
import com.propmanager.modules.lease.dto.LeaseRequestDto;
import com.propmanager.modules.lease.dto.LeaseResponseDto;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.entity.TenantProfile;
import com.propmanager.core.audit.AuditActionType;
import com.propmanager.core.audit.AuditActorResolver;
import com.propmanager.core.audit.AuditDomainEvent;
import com.propmanager.core.audit.AuditEntityType;
import com.propmanager.core.audit.snapshot.LeaseAuditSnapshot;
import org.springframework.context.ApplicationEventPublisher;
import com.propmanager.modules.lease.mapper.LeaseMapper;
import com.propmanager.modules.lease.repository.LeaseRepository;
import com.propmanager.modules.lease.repository.TenantProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of LeaseService.
 *
 * The three most critical operations in this class are activateLease(),
 * terminateLease(), and expireLease(). Each executes two state changes
 * within a single @Transactional boundary:
 *
 *   1. The Lease status transition (DRAFT/PENDING → ACTIVE, or ACTIVE → TERMINATED/EXPIRED)
 *   2. The Unit status transition (→ OCCUPIED on activation, → VACANT on release)
 *
 * Both changes commit together or roll back together — no partial state
 * is possible. This is the atomic unit status transition mandated by
 * TRD §9.6 Constraint C-06.
 *
 * The existsActiveLeaseForUnit() check before activation produces a clean
 * HTTP 409 with a structured error message. The partial unique index
 * idx_unique_active_lease_per_unit on the database is the final safety
 * net that enforces this under concurrent requests — the application
 * check and the database constraint are both required.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaseServiceImpl implements LeaseService {

    private final LeaseRepository           leaseRepository;
    private final TenantProfileRepository   tenantProfileRepository;
    private final UnitRepository            unitRepository;
    private final LeaseMapper               leaseMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver        auditActorResolver;

    // ── Create ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaseResponseDto createLease(LeaseRequestDto requestDto) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Creating lease for unit [{}] and tenant profile [{}] in org [{}]",
            requestDto.getUnitId(), requestDto.getTenantProfileId(), tenantId);

        validateDateOrdering(requestDto);

        Unit unit = resolveUnit(requestDto.getUnitId(), tenantId);
        TenantProfile tenantProfile = resolveTenantProfile(
            requestDto.getTenantProfileId(), tenantId);

        if (unit.getStatus() == UnitStatus.MAINTENANCE) {
            throw new ConflictException(
                "UNIT_UNDER_MAINTENANCE",
                "Unit [" + unit.getId() + "] is currently under maintenance. " +
                "Resolve the maintenance status before creating a lease."
            );
        }

        if (unit.getStatus() == UnitStatus.OCCUPIED) {
            throw new ConflictException(
                "UNIT_ALREADY_OCCUPIED",
                "Unit [" + unit.getId() + "] is currently OCCUPIED with an active lease. " +
                "Terminate the existing active lease before creating a new one."
            );
        }

        Lease lease = leaseMapper.toEntity(requestDto);
        lease.setTenantId(tenantId);
        lease.setUnit(unit);
        lease.setTenantProfile(tenantProfile);

        if (requestDto.getBillingDay() != null) {
            lease.setBillingDay(requestDto.getBillingDay());
        }
        if (requestDto.getSecurityDeposit() != null) {
            lease.setSecurityDeposit(requestDto.getSecurityDeposit());
        }

        Lease saved = leaseRepository.save(lease);
        leaseRepository.flush();

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.LEASE, saved.getId(),
            AuditActionType.CREATE, null, LeaseAuditSnapshot.of(saved)
        ));

        log.info("Lease created. ID: [{}], Status: DRAFT, Unit: [{}]",
            saved.getId(), saved.getUnit().getId());
        return leaseMapper.toResponseDto(
            leaseRepository.findByIdAndTenantId(saved.getId(), tenantId).orElseThrow());
    }

    // ── Read ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public LeaseResponseDto findById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return leaseMapper.toResponseDto(resolveLease(id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponseDto> findAll() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return leaseRepository.findAllByTenantId(tenantId)
            .stream().map(leaseMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponseDto> findAllByStatus(LeaseStatus status) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return leaseRepository.findAllByTenantIdAndStatus(tenantId, status)
            .stream().map(leaseMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponseDto> findAllByUnit(UUID unitId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return leaseRepository.findAllByUnitIdAndTenantId(unitId, tenantId)
            .stream().map(leaseMapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaseResponseDto> findAllByTenantProfile(UUID tenantProfileId) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return leaseRepository.findAllByTenantProfileIdAndTenantId(tenantProfileId, tenantId)
            .stream().map(leaseMapper::toResponseDto).toList();
    }

    // ── Lifecycle Transitions ─────────────────────────────────────────

    @Override
    @Transactional
    public LeaseResponseDto activateLease(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Activating lease [{}] in org [{}]", id, tenantId);

        Lease lease = resolveLease(id, tenantId);

        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            throw new ConflictException(
                "LEASE_ALREADY_ACTIVE",
                "Lease [" + id + "] is already ACTIVE."
            );
        }

        if (lease.getStatus() == LeaseStatus.TERMINATED ||
            lease.getStatus() == LeaseStatus.EXPIRED) {
            throw new ConflictException(
                "LEASE_ALREADY_CLOSED",
                "Lease [" + id + "] is " + lease.getStatus() +
                " and cannot be activated."
            );
        }

        // Application-layer check for clean 409 response before
        // the database partial unique index fires under concurrency.
        if (leaseRepository.existsActiveLeasForUnit(
            lease.getUnit().getId(), tenantId)) {
            throw new ConflictException(
                "UNIT_ALREADY_OCCUPIED",
                "Unit [" + lease.getUnit().getId() + "] already has an ACTIVE lease. " +
                "Terminate the existing lease before activating a new one."
            );
        }

        // ── Atomic transition ─────────────────────────────────────────
        // Both state changes commit together or roll back together.
        lease.setStatus(LeaseStatus.ACTIVE);

        Unit unit = lease.getUnit();
        UnitStatus previousUnitStatus = unit.getStatus();
        unit.setStatus(UnitStatus.OCCUPIED);
        unitRepository.save(unit);

        Lease saved = leaseRepository.save(lease);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.LEASE, saved.getId(),
            AuditActionType.ACTIVATE, null, LeaseAuditSnapshot.of(saved)
        ));

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.UNIT, unit.getId(),
            AuditActionType.STATUS_CHANGE,
            java.util.Map.of("status", previousUnitStatus.name()),
            java.util.Map.of("status", unit.getStatus().name())
        ));

        log.info("Lease activated. ID: [{}], Unit: [{}] → OCCUPIED", id, unit.getId());
        return leaseMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public LeaseResponseDto terminateLease(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Terminating lease [{}] in org [{}]", id, tenantId);

        Lease lease = resolveLease(id, tenantId);

        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new ConflictException(
                "LEASE_NOT_ACTIVE",
                "Only ACTIVE leases can be terminated. " +
                "Lease [" + id + "] is currently " + lease.getStatus() + "."
            );
        }

        // ── Atomic transition ─────────────────────────────────────────
        lease.setStatus(LeaseStatus.TERMINATED);

        Unit unit = lease.getUnit();
        UnitStatus previousUnitStatus = unit.getStatus();
        unit.setStatus(UnitStatus.VACANT);
        unitRepository.save(unit);

        Lease saved = leaseRepository.save(lease);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.LEASE, saved.getId(),
            AuditActionType.TERMINATE, null, LeaseAuditSnapshot.of(saved)
        ));

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.UNIT, unit.getId(),
            AuditActionType.STATUS_CHANGE,
            java.util.Map.of("status", previousUnitStatus.name()),
            java.util.Map.of("status", unit.getStatus().name())
        ));

        log.info("Lease terminated. ID: [{}], Unit: [{}] → VACANT", id, unit.getId());
        return leaseMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public LeaseResponseDto expireLease(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        log.info("Expiring lease [{}] in org [{}]", id, tenantId);

        Lease lease = resolveLease(id, tenantId);

        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new ConflictException(
                "LEASE_NOT_ACTIVE",
                "Only ACTIVE leases can be expired. " +
                "Lease [" + id + "] is currently " + lease.getStatus() + "."
            );
        }

        // ── Atomic transition ─────────────────────────────────────────
        lease.setStatus(LeaseStatus.EXPIRED);

        Unit unit = lease.getUnit();
        UnitStatus previousUnitStatus = unit.getStatus();
        unit.setStatus(UnitStatus.VACANT);
        unitRepository.save(unit);

        Lease saved = leaseRepository.save(lease);

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.LEASE, saved.getId(),
            AuditActionType.EXPIRE, null, LeaseAuditSnapshot.of(saved)
        ));

        eventPublisher.publishEvent(new AuditDomainEvent(
            this, tenantId, auditActorResolver.resolveActorId(),
            AuditEntityType.UNIT, unit.getId(),
            AuditActionType.STATUS_CHANGE,
            java.util.Map.of("status", previousUnitStatus.name()),
            java.util.Map.of("status", unit.getStatus().name())
        ));

        log.info("Lease expired. ID: [{}], Unit: [{}] → VACANT", id, unit.getId());
        return leaseMapper.toResponseDto(saved);
    }

    // ── Private Helpers ──────────────────────────────────────────────

    private Lease resolveLease(UUID id, UUID tenantId) {
        return leaseRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "LEASE_NOT_FOUND",
                "No lease found with ID: " + id
            ));
    }

    private Unit resolveUnit(UUID unitId, UUID tenantId) {
        return unitRepository.findByIdAndTenantId(unitId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "UNIT_NOT_FOUND",
                "No unit found with ID: " + unitId
            ));
    }

    private TenantProfile resolveTenantProfile(UUID profileId, UUID tenantId) {
        return tenantProfileRepository.findByIdAndTenantId(profileId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "TENANT_PROFILE_NOT_FOUND",
                "No tenant profile found with ID: " + profileId
            ));
    }

    private void validateDateOrdering(LeaseRequestDto requestDto) {
        if (requestDto.getEndDate() != null &&
            requestDto.getStartDate() != null &&
            !requestDto.getEndDate().isAfter(requestDto.getStartDate())) {
            throw new ConflictException(
                "INVALID_LEASE_DATES",
                "Lease end_date must be strictly after start_date."
            );
        }
    }
}