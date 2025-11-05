package com.ryuqq.crawlinghub.application.task.port.out;

/**
 * Outbox 저장 Port (Transactional Outbox Pattern)
 *
 * <p>⚠️ 트랜잭션 경계:
 * <ul>
 *   <li>Outbox 저장은 트랜잭션 내부에서 실행 (DB 작업)</li>
 *   <li>실제 SQS 발행은 별도 Polling Worker가 담당</li>
 * </ul>
 *
 * <p>Outbox Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface OutboxPort {

    /**
     * Outbox에 메시지 저장
     *
     * <p>트랜잭션 내부에서 실행되므로 안전합니다.
     * 실제 SQS 발행은 별도 Polling Worker가 처리합니다.
     *
     * @param aggregateType Aggregate 유형 (예: "CrawlTask")
     * @param aggregateId   Aggregate ID
     * @param eventType     이벤트 유형 (예: "CrawlTaskCreated")
     * @param payload       메시지 페이로드 (JSON)
     */
    void saveOutboxMessage(
        String aggregateType,
        Long aggregateId,
        String eventType,
        String payload
    );
}
