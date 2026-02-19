package com.ryuqq.crawlinghub.application.schedule.listener;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxTransactionManager;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerRegisteredEvent;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 스케줄러 등록 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 AWS EventBridge에 스케줄러 동기화 및 Outbox 상태 업데이트
 *
 * <p><strong>트랜잭션 단계</strong>: AFTER_COMMIT - 데이터 저장 확정 후 외부 시스템 호출
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>AWS EventBridge 스케줄러 Rule 생성
 *   <li>성공 시: Outbox 상태 → COMPLETED
 *   <li>실패 시: Outbox 상태 → FAILED (재시도 스케줄러에서 처리)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SchedulerRegisteredEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(SchedulerRegisteredEventListener.class);

    private final EventBridgeClientPort eventBridgeClientPort;
    private final CrawlSchedulerOutBoxQueryPort outBoxQueryPort;
    private final CrawlSchedulerOutBoxTransactionManager outBoxManager;

    public SchedulerRegisteredEventListener(
            EventBridgeClientPort eventBridgeClientPort,
            CrawlSchedulerOutBoxQueryPort outBoxQueryPort,
            CrawlSchedulerOutBoxTransactionManager outBoxManager) {
        this.eventBridgeClientPort = eventBridgeClientPort;
        this.outBoxQueryPort = outBoxQueryPort;
        this.outBoxManager = outBoxManager;
    }

    /**
     * 스케줄러 등록 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 AWS EventBridge에 스케줄러 Rule 생성 및 Outbox 상태 업데이트
     *
     * @param event 스케줄러 등록 이벤트
     */
    @EventListener
    public void handleSchedulerRegistered(SchedulerRegisteredEvent event) {
        log.info(
                "스케줄러 등록 이벤트 처리 시작: schedulerId={}, historyId={}, sellerId={}, schedulerName={}",
                event.getCrawlSchedulerIdValue(),
                event.getHistoryIdValue(),
                event.getSellerIdValue(),
                event.getScheduleNameValue());

        CrawlSchedulerHistoryId historyId = event.historyId();
        CrawlSchedulerOutBox outBox = outBoxQueryPort.findByHistoryId(historyId).orElse(null);

        if (outBox == null) {
            log.warn("Outbox를 찾을 수 없습니다: historyId={}", historyId.value());
            return;
        }

        try {
            eventBridgeClientPort.createScheduler(event);

            // 성공 시 Outbox 상태 업데이트
            outBoxManager.markAsCompleted(outBox);

            log.info(
                    "AWS EventBridge 스케줄러 생성 완료: schedulerId={}", event.getCrawlSchedulerIdValue());
        } catch (Exception e) {
            // 실패 시 Outbox 상태 업데이트 (재시도 스케줄러에서 처리)
            log.error(
                    "AWS EventBridge 스케줄러 생성 실패: schedulerId={}, error={}",
                    event.getCrawlSchedulerIdValue(),
                    e.getMessage(),
                    e);
            outBoxManager.markAsFailed(outBox, e.getMessage());
        }
    }
}
