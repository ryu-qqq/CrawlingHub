package com.ryuqq.crawlinghub.application.execution.port.out.query;

import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import java.util.List;
import java.util.Optional;

/**
 * CrawlExecution 조회 Port (Port Out - Query)
 *
 * <p>크롤링 실행 이력을 조회하는 포트입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlExecutionQueryPort {

    /**
     * CrawlExecution ID로 단건 조회
     *
     * @param crawlExecutionId CrawlExecution ID
     * @return CrawlExecution (Optional)
     */
    Optional<CrawlExecution> findById(CrawlExecutionId crawlExecutionId);

    /**
     * 조건으로 CrawlExecution 목록 조회
     *
     * @param criteria 조회 조건
     * @return CrawlExecution 목록
     */
    List<CrawlExecution> findByCriteria(CrawlExecutionCriteria criteria);

    /**
     * 조건으로 CrawlExecution 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    long countByCriteria(CrawlExecutionCriteria criteria);
}
