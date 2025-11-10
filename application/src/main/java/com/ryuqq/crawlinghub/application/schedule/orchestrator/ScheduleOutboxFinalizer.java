package com.ryuqq.crawlinghub.application.schedule.orchestrator;

import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleOutboxStateManager;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Schedule Outbox Finalizer (S3 Phase - Finalize)
 *
 * <p>Orchestration Pattern 3-Phase Lifecycle의 S3 Phase를 담당합니다:
 * <ul>
 *   <li>S1 (Accept): Facade가 DB + Outbox 저장 완료</li>
 *   <li>S2 (Execute): Processor가 Outbox를 읽고 EventBridge 호출</li>
 *   <li>S3 (Finalize): 이 Finalizer가 재시도 및 정리</li>
 * </ul>
 *
 * <p>핵심 책임:
 * <ul>
 *   <li>실패한 Outbox 재시도 (maxRetries 미만)</li>
 *   <li>완료된 Outbox 정리 (일정 시간 경과 후)</li>
 *   <li>영구 실패 Outbox 로깅 (재시도 초과)</li>
 * </ul>
 *
 * <p>실행 주기:
 * <ul>
 *   <li>재시도: 10분마다 (cron = 0 ASTERISK-SLASH10 ASTERISK ASTERISK ASTERISK ASTERISK)</li>
 *   <li>정리: 매 시간 (cron = 0 0 ASTERISK ASTERISK ASTERISK ASTERISK)</li>
 * </ul>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>Scheduled 메서드에는 Transactional 금지</li>
 *   <li>StateManager가 트랜잭션 관리 담당</li>
 *   <li>각 Outbox 처리는 독립 트랜잭션 (실패 격리)</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>Pure Java Constructor (Lombok 금지)</li>
 *   <li>Component (Spring Bean 등록)</li>
 *   <li>StateManager 위임 (Port 직접 호출 금지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleOutboxFinalizer {

    private static final Logger log = LoggerFactory.getLogger(ScheduleOutboxFinalizer.class);

    /**
     * 완료된 Outbox 보관 시간 (시간 단위)
     * 24시간 경과 후 정리
     */
    private static final int RETENTION_HOURS = 24;

    private final ScheduleOutboxStateManager stateManager;

    /**
     * 생성자
     *
     * @param stateManager Outbox 상태 관리자
     */
    public ScheduleOutboxFinalizer(ScheduleOutboxStateManager stateManager) {
        this.stateManager = Objects.requireNonNull(stateManager, "stateManager must not be null");
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
     * <p>실행 주기: 10분마다 (cron = 0 ASTERISK-SLASH10 ASTERISK ASTERISK ASTERISK ASTERISK)
     *
     * <p><strong>트랜잭션 관리:</strong></p>
     * <ul>
     *   <li>Scheduled 메서드에는 Transactional 금지</li>
     *   <li>StateManager 메서드에 Transactional 위임</li>
     *   <li>각 Outbox는 독립 트랜잭션으로 처리 (실패 격리)</li>
     * </ul>
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void retryFailedOutbox() {
        List<ScheduleOutbox> failedOutboxes = stateManager.findByOperationStateFailed();

        if (failedOutboxes.isEmpty()) {
            return; // 실패 Outbox 없으면 조용히 종료
        }

        log.info("[RETRY] 실패 Outbox 재시도 시작: {} 건", failedOutboxes.size());

        int retryCount = 0;
        int permanentFailureCount = 0;

        for (ScheduleOutbox outbox : failedOutboxes) {
            if (outbox.canRetry()) {
                // 재시도 가능: FAILED → PENDING 전환
                // StateManager가 트랜잭션 내에서 처리
                outbox.resetForRetry();
                stateManager.saveOutbox(outbox);
                retryCount++;

                log.info("Outbox 재시도 예약: ID={}, RetryCount={}/{}",
                    outbox.getId(), outbox.getRetryCount(), outbox.getMaxRetries());
            } else {
                // 재시도 불가: 영구 실패 (maxRetries 초과)
                permanentFailureCount++;

                log.error("[PERMANENT_FAILURE] Outbox 영구 실패: ID={}, RetryCount={}/{}, Error={}",
                    outbox.getId(), outbox.getRetryCount(), outbox.getMaxRetries(),
                    outbox.getErrorMessage());

                // TODO: 영구 실패 시 알림 전송 (Slack, Email 등)
                // TODO: Dead Letter Queue (DLQ)로 이동 고려
            }
        }

        log.info("[RETRY_COMPLETE] 재시도 완료: 재시도={}, 영구실패={}", retryCount, permanentFailureCount);
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
     * <p>실행 주기: 매 시간 (cron = 0 0 ASTERISK ASTERISK ASTERISK ASTERISK)
     *
     * <p>왜 정리가 필요한가?
     * <ul>
     *   <li>Outbox 테이블이 무한정 증가하면 성능 저하</li>
     *   <li>완료된 작업은 더 이상 필요 없음 (Idempotency는 24시간이면 충분)</li>
     *   <li>디스크 공간 확보</li>
     * </ul>
     *
     * <p><strong>트랜잭션 관리:</strong></p>
     * <ul>
     *   <li>Scheduled 메서드에는 Transactional 금지</li>
     *   <li>StateManager 메서드에 Transactional 위임</li>
     *   <li>각 Outbox는 독립 트랜잭션으로 삭제 (실패 격리)</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 * * * *")
    public void finalizeCompletedOutbox() {
        List<ScheduleOutbox> completedOutboxes = stateManager.findByWalStateCompleted();

        if (completedOutboxes.isEmpty()) {
            return; // 완료 Outbox 없으면 조용히 종료
        }

        log.info("[CLEANUP] 완료 Outbox 정리 시작: 총 {} 건", completedOutboxes.size());

        int deletedCount = 0;

        for (ScheduleOutbox outbox : completedOutboxes) {
            if (outbox.isOldEnough(RETENTION_HOURS)) {
                // StateManager가 트랜잭션 내에서 삭제
                stateManager.deleteOutbox(outbox);
                deletedCount++;

                log.debug("[DELETE] Outbox 삭제: ID={}, CompletedAt={}, Age={}시간 경과",
                    outbox.getId(),
                    outbox.getCompletedAt(),
                    java.time.Duration.between(outbox.getCompletedAt(), java.time.LocalDateTime.now()).toHours());
            }
        }

        if (deletedCount > 0) {
            log.info("[CLEANUP_COMPLETE] 정리 완료: {} 건 삭제 (보관 기간: {}시간)", deletedCount, RETENTION_HOURS);
        } else {
            log.debug("[INFO] 정리 대상 없음 (모두 {}시간 미만)", RETENTION_HOURS);
        }
    }
}
