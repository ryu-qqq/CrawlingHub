package com.ryuqq.crawlinghub.application.execution.port.out.command;

import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;

/**
 * CrawlExecution 저장 Port (Port Out - Command)
 *
 * <p>크롤링 실행 이력을 저장하는 포트입니다.
 *
 * <p>ID가 없으면 INSERT, ID가 있으면 UPDATE 처리됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlExecutionPersistencePort {

    /**
     * CrawlExecution 저장 (INSERT or UPDATE)
     *
     * <p>ID가 미할당이면 신규 저장, ID가 할당되어 있으면 업데이트합니다.
     *
     * @param crawlExecution 저장할 CrawlExecution
     * @return 저장된 CrawlExecution의 ID
     */
    CrawlExecutionId persist(CrawlExecution crawlExecution);
}
