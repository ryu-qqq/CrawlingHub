package com.ryuqq.crawlinghub.application.sync.manager.scheduler;

import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.sync.manager.messaging.ProductSyncMessageManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductSyncOutboxCriteria;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SyncOutbox SQS 발행 스케줄러 Manager
 *
 * <p><strong>책임</strong>: Outbox 스케줄러에서 PENDING/FAILED Outbox를 조회하여 SQS로 발행
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>Criteria 기반으로 PENDING/FAILED Outbox 조회
 *   <li>각 Outbox에 대해 SQS 메시지 발행
 *   <li>성공 시 상태 → SENT
 *   <li>실패 시 상태 → FAILED (재시도 횟수 증가)
 * </ol>
 *
 * <p><strong>조건</strong>: app.messaging.sqs.enabled=true 일 때만 활성화
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "app.messaging.sqs.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class SyncOutboxSqsPublishManager {

    private static final Logger log = LoggerFactory.getLogger(SyncOutboxSqsPublishManager.class);

    private final SyncOutboxQueryPort syncOutboxQueryPort;
    private final SyncOutboxTransactionManager transactionManager;
    private final ProductSyncMessageManager messageManager;

    public SyncOutboxSqsPublishManager(
            SyncOutboxQueryPort syncOutboxQueryPort,
            SyncOutboxTransactionManager transactionManager,
            ProductSyncMessageManager messageManager) {
        this.syncOutboxQueryPort = syncOutboxQueryPort;
        this.transactionManager = transactionManager;
        this.messageManager = messageManager;
    }

    /**
     * PENDING 상태의 Outbox를 SQS로 발행
     *
     * <p>스케줄러에서 주기적으로 호출됩니다.
     *
     * @param limit 조회 개수 제한
     * @return 발행 성공 건수
     */
    public int publishPendingOutboxes(int limit) {
        ProductSyncOutboxCriteria criteria =
                ProductSyncOutboxCriteria.byStatus(ProductOutboxStatus.PENDING, limit);
        return publishByCriteria(criteria, "PENDING");
    }

    /**
     * PENDING 또는 FAILED 상태의 Outbox를 SQS로 발행
     *
     * @param limit 조회 개수 제한
     * @return 발행 성공 건수
     */
    public int publishPendingOrFailedOutboxes(int limit) {
        ProductSyncOutboxCriteria criteria = ProductSyncOutboxCriteria.pendingOrFailed(limit);
        return publishByCriteria(criteria, "PENDING/FAILED");
    }

    /**
     * 재시도 가능한 Outbox를 SQS로 발행
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 발행 성공 건수
     */
    public int publishRetryableOutboxes(int maxRetryCount, int limit) {
        ProductSyncOutboxCriteria criteria =
                ProductSyncOutboxCriteria.retryable(maxRetryCount, limit);
        return publishByCriteria(criteria, "RETRYABLE");
    }

    private int publishByCriteria(ProductSyncOutboxCriteria criteria, String criteriaType) {
        List<CrawledProductSyncOutbox> outboxes = syncOutboxQueryPort.findByCriteria(criteria);

        if (outboxes.isEmpty()) {
            log.debug("SyncOutbox SQS 발행 대상 없음: criteriaType={}", criteriaType);
            return 0;
        }

        log.info("SyncOutbox SQS 발행 시작: criteriaType={}, count={}", criteriaType, outboxes.size());

        int successCount = 0;
        for (CrawledProductSyncOutbox outbox : outboxes) {
            if (publishSingleOutbox(outbox)) {
                successCount++;
            }
        }

        log.info(
                "SyncOutbox SQS 발행 완료: criteriaType={}, total={}, success={}, failed={}",
                criteriaType,
                outboxes.size(),
                successCount,
                outboxes.size() - successCount);

        return successCount;
    }

    private boolean publishSingleOutbox(CrawledProductSyncOutbox outbox) {
        try {
            // SQS 메시지 발행
            messageManager.publish(outbox);

            // 성공 시 SENT 상태로 변경
            transactionManager.markAsSent(outbox);

            log.debug(
                    "SyncOutbox SQS 발행 성공: outboxId={}, productId={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue());
            return true;

        } catch (Exception e) {
            // 실패 시 FAILED 상태로 변경
            transactionManager.markAsFailed(outbox, "SQS 발행 실패: " + e.getMessage());

            log.warn(
                    "SyncOutbox SQS 발행 실패: outboxId={}, productId={}, error={}",
                    outbox.getId(),
                    outbox.getCrawledProductIdValue(),
                    e.getMessage());
            return false;
        }
    }
}
