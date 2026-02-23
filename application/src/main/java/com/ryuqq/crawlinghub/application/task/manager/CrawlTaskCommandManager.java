package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Command Manager
 *
 * <p><strong>책임</strong>: CrawlTask 영속성 관리 (persist-only)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskCommandManager {

    private final CrawlTaskPersistencePort crawlTaskPersistencePort;

    public CrawlTaskCommandManager(CrawlTaskPersistencePort crawlTaskPersistencePort) {
        this.crawlTaskPersistencePort = crawlTaskPersistencePort;
    }

    /**
     * CrawlTask 저장
     *
     * @param crawlTask 저장할 CrawlTask
     * @return 저장된 CrawlTask ID
     */
    public CrawlTaskId persist(CrawlTask crawlTask) {
        return crawlTaskPersistencePort.persist(crawlTask);
    }
}
