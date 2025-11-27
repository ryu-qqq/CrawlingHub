package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 크롤링 타입
 *
 * <p>크롤링 결과의 유형을 구분합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum CrawlType {

    /**
     * 미니샵 목록 크롤링 - 기본 상품 정보
     */
    MINI_SHOP,

    /**
     * 상품 상세 크롤링 - 카테고리, 배송, 상세 이미지 등
     */
    DETAIL,

    /**
     * 옵션 크롤링 - 색상, 사이즈, 재고
     */
    OPTION
}
