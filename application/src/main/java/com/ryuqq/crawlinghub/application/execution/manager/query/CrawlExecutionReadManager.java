package com.ryuqq.crawlinghub.application.execution.manager.query;

import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution 조회 전용 Manager
 *
 * <p><strong>책임</strong>: CrawlExecution 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존, 트랜잭션 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlExecutionReadManager {

    private final CrawlExecutionQueryPort crawlExecutionQueryPort;

    public CrawlExecutionReadManager(CrawlExecutionQueryPort crawlExecutionQueryPort) {
        this.crawlExecutionQueryPort = crawlExecutionQueryPort;
    }

    /**
     * CrawlExecution ID로 단건 조회
     *
     * @param crawlExecutionId CrawlExecution ID
     * @return CrawlExecution (Optional)
     */
    public Optional<CrawlExecution> findById(CrawlExecutionId crawlExecutionId) {
        return crawlExecutionQueryPort.findById(crawlExecutionId);
    }

    /**
     * 조건으로 CrawlExecution 목록 조회
     *
     * @param criteria 조회 조건
     * @return CrawlExecution 목록
     */
    public List<CrawlExecution> findByCriteria(CrawlExecutionCriteria criteria) {
        return crawlExecutionQueryPort.findByCriteria(criteria);
    }

    /**
     * 조건으로 CrawlExecution 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    public long countByCriteria(CrawlExecutionCriteria criteria) {
        return crawlExecutionQueryPort.countByCriteria(criteria);
    }
}
