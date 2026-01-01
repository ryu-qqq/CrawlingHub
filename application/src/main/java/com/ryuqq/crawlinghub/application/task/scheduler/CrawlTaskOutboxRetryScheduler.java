package com.ryuqq.crawlinghub.application.task.scheduler;

import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.messaging.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
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
    private final CrawlTaskTransactionManager crawlTaskTransactionManager;
    private final CrawlTaskMessageManager crawlTaskMessageManager;
    private final ClockHolder clockHolder;

    public CrawlTaskOutboxRetryScheduler(
            CrawlTaskOutboxQueryPort outboxQueryPort,
            CrawlTaskOutboxTransactionManager outboxTransactionManager,
            CrawlTaskTransactionManager crawlTaskTransactionManager,
            CrawlTaskMessageManager crawlTaskMessageManager,
            ClockHolder clockHolder) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
        this.crawlTaskTransactionManager = crawlTaskTransactionManager;
        this.crawlTaskMessageManager = crawlTaskMessageManager;
        this.clockHolder = clockHolder;
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
     * <p>SQS 발행 성공 시 CrawlTask 상태도 PUBLISHED로 변경
     *
     * @param outbox 처리할 Outbox
     */
    private void processOutboxItem(CrawlTaskOutbox outbox) {
        try {
            // 1. CrawlTask 상태 업데이트 (WAITING → PUBLISHED)
            // SQS 발행 전에 상태를 변경하여 Worker의 Race Condition 방지
            crawlTaskTransactionManager.markAsPublished(
                    outbox.getCrawlTaskId(), clockHolder.getClock());

            // 2. SQS 메시지 발행
            crawlTaskMessageManager.publishFromOutbox(outbox);

            // 3. Outbox 상태 업데이트 (PENDING → SENT)
            outboxTransactionManager.markAsSent(outbox);

            log.debug("CrawlTask Outbox 처리 성공: taskId={}", outbox.getCrawlTaskId().value());
        } catch (Exception e) {
            outboxTransactionManager.markAsFailed(outbox);
            throw e;
        }
    }
}
