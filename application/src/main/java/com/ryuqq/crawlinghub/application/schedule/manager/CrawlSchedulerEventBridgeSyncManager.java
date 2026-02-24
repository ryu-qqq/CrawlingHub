package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * EventBridge 동기화 매니저
 *
 * <p><strong>책임</strong>: 아웃박스 기반 EventBridge 동기화 수행
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(prefix = "eventbridge", name = "target-arn")
public class CrawlSchedulerEventBridgeSyncManager {

    private final EventBridgeClientPort eventBridgeClientPort;

    public CrawlSchedulerEventBridgeSyncManager(EventBridgeClientPort eventBridgeClientPort) {
        this.eventBridgeClientPort = eventBridgeClientPort;
    }

    /**
     * 아웃박스에서 이벤트를 EventBridge에 동기화 (스케줄러 처리용)
     *
     * @param outBox 처리할 아웃박스
     */
    public void syncFromOutBox(CrawlSchedulerOutBox outBox) {
        eventBridgeClientPort.syncFromOutBox(outBox);
    }
}
