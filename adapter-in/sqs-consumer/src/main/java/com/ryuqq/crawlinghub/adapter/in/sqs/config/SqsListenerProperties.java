package com.ryuqq.crawlinghub.adapter.in.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SQS Listener 설정 Properties
 *
 * <p><strong>설정 예시 (application.yml)</strong>:
 *
 * <pre>{@code
 * aws:
 *   sqs:
 *     listener:
 *       crawl-task-queue-url: https://sqs.ap-northeast-2.amazonaws.com/.../crawl-task-queue
 *       event-bridge-trigger-queue-url: https://sqs.ap-northeast-2.amazonaws.com/.../eventbridge-trigger-queue
 *       product-sync-queue-url: https://sqs.ap-northeast-2.amazonaws.com/.../product-sync-queue
 *       crawl-task-dlq-url: https://sqs.ap-northeast-2.amazonaws.com/.../crawl-task-dlq
 * }</pre>
 *
 * <p>Spring Cloud AWS SQS의 @SqsListener에서 사용하는 큐 URL 설정
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "aws.sqs.listener")
public class SqsListenerProperties {

    /** CrawlTask 큐 URL */
    private String crawlTaskQueueUrl;

    /** EventBridge 트리거 큐 URL */
    private String eventBridgeTriggerQueueUrl;

    /** ProductSync 큐 URL */
    private String productSyncQueueUrl;

    /** CrawlTask DLQ URL */
    private String crawlTaskDlqUrl;

    /** CrawlTask 리스너 활성화 여부 */
    private boolean crawlTaskListenerEnabled = true;

    /** EventBridge 트리거 리스너 활성화 여부 */
    private boolean eventBridgeTriggerListenerEnabled = true;

    /** ProductSync 리스너 활성화 여부 */
    private boolean productSyncListenerEnabled = true;

    /** CrawlTask DLQ 리스너 활성화 여부 */
    private boolean crawlTaskDlqListenerEnabled = true;

    public String getCrawlTaskQueueUrl() {
        return crawlTaskQueueUrl;
    }

    public void setCrawlTaskQueueUrl(String crawlTaskQueueUrl) {
        this.crawlTaskQueueUrl = crawlTaskQueueUrl;
    }

    public String getEventBridgeTriggerQueueUrl() {
        return eventBridgeTriggerQueueUrl;
    }

    public void setEventBridgeTriggerQueueUrl(String eventBridgeTriggerQueueUrl) {
        this.eventBridgeTriggerQueueUrl = eventBridgeTriggerQueueUrl;
    }

    public String getProductSyncQueueUrl() {
        return productSyncQueueUrl;
    }

    public void setProductSyncQueueUrl(String productSyncQueueUrl) {
        this.productSyncQueueUrl = productSyncQueueUrl;
    }

    public String getCrawlTaskDlqUrl() {
        return crawlTaskDlqUrl;
    }

    public void setCrawlTaskDlqUrl(String crawlTaskDlqUrl) {
        this.crawlTaskDlqUrl = crawlTaskDlqUrl;
    }

    public boolean isCrawlTaskListenerEnabled() {
        return crawlTaskListenerEnabled;
    }

    public void setCrawlTaskListenerEnabled(boolean crawlTaskListenerEnabled) {
        this.crawlTaskListenerEnabled = crawlTaskListenerEnabled;
    }

    public boolean isEventBridgeTriggerListenerEnabled() {
        return eventBridgeTriggerListenerEnabled;
    }

    public void setEventBridgeTriggerListenerEnabled(boolean eventBridgeTriggerListenerEnabled) {
        this.eventBridgeTriggerListenerEnabled = eventBridgeTriggerListenerEnabled;
    }

    public boolean isProductSyncListenerEnabled() {
        return productSyncListenerEnabled;
    }

    public void setProductSyncListenerEnabled(boolean productSyncListenerEnabled) {
        this.productSyncListenerEnabled = productSyncListenerEnabled;
    }

    public boolean isCrawlTaskDlqListenerEnabled() {
        return crawlTaskDlqListenerEnabled;
    }

    public void setCrawlTaskDlqListenerEnabled(boolean crawlTaskDlqListenerEnabled) {
        this.crawlTaskDlqListenerEnabled = crawlTaskDlqListenerEnabled;
    }
}
