package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlWorkflow (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
 * - Complex conditions
 * - Joins
 * - Aggregations
 * - Projections
 * - Pagination (Offset-Based and No-Offset)
 */
@Repository
public class CrawlWorkflowQueryRepository {

    private static final QCrawlWorkflowEntity workflow = QCrawlWorkflowEntity.crawlWorkflowEntity;

    private final JPAQueryFactory queryFactory;

    public CrawlWorkflowQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Find all workflows by site ID
     * @param siteId the site ID
     * @return list of workflows for the site
     */
    public List<CrawlWorkflowEntity> findBySiteId(Long siteId) {
        return queryFactory
                .selectFrom(workflow)
                .where(workflow.siteId.eq(siteId))
                .orderBy(workflow.workflowName.asc())
                .fetch();
    }

    /**
     * Find workflows by site ID with Offset-Based pagination
     * @param siteId the site ID
     * @param pageable pagination parameters
     * @return page of workflows for the site
     */
    public Page<CrawlWorkflowEntity> findBySiteId(Long siteId, Pageable pageable) {
        List<CrawlWorkflowEntity> content = queryFactory
                .selectFrom(workflow)
                .where(workflow.siteId.eq(siteId))
                .orderBy(workflow.workflowName.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(workflow.count())
                .from(workflow)
                .where(workflow.siteId.eq(siteId))
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find workflows by site ID with No-Offset cursor-based pagination
     * @param siteId the site ID
     * @param lastWorkflowId last workflow ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of workflows after the cursor
     */
    public List<CrawlWorkflowEntity> findBySiteId(Long siteId, Long lastWorkflowId, int pageSize) {
        return queryFactory
                .selectFrom(workflow)
                .where(
                        workflow.siteId.eq(siteId),
                        lastWorkflowId != null ? workflow.workflowId.gt(lastWorkflowId) : null
                )
                .orderBy(workflow.workflowId.asc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * Find all active workflows
     * @return list of active workflows ordered by workflow name
     */
    public List<CrawlWorkflowEntity> findActiveWorkflows() {
        return queryFactory
                .selectFrom(workflow)
                .where(workflow.isActive.isTrue())
                .orderBy(workflow.workflowName.asc())
                .fetch();
    }

    /**
     * Find active workflows with Offset-Based pagination
     * @param pageable pagination parameters
     * @return page of active workflows
     */
    public Page<CrawlWorkflowEntity> findActiveWorkflows(Pageable pageable) {
        List<CrawlWorkflowEntity> content = queryFactory
                .selectFrom(workflow)
                .where(workflow.isActive.isTrue())
                .orderBy(workflow.workflowName.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(workflow.count())
                .from(workflow)
                .where(workflow.isActive.isTrue())
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find active workflows with No-Offset cursor-based pagination
     * @param lastWorkflowId last workflow ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of active workflows after the cursor
     */
    public List<CrawlWorkflowEntity> findActiveWorkflows(Long lastWorkflowId, int pageSize) {
        return queryFactory
                .selectFrom(workflow)
                .where(
                        workflow.isActive.isTrue(),
                        lastWorkflowId != null ? workflow.workflowId.gt(lastWorkflowId) : null
                )
                .orderBy(workflow.workflowId.asc())
                .limit(pageSize)
                .fetch();
    }

}
