package com.propmanager.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.propmanager.core.security.JwtService;
import com.propmanager.modules.organization.entity.Organization;
import com.propmanager.modules.organization.entity.OrgStatus;
import com.propmanager.modules.organization.entity.StaffMember;
import com.propmanager.modules.organization.entity.StaffRole;
import com.propmanager.modules.organization.entity.StaffStatus;
import com.propmanager.modules.organization.repository.OrganizationRepository;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Base class for all integration tests.
 *
 * Provides:
 *   - A real PostgreSQL instance via Testcontainers (JDBC URL protocol)
 *   - All 10 Liquibase migrations applied on first test run
 *   - Two pre-seeded organizations (ORG_ALPHA and ORG_BETA)
 *     for adversarial isolation testing
 *   - JWT tokens for every role combination needed across the test suite
 *   - MockMvc for HTTP-layer testing
 *
 * Every test class that needs database access extends this class.
 * The @Transactional annotation on the base class rolls back every
 * test method's database changes — tests are fully isolated from each other.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    // ── Injected Beans ─────────────────────────────────────────────────
    @Autowired protected MockMvc                  mockMvc;
    @Autowired protected ObjectMapper             objectMapper;
    @Autowired protected JwtService               jwtService;
    @Autowired protected OrganizationRepository   organizationRepository;
    @Autowired protected StaffMemberRepository    staffMemberRepository;

    // ── Tenant Alpha ───────────────────────────────────────────────────
    protected Organization orgAlpha;
    protected StaffMember  alphaAdmin;
    protected StaffMember  alphaManager;
    protected StaffMember  alphaViewer;

    protected String tokenAlphaAdmin;
    protected String tokenAlphaManager;
    protected String tokenAlphaViewer;

    // ── Tenant Beta ────────────────────────────────────────────────────
    protected Organization orgBeta;
    protected StaffMember  betaAdmin;

    protected String tokenBetaAdmin;

    // ── Setup ──────────────────────────────────────────────────────────
    @BeforeEach
    void seedTenants() {
        // Org Alpha
        orgAlpha = organizationRepository.save(Organization.builder()
            .name("Alpha Property Group")
            .status(OrgStatus.ACTIVE)
            .build());

        alphaAdmin = staffMemberRepository.save(StaffMember.builder()
            .tenantId(orgAlpha.getId())
            .email("admin@alpha.com")
            .passwordHash("$2a$10$irrelevant_hashed_value_for_tests")
            .fullName("Alpha Admin")
            .role(StaffRole.ADMINISTRATOR)
            .status(StaffStatus.ACTIVE)
            .build());

        alphaManager = staffMemberRepository.save(StaffMember.builder()
            .tenantId(orgAlpha.getId())
            .email("manager@alpha.com")
            .passwordHash("$2a$10$irrelevant_hashed_value_for_tests")
            .fullName("Alpha Manager")
            .role(StaffRole.PROPERTY_MANAGER)
            .status(StaffStatus.ACTIVE)
            .build());

        alphaViewer = staffMemberRepository.save(StaffMember.builder()
            .tenantId(orgAlpha.getId())
            .email("viewer@alpha.com")
            .passwordHash("$2a$10$irrelevant_hashed_value_for_tests")
            .fullName("Alpha Viewer")
            .role(StaffRole.VIEWER)
            .status(StaffStatus.ACTIVE)
            .build());

        // Org Beta
        orgBeta = organizationRepository.save(Organization.builder()
            .name("Beta Realty Solutions")
            .status(OrgStatus.ACTIVE)
            .build());

        betaAdmin = staffMemberRepository.save(StaffMember.builder()
            .tenantId(orgBeta.getId())
            .email("admin@beta.com")
            .passwordHash("$2a$10$irrelevant_hashed_value_for_tests")
            .fullName("Beta Admin")
            .role(StaffRole.ADMINISTRATOR)
            .status(StaffStatus.ACTIVE)
            .build());

        // Generate JWT tokens
        tokenAlphaAdmin   = buildToken(alphaAdmin,   orgAlpha.getId());
        tokenAlphaManager = buildToken(alphaManager, orgAlpha.getId());
        tokenAlphaViewer  = buildToken(alphaViewer,  orgAlpha.getId());
        tokenBetaAdmin    = buildToken(betaAdmin,     orgBeta.getId());
    }

    // ── Helpers ────────────────────────────────────────────────────────
    private String buildToken(StaffMember staff, UUID orgId) {
        UserDetails userDetails = User.builder()
            .username(staff.getEmail())
            .password(staff.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority(
                "ROLE_" + staff.getRole().name())))
            .build();
        return jwtService.generateAccessToken(userDetails, orgId);
    }

    protected String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    protected String bearerOf(String token) {
        return "Bearer " + token;
    }
}