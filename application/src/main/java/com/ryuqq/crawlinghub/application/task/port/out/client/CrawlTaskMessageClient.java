package com.ryuqq.crawlinghub.application.task.port.out.client;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;

/**
 * CrawlTask 메시지 발행 Port (Port Out - Messaging)
 *
 * <p>SQS 메시지 발행을 위한 추상화 인터페이스
 *
 * <p><strong>사용 시점</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromOutbox(CrawlTaskOutbox)}: 재시도 스케줄러에서 아웃박스 기반 호출
 *   <li>{@link #publish(CrawlTask, String)}: 직접 발행 (레거시 지원)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskMessageClient {

    /**
     * CrawlTask 메시지 발행
     *
     * <p>Transaction commit 후 호출되어야 함 (afterCommit)
     *
     * @param crawlTask 발행할 CrawlTask
     * @param idempotencyKey 멱등성 키 (중복 발행 방지)
     */
    void publish(CrawlTask crawlTask, String idempotencyKey);

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 아웃박스에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 아웃박스
     */
    void publishFromOutbox(CrawlTaskOutbox outbox);
}
