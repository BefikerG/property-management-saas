package com.propmanager.modules.billing.controller;

import com.propmanager.modules.billing.dto.BillingRunResponseDto;
import com.propmanager.modules.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing Engine",
     description = "Manual trigger for the automated Spring Batch billing job.")
public class BillingController {

    private final BillingService billingService;

    @Operation(summary = "Trigger billing run",
        description = "Manually triggers the Spring Batch billing engine for the current " +
                      "calendar month. Generates one invoice per active lease that does not " +
                      "yet have an invoice for this period. Completely idempotent — " +
                      "safe to call multiple times. Restricted to ADMINISTRATOR role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Billing run executed."),
        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT required."),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMINISTRATOR role required."),
        @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @PostMapping("/trigger")
    public ResponseEntity<BillingRunResponseDto> triggerBilling() {
        return ResponseEntity.ok(billingService.triggerBillingRun());
    }
}