package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Outbox Command Manager
 *
 * <p><strong>책임</strong>: Outbox 영속성 관리 (persist-only)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxCommandManager {

    private final CrawlTaskOutboxPersistencePort crawlTaskOutboxPersistencePort;

    public CrawlTaskOutboxCommandManager(
            CrawlTaskOutboxPersistencePort crawlTaskOutboxPersistencePort) {
        this.crawlTaskOutboxPersistencePort = crawlTaskOutboxPersistencePort;
    }

    /**
     * CrawlTaskOutbox 저장
     *
     * @param outbox 저장할 Outbox
     */
    public void persist(CrawlTaskOutbox outbox) {
        crawlTaskOutboxPersistencePort.persist(outbox);
    }
}
