package com.propmanager.modules.billing.config;

import com.propmanager.modules.billing.entity.Invoice;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import com.propmanager.modules.billing.repository.InvoiceRepository;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.repository.LeaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Spring Batch configuration for the automated billing engine.
 *
 * Architecture (TRD §2.2 — spring_batch schema):
 *   The Spring Batch JobRepository persists execution metadata to the
 *   spring_batch schema, completely isolated from the public business
 *   schema. spring.batch.jdbc.initialize-schema=always creates the
 *   Spring Batch tables automatically on startup.
 *
 * Job flow:
 *   @Scheduled trigger → BillingJobLauncher.runBillingJob()
 *     → Spring Batch Job: billingCycleJob
 *       → Step 1: billingStep
 *           ItemReader:    reads all ACTIVE leases
 *           ItemProcessor: builds Invoice entity (BigDecimal arithmetic)
 *           ItemWriter:    persists, skipping existing invoices (idempotency)
 *
 * Idempotency (TRD §9.2):
 *   The InvoiceWriter checks existsByLeaseIdAndBillingPeriod() before
 *   each insert. If the invoice already exists, the write is skipped.
 *   The database unique index idx_uq_invoice_per_lease_period is the
 *   final safety net under concurrent job execution.
 *
 * Financial precision (TRD §4.1):
 *   amount_due is snapshotted from lease.monthlyRent as BigDecimal.
 *   No floating-point arithmetic anywhere in the billing pipeline.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillingJobConfig {

    private final JobRepository              jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final LeaseRepository            leaseRepository;
    private final InvoiceRepository          invoiceRepository;
    private final JobLauncher                jobLauncher;

    private static final DateTimeFormatter PERIOD_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM");

    // ── Job Definition ────────────────────────────────────────────────

    @Bean
    public Job billingCycleJob() {
        return new JobBuilder("billingCycleJob", jobRepository)
            .start(billingStep())
            .build();
    }

    @Bean
    public Step billingStep() {
        return new StepBuilder("billingStep", jobRepository)
            .<Lease, Invoice>chunk(50, transactionManager)
            .reader(activeLeaseReader())
            .processor(buildInvoiceProcessor())
            .writer(buildInvoiceWriter())
            .build();
    }

    // ── Reader ────────────────────────────────────────────────────────

    /**
     * Reads ALL active leases across ALL organizations.
     * The billing engine is a cross-tenant operation by design —
     * it must process every active lease on the platform in one run.
     * TenantContext is intentionally not set here (batch context).
     */
    @Bean
    @StepScope
    public ListItemReader<Lease> activeLeaseReader() {
        List<Lease> activeLeases = leaseRepository
            .findAllByStatus(LeaseStatus.ACTIVE);
        log.info("BillingJob: found {} active leases to process.", activeLeases.size());
        return new ListItemReader<>(activeLeases);
    }

    // ── Processor ─────────────────────────────────────────────────────

    /**
     * Transforms a Lease into an Invoice for the current billing period.
     * Returns null if an invoice for this lease and period already exists
     * — Spring Batch skips null processor results automatically.
     *
     * Financial precision: amount_due is a BigDecimal snapshot of
     * the lease's monthly_rent. No arithmetic is performed here —
     * the rent amount is transferred exactly as stored.
     */
    private ItemProcessor<Lease, Invoice> buildInvoiceProcessor() {
    return lease -> {
        String billingPeriod = YearMonth.now().format(PERIOD_FORMATTER);
        if (invoiceRepository.existsByLeaseIdAndBillingPeriod(
            lease.getId(), billingPeriod)) {
            log.debug("BillingJob: invoice already exists for lease [{}] " +
                      "period [{}] — skipping.", lease.getId(), billingPeriod);
            return null;
        }
        LocalDate today = LocalDate.now();
        int billingDay = lease.getBillingDay().intValue();
        LocalDate dueDate = today.getDayOfMonth() <= billingDay
                ? today.withDayOfMonth(billingDay)
                : today.plusMonths(1).withDayOfMonth(billingDay);
        Invoice invoice = new Invoice();
        invoice.setTenantId(lease.getTenantId());
        invoice.setLease(lease);
        invoice.setBillingPeriod(billingPeriod);
        invoice.setAmountDue(lease.getMonthlyRent());
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setDueDate(dueDate);
        invoice.setIssuedAt(LocalDateTime.now());
        log.info("BillingJob: generating invoice for lease [{}], period [{}], amount [{}] ETB.",
            lease.getId(), billingPeriod, lease.getMonthlyRent());
        return invoice;
    };
}

    // ── Writer ────────────────────────────────────────────────────────

    /**
     * Persists generated invoices to the public.invoices table.
     * The database unique index idx_uq_invoice_per_lease_period
     * provides the final concurrency-safe idempotency guarantee.
     */
    private ItemWriter<Invoice> buildInvoiceWriter() {
    return invoices -> {
        invoiceRepository.saveAll(invoices);
        log.info("BillingJob: persisted {} invoice(s).", invoices.size());
    };
}

    // ── Scheduler ─────────────────────────────────────────────────────

    /**
     * Triggers the billing job on the configured cron schedule.
     * Uses JobParameters with a timestamp to allow the same job
     * to be launched multiple times (Spring Batch considers a job
     * instance unique by its parameters).
     */
    @Scheduled(cron = "${app.billing.cron}")
    public void runScheduledBillingJob() {
        log.info("BillingJob: scheduled trigger fired.");
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("billingPeriod", YearMonth.now().format(PERIOD_FORMATTER))
                .toJobParameters();

            jobLauncher.run(billingCycleJob(), params);
        } catch (Exception ex) {
            log.error("BillingJob: scheduled execution failed.", ex);
        }
    }

    /**
     * Programmatic trigger — called by BillingServiceImpl.triggerBillingRun().
     * Allows administrators to manually trigger the billing job via
     * POST /api/v1/billing/trigger without waiting for the cron schedule.
     */
    public JobExecution runManualBillingJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("billingPeriod", YearMonth.now().format(PERIOD_FORMATTER))
            .addString("triggeredBy", "MANUAL")
            .toJobParameters();

        return jobLauncher.run(billingCycleJob(), params);
    }
}