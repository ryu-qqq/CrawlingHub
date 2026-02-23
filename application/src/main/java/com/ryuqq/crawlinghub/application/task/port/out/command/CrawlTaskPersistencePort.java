package com.ryuqq.crawlinghub.application.task.port.out.command;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;

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
