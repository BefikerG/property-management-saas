package com.propmanager.billing;

import com.propmanager.base.BaseIntegrationTest;
import com.propmanager.modules.billing.entity.Invoice;
import com.propmanager.modules.billing.entity.InvoiceStatus;
import com.propmanager.modules.billing.repository.InvoiceRepository;
import com.propmanager.modules.inventory.entity.Property;
import com.propmanager.modules.inventory.entity.Unit;
import com.propmanager.modules.inventory.entity.UnitStatus;
import com.propmanager.modules.inventory.repository.PropertyRepository;
import com.propmanager.modules.inventory.repository.UnitRepository;
import com.propmanager.modules.lease.entity.Lease;
import com.propmanager.modules.lease.entity.LeaseStatus;
import com.propmanager.modules.lease.entity.TenantProfile;
import com.propmanager.modules.lease.repository.LeaseRepository;
import com.propmanager.modules.lease.repository.TenantProfileRepository;
import com.propmanager.modules.billing.config.BillingJobConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Batch billing engine idempotency tests.
 *
 * PRD v2.1 §6 — Release Criterion CR-02: Flawless Financial Cycles.
 * TRD v2.1 §9.2 — Composite unique index: UNIQUE(lease_id, billing_period).
 *
 * Verifies:
 *   - Exactly one invoice generated per active lease per billing period
 *   - Running the billing job N times produces exactly N invoices total
 *     (not N × N)
 *   - BigDecimal amount_due matches lease.monthly_rent exactly
 *   - Invoice status is UNPAID on generation
 */
@DisplayName("CR-02 — Billing Engine Idempotency & Financial Precision")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class BillingIdempotencyTest extends BaseIntegrationTest {

    @Autowired private PropertyRepository      propertyRepository;
    @Autowired private UnitRepository          unitRepository;
    @Autowired private TenantProfileRepository tenantProfileRepository;
    @Autowired private LeaseRepository         leaseRepository;
    @Autowired private InvoiceRepository       invoiceRepository;
    @Autowired private BillingJobConfig        billingJobConfig;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE invoices, leases, tenant_profiles, units, properties, staff_members, organizations CASCADE");
    }

    private static final BigDecimal RENT_AMOUNT =
        new BigDecimal("45000.00");
    private static final String BILLING_PERIOD  =
        YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

    private Lease activeLeaseAlpha;
    private Lease activeLeaseBeta;

    @BeforeEach
    void seedActiveLeases() {
        // Alpha lease
        Property alphaProp = propertyRepository.save(Property.builder()
            .tenantId(orgAlpha.getId())
            .name("Billing Test Building Alpha")
            .address("Test Addr")
            .locationCity("Addis Ababa")
            .build());

        Unit alphaUnit = unitRepository.save(Unit.builder()
            .tenantId(orgAlpha.getId())
            .property(alphaProp)
            .unitNumber("BT-001")
            .status(UnitStatus.OCCUPIED)
            .baselinePrice(RENT_AMOUNT)
            .currencyCode("ETB")
            .build());

        TenantProfile alphaProfile = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgAlpha.getId())
            .fullName("Billing Tenant Alpha")
            .email("billing.alpha@test.com")
            .build());

        activeLeaseAlpha = leaseRepository.save(Lease.builder()
            .tenantId(orgAlpha.getId())
            .unit(alphaUnit)
            .tenantProfile(alphaProfile)
            .status(LeaseStatus.ACTIVE)
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(RENT_AMOUNT)
            .billingDay((short) 1)
            .build());

        // Beta lease (different org — should also get its own invoice)
        Property betaProp = propertyRepository.save(Property.builder()
            .tenantId(orgBeta.getId())
            .name("Billing Test Building Beta")
            .address("Test Addr Beta")
            .locationCity("Addis Ababa")
            .build());

        Unit betaUnit = unitRepository.save(Unit.builder()
            .tenantId(orgBeta.getId())
            .property(betaProp)
            .unitNumber("BT-001")
            .status(UnitStatus.OCCUPIED)
            .baselinePrice(new BigDecimal("50000.00"))
            .currencyCode("ETB")
            .build());

        TenantProfile betaProfile = tenantProfileRepository.save(TenantProfile.builder()
            .tenantId(orgBeta.getId())
            .fullName("Billing Tenant Beta")
            .email("billing.beta@test.com")
            .build());

        activeLeaseBeta = leaseRepository.save(Lease.builder()
            .tenantId(orgBeta.getId())
            .unit(betaUnit)
            .tenantProfile(betaProfile)
            .status(LeaseStatus.ACTIVE)
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(LocalDate.now().plusYears(1))
            .monthlyRent(new BigDecimal("50000.00"))
            .billingDay((short) 1)
            .build());
    }

    @Test
    @DisplayName("Billing job generates exactly one invoice per active lease")
    void billingJobGeneratesExactlyOneInvoicePerLease() throws Exception {
        JobExecution execution = billingJobConfig.runManualBillingJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Invoice> alphaInvoices = invoiceRepository
            .findAllByLeaseIdAndTenantId(
                activeLeaseAlpha.getId(), orgAlpha.getId());
        List<Invoice> betaInvoices = invoiceRepository
            .findAllByLeaseIdAndTenantId(
                activeLeaseBeta.getId(), orgBeta.getId());

        assertThat(alphaInvoices).hasSize(1);
        assertThat(betaInvoices).hasSize(1);
    }

    @Test
    @DisplayName("Running billing job three times produces exactly one invoice — idempotency confirmed")
    void billingJobIsIdempotent() throws Exception {
        billingJobConfig.runManualBillingJob();
        billingJobConfig.runManualBillingJob();
        billingJobConfig.runManualBillingJob();

        List<Invoice> alphaInvoices = invoiceRepository
            .findAllByLeaseIdAndTenantId(
                activeLeaseAlpha.getId(), orgAlpha.getId());

        assertThat(alphaInvoices).hasSize(1);
    }

    @Test
    @DisplayName("Invoice amount_due equals lease monthly_rent exactly — BigDecimal precision")
    void invoiceAmountDueMatchesLeaseMonthlyRentExactly() throws Exception {
        billingJobConfig.runManualBillingJob();

        Invoice invoice = invoiceRepository
            .findAllByLeaseIdAndTenantId(
                activeLeaseAlpha.getId(), orgAlpha.getId())
            .get(0);

        assertThat(invoice.getAmountDue().compareTo(RENT_AMOUNT))
            .as("amount_due must equal monthly_rent exactly (BigDecimal comparison)")
            .isZero();
    }

    @Test
    @DisplayName("Generated invoice has UNPAID status and zero amount_paid")
    void generatedInvoiceIsUnpaidWithZeroAmountPaid() throws Exception {
        billingJobConfig.runManualBillingJob();

        Invoice invoice = invoiceRepository
            .findAllByLeaseIdAndTenantId(
                activeLeaseAlpha.getId(), orgAlpha.getId())
            .get(0);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        assertThat(invoice.getAmountPaid().compareTo(BigDecimal.ZERO)).isZero();
    }

    @Test
    @DisplayName("Invoice billing period matches current YYYY-MM")
    void invoiceBillingPeriodMatchesCurrentMonth() throws Exception {
        billingJobConfig.runManualBillingJob();

        Invoice invoice = invoiceRepository
            .findAllByLeaseIdAndTenantId(
                activeLeaseAlpha.getId(), orgAlpha.getId())
            .get(0);

        assertThat(invoice.getBillingPeriod()).isEqualTo(BILLING_PERIOD);
    }

    @Test
    @DisplayName("Beta org invoice is invisible to Alpha org — billing respects tenant isolation")
    void billingResultsAreTenantScoped() throws Exception {
        billingJobConfig.runManualBillingJob();

        // Alpha can see its own invoice
        List<Invoice> alphaCanSee = invoiceRepository
            .findAllByTenantId(orgAlpha.getId());
        assertThat(alphaCanSee)
            .noneMatch(i -> i.getTenantId().equals(orgBeta.getId()));

        // Beta can see its own invoice
        List<Invoice> betaCanSee = invoiceRepository
            .findAllByTenantId(orgBeta.getId());
        assertThat(betaCanSee)
            .noneMatch(i -> i.getTenantId().equals(orgAlpha.getId()));
    }
}