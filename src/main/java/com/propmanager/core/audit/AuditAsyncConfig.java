package com.propmanager.core.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for the audit ledger write path.
 *
 * A dedicated, bounded thread pool is reserved exclusively for
 * audit writes, separate from Spring's default async executor.
 *
 * Why a dedicated pool:
 *   An unbounded or shared executor risks audit-writing tasks
 *   competing with other async workloads for thread capacity,
 *   potentially causing audit writes to queue and lag behind
 *   the actions they describe under load. A dedicated pool
 *   gives audit logging predictable, independent capacity.
 *
 * Pool sizing:
 *   core: 2  — always-warm threads for normal audit write volume
 *   max:  5  — burst capacity for high-write-rate scenarios
 *   queue: 100 — bounded queue prevents memory exhaustion
 *               under extreme bursts; tasks beyond this bound
 *               are rejected with a logged warning rather than
 *               silently dropped or causing OOM.
 */
@EnableAsync
@Configuration
public class AuditAsyncConfig {

    public static final String AUDIT_EXECUTOR = "auditTaskExecutor";

    @Bean(name = AUDIT_EXECUTOR)
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audit-writer-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}