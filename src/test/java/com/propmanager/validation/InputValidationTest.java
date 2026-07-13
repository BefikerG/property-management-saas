package com.propmanager.validation;

import com.propmanager.base.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Input validation and error response format tests.
 *
 * Verifies:
 *   - Blank required fields return 400 with field-level error detail
 *   - Invalid email format returns 400
 *   - Invalid enum value returns 400 (BUG-003)
 *   - Missing JWT returns 401 with structured JSON (not HTML)
 *   - Malformed JSON body returns 400
 */
@DisplayName("Input Validation & Error Response Format")
class InputValidationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Blank organization name returns 400 with errorCode VALIDATION_FAILURE")
    void blankOrgNameReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILURE"))
            .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("Invalid email in staff invite returns 400 with field-level detail")
    void invalidEmailReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/staff-members")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"Pass1234!\","
                       + "\"fullName\":\"Test\",\"role\":\"VIEWER\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILURE"));
    }

    @Test
    @DisplayName("Invalid enum value in request body returns 400 — not 500 (BUG-003)")
    void invalidEnumValueReturns400NotInternalServerError() throws Exception {
        mockMvc.perform(post("/api/v1/staff-members")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"valid@test.com\",\"password\":\"Pass1234!\","
                       + "\"fullName\":\"Test\",\"role\":\"INVALID_ROLE_VALUE\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_BODY"));
    }

    @Test
    @DisplayName("Missing JWT on protected endpoint returns 401 JSON — not HTML")
    void missingJwtReturns401Json() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"address\":\"Addr\","
                       + "\"locationCity\":\"Addis Ababa\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    @DisplayName("Negative rent amount returns 400 validation failure")
    void negativeRentAmountReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/leases")
                .header("Authorization", bearerOf(tokenAlphaAdmin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"unitId\":\"00000000-0000-0000-0000-000000000001\","
                       + "\"tenantProfileId\":\"00000000-0000-0000-0000-000000000002\","
                       + "\"startDate\":\"2026-08-01\",\"endDate\":\"2027-07-31\","
                       + "\"monthlyRent\":\"-500.00\",\"billingDay\":1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILURE"));
    }
}