package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlSite (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
 * - Complex conditions
 * - Joins
 * - Aggregations
 * - Projections
 */
@Repository
public class CrawlSiteQueryRepository {

    private static final QCrawlSiteEntity site = QCrawlSiteEntity.crawlSiteEntity;

    private final JPAQueryFactory queryFactory;

    public CrawlSiteQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Find all active sites
     * @return list of active sites ordered by site name
     */
    public List<CrawlSiteEntity> findActiveSites() {
        return queryFactory
                .selectFrom(site)
                .where(site.isActive.isTrue())
                .orderBy(site.siteName.asc())
                .fetch();
    }

    /**
     * Check if site name exists
     * @param siteName the site name to check
     * @return true if exists, false otherwise
     */
    public boolean existsBySiteName(String siteName) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(site)
                .where(site.siteName.eq(siteName))
                .fetchFirst();

        return fetchOne != null;
    }

}
