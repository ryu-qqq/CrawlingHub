package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.port.out.client.CrawlTaskMessageClient;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
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

    private final CrawlTaskMessageClient crawlTaskMessageClient;

    public CrawlTaskMessageManager(CrawlTaskMessageClient crawlTaskMessageClient) {
        this.crawlTaskMessageClient = crawlTaskMessageClient;
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
                outbox.getCrawlTaskIdValue(),
                outbox.getIdempotencyKey());

        crawlTaskMessageClient.publishFromOutbox(outbox);

        log.info("CrawlTask 메시지 발행 완료 (Outbox): taskId={}", outbox.getCrawlTaskIdValue());
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
                crawlTask.getIdValue(),
                crawlTask.getCrawlSchedulerIdValue());

        crawlTaskMessageClient.publish(crawlTask, idempotencyKey);

        log.info("CrawlTask 메시지 발행 완료: taskId={}", crawlTask.getIdValue());
    }
}
