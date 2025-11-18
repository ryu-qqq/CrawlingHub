package com.ryuqq.crawlinghub.domain.fixture.seller;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.seller.event.SellerDeactivatedEvent;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * Seller Domain Event Fixture.
 */
public final class SellerEventFixture {

    private SellerEventFixture() {
    }

    public static SellerDeactivatedEvent aDeactivatedEvent() {
        return aDeactivatedEvent(SellerId.of(1L), LocalDateTime.parse("2025-11-30T10:00:00"));
    }

    public static SellerDeactivatedEvent aDeactivatedEvent(SellerId sellerId, LocalDateTime occurredAt) {
        return new SellerDeactivatedEvent(sellerId, occurredAt);
    }
}

