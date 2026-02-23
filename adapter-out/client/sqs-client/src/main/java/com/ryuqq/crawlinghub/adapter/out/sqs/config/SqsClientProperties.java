package com.ryuqq.crawlinghub.adapter.out.sqs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS SQS 클라이언트 Properties
 *
 * <p>sqs.yml에서 설정을 읽어옵니다.
 *
 * <p><strong>설정 예시 (sqs.yml)</strong>:
 *
 * <pre>{@code
 * sqs:
 *   region: ap-northeast-2
 *   queues:
 *     crawl-task: https://sqs.ap-northeast-2.amazonaws.com/.../crawl-task-queue
 *     product-sync: https://sqs.ap-northeast-2.amazonaws.com/.../product-sync-queue
 * }</pre>
 */
@ConfigurationProperties(prefix = "sqs")
public class SqsClientProperties {

    private String region = "ap-northeast-2";
    private String endpoint;
    private String messageGroupIdPrefix = "crawl-task-";
    private Queues queues = new Queues();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMessageGroupIdPrefix() {
        return messageGroupIdPrefix;
    }

    public void setMessageGroupIdPrefix(String messageGroupIdPrefix) {
        this.messageGroupIdPrefix = messageGroupIdPrefix;
    }

    public Queues getQueues() {
        return queues;
    }

    public void setQueues(Queues queues) {
        this.queues = queues;
    }

    public static class Queues {

        private String crawlTask;
        private String productSync;

        public String getCrawlTask() {
            return crawlTask;
        }

        public void setCrawlTask(String crawlTask) {
            this.crawlTask = crawlTask;
        }

        public String getProductSync() {
            return productSync;
        }

        public void setProductSync(String productSync) {
            this.productSync = productSync;
        }
    }
}
