package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlTask (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
 * - Complex conditions
 * - Joins
 * - Aggregations
 * - Projections
 * - Pagination (Offset-Based and No-Offset)
 */
@Repository
public class CrawlTaskQueryRepository {

    private static final QCrawlTaskEntity task = QCrawlTaskEntity.crawlTaskEntity;

    private final JPAQueryFactory queryFactory;

    public CrawlTaskQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Find all tasks for an execution
     * @param executionId the execution ID
     * @return list of tasks ordered by started time (desc)
     */
    public List<CrawlTaskEntity> findByExecutionId(Long executionId) {
        return queryFactory
                .selectFrom(task)
                .where(task.executionId.eq(executionId))
                .orderBy(task.startedAt.desc())
                .fetch();
    }

    /**
     * Find tasks for an execution with Offset-Based pagination
     * @param executionId the execution ID
     * @param pageable pagination parameters
     * @return page of tasks for the execution
     */
    public Page<CrawlTaskEntity> findByExecutionId(Long executionId, Pageable pageable) {
        List<CrawlTaskEntity> content = queryFactory
                .selectFrom(task)
                .where(task.executionId.eq(executionId))
                .orderBy(task.startedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(task.count())
                .from(task)
                .where(task.executionId.eq(executionId))
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find tasks for an execution with No-Offset cursor-based pagination
     * @param executionId the execution ID
     * @param lastTaskId last task ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of tasks after the cursor
     */
    public List<CrawlTaskEntity> findByExecutionId(Long executionId, Long lastTaskId, int pageSize) {
        return queryFactory
                .selectFrom(task)
                .where(
                        task.executionId.eq(executionId),
                        lastTaskId != null ? task.taskId.lt(lastTaskId) : null
                )
                .orderBy(task.startedAt.desc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * Find tasks by execution ID and status
     * @param executionId the execution ID
     * @param status the task status
     * @return list of tasks matching the filters
     */
    public List<CrawlTaskEntity> findByExecutionIdAndStatus(Long executionId, TaskStatus status) {
        return queryFactory
                .selectFrom(task)
                .where(
                        task.executionId.eq(executionId),
                        task.status.eq(status)
                )
                .orderBy(task.startedAt.desc())
                .fetch();
    }

    /**
     * Find tasks by execution ID and step ID
     * @param executionId the execution ID
     * @param stepId the step ID
     * @return list of tasks matching the filters
     */
    public List<CrawlTaskEntity> findByExecutionIdAndStepId(Long executionId, Long stepId) {
        return queryFactory
                .selectFrom(task)
                .where(
                        task.executionId.eq(executionId),
                        task.stepId.eq(stepId)
                )
                .orderBy(task.startedAt.desc())
                .fetch();
    }

    /**
     * Find tasks with dynamic filters
     * Supports optional filters: status, stepId
     * @param executionId the execution ID (required)
     * @param status optional task status filter
     * @param stepId optional step ID filter
     * @return list of tasks matching the filters
     */
    public List<CrawlTaskEntity> findWithFilters(Long executionId, TaskStatus status, Long stepId) {
        return queryFactory
                .selectFrom(task)
                .where(
                        task.executionId.eq(executionId),
                        statusEq(status),
                        stepIdEq(stepId)
                )
                .orderBy(task.startedAt.desc())
                .fetch();
    }

    /**
     * Find tasks with dynamic filters using Offset-Based pagination
     * @param executionId the execution ID (required)
     * @param status optional task status filter
     * @param stepId optional step ID filter
     * @param pageable pagination parameters
     * @return page of tasks matching the filters
     */
    public Page<CrawlTaskEntity> findWithFilters(Long executionId, TaskStatus status, Long stepId, Pageable pageable) {
        List<CrawlTaskEntity> content = queryFactory
                .selectFrom(task)
                .where(
                        task.executionId.eq(executionId),
                        statusEq(status),
                        stepIdEq(stepId)
                )
                .orderBy(task.startedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(task.count())
                .from(task)
                .where(
                        task.executionId.eq(executionId),
                        statusEq(status),
                        stepIdEq(stepId)
                )
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find tasks with dynamic filters using No-Offset cursor-based pagination
     * @param executionId the execution ID (required)
     * @param status optional task status filter
     * @param stepId optional step ID filter
     * @param lastTaskId last task ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of tasks matching the filters after the cursor
     */
    public List<CrawlTaskEntity> findWithFilters(Long executionId, TaskStatus status, Long stepId, Long lastTaskId, int pageSize) {
        return queryFactory
                .selectFrom(task)
                .where(
                        task.executionId.eq(executionId),
                        statusEq(status),
                        stepIdEq(stepId),
                        lastTaskId != null ? task.taskId.lt(lastTaskId) : null
                )
                .orderBy(task.startedAt.desc())
                .limit(pageSize)
                .fetch();
    }

    // Dynamic query helper methods
    private BooleanExpression statusEq(TaskStatus status) {
        return status != null ? task.status.eq(status) : null;
    }

    private BooleanExpression stepIdEq(Long stepId) {
        return stepId != null ? task.stepId.eq(stepId) : null;
    }
}
