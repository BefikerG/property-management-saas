package com.propmanager.security;

import com.propmanager.base.BaseIntegrationTest;
import com.propmanager.modules.inventory.entity.Property;
import com.propmanager.modules.inventory.entity.Unit;
import com.propmanager.modules.inventory.entity.UnitStatus;
import com.propmanager.modules.inventory.repository.PropertyRepository;
import com.propmanager.modules.inventory.repository.UnitRepository;
import com.propmanager.modules.lease.entity.TenantProfile;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.repository.TenantProfileRepository;
import com.propmanager.modules.lease.repository.LeaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Adversarial multi-tenant isolation test suite.
 *
 * PRD v2.1 §6 — Release Criterion CR-01: Zero Data Crosstalk.
 *
 * Strategy: seed identical data in both ORG_ALPHA and ORG_BETA,
 * then assert that every authenticated request from ORG_ALPHA
 * returns exactly zero records belonging to ORG_BETA, and vice versa.
 *
 * Each test is an independent adversarial scenario — not a happy path test.
 */
@DisplayName("CR-01 — Multi-Tenant Data Isolation (Adversarial)")
class MultiTenantIsolationTest extends BaseIntegrationTest {

    @Autowired private PropertyRepository     propertyRepository;
    @Autowired private UnitRepository         unitRepository;
    @Autowired private TenantProfileRepository tenantProfileRepository;
    @Autowired private LeaseRepository        leaseRepository;

    private Property     alphaProperty;
    private Property     betaProperty;
    private TenantProfile alphaTenantProfile;
    private TenantProfile betaTenantProfile;
    private Lease        alphaLease;

    @BeforeEach
    void seedInventory() {
        // ── Alpha data ─────────────────────────────────────────────────
        alphaProperty = propertyRepository.save(Property.builder()
            .tenantId(orgAlpha.getId())
            .name("Alpha Tower")
            .address("Bole Road 123")
            .locationCity("Addis Ababa")
            .build());

        Unit alphaUnit = unitRepository.save(Unit.builder()
            .tenantId(orgAlpha.getId())
            .property(alphaProperty)
            .unitNumber("A-101")
            .status(UnitStatus.VACANT)
            .baselinePrice(new BigDecimal("45000.00"))
            .currencyCode("ETB")
            .build());

        alphaTenantProfile = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgAlpha.getId())
            .fullName("Tigist Alemu")
            .email("tigist@example.com")
            .build());

        alphaLease = leaseRepository.save(Lease.builder()
            .tenantId(orgAlpha.getId())
            .unit(alphaUnit)
            .tenantProfile(alphaTenantProfile)
            .status(LeaseStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(new BigDecimal("45000.00"))
            .billingDay((short) 1)
            .build());

        // ── Beta data (identical structure, different org) ─────────────
        betaProperty = propertyRepository.save(Property.builder()
            .tenantId(orgBeta.getId())
            .name("Beta Tower")
            .address("Kazanchis Road 456")
            .locationCity("Addis Ababa")
            .build());

        unitRepository.save(Unit.builder()
            .tenantId(orgBeta.getId())
            .property(betaProperty)
            .unitNumber("B-101")
            .status(UnitStatus.VACANT)
            .baselinePrice(new BigDecimal("50000.00"))
            .currencyCode("ETB")
            .build());

        betaTenantProfile = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgBeta.getId())
            .fullName("Dawit Bekele")
            .email("dawit@example.com")
            .build());
    }

    // ── Property Isolation ─────────────────────────────────────────────

    @Test
    @DisplayName("Alpha admin sees only Alpha properties — Beta properties are invisible")
    void alphaAdminCannotSeesBetaProperties() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.name == 'Beta Tower')]").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.name == 'Alpha Tower')]").exists());
    }

    @Test
    @DisplayName("Beta admin sees only Beta properties — Alpha properties are invisible")
    void betaAdminCannotSeesAlphaProperties() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                .header("Authorization", bearerOf(tokenBetaAdmin))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.name == 'Alpha Tower')]").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.name == 'Beta Tower')]").exists());
    }

    @Test
    @DisplayName("Alpha admin requesting Beta property by ID receives 404 — not 403")
    void crossTenantPropertyByIdReturns404NotForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/properties/{id}", betaProperty.getId())
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("PROPERTY_NOT_FOUND"));
    }

    // ── Tenant Profile Isolation ───────────────────────────────────────

    @Test
    @DisplayName("Alpha admin sees only Alpha tenant profiles")
    void alphaAdminCannotSeesBetaTenantProfiles() throws Exception {
        mockMvc.perform(get("/api/v1/tenant-profiles")
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.email == 'dawit@example.com')]").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.email == 'tigist@example.com')]").exists());
    }

    @Test
    @DisplayName("Alpha admin requesting Beta tenant profile by ID receives 404")
    void crossTenantTenantProfileByIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/tenant-profiles/{id}", betaTenantProfile.getId())
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("TENANT_PROFILE_NOT_FOUND"));
    }

    // ── Lease Isolation ────────────────────────────────────────────────

    @Test
    @DisplayName("Alpha admin sees only Alpha leases — Beta leases invisible")
    void alphaAdminCannotSeesBetaLeases() throws Exception {
        mockMvc.perform(get("/api/v1/leases")
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.tenantId == '" + orgBeta.getId() + "')]")
                .doesNotExist());
    }

    @Test
    @DisplayName("Alpha admin requesting Beta lease by ID receives 404")
    void crossTenantLeaseByIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/leases/{id}", alphaLease.getId())
                .header("Authorization", bearerOf(tokenBetaAdmin)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("LEASE_NOT_FOUND"));
    }

    // ── Staff Member Isolation ─────────────────────────────────────────

    @Test
    @DisplayName("Alpha admin sees only Alpha staff members")
    void alphaAdminCannotSeesBetaStaffMembers() throws Exception {
        mockMvc.perform(get("/api/v1/staff-members")
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.email == 'admin@beta.com')]").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.email == 'admin@alpha.com')]").exists());
    }

    // ── Role-Based Isolation (Viewer cannot mutate) ────────────────────

    @Test
    @DisplayName("Alpha Viewer can read Alpha properties")
    void alphaViewerCanReadAlphaProperties() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                .header("Authorization", bearerOf(tokenAlphaViewer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.name == 'Alpha Tower')]").exists());
    }

    @Test
    @DisplayName("Alpha Viewer cannot see Beta properties")
    void alphaViewerCannotSeeBetaProperties() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                .header("Authorization", bearerOf(tokenAlphaViewer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.name == 'Beta Tower')]").doesNotExist());
    }
}