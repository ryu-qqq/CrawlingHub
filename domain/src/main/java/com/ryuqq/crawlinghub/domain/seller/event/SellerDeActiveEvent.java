package com.ryuqq.crawlinghub.domain.seller.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * 셀러 비활성화 이벤트
 *
 * <p><strong>용도</strong>: 셀러가 비활성화될 때 발행하여 크롤링 스케줄을 중지합니다.
 *
 * @param sellerId 비활성화된 셀러 ID
 * @author development-team
 * @since 1.0.0
 */
public record SellerDeActiveEvent(SellerId sellerId) implements DomainEvent {

    public static SellerDeActiveEvent of(SellerId sellerId) {
        return new SellerDeActiveEvent(sellerId);
    }

    /** Compact Constructor (검증 로직) */
    public SellerDeActiveEvent {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
    }

    public long getSellerIdValue() {
        return sellerId.value();
    }
}
