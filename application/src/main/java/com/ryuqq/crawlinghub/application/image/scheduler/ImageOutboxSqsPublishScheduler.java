package com.ryuqq.crawlinghub.application.image.scheduler;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.image.manager.messaging.ProductImageMessageManager;
import com.ryuqq.crawlinghub.application.image.manager.query.ProductImageOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 이미지 Outbox SQS 발행 스케줄러 (백업용)
 *
 * <p><strong>용도</strong>: EventListener에서 SQS 발행 실패한 PENDING/FAILED 상태의 ImageOutbox를 재발행
 *
 * <p><strong>실행 주기</strong>: 30초마다 (설정 가능)
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>분산 락 획득
 *   <li>PENDING/FAILED Outbox 조회 (limit 단위)
 *   <li>각 Outbox에 대해 SQS 메시지 발행
 *   <li>발행 성공 시 SENT 상태로 전환
 *   <li>발행 실패 시 FAILED 상태로 전환 (재시도 횟수 증가)
 * </ol>
 *
 * <p><strong>활성화 조건</strong>: scheduler.image-outbox-sqs.enabled=true
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "scheduler.image-outbox-sqs.enabled", havingValue = "true")
public class ImageOutboxSqsPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(ImageOutboxSqsPublishScheduler.class);
    private static final String JOB_NAME = "image-outbox-sqs-publish";
    private static final String LOCK_KEY = "scheduler:image-outbox-sqs-publish";
    private static final int MAX_ITERATIONS = 10;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long LOCK_WAIT_TIME = 0;
    private static final long LOCK_LEASE_TIME = 300;

    private final ProductImageOutboxReadManager readManager;
    private final ProductImageOutboxTransactionManager transactionManager;
    private final ProductImageMessageManager messageManager;
    private final DistributedLockPort distributedLockPort;

    public ImageOutboxSqsPublishScheduler(
            ProductImageOutboxReadManager readManager,
            ProductImageOutboxTransactionManager transactionManager,
            ProductImageMessageManager messageManager,
            DistributedLockPort distributedLockPort) {
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.messageManager = messageManager;
        this.distributedLockPort = distributedLockPort;
    }

    /**
     * PENDING/FAILED Outbox를 SQS로 발행합니다.
     *
     * <p>30초마다 실행됩니다.
     */
    @Scheduled(fixedDelayString = "${scheduler.image-outbox-sqs.fixed-delay:30000}")
    public void publishPendingOutboxes() {
        log.debug("[{}] Starting scheduled job", JOB_NAME);

        boolean executed =
                distributedLockPort.executeWithLock(
                        LOCK_KEY,
                        LOCK_WAIT_TIME,
                        LOCK_LEASE_TIME,
                        TimeUnit.SECONDS,
                        this::executePublish);

        if (!executed) {
            log.debug("[{}] Lock not acquired, skipping", JOB_NAME);
        }
    }

    private void executePublish() {
        try {
            int totalPublished = executeWithLimit();

            if (totalPublished > 0) {
                log.info("[{}] Completed. totalPublished={}", JOB_NAME, totalPublished);
            } else {
                log.debug("[{}] Completed. No outboxes to publish", JOB_NAME);
            }

        } catch (Exception e) {
            log.error("[{}] Failed", JOB_NAME, e);
            throw e;
        }
    }

    /** 최대 반복 횟수 제한하여 실행 */
    private int executeWithLimit() {
        int totalPublished = 0;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            List<ProductImageOutbox> outboxes =
                    readManager.findRetryableOutboxes(MAX_RETRY_COUNT, BATCH_SIZE);

            if (outboxes.isEmpty()) {
                break;
            }

            int published = publishOutboxes(outboxes);
            totalPublished += published;

            if (outboxes.size() < BATCH_SIZE) {
                break;
            }

            log.debug("[{}] Iteration {} completed. published={}", JOB_NAME, i + 1, published);
        }

        return totalPublished;
    }

    private int publishOutboxes(List<ProductImageOutbox> outboxes) {
        int successCount = 0;

        for (ProductImageOutbox outbox : outboxes) {
            if (publishSingleOutbox(outbox)) {
                successCount++;
            }
        }

        return successCount;
    }

    private boolean publishSingleOutbox(ProductImageOutbox outbox) {
        try {
            messageManager.publish(outbox);
            transactionManager.markAsSent(outbox);

            log.debug(
                    "ImageOutbox SQS 발행 성공: outboxId={}, imageId={}",
                    outbox.getId(),
                    outbox.getCrawledProductImageId());
            return true;

        } catch (Exception e) {
            transactionManager.markAsFailed(outbox, "SQS 발행 실패: " + e.getMessage());

            log.warn(
                    "ImageOutbox SQS 발행 실패: outboxId={}, imageId={}, error={}",
                    outbox.getId(),
                    outbox.getCrawledProductImageId(),
                    e.getMessage());
            return false;
        }
    }
}
