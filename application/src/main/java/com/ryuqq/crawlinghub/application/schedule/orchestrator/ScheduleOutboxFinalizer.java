package com.ryuqq.crawlinghub.application.schedule.orchestrator;

import com.ryuqq.crawlinghub.application.schedule.port.out.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 *
 */


/**
 * Schedule Outbox Finalizer (S3 Phase - Finalize)
 *
 * <p>Orchestration Pattern 3-Phase Lifecycle의 S3 Phase를 담당합니다:
 * <ul>
 *   <li>S1 (Accept): Facade가 DB + Outbox 저장 완료</li>
 *   <li>S2 (Execute): Processor가 Outbox를 읽고 EventBridge 호출</li>
 *   <li>S3 (Finalize): **이 Finalizer가 재시도 및 정리** ✅</li>
 * </ul>
 *
 * <p>핵심 책임:
 * <ul>
 *   <li>✅ 실패한 Outbox 재시도 (maxRetries 미만)</li>
 *   <li>✅ 완료된 Outbox 정리 (일정 시간 경과 후)</li>
 *   <li>✅ 영구 실패 Outbox 로깅 (재시도 초과)</li>
 * </ul>
 *
 * <p>실행 주기:
 * <ul>
 *   <li>재시도: 10분마다 (`cron = "0 ㅁ/10 * * * *")</li>
 *   <li>정리: 매 시간 (`cron = "0 0 * * * *"`)</li>
 * </ul>
 *
 * @author 개발자
 * @since 2024-01-01
 */

@Component
public class ScheduleOutboxFinalizer {

    private static final Logger log = LoggerFactory.getLogger(ScheduleOutboxFinalizer.class);

    /**
     * 완료된 Outbox 보관 시간 (시간 단위)
     * 24시간 경과 후 정리
     */
    private static final int RETENTION_HOURS = 24;

    private final SellerCrawlScheduleOutboxPort outboxPort;

    public ScheduleOutboxFinalizer(SellerCrawlScheduleOutboxPort outboxPort) {
        this.outboxPort = outboxPort;
    }

    /**
     * 실패한 Outbox 재시도 (S3 Phase - Retry)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>OPERATION_STATE=FAILED 조회</li>
     *   <li>재시도 가능 여부 확인 (retryCount < maxRetries)</li>
     *   <li>재시도 가능: FAILED → PENDING 전환 (Processor가 재처리)</li>
     *   <li>재시도 불가: 영구 실패 로깅</li>
     * </ol>
     *
     * <p>실행 주기: 10분마다 (`cron = "0 ㅁ/10 * * * *"`)
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void retryFailedOutbox() {
        List<SellerCrawlScheduleOutbox> failedOutboxes = outboxPort.findByOperationStateFailed();

        if (failedOutboxes.isEmpty()) {
            return; // 실패 Outbox 없으면 조용히 종료
        }

        log.info("🔄 실패 Outbox 재시도 시작: {} 건", failedOutboxes.size());

        int retryCount = 0;
        int permanentFailureCount = 0;

        for (SellerCrawlScheduleOutbox outbox : failedOutboxes) {
            if (outbox.canRetry()) {
                // 재시도 가능: FAILED → PENDING 전환
                outbox.resetForRetry();
                outboxPort.save(outbox);
                retryCount++;

                log.info("♻️ Outbox 재시도 예약: ID={}, RetryCount={}/{}",
                    outbox.getId(), outbox.getRetryCount(), outbox.getMaxRetries());
            } else {
                // 재시도 불가: 영구 실패 (maxRetries 초과)
                permanentFailureCount++;

                log.error("💀 Outbox 영구 실패: ID={}, RetryCount={}/{}, Error={}",
                    outbox.getId(), outbox.getRetryCount(), outbox.getMaxRetries(),
                    outbox.getErrorMessage());

                // TODO: 영구 실패 시 알림 전송 (Slack, Email 등)
                // TODO: Dead Letter Queue (DLQ)로 이동 고려
            }
        }

        log.info("✅ 재시도 완료: 재시도={}, 영구실패={}", retryCount, permanentFailureCount);
    }

    /**
     * 완료된 Outbox 정리 (S3 Phase - Cleanup)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>WAL_STATE=COMPLETED 조회</li>
     *   <li>완료 후 24시간 경과 여부 확인</li>
     *   <li>경과: DB에서 삭제 (디스크 공간 확보)</li>
     * </ol>
     *
     * <p>실행 주기: 매 시간 (`cron = "0 0 * * * *"`)
     *
     * <p>왜 정리가 필요한가?
     * <ul>
     *   <li>Outbox 테이블이 무한정 증가하면 성능 저하</li>
     *   <li>완료된 작업은 더 이상 필요 없음 (Idempotency는 24시간이면 충분)</li>
     *   <li>디스크 공간 확보</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void finalizeCompletedOutbox() {
        List<SellerCrawlScheduleOutbox> completedOutboxes = outboxPort.findByWalStateCompleted();

        if (completedOutboxes.isEmpty()) {
            return; // 완료 Outbox 없으면 조용히 종료
        }

        log.info("🧹 완료 Outbox 정리 시작: 총 {} 건", completedOutboxes.size());

        int deletedCount = 0;

        for (SellerCrawlScheduleOutbox outbox : completedOutboxes) {
            if (outbox.isOldEnough(RETENTION_HOURS)) {
                outboxPort.delete(outbox);
                deletedCount++;

                log.debug("🗑️ Outbox 삭제: ID={}, CompletedAt={}, Age={}시간 경과",
                    outbox.getId(),
                    outbox.getCompletedAt(),
                    java.time.Duration.between(outbox.getCompletedAt(), java.time.LocalDateTime.now()).toHours());
            }
        }

        if (deletedCount > 0) {
            log.info("✅ 정리 완료: {} 건 삭제 (보관 기간: {}시간)", deletedCount, RETENTION_HOURS);
        } else {
            log.debug("ℹ️ 정리 대상 없음 (모두 {}시간 미만)", RETENTION_HOURS);
        }
    }
}
