package com.ryuqq.crawlinghub.integration.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test-specific Crawl Worker application for integration testing.
 *
 * <p>This application excludes conflicting ClockConfig classes from other bootstrap modules:
 *
 * <ul>
 *   <li>Excludes: com.ryuqq.crawlinghub.bootstrap.config (web-api ClockConfig)
 *   <li>Uses: com.ryuqq.crawlinghub.config.ClockConfig (crawl-worker)
 * </ul>
 *
 * <p>Component scan packages are the same as CrawlWorkerApplication:
 *
 * <ul>
 *   <li>com.ryuqq.crawlinghub - Bootstrap (own package)
 *   <li>com.ryuqq.crawlinghub.application - Application Layer
 *   <li>com.ryuqq.crawlinghub.adapter.in.sqs - SQS Listener (Inbound)
 *   <li>com.ryuqq.crawlinghub.adapter.out.persistence - MySQL Persistence
 *   <li>com.ryuqq.crawlinghub.adapter.out.redis - Redis (Distributed Lock)
 *   <li>com.ryuqq.crawlinghub.adapter.out.sqs - SQS Publisher (Outbound)
 *   <li>com.ryuqq.crawlinghub.adapter.out.http - HTTP Client (Crawling)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {
            "com.ryuqq.crawlinghub",
            "com.ryuqq.crawlinghub.application",
            "com.ryuqq.crawlinghub.adapter.in.sqs",
            "com.ryuqq.crawlinghub.adapter.out.persistence",
            "com.ryuqq.crawlinghub.adapter.out.redis",
            "com.ryuqq.crawlinghub.adapter.out.sqs",
            "com.ryuqq.crawlinghub.adapter.out.http"
        },
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.REGEX,
                    pattern = "com\\.ryuqq\\.crawlinghub\\.bootstrap\\..*")
        })
@EntityScan(basePackages = {"com.ryuqq.crawlinghub.adapter.out.persistence"})
@ConfigurationPropertiesScan(
        basePackages = {
            "com.ryuqq.crawlinghub.adapter.in.sqs.config",
            "com.ryuqq.crawlinghub.adapter.out.sqs.config",
            "com.ryuqq.crawlinghub.adapter.out.redis.config",
            "com.ryuqq.crawlinghub.adapter.out.http.config"
        })
public class TestCrawlWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCrawlWorkerApplication.class, args);
    }
}
