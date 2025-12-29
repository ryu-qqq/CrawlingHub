package com.ryuqq.crawlinghub.domain.seller.vo;

import java.time.Instant;
import java.util.List;

/**
 * Seller Query Criteria
 *
 * <p>셀러 조회 조건 VO
 *
 * @param mustItSellerName 머스트잇 셀러명 (부분 일치 검색)
 * @param sellerName 셀러명 (부분 일치 검색)
 * @param statuses 셀러 상태 목록 (다중 선택 가능)
 * @param createdFrom 생성일 시작
 * @param createdTo 생성일 종료
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SellerQueryCriteria(
        MustItSellerName mustItSellerName,
        SellerName sellerName,
        List<SellerStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {

    public SellerQueryCriteria {
        statuses = statuses != null ? List.copyOf(statuses) : null;
    }

    /** 상태 필터 여부 (다중 상태) */
    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    /** 기간 필터 여부 */
    public boolean hasDateFilter() {
        return createdFrom != null || createdTo != null;
    }

    /**
     * 단일 상태 반환 (하위 호환성)
     *
     * @return 첫 번째 상태 또는 null
     */
    public SellerStatus status() {
        return hasStatusFilter() ? statuses.get(0) : null;
    }
}
