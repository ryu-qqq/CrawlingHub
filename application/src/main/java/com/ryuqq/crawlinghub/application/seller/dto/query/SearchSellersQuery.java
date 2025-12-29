package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.List;

/**
 * Search Sellers Query
 *
 * <p>셀러 목록 조회 조건
 *
 * @param mustItSellerName 머스트잇 셀러 이름 (부분 일치 검색, 선택)
 * @param sellerName 셀러 이름 (부분 일치 검색, 선택)
 * @param sellerStatuses 셀러 상태 목록 (다중 선택 가능, 선택)
 * @param createdFrom 생성일 시작 (선택)
 * @param createdTo 생성일 종료 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchSellersQuery(
        String mustItSellerName,
        String sellerName,
        List<SellerStatus> sellerStatuses,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {

    public SearchSellersQuery {
        sellerStatuses = sellerStatuses != null ? List.copyOf(sellerStatuses) : null;
    }

    /**
     * 상태 필터가 있는지 확인
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return sellerStatuses != null && !sellerStatuses.isEmpty();
    }

    /**
     * 기간 필터가 있는지 확인
     *
     * @return 기간 필터가 있으면 true
     */
    public boolean hasDateFilter() {
        return createdFrom != null || createdTo != null;
    }

    /**
     * 단일 상태 반환 (하위 호환성)
     *
     * @return 첫 번째 상태 또는 null
     */
    public SellerStatus sellerStatus() {
        return hasStatusFilter() ? sellerStatuses.get(0) : null;
    }
}
