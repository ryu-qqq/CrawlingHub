package com.ryuqq.crawlinghub.application.schedule.factory.query;

import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler QueryFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Query → Criteria 변환
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerQueryFactory {

    /**
     * SearchCrawlSchedulersQuery → CrawlSchedulerQueryCriteria 변환
     *
     * @param query 스케줄러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public CrawlSchedulerQueryCriteria createCriteria(SearchCrawlSchedulersQuery query) {
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;

        return new CrawlSchedulerQueryCriteria(
                sellerId, query.status(), query.page(), query.size());
    }
}
