package com.ryuqq.crawlinghub.application.sync.manager.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.product.dto.bundle.SyncOutboxBundle;
import com.ryuqq.crawlinghub.application.product.port.out.command.SyncOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 외부 동기화 Outbox 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SyncOutbox 영속성 관리
 *   <li>Outbox 상태 전환 관리
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>상태 전환</strong>:
 *
 * <pre>
 * PENDING → PROCESSING (외부 서버 API 호출 시작)
 * PROCESSING → COMPLETED (API 호출 성공)
 * PROCESSING → FAILED (API 호출 실패)
 * FAILED → PENDING (재시도 가능 시)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncOutboxTransactionManager {

    private final SyncOutboxPersistencePort syncOutboxPersistencePort;
    private final TimeProvider timeProvider;

    public SyncOutboxTransactionManager(
            SyncOutboxPersistencePort syncOutboxPersistencePort, TimeProvider timeProvider) {
        this.syncOutboxPersistencePort = syncOutboxPersistencePort;
        this.timeProvider = timeProvider;
    }

    // === 영속화 ===

    /**
     * SyncOutboxBundle의 Outbox 영속화
     *
     * <p>Factory에서 생성한 Bundle의 Outbox를 저장합니다.
     *
     * @param bundle Factory에서 생성한 SyncOutboxBundle
     */
    @Transactional
    public void persist(SyncOutboxBundle bundle) {
        syncOutboxPersistencePort.persist(bundle.outbox());
    }

    /**
     * SyncOutbox 영속화 (수동 동기화 트리거용)
     *
     * <p>수동 동기화 시 Event 없이 Outbox만 저장합니다.
     *
     * @param outbox 저장할 SyncOutbox
     */
    @Transactional
    public void persist(CrawledProductSyncOutbox outbox) {
        syncOutboxPersistencePort.persist(outbox);
    }

    // === 상태 전환 ===

    /**
     * SQS 발행 완료 (Scheduler에서 SQS 메시지 발행 성공)
     *
     * <p>PENDING/FAILED → SENT 상태 전환
     *
     * @param outbox SQS 발행된 Outbox
     */
    @Transactional
    public void markAsSent(CrawledProductSyncOutbox outbox) {
        outbox.markAsSent(timeProvider.now());
        syncOutboxPersistencePort.update(outbox);
    }

    /**
     * 처리 시작 (외부 서버 API 호출 시작)
     *
     * @param outbox 처리 시작할 Outbox
     */
    @Transactional
    public void markAsProcessing(CrawledProductSyncOutbox outbox) {
        outbox.markAsProcessing(timeProvider.now());
        syncOutboxPersistencePort.update(outbox);
    }

    /**
     * 동기화 완료 (신규 등록 시 외부 ID 저장)
     *
     * @param outbox 완료할 Outbox
     * @param externalProductId 외부 상품 ID (신규 등록 시)
     */
    @Transactional
    public void markAsCompleted(CrawledProductSyncOutbox outbox, Long externalProductId) {
        outbox.markAsCompleted(externalProductId, timeProvider.now());
        syncOutboxPersistencePort.update(outbox);
    }

    /**
     * 처리 실패
     *
     * @param outbox 실패한 Outbox
     * @param errorMessage 오류 메시지
     */
    @Transactional
    public void markAsFailed(CrawledProductSyncOutbox outbox, String errorMessage) {
        outbox.markAsFailed(errorMessage, timeProvider.now());
        syncOutboxPersistencePort.update(outbox);
    }

    /**
     * 재시도를 위해 PENDING으로 복귀
     *
     * @param outbox 재시도할 Outbox
     */
    @Transactional
    public void resetToPending(CrawledProductSyncOutbox outbox) {
        if (outbox.canRetry()) {
            outbox.resetToPending();
            syncOutboxPersistencePort.update(outbox);
        }
    }
}
