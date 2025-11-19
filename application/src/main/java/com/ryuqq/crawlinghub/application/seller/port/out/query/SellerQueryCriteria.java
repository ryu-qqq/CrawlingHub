package com.ryuqq.crawlinghub.application.seller.port.out.query;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * Seller 조회 조건 Value Object.
 *
 * @param status        필터링할 상태
 * @param sellerName    셀러명 키워드
 * @param mustItSellerId 외부 시스템 셀러 ID
 */
public record SellerQueryCriteria(
    SellerStatus status,
    String sellerName,
    Long mustItSellerId
) {
}

