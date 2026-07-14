package com.propmanager.core.scheduling;

import com.propmanager.modules.inventory.entity.UnitStatus;
import com.propmanager.modules.inventory.repository.UnitRepository;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.repository.LeaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Nightly scheduled job that automatically expires ACTIVE leases
 * whose end_date has passed.
 *
 * Execution schedule: 02:00 daily (configurable in application.yml).
 * Time chosen to minimize overlap with business-hours traffic and
 * the billing engine cron at 06:00.
 *
 * For each expired lease, two state changes commit atomically:
 *   1. Lease status → EXPIRED
 *   2. Unit status → VACANT
 *
 * This mirrors the manual expireLease() operation in LeaseServiceImpl
 * but runs without an authenticated principal — TenantContext is
 * intentionally not set (this is a cross-tenant system operation).
 *
 * Why this job is necessary:
 *   Without it, leases past their end_date remain ACTIVE indefinitely.
 *   Their units stay OCCUPIED, blocking new tenant assignments.
 *   Property managers would need to manually call POST /leases/{id}/expire
 *   for every expired lease — an operational burden that defeats the
 *   purpose of having structured lease end dates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaseExpiryJob {

    private final LeaseRepository leaseRepository;
    private final UnitRepository  unitRepository;

    /**
     * Runs nightly at 02:00.
     * Finds all ACTIVE leases with end_date < today and expires them,
     * atomically transitioning their units to VACANT.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireOverdueLeases() {
        LocalDate today = LocalDate.now();
        log.info("LeaseExpiryJob: starting nightly expiry run for date [{}]", today);

        List<Lease> overdueLeases = leaseRepository
            .findAllActiveWithEndDateBefore(today);

        if (overdueLeases.isEmpty()) {
            log.info("LeaseExpiryJob: no overdue leases found. Run complete.");
            return;
        }

        log.info("LeaseExpiryJob: found [{}] overdue lease(s) to expire.",
            overdueLeases.size());

        int expired = 0;
        for (Lease lease : overdueLeases) {
            try {
                lease.setStatus(LeaseStatus.EXPIRED);
                leaseRepository.save(lease);

                lease.getUnit().setStatus(UnitStatus.VACANT);
                unitRepository.save(lease.getUnit());

                log.info("LeaseExpiryJob: expired lease [{}], unit [{}] → VACANT",
                    lease.getId(), lease.getUnit().getId());
                expired++;

            } catch (Exception ex) {
                log.error("LeaseExpiryJob: failed to expire lease [{}]: {}",
                    lease.getId(), ex.getMessage(), ex);
                // Continue processing remaining leases — one failure
                // must not block all others from being expired.
            }
        }

        log.info("LeaseExpiryJob: run complete. Expired [{}] of [{}] overdue leases.",
            expired, overdueLeases.size());
    }
}