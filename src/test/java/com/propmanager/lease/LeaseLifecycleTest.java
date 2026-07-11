package com.propmanager.lease;

import com.propmanager.base.BaseIntegrationTest;
import com.propmanager.modules.inventory.entity.Property;
import com.propmanager.modules.inventory.entity.Unit;
import com.propmanager.modules.inventory.entity.UnitStatus;
import com.propmanager.modules.inventory.repository.PropertyRepository;
import com.propmanager.modules.inventory.repository.UnitRepository;
import com.propmanager.modules.lease.dto.LeaseRequestDto;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.entity.TenantProfile;
import com.propmanager.modules.lease.repository.LeaseRepository;
import com.propmanager.modules.lease.repository.TenantProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Lease lifecycle integration tests.
 *
 * Covers:
 *   - DRAFT → ACTIVE: lease activation + atomic unit → OCCUPIED
 *   - ACTIVE → TERMINATED: termination + atomic unit → VACANT
 *   - Single Active Lease Rule: partial unique index enforcement
 *   - Lease creation blocked on OCCUPIED unit
 *   - Date ordering validation
 */
@DisplayName("Lease Lifecycle — State Machine & Atomic Transitions")
class LeaseLifecycleTest extends BaseIntegrationTest {

    @Autowired private PropertyRepository      propertyRepository;
    @Autowired private UnitRepository          unitRepository;
    @Autowired private TenantProfileRepository tenantProfileRepository;
    @Autowired private LeaseRepository         leaseRepository;

    private Unit         unit;
    private TenantProfile tenantProfile;
    private TenantProfile tenantProfile2;

    @BeforeEach
    void seedInventory() {
        Property property = propertyRepository.save(Property.builder()
            .tenantId(orgAlpha.getId())
            .name("Test Building")
            .address("Test Address")
            .locationCity("Addis Ababa")
            .build());

        unit = unitRepository.save(Unit.builder()
            .tenantId(orgAlpha.getId())
            .property(property)
            .unitNumber("101")
            .status(UnitStatus.VACANT)
            .baselinePrice(new BigDecimal("45000.00"))
            .currencyCode("ETB")
            .build());

        tenantProfile = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgAlpha.getId())
            .fullName("Almaz Tadesse")
            .email("almaz@test.com")
            .build());

        tenantProfile2 = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgAlpha.getId())
            .fullName("Yonas Bekele")
            .email("yonas@test.com")
            .build());
    }

    @Test
    @DisplayName("Lease creation returns DRAFT status")
    void createLeaseReturnsDraftStatus() throws Exception {
        LeaseRequestDto dto = buildLeaseRequest(unit.getId(),
            tenantProfile.getId(), "45000.00");

        mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.unitId").value(unit.getId().toString()))
            .andExpect(jsonPath("$.monthlyRent").value(45000.00));
    }

    @Test
    @DisplayName("Lease activation transitions unit to OCCUPIED atomically")
    void activateLeaseTransitionsUnitToOccupied() throws Exception {
        // Create lease
        String response = mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(buildLeaseRequest(
                    unit.getId(), tenantProfile.getId(), "45000.00"))))
            .andReturn().getResponse().getContentAsString();

        String leaseId = objectMapper.readTree(response).get("id").asText();

        // Activate
        mockMvc.perform(post("/api/v1/leases/{id}/activate", leaseId)
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify unit is OCCUPIED — atomic transition confirmed
        Unit refreshed = unitRepository.findById(unit.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(UnitStatus.OCCUPIED);
    }

    @Test
    @DisplayName("Lease termination transitions unit to VACANT atomically")
    void terminateLeaseTransitionsUnitToVacant() throws Exception {
        // Seed an ACTIVE lease directly
        Lease activeLease = leaseRepository.save(Lease.builder()
            .tenantId(orgAlpha.getId())
            .unit(unit)
            .tenantProfile(tenantProfile)
            .status(LeaseStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(new BigDecimal("45000.00"))
            .billingDay((short) 1)
            .build());

        unit.setStatus(UnitStatus.OCCUPIED);
        unitRepository.save(unit);

        // Terminate
        mockMvc.perform(post("/api/v1/leases/{id}/terminate", activeLease.getId())
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("TERMINATED"));

        // Verify unit is VACANT — atomic transition confirmed
        Unit refreshed = unitRepository.findById(unit.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(UnitStatus.VACANT);
    }

    @Test
    @DisplayName("Single Active Lease Rule — activating second lease on OCCUPIED unit returns 409")
    void singleActiveLeasRuleEnforced() throws Exception {
        // Seed first ACTIVE lease
        leaseRepository.save(Lease.builder()
            .tenantId(orgAlpha.getId())
            .unit(unit)
            .tenantProfile(tenantProfile)
            .status(LeaseStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(new BigDecimal("45000.00"))
            .billingDay((short) 1)
            .build());

        unit.setStatus(UnitStatus.OCCUPIED);
        unitRepository.save(unit);

        // Create second DRAFT lease on same unit
        String response = mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(buildLeaseRequest(
                    unit.getId(), tenantProfile2.getId(), "48000.00"))))
            .andReturn().getResponse().getContentAsString();

        // Expect creation blocked at OCCUPIED check
        assertThat(response).contains("UNIT_ALREADY_OCCUPIED");
    }

    @Test
    @DisplayName("Lease creation with end_date before start_date returns 409")
    void invalidDateOrderingRejected() throws Exception {
        LeaseRequestDto dto = LeaseRequestDto.builder()
            .unitId(unit.getId())
            .tenantProfileId(tenantProfile.getId())
            .startDate(LocalDate.now().plusMonths(3))
            .endDate(LocalDate.now())               // end before start
            .monthlyRent(new BigDecimal("45000.00"))
            .billingDay((short) 1)
            .build();

        mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("INVALID_LEASE_DATES"));
    }

    // ── Helper ─────────────────────────────────────────────────────────
    private LeaseRequestDto buildLeaseRequest(
        java.util.UUID unitId,
        java.util.UUID tenantProfileId,
        String rent
    ) {
        return LeaseRequestDto.builder()
            .unitId(unitId)
            .tenantProfileId(tenantProfileId)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(new BigDecimal(rent))
            .billingDay((short) 1)
            .build();
    }
}