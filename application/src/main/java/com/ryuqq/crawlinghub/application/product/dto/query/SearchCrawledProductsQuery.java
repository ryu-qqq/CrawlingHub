package com.ryuqq.crawlinghub.application.product.dto.query;

/**
 * CrawledProduct 검색 Query
 *
 * <p>크롤링된 상품 목록 조회 조건
 *
 * @param sellerId 판매자 ID (정확히 일치, 선택)
 * @param itemNo 상품 번호 (정확히 일치, 선택)
 * @param itemName 상품명 (부분 일치, LIKE, 선택)
 * @param brandName 브랜드명 (부분 일치, LIKE, 선택)
 * @param needsSync 동기화 필요 여부 (선택)
 * @param allCrawled 모든 크롤링 완료 여부 (선택)
 * @param hasExternalId 외부 상품 ID 존재 여부 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawledProductsQuery(
        Long sellerId,
        Long itemNo,
        String itemName,
        String brandName,
        Boolean needsSync,
        Boolean allCrawled,
        Boolean hasExternalId,
        Integer page,
        Integer size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public SearchCrawledProductsQuery {
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
}
