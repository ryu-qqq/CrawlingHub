package com.ryuqq.crawlinghub.application.sync.scheduler;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import com.ryuqq.crawlinghub.application.sync.dto.command.SyncRetryResult;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RetrySyncUseCase;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 외부 서버 동기화 Outbox 재시도 스케줄러
 *
 * <p><strong>용도</strong>: 실패한 외부 서버 동기화 요청을 주기적으로 재시도
 *
 * <p><strong>실행 주기</strong>: 5분마다 (설정 가능)
 *
 * <p><strong>핵심 원칙</strong>:
 *
 * <ul>
 *   <li>UseCase를 통한 비즈니스 위임 (Port 직접 호출 금지)
 *   <li>분산 락으로 중복 실행 방지
 *   <li>최대 반복 횟수 제한 (무한 루프 방지)
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>분산 락 획득 시도
 *   <li>UseCase 호출하여 재시도 대상 처리
 *   <li>결과 로깅
 * </ol>
 *
 * <p><strong>활성화 조건</strong>: {@code scheduler.sync-outbox-retry.enabled=true}
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.sync-outbox-retry.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class SyncOutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SyncOutboxRetryScheduler.class);
    private static final String JOB_NAME = "sync-outbox-retry";
    private static final String LOCK_KEY = "scheduler:sync-outbox-retry";
    private static final int MAX_ITERATIONS = 10;
    private static final long LOCK_WAIT_TIME = 0;
    private static final long LOCK_LEASE_TIME = 300;

    private final RetrySyncUseCase retrySyncUseCase;
    private final DistributedLockPort distributedLockPort;

    public SyncOutboxRetryScheduler(
            RetrySyncUseCase retrySyncUseCase, DistributedLockPort distributedLockPort) {
        this.retrySyncUseCase = retrySyncUseCase;
        this.distributedLockPort = distributedLockPort;
    }

    /**
     * 실패한 외부 서버 동기화를 재시도합니다.
     *
     * <p>5분마다 실행됩니다.
     */
    @Scheduled(fixedDelayString = "${scheduler.sync-outbox-retry.fixed-delay:300000}")
    public void retry() {
        log.info("[{}] Starting scheduled job", JOB_NAME);

        boolean executed =
                distributedLockPort.executeWithLock(
                        LOCK_KEY,
                        LOCK_WAIT_TIME,
                        LOCK_LEASE_TIME,
                        TimeUnit.SECONDS,
                        this::executeRetry);

        if (!executed) {
            log.info("[{}] Lock not acquired, skipping", JOB_NAME);
        }
    }

    private void executeRetry() {
        try {
            SyncRetryResult totalResult = executeWithLimit();

            log.info(
                    "[{}] Completed. processed={}, succeeded={}, failed={}",
                    JOB_NAME,
                    totalResult.processed(),
                    totalResult.succeeded(),
                    totalResult.failed());

        } catch (Exception e) {
            log.error("[{}] Failed", JOB_NAME, e);
            throw e;
        }
    }

    /** 최대 반복 횟수 제한하여 실행 */
    private SyncRetryResult executeWithLimit() {
        int totalProcessed = 0;
        int totalSucceeded = 0;
        int totalFailed = 0;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            SyncRetryResult batchResult = retrySyncUseCase.execute();

            totalProcessed += batchResult.processed();
            totalSucceeded += batchResult.succeeded();
            totalFailed += batchResult.failed();

            if (!batchResult.hasMore()) {
                break;
            }

            log.debug(
                    "[{}] Iteration {} completed. processed={}",
                    JOB_NAME,
                    i + 1,
                    batchResult.processed());
        }

        return SyncRetryResult.of(totalProcessed, totalSucceeded, totalFailed, false);
    }
}
