package com.ryuqq.crawlinghub.domain.product.event;

/**
 * 변경 소스 Enum
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public enum ChangeSource {
    /**
     * 미니샵 크롤링 (이미지, 상품명, 가격)
     */
    MINI_SHOP,

    /**
     * 옵션 크롤링 (옵션, 재고)
     */
    OPTION,

    /**
     * 상세 크롤링 (모듈)
     */
    DETAIL
}

