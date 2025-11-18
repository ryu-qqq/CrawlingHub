package com.ryuqq.crawlinghub.domain.seller.event;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.fixture.seller.SellerEventFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SellerDeactivatedEvent 테스트")
class SellerDeactivatedEventTest {

    @Test
    @DisplayName("shouldCreateEventWithSellerIdAndOccurredAt")
    void shouldCreateEventWithSellerIdAndOccurredAt() {
        SellerId sellerId = SellerId.of(99L);
        LocalDateTime occurredAt = LocalDateTime.parse("2025-12-01T08:30:00");

        SellerDeactivatedEvent event = SellerEventFixture.aDeactivatedEvent(sellerId, occurredAt);

        assertAll(
            () -> assertEquals(sellerId, event.sellerId()),
            () -> assertEquals(occurredAt, event.occurredAt())
        );
    }

    @Test
    @DisplayName("shouldValidateEventArguments")
    void shouldValidateEventArguments() {
        LocalDateTime occurredAt = LocalDateTime.parse("2025-12-01T08:30:00");

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> SellerEventFixture.aDeactivatedEvent(null, occurredAt)),
            () -> assertThrows(IllegalArgumentException.class, () -> SellerEventFixture.aDeactivatedEvent(SellerId.of(1L), null))
        );
    }
}

