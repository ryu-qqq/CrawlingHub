package com.ryuqq.crawlinghub.domain.crawler.vo;

/**
 * 크롤러 작업 타입 Enum
 *
 * <p>머스트잇 API 크롤링 작업의 유형을 정의합니다.</p>
 *
 * <p>작업 유형:</p>
 * <ul>
 *   <li>{@link #MINISHOP} - 셀러 상품 목록 크롤링</li>
 *   <li>{@link #PRODUCT_DETAIL} - 상품 상세 정보 크롤링</li>
 *   <li>{@link #PRODUCT_OPTION} - 상품 옵션 정보 크롤링</li>
 * </ul>
 */
public enum CrawlerTaskType {

    /**
     * 셀러 상품 목록 크롤링
     * API: /mustit-api/facade-api/v1/searchmini-shop-search"?sellerId={seller_id}&pageNo={page}&pageSize={page_size}&order=LATEST
     */
    MINISHOP,

    /**
     * 상품 상세 정보 크롤링
     * API: /mustit-api/facade-api/v1/item/{item_no}/detail/top
     */
    PRODUCT_DETAIL,

    /**
     * 상품 옵션 정보 크롤링
     * API: /mustit-api/legacy-api/v1/auction_products/{item_no}/options
     */
    PRODUCT_OPTION;

    /**
     * String 값으로부터 CrawlerTaskType 생성 (표준 패턴)
     *
     * @param value 문자열 값
     * @return CrawlerTaskType enum
     * @throws IllegalArgumentException value가 null이거나 유효하지 않은 경우
     */
    public static CrawlerTaskType of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("CrawlerTaskType cannot be null");
        }
        return valueOf(value.toUpperCase());
    }
}
