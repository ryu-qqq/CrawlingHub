package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.condition;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchField;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerConditionBuilder - 검색 조건 빌더
 *
 * <p>CrawlSchedulerSearchCriteria를 BooleanExpression으로 변환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerConditionBuilder {

    private static final QCrawlSchedulerJpaEntity qScheduler =
            QCrawlSchedulerJpaEntity.crawlSchedulerJpaEntity;

    /**
     * 셀러 ID 조건
     *
     * @param criteria 검색 조건
     * @return BooleanExpression (조건 없으면 null)
     */
    public BooleanExpression sellerIdEq(CrawlSchedulerSearchCriteria criteria) {
        if (!criteria.hasSellerFilter()) {
            return null;
        }
        return qScheduler.sellerId.eq(criteria.sellerId().value());
    }

    /**
     * 상태 IN 조건
     *
     * @param criteria 검색 조건
     * @return BooleanExpression (조건 없으면 null)
     */
    public BooleanExpression statusIn(CrawlSchedulerSearchCriteria criteria) {
        if (!criteria.hasStatusFilter()) {
            return null;
        }
        return qScheduler.status.in(criteria.statuses());
    }

    /**
     * 검색어 LIKE 조건 (schedulerName)
     *
     * @param criteria 검색 조건
     * @return BooleanExpression (조건 없으면 null)
     */
    public BooleanExpression searchCondition(CrawlSchedulerSearchCriteria criteria) {
        if (!criteria.hasSearchCondition()) {
            return null;
        }
        if (criteria.searchField() == CrawlSchedulerSearchField.SCHEDULER_NAME) {
            return qScheduler.schedulerName.containsIgnoreCase(criteria.searchWord());
        }
        return null;
    }
}
