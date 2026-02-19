package com.ryuqq.crawlinghub.application.schedule.factory.query;

import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.domain.common.vo.DateRange;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
     * SearchCrawlSchedulersQuery → CrawlSchedulerPageCriteria 변환
     *
     * @param query 스케줄러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public CrawlSchedulerPageCriteria createCriteria(SearchCrawlSchedulersQuery query) {
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;
        DateRange dateRange = toDateRange(query.createdFrom(), query.createdTo());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size());

        return CrawlSchedulerPageCriteria.of(sellerId, query.statuses(), dateRange, pageRequest);
    }

    private DateRange toDateRange(Instant from, Instant to) {
        if (from == null && to == null) {
            return null;
        }
        LocalDate startDate =
                from != null ? LocalDate.ofInstant(from, ZoneId.systemDefault()) : null;
        LocalDate endDate = to != null ? LocalDate.ofInstant(to, ZoneId.systemDefault()) : null;
        return DateRange.of(startDate, endDate);
    }
}
