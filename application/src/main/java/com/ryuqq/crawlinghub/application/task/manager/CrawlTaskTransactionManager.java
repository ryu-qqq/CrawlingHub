package com.ryuqq.crawlinghub.application.task.manager;

import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Transaction Manager
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawlTask 저장
 *   <li>CrawlTaskOutbox 저장
 * </ul>
 *
 * <p><strong>주의</strong>: 검증 로직은 {@link CrawlTaskPersistenceValidator}에서 수행
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskTransactionManager {

    private final CrawlTaskPersistencePort crawlTaskPersistencePort;

    public CrawlTaskTransactionManager(CrawlTaskPersistencePort crawlTaskPersistencePort) {
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
