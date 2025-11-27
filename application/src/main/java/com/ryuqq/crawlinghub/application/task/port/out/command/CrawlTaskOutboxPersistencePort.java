package com.ryuqq.crawlinghub.application.task.port.out.command;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;

/**
 * CrawlTask Outbox 저장 Port (Port Out - Command)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskOutboxPersistencePort {

    /**
     * CrawlTaskOutbox 저장
     *
     * @param outbox 저장할 Outbox
     */
    void persist(CrawlTaskOutbox outbox);
}
