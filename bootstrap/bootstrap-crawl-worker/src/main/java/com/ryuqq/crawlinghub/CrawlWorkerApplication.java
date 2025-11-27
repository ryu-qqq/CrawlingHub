package com.ryuqq.crawlinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication
public class CrawlWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlWorkerApplication.class, args);
    }
}
