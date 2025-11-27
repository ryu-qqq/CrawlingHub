package com.ryuqq.crawlinghub.adapter.out.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS SQS 설정 Properties
 *
 * <p><strong>설정 예시 (application.yml)</strong>:
 *
 * <pre>{@code
 * aws:
 *   sqs:
 *     region: ap-northeast-2
 *     crawl-task-queue-url: https://sqs.ap-northeast-2.amazonaws.com/123456789012/crawl-task-queue
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "aws.sqs")
public class SqsProperties {

    /** AWS 리전 (기본값: ap-northeast-2) */
    private String region = "ap-northeast-2";

    /** CrawlTask 큐 URL */
    private String crawlTaskQueueUrl;

    /** 메시지 그룹 ID prefix (FIFO 큐용) */
    private String messageGroupIdPrefix = "crawl-task-";

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCrawlTaskQueueUrl() {
        return crawlTaskQueueUrl;
    }

    public void setCrawlTaskQueueUrl(String crawlTaskQueueUrl) {
        this.crawlTaskQueueUrl = crawlTaskQueueUrl;
    }

    public String getMessageGroupIdPrefix() {
        return messageGroupIdPrefix;
    }

    public void setMessageGroupIdPrefix(String messageGroupIdPrefix) {
        this.messageGroupIdPrefix = messageGroupIdPrefix;
    }
}
