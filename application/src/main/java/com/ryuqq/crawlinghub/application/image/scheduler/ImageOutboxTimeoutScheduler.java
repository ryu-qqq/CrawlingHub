package com.ryuqq.crawlinghub.application.image.scheduler;

import com.ryuqq.crawlinghub.application.common.port.out.lock.DistributedLockPort;
import com.ryuqq.crawlinghub.application.image.dto.response.ImageOutboxTimeoutResponse;
import com.ryuqq.crawlinghub.application.image.port.in.command.HandleImageOutboxTimeoutUseCase;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 이미지 Outbox 타임아웃 처리 스케줄러
 *
 * <p><strong>용도</strong>: PROCESSING 상태로 장시간 머물러 있는 Outbox를 FAILED로 변경
 *
 * <p><strong>실행 주기</strong>: 5분마다 (설정 가능)
 *
 * <p><strong>타임아웃 조건</strong>:
 *
 * <ul>
 *   <li>PROCESSING 상태
 *   <li>processedAt 기준 10분 경과
 * </ul>
 *
 * <p><strong>핵심 원칙</strong>:
 *
 * <ul>
 *   <li>UseCase를 통한 비즈니스 위임 (Port 직접 호출 금지)
 *   <li>분산 락으로 중복 실행 방지
 *   <li>최대 반복 횟수 제한 (무한 루프 방지)
 * </ul>
 *
 * <p><strong>활성화 조건</strong>: {@code scheduler.image-outbox-timeout.enabled=true}
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.image-outbox-timeout.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ImageOutboxTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(ImageOutboxTimeoutScheduler.class);
    private static final String JOB_NAME = "image-outbox-timeout";
    private static final String LOCK_KEY = "scheduler:image-outbox-timeout";
    private static final int MAX_ITERATIONS = 10;
    private static final long LOCK_WAIT_TIME = 0;
    private static final long LOCK_LEASE_TIME = 300;

    private final HandleImageOutboxTimeoutUseCase handleImageOutboxTimeoutUseCase;
    private final DistributedLockPort distributedLockPort;

    public ImageOutboxTimeoutScheduler(
            HandleImageOutboxTimeoutUseCase handleImageOutboxTimeoutUseCase,
            DistributedLockPort distributedLockPort) {
        this.handleImageOutboxTimeoutUseCase = handleImageOutboxTimeoutUseCase;
        this.distributedLockPort = distributedLockPort;
    }

    /**
     * 타임아웃된 이미지 Outbox를 처리합니다.
     *
     * <p>5분마다 실행됩니다.
     */
    @Scheduled(fixedDelayString = "${scheduler.image-outbox-timeout.fixed-delay:300000}")
    public void handleTimeout() {
        log.info("[{}] Starting scheduled job", JOB_NAME);

        boolean executed =
                distributedLockPort.executeWithLock(
                        LOCK_KEY,
                        LOCK_WAIT_TIME,
                        LOCK_LEASE_TIME,
                        TimeUnit.SECONDS,
                        this::executeTimeoutHandling);

        if (!executed) {
            log.info("[{}] Lock not acquired, skipping", JOB_NAME);
        }
    }

    private void executeTimeoutHandling() {
        try {
            int totalProcessed = executeWithLimit();

            log.info("[{}] Completed. totalProcessed={}", JOB_NAME, totalProcessed);

        } catch (Exception e) {
            log.error("[{}] Failed", JOB_NAME, e);
            throw e;
        }
    }

    /** 최대 반복 횟수 제한하여 실행 */
    private int executeWithLimit() {
        int totalProcessed = 0;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            ImageOutboxTimeoutResponse batchResult = handleImageOutboxTimeoutUseCase.execute();

            totalProcessed += batchResult.processed();

            if (!batchResult.hasMore()) {
                break;
            }

            log.debug(
                    "[{}] Iteration {} completed. processed={}",
                    JOB_NAME,
                    i + 1,
                    batchResult.processed());
        }

        return totalProcessed;
    }
}
