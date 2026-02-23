package com.ryuqq.crawlinghub.application.schedule.port.out.client;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;

/**
 * AWS EventBridge 클라이언트 포트
 *
 * <p><strong>용도</strong>: 아웃박스 기반으로 스케줄러 이벤트를 AWS EventBridge에 동기화
 *
 * @author development-team
 * @since 1.0.0
 */
public interface EventBridgeClientPort {

    /**
     * 아웃박스에서 이벤트 동기화
     *
     * <p>아웃박스의 이벤트 페이로드를 읽어 EventBridge에 동기화
     *
     * @param outBox 처리할 아웃박스
     */
    void syncFromOutBox(CrawlSchedulerOutBox outBox);
}
