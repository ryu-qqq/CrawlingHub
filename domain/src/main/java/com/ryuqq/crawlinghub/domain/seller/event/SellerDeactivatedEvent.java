package com.ryuqq.crawlinghub.domain.seller.event;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller가 비활성화 되었을 때 발행되는 Domain Event.
 */
public record SellerDeactivatedEvent(SellerId sellerId, LocalDateTime occurredAt) implements DomainEvent {

    public SellerDeactivatedEvent {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }
}

