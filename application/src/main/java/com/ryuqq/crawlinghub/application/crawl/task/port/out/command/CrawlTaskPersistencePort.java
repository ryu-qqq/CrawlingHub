package com.ryuqq.crawlinghub.application.crawl.task.port.out.command;

import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.crawl.task.identifier.CrawlTaskId;

/**
 * CrawlTask 저장 Port (Port Out - Command)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskPersistencePort {

    /**
     * CrawlTask 저장
     *
     * @param crawlTask 저장할 CrawlTask
     * @return 저장된 CrawlTask의 ID
     */
    CrawlTaskId persist(CrawlTask crawlTask);
}
