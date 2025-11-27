package com.ryuqq.crawlinghub.application.task.listener;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * CrawlTask 등록 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 SQS 발행 및 Outbox 상태 업데이트
 *
 * <p><strong>트랜잭션 단계</strong>: AFTER_COMMIT - 데이터 저장 확정 후 외부 시스템 호출
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>SQS 메시지 발행
 *   <li>성공 시: Outbox 상태 → SENT
 *   <li>실패 시: Outbox 상태 → FAILED (재시도 스케줄러에서 처리)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskRegisteredEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(CrawlTaskRegisteredEventListener.class);

    private final CrawlTaskMessageManager crawlTaskMessageManager;
    private final CrawlTaskOutboxQueryPort crawlTaskOutboxQueryPort;
    private final CrawlTaskOutboxTransactionManager crawlTaskOutboxTransactionManager;

    public CrawlTaskRegisteredEventListener(
            CrawlTaskMessageManager crawlTaskMessageManager,
            CrawlTaskOutboxQueryPort crawlTaskOutboxQueryPort,
            CrawlTaskOutboxTransactionManager crawlTaskOutboxTransactionManager) {
        this.crawlTaskMessageManager = crawlTaskMessageManager;
        this.crawlTaskOutboxQueryPort = crawlTaskOutboxQueryPort;
        this.crawlTaskOutboxTransactionManager = crawlTaskOutboxTransactionManager;
    }

    /**
     * CrawlTask 등록 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 SQS 발행 및 Outbox 상태 업데이트
     *
     * @param event CrawlTask 등록 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCrawlTaskRegistered(CrawlTaskRegisteredEvent event) {
        log.info(
                "CrawlTask 등록 이벤트 처리 시작: taskId={}, schedulerId={}, sellerId={}, taskType={}",
                event.getCrawlTaskIdValue(),
                event.getCrawlSchedulerIdValue(),
                event.getSellerIdValue(),
                event.taskType());

        CrawlTaskId crawlTaskId = event.crawlTaskId();
        CrawlTaskOutbox outbox =
                crawlTaskOutboxQueryPort.findByCrawlTaskId(crawlTaskId).orElse(null);

        if (outbox == null) {
            log.warn("Outbox를 찾을 수 없습니다: taskId={}", crawlTaskId.value());
            return;
        }

        try {
            // SQS 메시지 발행
            crawlTaskMessageManager.publishFromEvent(event);

            // 성공 시 Outbox 상태 업데이트
            crawlTaskOutboxTransactionManager.markAsSent(outbox);

            log.info(
                    "CrawlTask 등록 이벤트 처리 완료: taskId={}, endpoint={}",
                    event.getCrawlTaskIdValue(),
                    event.getEndpointUrl());
        } catch (Exception e) {
            // 실패 시 Outbox 상태 업데이트 (재시도 스케줄러에서 처리)
            log.error(
                    "CrawlTask SQS 발행 실패: taskId={}, error={}",
                    event.getCrawlTaskIdValue(),
                    e.getMessage());
            crawlTaskOutboxTransactionManager.markAsFailed(outbox);
        }
    }
}
