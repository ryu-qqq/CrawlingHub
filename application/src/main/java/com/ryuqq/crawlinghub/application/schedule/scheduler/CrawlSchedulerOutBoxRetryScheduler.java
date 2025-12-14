package com.ryuqq.crawlinghub.application.schedule.scheduler;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxTransactionManager;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 아웃박스 재시도 스케줄러.
 *
 * <p><strong>용도</strong>: PENDING/FAILED 상태의 아웃박스를 주기적으로 재처리
 *
 * <p><strong>동시성 제어</strong>: Optimistic Locking (version 필드)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.outbox.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawlSchedulerOutBoxRetryScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(CrawlSchedulerOutBoxRetryScheduler.class);
    private static final int BATCH_SIZE = 100;

    private final CrawlSchedulerOutBoxQueryPort outBoxQueryPort;
    private final CrawlSchedulerOutBoxTransactionManager outBoxManager;
    private final EventBridgeClientPort eventBridgeClientPort;

    public CrawlSchedulerOutBoxRetryScheduler(
            CrawlSchedulerOutBoxQueryPort outBoxQueryPort,
            CrawlSchedulerOutBoxTransactionManager outBoxManager,
            EventBridgeClientPort eventBridgeClientPort) {
        this.outBoxQueryPort = outBoxQueryPort;
        this.outBoxManager = outBoxManager;
        this.eventBridgeClientPort = eventBridgeClientPort;
    }

    /**
     * PENDING/FAILED 상태 아웃박스 재처리.
     *
     * <p>5분마다 실행되며, 배치 단위로 처리
     */
    @Scheduled(fixedDelay = 300000)
    public void processOutbox() {
        List<CrawlSchedulerOutBox> outBoxes = outBoxQueryPort.findPendingOrFailed(BATCH_SIZE);

        if (outBoxes.isEmpty()) {
            return;
        }

        log.info("아웃박스 재처리 시작: {} 건", outBoxes.size());

        int successCount = 0;
        int failCount = 0;

        for (CrawlSchedulerOutBox outBox : outBoxes) {
            try {
                processOutboxItem(outBox);
                successCount++;
            } catch (Exception e) {
                log.error(
                        "아웃박스 처리 실패: outBoxId={}, error={}",
                        outBox.getOutBoxIdValue(),
                        e.getMessage());
                failCount++;
            }
        }

        log.info("아웃박스 재처리 완료: 성공={}, 실패={}", successCount, failCount);
    }

    /**
     * 개별 아웃박스 항목 처리.
     *
     * @param outBox 처리할 아웃박스
     */
    private void processOutboxItem(CrawlSchedulerOutBox outBox) {
        try {
            eventBridgeClientPort.syncFromOutBox(outBox);
            outBoxManager.markAsCompleted(outBox);
            log.debug("아웃박스 처리 성공: outBoxId={}", outBox.getOutBoxIdValue());
        } catch (Exception e) {
            outBoxManager.markAsFailed(outBox, e.getMessage());
            throw e;
        }
    }
}
