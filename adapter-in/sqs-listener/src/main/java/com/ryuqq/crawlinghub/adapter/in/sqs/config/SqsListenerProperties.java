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

    /** CrawlTask DLQ URL */
    private String crawlTaskDlqUrl;

    /** ProductImage DLQ URL */
    private String productImageDlqUrl;

    /** ProductSync DLQ URL */
    private String productSyncDlqUrl;

    /** EventBridge Trigger DLQ URL */
    private String eventBridgeTriggerDlqUrl;

    /** CrawlTask 리스너 활성화 여부 */
    private boolean crawlTaskListenerEnabled = true;

    /** EventBridge 트리거 리스너 활성화 여부 */
    private boolean eventBridgeTriggerListenerEnabled = true;

    /** CrawlTask DLQ 리스너 활성화 여부 */
    private boolean crawlTaskDlqListenerEnabled = true;

    /** ProductImage DLQ 리스너 활성화 여부 */
    private boolean productImageDlqListenerEnabled = true;

    /** ProductSync DLQ 리스너 활성화 여부 */
    private boolean productSyncDlqListenerEnabled = true;

    /** EventBridge Trigger DLQ 리스너 활성화 여부 */
    private boolean eventBridgeTriggerDlqListenerEnabled = true;

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

    public boolean isCrawlTaskDlqListenerEnabled() {
        return crawlTaskDlqListenerEnabled;
    }

    public void setCrawlTaskDlqListenerEnabled(boolean crawlTaskDlqListenerEnabled) {
        this.crawlTaskDlqListenerEnabled = crawlTaskDlqListenerEnabled;
    }

    public String getProductImageDlqUrl() {
        return productImageDlqUrl;
    }

    public void setProductImageDlqUrl(String productImageDlqUrl) {
        this.productImageDlqUrl = productImageDlqUrl;
    }

    public String getProductSyncDlqUrl() {
        return productSyncDlqUrl;
    }

    public void setProductSyncDlqUrl(String productSyncDlqUrl) {
        this.productSyncDlqUrl = productSyncDlqUrl;
    }

    public String getEventBridgeTriggerDlqUrl() {
        return eventBridgeTriggerDlqUrl;
    }

    public void setEventBridgeTriggerDlqUrl(String eventBridgeTriggerDlqUrl) {
        this.eventBridgeTriggerDlqUrl = eventBridgeTriggerDlqUrl;
    }

    public boolean isProductImageDlqListenerEnabled() {
        return productImageDlqListenerEnabled;
    }

    public void setProductImageDlqListenerEnabled(boolean productImageDlqListenerEnabled) {
        this.productImageDlqListenerEnabled = productImageDlqListenerEnabled;
    }

    public boolean isProductSyncDlqListenerEnabled() {
        return productSyncDlqListenerEnabled;
    }

    public void setProductSyncDlqListenerEnabled(boolean productSyncDlqListenerEnabled) {
        this.productSyncDlqListenerEnabled = productSyncDlqListenerEnabled;
    }

    public boolean isEventBridgeTriggerDlqListenerEnabled() {
        return eventBridgeTriggerDlqListenerEnabled;
    }

    public void setEventBridgeTriggerDlqListenerEnabled(
            boolean eventBridgeTriggerDlqListenerEnabled) {
        this.eventBridgeTriggerDlqListenerEnabled = eventBridgeTriggerDlqListenerEnabled;
    }
}
