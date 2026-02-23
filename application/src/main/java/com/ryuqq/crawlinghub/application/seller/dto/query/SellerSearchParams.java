package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import java.time.Instant;
import java.util.List;

/**
 * 셀러 검색 파라미터 (CommonSearchParams 합성)
 *
 * @param mustItSellerName 머스트잇 셀러명 필터 (optional)
 * @param sellerName 셀러명 필터 (optional)
 * @param statuses 상태 필터 문자열 목록 (optional)
 * @param createdFrom 생성일 시작 (optional)
 * @param createdTo 생성일 종료 (optional)
 * @param searchParams 공통 검색 파라미터 (정렬, 페이징)
 * @author development-team
 * @since 1.0.0
 */
public record SellerSearchParams(
        String mustItSellerName,
        String sellerName,
        List<String> statuses,
        Instant createdFrom,
        Instant createdTo,
        CommonSearchParams searchParams) {

    public SellerSearchParams {
        statuses = statuses != null ? List.copyOf(statuses) : null;
        if (searchParams == null) {
            searchParams = CommonSearchParams.of(null, null, null, null, null, null, null);
        }
    }

    public static SellerSearchParams of(
            String mustItSellerName,
            String sellerName,
            List<String> statuses,
            Instant createdFrom,
            Instant createdTo,
            CommonSearchParams searchParams) {
        return new SellerSearchParams(
                mustItSellerName, sellerName, statuses, createdFrom, createdTo, searchParams);
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
