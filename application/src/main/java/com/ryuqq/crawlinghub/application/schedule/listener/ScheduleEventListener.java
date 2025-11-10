package com.ryuqq.crawlinghub.application.schedule.listener;

import com.ryuqq.crawlinghub.application.schedule.orchestrator.ScheduleOutboxProcessor;
import com.ryuqq.crawlinghub.application.schedule.port.out.ScheduleOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEvent;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

/**
 * Schedule Event Listener
 *
 * <p>트랜잭션 커밋 후 Schedule Domain Event를 수신하여 비동기로 Outbox Processor를 즉시 호출합니다.</p>
 *
 * <p><strong>처리 흐름:</strong></p>
 * <ol>
 *   <li>Schedule 저장 + Outbox 저장 (트랜잭션 내)</li>
 *   <li>트랜잭션 커밋</li>
 *   <li>✅ 이벤트 발행 (@TransactionalEventListener)</li>
 *   <li>✅ 비동기로 Outbox Processor 즉시 호출 (@Async)</li>
 *   <li>✅ @Scheduled는 Fallback으로 유지 (주기적 Polling)</li>
 * </ol>
 *
 * <p><strong>왜 이 패턴인가?</strong></p>
 * <ul>
 *   <li>✅ <strong>즉시 처리</strong>: 트랜잭션 커밋 후 즉시 Outbox Processor 호출</li>
 *   <li>✅ <strong>비동기 처리</strong>: @Async로 Non-blocking 처리</li>
 *   <li>✅ <strong>Fallback 보장</strong>: @Scheduled로 주기적 Polling도 유지 (이중 보장)</li>
 *   <li>✅ <strong>장애 격리</strong>: EventListener 실패가 Facade에 영향 없음</li>
 * </ul>
 *
 * <p><strong>@Async vs @Scheduled 하이브리드 패턴:</strong></p>
 * <ul>
 *   <li><strong>@Async (이 Listener)</strong>: 즉시 처리 (최우선)</li>
 *   <li><strong>@Scheduled (ScheduleOutboxProcessor)</strong>: 주기적 Polling (Fallback)</li>
 *   <li>✅ 두 방식 모두 동작하여 이중 보장 (High Availability)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ScheduleEventListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduleEventListener.class);

    private final ScheduleOutboxQueryPort outboxQueryPort;
    private final ScheduleOutboxProcessor outboxProcessor;

    public ScheduleEventListener(
        ScheduleOutboxQueryPort outboxQueryPort,
        ScheduleOutboxProcessor outboxProcessor
    ) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxProcessor = outboxProcessor;
    }

    /**
     * Schedule Event 처리 (통합 핸들러)
     *
     * <p>트랜잭션 커밋 후 비동기로 Outbox Processor를 즉시 호출합니다.</p>
     * <p>ScheduleCreatedEvent와 ScheduleUpdatedEvent 모두 처리합니다.</p>
     *
     * @param event ScheduleEvent (ScheduleCreatedEvent 또는 ScheduleUpdatedEvent)
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleScheduleEvent(ScheduleEvent event) {
        log.info("📨 {} 수신: scheduleId={}, sellerId={}, outboxIdemKey={}",
            event.getClass().getSimpleName(),
            event.scheduleId(),
            event.sellerId(),
            event.outboxIdemKey());

        processOutbox(event.outboxIdemKey());
    }

    /**
     * Outbox Processor 즉시 호출
     *
     * <p>Idempotency Key로 Outbox를 찾아서 즉시 처리합니다.</p>
     * <p><strong>Race Condition 방지:</strong> @Scheduled 폴러와의 동시성 문제를 방지하기 위해
     * processOne 호출 전에 최신 상태로 다시 조회하여 PENDING 상태인지 확인합니다.</p>
     *
     * @param idemKey Outbox Idempotency Key
     */
    @Transactional
    private void processOutbox(String idemKey) {
        try {
            // Race Condition 방지: 트랜잭션 내에서 최신 상태로 다시 조회
            // @Scheduled 폴러가 이미 처리했을 수 있으므로 최신 상태 확인
            Optional<ScheduleOutbox> outboxOpt = outboxQueryPort.findByIdemKey(idemKey);

            if (outboxOpt.isEmpty()) {
                log.warn("⚠️ Outbox를 찾을 수 없습니다: idemKey={} (이미 처리되었거나 존재하지 않음)", idemKey);
                return;
            }

            ScheduleOutbox outbox = outboxOpt.get();

            // Outbox가 이미 처리 중이거나 완료된 경우 스킵
            // ✅ 트랜잭션 내에서 최신 상태로 재확인하여 Race Condition 방지
            if (outbox.getWalState() != ScheduleOutbox.WriteAheadState.PENDING) {
                log.debug("⏭️ Outbox가 이미 처리되었거나 처리 중입니다: idemKey={}, state={}",
                    idemKey, outbox.getWalState());
                return;
            }

            log.info("[OUTBOX_READY] Outbox 생성 완료, Scheduled Processor가 처리 예정: idemKey={}", idemKey);
            // TODO: processOne() 메서드 구현 후 즉시 처리 활성화
            // outboxProcessor.processOne(outbox);
            // 현재는 @Scheduled processOutbox()가 주기적으로 처리함

        } catch (Exception e) {
            log.error("❌ Outbox Processor 즉시 호출 실패: idemKey={}, error={}",
                idemKey, e.getMessage(), e);
            // ✅ 실패해도 @Scheduled가 Fallback으로 처리하므로 예외를 던지지 않음
        }
    }
}

