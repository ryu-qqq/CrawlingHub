package com.ryuqq.crawlinghub.application.product.dto.query;

import java.time.Instant;
import java.util.List;

/**
 * CrawledProduct 검색 Query
 *
 * <p>크롤링된 상품 목록 조회 조건
 *
 * @param sellerId 판매자 ID (정확히 일치, 선택)
 * @param itemNos 상품 번호 목록 (다중 선택 가능, 선택)
 * @param itemName 상품명 (부분 일치, LIKE, 선택)
 * @param brandName 브랜드명 (부분 일치, LIKE, 선택)
 * @param minPrice 최소 가격 (선택)
 * @param maxPrice 최대 가격 (선택)
 * @param needsSync 동기화 필요 여부 (선택)
 * @param allCrawled 모든 크롤링 완료 여부 (선택)
 * @param hasExternalId 외부 상품 ID 존재 여부 (선택)
 * @param createdFrom 생성일시 시작 (선택)
 * @param createdTo 생성일시 종료 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawledProductsQuery(
        Long sellerId,
        List<Long> itemNos,
        String itemName,
        String brandName,
        Long minPrice,
        Long maxPrice,
        Boolean needsSync,
        Boolean allCrawled,
        Boolean hasExternalId,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {

    private static final Integer DEFAULT_PAGE = Integer.valueOf(0);
    private static final Integer DEFAULT_SIZE = Integer.valueOf(20);

    public SearchCrawledProductsQuery {
        itemNos = itemNos != null ? List.copyOf(itemNos) : null;
        page = (page == null) ? DEFAULT_PAGE : page;
        size = (size == null) ? DEFAULT_SIZE : size;
    }

    /**
     * 페이지 오프셋 계산
     *
     * @return 페이지 오프셋
     */
    public long getOffset() {
        return (long) page * size;
    }

    /**
     * 상품 번호 필터 여부
     *
     * @return 상품 번호 필터가 있으면 true
     */
    public boolean hasItemNosFilter() {
        return itemNos != null && !itemNos.isEmpty();
    }

    /**
     * 단일 상품 번호 반환 (하위 호환성)
     *
     * @return 첫 번째 상품 번호 또는 null
     */
    public Long itemNo() {
        return hasItemNosFilter() ? itemNos.get(0) : null;
    }

    /**
     * 기간 필터 여부
     *
     * @return 기간 필터가 있으면 true
     */
    public boolean hasDateFilter() {
        return createdFrom != null || createdTo != null;
    }

    /**
     * 가격 범위 필터 여부
     *
     * @return 가격 범위 필터가 있으면 true
     */
    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
}
