package com.ryuqq.crawlinghub.application.schedule.port.out.client;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerRegisteredEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerUpdatedEvent;

/**
 * AWS EventBridge 클라이언트 포트
 *
 * <p><strong>용도</strong>: 스케줄러 이벤트를 AWS EventBridge에 동기화
 *
 * <p><strong>트랜잭션 외부</strong>: AfterCommit 이벤트 리스너에서 호출
 *
 * @author development-team
 * @since 1.0.0
 */
public interface EventBridgeClientPort {

    /**
     * 스케줄러 등록 이벤트 동기화
     *
     * <p>AWS EventBridge에 새 스케줄러 Rule 생성
     *
     * @param event 스케줄러 등록 이벤트
     */
    void createScheduler(SchedulerRegisteredEvent event);

    /**
     * 스케줄러 수정 이벤트 동기화
     *
     * <p>AWS EventBridge의 스케줄러 Rule 수정
     *
     * @param event 스케줄러 수정 이벤트
     */
    void updateScheduler(SchedulerUpdatedEvent event);

    /**
     * 아웃박스에서 이벤트 동기화 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 아웃박스에서 이벤트 페이로드를 읽어 동기화
     *
     * @param outBox 재처리할 아웃박스
     */
    void syncFromOutBox(CrawlSchedulerOutBox outBox);
}
