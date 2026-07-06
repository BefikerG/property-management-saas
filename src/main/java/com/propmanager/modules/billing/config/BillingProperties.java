package com.propmanager.modules.billing.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed binding for billing engine configuration.
 * Values are read from application-{profile}.yml under app.billing.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.billing")
public class BillingProperties {

    /**
     * Cron expression for the scheduled billing job trigger.
     * Default: daily at 06:00 — "0 0 6 * * *"
     */
    private String cron;
}