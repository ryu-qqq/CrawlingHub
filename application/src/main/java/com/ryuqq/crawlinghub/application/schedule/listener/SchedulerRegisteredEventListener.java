package com.ryuqq.crawlinghub.application.schedule.listener;

import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 스케줄러 등록 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 AWS EventBridge에 스케줄러 동기화
 *
 * <p><strong>트랜잭션 단계</strong>: AFTER_COMMIT - 데이터 저장 확정 후 외부 시스템 호출
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SchedulerRegisteredEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(SchedulerRegisteredEventListener.class);

    private final EventBridgeClientPort eventBridgeClientPort;

    public SchedulerRegisteredEventListener(EventBridgeClientPort eventBridgeClientPort) {
        this.eventBridgeClientPort = eventBridgeClientPort;
    }

    /**
     * 스케줄러 등록 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 AWS EventBridge에 스케줄러 Rule 생성
     *
     * @param event 스케줄러 등록 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSchedulerRegistered(SchedulerRegisteredEvent event) {
        log.info(
                "스케줄러 등록 이벤트 처리 시작: schedulerId={}, sellerId={}, schedulerName={}",
                event.getCrawlSchedulerIdValue(),
                event.getSellerIdValue(),
                event.getScheduleNameValue());

        try {
            eventBridgeClientPort.createScheduler(event);
            log.info(
                    "AWS EventBridge 스케줄러 생성 완료: schedulerId={}", event.getCrawlSchedulerIdValue());
        } catch (Exception e) {
            // 외부 시스템 호출 실패 시 로깅만 수행 (아웃박스에서 재시도)
            log.error(
                    "AWS EventBridge 스케줄러 생성 실패: schedulerId={}, error={}",
                    event.getCrawlSchedulerIdValue(),
                    e.getMessage(),
                    e);
        }
    }
}
