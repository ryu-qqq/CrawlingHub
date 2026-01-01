package com.ryuqq.crawlinghub.application.task.manager.messaging;

import com.ryuqq.crawlinghub.application.task.port.out.messaging.CrawlTaskMessagePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 메시지 관리자
 *
 * <p><strong>책임</strong>: CrawlTask SQS 메시지 발행 관리
 *
 * <p><strong>발행 방식</strong>:
 *
 * <ul>
 *   <li>{@link #publishFromEvent(CrawlTaskRegisteredEvent)}: 이벤트 기반 발행 (트랜잭션 커밋 후)
 *   <li>{@link #publishFromOutbox(CrawlTaskOutbox)}: Outbox 기반 발행 (재시도용)
 *   <li>{@link #publish(CrawlTask, String)}: 직접 발행 (레거시 지원)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskMessageManager {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskMessageManager.class);

    private final CrawlTaskMessagePort crawlTaskMessagePort;

    public CrawlTaskMessageManager(CrawlTaskMessagePort crawlTaskMessagePort) {
        this.crawlTaskMessagePort = crawlTaskMessagePort;
    }

    /**
     * CrawlTaskRegisteredEvent 기반 메시지 발행
     *
     * <p>트랜잭션 커밋 후 이벤트 리스너에서 호출
     *
     * @param event CrawlTask 등록 이벤트
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publishFromEvent(CrawlTaskRegisteredEvent event) {
        log.debug(
                "CrawlTask 메시지 발행 시작 (이벤트): taskId={}, schedulerId={}",
                event.getCrawlTaskIdValue(),
                event.getCrawlSchedulerIdValue());

        crawlTaskMessagePort.publishFromEvent(event);

        log.info(
                "CrawlTask 메시지 발행 완료 (이벤트): taskId={}, taskType={}",
                event.getCrawlTaskIdValue(),
                event.taskType());
    }

    /**
     * Outbox 기반 메시지 발행 (재시도용)
     *
     * <p>PENDING/FAILED 상태의 Outbox에서 페이로드를 읽어 발행
     *
     * @param outbox 재처리할 Outbox
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publishFromOutbox(CrawlTaskOutbox outbox) {
        log.debug(
                "CrawlTask 메시지 발행 시작 (Outbox): taskId={}, idempotencyKey={}",
                outbox.getCrawlTaskId().value(),
                outbox.getIdempotencyKey());

        crawlTaskMessagePort.publishFromOutbox(outbox);

        log.info("CrawlTask 메시지 발행 완료 (Outbox): taskId={}", outbox.getCrawlTaskId().value());
    }

    /**
     * CrawlTask 직접 발행
     *
     * @param crawlTask 발행할 CrawlTask
     * @param idempotencyKey 멱등성 키
     * @throws RuntimeException SQS 발행 실패 시
     */
    public void publish(CrawlTask crawlTask, String idempotencyKey) {
        log.debug(
                "CrawlTask 메시지 발행 시작: taskId={}, schedulerId={}",
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value());

        crawlTaskMessagePort.publish(crawlTask, idempotencyKey);

        log.info("CrawlTask 메시지 발행 완료: taskId={}", crawlTask.getId().value());
    }
}
