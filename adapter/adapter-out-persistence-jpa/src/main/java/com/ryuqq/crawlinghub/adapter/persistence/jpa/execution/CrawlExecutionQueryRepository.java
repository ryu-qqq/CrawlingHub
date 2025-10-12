package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

        Long totalCount = queryFactory
                .select(execution.count())
                .from(execution)
                .where(execution.status.eq(status))
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

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
                        lastExecutionId != null ? execution.executionId.lt(lastExecutionId) : null
                )
                .orderBy(execution.startedAt.desc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * Find executions with dynamic filters
     * Supports optional filters: scheduleId, status, startDate, endDate
     * @param scheduleId optional schedule ID filter
     * @param status optional execution status filter
     * @param startDate optional start date filter (inclusive)
     * @param endDate optional end date filter (inclusive)
     * @param pageable pagination parameters
     * @return page of executions matching the filters
     */
    public Page<CrawlExecutionEntity> findWithFilters(
            Long scheduleId,
            ExecutionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        List<CrawlExecutionEntity> content = queryFactory
                .selectFrom(execution)
                .where(
                        scheduleIdEq(scheduleId),
                        statusEq(status),
                        startedAtGoe(startDate),
                        startedAtLoe(endDate)
                )
                .orderBy(execution.startedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(execution.count())
                .from(execution)
                .where(
                        scheduleIdEq(scheduleId),
                        statusEq(status),
                        startedAtGoe(startDate),
                        startedAtLoe(endDate)
                )
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find executions with dynamic filters using No-Offset cursor-based pagination
     * @param scheduleId optional schedule ID filter
     * @param status optional execution status filter
     * @param startDate optional start date filter (inclusive)
     * @param endDate optional end date filter (inclusive)
     * @param lastExecutionId last execution ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of executions matching the filters after the cursor
     */
    public List<CrawlExecutionEntity> findWithFilters(
            Long scheduleId,
            ExecutionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long lastExecutionId,
            int pageSize
    ) {
        return queryFactory
                .selectFrom(execution)
                .where(
                        scheduleIdEq(scheduleId),
                        statusEq(status),
                        startedAtGoe(startDate),
                        startedAtLoe(endDate),
                        lastExecutionId != null ? execution.executionId.lt(lastExecutionId) : null
                )
                .orderBy(execution.startedAt.desc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * Find all executions ordered by started time (descending)
     * @return list of all executions
     */
    public List<CrawlExecutionEntity> findAll() {
        return queryFactory
                .selectFrom(execution)
                .orderBy(execution.startedAt.desc())
                .fetch();
    }

    /**
     * Find all executions with Offset-Based pagination
     * @param pageable pagination parameters
     * @return page of all executions
     */
    public Page<CrawlExecutionEntity> findAll(Pageable pageable) {
        List<CrawlExecutionEntity> content = queryFactory
                .selectFrom(execution)
                .orderBy(execution.startedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(execution.count())
                .from(execution)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find all executions with No-Offset cursor-based pagination
     * @param lastExecutionId last execution ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of executions after the cursor
     */
    public List<CrawlExecutionEntity> findAll(Long lastExecutionId, int pageSize) {
        return queryFactory
                .selectFrom(execution)
                .where(
                        lastExecutionId != null ? execution.executionId.lt(lastExecutionId) : null
                )
                .orderBy(execution.startedAt.desc())
                .limit(pageSize)
                .fetch();
    }

    // Dynamic query helper methods
    private BooleanExpression scheduleIdEq(Long scheduleId) {
        return scheduleId != null ? execution.scheduleId.eq(scheduleId) : null;
    }

    private BooleanExpression statusEq(ExecutionStatus status) {
        return status != null ? execution.status.eq(status) : null;
    }

    private BooleanExpression startedAtGoe(LocalDateTime startDate) {
        return startDate != null ? execution.startedAt.goe(startDate) : null;
    }

    private BooleanExpression startedAtLoe(LocalDateTime endDate) {
        return endDate != null ? execution.startedAt.loe(endDate) : null;
    }

}
