package com.ryuqq.crawlinghub.domain.fixture.seller;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * Seller Aggregate Fixture.
 */
public final class SellerFixture {

    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.parse("2025-11-18T00:00:00");
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.parse("2025-11-18T00:00:00");
    private static final String DEFAULT_SELLER_NAME = "머스트잇 공식 스토어";

    private SellerFixture() {
    }

    public static Seller aNewSeller() {
        return Seller.forNew(defaultMustItSellerId(), DEFAULT_SELLER_NAME);
    }

    public static Seller aNewSeller(MustItSellerId mustItSellerId, String sellerName) {
        return Seller.forNew(mustItSellerId, sellerName);
    }

    public static Seller aSeller() {
        return Seller.of(defaultSellerId(), defaultMustItSellerId(), DEFAULT_SELLER_NAME, SellerStatus.ACTIVE);
    }

    public static Seller aSeller(
        SellerId sellerId,
        MustItSellerId mustItSellerId,
        String sellerName,
        SellerStatus status
    ) {
        return Seller.of(sellerId, mustItSellerId, sellerName, status);
    }

    public static Seller aReconstitutedSeller() {
        return Seller.reconstitute(
            defaultSellerId(),
            defaultMustItSellerId(),
            DEFAULT_SELLER_NAME,
            SellerStatus.INACTIVE,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    public static Seller anInactiveSeller() {
        return anInactiveSeller(DEFAULT_CREATED_AT, DEFAULT_UPDATED_AT);
    }

    public static Seller anInactiveSeller(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Seller.reconstitute(
            defaultSellerId(),
            defaultMustItSellerId(),
            DEFAULT_SELLER_NAME,
            SellerStatus.INACTIVE,
            createdAt,
            updatedAt
        );
    }

    public static Seller aReconstitutedSeller(
        SellerId sellerId,
        MustItSellerId mustItSellerId,
        String sellerName,
        SellerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return Seller.reconstitute(sellerId, mustItSellerId, sellerName, status, createdAt, updatedAt);
    }

    public static Seller anActiveSeller() {
        return anActiveSeller(DEFAULT_CREATED_AT, DEFAULT_UPDATED_AT);
    }

    public static Seller anActiveSeller(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Seller.reconstitute(
            defaultSellerId(),
            defaultMustItSellerId(),
            DEFAULT_SELLER_NAME,
            SellerStatus.ACTIVE,
            createdAt,
            updatedAt
        );
    }

    public static MustItSellerId defaultMustItSellerId() {
        return MustItSellerId.of(1000L);
    }

    public static SellerId defaultSellerId() {
        return SellerId.of(1L);
    }

}

