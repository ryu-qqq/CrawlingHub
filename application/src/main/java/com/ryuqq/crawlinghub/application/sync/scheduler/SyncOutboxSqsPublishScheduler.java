package com.ryuqq.crawlinghub.application.sync.scheduler;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import com.ryuqq.crawlinghub.application.sync.manager.scheduler.SyncOutboxSqsPublishManager;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 외부 동기화 Outbox SQS 발행 스케줄러 (백업용)
 *
 * <p><strong>용도</strong>: EventListener에서 SQS 발행 실패한 PENDING/FAILED 상태의 SyncOutbox를 재발행
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
 * <p><strong>활성화 조건</strong>: scheduler.sync-outbox-sqs.enabled=true
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "scheduler.sync-outbox-sqs.enabled", havingValue = "true")
public class SyncOutboxSqsPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(SyncOutboxSqsPublishScheduler.class);
    private static final String JOB_NAME = "sync-outbox-sqs-publish";
    private static final String LOCK_KEY = "scheduler:sync-outbox-sqs-publish";
    private static final int MAX_ITERATIONS = 10;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long LOCK_WAIT_TIME = 0;
    private static final long LOCK_LEASE_TIME = 300;

    private final SyncOutboxSqsPublishManager publishManager;
    private final DistributedLockPort distributedLockPort;

    public SyncOutboxSqsPublishScheduler(
            SyncOutboxSqsPublishManager publishManager, DistributedLockPort distributedLockPort) {
        this.publishManager = publishManager;
        this.distributedLockPort = distributedLockPort;
    }

    /**
     * PENDING/FAILED Outbox를 SQS로 발행합니다.
     *
     * <p>30초마다 실행됩니다.
     */
    @Scheduled(fixedDelayString = "${scheduler.sync-outbox-sqs.fixed-delay:30000}")
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
            // PENDING 또는 재시도 가능한 FAILED Outbox 발행
            int published = publishManager.publishRetryableOutboxes(MAX_RETRY_COUNT, BATCH_SIZE);

            totalPublished += published;

            // 더 이상 발행할 Outbox가 없으면 종료
            if (published < BATCH_SIZE) {
                break;
            }

            log.debug("[{}] Iteration {} completed. published={}", JOB_NAME, i + 1, published);
        }

        return totalPublished;
    }
}
