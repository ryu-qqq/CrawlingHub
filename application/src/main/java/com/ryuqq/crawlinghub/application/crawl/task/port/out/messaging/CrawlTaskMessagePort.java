package com.ryuqq.crawlinghub.application.crawl.task.port.out.messaging;

import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;

/**
 * CrawlTask 메시지 발행 Port (Port Out - Messaging)
 *
 * <p>SQS 메시지 발행을 위한 추상화 인터페이스
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskMessagePort {

    /**
     * CrawlTask 메시지 발행
     *
     * <p>Transaction commit 후 호출되어야 함 (afterCommit)
     *
     * @param crawlTask 발행할 CrawlTask
     * @param idempotencyKey 멱등성 키 (중복 발행 방지)
     */
    void publish(CrawlTask crawlTask, String idempotencyKey);
}
