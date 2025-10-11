package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Query Repository for CrawlSite (CQRS Pattern)
 * Handles all complex read operations using QueryDSL
 * - Complex conditions
 * - Joins
 * - Aggregations
 * - Projections
 * - Pagination (Offset-Based and No-Offset)
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
     * Find active sites with Offset-Based pagination
     * @param pageable pagination parameters
     * @return page of active sites
     */
    public Page<CrawlSiteEntity> findActiveSites(Pageable pageable) {
        List<CrawlSiteEntity> content = queryFactory
                .selectFrom(site)
                .where(site.isActive.isTrue())
                .orderBy(site.siteName.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(site.count())
                .from(site)
                .where(site.isActive.isTrue())
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Find active sites with No-Offset cursor-based pagination
     * @param lastSiteId last site ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of active sites after the cursor
     */
    public List<CrawlSiteEntity> findActiveSites(Long lastSiteId, int pageSize) {
        return queryFactory
                .selectFrom(site)
                .where(
                        site.isActive.isTrue(),
                        lastSiteId != null ? site.siteId.gt(lastSiteId) : null
                )
                .orderBy(site.siteId.asc())
                .limit(pageSize)
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
