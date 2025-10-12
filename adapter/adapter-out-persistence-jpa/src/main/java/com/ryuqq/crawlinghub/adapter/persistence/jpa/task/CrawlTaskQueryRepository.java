package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlTask (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
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

    // Dynamic query helper methods
    private BooleanExpression statusEq(TaskStatus status) {
        return status != null ? task.status.eq(status) : null;
    }

    private BooleanExpression stepIdEq(Long stepId) {
        return stepId != null ? task.stepId.eq(stepId) : null;
    }
}
