package com.ryuqq.crawlinghub.application.sync.port.out.messaging;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;

/**
 * ProductSync 메시지 발행 Port (Port Out - Messaging)
 *
 * <p>SQS 메시지 발행을 위한 추상화 인터페이스
 *
 * <p><strong>사용 시점</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromEvent(ExternalSyncRequestedEvent)}: 트랜잭션 커밋 후 이벤트 리스너에서 호출
 *   <li>{@link #publishFromOutbox(CrawledProductSyncOutbox)}: 재시도 스케줄러에서 아웃박스 기반 호출
 * </ul>
 *
 * <p><strong>Transactional Outbox 패턴</strong>:
 *
 * <pre>
 * 1. 도메인 저장 시 Outbox 함께 저장 (같은 트랜잭션)
 * 2. TransactionEventListener에서 SQS 발행
 * 3. 발행 실패 시 Outbox PENDING 상태 유지
 * 4. 스케줄러가 PENDING/FAILED 상태 Outbox 재처리
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProductSyncMessagePort {

    /**
     * CrawledProductSyncOutbox 기반 메시지 발행 (일반 발행)
     *
     * <p>Transaction commit 후 호출되어야 함 (afterCommit)
     *
     * @param outbox 발행할 CrawledProductSyncOutbox
     */
    void publish(CrawledProductSyncOutbox outbox);

    /**
     * ExternalSyncRequestedEvent 기반 메시지 발행
     *
     * <p>트랜잭션 커밋 후 이벤트 리스너에서 호출
     *
     * @param event 외부 동기화 요청 이벤트
     */
    void publishFromEvent(ExternalSyncRequestedEvent event);

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 아웃박스에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 아웃박스
     */
    void publishFromOutbox(CrawledProductSyncOutbox outbox);
}
