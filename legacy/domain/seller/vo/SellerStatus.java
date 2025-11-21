package com.ryuqq.crawlinghub.domain.seller.vo;

/**
 * 판매자의 활성화 상태를 표현하는 Enum입니다.
 *
 * <p>Aggregate 내부 상태 전이를 통해서만 변경되어야 하며, 외부에서는 값 조회만 허용됩니다.
 */
public enum SellerStatus {
    /** 판매자가 정상적으로 상품을 수집 중인 상태 */
    ACTIVE,

    /** 판매자가 비활성화되어 스케줄러가 동작하지 않는 상태 */
    INACTIVE
}
