package com.ryuqq.crawlinghub.application.execution.manager;

import com.ryuqq.crawlinghub.application.execution.port.out.command.CrawlExecutionPersistencePort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.id.CrawlExecutionId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawlExecution Command Manager
 *
 * <p><strong>책임</strong>: CrawlExecution 영속성 관리 (persist-only)
 *
 * <p><strong>트랜잭션</strong>: 이 클래스는 트랜잭션을 직접 관리하지 않음. 호출자(Coordinator)에서 트랜잭션 경계를 관리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlExecutionCommandManager {

    private final CrawlExecutionPersistencePort crawlExecutionPersistencePort;

    public CrawlExecutionCommandManager(
            CrawlExecutionPersistencePort crawlExecutionPersistencePort) {
        this.crawlExecutionPersistencePort = crawlExecutionPersistencePort;
    }

    /**
     * CrawlExecution 저장
     *
     * @param execution 저장할 CrawlExecution
     * @return 저장된 CrawlExecution ID
     */
    @Transactional
    public CrawlExecutionId persist(CrawlExecution execution) {
        return crawlExecutionPersistencePort.persist(execution);
    }
}
