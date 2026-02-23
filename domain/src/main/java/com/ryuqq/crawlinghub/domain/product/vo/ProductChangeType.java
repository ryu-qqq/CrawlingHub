package com.ryuqq.crawlinghub.domain.product.vo;

/**
 * 상품 변경 유형
 *
 * <p>크롤링 결과 감지된 변경 사항의 유형을 나타냅니다. 외부 서버 동기화 시 변경된 부분만 정확히 동기화하기 위해 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public enum ProductChangeType {
    /** 가격 변경 */
    PRICE,
    /** 이미지 변경 (썸네일 + 상세) */
    IMAGE,
    /** 상세설명 변경 */
    DESCRIPTION,
    /** 옵션/재고 변경 */
    OPTION_STOCK,
    /** 상품 기본정보 (이름, 브랜드, 카테고리, 배송정보 등) */
    PRODUCT_INFO
}
