package com.ryuqq.crawlinghub.domain.seller.aggregate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeactivatedEvent;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Seller Aggregate forNew 테스트")
class SellerTest {

    @AfterEach
    void tearDown() {
        Seller.changeClock(Clock.systemUTC());
    }

    @Test
    @DisplayName("shouldCreateNewSellerWithValidData")
    void shouldCreateNewSellerWithValidData() {
        MustItSellerId mustItSellerId = MustItSellerId.of(2024L);
        Seller seller = SellerFixture.aNewSeller(mustItSellerId, "머스트잇 스토어");

        assertAll(
            () -> assertTrue(seller.getSellerId().isNew()),
            () -> assertEquals(mustItSellerId, seller.getMustItSellerId()),
            () -> assertEquals("머스트잇 스토어", seller.getSellerName())
        );
    }

    @Test
    @DisplayName("shouldInitializeWithActiveStatus")
    void shouldInitializeWithActiveStatus() {
        Seller seller = SellerFixture.aNewSeller();

        assertEquals(SellerStatus.ACTIVE, seller.getStatus());
    }

    @Test
    @DisplayName("shouldSetCreatedAtAndUpdatedAt")
    void shouldSetCreatedAtAndUpdatedAt() {
        LocalDateTime fixedTime = LocalDateTime.ofInstant(Instant.parse("2025-11-18T00:00:00Z"), ZoneOffset.UTC);
        Seller.changeClock(Clock.fixed(fixedTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

        Seller seller = SellerFixture.aNewSeller();

        assertAll(
            () -> assertEquals(fixedTime, seller.getCreatedAt()),
            () -> assertEquals(fixedTime, seller.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("shouldCreateSellerWithOf")
    void shouldCreateSellerWithOf() {
        LocalDateTime fixedTime = LocalDateTime.ofInstant(Instant.parse("2025-11-19T00:00:00Z"), ZoneOffset.UTC);
        Seller.changeClock(Clock.fixed(fixedTime.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        SellerId sellerId = SellerId.of(10L);
        MustItSellerId mustItSellerId = MustItSellerId.of(3000L);
        String sellerName = "머스트잇 셀러 OF";

        Seller seller = SellerFixture.aSeller(sellerId, mustItSellerId, sellerName, SellerStatus.INACTIVE);

        assertAll(
            () -> assertEquals(sellerId, seller.getSellerId()),
            () -> assertEquals(mustItSellerId, seller.getMustItSellerId()),
            () -> assertEquals(sellerName, seller.getSellerName()),
            () -> assertEquals(SellerStatus.INACTIVE, seller.getStatus()),
            () -> assertFalse(seller.getSellerId().isNew()),
            () -> assertEquals(fixedTime, seller.getCreatedAt()),
            () -> assertEquals(fixedTime, seller.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("shouldReconstituteSellerWithAllFields")
    void shouldReconstituteSellerWithAllFields() {
        SellerId sellerId = SellerId.of(20L);
        MustItSellerId mustItSellerId = MustItSellerId.of(4000L);
        String sellerName = "머스트잇 셀러 RECON";
        LocalDateTime createdAt = LocalDateTime.parse("2025-11-01T00:00:00");
        LocalDateTime updatedAt = LocalDateTime.parse("2025-11-02T12:00:00");

        Seller seller = SellerFixture.aReconstitutedSeller(
            sellerId,
            mustItSellerId,
            sellerName,
            SellerStatus.ACTIVE,
            createdAt,
            updatedAt
        );

        assertAll(
            () -> assertEquals(sellerId, seller.getSellerId()),
            () -> assertEquals(mustItSellerId, seller.getMustItSellerId()),
            () -> assertEquals(sellerName, seller.getSellerName()),
            () -> assertEquals(SellerStatus.ACTIVE, seller.getStatus()),
            () -> assertEquals(createdAt, seller.getCreatedAt()),
            () -> assertEquals(updatedAt, seller.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("shouldDeactivateSellerWhenNoActiveSchedulers")
    void shouldDeactivateSellerWhenNoActiveSchedulers() {
        LocalDateTime createdAt = LocalDateTime.parse("2025-11-20T08:00:00");
        LocalDateTime updatedAt = LocalDateTime.parse("2025-11-20T12:00:00");
        LocalDateTime deactivatedAt = LocalDateTime.parse("2025-11-21T09:30:00");

        Seller seller = SellerFixture.anActiveSeller(createdAt, updatedAt);
        Seller.changeClock(Clock.fixed(deactivatedAt.atZone(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC));
        SellerDeactivatedEvent event = seller.deactivate(0);

        assertAll(
            () -> assertEquals(SellerStatus.INACTIVE, seller.getStatus()),
            () -> assertEquals(createdAt, seller.getCreatedAt()),
            () -> assertEquals(deactivatedAt, seller.getUpdatedAt()),
            () -> assertEquals(seller.getSellerId(), event.sellerId()),
            () -> assertEquals(deactivatedAt, event.occurredAt())
        );
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenActiveSchedulersExist")
    void shouldThrowExceptionWhenActiveSchedulersExist() {
        Seller seller = SellerFixture.anActiveSeller();

        assertThrows(SellerHasActiveSchedulersException.class, () -> seller.deactivate(1));
    }

    @Test
    @DisplayName("shouldPublishEventWhenDeactivated")
    void shouldPublishEventWhenDeactivated() {
        Seller seller = SellerFixture.anActiveSeller();

        SellerDeactivatedEvent event = seller.deactivate(0);

        assertAll(
            () -> assertEquals(seller.getSellerId(), event.sellerId()),
            () -> assertEquals(seller.getUpdatedAt(), event.occurredAt())
        );
    }

    @Test
    @DisplayName("shouldNotPublishEventWhenAlreadyInactive")
    void shouldNotPublishEventWhenAlreadyInactive() {
        Seller seller = SellerFixture.anInactiveSeller();

        SellerDeactivatedEvent event = seller.deactivate(0);

        assertNull(event);
    }

    @Test
    @DisplayName("shouldActivateInactiveSeller")
    void shouldActivateInactiveSeller() {
        LocalDateTime createdAt = LocalDateTime.parse("2025-11-22T10:00:00");
        LocalDateTime updatedAt = LocalDateTime.parse("2025-11-22T12:30:00");
        LocalDateTime activatedAt = LocalDateTime.parse("2025-11-23T09:00:00");

        Seller seller = SellerFixture.anInactiveSeller(createdAt, updatedAt);
        Seller.changeClock(Clock.fixed(activatedAt.atZone(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC));

        seller.activate();

        assertAll(
            () -> assertEquals(SellerStatus.ACTIVE, seller.getStatus()),
            () -> assertEquals(createdAt, seller.getCreatedAt()),
            () -> assertEquals(activatedAt, seller.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("shouldNotChangeAlreadyActiveSeller")
    void shouldNotChangeAlreadyActiveSeller() {
        LocalDateTime createdAt = LocalDateTime.parse("2025-11-24T08:00:00");
        LocalDateTime updatedAt = LocalDateTime.parse("2025-11-24T10:00:00");
        LocalDateTime laterTime = LocalDateTime.parse("2025-11-24T12:00:00");

        Seller seller = SellerFixture.anActiveSeller(createdAt, updatedAt);
        Seller.changeClock(Clock.fixed(laterTime.atZone(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC));

        seller.activate();

        assertAll(
            () -> assertEquals(SellerStatus.ACTIVE, seller.getStatus()),
            () -> assertEquals(createdAt, seller.getCreatedAt()),
            () -> assertEquals(updatedAt, seller.getUpdatedAt())
        );
    }
}

