package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Crawl Worker application entry point.
 *
 * <p>SQS message consumer for crawling task execution:
 *
 * <ul>
 *   <li>EventBridgeTriggerSqsListener - receives scheduler triggers from EventBridge
 *   <li>CrawlTaskSqsListener - executes crawling tasks
 *   <li>CrawlTaskDlqListener - handles failed messages
 * </ul>
 *
 * <p><strong>Message Flow:</strong>
 *
 * <pre>
 * EventBridge (cron)
 *     |
 *     v
 * eventbridge-trigger-queue --> EventBridgeTriggerSqsListener
 *     |                              |
 *     |                              v
 *     |                      TriggerCrawlTaskUseCase (creates CrawlTask)
 *     |                              |
 *     v                              v
 * crawl-task-queue -----------> CrawlTaskSqsListener
 *     |                              |
 *     |                              v
 *     |                      CrawlTaskExecutionUseCase (executes crawling)
 *     |
 *     v (on failure)
 * crawl-task-dlq ------------> CrawlTaskDlqListener
 * </pre>
 *
 * <p><strong>Component Scan 범위:</strong>
 *
 * <ul>
 *   <li>com.ryuqq.crawlinghub - Bootstrap (자체 패키지)
 *   <li>com.ryuqq.crawlinghub.application - Application Layer
 *   <li>com.ryuqq.crawlinghub.adapter.in.sqs - SQS Listener (Inbound)
 *   <li>com.ryuqq.crawlinghub.adapter.out.persistence - MySQL Persistence
 *   <li>com.ryuqq.crawlinghub.adapter.out.redis - Redis (Distributed Lock)
 *   <li>com.ryuqq.crawlinghub.adapter.out.sqs - SQS Publisher (Outbound)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication(
        scanBasePackages = {
            "com.ryuqq.crawlinghub",
            "com.ryuqq.crawlinghub.application",
            "com.ryuqq.crawlinghub.adapter.in.sqs",
            "com.ryuqq.crawlinghub.adapter.out.persistence",
            "com.ryuqq.crawlinghub.adapter.out.redis",
            "com.ryuqq.crawlinghub.adapter.out.sqs"
        })
@EntityScan(basePackages = {"com.ryuqq.crawlinghub.adapter.out.persistence"})
@ConfigurationPropertiesScan(
        basePackages = {
            "com.ryuqq.crawlinghub.adapter.in.sqs.config",
            "com.ryuqq.crawlinghub.adapter.out.sqs.config",
            "com.ryuqq.crawlinghub.adapter.out.redis.config"
        })
public class CrawlWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlWorkerApplication.class, args);
    }
}
