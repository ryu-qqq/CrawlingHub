package com.ryuqq.crawlinghub.domain.schedule.query;

import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;

/**
 * 크롤 스케줄러 검색 조건 (QueryContext 기반)
 *
 * <p>공통 인프라(QueryContext, SortKey, SearchField)를 활용한 표준 검색 조건입니다.
 *
 * @param sellerId 셀러 ID (optional, null이면 전체 조회)
 * @param statuses 스케줄러 상태 필터 목록 (optional)
 * @param searchField 검색 필드 (optional)
 * @param searchWord 검색어 (optional)
 * @param queryContext 정렬 + 페이징 컨텍스트
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerSearchCriteria(
        SellerId sellerId,
        List<SchedulerStatus> statuses,
        CrawlSchedulerSearchField searchField,
        String searchWord,
        QueryContext<CrawlSchedulerSortKey> queryContext) {

    public CrawlSchedulerSearchCriteria {
        statuses = statuses != null ? List.copyOf(statuses) : null;
        if (queryContext == null) {
            throw new IllegalArgumentException("queryContext must not be null");
        }
    }

    public static CrawlSchedulerSearchCriteria of(
            SellerId sellerId,
            List<SchedulerStatus> statuses,
            CrawlSchedulerSearchField searchField,
            String searchWord,
            QueryContext<CrawlSchedulerSortKey> queryContext) {
        return new CrawlSchedulerSearchCriteria(
                sellerId, statuses, searchField, searchWord, queryContext);
    }

    public boolean hasSellerFilter() {
        return sellerId != null;
    }

    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    public boolean hasSearchCondition() {
        return searchField != null && searchWord != null && !searchWord.isBlank();
    }

    public boolean hasSearchField() {
        return searchField != null;
    }

    public long offset() {
        return queryContext.offset();
    }

    public int size() {
        return queryContext.size();
    }

    public int page() {
        return queryContext.page();
    }
}
