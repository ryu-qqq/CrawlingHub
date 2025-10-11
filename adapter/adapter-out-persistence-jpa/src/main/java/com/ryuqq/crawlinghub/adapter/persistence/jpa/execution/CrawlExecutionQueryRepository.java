package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
 * - Pagination (Offset-Based and No-Offset)
 */
@Repository
public class CrawlExecutionQueryRepository {

    private static final QCrawlExecutionEntity execution = QCrawlExecutionEntity.crawlExecutionEntity;

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
        return queryFactory
                .selectFrom(execution)
                .where(execution.status.eq(status))
                .orderBy(execution.startedAt.desc())
                .fetch();
    }

    /**
     * Find executions by status with Offset-Based pagination
     * @param status the execution status
     * @param pageable pagination parameters
     * @return page of executions with the given status
     */
    public Page<CrawlExecutionEntity> findByStatus(ExecutionStatus status, Pageable pageable) {
        List<CrawlExecutionEntity> content = queryFactory
                .selectFrom(execution)
                .where(execution.status.eq(status))
                .orderBy(execution.startedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(execution.count())
                .from(execution)
                .where(execution.status.eq(status))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find executions by status with No-Offset cursor-based pagination
     * @param status the execution status
     * @param lastExecutionId last execution ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of executions after the cursor
     */
    public List<CrawlExecutionEntity> findByStatus(ExecutionStatus status, Long lastExecutionId, int pageSize) {
        return queryFactory
                .selectFrom(execution)
                .where(
                        execution.status.eq(status),
                        lastExecutionId != null ? execution.executionId.gt(lastExecutionId) : null
                )
                .orderBy(execution.executionId.asc())
                .limit(pageSize)
                .fetch();
    }

}
