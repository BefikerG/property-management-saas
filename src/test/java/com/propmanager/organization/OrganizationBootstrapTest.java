package com.propmanager.organization;

import com.propmanager.base.BaseIntegrationTest;
import com.propmanager.modules.organization.dto.OrganizationRequestDto;
import com.propmanager.modules.organization.repository.StaffMemberRepository;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Organization registration and DX-001 bootstrap tests.
 *
 * Verifies:
 *   - Org registration without admin fields succeeds (org only)
 *   - Org registration with admin fields creates org + admin atomically
 *   - Bootstrap admin has correct tenantId (not null)
 *   - Duplicate org name returns 409
 */
@DisplayName("Organization Registration & Bootstrap")
class OrganizationBootstrapTest extends BaseIntegrationTest {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @Test
    @DisplayName("Registration with admin fields creates org and admin atomically")
    void registrationWithAdminFieldsCreatesOrgAndAdminAtomically() throws Exception {
        OrganizationRequestDto dto = OrganizationRequestDto.builder()
            .name("Gamma Property Firm")
            .adminEmail("admin@gamma.com")
            .adminPassword("GammaPass123!")
            .adminFullName("Gamma Administrator")
            .build();

        String response = mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Gamma Property Firm"))
            .andReturn().getResponse().getContentAsString();

        String orgId = objectMapper.readTree(response).get("id").asText();

        // Verify the admin was created with the correct tenant_id (DX-001 fix)
        var adminOpt = staffMemberRepository.findByEmail("admin@gamma.com");
        assertThat(adminOpt).isPresent();
        assertThat(adminOpt.get().getTenantId().toString()).isEqualTo(orgId);
        assertThat(adminOpt.get().getPasswordHash()).isNotEqualTo("GammaPass123!");
    }

    @Test
    @DisplayName("Registration without admin fields creates org only — no staff seeded")
    void registrationWithoutAdminFieldsCreatesOrgOnly() throws Exception {
        OrganizationRequestDto dto = OrganizationRequestDto.builder()
            .name("Delta Property Firm")
            .build();

        String response = mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn().getResponse().getContentAsString();

        String orgId = objectMapper.readTree(response).get("id").asText();

        long staffCount = staffMemberRepository.findAllByTenantId(
            java.util.UUID.fromString(orgId), Pageable.unpaged()).getSize();
        assertThat(staffCount).isZero();
    }

    @Test
    @DisplayName("Duplicate organization name returns 409 ORGANIZATION_NAME_TAKEN")
    void duplicateOrgNameReturns409() throws Exception {
        OrganizationRequestDto dto = OrganizationRequestDto.builder()
            .name("Alpha Property Group") // already seeded in BaseIntegrationTest
            .build();

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("ORGANIZATION_NAME_TAKEN"));
    }

    @Test
    @DisplayName("Registration returns HTTP 201 with Location header")
    void registrationReturns201WithLocationHeader() throws Exception {
        OrganizationRequestDto dto = OrganizationRequestDto.builder()
            .name("Epsilon Realty")
            .build();

        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }
}