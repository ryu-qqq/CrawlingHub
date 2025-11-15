package com.ryuqq.crawlinghub.domain.vo;

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
     * API: /mustit-api/facade-api/v1/searchmini-shop-search
     */
    MINISHOP,

    /**
     * 상품 상세 정보 크롤링
     * API: /item/{itemNo}/detail/top
     */
    PRODUCT_DETAIL,

    /**
     * 상품 옵션 정보 크롤링
     * API: /auction_products/{itemNo}/options
     */
    PRODUCT_OPTION
}
