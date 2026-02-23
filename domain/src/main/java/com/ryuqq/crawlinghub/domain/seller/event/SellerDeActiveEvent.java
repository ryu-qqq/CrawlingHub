package com.ryuqq.crawlinghub.domain.seller.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;

/**
 * 셀러 비활성화 이벤트
 *
 * <p><strong>용도</strong>: 셀러가 비활성화될 때 발행하여 크롤링 스케줄을 중지합니다.
 *
 * @param sellerId 비활성화된 셀러 ID
 * @param occurredAt 이벤트 발생 시각
 * @author development-team
 * @since 1.0.0
 */
public record SellerDeActiveEvent(SellerId sellerId, Instant occurredAt) implements DomainEvent {

    /** Compact Constructor (검증 로직) */
    public SellerDeActiveEvent {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt은 null일 수 없습니다.");
        }
    }

    /**
     * 팩토리 메서드 (도메인 규칙)
     *
     * @param sellerId 셀러 ID
     * @param now 현재 시각
     * @return SellerDeActiveEvent
     */
    public static SellerDeActiveEvent of(SellerId sellerId, Instant now) {
        return new SellerDeActiveEvent(sellerId, now);
    }

    public long getSellerIdValue() {
        return sellerId.value();
    }
}
