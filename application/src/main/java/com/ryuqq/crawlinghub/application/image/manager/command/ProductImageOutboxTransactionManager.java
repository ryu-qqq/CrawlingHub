package com.ryuqq.crawlinghub.application.image.manager.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.product.port.out.command.ImageOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductImageOutbox 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>ProductImageOutbox 영속성 관리
 *   <li>Outbox 상태 전환 관리
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>상태 전환</strong>:
 *
 * <pre>
 * PENDING → PROCESSING (파일서버 API 호출 시작)
 * PROCESSING → COMPLETED (웹훅 수신 - 업로드 성공)
 * PROCESSING → FAILED (타임아웃 또는 오류)
 * FAILED → PENDING (재시도 가능 시)
 * </pre>
 *
 * <p><strong>SRP</strong>: ProductImageOutbox Aggregate에 대한 상태 관리만 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductImageOutboxTransactionManager {

    private final ImageOutboxPersistencePort outboxPersistencePort;
    private final TimeProvider timeProvider;

    public ProductImageOutboxTransactionManager(
            ImageOutboxPersistencePort outboxPersistencePort, TimeProvider timeProvider) {
        this.outboxPersistencePort = outboxPersistencePort;
        this.timeProvider = timeProvider;
    }

    // === 생성 ===

    /**
     * Outbox 단건 저장
     *
     * @param outbox 저장할 Outbox
     */
    @Transactional
    public void persist(ProductImageOutbox outbox) {
        outboxPersistencePort.persist(outbox);
    }

    /**
     * Outbox 일괄 저장
     *
     * <p>Bundle에서 생성된 Outbox 목록을 저장합니다.
     *
     * @param outboxes 저장할 Outbox 목록
     */
    @Transactional
    public void persistAll(List<ProductImageOutbox> outboxes) {
        if (outboxes == null || outboxes.isEmpty()) {
            return;
        }
        outboxPersistencePort.persistAll(outboxes);
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
    public void markAsSent(ProductImageOutbox outbox) {
        outbox.markAsSent(timeProvider.now());
        outboxPersistencePort.update(outbox);
    }

    /**
     * 처리 시작 (파일서버 API 호출 시작)
     *
     * @param outbox 처리 시작할 Outbox
     */
    @Transactional
    public void markAsProcessing(ProductImageOutbox outbox) {
        outbox.markAsProcessing(timeProvider.now());
        outboxPersistencePort.update(outbox);
    }

    /**
     * 업로드 완료 (웹훅 수신)
     *
     * @param outbox 완료할 Outbox
     */
    @Transactional
    public void markAsCompleted(ProductImageOutbox outbox) {
        outbox.markAsCompleted(timeProvider.now());
        outboxPersistencePort.update(outbox);
    }

    /**
     * 처리 실패
     *
     * @param outbox 실패한 Outbox
     * @param errorMessage 오류 메시지
     */
    @Transactional
    public void markAsFailed(ProductImageOutbox outbox, String errorMessage) {
        outbox.markAsFailed(errorMessage, timeProvider.now());
        outboxPersistencePort.update(outbox);
    }

    /**
     * 재시도를 위해 PENDING으로 복귀
     *
     * @param outbox 재시도할 Outbox
     */
    @Transactional
    public void resetToPending(ProductImageOutbox outbox) {
        if (outbox.canRetry()) {
            outbox.resetToPending();
            outboxPersistencePort.update(outbox);
        }
    }
}
