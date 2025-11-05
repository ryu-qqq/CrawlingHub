package com.ryuqq.crawlinghub.application.schedule.listener;

import com.ryuqq.crawlinghub.application.schedule.orchestrator.ScheduleOutboxProcessor;
import com.ryuqq.crawlinghub.application.schedule.port.out.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleCreatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;

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

    private final SellerCrawlScheduleOutboxPort outboxPort;
    private final ScheduleOutboxProcessor outboxProcessor;

    public ScheduleEventListener(
        SellerCrawlScheduleOutboxPort outboxPort,
        ScheduleOutboxProcessor outboxProcessor
    ) {
        this.outboxPort = outboxPort;
        this.outboxProcessor = outboxProcessor;
    }

    /**
     * Schedule Created Event 처리
     *
     * <p>트랜잭션 커밋 후 비동기로 Outbox Processor를 즉시 호출합니다.</p>
     *
     * @param event ScheduleCreatedEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleScheduleCreated(ScheduleCreatedEvent event) {
        log.info("📨 ScheduleCreatedEvent 수신: scheduleId={}, sellerId={}, outboxIdemKey={}",
            event.scheduleId(), event.sellerId(), event.outboxIdemKey());

        processOutbox(event.outboxIdemKey());
    }

    /**
     * Schedule Updated Event 처리
     *
     * <p>트랜잭션 커밋 후 비동기로 Outbox Processor를 즉시 호출합니다.</p>
     *
     * @param event ScheduleUpdatedEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleScheduleUpdated(ScheduleUpdatedEvent event) {
        log.info("📨 ScheduleUpdatedEvent 수신: scheduleId={}, sellerId={}, outboxIdemKey={}",
            event.scheduleId(), event.sellerId(), event.outboxIdemKey());

        processOutbox(event.outboxIdemKey());
    }

    /**
     * Outbox Processor 즉시 호출
     *
     * <p>Idempotency Key로 Outbox를 찾아서 즉시 처리합니다.</p>
     *
     * @param idemKey Outbox Idempotency Key
     */
    private void processOutbox(String idemKey) {
        try {
            Optional<SellerCrawlScheduleOutbox> outboxOpt = outboxPort.findByIdemKey(idemKey);

            if (outboxOpt.isEmpty()) {
                log.warn("⚠️ Outbox를 찾을 수 없습니다: idemKey={} (이미 처리되었거나 존재하지 않음)", idemKey);
                return;
            }

            SellerCrawlScheduleOutbox outbox = outboxOpt.get();

            // Outbox가 이미 처리 중이거나 완료된 경우 스킵
            if (outbox.getWalState() != SellerCrawlScheduleOutbox.WriteAheadState.PENDING) {
                log.debug("⏭️ Outbox가 이미 처리되었거나 처리 중입니다: idemKey={}, state={}", 
                    idemKey, outbox.getWalState());
                return;
            }

            log.info("🚀 Outbox Processor 즉시 호출: idemKey={}", idemKey);
            outboxProcessor.processOne(outbox);

        } catch (Exception e) {
            log.error("❌ Outbox Processor 즉시 호출 실패: idemKey={}, error={}", 
                idemKey, e.getMessage(), e);
            // ✅ 실패해도 @Scheduled가 Fallback으로 처리하므로 예외를 던지지 않음
        }
    }
}

