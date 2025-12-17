package com.ryuqq.crawlinghub.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * CrawlingHub Web API Application
 *
 * <p>Spring Boot 애플리케이션 진입점입니다.
 *
 * <p><strong>Component Scan 범위:</strong>
 *
 * <ul>
 *   <li>com.ryuqq.crawlinghub.adapter.in.rest - REST API Layer
 *   <li>com.ryuqq.crawlinghub.adapter.out.persistence - Persistence Layer
 *   <li>com.ryuqq.crawlinghub.adapter.out.eventbridge - EventBridge Layer
 *   <li>com.ryuqq.crawlinghub.adapter.out.redis - Redis Layer (UserAgent Pool)
 *   <li>com.ryuqq.crawlinghub.adapter.out.fileflow - Fileflow Layer (Image Upload)
 *   <li>com.ryuqq.crawlinghub.adapter.out.marketplace - Marketplace Layer (External Sync)
 *   <li>com.ryuqq.crawlinghub.application - Application Layer
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication(
        scanBasePackages = {
            "com.ryuqq.crawlinghub.bootstrap",
            "com.ryuqq.crawlinghub.adapter.in.rest",
            "com.ryuqq.crawlinghub.adapter.out.persistence",
            "com.ryuqq.crawlinghub.adapter.out.eventbridge",
            "com.ryuqq.crawlinghub.adapter.out.redis",
            "com.ryuqq.crawlinghub.adapter.out.http",
            "com.ryuqq.crawlinghub.adapter.out.sqs",
            "com.ryuqq.crawlinghub.adapter.out.fileflow",
            "com.ryuqq.crawlinghub.adapter.out.marketplace",
            "com.ryuqq.crawlinghub.application"
        })
@EntityScan(basePackages = {"com.ryuqq.crawlinghub.adapter.out.persistence"})
@ConfigurationPropertiesScan(
        basePackages = {
            "com.ryuqq.crawlinghub.adapter.in.rest.config.properties",
            "com.ryuqq.crawlinghub.adapter.out.eventbridge.config",
            "com.ryuqq.crawlinghub.adapter.out.redis.config",
            "com.ryuqq.crawlinghub.adapter.out.http.config",
            "com.ryuqq.crawlinghub.adapter.out.sqs.config",
            "com.ryuqq.crawlinghub.adapter.out.fileflow.config"
        })
public class CrawlingHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlingHubApplication.class, args);
    }
}
