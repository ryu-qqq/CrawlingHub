package com.ryuqq.crawlinghub.application.schedule.dto.query;

import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import java.util.List;

/**
 * 크롤 스케줄러 검색 파라미터 (CommonSearchParams 합성)
 *
 * @param sellerId 셀러 ID (optional)
 * @param statuses 상태 필터 문자열 목록 (optional)
 * @param searchField 검색 필드 (optional)
 * @param searchWord 검색어 (optional)
 * @param searchParams 공통 검색 파라미터 (정렬, 페이징)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerSearchParams(
        Long sellerId,
        List<String> statuses,
        String searchField,
        String searchWord,
        CommonSearchParams searchParams) {

    public CrawlSchedulerSearchParams {
        statuses = statuses != null ? List.copyOf(statuses) : null;
        if (searchParams == null) {
            searchParams = CommonSearchParams.of(null, null, null, null, null, null, null);
        }
    }

    public static CrawlSchedulerSearchParams of(
            Long sellerId,
            List<String> statuses,
            String searchField,
            String searchWord,
            CommonSearchParams searchParams) {
        return new CrawlSchedulerSearchParams(
                sellerId, statuses, searchField, searchWord, searchParams);
    }

    public Integer page() {
        return searchParams.page();
    }

    public Integer size() {
        return searchParams.size();
    }

    public String sortKey() {
        return searchParams.sortKey();
    }

    public String sortDirection() {
        return searchParams.sortDirection();
    }
}
