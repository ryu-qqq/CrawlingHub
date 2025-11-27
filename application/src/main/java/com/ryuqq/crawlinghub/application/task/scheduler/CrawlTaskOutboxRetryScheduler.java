package com.ryuqq.crawlinghub.application.task.scheduler;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Outbox 재시도 스케줄러
 *
 * <p><strong>용도</strong>: PENDING/FAILED 상태의 Outbox를 주기적으로 재처리
 *
 * <p><strong>동시성 제어</strong>: 배치 단위 처리로 부하 분산
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>PENDING/FAILED 상태 Outbox 조회 (배치 단위)
 *   <li>각 Outbox에 대해 SQS 메시지 발행 시도
 *   <li>성공 시: Outbox 상태 → SENT
 *   <li>실패 시: Outbox 상태 → FAILED (다음 스케줄에서 재시도)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "scheduler.crawl-task-outbox.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class CrawlTaskOutboxRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskOutboxRetryScheduler.class);
    private static final int BATCH_SIZE = 100;

    private final CrawlTaskOutboxQueryPort outboxQueryPort;
    private final CrawlTaskOutboxTransactionManager outboxTransactionManager;
    private final CrawlTaskMessageManager crawlTaskMessageManager;

    public CrawlTaskOutboxRetryScheduler(
            CrawlTaskOutboxQueryPort outboxQueryPort,
            CrawlTaskOutboxTransactionManager outboxTransactionManager,
            CrawlTaskMessageManager crawlTaskMessageManager) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
        this.crawlTaskMessageManager = crawlTaskMessageManager;
    }

    /**
     * PENDING/FAILED 상태 Outbox 재처리
     *
     * <p>5분마다 실행되며, 배치 단위로 처리
     */
    @Scheduled(fixedDelay = 300000)
    public void processOutbox() {
        CrawlTaskOutboxCriteria criteria = CrawlTaskOutboxCriteria.pendingOrFailed(BATCH_SIZE);
        List<CrawlTaskOutbox> outboxes = outboxQueryPort.findByCriteria(criteria);

        if (outboxes.isEmpty()) {
            return;
        }

        log.info("CrawlTask Outbox 재처리 시작: {} 건", outboxes.size());

        int successCount = 0;
        int failCount = 0;

        for (CrawlTaskOutbox outbox : outboxes) {
            try {
                processOutboxItem(outbox);
                successCount++;
            } catch (Exception e) {
                log.error(
                        "CrawlTask Outbox 처리 실패: taskId={}, error={}",
                        outbox.getCrawlTaskId().value(),
                        e.getMessage());
                failCount++;
            }
        }

        log.info("CrawlTask Outbox 재처리 완료: 성공={}, 실패={}", successCount, failCount);
    }

    /**
     * 개별 Outbox 항목 처리
     *
     * @param outbox 처리할 Outbox
     */
    private void processOutboxItem(CrawlTaskOutbox outbox) {
        try {
            crawlTaskMessageManager.publishFromOutbox(outbox);
            outboxTransactionManager.markAsSent(outbox);
            log.debug("CrawlTask Outbox 처리 성공: taskId={}", outbox.getCrawlTaskId().value());
        } catch (Exception e) {
            outboxTransactionManager.markAsFailed(outbox);
            throw e;
        }
    }
}
