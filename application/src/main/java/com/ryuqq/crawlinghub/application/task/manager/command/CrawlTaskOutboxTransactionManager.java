package com.ryuqq.crawlinghub.application.task.manager.command;

import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlTask Outbox 트랜잭션 관리자
 *
 * <p><strong>책임</strong>: Outbox 영속성 및 상태 변경 관리
 *
 * <p><strong>상태 전환</strong>:
 *
 * <pre>
 * PENDING → SENT (SQS 발행 성공)
 * PENDING → FAILED (SQS 발행 실패)
 * FAILED → SENT (재시도 성공)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxTransactionManager {

    private final CrawlTaskOutboxPersistencePort crawlTaskOutboxPersistencePort;
    private final ClockHolder clockHolder;

    public CrawlTaskOutboxTransactionManager(
            CrawlTaskOutboxPersistencePort crawlTaskOutboxPersistencePort,
            ClockHolder clockHolder) {
        this.crawlTaskOutboxPersistencePort = crawlTaskOutboxPersistencePort;
        this.clockHolder = clockHolder;
    }

    /**
     * CrawlTaskOutbox 저장
     *
     * @param outbox 저장할 Outbox
     */
    @Transactional
    public void persist(CrawlTaskOutbox outbox) {
        crawlTaskOutboxPersistencePort.persist(outbox);
    }

    /**
     * Outbox 발행 성공 처리
     *
     * <p>SQS 발행 성공 시 호출
     *
     * @param outbox 발행 성공한 Outbox
     */
    @Transactional
    public void markAsSent(CrawlTaskOutbox outbox) {
        outbox.markAsSent(clockHolder.getClock());
        crawlTaskOutboxPersistencePort.persist(outbox);
    }

    /**
     * Outbox 발행 실패 처리
     *
     * <p>SQS 발행 실패 시 호출
     *
     * @param outbox 발행 실패한 Outbox
     */
    @Transactional
    public void markAsFailed(CrawlTaskOutbox outbox) {
        outbox.markAsFailed(clockHolder.getClock());
        crawlTaskOutboxPersistencePort.persist(outbox);
    }
}
