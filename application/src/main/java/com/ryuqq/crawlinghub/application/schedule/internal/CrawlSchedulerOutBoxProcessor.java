package com.ryuqq.crawlinghub.application.schedule.internal;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerEventBridgeSyncManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxCommandManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 아웃박스 개별 항목 처리 Processor
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>PENDING → PROCESSING 전환
 *   <li>EventBridge에 동기화
 *   <li>성공 시 COMPLETED, 실패 시 FAILED 상태 변경
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxProcessor {

    private static final Logger log = LoggerFactory.getLogger(CrawlSchedulerOutBoxProcessor.class);

    private final CrawlSchedulerEventBridgeSyncManager eventBridgeSyncManager;
    private final CrawlSchedulerOutBoxCommandManager outBoxCommandManager;

    public CrawlSchedulerOutBoxProcessor(
            CrawlSchedulerEventBridgeSyncManager eventBridgeSyncManager,
            CrawlSchedulerOutBoxCommandManager outBoxCommandManager) {
        this.eventBridgeSyncManager = eventBridgeSyncManager;
        this.outBoxCommandManager = outBoxCommandManager;
    }

    /**
     * 아웃박스 항목 처리
     *
     * @param outBox 처리할 아웃박스
     * @return 처리 성공 여부
     */
    public boolean processOutbox(CrawlSchedulerOutBox outBox) {
        try {
            outBox.markAsProcessing(Instant.now());
            outBoxCommandManager.persist(outBox);

            eventBridgeSyncManager.syncFromOutBox(outBox);

            outBox.markAsCompleted(Instant.now());
            outBoxCommandManager.persist(outBox);

            log.debug("아웃박스 처리 성공: outBoxId={}", outBox.getOutBoxIdValue());
            return true;
        } catch (Exception e) {
            outBox.markAsFailed(e.getMessage(), Instant.now());
            outBoxCommandManager.persist(outBox);

            log.error(
                    "아웃박스 처리 실패: outBoxId={}, error={}", outBox.getOutBoxIdValue(), e.getMessage());
            return false;
        }
    }
}
