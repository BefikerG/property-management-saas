package com.propmanager.security;

import com.propmanager.base.BaseIntegrationTest;
import com.propmanager.modules.inventory.dto.PropertyRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RBAC authorization enforcement tests.
 *
 * Verifies that every role restriction defined in the service
 * interface @PreAuthorize annotations is enforced at the HTTP layer.
 *
 * Tests are structured as: [role] [action] → [expected status]
 */
@DisplayName("RBAC Authorization Enforcement")
class AuthorizationTest extends BaseIntegrationTest {

    // ── No JWT ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/properties"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").exists());
    }

    // ── Viewer restrictions ───────────────────────────────────────────

    @Test
    @DisplayName("VIEWER cannot create a property — returns 403")
    void viewerCannotCreateProperty() throws Exception {
        PropertyRequestDto dto = PropertyRequestDto.builder()
            .name("Blocked Property")
            .address("Test Address")
            .locationCity("Addis Ababa")
            .build();

        mockMvc.perform(post("/api/v1/properties")
                .header("Authorization", bearerOf(tokenAlphaViewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VIEWER cannot trigger billing run — returns 403")
    void viewerCannotTriggerBilling() throws Exception {
        mockMvc.perform(post("/api/v1/billing/trigger")
                .header("Authorization", bearerOf(tokenAlphaViewer)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VIEWER cannot invite a staff member — returns 403")
    void viewerCannotInviteStaff() throws Exception {
        mockMvc.perform(post("/api/v1/staff-members")
                .header("Authorization", bearerOf(tokenAlphaViewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@alpha.com\",\"password\":\"Pass1234!\","
                       + "\"fullName\":\"New Staff\",\"role\":\"VIEWER\"}"))
            .andExpect(status().isForbidden());
    }

    // ── Property Manager restrictions ─────────────────────────────────

    @Test
    @DisplayName("PROPERTY_MANAGER cannot trigger billing run — returns 403")
    void propertyManagerCannotTriggerBilling() throws Exception {
        mockMvc.perform(post("/api/v1/billing/trigger")
                .header("Authorization", bearerOf(tokenAlphaManager)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROPERTY_MANAGER cannot invite staff members — returns 403")
    void propertyManagerCannotInviteStaff() throws Exception {
        mockMvc.perform(post("/api/v1/staff-members")
                .header("Authorization", bearerOf(tokenAlphaManager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@alpha.com\",\"password\":\"Pass1234!\","
                       + "\"fullName\":\"New Staff\",\"role\":\"VIEWER\"}"))
            .andExpect(status().isForbidden());
    }

    // ── Administrator can do everything ───────────────────────────────

    @Test
    @DisplayName("ADMINISTRATOR can read properties — returns 200")
    void administratorCanReadProperties() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                .header("Authorization", bearerOf(tokenAlphaAdmin)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMINISTRATOR can create a property — returns 201")
    void administratorCanCreateProperty() throws Exception {
        PropertyRequestDto dto = PropertyRequestDto.builder()
            .name("New Admin Property")
            .address("CMC Road 789")
            .locationCity("Addis Ababa")
            .build();

        mockMvc.perform(post("/api/v1/properties")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.tenantId").value(orgAlpha.getId().toString()));
    }
}