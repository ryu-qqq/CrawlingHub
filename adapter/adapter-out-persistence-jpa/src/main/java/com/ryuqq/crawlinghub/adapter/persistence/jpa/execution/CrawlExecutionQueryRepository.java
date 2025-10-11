package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlExecution (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
 * - Complex conditions
 * - Joins
 * - Aggregations
 * - Projections
 * - Statistics queries
 */
@Repository
public class CrawlExecutionQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CrawlExecutionQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Find all executions by status
     * @param status the execution status
     * @return list of executions with the given status, ordered by started time (desc)
     */
    public List<CrawlExecutionEntity> findByStatus(ExecutionStatus status) {
        QCrawlExecutionEntity execution = QCrawlExecutionEntity.crawlExecutionEntity;

        return queryFactory
                .selectFrom(execution)
                .where(execution.status.eq(status))
                .orderBy(execution.startedAt.desc())
                .fetch();
    }

}
