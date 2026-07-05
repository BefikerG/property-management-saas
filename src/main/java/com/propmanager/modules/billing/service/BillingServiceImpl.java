package com.propmanager.modules.billing.service;

import com.propmanager.modules.billing.config.BillingJobConfig;
import com.propmanager.modules.billing.dto.BillingRunResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingJobConfig billingJobConfig;

    private static final DateTimeFormatter PERIOD_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public BillingRunResponseDto triggerBillingRun() {
        String billingPeriod = YearMonth.now().format(PERIOD_FORMATTER);
        log.info("Manual billing run triggered for period [{}]", billingPeriod);

        try {
            JobExecution execution = billingJobConfig.runManualBillingJob();

            int writeCount = execution.getStepExecutions().stream()
                .mapToInt(s -> (int) s.getWriteCount())
                .sum();
            int filterCount = execution.getStepExecutions().stream()
                .mapToInt(s -> (int) s.getFilterCount())
                .sum();

            log.info("Billing run complete. Generated: {}, Skipped: {}",
                writeCount, filterCount);

            return BillingRunResponseDto.builder()
                .billingPeriod(billingPeriod)
                .invoicesGenerated(writeCount)
                .invoicesSkipped(filterCount)
                .executedAt(LocalDateTime.now())
                .status(execution.getStatus().name())
                .message(execution.getStatus().name().equals("COMPLETED")
                ? "Billing run completed successfully."
                : "Billing run finished with status: " + execution.getStatus().name())
                .build();


        } catch (Exception ex) {
            log.error("Manual billing run failed for period [{}]", billingPeriod, ex);
            return BillingRunResponseDto.builder()
                .billingPeriod(billingPeriod)
                .invoicesGenerated(0)
                .invoicesSkipped(0)
                .executedAt(LocalDateTime.now())
                .status("FAILED")
                .message("Billing run failed: " + ex.getMessage())
                .build();
        }
    }
}