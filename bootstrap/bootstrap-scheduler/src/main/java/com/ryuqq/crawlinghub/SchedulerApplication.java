package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler application entry point.
 *
 * <p>Background scheduler for periodic tasks:
 *
 * <ul>
 *   <li>OutBox retry processing
 *   <li>EventBridge synchronization
 * </ul>
 *
 * <p>No REST endpoints, only @Scheduled tasks.
 *
 * <p><strong>Component Scan 범위:</strong>
 *
 * <ul>
 *   <li>com.ryuqq.crawlinghub - Bootstrap (자체 패키지)
 *   <li>com.ryuqq.crawlinghub.application - Application Layer
 *   <li>com.ryuqq.crawlinghub.adapter.out.persistence - MySQL Persistence
 *   <li>com.ryuqq.crawlinghub.adapter.out.redis - Redis Persistence
 *   <li>com.ryuqq.crawlinghub.adapter.out.eventbridge - EventBridge
 *   <li>com.ryuqq.crawlinghub.adapter.out.sqs - SQS
 *   <li>com.ryuqq.crawlinghub.adapter.out.http - HTTP Client
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication(
        scanBasePackages = {
            "com.ryuqq.crawlinghub",
            "com.ryuqq.crawlinghub.application",
            "com.ryuqq.crawlinghub.adapter.in.scheduler",
            "com.ryuqq.crawlinghub.adapter.out.persistence",
            "com.ryuqq.crawlinghub.adapter.out.redis",
            "com.ryuqq.crawlinghub.adapter.out.eventbridge",
            "com.ryuqq.crawlinghub.adapter.out.sqs",
            "com.ryuqq.crawlinghub.adapter.out.http"
        })
@EntityScan(basePackages = {"com.ryuqq.crawlinghub.adapter.out.persistence"})
@ConfigurationPropertiesScan(
        basePackages = {
            "com.ryuqq.crawlinghub.adapter.in.scheduler.config",
            "com.ryuqq.crawlinghub.adapter.out.eventbridge.config",
            "com.ryuqq.crawlinghub.adapter.out.sqs.config",
            "com.ryuqq.crawlinghub.adapter.out.http.config",
            "com.ryuqq.crawlinghub.adapter.out.redis.config"
        })
@EnableScheduling
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
