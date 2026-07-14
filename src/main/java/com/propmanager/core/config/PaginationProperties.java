package com.propmanager.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed binding for pagination configuration.
 * Applied uniformly across all list endpoints to prevent
 * unbounded queries as portfolio data volumes grow.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.pagination")
public class PaginationProperties {

    /** Default number of records per page when ?size= is omitted. */
    private int defaultPageSize = 20;

    /**
     * Hard ceiling on page size regardless of client request.
     * Prevents a caller from requesting Integer.MAX_VALUE records.
     */
    private int maxPageSize = 100;
}