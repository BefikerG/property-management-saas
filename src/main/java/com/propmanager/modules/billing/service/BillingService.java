package com.propmanager.modules.billing.service;

import com.propmanager.modules.billing.dto.BillingRunResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface BillingService {

    /**
     * Manually triggers the Spring Batch billing job for the current
     * calendar month. Idempotent — safe to call multiple times.
     * Restricted to ADMINISTRATOR role.
     */
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    BillingRunResponseDto triggerBillingRun();
}