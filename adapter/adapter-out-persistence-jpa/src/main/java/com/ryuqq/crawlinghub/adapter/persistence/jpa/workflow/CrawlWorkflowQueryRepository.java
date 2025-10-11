package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlWorkflow (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
 * - Complex conditions
 * - Joins
 * - Aggregations
 * - Projections
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

}
